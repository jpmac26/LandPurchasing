package com.m0pt0pmatt.LandPurchasing;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class LandManager {
	
	/**
	 * Adds a member to a region
	 * @param sender
	 * @param memberName
	 * @param regionName
	 */
	public void addMember(CommandSender sender, String memberName, String regionName){
		
		//make sure the command executor is a player
		if(!(sender instanceof Player)){
			sender.sendMessage("Sorry, only players can execute this command");
			return;
		}
		
		//assuming if they have an account, they are a valid identity to add as a member
		if(!LandPurchasing.economy.hasAccount(memberName)){
			sender.sendMessage(memberName + " is not an existing player");
			return;
		}
		
		//get the region manager for the homeworld
		RegionManager rm = LandPurchasing.wgplugin.getRegionManager(Bukkit.getWorld("HomeWorld"));
		if (rm == null){
			sender.sendMessage("No region manager for the homeworld");
			return;
		}
		
		//get the region in question
		ProtectedRegion region = rm.getRegion(sender.getName() + "__" + regionName);
		if (region == null){
			sender.sendMessage("No such region was found.");
			return;
		}
		
		//make sure the command executor is an owner of the plot
		if(!region.isOwner(sender.getName())){
			sender.sendMessage("You are not the owner of the specified region");
			return;
		}
		
		//add the player
		DefaultDomain d = region.getMembers();
		d.addPlayer(memberName);
		region.setMembers(d);
	}
	
	/**
	 * Removes a member from a region
	 * @param sender
	 * @param memberName
	 * @param regionName
	 */
	public void removeMember(CommandSender sender, String memberName, String regionName){
	
		//make sure the command executor is a player
		if(!(sender instanceof Player)){
			sender.sendMessage("Sorry, only players can execute this command");
			return;
		}
		
		//get the region manager for the homeworld
		RegionManager rm = LandPurchasing.wgplugin.getRegionManager(Bukkit.getWorld("HomeWorld"));
		if (rm == null){
			sender.sendMessage("No region manager for the homeworld");
			return;
		}
		
		//get the region in question
		ProtectedRegion region = rm.getRegion(sender.getName() + "__" + regionName);
		if (region == null){
			sender.sendMessage("No such region was found.");
			return;
		}
		
		//make sure the command executor is an owner of the plot
		if(!region.isOwner(sender.getName())){
			sender.sendMessage("You are not the owner of the specified region");
			return;
		}
		
		//get the members of the plot
		DefaultDomain d = region.getMembers();
		
		//check if the player actually was a member
		if(!d.contains(memberName)){
			sender.sendMessage("Player " + memberName + " is not a member of this region");
			return;
		}
		
		//remove the member from the plot
		d.removePlayer(memberName);
		region.setMembers(d);
	}
	
	/**
	 * Adds an owner to a region
	 * @param sender
	 * @param memberName
	 * @param regionName
	 */
	public void addOwner(CommandSender sender, String ownerName, String regionName){
		
		//make sure the command executor is a player
		if(!(sender instanceof Player)){
			sender.sendMessage("Sorry, only players can execute this command");
			return;
		}
		
		//assuming if they have an account, they are a valid identity to add as a member
		if(!LandPurchasing.economy.hasAccount(ownerName)){
			sender.sendMessage(ownerName + " is not an existing player");
			return;
		}
		
		//get the region manager for the homeworld
		RegionManager rm = LandPurchasing.wgplugin.getRegionManager(Bukkit.getWorld("HomeWorld"));
		if (rm == null){
			sender.sendMessage("No region manager for the homeworld");
			return;
		}
		
		//get the region in question
		ProtectedRegion region = rm.getRegion(sender.getName() + "__" + regionName);
		if (region == null){
			sender.sendMessage("No such region was found.");
			return;
		}
		
		//make sure the command executor is an owner of the plot
		if(!region.isOwner(sender.getName())){
			sender.sendMessage("You are not the owner of the specified region");
			return;
		}
		
		//add the player as an owner
		DefaultDomain d = region.getOwners();
		d.addPlayer(ownerName);
		region.setOwners(d);
	}
	
	public void removeOwner(CommandSender sender, String ownerName, String regionName){
		
		//make sure the command executor is a player
		if(!(sender instanceof Player)){
			sender.sendMessage("Sorry, only players can execute this command");
			return;
		}
		
		//get the region manager for the homeworld
		RegionManager rm = LandPurchasing.wgplugin.getRegionManager(Bukkit.getWorld("HomeWorld"));
		if (rm == null){
			sender.sendMessage("No region manager for the homeworld");
			return;
		}
		
		//get the region in question
		ProtectedRegion region = rm.getRegion(sender.getName() + "__" + regionName);
		if (region == null){
			sender.sendMessage("No such region was found.");
			return;
		}
		
		//make sure the command executor is an owner of the plot
		if(!region.isOwner(sender.getName())){
			sender.sendMessage("You are not the owner of the specified region");
			return;
		}
		
		//get the owners of the plot
		DefaultDomain d = region.getOwners();
		
		//check if the player actually was an owner
		if(!d.contains(ownerName)){
			sender.sendMessage("Player " + ownerName + " is not an owner of this region");
			return;
		}
		
		//remove the owner from the plot
		d.removePlayer(ownerName);
		region.setOwners(d);
	}

	/**
	 * Lists all regions a sender owns
	 * @param sender the player executing the command
	 */
	public void listRegions(CommandSender sender){
		sender.sendMessage("Here are all the regions you own:");
		
		//get the region manager for the homeworld
		RegionManager rm = LandPurchasing.wgplugin.getRegionManager(Bukkit.getWorld("HomeWorld"));
		if (rm == null){
			sender.sendMessage("No region manager for the homeworld");
			return;
		}
		
		//get all regions from the region manager
		Map<String, ProtectedRegion> regions = rm.getRegions();
		
		//check every region to see if the player owns it
		//this is costly but I don't know of a better way.
		for (ProtectedRegion r: regions.values()){
			
			//if the player owns the plot
			if (r.isOwner(sender.getName())){
				
				String plotName = r.getId();
				
				//make sure plot is a valid land plot
				if (plotName.startsWith(sender.getName())){
					sender.sendMessage(plotName.substring(sender.getName().length() + 2, plotName.length()));
				}
			}
		}
	}
	
	/**
	 * Prices the players selection of land
	 * @param sender
	 */
	public void getPrice(CommandSender sender){
		
		//make sure the command executor is a player
		if (!(sender instanceof Player)){
			sender.sendMessage("Sorry, only players can execute this command");
			return;
		}
		
		//make sure the player is in the right world
		if (!(Bukkit.getWorld("HomeWorld").getPlayers().contains(sender))){
			sender.sendMessage("Sorry, you have to be on the HomeWorld to buy land");
			return;
		}
		
		//get the WorldEdit selection
		Selection selection = LandPurchasing.weplugin.getSelection((Player) sender);
		
		//calculate cost of selection
		double height = selection.getMaximumPoint().getY() - selection.getMinimumPoint().getY() + 1;
		double length = selection.getMaximumPoint().getX() - selection.getMinimumPoint().getX() + 1;
		double width = selection.getMaximumPoint().getZ() - selection.getMinimumPoint().getZ() + 1;
		double cost = getCost(height, length, width);
		
		//tell the player
		sender.sendMessage("Your selection would cost " + cost + " to buy.");
	}
	
	/**
	 * Refunds a player for a plot of land he or she owns
	 * @param sender the player executing the command
	 * @param regionName the name of the region to be resold
	 */
	public void sellLand(CommandSender sender, String regionName){
		
		//make sure the command executor is a player
		if (!(sender instanceof Player)){
			sender.sendMessage("Sorry, only players can execute this command");
			return;
		}
		
		//make sure the player is in the right world
		if (!(Bukkit.getWorld("HomeWorld").getPlayers().contains(sender))){
			sender.sendMessage("Sorry, you have to be on the HomeWorld to buy land");
			return;
		}
		
		//make sure there is an economy
		if (LandPurchasing.economy == null){
			if (!LandPurchasing.setupEconomy()){
				sender.sendMessage("Error. No economy plugin loaded.");
				return;
			}
		}
		
		//get the region manager for the homeworld
		RegionManager rm = LandPurchasing.wgplugin.getRegionManager(Bukkit.getWorld("HomeWorld"));
		if (rm == null){
			sender.sendMessage("No region manager for the homeworld");
			return;
		}
		
		ProtectedRegion region = rm.getRegion(sender.getName() + "__" + regionName);
		if (region == null){
			sender.sendMessage("No such region was found.");
			return;
		}
		
		//find the value
		double height = region.getMaximumPoint().getY() - region.getMinimumPoint().getY() + 1;
		double length = region.getMaximumPoint().getX() - region.getMinimumPoint().getX() + 1;
		double width = region.getMaximumPoint().getZ() - region.getMinimumPoint().getZ() + 1;
		double cost = getCost(height, length, width);
		
		//remove the region
		rm.removeRegion(sender.getName() + "__" + regionName);
		
		//save WorldGuard
		try {
			rm.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//refund the player
		LandPurchasing.economy.depositPlayer(sender.getName(), cost);
		
		//notify the player
		sender.sendMessage("You have sold the plot of land and have been refunded its original cost of " + cost);
	}
	
	/**
	 * Allows a user to buy a selection of land, if the user has the funds
	 * @param sender the player executing the command
	 * @param name the name to be given to the newly purchased plot of land (This name is unique to the sender)
	 */
	public void buyLand(CommandSender sender, String name) {
		
		//make sure its a player
		if (!(sender instanceof Player)){
			sender.sendMessage("Sorry, only players can execute this command");
			return;
		}
		
		//make sure the player is in the right world
		if (!(Bukkit.getWorld("HomeWorld").getPlayers().contains(sender))){
			sender.sendMessage("Sorry, you have to be on the HomeWorld to buy land");
			return;
		}
		
		//make sure there is an economy
		if (LandPurchasing.economy == null){
			if (!LandPurchasing.setupEconomy()){
				sender.sendMessage("Error. No economy plugin loaded.");
				return;
			}
		}
		
		//get the players economy balance
		double money = LandPurchasing.economy.getBalance(sender.getName());
		
		//get the WorldEdit selection
		Selection selection = LandPurchasing.weplugin.getSelection((Player) sender);
		BlockVector b1 = new BlockVector(selection.getMinimumPoint().getX(), selection.getMinimumPoint().getY(), selection.getMinimumPoint().getZ());
		BlockVector b2 = new BlockVector(selection.getMaximumPoint().getX(), selection.getMaximumPoint().getY(), selection.getMaximumPoint().getZ());
		
		//calculate cost of selection
		double height = selection.getMaximumPoint().getY() - selection.getMinimumPoint().getY() + 1;
		double length = selection.getMaximumPoint().getX() - selection.getMinimumPoint().getX() + 1;
		double width = selection.getMaximumPoint().getZ() - selection.getMinimumPoint().getZ() + 1;
		double cost = getCost(height, length, width);
		
		//notify the player of the cost
		sender.sendMessage("cost: " + cost);
		
		//check for invalid name
		if (name.isEmpty() || name.equals(null)){
			sender.sendMessage("Bad name. Could not create region.");
			return;
		}
		
		//reassign the name to include the senders name, for uniqueness
		name = sender.getName() + "__" + name;
		
		//check if player has enough funds
		if (money < cost){
			sender.sendMessage("Not enough funds. Your selection costs " + cost + " but you only have " + money + ".");
			return;
		}
		
		//get the region manager for the homeworld
		RegionManager rm = LandPurchasing.wgplugin.getRegionManager(Bukkit.getWorld("HomeWorld"));
		if (rm == null){
			sender.sendMessage("No region manager for the homeworld");
			return;
		}
		
		//make sure name isn't already used
		if (rm.getRegion(name) != null){
			sender.sendMessage("I'm sorry, but that name is already in use.");
			return;
		}
		
		//make sure selection doesn't include other regions
		for (int x = b1.getBlockX(); x < b2.getBlockX() + 1; x++){
			for (int y = b1.getBlockY(); y < b2.getBlockY() + 1; y++){
				for (int z = b1.getBlockZ(); z < b2.getBlockZ() + 1; z++){
					if(!(rm.getApplicableRegionsIDs(new Vector(x,y,z)).isEmpty())){
						sender.sendMessage("I'm sorry, but your selection includes another region");
						return;
					}
				}
			}
		}
		
		//create WorldGuard Region
		ProtectedRegion region = new ProtectedCuboidRegion(name, b1, b2);
		
		//set proper flags
		LandPurchasing.flagManager.setDefaultFlags(region);
		
		//add the new region to WorldGuard
		rm.addRegion(region);
		
		//add player to the owner of the new region
		DefaultDomain newDomain = new DefaultDomain();
		newDomain.addPlayer(sender.getName());
		rm.getRegion(name).setOwners(newDomain);
		
		//save WorldGuard
		try {
			rm.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//remove funds from player
		LandPurchasing.economy.withdrawPlayer(sender.getName(), cost);
		
		//let the player know
		sender.sendMessage("Congradulations, you now own this region");
	}
	
	/**
	 * Method for generating the cost of a selection of land
	 * @param height height of the selection
	 * @param length length of the selection
	 * @param width width of the selection
	 * @return the cost of the given dimentions
	 */
	public static double getCost(double height, double length, double width){
		return (5 + height)*(length * width);
	}
}
