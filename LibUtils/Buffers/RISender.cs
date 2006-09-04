//*******************************************************************
/*

	Solution:	LibUtils
	Project:	LibUtils
	File:		RISender.cs

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
	
	//************
	/// <summary>
	/// A callback implemented by RIReceiver to be notified
	/// when a new buffer is made available.
	/// </summary>
	//************
	public delegate void BufferAvailableCallback(RISender sender);


	//***************************************************
	/// <summary>
	/// The RISender interface describes an entity that
	/// can produce data in the form of RBuffer instances.
	/// 
	/// The sender doesn't actually "send" any data.
	/// Instead it provides a buffer queue and can send
	/// a notification to am RIReceiver when new data is
	/// available. The receiver will be responsible for
	/// extracting the data from the queue.
	/// 
	/// The only mandatory rule here is that once a receiver
	/// received a notification, it must be able to fetch at
	/// least one data buffer from the sender queue.
	/// 
	/// The sender does not need to have as many queued
	/// data buffer as notifications sent. The notification
	/// merely indicate that at least one data buffer is
	/// available.
	/// This way, a receiver may choose to either pool
	/// the sender's queue repeatedly, or on the other
	/// hand it may choose to drop notification events
	/// if it doesn't need data, or to just keep a note
	/// that some data can be fetched at a later time.
	/// 
	/// Consequently, depending on the nature of the queue
	/// being managed, it may be up to the sender to remove
	/// obsolete queued buffers that have not be used yet.
	/// </summary>
	//***************************************************
	public interface RISender
	{
		//-------------------------------------------
		//----------- Public Events -----------------
		//-------------------------------------------

		//************
		/// <summary>
		/// RIReceivers should add their buffer available callback
		/// to this event to be notified of new buffers being
		/// available.
		/// </summary>
		//************
		event BufferAvailableCallback BufferAvailableEvent;

		//-------------------------------------------
		//----------- Public Properties -------------
		//-------------------------------------------


		//************
		/// <summary>
		/// Returns the buffer queue. 
		/// Callers should lock on the SyncRoot.
		/// </summary>
		//************
		Queue BufferQueue
		{
			get;
		}


		//-------------------------------------------
		//----------- Public Methods ----------------
		//-------------------------------------------



	} // class RISender
} // interface Alfray.LibUtils.Buffers


//---------------------------------------------------------------
//	[C# Template RM 20040516]
//	$Log: RISender.cs,v $
//	Revision 1.1.1.1  2005/04/28 21:33:48  ralf
//	Moved AppSkeleton.Utils in a separate LibUtils project
//	
//	Revision 1.1  2005/04/27 01:12:01  ralf
//	Updated Utils with files from Xeres
//	
//	Revision 1.2  2005/03/23 06:39:36  ralf
//	Fixed typos in comments
//	
//	Revision 1.1  2005/03/23 06:30:40  ralf
//	New RISender, RIReceiver and RSenderBase.
//	
//---------------------------------------------------------------
