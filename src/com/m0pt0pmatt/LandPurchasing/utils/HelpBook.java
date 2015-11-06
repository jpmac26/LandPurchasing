package com.m0pt0pmatt.LandPurchasing.utils;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

/**
 * A helpbook container class with all the text for the helpbook.
 * This is a temporary fix
 * @author Skyler
 *
 */
public class HelpBook {
	
	private static List<String> pages = Arrays.asList(
			"Land Purchasing: A beginner's guide",
			"Before continuing, it's important to realize that we use TWO services to protect our land; Worldguard (which you use landpurchasing to get to) and LWC. Worldguard protects blocks. LWC protects doors and chests.",
			"To begin doing anything with land, you first have to buy land. To do this, first select a region you want to buy (cuboid regions only) using the wand (which you can use //wand to get). Then use the ",
			"/buyland command to purchase it. Your buyland command should look like this:\n/buyland [name]\nWhere instead of '[name]' you put what you want to name the land. What you name the land must be unique and not",
			"contain any special characters. If you had enough money and the region was unique and didn't overlap with anyone else's, you will be told you were successful. If it overlaps, try moving your region somewhere where",
			"people (including yourself!) haven't purchased.\nAfter purchasing land, nobody but you (and members you add) can modify any of the blocks. To add members (who will not be able to sell the land, but will be able to",
			"modify it), use the /addmember command like this:\n/addmember [plot_name] [player_name]\nNote that the plot name is the same you gave it when you purchased it. Also note that the player you are trying to add must",
			"be online when you add them!\nAfter adding member, you should also consider the FLAGS that your land has. Flags let you turn on and off permission of non-members to do things. Some flags are:",
			"VEHICLE_PLACE, GREETING, and USE. To get a list of flags you can set, use /flagland ?. After that, set flags using the flagland command:\n/flagland [plot_name] [flag] [allow/deny]",
			"You can sell land after you have purchased it using the /sellland command. Note the 3 l's of death and confusion.",
			"The last thing to note is that some plots can be LEASED. These plots are in spawn, and have signs and gold blocks telling you what the plot is. Right-clicking the sign will also inform you on how to lease them.",
			"NOTE: Leased plots are only yours for 2 weeks. You can pay the next 2-week period ahead of time, but must remember to get on and pay your lease or your land will go back up for sale after your lease expires!",
			"For a list of leases available, use the /listings command.",
			"Command List:\n/listland\n/priceland\n/landinfo\n/buyland\n/sellland\n/addmember\n/removemember\n/flagland\n/listings\n/listleases"
			);
	
	public static void givePlayerBook(Player player) {
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta meta = (BookMeta) book.getItemMeta();
		meta.setPages(pages);
		meta.setDisplayName("Land Help Book");
		meta.setAuthor("Dove-Breneth III");
		book.setItemMeta(meta);
		
		player.getInventory().addItem(book);
	}
	
}
