package com.m0pt0pmatt.LandPurchasing.flags;

import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;

public enum CustomFlag{
	
	OUTSIDEPISTONS(new LandFlag(new StateFlag("outside-pistons", false))),
	BANKFLAG(new LandFlag(new BooleanFlag("bankflag")));
	
	private LandFlag flag;
	
	private CustomFlag(LandFlag flag){
		this.flag = flag;
	}
	
	public LandFlag getFlag(){
		return flag;
	}
	
}
