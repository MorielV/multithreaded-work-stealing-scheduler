package bgu.spl.a2.sim;


//represents a Wave -each one has an array of orders
public class Wave {
	Order[] orders;

	public Wave(int numOfOrders){
		orders=new Order[numOfOrders];
		
	}
	public Order[] getOrders() {
		return orders;
	}
	public void setWave(Order[] orderArray){		//set the wave
		this.orders=orderArray;
	}

	
	
	
	

}
