package com.m0pt0pmatt.LandPurchasing.Effects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.m0pt0pmatt.LandPurchasing.LandPurchasing;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;

/**
 * Special effect that replaces the exterior skin of a given cuboid for a time, and
 * then reverts it back to air
 * @author Skyler
 *
 */
public class AreaView implements Runnable {
	
	private List<Block> blocks;
	
	public AreaView(ProtectedCuboidRegion region) {
		
		BlockVector min, max;
		min = region.getMinimumPoint();
		max = region.getMaximumPoint();
		World world = Bukkit.getWorld("Homeworld"); //TODO make dynamic?
		
		//do some light math to set up an array for our blocks
		int w,h,l;
		w = max.getBlockX() - min.getBlockX();
		h = max.getBlockY() - min.getBlockY();
		l = max.getBlockZ() - min.getBlockZ();
		
		blocks = new ArrayList<Block>((2*w*l) + (2*w*h) + (2*l*h));
		
		//do two edges that have the whole face first
		for (int j = min.getBlockY(); j <= max.getBlockY(); j++)
		for (int k = min.getBlockZ(); k <= max.getBlockZ(); k++) {
			addBlock(world.getBlockAt(min.getBlockX(), j, k));
			addBlock(world.getBlockAt(max.getBlockX(), j, k));
		}
		
		//now go in a tube fashion, moving in the positive x direction
		
		for (int i = min.getBlockX() + 1; i <= max.getBlockX() - 1; i++) {
			//for ever layer here, we have a square
			//add the two sides that are on the extreme Y's
			for (int k = min.getBlockZ(); k <= max.getBlockZ(); k++) {
				addBlock(world.getBlockAt(i, min.getBlockY(), k));
				addBlock(world.getBlockAt(i, max.getBlockY(), k));
			}
			
			//finally grab the other two edges from min.y+1 to max.y-1
			for (int j = min.getBlockY() + 1; j <= max.getBlockY() - 1; j++) {
				addBlock(world.getBlockAt(i, j, min.getBlockZ()));
				addBlock(world.getBlockAt(i, j, max.getBlockZ()));
			}
		}
		
		//got all our blocks, not setup and run later
		setup();
		
		Bukkit.getScheduler().runTaskLater(LandPurchasing.plugin, this, 20 * 5);
	}
	
	/**
	 * Sets up an area view effect with the passed collection of blocks.<br/>
	 * <b>This constructor makes no check</b> against what's at the block position, and
	 * will overwrite what's there with glass and air!
	 * @param blocks
	 */
	public AreaView(Collection<Block> blocks) {
		this.blocks = new ArrayList<Block>(blocks);
		setup();
		
		//break down in 15 seconds
		Bukkit.getScheduler().runTaskLater(LandPurchasing.plugin, this, 20 * 5);
	}
	
	/**
	 * Go through and change all blocks to be glass
	 */
	private void setup() {
		for (Block block : blocks) {
			block.setType(Material.GLASS);
		}
	}
	
	/**
	 * Adds the block if it is Air, but doesn't if it is something else.<br />
	 * convenience method
	 * @param block
	 */
	private void addBlock(Block block) {
		if (block.getType().equals(Material.AIR)) {
			blocks.add(block);
		}
	}
	
	public void run() {
		for (Block block : blocks) {
			block.setType(Material.AIR);
		}
	}
	
}
