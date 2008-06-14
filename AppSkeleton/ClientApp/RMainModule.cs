//*******************************************************************
/*

	Solution:	AppSkeleton
	Project:	ClientApp
	File:		RMainModule.cs

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
using System.Windows.Forms;

using Alfray.LibUtils.Misc;

//*************************************
namespace Alfray.AppSkeletonNs.ClientApp
{
	//***************************************************
	/// <summary>
	/// Summary description for RMainModule.
	/// </summary>
	public class RMainModule
	{
		//-------------------------------------------
		//----------- Public Constants --------------
		//-------------------------------------------


		//-------------------------------------------
		//----------- Public Properties -------------
		//-------------------------------------------


		//**********************
		public static RPref Pref
		{
			get
			{
				return mMainMod.mPref;
			}
		}


		//******************************
		public static RMainForm MainForm
		{
			get
			{
				return RMainModule.mMainForm;
			}
		}

		//-------------------------------------------
		//----------- Public Methods ----------------
		//-------------------------------------------

		
		//****************
		/// <summary>
		/// The main entry point for the application.
		/// </summary>
		//****************
		[STAThread]
		static void Main()
		{
			Application.EnableVisualStyles();

			mMainMod = new RMainModule();
			mMainForm = new RMainForm();

			Application.Run(mMainForm);
		}


		//-------------------------------------------
		//----------- Private Methods ---------------
		//-------------------------------------------


		//-------------------------------------------
		//----------- Private Attributes ------------
		//-------------------------------------------

		private static RMainForm mMainForm;
		private static RMainModule mMainMod;
		private RPref mPref = new RPref();

	} // class RMainModule
} // namespace Alfray.AppSkeletonNs.ClientApp


//---------------------------------------------------------------
//	[C# Template RM 20040516]
//	$Log: RMainModule.cs,v $
//	Revision 1.3  2005/05/23 02:13:57  ralf
//	Added pref window skeleton.
//	Added load/save window settings for pref & debug windows.
//	
//	Revision 1.2  2005/03/20 19:48:39  ralf
//	Added GPL headers.
//	
//	Revision 1.1  2005/02/18 23:21:52  ralf
//	Creating both an App and a Class Lib
//	
//	Revision 1.1.1.1  2005/02/18 22:54:53  ralf
//	A skeleton application template, with NUnit testing
//	
//---------------------------------------------------------------
