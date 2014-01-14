package com.m0pt0pmatt.LandPurchasing.menus;

import org.bukkit.command.CommandSender;

import com.m0pt0pmatt.LandPurchasing.LandPurchasing;
import com.m0pt0pmatt.menuservice.api.Menu;

public class MenuStore {

	private MenuPartStore parts;
	
	public Menu mainMenu;
	
	public MenuStore(){
		parts = new MenuPartStore();
		
		createMainMenu();
	}
	
	private void createMainMenu(){
		mainMenu = new Menu();
		mainMenu.addPart(parts.exitButton);
	}
	
	/**
	 * Opens the LandMenu: A graphical interface for most land-related commands
	 * @param sender
	 */
	public void openMainMenu(CommandSender sender) {
		LandPurchasing.menuService.getRenderer("inventory").openMenu(LandPurchasing.menuStore.mainMenu, sender.getName());
	}
	
}
