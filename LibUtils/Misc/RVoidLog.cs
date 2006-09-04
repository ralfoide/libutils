//*******************************************************************
/*

	Solution:	LibUtils
	Project:	LibUtils
	File:		RVoidLog.cs

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

//*************************************
namespace Alfray.LibUtils.Misc
{
	//***************************************************
	/// <summary>
	/// A dummy RILog that does nothing.
	/// </summary>
	//***************************************************
	public class RVoidLog: RILog
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

		
		//***************
		/// <summary>
		/// Default constructor. Does nothing.
		/// </summary>
		//***************
		public RVoidLog()
		{
		}

		#region RILog Members

		//***********************
		/// <summary>
		/// Logs a string. The dummy log drops the string.
		/// </summary>
		//***********************
		public void Log(string s)
		{
		}

		//***********************
		/// <summary>
		/// Logs an object as string. The dummy log drops the string.
		/// </summary>
		//***********************
		public void Log(object o)
		{
		}

		#endregion


		//-------------------------------------------
		//----------- Private Methods ---------------
		//-------------------------------------------


		//-------------------------------------------
		//----------- Private Attributes ------------
		//-------------------------------------------

	} // class RVoidLog
} // namespace Alfray.LibUtils.Misc


//---------------------------------------------------------------
//	[C# Template RM 20040516]
//	$Log: RVoidLog.cs,v $
//	Revision 1.1.1.1  2005/04/28 21:33:48  ralf
//	Moved AppSkeleton.Utils in a separate LibUtils project
//	
//	Revision 1.3  2005/04/27 01:12:01  ralf
//	Updated Utils with files from Xeres
//	
//	Revision 1.2  2005/03/20 19:48:40  ralf
//	Added GPL headers.
//	
//	Revision 1.1.1.1  2005/02/18 22:54:53  ralf
//	A skeleton application template, with NUnit testing
//	
//---------------------------------------------------------------
