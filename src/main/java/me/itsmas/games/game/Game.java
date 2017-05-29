package me.itsmas.games.game;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import me.itsmas.games.GameManager;
import me.itsmas.games.game.phase.GamePhase;
import me.itsmas.games.game.phase.phases.game.PregamePhase;
import me.itsmas.games.game.phase.phases.lobby.LobbyCountdownPhase;
import me.itsmas.games.game.phase.phases.lobby.LobbyPhase;
import me.itsmas.games.game.phase.phases.lobby.LobbyWaitingPhase;
import me.itsmas.games.game.team.TeamGame;
import me.itsmas.games.map.GameMap;
import me.itsmas.games.util.Action;
import me.itsmas.games.util.C;
import me.itsmas.games.util.Utils;
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

import java.util.*;

/**
 * The representation of a Game
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class Game implements Listener
{
    public static final String SOLO_TEAM = "Players";

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
     * @param type The {@link GameType} of this
     */
    public Game(GameManager manager, GameType type)
    {
        this(manager, type, true);
    }

    public GameMode gameMode = GameMode.SURVIVAL;

    public boolean countdownTitle = true;

    public boolean joinLeaveMessages = true;

    public boolean preGameFreeze = true;

    public boolean specChat = false;
    public boolean specCmd = false;

    public boolean niceDeath = true;

    public boolean winnerGlow = true;

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

    /**
     * Whether the game is ending
     */
    private boolean ending = false;

    /**
     * Ends the game
     * @see #endGame(Player...)
     * @param players The winning players
     */
    public void endGame(Collection<Player> players)
    {
        endGame(players.toArray(new Player[0]));
    }

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
                String[] winnerMsg = getWinnerTitle();

                if (winnerMsg != null && winnerMsg.length == 2)
                {
                    player.sendTitle(winnerMsg[0], winnerMsg[1], 10, 30, 10);
                }

                Sound winnerSound = getWinnerSound();

                if (winnerSound != null)
                {
                    player.playSound(player.getLocation(), winnerSound, 1F, 2F);
                }

                if (winnerGlow)
                {
                    player.setGlowing(true);
                }
            }
            else
            {
                String[] loserMsg = getLoserTitle();

                if (loserMsg != null && loserMsg.length == 2)
                {
                    player.sendTitle(loserMsg[0], loserMsg[1], 10, 30, 10);
                }

                Sound loserSound = getLoserSound();

                if (loserSound != null)
                {
                    player.playSound(player.getLocation(), loserSound, 1F, 1F);
                }
            }
        }

        resetTeams();

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                forEachPlayer(player -> player.kickPlayer(C.GREEN + C.BOLD + "The game is over, thanks for playing!"));
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
     * Clears all scoreboard teams
     */
    private void resetTeams()
    {
        Bukkit.getScoreboardManager().getMainScoreboard().getTeams().forEach(team -> team.getEntries().forEach(team::removeEntry));
    }

    /**
     * Gets the title message displayed to the winners of a game
     * <b>Must return an array with length 2</b>
     * @return The title and subtitle displayed to winners
     */
    protected String[] getWinnerTitle()
    {
        return new String[] {C.GOLD + C.BOLD + "WINNER", C.GREEN + "You won the game!"};
    }

    /**
     * Gets the sound played to winners of a game
     * @return The sound played
     */
    protected Sound getWinnerSound()
    {
        return Sound.ENTITY_PLAYER_LEVELUP;
    }

    /**
     * Gets the title message displayed to losers of a game
     * <b>Must return an array with length 2</b>
     * @return The title and subtitle displayed to losers
     */
    protected String[] getLoserTitle()
    {
        return new String[] {C.RED + C.BOLD + "LOSS", C.GRAY + "You lost the game"};
    }

    /**
     * Gets the sound played to losers of a game
     * @return The sound played
     */
    protected Sound getLoserSound()
    {
        return Sound.ENTITY_ENDERDRAGON_GROWL;
    }

    /**
     * Unloads the world this game is taking place on
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

        winners.forEach(winner -> builder.append(winner.getDisplayName()).append(", "));

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

        if (joinLeaveMessages)
        {
            broadcastJoinOrLeaveMessage(player, "joined");
        }

        ((LobbyPhase) currentPhase).addPlayer(player);
    }

    /**
     * Handles a player leaving the game
     * @param player The player who is leaving
     */
    public final void handleLeave(Player player)
    {
        removeFromTeam(player);

        if (players.remove(player.getUniqueId()))
        {
            if (joinLeaveMessages)
            {
                if (isLobby())
                {
                    broadcastJoinOrLeaveMessage(player, "left");
                }
                else
                {
                    broadcast(C.YELLOW + player.getDisplayName() + C.GREEN + " left the game");
                }
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

        broadcast(C.YELLOW + player.getDisplayName() + C.GREEN + " " + action + " the game " + C.YELLOW + "(" + getPlayersSize() + "/" + getType().getMaxPlayers() + ")");

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
     * Removes a player from their team in the game
     * @param player The player to remove
     */
    private void removeFromTeam(Player player)
    {
        if (this instanceof TeamGame)
        {
            ((TeamGame) this).getTeam(player).removePlayer(player);
        }
    }

    /**
     * Eliminates a player from the game
     * @param target The player to be eliminated
     * @param slayer The player responsible for this player's elimination
     */
    protected void eliminate(Player target, Player slayer)
    {
        if (!inProgress())
        {
            return;
        }

        removeFromTeam(target);

        UUID id = target.getUniqueId();

        players.remove(id);
        spectators.add(id);

        target.setHealth(20);

        target.getInventory().clear();

        Bukkit.getOnlinePlayers().stream().filter(pl -> !pl.equals(target)).forEach(pl -> pl.hidePlayer(target));
        forEachPlayer(other -> other.hidePlayer(target));

        target.sendTitle(C.RED + C.BOLD + "Eliminated", C.GRAY + "You are now spectating", 10, 30, 10);
        target.sendMessage(C.RED + C.BOLD + "You were eliminated from the game");

        String broadcast = getEliminationMessage(target, slayer);

        if (broadcast != null)
        {
            broadcast(broadcast);
        }

        Sound sound = getEliminationSound();

        if (sound != null)
        {
            forEachPlayer(player -> player.playSound(player.getLocation(), sound, 1F, 2F));
        }

        target.setGameMode(GameMode.SPECTATOR);
        target.getVelocity().multiply(new Vector(0, 0.5, 0));
    }

    /**
     * Gets the message to broadcast when a player is eliminated
     * @see #eliminate(Player, Player)
     * @param target The player being eliminated
     * @param slayer The player eliminating the target
     * @return The elimination message
     */
    protected String getEliminationMessage(Player target, Player slayer)
    {
        return C.YELLOW + target.getDisplayName() + C.YELLOW + C.RED + " was eliminated" + (slayer == null ? "" : " by " + C.YELLOW + slayer.getDisplayName());
    }

    /**
     * Gets the sound played to all players when a player is eliminated
     * @return The elimination sound
     */
    protected Sound getEliminationSound()
    {
        return Sound.ENTITY_LIGHTNING_IMPACT;
    }

    /**
     * Gets whether a player is playing the game by {@link UUID}
     * @param id The UUID of the player
     * @return Whether the player is playing the game
     */
    public final boolean isPlaying(UUID id)
    {
        return players.contains(id);
    }

    /**
     * Gets whether a player is playing the game
     * @param player The player to test
     * @return Whether the player is playing
     */
    public final boolean isPlaying(Player player)
    {
        return isPlaying(player.getUniqueId());
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
    public Set<Player> getPlayers(boolean spectators)
    {
        Set<Player> players;

        players =
                spectators ?
                        Utils.onlinePlayerSet(this.players, this.spectators) :
                        Utils.onlinePlayerSet(this.players);

        return players;
    }

    /* Events */
    @EventHandler (priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent event)
    {
        Player player = event.getPlayer();

        if (isSpectating(player) && !player.isOp() && !specChat)
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

        if (isSpectating(player) && !specCmd)
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
        if (niceDeath && event.getEntity() instanceof Player)
        {
            Player player = (Player) event.getEntity();

            if (player.getHealth() - event.getDamage() <= 0.0D)
            {
                // The player would die from this hit
                event.setDamage(0);

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

                if (deathDropItems)
                {
                    spawnItems(player);
                }

                eliminate(player, killer);
            }
        }
    }

    /**
     * Spawns a player's items on death naturally
     * @param player The player
     */
    private void spawnItems(Player player)
    {
        World world = player.getWorld();
        Location deathLoc = player.getLocation();
        ItemStack[] items = player.getInventory().getContents();

        Arrays.stream(items).filter(Objects::nonNull).forEach(item -> world.dropItemNaturally(deathLoc, item));
    }

    /**
     * Gets the title of the sidebar to send to players
     * @return The sidebar title
     */
    public String getSidebarTitle()
    {
        return C.AQUA + C.BOLD + getType().toString();
    }

    /**
     * Gets the sidebar to display to a player
     * @param player The player
     * @return The sidebar to display
     */
    public List<String> getSidebar(Player player)
    {
       return currentPhase.getSidebar(player);
    }

    /**
     * Resets number of blank lines on the sidebar
     */
    public void resetBlanks()
    {
        currentPhase.resetBlanks();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event)
    {
        Bukkit.broadcastMessage("death called");

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
    public void onEntitySpawn(CreatureSpawnEvent event)
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
        else if (status == Status.DECLINED && forceResourcePack)
        {
            player.kickPlayer(C.RED + "You must accept the resource pack to play this game");
        }
        else if (status == Status.FAILED_DOWNLOAD && forceResourcePack)
        {
            player.kickPlayer(C.RED + "There was an error downloading the required resource pack");
        }
    }
}
