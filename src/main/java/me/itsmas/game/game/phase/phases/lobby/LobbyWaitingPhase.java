package me.itsmas.game.game.phase.phases.lobby;

import me.itsmas.game.game.Game;
import org.bukkit.Sound;

/**
 * The phase of a game where players are waiting in the lobby before the countdown
 */
public class LobbyWaitingPhase extends LobbyPhase
{
    /**
     * The minimum needed amount of players for the game
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
}
