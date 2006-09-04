//*******************************************************************
/*

	Solution:	LibUtils
	Project:	LibUtils
	File:		RAsyncLog.cs

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
using System.Windows.Forms;

//*************************************
namespace Alfray.LibUtils.Misc
{
	//***************************************************
	/// <summary>
	/// RAsyncLog is a pass-thru RILog object.
	/// It defers all log strings to the logger object specified
	/// in the constructor.
	/// If the logger object is a WinForm's Control instance,
	/// it uses Control.Invoke to make sure the logger is
	/// called in the context of its native thread.
	/// 
	/// Use RAsyncLog when you want to log operations from a
	/// work thread, as those cannot directly access the UI.
	/// </summary>
	//***************************************************
	public class RAsyncLog: RILog
	{
		//-------------------------------------------
		//----------- Public Constants --------------
		//-------------------------------------------


		//-------------------------------------------
		//----------- Public Properties -------------
		//-------------------------------------------



		//*****************
		/// <summary>
		/// Gets or sets the target of the async logger
		/// </summary>
		//*****************
		public RILog Logger
		{
			get
			{
				return mLogger;
			}
			set
			{
				mLogger = value;

				if (mLogger != null && mLogger is Control)
					mControl = mLogger as Control;
				else
					mControl = null;
			}
		}


		//*********************************
		/// <summary>
		/// If the last Log() operation was done using an
		/// asynchronous BeginInvoke, this property will
		/// return the corresponding IAsyncResult.
		/// 
		/// This is null if a Log() operation was done
		/// using a direct log member call.
		/// 
		/// By using LastAsyncResult.AsyncWaitHandle.WaitOne()
		/// you can wait for completion.
		/// </summary>
		//*********************************
		public IAsyncResult LastAsyncResult
		{
			get
			{
				return mAsyncResult;
			}
		}


		//-------------------------------------------
		//----------- Public Methods ----------------
		//-------------------------------------------

		
		//***************
		/// <summary>
		/// Default constructor. Does nothing.
		/// </summary>
		//***************
		public RAsyncLog(RILog logger)
		{
			Logger = logger;
		}

		#region RILog Members

		//***********************
		/// <summary>
		/// Logs a string via the target logger.
		/// If that logger is a WinForm Control, uses Control.Invoke
		/// to access the logger in its native control thread context.
		/// </summary>
		//***********************
		public void Log(string s)
		{
			mAsyncResult = null;

			if (mControl != null && mControl.Handle != IntPtr.Zero && mControl.InvokeRequired)
				mAsyncResult = mControl.BeginInvoke(new logStringDelegate(mLogger.Log), new object[] { s });
			else if (mLogger != null)
				mLogger.Log(s);
		}

		//***********************
		/// <summary>
		/// Logs an object as a string via the target logger.
		/// If that logger is a WinForm Control, uses Control.Invoke
		/// to access the logger in its native control thread context.
		/// </summary>
		//***********************
		public void Log(object o)
		{
			mAsyncResult = null;

			if (mControl != null && mControl.Handle != IntPtr.Zero && mControl.InvokeRequired)
				mAsyncResult = mControl.BeginInvoke(new logObjectDelegate(mLogger.Log), new object[] { o });
			else if (mLogger != null)
				mLogger.Log(o);
		}

		#endregion


		//-------------------------------------------
		//----------- Private Methods ---------------
		//-------------------------------------------


		//-------------------------------------------
		//----------- Private Types -----------------
		//-------------------------------------------

		private delegate void logStringDelegate(string s);
		private delegate void logObjectDelegate(object o);

		//-------------------------------------------
		//----------- Private Attributes ------------
		//-------------------------------------------

		private RILog			mLogger		 = null;
		private Control			mControl	 = null;
		private IAsyncResult	mAsyncResult = null;


	} // class RAsyncLog
} // namespace Alfray.LibUtils.Misc


//---------------------------------------------------------------
//	[C# Template RM 20040516]
//	$Log: RAsyncLog.cs,v $
//	Revision 1.2  2005/07/26 14:59:02  ralf
//	Compact Framework version: LibUtilsPocket
//	
//	Revision 1.1  2005/07/23 14:56:21  ralf
//	Added RAsyncLog
//	
//---------------------------------------------------------------
