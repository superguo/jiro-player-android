package com.superguo.jiroplayer;

import com.superguo.ogl2d.O2Texture;
import com.superguo.ogl2d.O2TextureManager;

public class FPSHolder {
	private static int 	 iFPSCount;
	private static int 	 iFPSDisplay;
	private static long	 iLastFPSRecTime;
	private static O2Texture iFPSTex;
	
	public static void dispose()
	{
		if (iFPSTex!=null)
		{
			iFPSTex.dispose();
			iFPSTex = null;
		}
	}
	
	public static void showFPS()
	{
		iFPSCount++;

		long FPSRecTime = android.os.SystemClock.uptimeMillis();
		if (FPSRecTime - iLastFPSRecTime > 1000)
		{
			if (iFPSTex!=null)
			{
				iFPSTex.dispose();
				iFPSTex = null;
			}
			iFPSDisplay = iFPSCount;
			iFPSCount = 0;
			iLastFPSRecTime = FPSRecTime;
		}
		
		if (iFPSDisplay>0)
		{
			iFPSTex = O2TextureManager.getInstance().createFromString("FPS: " + iFPSDisplay, false);
			iFPSTex.draw(10, 350);
		}
	}
}
