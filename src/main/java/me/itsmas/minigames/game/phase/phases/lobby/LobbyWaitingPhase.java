package me.itsmas.minigames.game.phase.phases.lobby;

import me.itsmas.minigames.game.Game;
import me.itsmas.minigames.util.C;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * The phase of a minigames where players are waiting in the lobby before the countdown
 */
public class LobbyWaitingPhase extends LobbyPhase
{
    /**
     * The minimum needed amount of players for the minigames
     */
    private final int neededPlayers;

    public LobbyWaitingPhase(Game game)
    {
        super(game);

        neededPlayers = game.getType().getMinPlayers();
    }

    @Override
    public void onUpdate()
    {
        if (game.getPlayersSize() >= neededPlayers)
        {
            game.forEachPlayer(player -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 2F, 1F));

            endPhase();
        }
    }

    @Override
    public List<String> getSidebar(Player player)
    {
        List<String> sidebar = new ArrayList<>();

        sidebar.add(C.GREEN + C.BOLD + "Waiting");
        writeBlank(sidebar);

        sidebar.add(C.YELLOW + C.BOLD + "Players");
        sidebar.add(game.getPlayersSize() + "/" + game.getType().getMaxPlayers());
        writeBlank(sidebar);

        sidebar.add(C.BLUE + C.BOLD + "Map");
        sidebar.add(game.getMap().getName());

        return sidebar;
    }

    @Override
    public String getActionBar(Player player)
    {
        return C.BLUE + C.BOLD + "Waiting for players...";
    }
}
