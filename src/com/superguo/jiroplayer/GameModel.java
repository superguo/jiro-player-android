package com.superguo.jiroplayer;

public final class GameModel {
	private PlayLayout iDefaultLayout = new PlayLayout();
	public PlayLayout iLayout;
	
	public GameModel()
	{
		iLayout = (PlayLayout) iDefaultLayout.clone();
	}
}
