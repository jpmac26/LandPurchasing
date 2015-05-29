package com.m0pt0pmatt.LandPurchasing;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.m0pt0pmatt.LandPurchasing.Scheduling.Date;
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
	 * &nbsp;&nbsp;&nbsp;&nbsp;Date:
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;SerializedDateStuff
	 * </p>
	 * @param section The section to load from
	 * @return The resultant LeaseLand object, or <i>null</i> if there was an error
	 */
	public static LeaseLand fromConfig(ConfigurationSection section) {
		if (section == null) {
			return null;
		}
		
		//TODO check and make sure that region doesn't conflict with any others!
		LeaseLand land = new LeaseLand();
		
		
		
		return land;
	}
	
	/**
	 * Constructor-method use only!
	 */
	private LeaseLand() {
		super();
	}
	
	public LeaseLand(ProtectedCuboidRegion region) {
		super();
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
		config.createSection("DueDate");
		
		config.set("Block1.X", this.land.getMinimumPoint().getBlockX());
		config.set("Block1.Y", this.land.getMinimumPoint().getBlockY());
		config.set("Block1.Z", this.land.getMinimumPoint().getBlockZ());

		config.set("Block2.X", this.land.getMinimumPoint().getBlockX());
		config.set("Block2.Y", this.land.getMinimumPoint().getBlockY());
		config.set("Block2.Z", this.land.getMinimumPoint().getBlockZ());
		
		config.set("DueDate.Day", dueDate.getDay());
		config.set("DueDate.Month", dueDate.getMonth());
		config.set("DueDate.Year", dueDate.getYear());
		
		return config;
	}
}
