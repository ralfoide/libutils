//*******************************************************************
/*

	Solution:	LibUtils
	Project:	XeresLib
	File:		RFreqCounter.cs

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
	/// RFreqCounter is an helper class to compute frequency
	/// stat info such as frame rate.
	/// Simply increment the counter each time an event happens
	/// and get the number of events per seconds (average since
	/// beginning or last reset.)
	/// </summary>
	//***************************************************
	public class RFreqCounter
	{
		//-------------------------------------------
		//----------- Public Constants --------------
		//-------------------------------------------


		//-------------------------------------------
		//----------- Public Properties -------------
		//-------------------------------------------

		//**************************
		/// <summary>
		/// Number of events that happened.
		/// This value should typically only be incremented.
		/// </summary>
		//**************************
		public long Count
		{
			get
			{
				return mCount;
			}
			set
			{
				mCount = value;
				mLastTime = DateTime.Now;

				// Remember we'll need to update the average but only
				// do it when it is actually requested
				mNeedUpdate = true;

				if (mStartTime.Ticks == 0)
				{
					// First time, initialize to Now
					mStartTime = mLastTime;
					mStartCount = mCount;

					// DEBUG
					// System.Diagnostics.Debug.WriteLine("Reset tick = " + mStartTime.Ticks.ToString());
				}

			}
		}


		//**************************
		/// <summary>
		/// Returns the average per second.
		/// </summary>
		//**************************
		public double AvgPerSec
		{
			get
			{
				updateAverage();
				return mAvgPerSec;
			}
		}

		//-------------------------------------------
		//----------- Public Methods ----------------
		//-------------------------------------------

		
		//*******************
		/// <summary>
		/// Initializes a new counter.
		/// Event count is initialized to zero.
		/// Average per seconds is zero at first (i.e. N/A)
		/// </summary>
		//*******************
		public RFreqCounter()
		{
		}


		//-------------------------------------------
		//----------- Private Methods ---------------
		//-------------------------------------------

		//**************************
		private void updateAverage()
		{
			if (mNeedUpdate && mStartTime.Ticks > 0)
			{				// Get the time difference
				TimeSpan ts = mLastTime - mStartTime;
				long tts = (long)(ts.TotalSeconds);

				// Check we have valid start/last times
				// i.e. it must be positive non-null
				if (tts > 0)
				{
					long nb = mCount - mStartCount;
					mAvgPerSec = (double)(nb) / (double)(tts);

					// DEBUG
					System.Diagnostics.Debug.WriteLine(String.Format("sec {0} - bytes {1} - freq {2:#.##}",
						tts, nb, mAvgPerSec));

					// reset update flag only if average was really computed
					mNeedUpdate = false;
				}
			}
		}


		//-------------------------------------------
		//----------- Private Attributes ------------
		//-------------------------------------------

		private bool		mNeedUpdate = true;
		private double		mAvgPerSec= 0.0;
		private long		mCount = 0;
		private long		mStartCount = 0;
		private DateTime	mStartTime = new DateTime(0);
		private DateTime	mLastTime  = new DateTime(0);


	} // class RFreqCounter
} // namespace Alfray.LibUtils.Misc


//---------------------------------------------------------------
//	[C# Template RM 20040516]
//	$Log: RFreqCounter.cs,v $
//	Revision 1.1.1.1  2005/04/28 21:33:48  ralf
//	Moved AppSkeleton.Utils in a separate LibUtils project
//	
//	Revision 1.1  2005/04/27 01:12:01  ralf
//	Updated Utils with files from Xeres
//	
//	Revision 1.1  2005/03/28 00:24:42  ralf
//	Added RFreqCounter
//	
//---------------------------------------------------------------
