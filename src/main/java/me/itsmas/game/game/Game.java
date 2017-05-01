package me.itsmas.game.game;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import me.itsmas.game.GameManager;
import me.itsmas.game.game.phase.GamePhase;
import me.itsmas.game.game.phase.PregamePhase;
import me.itsmas.game.game.phase.phases.lobby.LobbyCountdownPhase;
import me.itsmas.game.game.phase.phases.lobby.LobbyPhase;
import me.itsmas.game.game.phase.phases.lobby.LobbyWaitingPhase;
import me.itsmas.game.map.GameMap;
import me.itsmas.game.util.Action;
import me.itsmas.game.util.C;
import me.itsmas.game.util.Utils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The representation of a Game
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class Game implements Listener
{
    /**
     * The plugin instance
     */
    private final GameManager manager;

    /**
     * Gets the plugin instance
     * @return The plugin instance
     */
    public final GameManager getManager()
    {
        return manager;
    }

    /**
     * The GameType of this game
     */
    private final GameType type;

    /**
     * Gets the {@link GameType} of the current game
     * @return The current game's type
     */
    public final GameType getType()
    {
        return type;
    }

    /**
     * The map the game is taking place on
     */
    private final GameMap map;

    /**
     * Gets the map for this game
     * @return This game's map
     */
    public final GameMap getMap()
    {
        return map;
    }

    /**
     * Game constructor
     * @param manager The plugin instance
     * @param type The {@link GameType} of this game
     */
    public Game(GameManager manager, GameType type)
    {
        this(manager, type, true);
    }

    public GameMode gameMode = GameMode.SURVIVAL;

    public boolean deathDropItems = true;

    public boolean blockBreak = false;
    public boolean blockPlace = false;

    public boolean itemDrop = false;

    public boolean entitySpawn = false;

    public boolean damage = false;
    public boolean pvp = false;

    private boolean hunger = false;

    public String resourcePackUrl = null;
    public boolean forceResourcePack = false;

    /**
     * Game constructor
     * @param manager The plugin instance
     * @param type The {@link GameType} of this game
     * @param defaultPhases Whether to use the default phases (waiting and countdown)
     */
    public Game(GameManager manager, GameType type, boolean defaultPhases)
    {
        this.manager = manager;
        this.type = type;

        this.map = manager.getMapManager().getSuitableMap(type);

        if (defaultPhases)
        {
            phases = Lists.newArrayList(
                new LobbyWaitingPhase(this),
                new LobbyCountdownPhase(this),
                new PregamePhase(this)
            );
        }

        assignPhase();

        Utils.register(this);
    }

    private boolean ending = false;

    /**
     * Ends the game by kicking all players and unloading the map world
     * Calls GameManager#restart to restart the server.
     * @see #map
     * @see GameManager#restart()
     * @param winners The players who won the game
     */
    public void endGame(Player... winners)
    {
        ending = true;

        currentPhase.endPhase();
        Utils.unregister(this);

        Set<Player> gameWinners = Sets.newHashSet(winners);

        for (Player player : getPlayers(false))
        {
            if (gameWinners.contains(player))
            {
                player.sendTitle(C.GOLD + C.BOLD + "WINNER", C.GREEN + "You won the game!", 10, 30, 10);

                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1.2F);

                player.setGlowing(true);
            }
            else
            {
                player.sendTitle(C.RED + C.BOLD + "LOSS", C.GRAY + "You lost the game", 10, 30, 10);

                player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_GROWL, 1F, 1.5F);
            }
        }

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                forEachPlayer(player -> player.kickPlayer(C.GREEN + C.BOLD + "The game is now over, thanks for playing!"));
                unloadWorld();

                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        manager.restart();
                    }
                }.runTaskLater(manager, 10L);
            }
        }.runTaskLater(manager, 100L);
    }

    /**
     * Unloads the world this game takes place on
     */
    public final void unloadWorld()
    {
        Bukkit.unloadWorld(getMap().getWorld(), false);
    }


    /**
     * Broadcasts a message in chat announcing the winners
     * @param winners The winning players
     */
    private void broadcastWinners(Set<Player> winners)
    {
        if (winners.isEmpty())
        {
            return;
        }

        StringBuilder builder = new StringBuilder(C.GOLD + C.BOLD + "Winners: " + C.GREEN);

        winners.forEach(winner -> builder.append(winner.getName()).append(", "));

        String raw = builder.toString();
        String winnersString = raw.substring(0, raw.length() - 1);

        broadcast(winnersString);
    }

    /**
     * Whether the game is currently in progress
     * @return Whether the game is in progress
     */
    public boolean inProgress()
    {
        return currentPhase.isIngame();
    }

    /**
     * Gets whether the current {@link GamePhase} is a lobby phase
     * @return Whether the game is currently joinable
     */
    boolean isLobby()
    {
        return currentPhase instanceof LobbyPhase;
    }

    /**
     * Gets whether the game is currently full
     * @return Whether the game is full
     */
    boolean isFull()
    {
        return getPlayersSize() >= getType().getMaxPlayers();
    }

    /**
     * Gets whether the game is currently joinable
     * @return Whether the game is currently joinable
     */
    private boolean isJoinable()
    {
        return isLobby() && !isFull();
    }

    /**
     * Handles a player joining the game
     * @throws AssertionError If the game is not in a lobby phase or is full
     * @see #isLobby()
     * @see #isFull()
     * @param player The player who is joining
     */
    void handleJoin(Player player)
    {
        assert isJoinable();

        players.add(player.getUniqueId());

        if (resourcePackUrl != null)
        {
            player.sendMessage(
                    forceResourcePack ?
                            C.RED + C.BOLD + "This game requires the use of a custom resource pack" :
                            C.GREEN + "This game recommends the use of a custom resource pack"
            );

            player.setResourcePack(resourcePackUrl);
        }

        broadcastJoinOrLeaveMessage(player, "joined");

        ((LobbyPhase) currentPhase).addPlayer(player);
    }

    /**
     * Handles a player leaving the game
     * @param player The player who is leaving
     */
    public final void handleLeave(Player player)
    {
        if (players.remove(player.getUniqueId()))
        {
            if (isLobby())
            {
                broadcastJoinOrLeaveMessage(player, "left");
            }
            else
            {
                broadcast(C.YELLOW + player.getName() + C.GREEN + " left the game");
            }
        }
        else
        {
            spectators.remove(player.getUniqueId());
        }
    }

    /**
     * Broadcasts a "%player% joined/left the game" message
     * @param player The player joining or leaving
     * @param action The action the player performed (joined/left)
     */
    private void broadcastJoinOrLeaveMessage(Player player, String action)
    {
        assert action.equals("joined") || action.equals("left");

        broadcast(C.YELLOW + player.getName() + C.GREEN + " " + action + " the game " + C.YELLOW + "(" + getPlayersSize() + "/" + getType().getMaxPlayers() + ")");

        if (getPlayersSize() <= getType().getMinPlayers())
        {
            broadcast(C.YELLOW + "Players needed: " + C.GREEN + (getType().getMinPlayers() - getPlayersSize()));
        }
    }

    /**
     * Eliminates a player from the game
     * @param target The player to be eliminated
     */
    protected void eliminate(Player target)
    {
        eliminate(target, null);
    }

    /**
     * Eliminates a player from the game
     * @param target The player to be eliminated
     * @param slayer The player responsible for this player's elimination
     */
    protected void eliminate(Player target, Player slayer)
    {
        UUID id = target.getUniqueId();

        players.remove(id);
        spectators.add(id);

        target.setHealth(20);

        target.getInventory().clear();

        forEachPlayer(other -> other.hidePlayer(target));

        target.sendTitle(C.RED + C.BOLD + "Eliminated", C.GRAY + "You are now spectating", 10, 30, 10);
        target.sendMessage(C.RED + C.BOLD + "You were eliminated from the game");

        broadcast(C.YELLOW + target.getName() + C.YELLOW + C.RED + " was eliminated" + (slayer == null ? "" : " by " + slayer.getName()));

        forEachPlayer(player -> player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_IMPACT, 1F, 2F));

        target.setGameMode(GameMode.SPECTATOR);
        target.getVelocity().multiply(new Vector(0, 0.5, 0));
    }

    /**
     * Gets whether a player is playing the game
     * @param player The player to test
     * @return Whether the player is playing
     */
    public final boolean isPlaying(Player player)
    {
        return players.contains(player.getUniqueId());
    }

    /**
     * Gets whether a player is spectating the game
     * @param player The player to test
     * @return Whether the player is spectating
     */
    private boolean isSpectating(Player player)
    {
        return spectators.contains(player.getUniqueId());
    }

    /**
     * The phases of this game
     */
    private List<GamePhase> phases;

    /**
     * Adds a phase to this game
     * @param phase The phase to add
     */
    protected final void addPhase(GamePhase phase)
    {
        phases.add(phase);
    }

    /**
     * Moves the game onto the next phase
     */
    public final void nextPhase()
    {
        if (!ending)
        {
            phaseIndex++;

            if (phaseIndex > phases.size() - 1)
            {
                endGame();
                return;
            }

            assignPhase();
        }
    }

    /**
     * Reverts the game to the last phase
     */
    public final void previousPhase()
    {
        phaseIndex--;

        assignPhase();
    }

    /**
     * Sets the current phase
     */
    private void assignPhase()
    {
        currentPhase = phases.get(phaseIndex);

        currentPhase.startPhase();
    }

    /**
     * The index of the current game phase
     */
    private int phaseIndex = 0;

    /**
     * The current game phase
     */
    private GamePhase currentPhase;

    /**
     * Reverts this game's phase to a lobby phase
     * <b>Only to be used when a countdown is cancelled after a player leaves</b>
     */
    public final void revertToLobby()
    {
        phaseIndex = 0;

        assignPhase();
    }

    /**
     * The players in the game
     */
    private final Set<UUID> players = Sets.newHashSet();

    /**
     * Gets the amount of players in the game
     * @return The amount of players
     */
    public final int getPlayersSize()
    {
        return players.size();
    }

    /**
     * The players spectating the game
     */
    private final Set<UUID> spectators = Sets.newHashSet();

    /**
     * Broadcasts a message to all players and spectators in the game
     * @param msg The message to broadcast
     */
    public void broadcast(String msg)
    {
        broadcast(msg, true);
    }

    /**
     * Broadcasts a message to all players and spectators in the game
     * @param msg The message to broadcast
     * @param spectators Whether to send the message to spectators
     */
    public void broadcast(String msg, boolean spectators)
    {
        Set<Player> players = getPlayers(spectators);

        players.forEach(player -> player.sendMessage(msg));
    }

    /**
     * Executes an {@link Action} for every player in the game
     * @param action The action to run
     */
    public void forEachPlayer(Action<Player> action)
    {
        forEachPlayer(action, true);
    }

    /**
     * Executes an {@link Action} for every player in the game
     * @param action The action to run
     * @param spectators Whether to execute the action on spectators
     */
    public void forEachPlayer(Action<Player> action, boolean spectators)
    {
        Set<Player> players = getPlayers(spectators);

        players.forEach(action::run);
    }

    /**
     * Gets a set of all players in the game
     * @return The set of players
     */
    public Set<Player> getPlayers()
    {
        return getPlayers(true);
    }

    /**
     * Gets a set of players in the game
     * @param spectators Whether to include spectators in the set
     * @return The set of players in the game
     */
    public  Set<Player> getPlayers(boolean spectators)
    {
        Set<Player> players;

        players =
                spectators ?
                        Utils.onlinePlayerSet(this.players, this.spectators) :
                        Utils.onlinePlayerSet(this.players);

        return players;
    }

    /* Events */
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event)
    {
        Player player = event.getPlayer();

        if (isSpectating(player) && !player.isOp())
        {
            event.setCancelled(true);
            player.sendMessage(C.GRAY + "Spectators may not talk");
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onCommandProcess(PlayerCommandPreprocessEvent event)
    {
        Player player = event.getPlayer();
        String command = event.getMessage().split(" ")[0].substring(1);

        if (isSpectating(player))
        {
            if (!player.isOp() && !command.equalsIgnoreCase("leave"))
            {
                event.setCancelled(true);

                player.sendMessage(C.GRAY + "You cannot use this command as a spectator");
                player.sendMessage(C.GRAY + "Type " + C.GREEN + "/leave " + C.GRAY + " to leave the game");

                return;
            }

            player.kickPlayer(C.GREEN + C.BOLD + "Thanks for playing!");
        }
    }

    @EventHandler
    public void onDeathDamage(EntityDamageEvent event)
    {
        if (event.getEntity() instanceof Player)
        {
            Player player = (Player) event.getEntity();

            if (deathDropItems && player.getHealth() - event.getDamage() <= 0.0D)
            {
                // The player would die from this hit
                event.setDamage(0);

                World world = player.getWorld();
                Location deathLoc = player.getLocation().clone();
                ItemStack[] items = player.getInventory().getContents().clone();

                Player killer = null;

                // Eliminate the player and drop their items
                if (event instanceof EntityDamageByEntityEvent)
                {
                    EntityDamageByEntityEvent edbe = (EntityDamageByEntityEvent) event;

                    if (edbe.getDamager() instanceof Player)
                    {
                        killer = (Player) edbe.getDamager();
                    }
                }

                eliminate(player, killer);

                Arrays.stream(items).forEach(item -> world.dropItemNaturally(deathLoc, item));
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event)
    {
        if (!deathDropItems)
        {
            event.setKeepInventory(true);
        }

        event.setDeathMessage(null);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event)
    {
        if (!blockPlace)
        {
            event.setBuild(false);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        if (!blockBreak)
        {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event)
    {
        if (!itemDrop)
        {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event)
    {
        if (!damage)
        {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event)
    {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player)
        {
            if (!pvp)
            {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event)
    {
        if (!entitySpawn)
        {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event)
    {
        if (!hunger)
        {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event)
    {
        Player player = event.getPlayer();
        Status status = event.getStatus();

        if (status == Status.ACCEPTED)
        {
            player.sendMessage(C.GREEN + "Sending you the resource pack...");
        }
        else if (status == Status.SUCCESSFULLY_LOADED)
        {
            player.sendMessage(C.GREEN + "The resource pack was loaded successfully!");
        }
        else if (status == Status.DECLINED)
        {
            if (forceResourcePack)
            {
                player.kickPlayer(C.RED + "You must accept the resource pack to play this game");
            }
        }
        else if (status == Status.FAILED_DOWNLOAD && forceResourcePack)
        {
            player.kickPlayer(C.RED + "There was an error downloading the required resource pack");
        }
    }
}
