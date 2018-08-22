package bgu.spl.a2;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * this class represents a single work stealing processor, it is
 * {@link Runnable} so it is suitable to be executed by threads.
 */
public class Processor implements Runnable {

    private final WorkStealingThreadPool pool;
    private final int id;

    /**
     * constructor for this class
     *
     * @param id   - the processor id (every processor need to have its own unique
     *             id inside its thread pool)
     * @param pool - the thread pool which owns this processor
     */
    /*package*/ Processor(int id, WorkStealingThreadPool pool) {
        this.id = id;
        this.pool = pool;
    }

    //getter (package protected)s
    int getId() {
        return this.id;
    }

    //getter (package protected)
    WorkStealingThreadPool getPool() {
        return this.pool;
    }

    @Override
    public void run() {
        while (!this.pool.shutDown) {
            ConcurrentLinkedDeque<Task<?>> myQ = this.pool.getQarray().get(this.id);    //get access to this processor's queue
            int currentVersion = this.getPool().vm.getVersion();
            if (myQ.isEmpty()) {                                                        //try stealing
                pool.steal(this, currentVersion);
            }
            //reaching this line means the queue is NOT empty anymore OR we were interrupted
            else if (!this.pool.shutDown) {    //if this thread is not interrupted
                Task<?> toRun = myQ.pollFirst();
                if (toRun != null)
                    toRun.handle(this);                                                            //handle the task (with the current Processor)

            }
        }
    }


}