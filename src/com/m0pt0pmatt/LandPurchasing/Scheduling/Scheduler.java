package com.m0pt0pmatt.LandPurchasing.Scheduling;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.m0pt0pmatt.LandPurchasing.LandPurchasing;
import com.m0pt0pmatt.LandPurchasing.LeaseLand;
import com.sk89q.worldguard.domains.DefaultDomain;

/**
 * Scheduler class for checking lease agreements and making sure nobody is late!
 * @author Skyler
 *
 */
public class Scheduler extends BukkitRunnable {
	
	private static Scheduler sched = null;
	
	public static Scheduler getScheduler() {
		if (sched == null) {
			sched = new Scheduler();
		}
		
		return sched;
	}
	
	private Scheduler() {
		//run self now and every 30 minutes
		Bukkit.getScheduler().runTaskTimerAsynchronously(LandPurchasing.plugin, this, 0l, 1000 * 60 * 30);
	}
	
	@Override
	public void run() {
		
		LandPurchasing.plugin.getLogger().info("Running lease check...");
		
		//grab all our leased plots
		Set<LeaseLand> plots = LandPurchasing.landManager.getLeasePlots();
		if (plots.isEmpty()) {
			return;
		}
		

		//grab right now's time and date
		Date now = new Date();
		
		//set up out calender to reflect two days from now, for warning (see for loop)
		Calendar warningDate = Calendar.getInstance();
		warningDate.clear();
		warningDate.setTime(now);
		warningDate.add(Calendar.DATE, 2);
		
		for (LeaseLand plot : plots) {
			if (plot.getDueDate() != null) {
				//it's being leased. Check the date
				
				if (plot.getDueDate().before(now)) {
					//D: oh no! They're late!

					Player owner = Bukkit.getPlayer(plot.getRegion().getOwners().getUniqueIds().iterator().next());
					if (owner.isOnline()) {
						owner.sendMessage("Your lease for the plot [" + plot.getID() + "] just expired!");
					}
					
					plot.setDueDate(null);
					plot.getRegion().setOwners(new DefaultDomain());
					
					
					continue;
				}
				
				//it's not late, but is it getting close?
				//warn them within two days
				if (warningDate.getTime().after(plot.getDueDate())) {
					//less than two days till it expires!
					Player owner = Bukkit.getPlayer(plot.getRegion().getOwners().getUniqueIds().iterator().next());
					if (owner.isOnline()) {
						owner.sendMessage("Reminder: Your lease for the plot [" + plot.getID() + 
								"] expires " + plot.getDueDate());
					}
					//TODO if we get a mail system, add a mail message?
				}
			}
		}
	}
	

}
