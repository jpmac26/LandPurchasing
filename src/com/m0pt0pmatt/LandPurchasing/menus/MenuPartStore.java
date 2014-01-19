package com.m0pt0pmatt.LandPurchasing.menus;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.m0pt0pmatt.LandPurchasing.LandPurchasing;
import com.m0pt0pmatt.menuservice.api.AbstractComponent;
import com.m0pt0pmatt.menuservice.api.Component;
import com.m0pt0pmatt.menuservice.api.ComponentType;
import com.m0pt0pmatt.menuservice.api.MenuPart;
import com.m0pt0pmatt.menuservice.api.actions.ActionListener;
import com.m0pt0pmatt.menuservice.api.actions.DefaultAction;
import com.m0pt0pmatt.menuservice.api.attributes.Attribute;

/**
 * The MenuPart store holds menu parts for the entire plugin
 * @author Matthew
 *
 */
public class MenuPartStore {

	public MenuPart exitButton;
	
	public ActionListener mainListener;
	
	public MenuPartStore(){
		mainListener = new MainListener();
		createExitButton();
	}
	
	private void createExitButton(){
		
		List<Component> components = new LinkedList<Component>();
		AbstractComponent exitButton = new AbstractComponent();
		exitButton.setTag("exitButton");
		exitButton.setType(ComponentType.BUTTON);
		exitButton.addAttribute(Attribute.ITEM, new ItemStack(Material.APPLE));
		exitButton.addAction(DefaultAction.LEFT_CLICK, 0);
		
		components.add(exitButton);
		
		this.exitButton = new MenuPart("exitButton", components);
		this.exitButton.setListener(mainListener);
		
	}
	
	public MenuPart createLandList(String playerName, int startIndex, int size, int rowSize){
		
		List<Component> components = new LinkedList<Component>();
		
		List<String> regions = LandPurchasing.landManager.getRegions(Bukkit.getPlayer(playerName));
		for (String regionName: regions){
			Component component = new AbstractComponent();
			component.setTag("landButton_" + regionName);
			component.setType(ComponentType.BUTTON);
			component.addAction(DefaultAction.LEFT_CLICK, 1);
			component.addAttribute("landplot", regionName);
			component.addAttribute(Attribute.ITEM, new ItemStack(Material.WOOL));
			components.add(component);
		}
		
		MenuPart landList = new MenuPart("landlist_" + playerName, components);
		landList.setListener(mainListener);
		return landList;
	}
	
}
