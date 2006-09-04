//*******************************************************************
/*

	Solution:	LibUtils
	Project:	LibUtilsTests
	File:		RTestPref.cs

	Copyright 2005, Raphael MOLL.

	This file is part of AppSkeleton.

	AppSkeleton is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.

	AppSkeleton is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with AppSkeleton; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

*/
//*******************************************************************


using System;
using System.Drawing;
using System.Collections;

using NUnit.Framework;
using Alfray.LibUtils.Misc;

//*************************************
namespace Alfray.LibUtils.Tests.Misc
{
	//***************************************************
	/// <summary>
	/// Tests RPref.
	/// 
	/// Derived from RPref in order to be able to access 
	/// the private method settingFileName().
	/// </summary>
	//***************************************************
	[TestFixture]
	public class RTestPref: RPref
	{
		//-------------------------------------------
		//----------- Public Constants --------------
		//-------------------------------------------


		//-------------------------------------------
		//----------- Public Properties -------------
		//-------------------------------------------


		//-------------------------------------------
		//----------- Public Methods ----------------
		//-------------------------------------------

		
		//****************
		[SetUp]
		public void SetUp()
		{
			t = new RPref();
		}

		//********************
		[TearDown]
		public void TearDown()
		{
		}

		//****************
		[Test]
		public void TestInit()
		{
			Assert.IsNotNull(t);
			Assert.IsNotNull(t.Settings);

			// This key should not exist
			Assert.IsNull(t[kTestKey]);
		}

		//************************
		[Test]
		public void TestSetRemoveKey()
		{
			// This key should not exist
			Assert.IsNull(t[kTestKey]);

			const string kValue = "test-value-42-42";

			// add it
			t[kTestKey] = kValue;

			// it must exist now
			Assert.AreEqual(kValue, t[kTestKey]);

			// remove it
			t[kTestKey] = null;

			// This key should not exist
			Assert.IsNull(t[kTestKey]);

			// check it *really* doesn't exist anymore
			Assert.IsFalse(t.Settings.ContainsKey(kTestKey));
		}

		//**************************
		[Test]
		public void TestSettingFileName()
		{
			// Check the pref object as a filename out of the box
			// (doesn't check was it is exactly as it is app-specific)
			string s = this.settingFileName();
			Assert.IsTrue(s != null && s != "");
		}

		//************************
		[Test]
		public void TestLoad()
		{
			// Check we can load the xml
			Assert.IsTrue(t.Load());
		}

		//************************
		[Test]
		public void TestSave()
		{
			// Check we can load then save the xml
			Assert.IsTrue(t.Load());
			Assert.IsTrue(t.Save());
		}


		//************************
		[Test]
		public void TestGetSetRect()
		{
			// set a rect
			
			Rectangle r1 = new Rectangle(-42, 43, 44, -45);

			t.SetRect(kTestKey, r1);

			// check the keys exist with the correct text values
			Assert.AreEqual("-42", t["rect_"+kTestKey+"_x"]);
			Assert.AreEqual( "43", t["rect_"+kTestKey+"_y"]);
			Assert.AreEqual( "44", t["rect_"+kTestKey+"_w"]);
			Assert.AreEqual("-45", t["rect_"+kTestKey+"_h"]);

			// get the rect
			
			Rectangle r2;

			Assert.IsTrue(t.GetRect(kTestKey, out r2));
			Assert.AreEqual(r1, r2);
		}


		//************************
		[Test]
		public void TestGetSetSize()
		{
			// set a rect
			
			Size z1 = new Size(42, 43);

			t.SetSize(kTestKey, z1);

			// check the keys exist with the correct text values
			Assert.AreEqual("42", t["size_"+kTestKey+"_w"]);
			Assert.AreEqual("43", t["size_"+kTestKey+"_h"]);

			// get the size
			
			Size z2;

			Assert.IsTrue(t.GetSize(kTestKey, out z2));
			Assert.AreEqual(z1, z2);
		}


		//************************
		[Test]
		public void TestGetSetEnum()
		{
			// Create some enumerable

			ArrayList a = new ArrayList();
			a.Add("foo");	// item 0
			a.Add("bar");	// item 1
			a.Add("42");	// item 2

			// Add it to the prefs

			t.SetEnumeration(kTestKey, a);

			// Now retrieve it

			string[] str_array;

			Assert.IsTrue(t.GetEnumeration(kTestKey, out str_array));
			Assert.AreEqual(a.ToArray(), str_array);

			// Now let's try with some null elements

			a.Add(null);	// item 3
			a.Add("end");	// item 4

			// This will replace the old set by the new one

			t.SetEnumeration(kTestKey, a);

			// Now retrieve it

			Assert.IsTrue(t.GetEnumeration(kTestKey, out str_array));

			// They should not be equal since null will be an empty string now

			Assert.IsFalse(a.ToArray() == str_array);

			// Create the equivalent of the array by replacing item 3
			// (a null pointer) by an empty string and check again
			a.RemoveAt(3);
			a.Insert(3, "");

			Assert.AreEqual(a.ToArray(), str_array);
		}


		//-------------------------------------------
		//----------- Private Methods ---------------
		//-------------------------------------------




		//-------------------------------------------
		//----------- Private Attributes ------------
		//-------------------------------------------

		private const string kTestKey = "reserved-test-key";

		private RPref t;

	} // class RTestPref
} // namespace Alfray.LibUtils.Tests.Misc


//---------------------------------------------------------------
//	[C# Template RM 20040516]
//	$Log: RTestPref.cs,v $
//	Revision 1.1  2005/07/22 14:51:13  ralf
//	Reorganizes LibUtilsTests in subdirs.
//	Added RUtil.AspectRatio.
//	
//	Revision 1.3  2005/05/30 20:44:35  ralf
//	Using uniform variable "t" for tested object
//	
//	Revision 1.2  2005/05/25 03:52:22  ralf
//	Added Get/SetEnumeration in prefs
//	
//	Revision 1.1.1.1  2005/04/28 21:33:48  ralf
//	Moved AppSkeleton.Utils in a separate LibUtils project
//	
//	Revision 1.2  2005/04/27 01:12:01  ralf
//	Updated Utils with files from Xeres
//	
//	Revision 1.1  2005/03/20 19:55:22  ralf
//	Updated Utils.RPrefs
//	
//	Revision 1.3  2005/03/20 19:48:40  ralf
//	Added GPL headers.
//	
//	Revision 1.2  2005/02/21 03:35:10  ralf
//	New Get/SetRect
//	
//	Revision 1.1.1.1  2005/02/18 22:54:53  ralf
//	A skeleton application template, with NUnit testing
//	
//---------------------------------------------------------------
