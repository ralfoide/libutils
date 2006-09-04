//*******************************************************************
/*

	Solution:	LibUtils
	Project:	LibUtilsTests
	File:		RTestFreqCounter.cs

	Copyright 2005, Raphael MOLL.

	This file is part of LibUtils.

	LibUtils is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.

	LibUtils is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with LibUtils; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

*/
//*******************************************************************



using System;
using System.Threading;

using NUnit.Framework;

using Alfray.LibUtils.Misc;

//*************************************
namespace Alfray.LibUtils.Tests.Misc
{
	//***************************************************
	/// <summary>
	/// Summary description for RTestFreqCounter.
	/// </summary>
	//***************************************************
	[TestFixture]
	public class RTestFreqCounter
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
			t = new RFreqCounter();
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
			Assert.AreEqual(0, t.Count);
			Assert.AreEqual(0.0, t.AvgPerSec);
		}

		//****************
		[Test]
		public void TestCount()
		{
			Assert.IsNotNull(t);

			Assert.AreEqual(0, t.Count);

			t.Count += 50;
			Assert.AreEqual(50, t.Count);

			// Substracting count values is not recommended
			// yet it should work nonetheless
			t.Count -= 10;
			Assert.AreEqual(40, t.Count);
		}

		//****************
		[Test]
		public void TestAvgPerSec()
		{
			Assert.IsNotNull(t);

			Assert.AreEqual(0, t.Count);
			Assert.AreEqual(0.0, t.AvgPerSec);

			// We can't wait in a quick nunit test executed on the fly.
			// Since the average is per seconds, we would have to
			// wait several seconds to get a reasonable test. So I just
			// skip it here. If this doesn't work it will be obvious in 
			// the UI.
			// (Another approach is to write a test, try it once and
			// comment it for later usage only in case of issue... let's 
			// do this here)

#if LONGTESTS

			t.Count += 4000;

			// Wait 2 seconds and a tiny bit more
			Thread.Sleep(2200); // 2.2 s in micro seconds

			t.Count += 6000;

			// Wait 2 seconds and a tiny bit more -- again
			Thread.Sleep(2200); // 2.2 s in micro seconds

			// 6000 bytes averaged over 2 seconds.
			// The count is done on an integer number of seconds
			// so it should be 2 seconds, not 2.2
			// Notes:
			// 1- The count is *after* the first time the time is set
			//    so the 4000 above should not be counted in.
			// 2- The average must be computed when the Count is
			//    incremented, *not* when the average is requested so
			//    in this case it should be after 2 secondes, not 4.
			Assert.AreEqual(6000 / 2, t.AvgPerSec);

#endif // LONGTESTS

		}

		//-------------------------------------------
		//----------- Private Methods ---------------
		//-------------------------------------------


		//-------------------------------------------
		//----------- Private Attributes ------------
		//-------------------------------------------

		private RFreqCounter t;

	} // class RTestFreqCounter
} // namespace Alfray.LibUtils.Tests.Misc


//---------------------------------------------------------------
//	[C# Template RM 20040516]
//	$Log: RTestFreqCounter.cs,v $
//	Revision 1.1  2005/07/22 14:51:13  ralf
//	Reorganizes LibUtilsTests in subdirs.
//	Added RUtil.AspectRatio.
//	
//	Revision 1.2  2005/05/30 20:44:35  ralf
//	Using uniform variable "t" for tested object
//	
//	Revision 1.1.1.1  2005/04/28 21:33:48  ralf
//	Moved AppSkeleton.Utils in a separate LibUtils project
//	
//	Revision 1.1  2005/04/27 01:12:01  ralf
//	Updated Utils with files from Xeres
//	
//	Revision 1.1  2005/03/28 00:24:29  ralf
//	New tests
//	
//---------------------------------------------------------------
