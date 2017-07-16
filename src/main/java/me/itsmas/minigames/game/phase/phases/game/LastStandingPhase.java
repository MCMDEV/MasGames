package me.itsmas.minigames.game.phase.phases.game;

import com.google.common.collect.Iterables;
import me.itsmas.minigames.game.Game;
import me.itsmas.minigames.game.phase.GamePhase;
import me.itsmas.minigames.game.team.Team;
import me.itsmas.minigames.game.team.TeamGame;
import org.bukkit.entity.Player;

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
        if (game instanceof TeamGame)
        {
            TeamGame tGame = (TeamGame) game;

            if (tGame.getAliveTeams().length == 1)
            {
                Team team = tGame.getAliveTeams()[0];

                game.endGame(team.getPlayers());
            }
            else if (tGame.getAliveTeams().length == 0)
            {
                game.endGame();
            }
        }
        else
        {
            if (game.getPlayersSize() == 1)
            {
                Player winner = Iterables.getFirst(game.getPlayers(false), null);
                game.endGame(winner);
            }
            else if (game.getPlayersSize() == 0)
            {
                game.endGame();
            }
        }
    }
}
