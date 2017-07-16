package me.itsmas.minigames.game.team;

import me.itsmas.minigames.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * The representation of a team in a game
 */
public class Team
{
    /**
     * The name of the team
     */
    private final String name;

    /**
     * The colour of the team
     */
    private final ChatColor colour;

    /**
     * The team's prefix
     */
    private final String prefix;

    /**
     * Constructor for a team
     * @param name The team name
     * @param colour The team colour
     */
    public Team(String name, ChatColor colour)
    {
        this(name, colour, colour.toString());
    }

    public Team(String name, ChatColor colour, String prefix)
    {
        this.name = name;
        this.colour = colour;
        this.prefix = prefix;
    }

    /**
     * Gets the team's name
     * @return The team name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets the team's colour
     * @return The team colour
     */
    public ChatColor getColour()
    {
        return colour;
    }

    /**
     * Gets the team's prefix
     * @return The team prefix
     */
    public String getPrefix()
    {
        return prefix;
    }

    /**
     * The players in this team
     */
    private Set<UUID> players = new HashSet<>();

    /**
     * Gets the {@link UUID}s of players on the team
     * @return The team's players
     */
    public Set<UUID> getPlayerIds()
    {
        return players;
    }

    /**
     * Gets the players on the team
     * @return The team's players
     */
    public Set<Player> getPlayers()
    {
        return Utils.onlinePlayerSet(players);
    }

    /**
     * Adds a player to the team
     * @param player The player
     */
    public void addPlayer(Player player)
    {
        players.add(player.getUniqueId());
    }

    /**
     * Removes a player from the team
     * @param player The player
     */
    public void removePlayer(Player player)
    {
        players.remove(player.getUniqueId());
    }
}
