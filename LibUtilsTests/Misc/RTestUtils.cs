//*******************************************************************
/*

	Solution:	LibUtils
	Project:	LibUtils
	File:		RTestUtils.cs

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
using System.Drawing;

using NUnit.Framework;

using Alfray.LibUtils.Misc;

//*************************************
namespace Alfray.LibUtils.Tests.Misc
{
	//***************************************************
	/// <summary>
	/// RTestUtils tests RUtils.
	/// </summary>
	[TestFixture]
	//***************************************************
	public class RTestUtils
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
		[Test]
		public void TestAspectRatio()
		{
			Assert.AreEqual(new Size( 10,  20), RUtils.AspectRatio(new Size( 100,  200),  20));
			Assert.AreEqual(new Size( 96,  72), RUtils.AspectRatio(new Size(1600, 1200),  96));
			Assert.AreEqual(new Size(800, 600), RUtils.AspectRatio(new Size(1024,  768), 800));
		}



		//-------------------------------------------
		//----------- Private Methods ---------------
		//-------------------------------------------


		//-------------------------------------------
		//----------- Private Attributes ------------
		//-------------------------------------------

	} // class RTestUtils
} // namespace Alfray.LibUtils.Tests.Misc

//---------------------------------------------------------------
//	[C# Template RM 20040516]
//	$Log: RTestUtils.cs,v $
//	Revision 1.1  2005/07/22 14:51:13  ralf
//	Reorganizes LibUtilsTests in subdirs.
//	Added RUtil.AspectRatio.
//	
//	
//---------------------------------------------------------------
