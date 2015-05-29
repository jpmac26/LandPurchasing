package com.m0pt0pmatt.LandPurchasing.Scheduling;

/**
 * Friendly date-tracker for storing simple date information on a more consistent basis than
 * java.util.Date
 * @author Skyler
 *
 */
public class Date implements Comparable<Date> {
	
	private int year;
	private int month;
	private int day;
	
	
	public Date(int year, int month, int day) {
		this.year = year;
		this.month = month;
		this.day = day;
	}
	
	
	
	public int getYear() {
		return year;
	}



	public void setYear(int year) {
		this.year = year;
	}



	public int getMonth() {
		return month;
	}



	public void setMonth(int month) {
		this.month = month;
	}



	public int getDay() {
		return day;
	}



	public void setDay(int day) {
		this.day = day;
	}



	public String toString() {
		return "Day: " + day + "  Month: " + month + "  Year: " + year;
	}
	
	@Override
	public int compareTo(Date arg0) {
		
		return (hashCode() - arg0.hashCode());
		
	}
	
	@Override
	public int hashCode() {
		int hash = 0;
		
		hash += day;
		hash += month * 100;
		hash += year * 100 * 100;
		return hash;
	}

}
