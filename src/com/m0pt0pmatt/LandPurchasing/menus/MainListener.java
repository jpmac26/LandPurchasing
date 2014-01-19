package com.m0pt0pmatt.LandPurchasing.menus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.m0pt0pmatt.LandPurchasing.LandPurchasing;
import com.m0pt0pmatt.menuservice.api.Component;
import com.m0pt0pmatt.menuservice.api.actions.Action;
import com.m0pt0pmatt.menuservice.api.actions.ActionListener;

public class MainListener implements ActionListener{

	@Override
	public String getName() {
		return "LandListener";
	}

	@Override
	public String getPlugin() {
		return "LandPurchasing";
	}

	@Override
	public void playerAdded(String arg0) {}

	@Override
	public void playerCountZero(String arg0) {}

	@Override
	public void playerRemoved(String arg0) {}

	@Override
	public void handleAction(Action action, int actionTag, String playerName, Component component) {
		
		Player player = Bukkit.getPlayer(playerName);
		
		switch(ActionTag.getActionTag(actionTag)){
		case EXIT:
			exitMenu(playerName);
			break;
		case OPENPLOTMENU:
			LandPurchasing.menuStore.openPlotMenu(player, (String) component.getAttribute("landplot"));
		}
			
	}


	private void exitMenu(String playerName) {
		Bukkit.getPlayer(playerName).closeInventory();
	}

}
