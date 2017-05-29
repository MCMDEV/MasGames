package me.itsmas.games.game.phase;

import com.google.common.collect.Sets;
import me.itsmas.games.game.Game;
import me.itsmas.games.util.Utils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Set;

/**
 * The representation of a phase in a games
 */
public abstract class GamePhase implements Listener
{
    /**
     * The games this phase is happening on
     */
    protected final Game game;

    /**
     * Whether this phase is ingame
     */
    private final boolean ingame;

    /**
     * Gets whether this phase is an ingame phase
     * @see #GamePhase(Game, boolean)
     * @see #ingame
     * @return Whether the phase is ingame
     */
    public final boolean isIngame()
    {
        return ingame;
    }

    /**
     * GamePhase constructor
     * @param game The games this phase will occur on
     * @param ingame Whether this phase counts as ingame
     */
    public GamePhase(Game game, boolean ingame)
    {
        this.game = game;
        this.ingame = ingame;
    }

    /**
     * The tasks associated with this phase
     */
    private final Set<BukkitRunnable> tasks = Sets.newHashSet();

    /**
     * The listeners associated with this phase
     */
    private final Set<Listener> listeners = Sets.newHashSet();

    /**
     * Method called when this phase starts
     */
    public final void startPhase()
    {
        Utils.register(this);

        runRepeatingTask(this::onUpdate, 0L, 20L);
        runRepeatingTask(this::sendActionBars, 0L, 20L);

        onStart();
    }

    /**
     * Sends the relevant action bars to all players
     */
    protected final void sendActionBars()
    {
        game.forEachPlayer(this::sendActionBar);
    }

    /**
     * Sends the relevant actionbar to a player
     * @param player The player
     */
    protected final void sendActionBar(Player player)
    {
        String raw = getActionBar(player);

        if (raw != null)
        {
            BaseComponent component = new TextComponent(raw);

            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
        }
    }

    /**
     * Method called when this phase ends
     */
    public final void endPhase()
    {
        cleanup();

        onEnd();

        game.nextPhase();
    }

    /**
     * Cleans up remaining tasks and unregisters this class
     */
    protected final void cleanup()
    {
        Utils.unregister(this);

        tasks.forEach(BukkitRunnable::cancel);
        tasks.clear();

        listeners.forEach(HandlerList::unregisterAll);
        listeners.clear();
    }

    protected final void addListener(Listener listener)
    {
        Utils.register(listener);
        listeners.add(listener);
    }

    /**
     * Runs a task
     * @param runnable The task to run
     * @param ticks The ticks to delay the task by
     */
    protected final void runTaskLater(Runnable runnable, long ticks)
    {
        BukkitRunnable bukkitRunnable = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                runnable.run();
            }
        };

        bukkitRunnable.runTaskLater(game.getManager(), ticks);

        addTask(bukkitRunnable);
    }

    /**
     * Runs a repeating task
     * @param runnable The task to run
     * @param delay The ticks to delay the task by
     * @param interval The interval in ticks between each repetition
     */
    protected final void runRepeatingTask(Runnable runnable, long delay, long interval)
    {
        runRepeatingTask(runnable, -1, delay, interval);
    }

    /**
     * Runs a repeating task
     * @param runnable The task to run
     * @param repetitions The number of times to repeat this task
     * @param delay The ticks to delay the task by
     * @param interval The interval in ticks between each repetition
     */
    protected final void runRepeatingTask(Runnable runnable, int repetitions, long delay, long interval)
    {
        BukkitRunnable bukkitRunnable = new BukkitRunnable()
        {
            int execution = 0;

            @Override
            public void run()
            {
                if (repetitions != -1)
                {
                    execution++;

                    if (execution > repetitions)
                    {
                        cancel();
                        return;
                    }
                }

                runnable.run();
            }
        };

        bukkitRunnable.runTaskTimer(game.getManager(), delay, interval);

        addTask(bukkitRunnable);
    }

    /**
     * Adds a runnable to the task set
     * @see #tasks
     * @param runnable The runnable to add
     */
    private void addTask(BukkitRunnable runnable)
    {
        tasks.add(runnable);
    }

    /**
     * Method called when a games phase starts
     * To be overridden for desired behaviour
     */
    protected void onStart(){}

    /**
     * Method called every second while this phase is active
     * To be overridden for desired behaviour
     */
    public void onUpdate(){}

    /**
     * Method called when a games phase ends
     * To be overridden for desired behaviour
     */
    protected void onEnd(){}

    /**
     * Gets the actionbar message to display to a player
     * @param player The player
     * @return The actionbar message
     */
    protected String getActionBar(Player player)
    {
        return null;
    }

    /**
     * Gets the sidebar to display to a player
     * @param player The player
     * @return The sidebar to display
     */
    public List<String> getSidebar(Player player)
    {
        return null;
    }

    /**
     * The number of blank lines on the sidebar
     */
    private int blanks = 0;

    /**
     * Resets the number of lines on the sidebar
     */
    public void resetBlanks()
    {
        blanks = 0;
    }

    /**
     * Writes a blank line to a sidebar
     * @param sidebar The sidebar to write to
     */
    protected void writeBlank(List<String> sidebar)
    {
        blanks++;

        sidebar.add(StringUtils.repeat(' ', blanks));
    }
}
