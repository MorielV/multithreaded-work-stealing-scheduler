/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.spl.a2.test;

import bgu.spl.a2.Task;
import bgu.spl.a2.WorkStealingThreadPool;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class MergeSort extends Task<int[]> {

    private final int[] array;
    public MergeSort(int[] array) {
        this.array = array;
    }
    public int[] merge(int[] a1, int[] a2){
       	int[] toReturn=new int[a1.length+a2.length];
       	int returnIndex=0;
       	int index1=0;
       	int index2=0;
       	while(returnIndex<toReturn.length){
       		
       		if(index2<a2.length&&index1<a1.length&&a1[index1]<a2[index2]){
       			toReturn[returnIndex]=a1[index1];
       			index1++;
       			returnIndex++;
       			
       		}
       		else if(index2<a2.length&&index1<a1.length&&a1[index1]>=a2[index2]){
       			toReturn[returnIndex]=a2[index2];
       			index2++;
       			returnIndex++;       		
       		}
       		else if(index1>=a1.length){
       			
       			toReturn[returnIndex]=a2[index2];
       			index2++;
       			returnIndex++;
       		}
       		else if(index2>=a2.length){
       			
       			toReturn[returnIndex]=a1[index1];
       			index1++;
       			returnIndex++;
       			
       		}
       	}
       	return toReturn;
    }
    @Override
    protected void start() {
    	if(array.length>1){
    	int [] leftArray=Arrays.copyOfRange(this.array, 0, this.array.length/2);
    	int [] rightArray=Arrays.copyOfRange(this.array, this.array.length/2, this.array.length);
    	MergeSort left=new MergeSort(leftArray);
    	MergeSort right=new MergeSort(rightArray);
    	LinkedList<MergeSort> toResolve=new LinkedList<MergeSort>();
    	//spawn
    	spawn(left);
    	spawn(right);
    	toResolve.add(left);
    	toResolve.add(right);
    	
    	this.whenResolved(toResolve, ()->{
    		int[] leftReadyArray=toResolve.get(0).getResult().get();		//left sorted array
    		int[] rightReadyArray=toResolve.get(1).getResult().get();	//right sorted array
    		complete(merge(leftReadyArray,rightReadyArray));
    	});
    	}
    	else
    		complete(array);
    	
    } 
    public static void main(String[] args) throws InterruptedException {
   
    	WorkStealingThreadPool pool = new WorkStealingThreadPool(4);
        int n = 1000000; //you may check on different number of elements if you like
        int[] array = new Random().ints(n).toArray();
        MergeSort task = new MergeSort(array);
        CountDownLatch l = new CountDownLatch(1);
        pool.start();
        pool.submit(task);
        task.getResult().whenResolved(() -> {
            //warning - a large print!! - you can remove this line if you wish
        	System.out.println(Arrays.toString(task.getResult().get()));
        	System.out.println(n);
            l.countDown();
        });
        l.await();
        pool.shutdown();
    }

}
