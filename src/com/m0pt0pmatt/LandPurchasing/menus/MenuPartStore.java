package com.m0pt0pmatt.LandPurchasing.menus;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.m0pt0pmatt.menuservice.api.AbstractComponent;
import com.m0pt0pmatt.menuservice.api.Component;
import com.m0pt0pmatt.menuservice.api.ComponentType;
import com.m0pt0pmatt.menuservice.api.MenuPart;
import com.m0pt0pmatt.menuservice.api.attributes.Attribute;

/**
 * The MenuPart store holds menu parts for the entire plugin
 * @author Matthew
 *
 */
public class MenuPartStore {

	public MenuPart exitButton;
	
	public MenuPartStore(){
		createExitButton();
	}
	
	private void createExitButton(){
		
		List<Component> components = new LinkedList<Component>();
		AbstractComponent exitButton = new AbstractComponent();
		exitButton.setTag("exitButton");
		exitButton.setType(ComponentType.BUTTON);
		exitButton.addAttribute(Attribute.ITEM, new ItemStack(Material.APPLE));
		List<Integer> intActions = new LinkedList<Integer>();
		intActions.add(1);
		exitButton.addAction("onClick", intActions);
		
		components.add(exitButton);
		
		this.exitButton = new MenuPart("exitButton", components);
		this.exitButton.setListener(new ExitListener());
		
	}
	
}
