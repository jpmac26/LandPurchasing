package com.m0pt0pmatt.LandPurchasing;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;







//import com.m0pt0pmatt.LandPurchasing.Scheduling.Date;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;

/**
 * Holds information about a plot of land that is able to be or is being leased out.<br />
 * These plots hold information about when their lease is up
 * @author Skyler
 */
public class LeaseLand extends Land {
	
	private Date dueDate;
	
	private Location signLoc;
	
	private BlockFace facing;
	
	/**
	 * Creates a new LeaseLand object from the passed configuration section<br />
	 * <p>
	 * Configuration sections take the following format:<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;Block1:<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;X: 312<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Y: 70<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Z: -700<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;Block2:<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;X: 312<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Y: 70<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Z: -700<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;DueDate: 152362727<br />
//	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Day: 14<br />
//	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Month: 09<br />
//	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Year: 2014<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;Owner: 3515-gaey3-61-61-ah-36-ahh<br />
	 * </p>
	 * @param name The name of the section, for region ID purposes
	 * @param section The section to load from
	 * @return The resultant LeaseLand object, or <i>null</i> if there was an error
	 */
	public static LeaseLand fromConfig(String name, ConfigurationSection section) {
		if (section == null || name == null || name.trim().isEmpty()) {
			return null;
		}
		
		//TODO check and make sure that region doesn't conflict with any others!
		
		
		//load up info from config
		BlockVector b1, b2;
		b1 = new BlockVector(section.getInt("Block1.X"), section.getInt("Block1.Y"), section.getInt("Block1.Z"));
		b2 = new BlockVector(section.getInt("Block2.X"), section.getInt("Block2.Y"), section.getInt("Block2.Z"));
		
		Location signLoc = new Location(Bukkit.getWorld("Homeworld"), 
				section.getInt("Sign.X"),
				section.getInt("Sign.Y"),
				section.getInt("Sign.Z"));
		
		ProtectedCuboidRegion region = new ProtectedCuboidRegion(name, b1, b2);
		
		
		LandPurchasing.wgplugin.getRegionManager(Bukkit.getWorld("Homeworld")).addRegion(region);
		

		LeaseLand lease = new LeaseLand(signLoc, region);
		lease.facing = BlockFace.valueOf(section.getString("Sign.Face"));
		
//		if (section.contains("Owner") && section.contains("DueDate") && section.contains("DueDate.Day") && section.contains("DueDate.Month") && section.contains("DueDate.Year")) {
		if (section.contains("Owner") && section.contains("DueDate")) {
		    //due date information!
			Calendar calendar = Calendar.getInstance();
			calendar.clear();
			calendar.setTime(new Date(section.getLong("DueDate")));
			lease.dueDate = calendar.getTime();
			
			DefaultDomain dom = new DefaultDomain();
			dom.addPlayer(UUID.fromString(section.getString("Owner")));
			lease.land.setOwners(dom);
			
		}
		//else do nothing, as assumed no date info on creation
		
		

		lease.updateSign();
		
		return lease;
	}
	
	
	public LeaseLand(Location signLocation, ProtectedCuboidRegion region) {
		super(region);
		dueDate = null;
		setSignLocation(signLocation);
		
		updateSign();
	}
	
	public Date getDueDate() {
		return dueDate;
	}
	
	public void setDueDate(Date date) {
		dueDate = date;
		
		//also put up or take down sign!
		updateSign();
	}
	
	public Location getSignLocation() {
		return this.signLoc;
	}
	
	/**
	 * Returns the current LEaseLand object as a configuration section, for easy saving out
	 * to config.<br />
	 * The output of this method should be such that creating a LeaseLand object from it should
	 * be equivalent to the first.
	 * @return A configuration-section version of this LeaseLand object.
	 */
	public ConfigurationSection toConfig() {
		ConfigurationSection config = new YamlConfiguration();
		
		config.createSection("Block1");
		config.createSection("Block2");
		config.createSection("Sign");
		
		config.set("Block1.X", this.land.getMinimumPoint().getBlockX());
		config.set("Block1.Y", this.land.getMinimumPoint().getBlockY());
		config.set("Block1.Z", this.land.getMinimumPoint().getBlockZ());

		config.set("Block2.X", this.land.getMaximumPoint().getBlockX());
		config.set("Block2.Y", this.land.getMaximumPoint().getBlockY());
		config.set("Block2.Z", this.land.getMaximumPoint().getBlockZ());
		
		config.set("Sign.X", signLoc.getBlockX());
		config.set("Sign.Y", signLoc.getBlockY());
		config.set("Sign.Z", signLoc.getBlockZ());
		config.set("Sign.Face", facing.name());
		
		if (dueDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.clear();
			cal.setTime(dueDate);
//			config.set("DueDate.Day", cal.get(Calendar.DATE));
//			config.set("DueDate.Month", cal.get(Calendar.MONTH));
//			config.set("DueDate.Year", cal.get(Calendar.YEAR));
//			config.set("DueDate.Time", cal.get(Calendar.));
			config.set("DueDate", cal.getTime().getTime());
			
			//set owner as just first uuid
			config.set("Owner", land.getOwners().getUniqueIds().iterator().next().toString());
		}
		return config;
	}
	
	/**
	 * Checks current status and puts up or takes down the lease sign to reflect it
	 */
	private void updateSign() {

		Location tmpLoc = signLoc.clone().add(0.0, 1.0, 0.0);
		tmpLoc.getBlock().setType(Material.AIR);
		signLoc.getBlock().setType(Material.AIR);
		
		if (dueDate == null) {
			signLoc.getBlock().setType(Material.GOLD_BLOCK);
			Block block = tmpLoc.getBlock();
			block.setType(Material.SIGN_POST);
			Sign sign = (Sign) block.getState();
			sign.setLine(1, getID());
			sign.setLine(2, "$" + getCost());
			
			org.bukkit.material.Sign sobj = new org.bukkit.material.Sign();
			sobj.setFacingDirection(facing);
			
			sign.setData(sobj);
			
			sign.update();
		}
	}
	
	public void setSignLocation(Location loc) {
		
		signLoc = loc;
		
		//nasty copied code
		
		float y = loc.getYaw();
	     
        if( y < 0 ){y += 360;}
     
        y %= 360;
     
        int i = (int)((y+8) / 22.5);
        i += 4;
        i %= 16;
     
        if(i == 0){facing = BlockFace.WEST;}
        else if(i == 1){facing = BlockFace.WEST_NORTH_WEST;}
        else if(i == 2){facing = BlockFace.NORTH_WEST;}
        else if(i == 3){facing = BlockFace.NORTH_NORTH_WEST;}
        else if(i == 4){facing = BlockFace.NORTH;}
        else if(i == 5){facing = BlockFace.NORTH_NORTH_EAST;}
        else if(i == 6){facing = BlockFace.NORTH_EAST;}
        else if(i == 7){facing = BlockFace.EAST_NORTH_EAST;}
        else if(i == 8){facing = BlockFace.EAST;}
        else if(i == 9){facing = BlockFace.EAST_SOUTH_EAST;}
        else if(i == 10){facing = BlockFace.SOUTH_EAST;}
        else if(i == 11){facing = BlockFace.SOUTH_SOUTH_EAST;}
        else if(i == 12){facing = BlockFace.SOUTH;}
        else if(i == 13){facing = BlockFace.SOUTH_SOUTH_WEST;}
        else if(i == 14){facing = BlockFace.SOUTH_WEST;}
        else if(i == 15){facing = BlockFace.WEST_SOUTH_WEST;}
        else {facing = BlockFace.WEST;}
		
	}
	
	@Override
	public int getCost() {
		return super.getCost() / 2;
	}
	
	@Override
	public String toString() {
		return "Lease Plot [" + this.getID()+ "]";
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof LeaseLand)) {
			return false;
		}
		
		LeaseLand other = (LeaseLand) o;
		
		return (other.getID().equals(getID()));
	}
}
