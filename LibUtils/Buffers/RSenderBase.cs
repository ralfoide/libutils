//*******************************************************************
/*

	Solution:	LibUtils
	Project:	LibUtils
	File:		RSenderBase.cs

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
using System.Collections;

//*************************************
namespace Alfray.LibUtils.Buffers
{
	//***************************************************
	/// <summary>
	/// RSenderBase is a default base implementation of
	/// RISender. It provides a default implementation
	/// that has a buffer queue, can add a new buffer,
	/// can remove the first available buffer, can purge
	/// old buffers (when adding a new one) and performs
	/// the event dispatch.
	/// </summary>
	//***************************************************
	public class RSenderBase: RISender
	{
		//-------------------------------------------
		//----------- Public Constants --------------
		//-------------------------------------------


		//-------------------------------------------
		//----------- Public Events -----------------
		//-------------------------------------------

		
		#region RISender Events

		//************
		/// <summary>
		/// RIReceivers should add their buffer available callback
		/// to this event to be notified of new buffers being
		/// available.
		/// </summary>
		//************
		public event BufferAvailableCallback BufferAvailableEvent;

		#endregion

		//-------------------------------------------
		//----------- Public Properties -------------
		//-------------------------------------------

		#region RISender Members

		//************
		/// <summary>
		/// Returns the buffer queue. 
		/// Callers should lock on the SyncRoot.
		/// </summary>
		//************
		public System.Collections.Queue BufferQueue
		{
			get
			{
				return mQueue;
			}
		}

		#endregion

		//-------------------------------------------
		//----------- Public Methods ----------------
		//-------------------------------------------

		
		//******************
		/// <summary>
		/// Creates a new RSenderBase without a queue limit
		/// </summary>
		//******************
		public RSenderBase()
		{
			mQueueMaxLen = 0;
			mQueue = new Queue();
		}


		
		//*********************************
		/// <summary>
		/// Creates a new RSenderBase with a queue limit
		/// </summary>
		//*********************************
		public RSenderBase(int queueMaxLen): this()
		{
			mQueueMaxLen = queueMaxLen;
		}



		//-------------------------------------------
		//----------- Protected Methods -------------
		//-------------------------------------------


		//***************************************
		/// <summary>
		/// Add a buffer to the internal queue.
		/// 
		/// If there's a max number of buffers allowed in the
		/// queue, automatically delete as many buffers as 
		/// necessary *before* adding the new one to make sure
		/// the queue does not contain more buffers than allowed.
		/// 
		/// If notifications callback have been set via
		/// BufferAvailableEvent, send the notification.
		/// 
		/// The queue is being locked while buffers are deleted
		/// or added, but not while the notifications are sent.
		/// 
		/// This method is protected so that only derived
		/// classes can add buffers to this sender.
		/// </summary>
		/// <param name="buffer">The buffer to add</param>
		//***************************************
		protected void AddBuffer(RIBuffer buffer)
		{
			lock (mQueue.SyncRoot)
			{
				// drop old buffers
				if (mQueueMaxLen > 0)
					while(mQueue.Count >= mQueueMaxLen)
						mQueue.Dequeue();

				// enqueue it
				mQueue.Enqueue(buffer);
			}

			// send notification
			if (BufferAvailableEvent != null)
				BufferAvailableEvent(this);
		}


		//-------------------------------------------
		//----------- Private Methods ---------------
		//-------------------------------------------


		//-------------------------------------------
		//----------- Private Attributes ------------
		//-------------------------------------------

		private Queue	mQueue		 = null;
		private int		mQueueMaxLen = 0;

	} // class RSenderBase
} // namespace Alfray.LibUtils.Buffers


//---------------------------------------------------------------
//	[C# Template RM 20040516]
//	$Log: RSenderBase.cs,v $
//	Revision 1.1.1.1  2005/04/28 21:33:48  ralf
//	Moved AppSkeleton.Utils in a separate LibUtils project
//	
//	Revision 1.1  2005/04/27 01:12:01  ralf
//	Updated Utils with files from Xeres
//	
//	Revision 1.1  2005/03/23 06:30:40  ralf
//	New RISender, RIReceiver and RSenderBase.
//	
//---------------------------------------------------------------
