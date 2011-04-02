package com.superguo.jiroplayer;

import com.superguo.ogl2d.O2Texture;
import com.superguo.ogl2d.O2TextureManager;

public class FPSHolder {
	private static FPSHolder 	gInstance;
	private int 	 			iFPSCount;
	private int 	 			iFPSDisplay;
	private long	 			iLastFPSRecTime;
	private O2Texture 			iFPSTex;
	private O2Texture 			iNumerTex[];
	
	private FPSHolder()
	{
		O2TextureManager mgr = O2TextureManager.getInstance();
		iFPSTex = mgr.createFromString("FPS: ", true);
		iNumerTex = new O2Texture[10];
		for (int i=0; i<10; ++i)
		{
			iNumerTex[i] = mgr.createFromString(Integer.toString(i), true);
		}
	}

	public final static FPSHolder getInstance()
	{
		if (gInstance==null)
			gInstance = new FPSHolder();
		return gInstance;
	}
	
	public void dispose()
	{
		if (iFPSTex!=null)
		{
			iFPSTex.dispose();
			iFPSTex = null;
		}
		
		if (iNumerTex!=null)
		{
			for (O2Texture tex : iNumerTex)
				tex.dispose();
			iNumerTex = null;
		}
		
		gInstance = null;
	}
	
	public void showFPS()
	{
		iFPSCount++;

		long FPSRecTime = android.os.SystemClock.uptimeMillis();
		if (FPSRecTime - iLastFPSRecTime > 1000)
		{
			iFPSDisplay = iFPSCount;
			iFPSCount = 0;
			iLastFPSRecTime = FPSRecTime;
		}
		
		if (iFPSDisplay>0)
		{
			int x = 10;
			int y = 350;
			boolean drawn100 = false;
			iFPSTex.draw(x, y);
			x += iFPSTex.getWidth();
			
			int index = iFPSDisplay / 100;
			if (index>9) index = 9;
			if (index>0)
			{
				iNumerTex[index].draw(x, y);
				drawn100 = true;
				x += iNumerTex[index].getWidth();
			}
			
			index = (iFPSDisplay % 100)/10;
			if (drawn100 || index>0)
			{
				iNumerTex[index].draw(x, y);
				x += iNumerTex[index].getWidth();
			}
			
			index = iFPSDisplay % 10;
			iNumerTex[index].draw(x, y);
		}
	}
}
