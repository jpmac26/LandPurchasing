package com.m0pt0pmatt.LandPurchasing;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class LandPurchasing extends JavaPlugin{

	public static FlagManager flagManager = null;
	public static LandManager landManager = null;
	public static Economy economy = null;
	
	/**
	 * WorldGuard Plugin
	 */
	public static WorldGuardPlugin wgplugin = null;
	
	/**
	 * WorldEdit Plugin
	 */
	public static WorldEditPlugin weplugin = null;
	
	public void onLoad(){
		//set up the landmanager
		landManager = new LandManager();
		
		//set up active flags management
		flagManager = new FlagManager();
	}
	
	public void onEnable(){
		weplugin = getWorldEdit();
		wgplugin = getWorldGuard();
		setupEconomy();
	}
	
	/**
	 * method for WorldGuard to get the WorldGuard Plugin
	 * @return the WorldGuard Plugin
	 */
	public static WorldGuardPlugin getWorldGuard() {
	    Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
	    
	    // WorldGuard may not be loaded
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	    	return null; // Maybe you want throw an exception instead
	    }
	    
	    return (WorldGuardPlugin) plugin;
	}
	
	/**
	 * method for WorldEdit to get the WorldEdit Plugin
	 * @return the WorldEdit Plugin
	 */
	public static WorldEditPlugin getWorldEdit() {
	    Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
	 
	    // WorldGuard may not be loaded
	    if (plugin == null || !(plugin instanceof WorldEditPlugin)) {
	        return null; // Maybe you want throw an exception instead
	    }
	 
	    return (WorldEditPlugin) plugin;
	}
	
	/**
	 * Uses Vault to hook into an economy plugin
	 * @return
	 */
	public static boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("addMember")){
			
			if (args.length != 2){
				sender.sendMessage("Wrong number of arguments.");
				return false;
			}
			else{
				landManager.addMember(sender, args[0], args[1]);
			}
			return true;
		}
		if(cmd.getName().equalsIgnoreCase("removeMember")){
			
			if (args.length != 2){
				sender.sendMessage("Wrong number of arguments.");
				return false;
			}
			else{
				landManager.removeMember(sender, args[0], args[1]);
			}
			return true;
		}
		
		if(cmd.getName().equalsIgnoreCase("addOwner")){
			
			if (args.length != 2){
				sender.sendMessage("Wrong number of arguments.");
				return false;
			}
			else{
				landManager.addOwner(sender, args[0], args[1]);
			}
			return true;
		}
		if(cmd.getName().equalsIgnoreCase("removeOwner")){
			
			if (args.length != 2){
				sender.sendMessage("Wrong number of arguments.");
				return false;
			}
			else{
				landManager.removeOwner(sender, args[0], args[1]);
			}
			return true;
		}
		
		/**
		 * player wants to change the flags on their land
		 */
		if(cmd.getName().equalsIgnoreCase("flag")){
			
			if (args.length != 3){
				sender.sendMessage("Wrong number of arguments.");
				return false;
			}
			else{
				flagManager.setFlag(sender, args);
			}
			return true;
		}
		
		/**
		 * player wants to buy a plot of land
		 */
		if(cmd.getName().equalsIgnoreCase("buyland")){
			if (args.length != 1){
				sender.sendMessage("Wrong number of arguments.");
				return false;
			}
			else{
				landManager.buyLand(sender, args[0]);
			}
			return true;
		}
		
		/**
		 * player wants to sell a plot of land
		 */
		if(cmd.getName().equalsIgnoreCase("sellland")){
			if (args.length != 1){
				sender.sendMessage("Wrong number of arguments.");
				return false;
			}
			else{
				landManager.sellLand(sender, args[0]);
			}
			return true;
		}
		
		/**
		 * player wants to see the land they own
		 */
		if(cmd.getName().equalsIgnoreCase("listland")){
			if (args.length != 0){
				sender.sendMessage("Wrong number of arguments.");
				return false;
			}
			else{
				landManager.listRegions(sender);
			}
			return true;
		}
		
		//gets the price of your selection
		if(cmd.getName().equalsIgnoreCase("price")){
			if (args.length != 0){
				sender.sendMessage("Wrong number of arguments.");
				return false;
			}
			else{
				landManager.getPrice(sender);
			}
			return true;
		}
		
		return false;
	}
	
}
