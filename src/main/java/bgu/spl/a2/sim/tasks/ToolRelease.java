package bgu.spl.a2.sim.tasks;

import bgu.spl.a2.Task;
import bgu.spl.a2.sim.Warehouse;
import bgu.spl.a2.sim.tools.Tool;

public class ToolRelease extends Task<Tool>{

	Tool tool;
	Warehouse house;
	public ToolRelease(Tool tool, Warehouse house){
		this.tool=tool;
		this.house=house;
	}
	protected void start() {
		house.releaseTool(tool);
		

		
		
		
		
		
	}

}
