package bgu.spl.a2.sim.tasks;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import bgu.spl.a2.Deferred;
import bgu.spl.a2.Task;
import bgu.spl.a2.sim.Product;
import bgu.spl.a2.sim.Warehouse;
import bgu.spl.a2.sim.conf.ManufactoringPlan;
import bgu.spl.a2.sim.tools.Tool;

public class ManufactoringTask extends Task<Product> {

	Product product;					//product DESCRIPTION
	Warehouse house;					//Warehouse - with plans
	
	
	//Constructor
	public ManufactoringTask(Product product ,Warehouse house){
		this.product=product;
		this.house=house;
	}
	
	protected LinkedList<Deferred<Tool>>  getAllTools(ManufactoringPlan plan){
		LinkedList<Deferred<Tool>> toolList=new LinkedList<Deferred<Tool>>();				//tool list
		String[] toolsAsStrings=plan.getTools();
		if(plan.getParts().length!=0){		//IF acquiring tools is even needed
		for (int i=0; i<toolsAsStrings.length; i++)
			toolList.add(house.acquireTool(toolsAsStrings[i]));		
		}
		return toolList;
		
	}
	
	protected void start() {
		
		ManufactoringPlan productPlan=house.getPlan(product.getName());			//plan for this product		
		LinkedList <ManufactoringTask> neededPartsAsTasks=new LinkedList<ManufactoringTask>();//Parts (as Tasks) that "this" depends on	
		
		String[] partNames=productPlan.getParts();				//name of each part(Product) that this Product needs
		String[] toolNames=productPlan.getTools();				//Names of Tools needed to create this Product

		if(partNames.length!=0 && toolNames.length!=0){	//if there are any parts AND tools for this Product
		for(int i=0; i<partNames.length; i++)			//add Parts (as tasks) to "this" Product
			this.product.addPart(new Product(this.product.getStartId()+1, partNames[i]));
		Iterator<Product> partsIterator=product.getParts().iterator();	//iterator for the parts (as Products)
		while(partsIterator.hasNext()){									//Create all the sub products (Parts)
			ManufactoringTask current=new ManufactoringTask(partsIterator.next(), house);
			neededPartsAsTasks.add(current);											//create a new sub-task and add to List
			this.spawn(current);														//spawn each sub-task
		}
		
		AtomicInteger timesUsed=new AtomicInteger(toolNames.length);			//num of tools to use
		AtomicLong afterUse=new AtomicLong(product.getStartId());			//will be the ID of the manufactured task		
		whenResolved(neededPartsAsTasks, ()->{		//when all subProducts are done (meaning their Tasks' Deferred values are resolved)
				for (String toolName :toolNames){										//for each tool name
					Deferred<Tool> currentTool=house.acquireTool(toolName);				// acquire tool from warehouse	
					currentTool.whenResolved(()->{												//when the tool is resolved, USE IT
						afterUse.addAndGet(currentTool.get().useOn(this.product));				//add the result of using tool "t" on product "currentSubProduct) (WHY CASTING???)
					
						ToolRelease rTool=new ToolRelease(currentTool.get(), this.house);
						spawn(rTool);
					//	house.releaseTool(currentTool.get());//RELEASE THE TOOL
						if(timesUsed.decrementAndGet()==0){								//if all tools were used
							product.setFinalId(afterUse.get()); 						//set "this" product's final Id
							this.complete(this.product);								
						}
					});									
				}			
	    	});
		
	}
	 else{		//meaning this product does not have any parts!!
		 	this.product.setFinalId(this.product.getStartId()); 		//finalId=startId
			this.complete(this.product);
		
	 }
	
	}
}
