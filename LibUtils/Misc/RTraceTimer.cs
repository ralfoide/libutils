//*******************************************************************
/*

	Solution:	LibUtils
	Project:	LibUtils
	File:		RTraceTimer.cs

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
	/// RTraceTimer creates a simple timer utility for
	/// measure performance. Create an instance to start
	/// timing, then repeatedly call SetPoint(),
	/// CheckPoint() and finally EndTotal().
	/// </summary>
	//***************************************************
	public class RTraceTimer
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

		
		//****************************************
		/// <summary>
		/// Creates a new instance of the timer
		/// with the start date set to now.
		/// </summary>
		/// <param name="log">Output log object</param>
		/// <param name="name">Name displayed in output</param>
		//****************************************
		public RTraceTimer(RILog log, string name)
		{
			mLog = log;
			mName = name;
			mStart = DateTime.Now;
			mPoint = mStart;
		}


		//********************
		/// <summary>
		/// Force the next check point comparison time.
		/// </summary>
		//********************
		public void SetPoint()
		{
			mPoint = DateTime.Now;
		}


		//********************************
		/// <summary>
		/// Display the time elapsed since the instance
		/// creation (start time) and the last check point
		/// time.
		/// </summary>
		/// <param name="msg">Name displayed in output</param>
		//********************************
		public void CheckPoint(string msg)
		{
			DateTime now = DateTime.Now;
			TimeSpan from_start = mStart - now;
			TimeSpan from_point = mPoint - now;

			mLog.Log(String.Format("[RT: {0}/{1}] - Last: {2} - Total: {3}",
				mName, msg, from_point, from_start));

			SetPoint();
		}


		//********************
		/// <summary>
		/// Display the time elapsed between when the
		/// instance was created and now.
		/// </summary>
		//********************
		public void EndTotal()
		{
			TimeSpan from_start = mStart - DateTime.Now;

			mLog.Log(String.Format("[RT: {0}/End] - Total: {1}",
				mName, from_start));
		}


		//-------------------------------------------
		//----------- Private Methods ---------------
		//-------------------------------------------


		//-------------------------------------------
		//----------- Private Attributes ------------
		//-------------------------------------------


		private DateTime	mStart;
		private DateTime	mPoint;

		private string		mName;
		private RILog		mLog;


	} // class RTraceTimer
} // namespace Alfray.LibUtils.Misc


//---------------------------------------------------------------
//	[C# Template RM 20040516]
//	$Log: RTraceTimer.cs,v $
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
