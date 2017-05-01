package me.itsmas.game.util;

import com.google.common.collect.Sets;
import me.itsmas.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.SpigotConfig;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

/**
 * Class containing global utility methods
 */
public class Utils
{
    /**
     * Private constructor
     * No instances of the class are needed
     */
    private Utils(){}

    /**
     * The plugin instance
     */
    private static final GameManager plugin = JavaPlugin.getPlugin(GameManager.class);

    /**
     * Registers a {@link Listener}
     * @param listener The listener to register
     */
    public static void register(Listener listener)
    {
        Bukkit.getPluginManager().registerEvents(listener, plugin);
    }

    /**
     * Unregisters a {@link Listener}
     * @param listener The listener to unregister
     */
    public static void unregister(Listener listener)
    {
        HandlerList.unregisterAll(listener);
    }

    /**
     * Returns a set of online players from an array of Set<UUID>s
     * @param sets The sets to check for online players
     * @return The online players
     */
    @SafeVarargs
    public static Set<Player> onlinePlayerSet(Set<UUID>... sets)
    {
        Set<Player> target = Sets.newHashSet();

        for (Set<UUID> set : sets)
        {
            set.forEach(uuid ->
            {
                Player player = Bukkit.getPlayer(uuid);

                if (player != null)
                {
                    target.add(player);
                }
            });
        }

        return target;
    }

    /* Restart Util */
    static
    {
        setRestartScript();
    }

    /**
     * Assigns the script for restarting the server
     * Allows the server to restart automatically once a game ends
     * @see SpigotConfig#restartScript
     */
    private static void setRestartScript()
    {
        File directory = new File(".");

        if (directory.isDirectory())
        {
            File[] files = directory.listFiles();

            if (files != null)
            {
                File scriptFile = Arrays.stream(files).filter(
                        file -> !file.isDirectory() && hasExtension(file, "bat", "sh", "bash") && file.getName().toLowerCase().contains("start")
                ).findFirst().orElse(null);

                if (scriptFile != null)
                {
                    Bukkit.getLogger().info("[MasGames] Found restart script: " + scriptFile.getName());

                    SpigotConfig.restartScript = "./" + scriptFile.getName();
                    return;
                }

                System.err.println("[MasGames] Could not find valid restart script");
            }
        }
    }

    /**
     * Gets whether a file has one of the required extensions
     * @param file The file to check
     * @param extensions The extensions tested for
     * @return Whether the file has a required extension
     */
    private static boolean hasExtension(File file, String... extensions)
    {
        for (String ext : extensions)
        {
            if (getExtension(file).equals(ext.toLowerCase()))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets the extension of a file
     * @param file The file to get the extension of
     * @return The file's extension
     */
    private static String getExtension(File file)
    {
        String[] split = file.getName().split("\\.");

        if (split.length > 0)
        {
            return split[split.length - 1].toLowerCase();
        }

        return "";
    }
}
