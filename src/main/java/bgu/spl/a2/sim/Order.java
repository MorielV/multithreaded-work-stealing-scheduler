package bgu.spl.a2.sim;

public class Order {
	String productName;
	long qty;
	long startId;
	
	public Order(String productName, long qty, long startId){
		this.productName=productName;
		this.qty=qty;
		this.startId=startId;
	}
	
	public String getProductName() {
		return productName;
	}

	public void setProduct(String productName) {
		this.productName = productName;
	}

	public long getQty() {
		return qty;
	}

	public long getStartId() {
		return startId;
	}

	
	
	
	
	
	
	

}
