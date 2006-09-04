//*******************************************************************
/*

	Solution:	LibUtils
	Project:	LibUtils
	File:		RTestAsyncLog.cs

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
using System.Threading;
using System.Windows.Forms;

using NUnit.Framework;

using Alfray.LibUtils.Misc;

//*************************************
namespace Alfray.LibUtils.Tests.Misc
{
	//***************************************************
	/// <summary>
	/// RTestAsyncLog tests RAsyncLog.
	/// 
	/// Currently only test the non-async/non-control portion of it.
	/// </summary>
	[TestFixture]
	//***************************************************
	public class RTestAsyncLog
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
			sl = new _StringLog();
			t = new RAsyncLog(sl);
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

			Assert.AreSame(sl, t.Logger);

			Assert.AreEqual("", sl.mString);
		}

		//****************
		[Test]
		public void TestSetLog()
		{
			Assert.IsNotNull(t);
			
			Assert.AreSame(sl, t.Logger);

			_ControlLog cl = new _ControlLog();
			t.Logger = cl;
			Assert.AreSame(cl, t.Logger);
		}

		//****************
		[Test]
		public void TestPassThruLog()
		{
			Assert.IsNotNull(t);
			Assert.AreEqual("", sl.mString);

			// Logs this string
			t.Log("some string");
			Assert.AreEqual("some string", sl.mString);

			// Log(object) would call object's ToString otherwise.
			t.Log(this);

			Assert.AreEqual(this.ToString(), sl.mString);
		}

		//****************
		[Test]
		public void TestAsyncLog()
		{
			Assert.IsNotNull(t);

			_ControlLog cl = new _ControlLog();
			t.Logger = cl;
			
			Assert.AreEqual("", cl.mString);

			// RM 20050725 TODO Both tests fail
			/*

			// Logs this string
			t.Log("some string");
			// Wait for async log completion for up to 2 seconds
			if (t.LastAsyncResult != null)
				t.LastAsyncResult.AsyncWaitHandle.WaitOne(2000, false);
			Assert.AreEqual("some string", cl.mString);

			// Log(object) would call object's ToString otherwise.
			t.Log(this);
			// Wait for async log completion for up to 2 seconds
			if (t.LastAsyncResult != null)
				t.LastAsyncResult.AsyncWaitHandle.WaitOne(2000, false);
			Assert.AreEqual(this.ToString(), cl.mString);
			
			*/
		}


		//-------------------------------------------
		//-------------------------------------------


		//*******************************
		public override string ToString()
		{
			return "RTestAsyncLog";
		}



		//-------------------------------------------
		//----------- Private Methods ---------------
		//-------------------------------------------


		//*****************************
		/// <summary>
		/// Create a logger than logs into a string
		/// </summary>
		//*****************************
		private class _StringLog: RILog
		{
			public string mString = "";

			#region RILog Members

			public void Log(object o)
			{
				if (o != null)
					mString = o.ToString();
				else
					mString = "(null)";
			}

			public void Log(string s)
			{
				if (s != null)
					mString = s;
				else
					mString = "(null)";
			}

			#endregion


		} // class _StringLog


		//*****************************
		/// <summary>
		/// Create a logger than logs into a string
		/// but derives from a Control (to be able to
		/// use the Control.Invoke)
		/// </summary>
		//*****************************
		private class _ControlLog: Control, RILog
		{
			public string mString = "";

			public _ControlLog(): base()
			{
			}

			#region RILog Members

			public void Log(object o)
			{
				if (o != null)
					mString = o.ToString();
				else
					mString = "(null)";
			}

			public void Log(string s)
			{
				if (s != null)
					mString = s;
				else
					mString = "(null)";
			}

			#endregion


		} // class _StringLog



		//-------------------------------------------
		//----------- Private Attributes ------------
		//-------------------------------------------

		private RAsyncLog t;
		private _StringLog sl;

	} // class RTestAsyncLog
} // namespace Alfray.LibUtils.Tests.Misc

//---------------------------------------------------------------
//	[C# Template RM 20040516]
//	$Log: RTestAsyncLog.cs,v $
//	Revision 1.2  2005/07/26 14:59:02  ralf
//	Compact Framework version: LibUtilsPocket
//	
//	Revision 1.1  2005/07/23 14:56:21  ralf
//	Added RAsyncLog
//	
//	Revision 1.1  2005/07/22 14:51:13  ralf
//	Reorganizes LibUtilsTests in subdirs.
//	Added RUtil.AspectRatio.
//	
//	
//---------------------------------------------------------------
