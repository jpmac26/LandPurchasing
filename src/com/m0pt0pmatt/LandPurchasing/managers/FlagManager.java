/**
 * 
 */
package com.m0pt0pmatt.LandPurchasing.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.m0pt0pmatt.LandPurchasing.LandPurchasing;
import com.m0pt0pmatt.LandPurchasing.flags.CustomFlag;
import com.m0pt0pmatt.LandPurchasing.flags.LandFlag;
//import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * Contains functions for changing the 
 * worldguard flags of pre-existing regions
 * @author Lucas Stuyvesant
 */
public class FlagManager {
	
	private Map<String, LandFlag> flags = new HashMap<String, LandFlag>();	
	private final String PVP_WARNING = "WARNING: You are entering a PVP-enabled region\n";
	private final String PVP_LEAVE = "WARNING: You are leaving a PVP-enabled region\n";
	
	/**
	 * initializes flags
	 */
	public FlagManager(){
		
		//default stateFlags
		flags.put(DefaultFlag.MOB_DAMAGE.getName(), new LandFlag(DefaultFlag.MOB_DAMAGE, false, 0));
		flags.put(DefaultFlag.MOB_SPAWNING.getName(), new LandFlag(DefaultFlag.MOB_SPAWNING, false, 0));
		flags.put(DefaultFlag.CREEPER_EXPLOSION.getName(), new LandFlag(DefaultFlag.CREEPER_EXPLOSION, false, 0));
		flags.put(DefaultFlag.ENDER_BUILD.getName(), new LandFlag(DefaultFlag.ENDER_BUILD, false, 0));
		flags.put(DefaultFlag.TNT.getName(), new LandFlag(DefaultFlag.TNT, false, 0));
		flags.put(DefaultFlag.FIRE_SPREAD.getName(), new LandFlag(DefaultFlag.FIRE_SPREAD, false, 0));
		flags.put(DefaultFlag.GHAST_FIREBALL.getName(), new LandFlag(DefaultFlag.GHAST_FIREBALL, false, 0));
		flags.put(DefaultFlag.CHEST_ACCESS.getName(), new LandFlag(DefaultFlag.CHEST_ACCESS, false, 0));
		flags.put(DefaultFlag.DESTROY_VEHICLE.getName(), new LandFlag(DefaultFlag.DESTROY_VEHICLE, false, 0));
		flags.put(DefaultFlag.PLACE_VEHICLE.getName(), new LandFlag(DefaultFlag.PLACE_VEHICLE, false, 0));
		
		//other stateFlags
		flags.put(DefaultFlag.PVP.getName(), new LandFlag(DefaultFlag.PVP, true, 0.1));
		flags.put(DefaultFlag.ENDERPEARL.getName(), new LandFlag(DefaultFlag.ENDERPEARL, false, 0));
		flags.put(DefaultFlag.PISTONS.getName(), new LandFlag(DefaultFlag.PISTONS, false, 0));
		flags.put(DefaultFlag.USE.getName(), new LandFlag(DefaultFlag.USE, false, 0));

		//possibly misunderstood flags, message won't be sent to owner alone
		//TODO:make these work how we want. Going to require custom flags
		//flags.put(DefaultFlag.NOTIFY_ENTER.getName(), new LandFlag(DefaultFlag.NOTIFY_ENTER, false, 0));
		//flags.put(DefaultFlag.NOTIFY_LEAVE.getName(), new LandFlag(DefaultFlag.NOTIFY_LEAVE, false, 0));
		
		//string flags
		flags.put(DefaultFlag.GREET_MESSAGE.getName(), new LandFlag(DefaultFlag.GREET_MESSAGE, false, 1000));
		flags.put(DefaultFlag.FAREWELL_MESSAGE.getName(), new LandFlag(DefaultFlag.FAREWELL_MESSAGE, false, 1000));
		
		//TODO: list flags
		//allowed-cmds and blocked-cmds
		//to be used for atm's and a bank
		
		//custom flags
		flags.put(CustomFlag.OUTSIDEPISTONS.getFlag().getFlag().getName(),CustomFlag.OUTSIDEPISTONS.getFlag());
		flags.put(CustomFlag.BANKFLAG.getFlag().getFlag().getName(),CustomFlag.BANKFLAG.getFlag());
	}
	
	public void setDefaultFlags(ProtectedRegion region){
		region.setFlag(DefaultFlag.MOB_DAMAGE, StateFlag.State.DENY);
		region.setFlag(DefaultFlag.MOB_SPAWNING, StateFlag.State.DENY);
		region.setFlag(DefaultFlag.CREEPER_EXPLOSION, StateFlag.State.DENY);
		region.setFlag(DefaultFlag.ENDER_BUILD, StateFlag.State.DENY);
		region.setFlag(DefaultFlag.TNT, StateFlag.State.DENY);
		region.setFlag(DefaultFlag.FIRE_SPREAD, StateFlag.State.DENY);
		region.setFlag(DefaultFlag.GHAST_FIREBALL, StateFlag.State.DENY);
		region.setFlag(DefaultFlag.CHEST_ACCESS, StateFlag.State.ALLOW);
		region.setFlag((StateFlag)CustomFlag.OUTSIDEPISTONS.getFlag().getFlag(), StateFlag.State.DENY);	
		region.setFlag((StateFlag)CustomFlag.BANKFLAG.getFlag().getFlag(), StateFlag.State.DENY);	
	}
	
	public Set<String> getFlags() {
		return this.flags.keySet();
	}

	/**
	 * 
	 * @param sender
	 * @param args arg[0] = plot name, arg[1] = flag name, arg[2] = value
	 * @return
	 */
	public boolean setFlag(CommandSender sender, String[] args){
		
		String plotName = args[0];
		String flagName = args[1];
		
		String value = "";
		for (int i = 2; i < args.length; i++){
			value = value + " " + args[i];
		}
		value = value.substring(1, value.length());
		
		//get the region manager for the homeworld
		RegionManager rm = LandPurchasing.wgplugin.getRegionManager(Bukkit.getWorld("HomeWorld"));
		if (rm == null){
			sender.sendMessage("No region manager for the homeworld");
			return false;
		}
		
		ProtectedRegion region = rm.getRegion(((Player) sender).getUniqueId() + "__" + plotName);
		if (region == null){
			sender.sendMessage("No such region was found.");
			return false;
		}
		
		LandFlag landFlag = flags.get(flagName);
		if (landFlag == null){
			sender.sendMessage("Unknown flag name. For a list of flags, instead use /flagland ?");
			return false;
		}
		
		Flag<?> flag = landFlag.getFlag(); 
		if (flag == null){
			sender.sendMessage("Unknown flag name");
			return false;
		}
		
		//determine cost of setting the flag
		double cost = 0;
		if (landFlag.costScales()){
			double height = region.getMaximumPoint().getY() - region.getMinimumPoint().getY() + 1;
			double length = region.getMaximumPoint().getX() - region.getMinimumPoint().getX() + 1;
			double width = region.getMaximumPoint().getZ() - region.getMinimumPoint().getZ() + 1;
			cost = landFlag.getCost() * LandManager.getCost(height, length, width);
		}
		else{
			cost = landFlag.getCost();
		}
		
		//check if the player can afford changing this flag
		if (!LandPurchasing.economy.has((OfflinePlayer) sender, (int)cost)){
			sender.sendMessage("You do not have the required funds. Changing this flag costs $" + (int)cost);
			return false;
		}
		
		//withdraw the funds from the player to the server
		LandPurchasing.economy.withdrawPlayer((OfflinePlayer) sender, (int)cost);
		//LandPurchasing.economy.depositPlayer("__Server", (int)cost);
		//removed cause yolo   -sm
		
		//special cases come first, before the flag is set
		
		//if pvp is being changed, update greeting and farewell messages
		if (flag.getName().equalsIgnoreCase(DefaultFlag.PVP.getName())){
			
			if (value.equalsIgnoreCase("allow")){
				//required warning upon entry/exit of pvp region
				String s = region.getFlag(DefaultFlag.GREET_MESSAGE);
				if(s == null){
					assignFlag(region,DefaultFlag.GREET_MESSAGE,PVP_WARNING);
				}
				else{
					if(!s.startsWith(PVP_WARNING)){
						return true;
					}
					assignFlag(region,DefaultFlag.GREET_MESSAGE,PVP_WARNING + s);
				}
				
				s = region.getFlag(DefaultFlag.FAREWELL_MESSAGE);
				if(s == null){
					assignFlag(region,DefaultFlag.FAREWELL_MESSAGE,PVP_LEAVE);
				}
				else{
					if(!s.startsWith(PVP_LEAVE)){
						return true;
					}
					assignFlag(region,DefaultFlag.FAREWELL_MESSAGE,PVP_LEAVE + s);
				}
			}
			else if (value.equalsIgnoreCase("deny")){
			
				String s = region.getFlag(DefaultFlag.GREET_MESSAGE);
				if(s != null){
					if(s.equals(PVP_WARNING)){
						assignFlag(region,DefaultFlag.GREET_MESSAGE,"");
					}
					else{
						assignFlag(region,DefaultFlag.GREET_MESSAGE,s.substring(PVP_WARNING.length()));
					}
				}
				
				s = region.getFlag(DefaultFlag.FAREWELL_MESSAGE);
				if(s != null){
					if(s.equals(PVP_WARNING)){
						assignFlag(region,DefaultFlag.FAREWELL_MESSAGE,"");
					}
					else{
						assignFlag(region,DefaultFlag.FAREWELL_MESSAGE,s.substring(PVP_LEAVE.length()));
					}
				}
			}				
		}
		
		//check if the flag is a state flag
		if (flag instanceof StateFlag){
			
			if(value.equalsIgnoreCase("deny")){
				assignFlag(region,(StateFlag)flag,StateFlag.State.DENY);
			}
			else if (value.equalsIgnoreCase("allow")){
				assignFlag(region,(StateFlag)flag,StateFlag.State.ALLOW);
			}
			else{
				sender.sendMessage(value + "is not a valid state for the flag " + flag.getName());
				sender.sendMessage("Expected allow or deny");
				return false;
			}
		}
		
		//check i the flag is a boolean flag
		else if (flag instanceof BooleanFlag){
						
			if(value.equalsIgnoreCase("true")){
				assignFlag(region,(BooleanFlag)flag,true);
			}
			else if (value.equalsIgnoreCase("false")){
				assignFlag(region,(BooleanFlag)flag,false);
			}
			else{
				sender.sendMessage(value + "is not a valid value for the flag " + flag.getName());
				sender.sendMessage("Expected true or false");
				return false;
			}
		}
		
		//check if the flag is a string flag
		else if (flag instanceof StringFlag){
			
			//check if flag is a WARNING flag
			if(flagName.startsWith("WARNING:")){
				sender.sendMessage("Messages starting with 'Warning:' are reserved for server use");
				return false;
			}
			
			assignFlag(region,(StringFlag)flag,value);
		}
		
		//unknown flag
		else{
			sender.sendMessage("Unknown flag type");
			return false;
		}
		
		sender.sendMessage("You have successfully set flag " + flagName + " to " + value + " for the plot " + plotName + " for a cost of $" + cost);
			try {
				rm.save();
			} catch (StorageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;	
	}
	
	/**
	 * Receives instructions and executes the corresponding command
	 */
	private void assignFlag(ProtectedRegion region, StateFlag flag, State value){
		region.setFlag(flag, value);
	}
	
	/**
	 * Receives instructions and executes the corresponding command
	 */
	private void assignFlag(ProtectedRegion region, BooleanFlag flag, boolean value){
		region.setFlag(flag, value);
	}
	
	/**
	 * Receives instructions and executes the corresponding command
	 */
	private  void assignFlag(ProtectedRegion region, StringFlag flag, String value){
		region.setFlag(flag, value);
	}

}
