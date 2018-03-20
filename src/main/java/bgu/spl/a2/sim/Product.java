package bgu.spl.a2.sim;

import java.util.LinkedList;
import java.util.List;

/**
 * A class that represents a product produced during the simulation.
 */
public class Product implements java.io.Serializable {
	String name;
	long startId;
	long finalId;
	LinkedList<Product> partList;
	
	/**
	* Constructor 
	* @param startId - Product start id
	* @param name - Product name
	*/
    public Product(long startId, String name){
    	this.name=name;
    	this.startId=startId;
    	partList=new LinkedList<Product>();
    }

	/**
	* @return The product name as a string
	*/
    public String getName(){
    	return this.name;
    }

	/**
	* @return The product start ID as a long. start ID should never be changed.
	*/
    public long getStartId(){
    	return this.startId;
    }
    
	/**
	* @return The product final ID as a long. 
	* final ID is the ID the product received as the sum of all UseOn(); 
	*/
    public long getFinalId(){
    	return this.finalId;
    }
    /**
     * set the final Id of this product
     * after Manufacturing it
     * @param finalId
     */

    public void setFinalId(long finalId){
    	this.finalId=finalId;
    }
	/**
	* @return Returns all parts of this product as a List of Products
	*/
    public LinkedList<Product> getParts(){
    	return this.partList;
    }

	/**
	* Add a new part to the product
	* @param p - part to be added as a Product object
	*/
    public void addPart(Product p){
    	this.partList.add(p);
    	
    	
    }


}