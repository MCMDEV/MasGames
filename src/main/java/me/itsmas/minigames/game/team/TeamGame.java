package me.itsmas.minigames.game.team;

import me.itsmas.minigames.GameManager;
import me.itsmas.minigames.game.Game;
import me.itsmas.minigames.game.GameType;
import me.itsmas.minigames.util.C;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * The representation of a team-based game
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class TeamGame extends Game
{
    /**
     * Constructor for a Team Game
     * @param manager The plugin instance
     * @param type The {@link GameType} of this game
     * @param teamSize The amount of players on each team
     * @param teams The teams in this game
     */
    public TeamGame(GameManager manager, GameType type, int teamSize, Team... teams)
    {
        super(manager, type);

        this.teamSize = teamSize;
        this.teams = teams;

        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    }

    /**
     * Whether players on the same team should be able to attack eachother
     */
    public boolean friendlyFire = false;

    /**
     * Whether a team chat should be available
     */
    public boolean teamChat = true;

    /**
     * The prefix to put before a message to talk in team chat
     */
    public char teamChatPrefix = '!';

    /**
     * The amount of players on each team
     */
    private final int teamSize;

    /**
     * The teams in the game
     */
    private final Team[] teams;

    /**
     * Gets the teams in the game
     * @return The teams
     */
    public final Team[] getTeams()
    {
        return teams;
    }

    /**
     * Gets the remaining teams in the game
     * @return The alive teams
     */
    public final Team[] getAliveTeams()
    {
        return Arrays.stream(teams).filter(this::isAlive).collect(Collectors.toSet()).toArray(new Team[0]);
    }

    /**
     * Map of player {@link UUID}s to their respective @{@link Team}
     */
    private Map<UUID, Team> playerToTeam = new HashMap<>();

    /**
     * Gets the team of a player
     * @param player The player
     * @return The player's team
     */
    public final Team getTeam(Player player)
    {
        return playerToTeam.get(player.getUniqueId());
    }

    /**
     * Gets whether all the players on a team are alive
     * @param team The team
     */
    private boolean isAlive(Team team)
    {
        return team.getPlayerIds().size() > 0;
    }

    /**
     * The scoreboard
     */
    private Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

    /**
     * Initialises the game's teams
     */
    public void initTeams()
    {
        if (teamChat)
        {
            broadcast(C.GREEN + C.BOLD + "Start your message with '" + C.YELLOW + teamChatPrefix + C.GREEN + C.BOLD + "' to talk in team chat");
        }

        for (Team team : teams)
        {
            initTeam(team);
        }

        List<Player> tPlayers = new ArrayList<>();
        tPlayers.addAll(getPlayers(false));

        Collections.shuffle(tPlayers, ThreadLocalRandom.current());

        int index = -1;

        for(Player player : tPlayers)
        {
            index++;

            // Distribute teams
            Team team = teams[index % teams.length];
            team.addPlayer(player);

            playerToTeam.put(player.getUniqueId(), team);

            // Change player display name
            ChatColor colour = team.getColour();

            player.setDisplayName(colour + player.getName());
            player.setPlayerListName(colour + player.getName());

            // Adjust scoreboards
            player.setScoreboard(scoreboard);
            scoreboard.getTeam(team.getName()).addEntry(player.getName());
        }
    }

    /**
     * Initialises a team on the scoreboard
     * @param team The team to initialise
     */
    private void initTeam(Team team)
    {
        org.bukkit.scoreboard.Team scTeam = scoreboard.getTeam(team.getName());

        if (scTeam == null)
        {
            scTeam = scoreboard.registerNewTeam(team.getName());
        }

        scTeam.setAllowFriendlyFire(friendlyFire);

        scTeam.setPrefix(team.getPrefix());
        scTeam.setSuffix(C.RESET);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        if (teamChat)
        {
            Player player = event.getPlayer();

            if (!isPlaying(player))
            {
                return;
            }

            String message = event.getMessage();

            if (message.startsWith(String.valueOf(teamChatPrefix)))
            {
                Team team = getTeam(player);

                event.setFormat(team.getColour() + C.BOLD + "[TEAM] " + event.getFormat());

                removeNonTeam(team, event.getRecipients());
            }
        }
    }

    /**
     * Stops non-team members from receiving a chat message
     * @param team The team the players should be on to receive the message
     * @param recipients The players receiving the message
     */
    private void removeNonTeam(Team team, Set<Player> recipients)
    {
        recipients.iterator().forEachRemaining(recipient ->
        {
            if (team != getTeam(recipient))
            {
                recipients.remove(recipient);
            }
        });
    }
}
