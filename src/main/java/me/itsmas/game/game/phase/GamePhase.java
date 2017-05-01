package me.itsmas.game.game.phase;

import com.google.common.collect.Sets;
import me.itsmas.game.game.Game;
import me.itsmas.game.util.Utils;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;

/**
 * The representation of a phase in a game
 */
public abstract class GamePhase implements Listener
{
    /**
     * The game this phase is happening on
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
     * @param game The game this phase will occur on
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

        onStart();
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
     * Method called when a game phase starts
     * To be overridden for desired behaviour
     */
    protected void onStart(){}

    /**
     * Method called every second while this phase is active
     * To be overridden for desired behaviour
     */
    public void onUpdate(){}

    /**
     * Method called when a game phase ends
     * To be overridden for desired behaviour
     */
    protected void onEnd(){}
}
