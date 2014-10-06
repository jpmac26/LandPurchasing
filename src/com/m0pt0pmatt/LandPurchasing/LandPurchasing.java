package com.m0pt0pmatt.LandPurchasing;

import java.util.UUID;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import com.m0pt0pmatt.LandPurchasing.managers.FlagManager;
import com.m0pt0pmatt.LandPurchasing.managers.LandManager;
import com.m0pt0pmatt.LandPurchasing.managers.LandService;
import com.m0pt0pmatt.LandPurchasing.managers.LandServiceProvider;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * LandPurchasing is a plugin which allows players to purchase custom plots of protected land
 * LandPurchasing uses WorldGuard as its backend.
 * 
 * @author Matthew Broomfield and Lucas Stuyvesant
 */
public class LandPurchasing extends JavaPlugin{

	/**
	 * The LandManager performs creation, deletion, and permission operations on
	 * plots of land
	 */
	public static LandManager landManager = null;
	
	/**
	 * The FlagManager performs flag changing operations on plots of land
	 */
	public static FlagManager flagManager = null;
	
	/**
	 * Land listener adds extra functionality to land plots
	 */
	public static LandListener landListener = null;
	
	/**
	 * The Vault Economy
	 */
	public static Economy economy = null;
	
	/**
	 * The WorldGuard hook
	 */
	public static WorldGuardPlugin wgplugin = null;
	
	/**
	 * The WorldEdit hook
	 */
	public static WorldEditPlugin weplugin = null;
	
	public static Plugin plugin;
	
	private static LandService landService;
		
	/**
	 * Hook into other plugins
	 */
	public void onEnable(){
		plugin = this;
		weplugin = getWorldEdit();
		wgplugin = getWorldGuard();
		setupEconomy();
		
		//set up the landmanager
		landManager = new LandManager(this, wgplugin);
		
		//set up active flags management
		flagManager = new FlagManager();
		
		//set up land listener
		landListener = new LandListener();
		Bukkit.getPluginManager().registerEvents(landListener, this);
				
		//setup land service
		landService = new LandServiceProvider(flagManager, landManager);
		Bukkit.getServicesManager().register(LandService.class, landService, this, ServicePriority.Normal);
		
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
	
	/**
	 * Handle land-based commands
	 * Current list of commands are:
	 *	/priceland
	 * 	/buyland [plot_name]
	 * 	/sellland [plot_name]
	 * 	/listland
	 * 	/flagland [plot_name] [flag_name] [flag_state]
	 *	/addmember [plot_name] [player_name]
	 *	/removemember [plot_name] [player_name]
	 * 	/addowner [plot_name] [player_name]
	 * 	/removemember [plot_name] [player_name]
	 * 	/buyatm [atm_name]
	 */
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		
		/**
		 * player wants to buy an atm
		 */
		if(cmd.getName().equalsIgnoreCase(LandCommand.BUYATM.getCommand())){
			if (args.length != 1){
				sender.sendMessage("Wrong number of arguments.");
				return false;
			}
			else{
				landManager.buyLand(sender, args[0]);
				String[] atmArgs = {args[0],"bankFlag","allow"};
				flagManager.setFlag(sender, atmArgs);
			}
			return true;
		}
		
		/**
		 * player want to know the price of the selected land
		 */
		if(cmd.getName().equalsIgnoreCase(LandCommand.PRICELAND.getCommand())){
			if (args.length != 0){
				sender.sendMessage("Wrong number of arguments.");
				return false;
			}
			else{
				landManager.getPrice(sender);
			}
			return true;
		}
		
		/**
		 * player wants to buy a plot of land
		 */
		if(cmd.getName().equalsIgnoreCase(LandCommand.BUYLAND.getCommand())){
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
		if(cmd.getName().equalsIgnoreCase(LandCommand.SELLLAND.getCommand())){
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
		if(cmd.getName().equalsIgnoreCase(LandCommand.LISTLAND.getCommand())){
			if (args.length != 0){
				sender.sendMessage("Wrong number of arguments.");
				return false;
			}
			else{
				landManager.listRegions(sender);
			}
			return true;
		}
		
		/**
		 * player wants to change the flags on their land
		 */
		if(cmd.getName().equalsIgnoreCase(LandCommand.FLAGLAND.getCommand())){
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("?")) {
					//print out available flags
					sender.sendMessage("Use any one of these flags with flagland:");
					//To avoid spam, we construct one big string
					String msg = " ";
					for (String flag : flagManager.getFlags()) {
						msg = msg + flag + "   ";
					}
					sender.sendMessage(msg);
					return true;
				}
				return false;
			}
			else if (args.length < 3){
				sender.sendMessage("Wrong number of arguments.");
				return false;
			}
			else{
				flagManager.setFlag(sender, args);
			}
			return true;
		}
		
		/**
		 * player wants to add a member to a plot of land
		 */
		if(cmd.getName().equalsIgnoreCase(LandCommand.ADDMEMBERLAND.getCommand())){
			
			if (args.length != 2){
				sender.sendMessage("Wrong number of arguments.");
				return false;
			}
			else{
				landManager.addMember(sender, args[0], args[1]);
			}
			return true;
		}
		
		/**
		 * player wants to remove a member from a plot of land
		 */
		if(cmd.getName().equalsIgnoreCase(LandCommand.REMOVEMEMBERLAND.getCommand())){
			
			if (args.length != 2){
				sender.sendMessage("Wrong number of arguments.");
				return false;
			}
			else{
				landManager.removeMember(sender, args[0], args[1]);
			}
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase(LandCommand.LANDINFO.getCommand())) {
			Player player;
			ApplicableRegionSet regions;
			ProtectedRegion region;
			if (!(sender instanceof Player)) {
				this.getLogger().info("You cannot run this command from the terminal!");
				return false;
			}
			player = (Player) sender;
			regions = wgplugin.getRegionManager(player.getWorld()).getApplicableRegions(player.getLocation().add(0, -1, 0));
			if (regions.size() == 0) {
				//not in any regions
				sender.sendMessage("You are not currently in a region!");
				return true;
			}
			//theoretically, there will only be one region. we're just going to grab the first under the first
			region = (ProtectedRegion) regions.getRegions().toArray()[0];
			
			if (region.isOwner(new BukkitPlayer(wgplugin, player))) {
				player.sendMessage("You own this region.");
				player.sendMessage("Region name: " + region.getId().substring( player.getUniqueId().toString().length() + 2     , region.getId().length()));
				player.sendMessage("Members: ");
				for (String name : region.getMembers().getPlayers()) {
					player.sendMessage(Bukkit.getOfflinePlayer(UUID.fromString(name.substring(5))).getName());
				}
				BlockVector min, max;
				min = region.getMinimumPoint();
				max = region.getMaximumPoint();
				player.sendMessage("This region extends from (" + min.getBlockX() + ", " + min.getBlockY() + ", " + min.getBlockZ() + ") to (" + max.getBlockX() + ", " + max.getBlockY() + ", " + max.getBlockZ() + ")");
			}
			else {
				//tell them who owns it
				player.sendMessage("This land is owned by " + Bukkit.getOfflinePlayer(UUID.fromString(region.getOwners().toPlayersString().substring(5))).getName());
			}
			return true;
		}
				
		return false;
	}
	
}
