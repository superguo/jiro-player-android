

package com.superguo.jiroplayer;

import javax.microedition.khronos.opengles.*;

import android.graphics.*;
import android.view.*;

import com.superguo.jiroplayer.*;
import com.superguo.ogl2d.*;

public class PlayScene extends O2Scene {
	private GameModel iGameModel;
	private GameModel.Layout iLayout;
	//private O2Texture mDebugTextSprite;
	private O2Sprite iBgSprite;
	private O2Sprite iMTaikoSprite;
	private O2TextureSlices iNotesSlices;
	private O2Sprite iTargetNoteSprite;
	
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
		O2TextureManager mgr = director.getTextureManager();
		iBgSprite 			= new O2Sprite(mgr.createFromResource(R.drawable.bg, true));
		iMTaikoSprite		= new O2Sprite(mgr.createFromResource(R.drawable.mtaiko, true));
		iMTaikoSprite.iValign = O2Sprite.VALIGN_MIDDLE;
		iMTaikoSprite.iX	= 0;
		iMTaikoSprite.iY	= iLayout.iScrollFieldY;
		
		// mDebugTextSprite 	= mgr.createFromString("hello", true);
		iNotesSlices		= mgr.createSpriteSheetWithRowsAndCols(
				mgr.createFromResource(R.drawable.notes, true),
				2,
				13);
		iTargetNoteSprite 			= new O2Sprite(iNotesSlices, 0);
		iTargetNoteSprite.iValign	=  O2Sprite.VALIGN_MIDDLE;
		iTargetNoteSprite.iX		= 130;
		iTargetNoteSprite.iY		= iLayout.iScrollFieldY;
	}

	@Override
	public void onLeavingScene() {
		iBgSprite.dispose();
		iBgSprite = null;
		
		iMTaikoSprite.dispose();
		iMTaikoSprite = null;
		
		iTargetNoteSprite.dispose();
		iTargetNoteSprite = null;
		
		iNotesSlices = null;
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
	public void postDraw(GL10 gl) {
		MotionEvent e = getMotionEvent();
		if (e!=null)
		{
			float x = director.toXLogical(e.getX());
			float y = director.toYLogical(e.getY());
		}
	}
}
