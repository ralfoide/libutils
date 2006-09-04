//*******************************************************************
/*

	Solution:	LibUtils
	Project:	LibUtils
	File:		RTestVoidLog.cs

	Copyright 2005, Raphael MOLL.

	This file is part of Rivet.

	Rivet is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.

	Rivet is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with Rivet; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

*/
//*******************************************************************

using System;

using NUnit.Framework;

using Alfray.LibUtils.Misc;

//*************************************
namespace Alfray.LibUtils.Tests.Misc
{
	//***************************************************
	/// <summary>
	/// RTestVoidLog tests RVoidLog.
	/// </summary>
	[TestFixture]
	//***************************************************
	public class RTestVoidLog
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
			t = new RVoidLog();
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
		}

		//****************
		[Test]
		public void TestLog()
		{
			Assert.IsNotNull(t);

			// This call does nothing -- no log is written
			t.Log("some string");

			// This call does nothing -- no log is written
			// Log(object) would call object's ToString otherwise.
			t.Log(this);
		}


		//-------------------------------------------
		//-------------------------------------------


		//*******************************
		public override string ToString()
		{
			return "RTestVoidLog";
		}



		//-------------------------------------------
		//----------- Private Methods ---------------
		//-------------------------------------------


		//-------------------------------------------
		//----------- Private Attributes ------------
		//-------------------------------------------

		private RVoidLog t;

	} // class RTestVoidLog
} // namespace Alfray.LibUtils.Tests.Misc

//---------------------------------------------------------------
//	[C# Template RM 20040516]
//	$Log: RTestVoidLog.cs,v $
//	Revision 1.1  2005/07/23 14:56:21  ralf
//	Added RAsyncLog
//	
//	Revision 1.1  2005/07/22 14:51:13  ralf
//	Reorganizes LibUtilsTests in subdirs.
//	Added RUtil.AspectRatio.
//	
//	
//---------------------------------------------------------------
