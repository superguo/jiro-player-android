

package com.superguo.jiroplayer;

import javax.microedition.khronos.opengles.*;

import android.graphics.*;
import android.view.*;

import com.superguo.jiroplayer.*;
import com.superguo.ogl2d.*;

public class PlayScene extends O2Scene {
	private GameModel iGameModel;
	private GameModel.Layout iLayout;
	private O2Texture mDebugTextSprite;
	private O2Texture mBgSprite;
	private O2Texture mMTaikoSprite;
	private O2Texture mNotesSprite;
	private O2TextureDivider mNotesSpriteSheet;
	
	/*
	private Paint paint;
	private Typeface typeface;
	private long paintId;
	*/
	
	public PlayScene(O2Director director, GameModel gameModel)
	{
		super(director);
		iGameModel = gameModel;
		iLayout = gameModel.iLayout;
		
		/*
		paint = new Paint();
		typeface = Typeface.createFromAsset(director.getContext().getAssets(), "a-otf-kanteiryustd-ultra.otf");
		paint.setTypeface(typeface);
		paintId = director.addPaint(paint);
		*/
	}
	

	@Override
	public void onEnteringScene() {
		O2TextureManager mgr = director.getSpriteManager();
		mBgSprite 			= mgr.createFromResource(R.drawable.bg, true);
		mMTaikoSprite		= mgr.createFromResource(R.drawable.mtaiko, true);
		mMTaikoSprite.valign = O2Texture.VALIGN_MIDDLE;
		// mDebugTextSprite 	= mgr.createFromString("hello", true);
		mNotesSprite		= mgr.createFromResource(R.drawable.notes, true);
		mNotesSpriteSheet 	= mgr.createSpriteSheetWithRowsAndCols(mNotesSprite, 2, 13);
		
	}

	@Override
	public void onLeavingScene() {
		mBgSprite.dispose();
		mBgSprite = null;
		
		mNotesSprite.dispose();
		mNotesSprite = null;
		
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
		mBgSprite.draw(0, 0);
		mMTaikoSprite.draw(0, iLayout.iScrollFieldY);
		MotionEvent e = getMotionEvent();
		if (e!=null)
		{
			float x = director.toXLogical(e.getX());
			float y = director.toYLogical(e.getY());
		}
	}
}
