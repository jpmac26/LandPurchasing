package com.m0pt0pmatt.LandPurchasing.managers;

public class LandServiceProvider implements LandService{

	private FlagManager flagManager = null;
	private LandManager landManager = null;
	
	public LandServiceProvider (FlagManager flagManager, LandManager landManager){
		this.flagManager = flagManager;
		this.landManager = landManager;
	}
	
	@Override
	public FlagManager getFlagManager() {
		return flagManager;
	}

	@Override
	public LandManager getLandManager() {
		return landManager;
	}

}
