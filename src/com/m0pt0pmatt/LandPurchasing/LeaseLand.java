package com.m0pt0pmatt.LandPurchasing;

import java.util.Date;

import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;

/**
 * Holds information about a plot of land that is able to be or is being leased out.<br />
 * These plots hold information about when their lease is up
 * @author Skyler
 */
public class LeaseLand extends Land {
	
	private Date dueDate;
	
	public LeaseLand(ProtectedCuboidRegion region) {
		super();
		dueDate = null;
	}
	
	public Date getDueDate() {
		return dueDate;
	}
	
	public void setDueDate(Date date) {
		dueDate = (Date) date.clone();
	}
}
