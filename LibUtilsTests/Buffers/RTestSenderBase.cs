//*******************************************************************
/*

	Solution:	LibUtils
	Project:	LibUtilsTests
	File:		RTestSenderBase.cs

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

using NUnit.Framework;

using Alfray.LibUtils.Buffers;

//***************************************
namespace Alfray.LibUtils.Tests
{
	//***************************************************
	/// <summary>
	/// Tests RSenderBase.
	/// 
	/// Derived from RSenderBase in order to be able to access
	/// the protected method AddBuffer().
	/// </summary>
	//***************************************************
	[TestFixture]
	public class RTestSenderBase: RSenderBase
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


		//******************************
		public RTestSenderBase(): base()
		{	
		}

		//*************************************
		public RTestSenderBase(int queueMaxLen): base(queueMaxLen)
		{	
		}

		
		//****************
		[SetUp]
		public void SetUp()
		{
			t = new RSenderBase();
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
			
			Assert.IsNotNull(t.BufferQueue);
			Assert.AreEqual(0, t.BufferQueue.Count);
		}

		//****************
		[Test]
		public void TestAddBuffer()
		{
			// AddBuffer is protected so instead of instanciating
			// RSenderBase here we instanciate this test class
			// to be able to access the protected member.

			RTestSenderBase t = new RTestSenderBase();

			Assert.IsNotNull(t);
			
			// queue should be empty
			Assert.AreEqual(0, t.BufferQueue.Count);

			// set an event callback
			t.BufferAvailableEvent += new BufferAvailableCallback(callback);

			// create a buffer
			byte [] data = new byte[5]{ 0, 1, 42, 3, 42 };
			RBuffer b = new RBuffer(data);

			// check no buffer has been received yet
			Assert.IsNull(mLastBuffer);

			// add the buffer (will call the callback)
			t.AddBuffer(b);

			// check we got the buffer
			Assert.IsNotNull(mLastBuffer);
			
			// check it's the same buffer
			Assert.AreSame(b, mLastBuffer);

			// queue should be empty
			Assert.AreEqual(0, t.BufferQueue.Count);
		}


		//****************
		[Test]
		public void TestQueueLen()
		{
			// AddBuffer is protected so instead of instanciating
			// RSenderBase here we instanciate this test class
			// to be able to access the protected member.

			// create a sender that hold 3 buffers max
			RTestSenderBase t = new RTestSenderBase(3);

			// queue should be empty
			Assert.AreEqual(0, t.BufferQueue.Count);

			// create buffers
			byte [] data = new byte[5]{ 0, 1, 42, 3, 42 };
	
			t.AddBuffer(new RBuffer(data));
			Assert.AreEqual(1, t.BufferQueue.Count);

			t.AddBuffer(new RBuffer(data));
			Assert.AreEqual(2, t.BufferQueue.Count);

			t.AddBuffer(new RBuffer(data));
			Assert.AreEqual(3, t.BufferQueue.Count);
		
			// adding this buffer should remove the oldest one
			t.AddBuffer(new RBuffer(data));
			Assert.AreEqual(3, t.BufferQueue.Count);
		}

		//-------------------------------------------
		//----------- Private Methods ---------------
		//-------------------------------------------


		//************************************
		private void callback(RISender sender)
		{
			// lock queue... always lock the queue (it may be
			// useless here but this can also be used as a use case)
			lock(sender.BufferQueue.SyncRoot)
			{
				// check there's exactly one buffer in the queue
				Assert.AreEqual(1, sender.BufferQueue.Count);

				// get buffer from the sender
				mLastBuffer = sender.BufferQueue.Dequeue() as RIBuffer;

				// check it is not null
				Assert.IsNotNull(mLastBuffer);
			}
		}


		//-------------------------------------------
		//----------- Private Attributes ------------
		//-------------------------------------------

		private RSenderBase t;

		private RIBuffer mLastBuffer = null;

	} // class RTestSenderBase
} // namespace Alfray.LibUtils.Tests


//---------------------------------------------------------------
//	[C# Template RM 20040516]
//	$Log: RTestSenderBase.cs,v $
//	Revision 1.1  2005/10/21 04:31:05  ralf
//	Update
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
//	Revision 1.1  2005/03/23 06:31:07  ralf
//	Tests for RBuffer, RIBuffer, RISender, RIReceiver and RSenderBase.
//	
//	
//---------------------------------------------------------------
