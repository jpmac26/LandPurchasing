/**
 * 
 */
package com.m0pt0pmatt.LandPurchasing;

import java.util.ArrayList;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;

/**
 * Object for worldguard regions
 * @author Lucas Stuyvesant
 */
public class Land {

	ProtectedCuboidRegion land;
	ArrayList<BlockVector2D> chunks;
	String id;
	double costPerBlock;

	public Land(){}
	
	public Land(ProtectedCuboidRegion region){
		land = region;
		chunks = new ArrayList<BlockVector2D>();
		addChunks(region);
		int xDim = region.getMaximumPoint().getBlockX() - region.getMinimumPoint().getBlockX() + 1;
		int yDim = region.getMaximumPoint().getBlockY() - region.getMinimumPoint().getBlockY() + 1;
		int zDim = region.getMaximumPoint().getBlockZ() - region.getMinimumPoint().getBlockZ() + 1;
		costPerBlock = getCost(xDim, yDim, zDim) / (xDim * yDim * zDim);
	}
	
	/**
	 * Adds the chunks spanned by the region to the list
	 * @param region
	 */
	private void addChunks(ProtectedCuboidRegion region) {
		int xDim = 0;
		int xMin = region.getMinimumPoint().getBlockX();
		int xMax = region.getMaximumPoint().getBlockX();
		int zDim = 0;
		int zMin = region.getMinimumPoint().getBlockZ();
		int zMax = region.getMaximumPoint().getBlockZ();
		int i;

		//get number of chunks in x direction
		if(xMin % 16 != 0){
			xDim++;
		}
		i = xMin + (16 - xMin % 16);
		while(i < xMax){
			i += 16;
			xDim++;
		}
		if(xMax % 16 != 0){
			xDim++;
		}
		
		//get number of chunks in z direction
		if(zMin % 16 != 0){
			zDim++;
		}
		i = zMin + (16 - zMin % 16);
		while(i < zMax){
			i += 16;
			zDim++;
		}
		if(zMax % 16 != 0){
			zDim++;
		}
		
		//build chunks and add to list
		BlockVector2D c;
		for(int x = 0; x < xDim; x++){
			for(int z = 0; z < zDim; z++){
				if(!chunks.contains(c = new BlockVector2D(Math.floor(xMin + 16 * x),Math.floor(zMin + 16 * z)))){
					chunks.add(c);
				}
			}
		}
	}

	/**
	 * Method for generating the cost of a selection of land
	 * @param height height of the selection
	 * @param length length of the selection
	 * @param width width of the selection
	 * @return the cost of the given dimentions
	 */
	private double getCost(double height, double length, double width){
		return (5 + height)*(length * width);
	}

}
