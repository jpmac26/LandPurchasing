package com.m0pt0pmatt.LandPurchasing;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;
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
	 * @param section The section to load from
	 * @return The resultant LeaseLand object, or <i>null</i> if there was an error
	 */
	public static LeaseLand fromConfig(String name, ConfigurationSection section) {
		if (section == null) {
			return null;
		}
		
		//TODO check and make sure that region doesn't conflict with any others!
		
		//load up info from config
		BlockVector b1, b2;
		b1 = new BlockVector(section.getInt("Block1.X"), section.getInt("Block1.Y"), section.getInt("Block1.Z"));
		b2 = new BlockVector(section.getInt("Block2.X"), section.getInt("Block2.Y"), section.getInt("Block2.Z"));
		
		
		ProtectedCuboidRegion region = new ProtectedCuboidRegion(name, b1, b2);
		
		
		LandPurchasing.wgplugin.getRegionManager(Bukkit.getWorld("Homeworld")).addRegion(region);
		

		LeaseLand lease = new LeaseLand(region);
		
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
		
		return lease;
	}
	
	
	public LeaseLand(ProtectedCuboidRegion region) {
		super(region);
		dueDate = null;
	}
	
	public Date getDueDate() {
		return dueDate;
	}
	
	public void setDueDate(Date date) {
		dueDate = date;
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
		
		config.set("Block1.X", this.land.getMinimumPoint().getBlockX());
		config.set("Block1.Y", this.land.getMinimumPoint().getBlockY());
		config.set("Block1.Z", this.land.getMinimumPoint().getBlockZ());

		config.set("Block2.X", this.land.getMaximumPoint().getBlockX());
		config.set("Block2.Y", this.land.getMaximumPoint().getBlockY());
		config.set("Block2.Z", this.land.getMaximumPoint().getBlockZ());
		
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
}
