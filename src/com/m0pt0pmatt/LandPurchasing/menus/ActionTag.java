package com.m0pt0pmatt.LandPurchasing.menus;

import java.util.HashMap;
import java.util.Map;

public enum ActionTag {
	EXIT(0),
	OPENPLOTMENU(1);
	
	private int tag;
	
	private static Map<Integer, ActionTag> tags = new HashMap<Integer, ActionTag>();
	static{
		tags.put(EXIT.tag, EXIT);
		tags.put(OPENPLOTMENU.tag, OPENPLOTMENU);
	}
	
	
	private ActionTag(int tag){
		this.tag = tag;
	}
	
	public int getTag(){
		return tag;
	}

	public static ActionTag getActionTag(int actionTag) {
		return tags.get(actionTag);
	}
	
}
