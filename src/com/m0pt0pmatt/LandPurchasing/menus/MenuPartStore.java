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
	
	public MenuPart createLandList(String playerName, int startIndex){
		
		List<Component> components = new LinkedList<Component>();
		
		Component leftButton = new AbstractComponent();
		leftButton.setTag("leftButton");
		leftButton.setType(ComponentType.BUTTON);
		leftButton.addAction(DefaultAction.LEFT_CLICK, 2);
		leftButton.addAttribute(Attribute.ITEM, new ItemStack(Material.BEDROCK));
		leftButton.addAttribute("nextStart", java.lang.Math.max(0, startIndex - (9 * 4)));
		leftButton.addAttribute(Attribute.X, 0);
		leftButton.addAttribute(Attribute.Y, 4);
		components.add(leftButton);
		
		List<String> regions = LandPurchasing.landManager.getRegions(Bukkit.getPlayer(playerName));
		
		int i = startIndex;
		int end = startIndex + (4*9);
		int x = 0;
		int y = 0;
		int nextStart = 0;
		
		for (String regionName: regions){
			
			if (i < startIndex){
				i++;
				continue;
			}
			if (x > 9){
				x = 0;
				y++;
			}
			if (y > 3){
				nextStart = i;
				break;
			}
			
			Component component = new AbstractComponent();
			component.setTag("landButton_" + regionName);
			component.setType(ComponentType.BUTTON);
			component.addAction(DefaultAction.LEFT_CLICK, 1);
			component.addAttribute("landplot", regionName);
			component.addAttribute(Attribute.ITEM, new ItemStack(Material.WOOL));
			component.addAttribute(Attribute.X, x);
			component.addAttribute(Attribute.Y, y);
			components.add(component);
			i++;
			x++;
		}
		
		Component rightButton = new AbstractComponent();
		rightButton.setTag("rightButton");
		rightButton.setType(ComponentType.BUTTON);
		rightButton.addAction(DefaultAction.LEFT_CLICK, 3);
		rightButton.addAttribute(Attribute.ITEM, new ItemStack(Material.BEDROCK));
		rightButton.addAttribute("nextStart", nextStart);
		rightButton.addAttribute(Attribute.X, 8);
		rightButton.addAttribute(Attribute.Y, 4);
		components.add(rightButton);
		
		MenuPart landList = new MenuPart("landlist_" + playerName, components);
		landList.setListener(mainListener);
		return landList;
	}
	
}
