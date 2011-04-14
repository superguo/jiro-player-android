package com.superguo.jiroplayer.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.superguo.jiroplayer.*;

import android.content.Intent;
import android.content.res.AssetManager;
import android.test.*;
import android.test.suitebuilder.annotation.MediumTest;
import android.util.Log;

public class TJAFormatTest extends ActivityUnitTestCase<TestActivity> {
	private Intent iStartIntent;  
	
	public TJAFormatTest() throws IOException {
		super(TestActivity.class);

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		iStartIntent = new Intent(Intent.ACTION_MAIN);  
	}
	
	@MediumTest
	public void testTJAFormat1() throws IOException
	{
		TestActivity ac = startActivity(iStartIntent, null, null);
		InputStream is = ac.getAssets().open("sample.tja", AssetManager.ACCESS_RANDOM);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		TJAFormat tjaFormat = new TJAFormat(br);
		Log.d("jiro-test", "wave=" + tjaFormat.iWave);
		br = null;
		is.close();
		assertNotNull(tjaFormat.iCourses);
		assertEquals(4, tjaFormat.iCourses.length);
	}

	@MediumTest
	public void testTJAFormat2() throws IOException
	{
		TestActivity ac = startActivity(iStartIntent, null, null);
		InputStream is = ac.getAssets().open("sample2.tja", AssetManager.ACCESS_RANDOM);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		TJAFormat tjaFormat = new TJAFormat(br);
		Log.d("jiro-test", "wave=" + tjaFormat.iWave);
		br = null;
		is.close();
		assertNotNull(tjaFormat.iCourses);
		assertEquals(4, tjaFormat.iCourses.length);
	}
}
