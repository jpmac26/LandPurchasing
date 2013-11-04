/**
 * 
 */
package com.m0pt0pmatt.LandPurchasing;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.EntityTypeFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * Contains functions for changing the 
 * worldguard flags of pre-existing regions
 * @author Lucas Stuyvesant
 */
public class FlagManager {
	
	private List<StateFlag> stateFlags = new ArrayList<StateFlag>();	
	private List<BooleanFlag> booleanFlags = new ArrayList<BooleanFlag>();
	private List<StringFlag> stringFlags = new ArrayList<StringFlag>();	
	private final String PVP_WARNING = "WARNING: You are entering a PVP-enabled region\n";
	private final String PVP_LEAVE = "WARNING: You are leaving a PVP-enabled region\n";
	
	public FlagManager(){
		//default stateFlags
		stateFlags.add(DefaultFlag.MOB_DAMAGE);
		stateFlags.add(DefaultFlag.MOB_SPAWNING);
		stateFlags.add(DefaultFlag.CREEPER_EXPLOSION);
		stateFlags.add(DefaultFlag.ENDER_BUILD);
		stateFlags.add(DefaultFlag.TNT);
		stateFlags.add(DefaultFlag.FIRE_SPREAD);
		stateFlags.add(DefaultFlag.GHAST_FIREBALL);
		stateFlags.add(DefaultFlag.CHEST_ACCESS);
		//other stateFlags
		stateFlags.add(DefaultFlag.PVP);
		stateFlags.add(DefaultFlag.ENDERPEARL);
		stateFlags.add(DefaultFlag.PISTONS);
		stateFlags.add(DefaultFlag.USE);

		//possibly misunderstood flags, message won't be sent to owner alone
		booleanFlags.add(DefaultFlag.NOTIFY_ENTER);
		booleanFlags.add(DefaultFlag.NOTIFY_LEAVE);
		
		//string flags
		stringFlags.add(DefaultFlag.GREET_MESSAGE);
		stringFlags.add(DefaultFlag.FAREWELL_MESSAGE);
	}
	
	public void setDefaultFlags(ProtectedCuboidRegion region){
		region.setFlag(DefaultFlag.MOB_DAMAGE, StateFlag.State.DENY);
		region.setFlag(DefaultFlag.MOB_SPAWNING, StateFlag.State.DENY);
		region.setFlag(DefaultFlag.CREEPER_EXPLOSION, StateFlag.State.DENY);
		region.setFlag(DefaultFlag.ENDER_BUILD, StateFlag.State.DENY);
		region.setFlag(DefaultFlag.TNT, StateFlag.State.DENY);
		region.setFlag(DefaultFlag.FIRE_SPREAD, StateFlag.State.DENY);
		region.setFlag(DefaultFlag.GHAST_FIREBALL, StateFlag.State.DENY);
		region.setFlag(DefaultFlag.CHEST_ACCESS, StateFlag.State.ALLOW);	
	}
	
	/**
	 * 
	 * @param sender
	 * @param args arg[0] = flag name, arg[1] = value, arg[2] = region
	 * @return
	 */
	public boolean setFlag(CommandSender sender, String[] args){

		int flagId = 0;	//1 for StateFlag, 2 for BooleanFlag, 3 for StringFlag, 0 for error
		int flagIndex = 0;
		StateFlag stateFlag = null;
		BooleanFlag booleanFlag = null;
		StringFlag stringFlag = null;
		
		//check if flag name is valid
		for(int i=0; i < stateFlags.size(); i++){
			if(stateFlags.get(i).getName().equals(args[0])){
				stateFlag = stateFlags.get(i);
				flagId = 1;
				flagIndex = i;
				break;
			}
		}
				
		if(flagId == 0){
			//check if flag name is valid as BooleanFlag
			for(int i=0; i < booleanFlags.size(); i++){
				if(booleanFlags.get(i).getName().equals(args[0])){
					booleanFlag = booleanFlags.get(i);
					flagId = 2;
					flagIndex = i;
					break;
				}
			}
		}
		
		if(flagId == 0){
			//check if flag name is valid as StringFlag
			for(int i=0; i < stringFlags.size(); i++){
				if(stringFlags.get(i).getName().equals(args[0])){
					stringFlag = stringFlags.get(i);
					flagId = 3;
					flagIndex = i;
					break;
				}
			}
		}
		
		RegionManager rm = LandPurchasing.wgplugin.getRegionManager(Bukkit.getWorld("HomeWorld"));
		ProtectedRegion region = rm.getRegion(sender.getName() + "__" + args[2]);
		if (region == null){
			sender.sendMessage("No such region was found.");
			return false;
		}
		
		switch(flagId){
		case 1:	//stateflags
			if(args[1].equals("deny")){
				switch(flagIndex){
				case 0:
					assignFlag(region,DefaultFlag.MOB_DAMAGE,StateFlag.State.DENY);
				case 1:
					assignFlag(region,DefaultFlag.MOB_SPAWNING,StateFlag.State.DENY);		
				case 2:
					assignFlag(region,DefaultFlag.CREEPER_EXPLOSION,StateFlag.State.DENY);
				case 3:
					assignFlag(region,DefaultFlag.ENDER_BUILD,StateFlag.State.DENY);
				case 4:
					assignFlag(region,DefaultFlag.TNT,StateFlag.State.DENY);
				case 5:
					assignFlag(region,DefaultFlag.FIRE_SPREAD,StateFlag.State.DENY);
				case 6:
					assignFlag(region,DefaultFlag.GHAST_FIREBALL,StateFlag.State.DENY);
				case 7:
					assignFlag(region,DefaultFlag.CHEST_ACCESS,StateFlag.State.DENY);
				case 8:
					assignFlag(region,DefaultFlag.PVP,StateFlag.State.DENY);
					
					//required warning upon entry to pvp region
					String s = region.getFlag(DefaultFlag.GREET_MESSAGE);
					if(s != null){
						if(s.equals(PVP_WARNING)){
							assignFlag(region,DefaultFlag.GREET_MESSAGE,"");
						}
						else if(s.startsWith(PVP_WARNING)){
							s.replaceFirst(PVP_WARNING, "");
							assignFlag(region,DefaultFlag.GREET_MESSAGE,s);
						}
					}
					s = region.getFlag(DefaultFlag.FAREWELL_MESSAGE);
					if(s != null){
						if(s.equals(PVP_LEAVE)){
							assignFlag(region,DefaultFlag.FAREWELL_MESSAGE,"");
						}
						else if(s.startsWith(PVP_LEAVE)){
							s.replaceFirst(PVP_LEAVE, "");
							assignFlag(region,DefaultFlag.FAREWELL_MESSAGE,s);
						}
					}
					//10 % of land cost is pvp flag cost
					double height = region.getMaximumPoint().getY() - region.getMinimumPoint().getY() + 1;
					double length = region.getMaximumPoint().getX() - region.getMinimumPoint().getX() + 1;
					double width = region.getMaximumPoint().getZ() - region.getMinimumPoint().getZ() + 1;
					double cost = LandManager.getCost(height, length, width);
					LandPurchasing.economy.withdrawPlayer(sender.getName(), Math.ceil(0.1 * cost));
				case 9:
					assignFlag(region,DefaultFlag.ENDERPEARL,StateFlag.State.DENY);
				case 10:
					assignFlag(region,DefaultFlag.PISTONS,StateFlag.State.DENY);
				case 11:
					assignFlag(region,DefaultFlag.USE,StateFlag.State.DENY);
				}
			}
			else if(args[1].equals("allow")){
				switch(flagIndex){
				case 0:
					assignFlag(region,DefaultFlag.MOB_DAMAGE,StateFlag.State.ALLOW);
				case 1:
					assignFlag(region,DefaultFlag.MOB_SPAWNING,StateFlag.State.ALLOW);		
				case 2:
					assignFlag(region,DefaultFlag.CREEPER_EXPLOSION,StateFlag.State.ALLOW);
				case 3:
					assignFlag(region,DefaultFlag.ENDER_BUILD,StateFlag.State.ALLOW);
				case 4:
					assignFlag(region,DefaultFlag.TNT,StateFlag.State.ALLOW);
				case 5:
					assignFlag(region,DefaultFlag.FIRE_SPREAD,StateFlag.State.ALLOW);
				case 6:
					assignFlag(region,DefaultFlag.GHAST_FIREBALL,StateFlag.State.ALLOW);
				case 7:
					assignFlag(region,DefaultFlag.CHEST_ACCESS,StateFlag.State.ALLOW);
				case 8:
					assignFlag(region,DefaultFlag.PVP,StateFlag.State.ALLOW);
					
					//required warning upon entry/exit of pvp region
					String s = region.getFlag(DefaultFlag.GREET_MESSAGE);
					if(s != null){
						assignFlag(region,DefaultFlag.GREET_MESSAGE,PVP_WARNING + s);
					}
					else {
						assignFlag(region,DefaultFlag.GREET_MESSAGE,PVP_WARNING);
					}
					
					s = region.getFlag(DefaultFlag.FAREWELL_MESSAGE);
					if(s != null){
						assignFlag(region,DefaultFlag.FAREWELL_MESSAGE,PVP_LEAVE + s);
					}
					else {
						assignFlag(region,DefaultFlag.FAREWELL_MESSAGE,PVP_LEAVE);
					}
					
					//10 % of land cost is pvp flag cost
					double height = region.getMaximumPoint().getY() - region.getMinimumPoint().getY() + 1;
					double length = region.getMaximumPoint().getX() - region.getMinimumPoint().getX() + 1;
					double width = region.getMaximumPoint().getZ() - region.getMinimumPoint().getZ() + 1;
					double cost = LandManager.getCost(height, length, width);
					LandPurchasing.economy.withdrawPlayer(sender.getName(), Math.ceil(0.1 * cost));
					
				case 9:
					assignFlag(region,DefaultFlag.ENDERPEARL,StateFlag.State.ALLOW);
				case 10:
					assignFlag(region,DefaultFlag.PISTONS,StateFlag.State.ALLOW);
				case 11:
					assignFlag(region,DefaultFlag.USE,StateFlag.State.ALLOW);
				}
			}
			else{
				sender.sendMessage("Invalid argument " + args[1]);
			}
		case 2:	//boolean flags
			if(args[1].equals("deny")){
				switch(flagIndex){
				case 0:
					assignFlag(region,DefaultFlag.NOTIFY_ENTER,false);
				case 1:
					assignFlag(region,DefaultFlag.NOTIFY_LEAVE,false);
				}
			}
			else if(args[1].equals("allow")){
				switch(flagIndex){
				case 0:
					assignFlag(region,DefaultFlag.NOTIFY_ENTER,true);
				case 1:
					assignFlag(region,DefaultFlag.NOTIFY_LEAVE,true);
				}
			}
			else{
				sender.sendMessage("Invalid argument " + args[1]);
			}
		case 3:	//string flags
			if(args[1].startsWith("WARNING:")){
				sender.sendMessage("Messages starting with 'Warning:' are reserved for server use");
				return false;
			}
			
			LandPurchasing.economy.withdrawPlayer(sender.getName(), 1000);
			
			switch(flagIndex){
			case 0:
				assignFlag(region,DefaultFlag.GREET_MESSAGE,args[1]);
			case 1:
				assignFlag(region,DefaultFlag.FAREWELL_MESSAGE,args[1]);
			}
		case 0:
			sender.sendMessage("Invalid flag name");
			return false;
		}
		return true;	
	}
	
	/**
	 * Recieves instructions and executes the cooresponding command
	 */
	private void assignFlag(ProtectedRegion region, StateFlag flag, StateFlag.State value){
		region.setFlag(flag, value);
	}
	
	private void assignFlag(ProtectedRegion region, BooleanFlag flag, boolean value){
		region.setFlag(flag, value);
	}
	
	private void assignFlag(ProtectedRegion region, StringFlag flag, String value){
		region.setFlag(flag, value);
	}

}
