package com.m0pt0pmatt.LandPurchasing.flags;

import com.sk89q.worldguard.protection.flags.Flag;

/**
 * A LandFlag represents a WorldGuard region flag, along with its price and other details
 * @author Matthew
 *
 */
public class LandFlag {

	private Flag<?> flag;
	private boolean costScales;
	private double cost;
	
	public LandFlag(Flag<?> flag, boolean costScales, double cost){
		this.flag = flag;
		this.cost = cost;
		this.costScales = costScales;
	}
	
	public LandFlag(Flag<?> flag){
		this.flag = flag;
		this.cost = 0;
	}
	
	public Flag<?> getFlag(){
		return flag;
	}
	
	public double getCost(){
		return cost;
	}
	
	public boolean costScales(){
		return costScales;
	}

}
