package me.itsmas.game.game.phase.phases.game;

import com.google.common.collect.Lists;
import me.itsmas.game.game.Game;
import me.itsmas.game.game.phase.GamePhase;

/**
 * A game phase where the last man standing wins
 */
public class LastStandingPhase extends GamePhase
{
    public LastStandingPhase(Game game)
    {
        super(game, true);
    }

    @Override
    public void onUpdate()
    {
        if (game.getPlayersSize() == 1)
        {
            game.endGame(Lists.newArrayList(game.getPlayers(false)).get(0));
        }
        else if (game.getPlayersSize() == 0)
        {
            game.endGame();
        }
    }
}
