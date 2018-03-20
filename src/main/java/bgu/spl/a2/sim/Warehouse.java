package bgu.spl.a2.sim;

 
import bgu.spl.a2.sim.conf.ManufactoringPlan;
import bgu.spl.a2.sim.tools.GcdScrewDriver;
import bgu.spl.a2.sim.tools.NextPrimeHammer;
import bgu.spl.a2.sim.tools.RandomSumPliers;
import bgu.spl.a2.sim.tools.Tool;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.a2.Deferred;

/**
 * A class representing the warehouse in your simulation
 * 
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add to this class can
 * only be private!!!
 *
 */
public class Warehouse {

	ConcurrentLinkedQueue<GcdScrewDriver> gsd;								//gs-driver
	ConcurrentLinkedQueue<NextPrimeHammer> nph;							//np-hammer
	ConcurrentLinkedQueue<RandomSumPliers> rsp;							//rs-pliers
	ConcurrentLinkedQueue<ManufactoringPlan>planList;
	
	ConcurrentLinkedQueue<Deferred<Tool>> gsdWaitList;			//GcdScrewDriver waiting list
	ConcurrentLinkedQueue<Deferred<Tool>> nphWaitList;			//NextPrimeHammer waiting list
	ConcurrentLinkedQueue<Deferred<Tool>> rspWaitList;			//RandomSumPliers waiting list
	/**
	* Constructor
	*/
    public Warehouse(){
    	//queues of tools
    	gsd=new ConcurrentLinkedQueue<GcdScrewDriver>();
    	nph=new ConcurrentLinkedQueue<NextPrimeHammer>();
    	rsp=new ConcurrentLinkedQueue<RandomSumPliers>();
    	//queues of deferred's
    	gsdWaitList=new ConcurrentLinkedQueue<Deferred<Tool>>();
    	nphWaitList=new ConcurrentLinkedQueue<Deferred<Tool>>();
    	rspWaitList=new ConcurrentLinkedQueue<Deferred<Tool>>();
    	//queue of Plans
    	planList=new ConcurrentLinkedQueue<ManufactoringPlan>();
    }
    
    public void toolStatus(){
    	
    	System.out.println("---------------------- TOOL STATUS ----------------------------");
    	System.out.println("Number of GcdScrewDrivers is: " + gsd.size());
    	System.out.println();
    	System.out.println("Number of NextPrimeHammers is: " + nph.size());
    	System.out.println();
    	System.out.println("Number of RandomSumPliers is: " + rsp.size());
    	System.out.println();
    	//WAITING?
    	System.out.println("waiting on GcdScrewDrivers : " + gsdWaitList.size());
    	System.out.println();
    	System.out.println("waiting on NextPrimeHammers : " + nphWaitList.size());
    	System.out.println();
    	System.out.println("waiting on RandomSumPliers : " + rspWaitList.size());
    	System.out.println();
    	System.out.println("---------------------- ------------ ----------------------------");

    	
    	
    }
    
    
    
	/**
	* Tool acquisition procedure
	* Note that this procedure is non-blocking and should return immediately
	* @param type - string describing the required tool
	* @return a deferred promise for the  requested tool
	*/
    public Deferred<Tool> acquireTool(String type){
    	Deferred<Tool> def=new Deferred<Tool>();
    	if(type.equals("gs-driver")){
    			if(gsd.isEmpty())
    				gsdWaitList.add(def);
    			else{
    				try{
    				Tool toGive=gsd.remove();	//remove from list of GcdScrewDrivers
    				def.resolve(toGive);			//resolve a deferred with it
    				}
    				catch(NoSuchElementException e){
    					gsdWaitList.add(def);
    				}
    				}   				
    	}
    	else if(type.equals("np-hammer")){
    			if(nph.isEmpty())
    				nphWaitList.add(def);
    			else{
    				try{
    				Tool toGive=nph.remove();	//remove from list of NextPrimeHammer
    				def.resolve(toGive);			//resolve a deferred with it
    				}
    				catch(NoSuchElementException e){
    					gsdWaitList.add(def);
    				}
    			}
    	}
    	else if(type.equals("rs-pliers")){
			if(rsp.isEmpty())
				rspWaitList.add(def);
			else{
				try{
				Tool toGive=rsp.remove();	//remove from list of RandomSumPliers
				def.resolve(toGive);			//resolve a deferred with it
				}
				catch(NoSuchElementException e){
					gsdWaitList.add(def);
				}
			}
    	}
    	
    	return def;
    	
    
    }
    
	/**
	* Tool return procedure - releases a tool which becomes available in the warehouse upon completion.
	* @param tool - The tool to be returned
	*/
    public void releaseTool(Tool tool){
   
    	
    	String type=tool.getType();
    	if(type.equals("gs-driver")){
    			try{
    			(gsdWaitList.remove()).resolve(tool);
    			}
    			catch(NoSuchElementException e){
    				this.addTool(tool, 1);
    			}
    	}
    	else if(type.equals("np-hammer")){
    			try{
    			nphWaitList.remove().resolve(tool);
    			}
    			catch(NoSuchElementException e){
    				this.addTool(tool, 1);
    			}
    			
    	}
    	else if(type.equals("rs-pliers")){
    			try{
    			rspWaitList.remove().resolve(tool);
    			}
    			catch (NoSuchElementException e){
    				this.addTool(tool, 1);
    			}
    	}
    }

   
	
	/**
	* Getter for ManufactoringPlans
	* @param product - a string with the product name for which a ManufactoringPlan is desired
	* @return A ManufactoringPlan for product
	*/
    public ManufactoringPlan getPlan(String product){
    	
    	Iterator<ManufactoringPlan> iterator=planList.iterator();
    	boolean foundPlan=false;
    	ManufactoringPlan current=null;
    	while(iterator.hasNext() && !foundPlan){
    		 current=iterator.next();
    		if(current.getProductName().equals(product))
    			foundPlan=true;
    	}
    	
    	
    	
    		return current;
    	
    	
    }
	
	/**
	* Store a ManufactoringPlan in the warehouse for later retrieval
	* @param plan - a ManufactoringPlan to be stored
	*/
    public void addPlan(ManufactoringPlan plan){
    	this.planList.add(plan);
    	
    	
    }
    
	/**
	* Store a qty Amount of tools of type tool in the warehouse for later retrieval
	* @param tool - type of tool to be stored
	* @param qty - amount of tools of type tool to be stored
	*/
    public void addTool(Tool tool, int qty){
    	String type=tool.getType();
    	
    	if(type.equals("gs-driver")){
    		for(int i=0; i<qty; i++)
        		gsd.add(new GcdScrewDriver());
    	}
    	
    	else if(type.equals("np-hammer")){
    		for(int i=0; i<qty; i++)
        		nph.add(new NextPrimeHammer());
    	}
    	else{ 							//(type=="rs-pliers")
    		for(int i=0; i<qty; i++)
        		rsp.add(new RandomSumPliers());
        	
    	}
    
    	
    	
    	
    }

}