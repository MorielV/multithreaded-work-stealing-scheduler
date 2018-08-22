package bgu.spl.a2;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * this class represents a deferred result i.e., an object that eventually will
 * be resolved to hold a result of some operation, the class allows for getting
 * the result once it is available and registering a callback that will be
 * called once the result is available.
 *
 * @param <T> the result type
 */
public class Deferred<T> {

    private T resolvedValue;
    private boolean isResolved = false;                                                        //will be true after Resolved was called
    private ConcurrentLinkedQueue<Runnable> callBacksQ = new ConcurrentLinkedQueue<>();    //list of Runnable's

    protected ConcurrentLinkedQueue<Runnable> getCallBacksQ() {
        return this.callBacksQ;
    }

    /**
     * @return the resolved value if such exists (i.e., if this object has been
     * {@link #resolve(java.lang.Object)}ed yet
     * @throws IllegalStateException in the case where this method is called and
     *                               this object is not yet resolved
     */
    public T get() {
        if (isResolved())
            return this.resolvedValue;
        else
            throw new IllegalStateException("Not yet Resolved");
    }

    /**
     * @return true if this object has been resolved - i.e., if the method
     * {@link #resolve(java.lang.Object)} has been called on this object before.
     * @pre: isResolved==true || isResolved==false
     * @post:
     */
    private boolean isResolved() {
        return (this.isResolved);
    }

    /**
     * resolve this deferred object - from now on, any call to the method
     * {@link #get()} should return the given value
     * <p>
     * Any callbacks that were registered to be notified when this object is
     * resolved via the {@link #whenResolved(java.lang.Runnable)} method should
     * be executed before this method returns
     *
     * @param value - the value to resolve this deferred object with
     * @throws IllegalStateException in the case where this object is already
     *                               resolved
     * @pre: isResolved==false;
     * @post: isResolved=true &&  resolvedValue!=null
     * @post: callBacksList==null (all callbacks were called!!)
     */

    public void resolve(T value) {
        if (this.isResolved)                                    //if this is already resolved
            throw new IllegalStateException();
        else {
            this.resolvedValue = value;                        //set the value
            this.isResolved = true;
            while (!callBacksQ.isEmpty()) {        //run all runnables
                Runnable current = callBacksQ.poll();
                if (current != null)
                    current.run();
            }
        }
    }

    /**
     * add a callback to be called when this object is resolved. if while
     * calling this method the object is already resolved - the callback should
     * be called immediately
     * <p>
     * Note that in any case, the given callback should never get called more
     * than once, in addition, in order to avoid memory leaks - once the
     * callback got called, this object should not hold its reference any
     * longer.
     *
     * @param callback the callback to be called when the deferred object is
     *                 resolved
     * @pre: callback!=null
     * @post:callBacksList size =pre.callBacksList+1
     */
    public synchronized void whenResolved(Runnable callback) {

        if (this.isResolved)            //if this deferred is resolved- run the callback
            callback.run();
        else                        //else, add.
            this.callBacksQ.add(callback);            //add a callback
    }

    public int numOfWaiting() {

        return this.callBacksQ.size();
    }
}
