package io.github.bakedlibs.dough.scheduling;

import java.util.function.IntConsumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.molean.folia.adapter.SchedulerContext;
import org.bukkit.plugin.Plugin;

/**
 * This class provides useful utilities to schedule Tasks (sync and async).
 * Tasks are added into a Queue and then run sequentially via {@link TaskQueue#execute(Plugin)}
 * You can provide a delay between the individual tasks via the ticks argument in
 * {@link TaskQueue#thenRun(int, Runnable, SchedulerContext)}
 * If you need to access the index of your current task (whether it is the first, last or xth task) you can use
 * the methods with {@link IntConsumer} as an argument, otherwise just use the ones with {@link Runnable}
 * 
 * @author TheBusyBiscuit
 *
 */
public class TaskQueue {

    private TaskNode head;

    /**
     * Use this method to execute the final Task Queue.
     * You should add the tasks before-hand.
     * An {@link IllegalStateException} will be thrown if the queue is empty.
     * 
     * @param plugin
     *            The plugin that is performing this execution
     */
    public void execute(@Nonnull Plugin plugin) {
        if (head == null) {
            throw new IllegalStateException("Cannot execute TaskQueue, no head was found");
        }

        run(plugin, head, 0);
    }

    private void run(@Nonnull Plugin plugin, @Nullable TaskNode node, int index) {
        if (node == null) {
            return;
        }

        Runnable runnable = () -> {
            node.execute(index);
            run(plugin, node.getNextNode(), index + 1);
        };

        node.getContext().runTaskLater(plugin, runnable, node.getDelay());
    }

    /**
     * This method will schedule the given Task with no delay and <strong>synchronously</strong>.
     * Use the {@link Integer} parameter in your {@link IntConsumer} to determine the task's index.
     *
     * @param consumer The callback to run
     * @return The current instance of {@link TaskQueue}
     */
    public @Nonnull TaskQueue thenRun(@Nonnull IntConsumer consumer, SchedulerContext context) {
        return append(new TaskNode(consumer, context));
    }

    /**
     * This method will schedule the given Task with no delay and <strong>synchronously</strong>.
     * 
     * @param runnable
     *            The callback to run
     * 
     * @return The current instance of {@link TaskQueue}
     */
    public @Nonnull TaskQueue thenRun(@Nonnull Runnable runnable, SchedulerContext context) {
        return thenRun(i -> runnable.run(), context);
    }

    /**
     * This method will schedule the given Task with no delay and <strong>asynchronously</strong>.
     * Use the {@link Integer} parameter in your {@link IntConsumer} to determine the task's index.
     * 
     * @param consumer
     *            The callback to run
     * 
     * @return The current instance of {@link TaskQueue}
     */
    public @Nonnull TaskQueue thenRunAsynchronously(@Nonnull IntConsumer consumer) {
        return append(new TaskNode(consumer, SchedulerContext.ofAsync()));
    }

    /**
     * This method will schedule the given Task with no delay and <strong>asynchronously</strong>.
     * 
     * @param runnable
     *            The callback to run
     * 
     * @return The current instance of {@link TaskQueue}
     */
    public @Nonnull TaskQueue thenRunAsynchronously(@Nonnull Runnable runnable) {
        return thenRunAsynchronously(i -> runnable.run());
    }

    /**
     * This method will schedule the given Task with the given delay and <strong>synchronously</strong>.
     * Use the {@link Integer} parameter in your {@link IntConsumer} to determine the task's index.
     * 
     * @param ticks
     *            The time to wait before running this task after the previous one.
     * @param consumer
     *            The callback to run
     * 
     * @return The current instance of {@link TaskQueue}
     */
    public @Nonnull TaskQueue thenRun(int ticks, @Nonnull IntConsumer consumer, SchedulerContext context) {
        if (ticks < 1) {
            throw new IllegalArgumentException("thenAfter() must be given a time that is greater than zero!");
        }

        return append(new TaskNode(consumer, ticks, context));
    }

    /**
     * This method will schedule the given Task with the given delay and <strong>synchronously</strong>.
     * 
     * @param ticks
     *            The time to wait before running this task after the previous one.
     * @param runnable
     *            The callback to run
     * 
     * @return The current instance of {@link TaskQueue}
     */
    public @Nonnull TaskQueue thenRun(int ticks, @Nonnull Runnable runnable,SchedulerContext context) {
        return thenRun(ticks, i -> runnable.run(), context);
    }

    /**
     * This method will schedule the given Task with the given delay and <strong>asynchronously</strong>.
     * Use the {@link Integer} parameter in your {@link IntConsumer} to determine the task's index.
     * 
     * @param ticks
     *            The time to wait before running this task after the previous one.
     * @param consumer
     *            The callback to run
     * 
     * @return The current instance of {@link TaskQueue}
     */
    public @Nonnull TaskQueue thenRunAsynchronously(int ticks, @Nonnull IntConsumer consumer) {
        if (ticks < 1) {
            throw new IllegalArgumentException("thenAfter() must be given a time that is greater than zero!");
        }

        return append(new TaskNode(consumer, ticks, SchedulerContext.ofAsync()));
    }

    /**
     * This method will schedule the given Task with the given delay and <strong>synchronously</strong>.
     * 
     * @param ticks
     *            The time to wait before running this task after the previous one.
     * @param runnable
     *            The callback to run
     * 
     * @return The current instance of {@link TaskQueue}
     */
    public @Nonnull TaskQueue thenRunAsynchronously(int ticks, @Nonnull Runnable runnable) {
        return thenRunAsynchronously(ticks, i -> runnable.run());
    }

    /**
     * This method will schedule the given Task with no delay and <strong>synchronously</strong>.
     * The task will be repeated for the given amount of iterations.
     * Use the {@link Integer} parameter in your {@link IntConsumer} to determine the task's index.
     * 
     * @param iterations
     *            The amount of times to repeat this task
     * @param consumer
     *            The callback to run
     * 
     * @return The current instance of {@link TaskQueue}
     */
    public @Nonnull TaskQueue thenRepeat(int iterations, @Nonnull IntConsumer consumer, SchedulerContext context) {
        for (int i = 0; i < iterations; i++) {
            append(new TaskNode(consumer, context));
        }

        return this;
    }

    /**
     * This method will schedule the given Task with no delay and <strong>synchronously</strong>.
     * The task will be repeated for the given amount of iterations.
     * 
     * @param iterations
     *            The amount of times to repeat this task
     * @param runnable
     *            The callback to run
     * 
     * @return The current instance of {@link TaskQueue}
     */
    public @Nonnull TaskQueue thenRepeat(int iterations, @Nonnull Runnable runnable, SchedulerContext context) {
        return thenRepeat(iterations, i -> runnable.run(), context);
    }

    /**
     * This method will schedule the given Task with no delay and <strong>asynchronously</strong>.
     * The task will be repeated for the given amount of iterations.
     * Use the {@link Integer} parameter in your {@link IntConsumer} to determine the task's index.
     * 
     * @param iterations
     *            The amount of times to repeat this task
     * @param consumer
     *            The callback to run
     * 
     * @return The current instance of {@link TaskQueue}
     */
    public @Nonnull TaskQueue thenRepeatAsynchronously(int iterations, @Nonnull IntConsumer consumer) {
        for (int i = 0; i < iterations; i++) {
            append(new TaskNode(consumer, SchedulerContext.ofAsync()));
        }

        return this;
    }

    /**
     * This method will schedule the given Task with no delay and <strong>asynchronously</strong>.
     * The task will be repeated for the given amount of iterations.
     * 
     * @param iterations
     *            The amount of times to repeat this task
     * @param runnable
     *            The callback to run
     * 
     * @return The current instance of {@link TaskQueue}
     */
    public @Nonnull TaskQueue thenRepeatAsynchronously(int iterations, @Nonnull Runnable runnable) {
        return thenRepeatAsynchronously(iterations, i -> runnable.run());
    }

    /**
     * This method will schedule the given Task with the given delay and <strong>synchronously</strong>.
     * The task will be repeated for the given amount of iterations.
     * Use the {@link Integer} parameter in your {@link IntConsumer} to determine the task's index.
     * 
     * @param ticks
     *            The delay between executions (including the start delay)
     * @param iterations
     *            The amount of times to repeat this task
     * @param consumer
     *            The callback to run
     * 
     * @return The current instance of {@link TaskQueue}
     */
    public @Nonnull TaskQueue thenRepeatEvery(int ticks, int iterations, @Nonnull IntConsumer consumer, SchedulerContext context) {
        if (ticks < 1) {
            throw new IllegalArgumentException("thenRepeatEvery() must be given a time that is greater than zero!");
        }

        for (int i = 0; i < iterations; i++) {
            append(new TaskNode(consumer, ticks, context));
        }

        return this;
    }

    /**
     * This method will schedule the given Task with the given delay and <strong>synchronously</strong>.
     * The task will be repeated for the given amount of iterations.
     * 
     * @param ticks
     *            The delay between executions (including the start delay)
     * @param iterations
     *            The amount of times to repeat this task
     * @param runnable
     *            The callback to run
     * 
     * @return The current instance of {@link TaskQueue}
     */
    public @Nonnull TaskQueue thenRepeatEvery(int ticks, int iterations, @Nonnull Runnable runnable,SchedulerContext context) {
        return thenRepeatEvery(ticks, iterations, i -> runnable.run(), context);
    }

    /**
     * This method will schedule the given Task with the given delay and <strong>asynchronously</strong>.
     * The task will be repeated for the given amount of iterations.
     * Use the {@link Integer} parameter in your {@link IntConsumer} to determine the task's index.
     * 
     * @param ticks
     *            The delay between executions (including the start delay)
     * @param iterations
     *            The amount of times to repeat this task
     * @param consumer
     *            The callback to run
     * 
     * @return The current instance of {@link TaskQueue}
     */
    public @Nonnull TaskQueue thenRepeatEveryAsynchronously(int ticks, int iterations, @Nonnull IntConsumer consumer) {
        if (ticks < 1) {
            throw new IllegalArgumentException("thenRepeatEveryAsynchronously() must be given a time that is greater than zero!");
        }

        for (int i = 0; i < iterations; i++) {
            append(new TaskNode(consumer, ticks, SchedulerContext.ofAsync()));
        }

        return this;
    }

    /**
     * This method will schedule the given Task with the given delay and <strong>asynchronously</strong>.
     * The task will be repeated for the given amount of iterations.
     * 
     * @param ticks
     *            The delay between executions (including the start delay)
     * @param iterations
     *            The amount of times to repeat this task
     * @param runnable
     *            The callback to run
     * 
     * @return The current instance of {@link TaskQueue}
     */
    public @Nonnull TaskQueue thenRepeatEveryAsynchronously(int ticks, int iterations, @Nonnull Runnable runnable) {
        return thenRepeatEveryAsynchronously(ticks, iterations, i -> runnable.run());
    }

    /**
     * This method will make the task run the given callback until eternity.
     * The task will be run with no delay and <strong>synchronously</strong>.
     * Do not add other tasks after calling this method.
     * 
     * @param consumer
     *            The callback to run
     */
    public void thenLoop(@Nonnull IntConsumer consumer, SchedulerContext context) {
        TaskNode node = new TaskNode(consumer, context);
        node.setNextNode(node);
        append(node);
    }

    /**
     * This method will make the task run the given callback until eternity.
     * The task will be run with no delay and <strong>synchronously</strong>.
     * Do not add other tasks after calling this method.
     * 
     * @param runnable
     *            The callback to run
     */
    public void thenLoop(@Nonnull Runnable runnable,SchedulerContext context) {
        thenLoop(i -> runnable.run(), context);
    }

    /**
     * This method will make the task run the given callback until eternity.
     * The task will be run with no delay and <strong>asynchronously</strong>.
     * Do not add other tasks after calling this method.
     * 
     * @param consumer
     *            The callback to run
     */
    public void thenLoopAsynchronously(@Nonnull IntConsumer consumer) {
        TaskNode node = new TaskNode(consumer, SchedulerContext.ofAsync());
        node.setNextNode(node);
        append(node);
    }

    /**
     * This method will make the task run the given callback until eternity.
     * The task will be run with no delay and <strong>asynchronously</strong>.
     * Do not add other tasks after calling this method.
     * 
     * @param runnable
     *            The callback to run
     */
    public void thenLoopAsynchronously(@Nonnull Runnable runnable) {
        thenLoopAsynchronously(i -> runnable.run());
    }

    /**
     * This method will make the task run the given callback until eternity.
     * The task will be run with the given delay and <strong>synchronously</strong>.
     * Do not add other tasks after calling this method.
     * 
     * @param ticks
     *            The delay between executions (including the start delay)
     * @param consumer
     *            The callback to run
     */
    public void thenLoopEvery(int ticks, @Nonnull IntConsumer consumer, SchedulerContext context) {
        if (ticks < 1) {
            throw new IllegalArgumentException("thenLoopEvery() must be given a time that is greater than zero!");
        }

        TaskNode node = new TaskNode(consumer, ticks, context);
        node.setNextNode(node);
        append(node);
    }

    /**
     * This method will make the task run the given callback until eternity.
     * The task will be run with the given delay and <strong>synchronously</strong>.
     * Do not add other tasks after calling this method.
     * 
     * @param ticks
     *            The delay between executions (including the start delay)
     * @param runnable
     *            The callback to run
     */
    public void thenLoopEvery(int ticks, @Nonnull Runnable runnable,SchedulerContext context) {
        thenLoopEvery(ticks, i -> runnable.run(), context);
    }

    /**
     * This method will make the task run the given callback until eternity.
     * The task will be run with the given delay and <strong>asynchronously</strong>.
     * Do not add other tasks after calling this method.
     * 
     * @param ticks
     *            The delay between executions (including the start delay)
     * @param consumer
     *            The callback to run
     */
    public void thenLoopEveryAsynchronously(int ticks, @Nonnull IntConsumer consumer) {
        if (ticks < 1) {
            throw new IllegalArgumentException("thenLoopEveryAsynchronously() must be given a time that is greater than zero!");
        }

        TaskNode node = new TaskNode(consumer, ticks, SchedulerContext.ofAsync());
        node.setNextNode(node);
        append(node);
    }

    /**
     * This method will make the task run the given callback until eternity.
     * The task will be run with the given delay and <strong>asynchronously</strong>.
     * Do not add other tasks after calling this method.
     * 
     * @param ticks
     *            The delay between executions (including the start delay)
     * @param runnable
     *            The callback to run
     */
    public void thenLoopEveryAsynchronously(int ticks, @Nonnull Runnable runnable) {
        thenLoopEveryAsynchronously(ticks, i -> runnable.run());
    }

    /**
     * This method will make the Queue just do nothing for the given amount of ticks.
     * You should not really be using this method but it exists.
     * 
     * @param ticks
     *            The amount of ticks to wait for
     * 
     * @return The current instance of {@link TaskQueue}
     */
    public @Nonnull TaskQueue thenWait(int ticks, SchedulerContext context) {
        TaskNode node = new TaskNode(i -> {}, context);
        node.setDelay(ticks);
        return append(node);
    }

    private @Nonnull TaskQueue append(@Nonnull TaskNode node) {
        if (head == null) {
            head = node;
        } else {
            TaskNode current = head;

            while (current.hasNextNode()) {
                if (current == current.getNextNode()) {
                    throw new IllegalAccessError("You cannot append to a TaskQueue that contains a loop");
                }

                current = current.getNextNode();
            }

            current.setNextNode(node);
        }

        return this;
    }

}
