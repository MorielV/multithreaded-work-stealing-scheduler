/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.spl.a2.sim;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.io.ObjectOutputStream;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.io.FileOutputStream;

import bgu.spl.a2.Deferred;
import bgu.spl.a2.WorkStealingThreadPool;
import bgu.spl.a2.sim.InitSimulation.plans;
import bgu.spl.a2.sim.InitSimulation.tools;
import bgu.spl.a2.sim.conf.ManufactoringPlan;
import bgu.spl.a2.sim.tasks.ManufactoringTask;
import bgu.spl.a2.sim.tools.GcdScrewDriver;
import bgu.spl.a2.sim.tools.NextPrimeHammer;
import bgu.spl.a2.sim.tools.RandomSumPliers;


/**
 * A class describing the simulator for part 2 of the assignment
 */
public class Simulator {
	static Warehouse house=new Warehouse();
	static WorkStealingThreadPool pool;
	static int numOfThreads;
	static tools[] toolArray;
	static plans[] planArray;
	static Wave[] waveArray;
	/**
	 * initialize the simulator-
	 * parse JSON file
	 * get Tools for the Warehouse
	 * get Plans
	 * get Waves 
	 * @param init
	 */
	public static long getNumOfTasks(int waveNum){
		long counter=0;
			Order[] orders=waveArray[waveNum].getOrders();
			for(int j=0; j<orders.length; j++)
				counter=counter+orders[j].getQty();	
		return counter;	
	}
	
	public static void initialize(InitSimulation init){	
		//Initiate Number of Threads
				numOfThreads=init.getThreads();
		//add Tools to Warehouse!
				 toolArray=init.getTools();
				for(int i=0; i<toolArray.length; i++){	
					if(toolArray[i].tool.equals("gs-driver"))
						house.addTool(new GcdScrewDriver(), toolArray[i].qty);
					else if(toolArray[i].tool.equals("np-hammer"))
						house.addTool(new NextPrimeHammer(),toolArray[i].qty);
					else if(toolArray[i].tool.equals("rs-pliers"))
						house.addTool(new RandomSumPliers(),toolArray[i].qty);
				}	
		//Add Plans;
				 planArray=init.getPlans();
				for(int i=0; i<planArray.length; i++)
					house.addPlan(new ManufactoringPlan(planArray[i].product,planArray[i].parts,planArray[i].tools ));
		//Add waves
				waveArray=new Wave[init.getWaves().length];
				
				for(int i=0; i<init.getWaves().length; i++){		//for every WAVE
					
					int numOfOrders=init.getWaves()[i].length;		//	create Wave (includes an array or orders)
					Order[] orderArray=new Order[numOfOrders];
					waveArray[i]=new Wave(numOfOrders);				//call Wave constuctor for each wave in the array
					for(int j=0; j<numOfOrders; j++){
						String product=init.getWaves()[i][j].getProduct();
						long qty=init.getWaves()[i][j].getQty();
						long startId=init.getWaves()[i][j].getStartId();
						orderArray[j]=new Order(product, qty, startId);			//order created!
					}
					waveArray[i].setWave(orderArray);							//Set this wave (which is an Order array)
				}
	}
	
	/**
	* Begin the simulation
	* Should not be called before attachWorkStealingThreadPool()
	*/
    public static ConcurrentLinkedQueue<Product> start(){
		pool.start();
		int waveNum=0;			//the wave we currently handle
		ConcurrentLinkedQueue<Product> productQueue= new ConcurrentLinkedQueue<Product>();
		ConcurrentLinkedQueue<Deferred<Product>> defQueue=new ConcurrentLinkedQueue<Deferred<Product>>();	//to make sure Output order == input-Order
		while(waveNum<waveArray.length){
	    	AtomicLong waveCountDown=new AtomicLong(getNumOfTasks(waveNum));	//num of manufacturing tasks in this wave
			CountDownLatch l=new CountDownLatch(1);								//to indicate when a wave is done
			Order[] currentWave=waveArray[waveNum].getOrders();
			for(int i=0; i<currentWave.length; i++){
				Order currentOrder=currentWave[i];								//current order to handle
				for(int j=0; j<currentOrder.getQty(); j++){						//each order has "currentOrder.getQty()" phones to make
					ManufactoringTask currentTask=new ManufactoringTask(new Product(currentOrder.getStartId()+j, currentOrder.getProductName()), house);
					defQueue.add(currentTask.getResult());						//add this tasks's deferred to the queue
					pool.submit(currentTask); 									//submit task to the pool
					
					currentTask.getResult().whenResolved(()->{
						if(waveCountDown.decrementAndGet()==0)
							l.countDown();		
						});
				}
			}
			try{
			l.await();
			}
			catch(InterruptedException e){}
			//Wave ended-get (and remove) products from the deferred Queue 
			while(!defQueue.isEmpty()){
				Deferred<Product> current=defQueue.poll();
				if(current!=null)
					productQueue.add(current.get());
			}
			waveNum++;
		}
		///WAVES FINISHED
		try {
			pool.shutdown();
		} catch (InterruptedException e) {}
			
		return productQueue;
		
		
		
    	
    	
    	
    	
    }
	
	/**
	* attach a WorkStealingThreadPool to the Simulator, this WorkStealingThreadPool will be used to run the simulation
	* @param myWorkStealingThreadPool - the WorkStealingThreadPool which will be used by the simulator
	*/
	public static void attachWorkStealingThreadPool(WorkStealingThreadPool myWorkStealingThreadPool){
		pool=myWorkStealingThreadPool;
		
		
	}
	
	public static int main(String [] args){
		
		FileReader reader=null;
		Gson gson=new Gson();
		
		try {
			reader=new FileReader(args[0]);
		} catch (FileNotFoundException e) {}
	
		InitSimulation init=gson.fromJson(reader, InitSimulation.class);
		
	initialize(init);				//Initialize the simulator 
	attachWorkStealingThreadPool(new WorkStealingThreadPool(numOfThreads));
	ConcurrentLinkedQueue<Product> pq=new ConcurrentLinkedQueue<Product>();
	pq=start();

	FileOutputStream fout;
	
	Iterator<Product> iterator=pq.iterator();
	int i=0;
	while(iterator.hasNext()){
		i++;
		Product current=iterator.next();
		System.out.println(i+" Name: "+current.getName()+"     finallId:          "+current.getFinalId());
		
	}
	try {
		fout = new FileOutputStream("result.ser");
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(pq);
		oos.close();
	} catch (IOException e) { 
	}

		
		return 0;
		}
		
	}
