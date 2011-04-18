package com.superguo.jiroplayer;

public final class GameModel {
	private PlayLayout iDefaultLayout = new PlayLayout();
	public PlayLayout iLayout;
	public PlayModel iPlayingModel = new PlayModel(); 
	
	public GameModel()
	{
		iLayout = (PlayLayout) iDefaultLayout.clone();
	}
}
