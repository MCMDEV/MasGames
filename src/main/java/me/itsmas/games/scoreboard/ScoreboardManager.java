package me.itsmas.games.scoreboard;

import me.itsmas.games.GameManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager implements Listener
{
    private final GameManager manager;

    public ScoreboardManager(GameManager manager)
    {
        this.manager = manager;

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                boards.values().forEach(BoardWrapper::update);
                manager.getGame().resetBlanks();
            }
        }.runTaskTimer(manager, 0L, 2L);
    }

    private final Map<UUID, BoardWrapper> boards = new HashMap<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();

        boards.put(player.getUniqueId(), new BoardWrapper(manager, player));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();

        BoardWrapper board = boards.get(player.getUniqueId());
        board.unregister();

        boards.remove(player.getUniqueId());
    }
}
