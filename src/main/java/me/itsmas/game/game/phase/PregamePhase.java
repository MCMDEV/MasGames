package me.itsmas.game.game.phase;

import com.google.common.collect.Sets;
import me.itsmas.game.game.Game;
import me.itsmas.game.util.C;
import me.itsmas.game.util.MathUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The phase in a game before the game has started
 */
public class PregamePhase extends GamePhase
{
    public PregamePhase(Game game)
    {
        super(game, true);
    }

    @Override
    public void onStart()
    {
        game.broadcast(C.BLUE + C.BOLD + "The game will begin in 5 seconds");

        handlePlayers();

        AtomicInteger i = new AtomicInteger(5);

        runRepeatingTask(() ->
        {
            if (i.decrementAndGet() == 0)
            {
                game.forEachPlayer(player -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASEDRUM, 2F, 2F));

                game.broadcast(C.GOLD + "The game has begun!");

                endPhase();
            }
            else
            {
                game.forEachPlayer(player ->
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_SNARE, 1F, 2F)
                );
            }
        }, 5, 0L, 20L);
    }

    /**
     * Spawns the players on the map
     */
    private void handlePlayers()
    {
        game.forEachPlayer(player -> player.setGameMode(game.gameMode));

        List<Location> spawns = game.getMap().getSpawns("Players");
        Set<Integer> used = Sets.newHashSet();

        for (Player player : game.getPlayers(false))
        {
            int i;

            do
            {
                i = MathUtil.nextInt(spawns.size());
            } while (used.contains(i));

            player.teleport(spawns.get(i));
            used.add(i);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event)
    {
        if (game.isPlaying(event.getPlayer()))
        {
            event.setTo(event.getFrom());
        }
    }
}
