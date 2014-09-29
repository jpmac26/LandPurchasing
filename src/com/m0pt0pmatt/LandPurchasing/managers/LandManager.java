package com.m0pt0pmatt.LandPurchasing.managers;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.m0pt0pmatt.LandPurchasing.LandPurchasing;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class LandManager {
	
	@SuppressWarnings("unused")
	private LandPurchasing plugin;
	private WorldGuardPlugin wgplugin;
	
	public LandManager(LandPurchasing plugin, WorldGuardPlugin wgplugin) {
		this.plugin = plugin;
		this.wgplugin = wgplugin;
	}
	
	
	/**
	 * Adds a member to a region
	 * @param sender
	 * @param memberName
	 * @param regionName
	 */
	public void addMember(CommandSender sender, String regionName, String memberName){
		
		//make sure the command executor is a player
		if(!(sender instanceof Player)){
			sender.sendMessage("Sorry, only players can execute this command");
			return;
		}
		
		if (Bukkit.getPlayer(memberName) == null) {
			//player isn't currently online with thtat name
			sender.sendMessage("This player is not currently online!");
			return;
		}
		
		//assuming if they have an account, they are a valid identity to add as a member
		if(!LandPurchasing.economy.hasAccount(Bukkit.getOfflinePlayer(memberName))){
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
		ProtectedRegion region = rm.getRegion(((Player) sender).getUniqueId().toString() + "__" + regionName);
		if (region == null){
			sender.sendMessage("No such region was found.");
			return;
		}
		
		//make sure the command executor is an owner of the plot
		if(!region.isOwner(new BukkitPlayer(wgplugin, Bukkit.getPlayer(memberName)))){
			sender.sendMessage("You are not the owner of the specified region");
			return;
		}
		
		//add the player
		DefaultDomain d = region.getMembers();
		d.addPlayer(Bukkit.getPlayer(memberName).getUniqueId());
		region.setMembers(d);
		
		sender.sendMessage(memberName + " is now a member of the plot " + regionName);
	}
	
	/**
	 * Removes a member from a region
	 * @param sender
	 * @param memberName
	 * @param regionName
	 */
	public void removeMember(CommandSender sender, String regionName, String memberName){
	
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
		ProtectedRegion region = rm.getRegion(((Player) sender).getUniqueId().toString() + "__" + regionName);
		if (region == null){
			sender.sendMessage("No such region was found.");
			return;
		}
		
		//make sure the command executor is an owner of the plot
		if(!region.isOwner((LocalPlayer) sender)){
			sender.sendMessage("You are not the owner of the specified region");
			return;
		}
		
		//player that they are trying to remove isn't online.
		if (Bukkit.getPlayer(memberName) == null) {
			sender.sendMessage("That player is not currently online.");
			return;
		}
		
		//get the members of the plot
		DefaultDomain d = region.getMembers();
		
		//check if the player actually was a member
		if(!d.contains(new BukkitPlayer(wgplugin, Bukkit.getPlayer(memberName)))){
			sender.sendMessage("Player " + memberName + " is not a member of this region");
			return;
		}
		
		//remove the member from the plot
		d.removePlayer(memberName);
		region.setMembers(d);
		
		sender.sendMessage(memberName + " is no longer a member of the plot " + regionName);
	}

	public List<String> getRegions(CommandSender sender){
		List<String> regionList = new LinkedList<String>();
		
		//get the region manager for the homeworld
		RegionManager rm = LandPurchasing.wgplugin.getRegionManager(Bukkit.getWorld("HomeWorld"));
		if (rm == null){
			sender.sendMessage("No region manager for the homeworld");
			return regionList;
		}
		
		//get all regions from the region manager
		Map<String, ProtectedRegion> regions = rm.getRegions();
		
		//check every region to see if the player owns it
		//this is costly but I don't know of a better way.
		for (ProtectedRegion r: regions.values()){
			
			//if the player owns the plot
			if (r.isOwner(new BukkitPlayer(wgplugin, (Player) sender))){
				
				String plotName = r.getId();
				
				//make sure plot is a valid land plot
				if (plotName.startsWith(((Player) sender).getUniqueId().toString())){
					regionList.add(plotName.substring(((Player) sender).getUniqueId().toString().length() + 2, plotName.length()));
				}
			}
		}
		return regionList;
	}
	
	/**
	 * Lists all regions a sender owns
	 * @param sender the player executing the command
	 */
	public void listRegions(CommandSender sender){
		sender.sendMessage("Here are all the regions you own:");
		List<String> regions = this.getRegions(sender);
		for (String regionName: regions){
			sender.sendMessage(regionName);
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
		
		ProtectedRegion region = rm.getRegion(((Player) sender).getUniqueId().toString() + "__" + regionName);
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
		rm.removeRegion(((Player) sender).getUniqueId().toString() + "__" + regionName);
		
		//save WorldGuard
		try {
			rm.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//refund the player
		LandPurchasing.economy.depositPlayer((OfflinePlayer) sender, cost);
		
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
		double money = LandPurchasing.economy.getBalance((OfflinePlayer) sender);
		
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
		
		//make sure the name doesn't start iwth "__bank__", which indicates a bank
		if (name.startsWith("__bank__")) {
			sender.sendMessage("Names are not allowed to start with __bank__.");
			return;
		}
		
		
		//reassign the name to include the senders name, for uniqueness
		name = ((Player) sender).getUniqueId().toString() + "__" + name;
		
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
		
		//make sure the name doesn't start iwth "__bank__", which indicates a bank
		if (name.startsWith("__bank__")) {
			sender.sendMessage("Names are not allowed to start with __bank__.");
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
		newDomain.addPlayer(((Player) sender).getUniqueId());
		rm.getRegion(name).setOwners(newDomain);
		
		//save WorldGuard
		try {
			rm.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//remove funds from player
		LandPurchasing.economy.withdrawPlayer((OfflinePlayer) sender, cost);
		
		//let the player know
		sender.sendMessage("Congratulations, you now own this region");
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
