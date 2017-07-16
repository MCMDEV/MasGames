package me.itsmas.minigames.scoreboard;

import me.itsmas.minigames.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for {@link Scoreboard}
 * Used to display custom sidebars to players
 */
public class BoardWrapper
{
    /**
     * The plugin instance
     */
    private final GameManager manager;

    /**
     * The player linked to this board
     */
    private final Player player;

    /**
     * The {@link Scoreboard} object
     */
    private final Scoreboard scoreboard;

    /**
     * BoardWrapper constructor
     * @param manager The plugin instance
     * @param player The player linked to this board
     */
    BoardWrapper(GameManager manager, Player player)
    {
        this.manager = manager;
        this.player = player;

        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        player.setScoreboard(scoreboard);

        setObjective();
    }

    /**
     * Sets the scoreboard objective
     */
    private void setObjective()
    {
        objective = scoreboard.registerNewObjective("sidebar", "dummy");

        objective.setDisplayName(manager.getGame().getSidebarTitle());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    /**
     * The scoreboard's custom objective
     */
    private Objective objective;

    /**
     * The last sidebar
     */
    private List<String> sidebar = new ArrayList<>();

    /**
     * Updates the sidebar content
     */
    void update()
    {
        List<String> sidebar = manager.getGame().getSidebar(player);

        if (sidebar == null)
        {
            unregisterObjective();
            return;
        }

        setSidebar(sidebar);
    }

    /**
     * Sets the scoreboard's sidebar
     * @param sidebar The sidebar
     */
    private void setSidebar(List<String> sidebar)
    {
        if (objective == null)
        {
            setObjective();
        }

        objective.setDisplayName(manager.getGame().getSidebarTitle());

        List<String> lines = new ArrayList<>();

        if (sidebar.size() > 0 && sidebar.size() <= 15)
        {
            int score = 15;

            for (String line : sidebar)
            {
                if (!lines.contains(line))
                {
                    lines.add(line);

                    objective.getScore(line).setScore(score);
                    score--;
                }
            }
        }

        for (String last : this.sidebar)
        {
            if (!lines.contains(last))
            {
                scoreboard.resetScores(last);
            }
        }

        this.sidebar = lines;
    }

    /**
     * Unregisters this scoreboard
     */
    void unregister()
    {
        unregisterObjective();

        sidebar.clear();
    }

    /**
     * Unregisters the scoreboard objective
     */
    private void unregisterObjective()
    {
        if (objective != null)
        {
            objective.unregister();
            objective = null;
        }
    }
}
