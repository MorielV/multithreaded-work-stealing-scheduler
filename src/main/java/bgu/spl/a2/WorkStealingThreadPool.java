package bgu.spl.a2;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * represents a work stealing thread pool -
 */
public class WorkStealingThreadPool {
    private int numOfThreads;
    private Thread[] threadArray;                                            //LinkedList of  processors
    private Processor[] procArray;                                            //array of Processors
    private CopyOnWriteArrayList<ConcurrentLinkedDeque<Task<?>>> qArray;
    VersionMonitor vm;
    boolean shutDown = false; // true if we need to shut down.

    //getters
    CopyOnWriteArrayList<ConcurrentLinkedDeque<Task<?>>> getQarray() {
        return this.qArray;
    }

    protected Thread[] getThreadArray() {
        return this.threadArray;
    }

    protected Processor[] getProcArray() {
        return this.procArray;
    }

    /**
     * creates a {@link WorkStealingThreadPool} which has nthreads
     * {@link Processor}s. Note, threads should not get started until calling to
     * the {@link #start()} method.
     *
     * @param nthreads the number of threads that should be started by this
     *                 thread pool
     */
    public WorkStealingThreadPool(int nthreads) {

        this.numOfThreads = nthreads;                                          //initialize number of required threads
        threadArray = new Thread[nthreads];                                    //create new array of threads
        procArray = new Processor[nthreads];                                   //create new array of processors
        qArray = new CopyOnWriteArrayList<>();                                 //CopyOnWriteArrayList
        vm = new VersionMonitor();                                             //create a new VersionMonitor

        for (int i = 0; i < nthreads; i++) {
            procArray[i] = new Processor(i, this);                            //new Processor (send this pool)
            threadArray[i] = new Thread(procArray[i], Integer.toString(i));        //new Thread
            qArray.add(new ConcurrentLinkedDeque<>());                             //adding new deque of tasks

        }

    }

    /**
     * submits a task to be executed by a processor belongs to this thread pool
     *
     * @param task the task to execute
     */
    public void submit(Task<?> task) {
        int receiverProc = (int) ((Math.random() * numOfThreads - 1));    //select a random Processor
        qArray.get(receiverProc).addFirst(task);                          //add "task" to the random processor's Queue
        vm.inc();
    }

    /**
     * closes the thread pool - this method interrupts all the threads and wait
     * for them to stop - it is returns *only* when there are no live threads in
     * the queue.
     * <p>
     * after calling this method - one should not use the queue anymore.
     *
     * @throws InterruptedException          if the thread that shut down the threads is
     *                                       interrupted
     * @throws UnsupportedOperationException if the thread that attempts to
     *                                       shutdown the queue is itself a processor of this queue
     */
    public void shutdown() throws InterruptedException {
        //make sure the current thread is not from a Processor from the POOL
        for (int i = 0; i < procArray.length; i++) {
            if (Thread.currentThread().getId() == threadArray[i].getId()) {
                throw new UnsupportedOperationException("Error!");
            }
        }

        //interrupt all
        this.shutDown = true;
        for (int i = 0; i < procArray.length; i++) {
            threadArray[i].interrupt();        //interrupt thread

            //make sure current thread is not interrupted
            if (Thread.currentThread().isInterrupted())
                throw new InterruptedException("The interrupter was interrupted!!!");

            vm.inc();
            threadArray[i].join();            //wait for all threads to die
        }    //reach this line only if all processors are dead
    }

    /**
     * start the threads belongs to this thread pool
     */
    public void start() {
        for (Thread t : threadArray) {
            t.start();
        }
    }

    /**
     * steal half tasks from different processor if u can.
     *
     * @param thief          the stealing processor
     * @param currentVersion the current value of the version monitor
     */
    void steal(Processor thief, int currentVersion) {

        boolean foundVictim = false;
        int victimIndex = (thief.getId() + 1) % procArray.length;                    //CIRCULAR
        for (int i = victimIndex; i != thief.getId() && !foundVictim && !this.shutDown && qArray.get(thief.getId()).isEmpty(); i = (i + 1) % procArray.length) {    //make sure the processor doesn't reach itself
            if (qArray.get(i).size() > 1) {
                foundVictim = true;                                            //found a victim
                victimIndex = i;                                                //save the victim's number
            }
        }
        //if we found a processor to steal from
        if (foundVictim) {
            int numToSteal = qArray.get(victimIndex).size() / 2;
            dequeTasks(thief.getId(), victimIndex, numToSteal);        //Dequeue "numToSteal" taks from the victim, into the thief's queue
        } else {
            try {
                vm.await(currentVersion);                    //wait for new tasks in the pool
            } catch (InterruptedException ignored) {
            }                //new tasks spawned, try to steal again
        }
    }

    private void dequeTasks(int thief, int victim, int numOfTasks) {
        for (int i = 0; i < numOfTasks; i++) {
            Task<?> stolen = qArray.get(victim).pollLast();
            if (stolen != null) {
                qArray.get(thief).addFirst(stolen);
            }
        }
    }
}
