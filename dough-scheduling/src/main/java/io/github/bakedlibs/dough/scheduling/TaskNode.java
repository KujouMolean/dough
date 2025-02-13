package io.github.bakedlibs.dough.scheduling;

import java.util.function.IntConsumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.molean.folia.adapter.SchedulerContext;
import org.apache.commons.lang.Validate;

// TODO: Convert to Java 16 record
class TaskNode {

    private final IntConsumer runnable;
    private final SchedulerContext context;
    private int delay = 0;
    private TaskNode nextNode;

    protected TaskNode(@Nonnull IntConsumer consumer, SchedulerContext context) {
        this.runnable = consumer;
        this.context = context;
    }

    protected TaskNode(@Nonnull IntConsumer consumer, int delay, SchedulerContext context) {
        this.runnable = consumer;
        this.delay = delay;
        this.context = context;
    }

    protected boolean hasNextNode() {
        return nextNode != null;
    }

    public @Nullable TaskNode getNextNode() {
        return nextNode;
    }

    public void setNextNode(@Nullable TaskNode node) {
        this.nextNode = node;
    }

    public void execute(int index) {
        runnable.accept(index);
    }

    public SchedulerContext getContext() {
        return context;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        Validate.isTrue(delay >= 0, "The delay cannot be negative.");

        this.delay = delay;
    }

}
