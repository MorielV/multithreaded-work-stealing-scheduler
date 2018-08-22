package bgu.spl.a2;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Describes a monitor that supports the concept of versioning - its idea is
 * simple, the monitor has a version number which you can receive via the method
 * {@link #getVersion()} once you have a version number, you can call
 * {@link #await(int)} with this version number in order to wait until this
 * version number changes.
 *
 * you can also increment the version number by one using the {@link #inc()}
 * method.
 *
 */
class VersionMonitor {

	private AtomicInteger version=new AtomicInteger(0); 			//represents version (amount of sub tasks of the Task)
	
	
	/**
	 * @pre: none
	 * @post: getVersion()==this.version
	 * 
	 * @return version (int)
	 */
	synchronized int getVersion() {
		return this.version.get();
    }
	/**
	 * @pre: none
	 * @post: this.getVersion()-1==@pre(this)

	 * @return none
	 */
    synchronized void inc() {
       this.version.incrementAndGet();
       this.notifyAll(); 			//wake all waiting threads up
    }

    /**
     * @pre: version==getVersion()
     * @post: getVersion!=pre(getVersion)
     * @param version the value ,we waiting for it to change
     */
    synchronized void await(int version) throws InterruptedException {
    	//while the version doesn't change, wait.
    	while(version==this.version.get()){
    		this.wait();
    	}
    	throw new InterruptedException();
    }
}
