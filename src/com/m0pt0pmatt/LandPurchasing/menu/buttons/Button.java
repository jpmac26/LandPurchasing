package com.m0pt0pmatt.LandPurchasing.menu.buttons;

import java.util.HashMap;
import java.util.Map;

import com.m0pt0pmatt.menuservice.api.AbstractComponent;
import com.m0pt0pmatt.menuservice.api.Component;
import com.m0pt0pmatt.menuservice.api.ComponentType;
import com.m0pt0pmatt.menuservice.api.attributes.Attribute;
import com.m0pt0pmatt.menuservice.api.rendering.Renderable;

public abstract class Button implements Renderable{

	protected String action;
	protected String description;
	
	@Override
	public Component toComponent() {
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put(Attribute.TEXT.getName(), action);
		AbstractComponent component = new AbstractComponent(attributes);
		component.setTag("button-action");
		component.setType(ComponentType.BUTTON);
		return component;
	}
	
	

}
