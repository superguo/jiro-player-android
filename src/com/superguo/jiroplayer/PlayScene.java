

package com.superguo.jiroplayer;

import javax.microedition.khronos.opengles.*;
import com.superguo.ogl2d.*;

public class PlayScene extends O2Scene {
	private O2Director director;
	private O2Sprite bgSprite;
	
	public PlayScene(O2Director director, GameModel gameModel)
	{
		super(director);
		bgSprite = director.getSpriteManager().createFromResource(R.drawable.bg, true);
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void draw(GL10 gl) {
		bgSprite.draw(0, 0);		
	}

}
