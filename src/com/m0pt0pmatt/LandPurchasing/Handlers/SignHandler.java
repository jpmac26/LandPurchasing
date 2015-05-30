package com.m0pt0pmatt.LandPurchasing.Handlers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.m0pt0pmatt.LandPurchasing.LandPurchasing;
import com.m0pt0pmatt.LandPurchasing.LeaseLand;
import com.m0pt0pmatt.LandPurchasing.Effects.AreaView;
import com.sk89q.worldedit.BlockVector;

/**
 * Handler for handling info requests from lease signs
 * @author Skyler
 *
 */
public class SignHandler implements Listener {
	
	private static SignHandler handler;
	
	public static SignHandler getHandler() {
		if (handler == null) {
			handler = new SignHandler();
		}
		
		return handler;
	}
	
	private SignHandler() {
		Bukkit.getPluginManager().registerEvents(this, LandPurchasing.plugin);
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent e) {
		if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			//cycle through our signs and see if we can find one that's at that location
			Location clickedLoc = e.getClickedBlock().getLocation().add(0, -1, 0);
			
			for (LeaseLand plot : LandPurchasing.landManager.getLeasePlots()) {
				//if (plot.getSignLocation()..equals(clickedLoc)) {
				if (plot.getSignLocation().distance(clickedLoc) <= 0.1)	{
					//found it!
					//send user info, and display the outer shell with glass
					new AreaView(plot.getRegion());
					
					int area = plot.getRegion().volume() / (1 + (plot.getRegion().getMaximumPoint().getBlockY() - plot.getRegion().getMinimumPoint().getBlockY()));
					BlockVector min, max;
					min = plot.getRegion().getMinimumPoint();
					max = plot.getRegion().getMaximumPoint();
					
					e.getPlayer().sendMessage(plot.getID());
					e.getPlayer().sendMessage("This region extends from (" + min.getBlockX() + ", " + min.getBlockY() + ", " + min.getBlockZ() + ") to (" + max.getBlockX() + ", " + max.getBlockY() + ", " + max.getBlockZ() + ")");
					e.getPlayer().sendMessage("This plot contains " + 
							plot.getRegion().volume() + " blocks, as has an base of " + area
							);
					e.getPlayer().sendMessage("The price for this plot is " + 
						ChatColor.DARK_GREEN + "$" + plot.getCost() + ChatColor.RESET);
					e.getPlayer().sendMessage("");
					e.getPlayer().sendMessage("To lease this plot, type " +
						ChatColor.DARK_PURPLE + "/leaseland " + plot.getID() + ChatColor.RESET);
										
					return;
				}
			}
		}
	}
}
