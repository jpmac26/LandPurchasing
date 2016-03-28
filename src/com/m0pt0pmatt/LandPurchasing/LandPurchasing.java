package com.m0pt0pmatt.LandPurchasing;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import com.m0pt0pmatt.LandPurchasing.Handlers.SignHandler;
import com.m0pt0pmatt.LandPurchasing.Scheduling.Scheduler;
import com.m0pt0pmatt.LandPurchasing.managers.FlagManager;
import com.m0pt0pmatt.LandPurchasing.managers.LandManager;
import com.m0pt0pmatt.LandPurchasing.managers.LandService;
import com.m0pt0pmatt.LandPurchasing.managers.LandServiceProvider;
import com.m0pt0pmatt.LandPurchasing.utils.HelpBook;
import com.m0pt0pmatt.bettereconomy.BetterEconomy;
import com.m0pt0pmatt.bettereconomy.EconomyManager;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * LandPurchasing is a plugin which allows players to purchase custom plots of protected land
 * LandPurchasing uses WorldGuard as its backend.
 * 
 * @author Matthew Broomfield, Lucas Stuyvesant, and Skyler Manzanares
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
	public static EconomyManager economy = null;
	
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
	
	private static final String configFileName = "config.yml";
	
	private static YamlConfiguration config;
	
	private static final double version = 1.23;
	
	/**
	 * Load up configuration
	 */
	public void onLoad() {
		File configFile = new File(this.getDataFolder(), configFileName);
		
		//make sure our config file is valid
		if (!configFile.exists() || configFile.isDirectory()) {
			
			getLogger().warning(ChatColor.YELLOW + "Invalid Configuration file "
					+ "for LandPurchasing Plugin!" + ChatColor.RESET);
			getLogger().warning("Running with Empty Configuration");
			
			//create empty default config
			config = new YamlConfiguration();
			config.set("version", LandPurchasing.version);
			
			//creat empty plot section
			config.createSection("LeasePlots");
			
			return;
		}
		
		//found file, load up config
		config = new YamlConfiguration();
		
		
		try {
			config.load(configFile);
		} catch (Exception e) {
			getLogger().warning(e.getMessage());
			
			getLogger().warning("Running with Empty Configuration");
			//just set up a default one
			config = new YamlConfiguration();
			config.set("version", LandPurchasing.version);
			
			//create empty plot section
			config.createSection("LeasePlots");
		}
	}
		
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
		
		//load up config
		//TODO make version info useful
		getLogger().info("Ignoring version information in config...");
		ConfigurationSection plotList = config.getConfigurationSection("LeasePlots");
		if (plotList == null) {
			getLogger().warning("Error encountered when reading from config file: Null LeasePlots section!");
			return;
		}
		
		if (plotList.getKeys(false).isEmpty()) {
			//no leased plots
			getLogger().info("Found no leased plot information to load!");
			return;
		}
		
		getLogger().info("Loading leased plot information");
		int count = 0;
		for (String plotName : plotList.getKeys(false)) {
			//go through to each plot and load it up
			if (plotName.trim().isEmpty()) {
				getLogger().warning("Found funky key in LeasePlot section...");
				continue;
			}
			
			//create a LeaseLand object from the configuration section
			LeaseLand plot = LeaseLand.fromConfig(plotName, plotList.getConfigurationSection(plotName));
			
			if (plot == null) {
				getLogger().warning("Unable to properly load plot: " + plotName);
				continue;
			}
			
			//check and make sure this region doesn't intersect any others
			//TODO add check here? What about spawn region?
			
			landManager.addLeasePlot(plot);
			count ++;
		}
		getLogger().info("Loaded " + count + " plots!");
		
		getLogger().info("Starting scheduler");
		Scheduler.getScheduler();
		SignHandler.getHandler();
	}
	
	@Override
	public void onDisable() {
		
		//remove all leased plot locations, so we can recreate them on next enable and not
		//create overlapping regions!
		
		//RegionManager rm = wgplugin.getRegionManager(Bukkit.getWorld("Homeworld"));
		
		//save out config!
		getLogger().info("Saving plot information...");
		YamlConfiguration newConfig = new YamlConfiguration();
		newConfig.set("version", config.get("version"));
		
		newConfig.createSection("LeasePlots");
		for (LeaseLand land : landManager.getLeasePlots()) {
			newConfig.createSection("LeasePlots." + land.land.getId(), land.toConfig().getValues(true));
		}
		
		try {
			newConfig.save(new File(getDataFolder(), configFileName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		getLogger().info("Removing leased plots...");
//		for (LeaseLand land : landManager.getLeasePlots()) {
//			rm.removeRegion(land.land.getId());
//		}
	}
	
	/**
	 * method for WorldGuard to get the WorldGuard Plugin
	 * @return the WorldGuard Plugin
	 */
	public static WorldGuardPlugin getWorldGuard() {
	    Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
	    
	    // WorldGuard may not be loaded
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	    	Bukkit.getLogger().warning("LandPurchasing failed to hook into WorldGuard plugin!");
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
	    	Bukkit.getLogger().warning("LandPurchasing failed to hook into WorldEdit plugin!");
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
		if (Bukkit.getPluginManager().isPluginEnabled("BetterEconomy")) {
			economy = BetterEconomy.economy;
			return true;
		}
		else
			return false;
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
		 * Admin is setting up a leased plot
		 */
		if (cmd.getName().equalsIgnoreCase(LandCommand.LEASE.getCommand())) {
			if (args.length != 1) {
				sender.sendMessage("Invalid number of arguments.");
				return false;
			}
			landManager.lease(sender, args[0]);
			return true;
		}
		
		/**
		 * Player attempting to renew a lease
		 */
		if (cmd.getName().equalsIgnoreCase(LandCommand.RENEWLEASE.getCommand())) {
			if (args.length != 1) {
				sender.sendMessage("Invalid number of arguments.");
				return false;
			}
			landManager.renewLease(sender, args[0]);
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase(LandCommand.LEASELAND.getCommand())) {
			if (args.length != 1) {
				sender.sendMessage("Invalid number of arguments.");
				return false;
			}
			landManager.leaseLand(sender, args[0]);
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
		 * Player would like the local listings
		 */
		if (cmd.getName().equalsIgnoreCase(LandCommand.LISTINGS.getCommand())) {
			if (args.length != 0){
				sender.sendMessage("Wrong number of arguments.");
				return false;
			}
			if (sender instanceof Player) {
				
				if (((Player) sender).getInventory().firstEmpty() == -1) {
					sender.sendMessage("There is no room in your inventory!");
					return true;
				}
				
				ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
				BookMeta meta = (BookMeta) book.getItemMeta();
				
				meta.setDisplayName("Local Listings");
				meta.setTitle("Lease Listings");
				meta.setAuthor("");
				
				meta.addPage("Listed herein are all available plots to lease.\n" +  
						"Each plot lists its address and the price.\n\n" + 
						"All leases are offered in\n" +  
						ChatColor.DARK_RED + "2 week" + ChatColor.BLACK + "\nperiods.\n\n"
								+ (landManager.getAvailableLeasePlots().isEmpty() ? 
								ChatColor.DARK_RED + "There are no available plots to lease!" + ChatColor.BLACK : 
								"There are " + ChatColor.DARK_GREEN + 
										landManager.getAvailableLeasePlots().size() + 
								ChatColor.BLACK + " plots available to lease!"));
				
				if (!landManager.getAvailableLeasePlots().isEmpty()) {
					for (LeaseLand plot : landManager.getAvailableLeasePlots()) {
						Location b = plot.getSignLocation();
						meta.addPage(ChatColor.DARK_BLUE + plot.getID() + ChatColor.BLACK + "\n" +
								ChatColor.DARK_GREEN + "$" + plot.getCost() + ChatColor.BLACK + "\n" +
								"-----\n\n" + 
								"Located around\n(" + 
								b.getBlockX() + ", " + b.getBlockY() + ", " + b.getBlockZ() + 
								")"
								);
					}
				}
				
				book.setItemMeta(meta);
				
				Player player = (Player) sender;
				
				if (player.getInventory().contains(Material.WRITTEN_BOOK)) {
					player.getInventory().setItem(player.getInventory().first(Material.WRITTEN_BOOK), book);
					player.sendMessage("Your listings book has been updated!");
				} else {				
					player.getInventory().addItem(book);
					player.sendMessage("A book containing the listings has been added to your inventory.");
				}
				
			} else {
				sender.sendMessage("Only players can use this command!");
			}
			return true;
		}
		
		/**
		 * player wants to see what plots are available to lease
		 */
		if(cmd.getName().equalsIgnoreCase(LandCommand.LISTLEASE.getCommand())){
			if (args.length != 0){
				sender.sendMessage("Wrong number of arguments.");
				return false;
			}
			else{
				landManager.listLeaseProperties(sender);
			}
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase(LandCommand.LANDHELP.getCommand())) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("You must be a player to use this command.");
				return true;
			}
			
			HelpBook.givePlayerBook((Player) sender);
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
					boolean trig = false;
					for (String flag : flagManager.getFlags()) {
						msg = msg + (trig ? ChatColor.DARK_PURPLE : ChatColor.GOLD) + flag + "   ";
						trig = !trig;
					}
					msg += ChatColor.RESET;
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
			
			//do quick check that it isn't spawn: this should be the only time that there
			//are overlapping regions: leased land in spawn
			if (region.getId().equalsIgnoreCase("spawn")) {
				if (regions.getRegions().size() > 1 ) {
					region = (ProtectedRegion) regions.getRegions().toArray()[1];
				} 
				else {
					//the player is in spawn only
					player.sendMessage("You are currently in the spawn, est. 2015");
					return true;
				}
			}
				
			
			//two modes: it's a leased property (no UUID header) or it's a player's property
			//if it's a leased property, display different stats and don't substring the name
			if (LandPurchasing.landManager.getPlot(region.getId()) != null) {
				//it's a leased plot!
				LeaseLand plot = landManager.getPlot(region.getId());
				
				player.sendMessage("Plot name: " + region.getId());
								
				if (region.isOwner(new BukkitPlayer(wgplugin, player))) {
					player.sendMessage("You are currently leasing this property.");
					player.sendMessage("Your lease expires on " + plot.getDueDate());
					player.sendMessage("    To renew your lease, use the /renewlease command");
					
					player.sendMessage("Members: ");
					for (String name : region.getMembers().getPlayers()) {
						player.sendMessage(Bukkit.getOfflinePlayer(UUID.fromString(name.substring(5))).getName());
					}
					BlockVector min, max;
					min = region.getMinimumPoint();
					max = region.getMaximumPoint();
					player.sendMessage("This region extends from (" + min.getBlockX() + ", " + min.getBlockY() + ", " + min.getBlockZ() + ") to (" + max.getBlockX() + ", " + max.getBlockY() + ", " + max.getBlockZ() + ")");
				} else {
					if (plot.getDueDate() == null) {
						player.sendMessage("This plot is available to lease!");
						return true;
					}
					
					//someone owns it
					//tell them who it is and when their lease expires?
					player.sendMessage("This land is being leased by by " + Bukkit.getOfflinePlayer(UUID.fromString(region.getOwners().toPlayersString().substring(5))).getName());
					player.sendMessage("Their lease expires on " + plot.getDueDate());
					
				}
				
				return true;
			}
			
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
