package me.itsmas.games.game;

import me.itsmas.games.GameManager;
import me.itsmas.games.util.C;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles login attempts to the server
 */
public class LoginListener implements Listener
{
    /**
     * The plugin instance
     */
    private final GameManager manager;

    /**
     * LoginListener constructor
     * @param manager The plugin instance
     */
    public LoginListener(GameManager manager)
    {
        this.manager = manager;
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onLogin(PlayerLoginEvent event)
    {
        Game game = manager.getGame();

        if (game != null)
        {
            if (!game.isLobby())
            {
                event.disallow(Result.KICK_OTHER, C.RED + "This game is already in progress");
            }
            else if (game.isFull())
            {
                event.disallow(Result.KICK_OTHER, C.RED + "This game is full");
            }
        }
        else
        {
            event.disallow(Result.KICK_OTHER, C.RED + "An error occured creating the game instance");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        event.setJoinMessage(null);

        manager.getGame().handleJoin(event.getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerQuitEvent event)
    {
        event.setQuitMessage(null);

        manager.getGame().handleLeave(event.getPlayer());
    }
}
