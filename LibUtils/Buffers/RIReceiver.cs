//*******************************************************************
/*

	Solution:	LibUtils
	Project:	LibUtils
	File:		RIReceiver.cs

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
namespace Alfray.LibUtils.Buffers
{
	//***************************************************
	/// <summary>
	/// The RIReceiver interface describes the counterpart
	/// to RISender, that is an entity that can receive
	/// notifications when a sender has new buffers available.
	/// 
	/// See full comments in RISender's class description.
	/// 
	/// RIReceiver derived classes do not actually "receive"
	/// buffers. They are only notified when a sender
	/// produces new buffers. The implementation here is
	/// responsible for grabbing the buffers when appropriate
	/// with the only guaranteed rule that at least one buffer
	/// will be made available.
	/// Implementations should also be careful in locking the
	/// sender's queue when dequeuing buffers.
	/// 
	/// Here are various strategies to implement the callback:
	/// - The receiver can immediately access the sender's queue
	///   (lock it, get the buffer and release the lock) and then
	///   immediately process the buffer. This is straightforward 
	///   to implement but has the inconvenience of blocking
	///   the sender. Also beware that this executes in the 
	///   working thread of the sender.
	/// - The receiver can dequeue buffers from the sender 
	///   and enqueue them in its own private queue for later
	///   asynchronous processing. This assumes the receiver has
	///   its own worker thread which repeatedly pools its
	///   internal queue or an internal asynchronous timer.
	/// - The receiver can simply set a signal. In its worker
	///   thread the receiver can then lock on signal. When the
	///   signal is set, the worker thread unsets it, dequeues
	///   buffers from the sender's queue and process them till
	///   the queue is exhausted. The sender queue should only be
	///   locked in the minimal amount of time it takes to dequeue
	///   one buffer.
	/// </summary>
	//***************************************************
	public interface RIReceiver
	{
		//-------------------------------------------
		//----------- Public Properties -------------
		//-------------------------------------------


		//-------------------------------------------
		//----------- Public Methods ----------------
		//-------------------------------------------

		//*************************************
		/// <summary>
		/// OnBufferAvailable will be called by an RISender
		/// when at least one new buffer is available.
		/// 
		/// This matches the Utils.BufferAvailableCallback
		/// delegate.
		/// </summary>
		/// <param name="sender">The sender which holds the new buffer(s)</param>
		//*************************************
		void OnBufferAvailable(RISender sender);


	} // class RIReceiver
} // namespace Alfray.LibUtils.Buffers


//---------------------------------------------------------------
//	[C# Template RM 20040516]
//	$Log: RIReceiver.cs,v $
//	Revision 1.1.1.1  2005/04/28 21:33:48  ralf
//	Moved AppSkeleton.Utils in a separate LibUtils project
//	
//	Revision 1.1  2005/04/27 01:12:01  ralf
//	Updated Utils with files from Xeres
//	
//	Revision 1.2  2005/03/23 06:37:33  ralf
//	Fixed typos in comments
//	
//	Revision 1.1  2005/03/23 06:30:40  ralf
//	New RISender, RIReceiver and RSenderBase.
//	
//---------------------------------------------------------------
