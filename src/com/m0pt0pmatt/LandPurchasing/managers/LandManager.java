package com.m0pt0pmatt.LandPurchasing.managers;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.m0pt0pmatt.LandPurchasing.LandPurchasing;
import com.m0pt0pmatt.LandPurchasing.LeaseLand;
import com.m0pt0pmatt.LandPurchasing.Effects.AreaView;
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
	
	private Set<LeaseLand> leasePlots;
	
	
	public LandManager(LandPurchasing plugin, WorldGuardPlugin wgplugin) {
		this.plugin = plugin;
		this.wgplugin = wgplugin;
		
		leasePlots = new HashSet<LeaseLand>();
	}
	
	public Set<LeaseLand> getLeasePlots() {
		return leasePlots;
	}
	
	/**
	 * Fetches a set of all lease plots that are available (no tenant)
	 * @return A set containing all lease plots, or an empty set if there are none
	 */
	public Set<LeaseLand> getAvailableLeasePlots() {
		Set<LeaseLand> avail = new HashSet<LeaseLand>();
		
		for (LeaseLand plot : leasePlots) {
			if (plot.getDueDate() == null) {
				avail.add(plot);
			}
		}
		
		return avail;
	}
	
	public void addLeasePlot(LeaseLand plot) {
		leasePlots.add(plot);
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
		//no way currently around deprecated call, as it's not the player to be added calling the method
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
		if(!region.isOwner(new BukkitPlayer(wgplugin, (Player) sender))){
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
				} else {
					regionList.add("Lease: " + plotName);
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
	 * Tells the command sender all the properties that are available to be leased
	 * @param sender
	 */
	public void listLeaseProperties(CommandSender sender) {
		
		Set<LeaseLand> plots = getAvailableLeasePlots();
		
		if (plots.isEmpty()) {
			sender.sendMessage("There are currently no available properties to lease.");
			return;
		}
		
		sender.sendMessage("The following properties are available to lease:");
		for (LeaseLand plot : getAvailableLeasePlots()) {
			sender.sendMessage(plot.getID());
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
		
		//get the region manager for the homeworld
				RegionManager rm = LandPurchasing.wgplugin.getRegionManager(Bukkit.getWorld("HomeWorld"));
				if (rm == null){
					sender.sendMessage("No region manager for the homeworld");
					return;
				}
		
		//make sure it's not null or empty
		if (selection == null || selection.getArea() == 0) {
			sender.sendMessage("You must select a region first!");
			return;
		}
		BlockVector b1 = new BlockVector(selection.getMinimumPoint().getX(), selection.getMinimumPoint().getY(), selection.getMinimumPoint().getZ());
		BlockVector b2 = new BlockVector(selection.getMaximumPoint().getX(), selection.getMaximumPoint().getY(), selection.getMaximumPoint().getZ());
		
		//calculate cost of selection
		double height = selection.getMaximumPoint().getY() - selection.getMinimumPoint().getY() + 1;
		double length = selection.getMaximumPoint().getX() - selection.getMinimumPoint().getX() + 1;
		double width = selection.getMaximumPoint().getZ() - selection.getMinimumPoint().getZ() + 1;
		double cost = getCost(height, length, width);
		
		//notify the player of the cost
		sender.sendMessage("cost: " + cost);
		
		//check if player has enough funds
		if (money < cost){
				sender.sendMessage("Not enough funds. Your selection costs " + cost + " but you only have " + money + ".");
				return;
			}
				
		
		//check for invalid name
		if (!validName(name)) {
			sender.sendMessage("Invalid land name!");
			return;
		}
		
		//reassign the name to include the senders name, for uniqueness
		name = ((Player) sender).getUniqueId().toString() + "__" + name;
				

		//make sure name isn't already used
		if (rm.getRegion(name) != null){
			sender.sendMessage("I'm sorry, but that name is already in use.");
			return;
		}
		
		//make sure selection doesn't include other regions
		if (!selectionEmpty(rm, b1, b2)) {
			//contains a purchased region
			sender.sendMessage("I'm sorry, but your selection includes another region");
			return;
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
	 * Sets up the given property to be able to be leased out
	 * @param sender
	 * @param name
	 */
	public void lease(CommandSender sender, String name) {
		
		//perform basic service checks
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
		
		//grab the region manager and selection
		RegionManager rm = LandPurchasing.wgplugin.getRegionManager(Bukkit.getWorld("Homeworld"));
		Selection selection = LandPurchasing.weplugin.getSelection((Player) sender);
		if (selection == null || selection.getArea() == 0) {
			sender.sendMessage("You must make an area selection first!");
			return;
		}
		BlockVector b1 = new BlockVector(selection.getMinimumPoint().getX(), selection.getMinimumPoint().getY(), selection.getMinimumPoint().getZ());
		BlockVector b2 = new BlockVector(selection.getMaximumPoint().getX(), selection.getMaximumPoint().getY(), selection.getMaximumPoint().getZ());
		
		
		//check the name
		if (!validName(name)) {
			sender.sendMessage("Invalid plot name");
			return;
		}
		
		//also make sure that property name doesn't exist
		if (rm.getRegion(name) != null) {
			sender.sendMessage("That lease plot ID is already in use!");
			return;
		}
		
		
		
		//don't need to check balance, cause it's being set up to lease
		//need to make sure there doesn't exist a region there though!
		if (!selectionEmpty(rm, b1, b2)) {
			sender.sendMessage("This region contains another region!");
			return;
		}
		
		//TODO confirmation period
		
		//passed all checks, not create and register region
		ProtectedCuboidRegion region = new ProtectedCuboidRegion(name, b1, b2);
		
		//set proper flags
		LandPurchasing.flagManager.setDefaultFlags(region);
		
		//add the new region to WorldGuard
		rm.addRegion(region);
		
				
		//save WorldGuard
		try {
			rm.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//registere plot with our own database
		leasePlots.add(new LeaseLand(((Player) sender).getLocation(), region));
		
		sender.sendMessage("Leased plot has been successfully registered with sign location"
				+ " where you're standing!");
		
		new AreaView(region);
		
	}
	
	/**
	 * Player is attempting to lease a plot of land
	 * @param sender
	 * @param name
	 */
	public void leaseLand(CommandSender sender, String name) {
		
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
		
		//get the region manager for the homeworld
		RegionManager rm = LandPurchasing.wgplugin.getRegionManager(Bukkit.getWorld("HomeWorld"));
		if (rm == null){
			sender.sendMessage("No region manager for the homeworld");
			return;
		}
		
		ProtectedRegion region = rm.getRegion(name);
		if (region == null) {
			sender.sendMessage("Invalid plot name!");
			return;
		}
		
		LeaseLand plot = getPlot(name);
		if (plot == null) {
			sender.sendMessage("Unable to locate lease plot with that name!");
			return;
		}
		
		if (plot.getDueDate() != null) {
			sender.sendMessage("That plot is already being leased out, and is not available!");
			return;
		}
		
		int cost = (plot.getCost());
		if (money < cost) {
			sender.sendMessage("You do not have sufficient funds to lease this property!");
			return;
		}
		
		//passed checks, lease it out!
		LandPurchasing.economy.withdrawPlayer((OfflinePlayer) sender, cost);
		DefaultDomain domain = new DefaultDomain();
		domain.addPlayer( ((Player) sender).getUniqueId()   );
		region.setOwners(domain);
		
		//set new due date to this date + 14 days D:
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, 14);
		plot.setDueDate(cal.getTime());

		//congratulate new lease owner
		sender.sendMessage("Congratulations! You are now leasing this property!");
		sender.sendMessage("Your lease expires on " + cal.getTime());
		
	}
	
	/**
	 * Renews the provided plot
	 * @param sender
	 * @param plot
	 */
	public void renewLease(CommandSender sender, String name) {
		
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
		double cost;
		
		//get the region manager for the homeworld
		RegionManager rm = LandPurchasing.wgplugin.getRegionManager(Bukkit.getWorld("HomeWorld"));
		if (rm == null){
			sender.sendMessage("No region manager for the homeworld");
			return;
		}
		
		ProtectedRegion region = rm.getRegion(name);
		if (region == null) {
			sender.sendMessage("Invalid plot name!");
			return;
		}
		
		LeaseLand plot = getPlot(name);
		cost = plot.getCost();
		
		if (plot.getDueDate() == null || !plot.getRegion().getOwners().contains(new BukkitPlayer(LandPurchasing.wgplugin, (Player) sender))) {
			sender.sendMessage("You are not leasing that plot!");
			return;
		}
		
		if (money < cost) {
			sender.sendMessage("You do not have enough money to renew your lease, which costs " + cost);
			return;
		}
		
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, 14);
		if (cal.getTime().compareTo(plot.getDueDate()) < 0) {
			//this is: "is 14 days from now the same or LATER than the due date?
			//e.g. is the due date already more than 14 days away (can't renew)
			sender.sendMessage("You cannot renew your lease, as your current due-date is still"
					+ " over 2 weeks away!");
			return;
		}
		
		cal.clear();
		cal.setTime(plot.getDueDate());
		cal.add(Calendar.DATE, 14);
		plot.setDueDate(cal.getTime());
		
		LandPurchasing.economy.withdrawPlayer((Player) sender, cost);
		
		sender.sendMessage("You have renewed your lease for $" + cost);
		sender.sendMessage("Your new due-date is " + cal.getTime());
		
	}
	
	/**
	 * Removes the held lease on the provided plot
	 * @param plot
	 */
	public void releaseLease(LeaseLand plot) {
		
		plot.setDueDate(null);
		plot.getRegion().setOwners(new DefaultDomain());
		
		//TODO fancy sign stuff
		
	}
	
	
	
	
	
	
	//TODO scheduled lease checks!!!!!
	//     remove owner, due date null!
	//TODO list leased land
	//TODO pay lease command?
	//TODO lease book!
	//TODO lease sign!
	
	
	
	
	
	
	
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
	
	
	/**
	 * Checks whether or not the selected region between the two passed vectors contain
	 * a region that's already purchased
	 * @param rm The region manager to check with
	 * @param b1 The first block vector to check between. This should be the 'smallest'
	 * @param b2 The second block vector. 
	 * @return True - Selection did NOT contain a purchased region.<br />
	 * False - Selection is not free of any purchased regions
	 */
	private boolean selectionEmpty(RegionManager rm, BlockVector b1, BlockVector b2) {
		//go through every block and check if it's purchased
		for (int x = b1.getBlockX(); x < b2.getBlockX() + 1; x++){
			for (int y = b1.getBlockY(); y < b2.getBlockY() + 1; y++){
				for (int z = b1.getBlockZ(); z < b2.getBlockZ() + 1; z++){
					if(!(rm.getApplicableRegionsIDs(new Vector(x,y,z)).isEmpty())){
						//this block is purchased!
						return false;
					}
				}
			}
		}
		
		//none of the blocks were already purchased!
		return true;
	}
	
	/**
	 * Checks if the passed name is valid, according to the predefined name rules 
	 * @param name Name to check
	 * @return true if the name is valid, and false if it violates a rule
	 */
	private boolean validName(String name) {
		
		if (name.isEmpty() || name.equals(null)){
			return false;
		}
		
		//no special characaters
		if (!name.matches("[a-zA-Z0-9]+")) {
			return false;
		}
		
		//make sure the name doesn't start iwth "__bank__", which indicates a bank
		if (name.startsWith("__bank__")) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Iterates over lease plot set and returns the LeaseLand object that matches the 
	 * passed ID, or null if none do
	 * @param id
	 * @return
	 */
	public LeaseLand getPlot(String id) {
		for (LeaseLand plot : leasePlots) {
			if (plot.getID().equals(id)) {
				return plot;
			}
		}
		
		return null;
	}

}
