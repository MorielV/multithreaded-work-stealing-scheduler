package bgu.spl.a2.sim;




class InitSimulation{

	int threads;
	tools[] tools;
	plans[] plans;
	waves[][] waves;
	
class tools	{	
	String tool;
	int qty;
	

	public String toString(){
		return this.tool;
	}
	public int getQty(){
		return this.qty;
	}
}
class plans{
	String[] parts;
	String[] tools;
	String product;
	
	public String[] getParts(){
		return this.parts;
	}
	public String[] getTools(){
		return this.tools;
	}
	public String getProduct(){
		return this.product;
	}
}
class waves{
	String product;
	long startId;
	long qty;
	public String getProduct() {
		return product;
	}
	public long getStartId() {
		return startId;
	}
	public long getQty() {
		return qty;
	}
}

public int getThreads(){
	return this.threads;
}

public tools[] getTools(){
	return this.tools;
}
public plans[] getPlans(){
	return this.plans;
}
public bgu.spl.a2.sim.InitSimulation.waves[][] getWaves(){
	return this.waves;
}
}


	
	
	




