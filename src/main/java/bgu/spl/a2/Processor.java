package bgu.spl.a2;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * this class represents a single work stealing processor, it is
 * {@link Runnable} so it is suitable to be executed by threads.
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 *
 */
public class Processor implements Runnable {

    private final WorkStealingThreadPool pool;
    private final int id;
    /**
     * constructor for this class
     *
     * IMPORTANT:
     * 1) this method is package protected, i.e., only classes inside
     * the same package can access it - you should *not* change it to
     * public/private/protected
     *
     * 2) you may not add other constructors to this class
     * nor you allowed to add any other parameter to this constructor - changing
     * this may cause automatic tests to fail..
     *
     * @param id - the processor id (every processor need to have its own unique
     * id inside its thread pool)
     * @param pool - the thread pool which owns this processor
     */
    /*package*/ Processor(int id, WorkStealingThreadPool pool) {
        this.id = id;
        this.pool = pool;
    }
    //getter (package protected)s
    int getId(){
    	return this.id;
    }
    //getter (package protected)
    WorkStealingThreadPool getPool(){
    	return this.pool;
    }
    @Override
    public void run() {
    	while(!this.pool.shutDown){
        	ConcurrentLinkedDeque<Task<?>> myQ=this.pool.getQarray().get(this.id);	//get access to this processsor's queue
        	int currentVersion=this.getPool().vm.getVersion();
        	if(myQ.isEmpty()){														//try stealing
        		pool.steal(this, currentVersion);
    		}
    		//reaching this line means the queue is NOT empty anymore OR we were interrupted
    		else if(!this.pool.shutDown && !myQ.isEmpty() )	{	//if this thread is not interrupted    		 
    			Task<?> toRun=myQ.pollFirst();
    			if(toRun!=null)
    				toRun.handle(this);															//handle the task (with the current Processor)
    			
    		}
    	}
    }
    
   
    }