//*******************************************************************
/*

	Solution:	LibUtils
	Project:	LibUtils
	File:		RIBuffer.cs

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
	/// RIBuffer is an interface for representing a binary
	/// data buffer with associated meta data.
	/// 
	/// No specific semantic or usage pattern is associated
	/// with the binary data block or the metadata.
	/// Anybody can modify this data as needed.
	/// It is up to the caller to control side effects
	/// resulting of its usage, including but not limited 
	/// to exclusive vs. concurrent usage.
	/// </summary>
	//***************************************************
	public interface RIBuffer
	{
		//-------------------------------------------
		//----------- Public Constants --------------
		//-------------------------------------------


		//-------------------------------------------
		//----------- Public Properties -------------
		//-------------------------------------------


		//************
		/// <summary>
		/// The underlying binary data storage.
		/// </summary>
		//************
		byte[] Data
		{
			get;
		}


		//************
		/// <summary>
		/// The metadata describing this buffer.
		/// </summary>
		//************
		Hashtable Metadata
		{
			get;
		}


		//-------------------------------------------
		//----------- Public Methods ----------------
		//-------------------------------------------

		

	} // class RIBuffer
} // namespace Alfray.LibUtils.Buffers


//---------------------------------------------------------------
//	[C# Template RM 20040516]
//	$Log: RIBuffer.cs,v $
//	Revision 1.1.1.1  2005/04/28 21:33:48  ralf
//	Moved AppSkeleton.Utils in a separate LibUtils project
//	
//	Revision 1.1  2005/04/27 01:12:01  ralf
//	Updated Utils with files from Xeres
//	
//	Revision 1.1  2005/03/23 06:29:38  ralf
//	New RIBuffer
//	
//---------------------------------------------------------------
