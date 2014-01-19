package com.m0pt0pmatt.LandPurchasing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import com.m0pt0pmatt.LandPurchasing.flags.CustomFlag;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * The land listener adds extra functionality to land plots across the server
 * @author Matthew
 *
 */
public class LandListener implements Listener{

	
	@EventHandler
	public void onPistonExtend(BlockPistonExtendEvent event){
		
		World world = event.getBlock().getWorld();
		
		//get the region manager for the world
		RegionManager regionManager = LandPurchasing.wgplugin.getRegionManager(world);
		
		List<Block> pushedBlocks = event.getBlocks();
		
		Block pistonBlock = event.getBlock();
		
		Vector v = new Vector(pistonBlock.getX(), pistonBlock.getY(), pistonBlock.getZ());
		Set<String> pistonSet = new HashSet<String> (regionManager.getApplicableRegionsIDs(v));
		
		int x = 0;
		int y = 0;
		int z = 0;
		switch(event.getDirection()){
		case NORTH:
			z = -1;
			break;
		case SOUTH:
			z = 1;
			break;
		case EAST:
			x = 1;
			break;
		case WEST:
			x = -1;
			break;
		case UP:
			y = 1;
			break;
		case DOWN:
			y = -1;
			break;
		default:
			break;
		}
		
		for (Block block: pushedBlocks){
			v = new Vector(block.getX() + x, block.getY() + y, block.getZ() + z);
			Set<String> blockSet = new HashSet<String> (regionManager.getApplicableRegionsIDs(v));
			
			if (!blockSet.equals(pistonSet)){
				
				for (String regionName: blockSet){
										
					ProtectedRegion region = regionManager.getRegion(regionName);
					State state = region.getFlag(new StateFlag("outside-pistons", false));
					
					if (state == null || state.equals(State.DENY)){
						event.setCancelled(true);
						return;
					}
				}
			}
		}
		
	}
	
	@EventHandler
	public void onPistonRetract(BlockPistonRetractEvent event){
				
		if (!event.isSticky()) return;
				
		World world = event.getBlock().getWorld();
		
		//get the region manager for the world
		RegionManager regionManager = LandPurchasing.wgplugin.getRegionManager(world);
		
		Location retractedBlockLocation = event.getRetractLocation();
		
		Location pistonLocation = event.getBlock().getLocation();
		
		Vector v = new Vector(retractedBlockLocation.getX(), retractedBlockLocation.getY(), retractedBlockLocation.getZ());
		Set<String> newSet = new HashSet<String> (regionManager.getApplicableRegionsIDs(v));
		
		v = new Vector(pistonLocation.getX(), pistonLocation.getY(), pistonLocation.getZ());
		Set<String> oldSet = new HashSet<String> (regionManager.getApplicableRegionsIDs(v));
		
		if (!newSet.equals(oldSet)){
			
			for (String regionName: newSet){
				
				ProtectedRegion region = regionManager.getRegion(regionName);
				
				Object state = region.getFlag(CustomFlag.OUTSIDEPISTONS.getFlag().getFlag());
								
				if (state == null || state.equals(State.DENY)){
					event.setCancelled(true);
					return;
				}
			}
			for (String regionName: oldSet){
				
				ProtectedRegion region = regionManager.getRegion(regionName);
				
				Object state = region.getFlag(CustomFlag.OUTSIDEPISTONS.getFlag().getFlag());
								
				if (state == null || state.equals(State.DENY)){
					event.setCancelled(true);
					return;
				}
			}
		}
		
	}
}
