package bgu.spl.a2;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * an abstract class that represents a task that may be executed using the
 * {@link WorkStealingThreadPool}
 *
 * @param <R> the task result type
 */
public abstract class Task<R> {

    private Runnable continueRunning;
    private Deferred<R> def = new Deferred<>();        //call Deferred Constructor (Can't change any TASK constructor)
    private Processor tHandler;
    private boolean ran;                                //indicates whether this task has run before.

    /**
     * start handling the task - note that this method is protected, a handler
     * cannot call it directly but instead must use the
     * {@link #handle(bgu.spl.a2.Processor)} method
     */
    protected abstract void start();

    /**
     * start/continue handling the task
     * <p>
     * this method should be called by a processor in order to start this task
     * or continue its execution in the case where it has been already started,
     * any sub-tasks / child-tasks of this task should be submitted to the queue
     * of the handler that handles it currently
     *
     * @param handler the handler that wants to handle the task
     */
    /*package*/
    final synchronized void handle(Processor handler) {

        this.tHandler = handler;                // the handler of this task

        if (!ran) {                            //if it's the first time this task ran
            ran = true;                        //mark this task as "ran"
            start();
        } else {                                //if this task ran before-it means it waited for other tasks (or its deferred to resolve)

            continueRunning.run();
        }
    }

    /**
     * This method schedules a new task (a child of the current task) to the
     * same processor which currently handles this task.
     *
     * @param task the task to execute
     */
    protected final void spawn(Task<?>... task) {
        //add each task to the queue of the handler.
        for (Task<?> aTask : task) {
            tHandler.getPool().getQarray().get(tHandler.getId()).add(aTask);
            tHandler.getPool().vm.inc();                                       //increase version of VersionMonitor
        }

    }

    /**
     * add a callback to be executed once *all* the given tasks results are
     * resolved
     *
     * @param tasks    Collection of tasks
     * @param callback the callback to execute once all the results are resolved
     */
    protected final void whenResolved(Collection<? extends Task<?>> tasks, Runnable callback) {
        if (!tasks.isEmpty()) {
            AtomicInteger numOfRunning = new AtomicInteger(tasks.size());     // number of running tasks

            for (Task<?> currentTask : tasks) {                               //for every task that "callback" depends on
                currentTask.getResult().whenResolved(() -> {
                    if (numOfRunning.decrementAndGet() == 0) {
                        this.continueRunning = callback;
                        spawn(this);        //Reschedule this task, it is ready.
                    }
                });
            }
        } else
            callback.run();
    }

    /**
     * resolve the internal result - should be called by the task derivative
     * once it is done.
     *
     * @param result - the task calculated result
     */
    protected final void complete(R result) {
        this.def.resolve(result);
    }

    /**
     * @return this task deferred result
     */
    public final Deferred<R> getResult() {
        return def;
    }

}
