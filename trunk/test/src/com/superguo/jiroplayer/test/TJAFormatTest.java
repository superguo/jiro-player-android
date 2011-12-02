package com.superguo.jiroplayer.test;

import com.superguo.jiroplayer.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Intent;
import android.content.res.AssetManager;
import android.test.*;
import android.test.suitebuilder.annotation.MediumTest;
import android.util.Log;

public class TJAFormatTest extends ActivityUnitTestCase<TestActivity> {
	private Intent iStartIntent; 
	private TJAFormatParser iParser;
	
	public TJAFormatTest() throws IOException {
		super(TestActivity.class);

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		iStartIntent = new Intent(Intent.ACTION_MAIN);  
		iParser = new TJAFormatParser();
	}
	
	@MediumTest
	public void testTJAFormat1() throws IOException
	{
		TestActivity ac = startActivity(iStartIntent, null, null);
		InputStream is = ac.getAssets().open("sample.tja", AssetManager.ACCESS_RANDOM);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		TJAFormat tjaFormat = TJAFormat.fromReader(iParser, br);
		br = null;
		is.close();
		assertNotNull(tjaFormat.iCourses);
		assertEquals(4, tjaFormat.iCourses.length);
		assertEquals("sample.ogg", tjaFormat.iWave);
	}

	@MediumTest
	public void testTJAFormat2() throws IOException
	{
		TestActivity ac = startActivity(iStartIntent, null, null);
		InputStream is = ac.getAssets().open("sample2.tja", AssetManager.ACCESS_RANDOM);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		TJAFormat tjaFormat = TJAFormat.fromReader(iParser, br);
		br = null;
		is.close();
		com.superguo.jiroplayer.TJAFormat.TJACourse course;
		assertEquals("sample2.ogg", tjaFormat.iWave);
		assertEquals("Fill it a Try", tjaFormat.iTitle);
		assertNotNull(tjaFormat.iCourses);
		assertEquals(4, tjaFormat.iCourses.length);
		
		course = tjaFormat.iCourses[0];
		assertEquals(0, course.iCourse);
		assertEquals(130.0f, course.iBPM, 0.001f);
		
		course = tjaFormat.iCourses[1];
		assertEquals(1, course.iCourse);
		assertEquals(130.0f, course.iBPM, 0.001f);
		
		course = tjaFormat.iCourses[2];
		assertEquals(2, course.iCourse);
		assertEquals(130.0f, course.iBPM, 0.001f);
		
		course = tjaFormat.iCourses[3];
		assertEquals(3, course.iCourse);
		assertEquals(130.0f, course.iBPM, 0.001f);
		assertEquals(8, course.iLevel);
		assertNotNull(course.iBalloon);
		assertEquals(1, course.iBalloon.length);
		assertEquals(18, course.iBalloon[0]);
		assertTrue(course.iHasBranch);
	}
}
