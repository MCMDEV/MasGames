package me.itsmas.game.game.phase.phases.lobby;

import me.itsmas.game.game.Game;
import me.itsmas.game.util.C;
import org.bukkit.Sound;

/**
 * The phase of a game where the countdown is in progress
 */
public class LobbyCountdownPhase extends LobbyPhase
{
    public LobbyCountdownPhase(Game game)
    {
        super(game);

        this.startTime = (countdownTime = DEFAULT_COUNTDOWN_TIME);
    }

    public LobbyCountdownPhase(Game game, int countdownTime)
    {
        super(game);

        this.startTime = (this.countdownTime = countdownTime);
    }

    /**
     * The default countdown start time
     */
    private final int DEFAULT_COUNTDOWN_TIME = 30;

    /**
     * The start time of the countdown
     */
    private final int startTime;

    /**
     * The current time of the countdown
     */
    private int countdownTime;

    @Override
    public void onStart()
    {
        announceTime();
    }

    @Override
    public void onUpdate()
    {
        if (game.getPlayersSize() < game.getType().getMinPlayers())
        {
            cleanup();
            countdownTime = startTime;

            game.broadcast(C.RED + "There are no longer enough players to start");
            game.revertToLobby();
        }
        else
        {
            countdownTime--;

            if (countdownTime == 0)
            {
                game.forEachPlayer(player ->
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 2F)
                );

                endPhase();
                return;
            }

            // Play a sound every 5 seconds <= 20 seconds and every second <= 5 seconds
            if ((countdownTime <= 20 && countdownTime % 5 == 0) || countdownTime <= 5)
            {
                announceTime();
            }
        }
    }

    /**
     * Announces the countdown time to all players
     */
    private void announceTime()
    {
        String s = countdownTime == 1 ? "" : "s";
        game.broadcast(C.YELLOW + "The game will start in " + countdownTime + " second" + s);

        game.forEachPlayer(player ->
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1F, 2F)
        );
    }
}
