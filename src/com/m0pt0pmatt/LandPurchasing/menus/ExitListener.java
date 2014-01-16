package com.m0pt0pmatt.LandPurchasing.menus;

import org.bukkit.Bukkit;

import com.m0pt0pmatt.menuservice.api.actions.ActionEvent;
import com.m0pt0pmatt.menuservice.api.actions.ActionListener;

public class ExitListener implements ActionListener{

	@Override
	public String getName() {
		return "ExitListener";
	}

	@Override
	public String getPlugin() {
		return "LandPurchasing";
	}

	@Override
	public void handleAction(ActionEvent event) {
		Bukkit.getPlayer(event.getPlayerName()).closeInventory();
	}

	@Override
	public void playerAdded(String arg0) {}

	@Override
	public void playerCountZero(String arg0) {}

	@Override
	public void playerRemoved(String arg0) {}

}
