package bgu.spl.a2.sim.tools;

import java.math.BigInteger;
import bgu.spl.a2.sim.Product;

public class GcdScrewDriver implements Tool {

	 public long useOn(Product p){
	    	long value=0;
	    	for(Product part : p.getParts()){
	    		value+=Math.abs(operate(part.getFinalId()));
	    		
	    	}
	      return value;
	    }
	 
	public long operate(long id){
    	BigInteger b1 = BigInteger.valueOf(id);
        BigInteger b2 = BigInteger.valueOf(reverse(id));
        long value= (b1.gcd(b2)).longValue();
        return value;
    }
		
	public long reverse(long n){
    long reverse=0;
    while( n != 0 ){
        reverse = reverse * 10;
        reverse = reverse + n%10;
        n = n/10;
    }
    return reverse;
  }

	@Override
	public String getType() {
		return "gs-driver";
	}

	

}
