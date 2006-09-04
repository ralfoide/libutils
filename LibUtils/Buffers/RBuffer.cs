//*******************************************************************
/*

	Solution:	LibUtils
	Project:	LibUtils
	File:		RBuffer.cs

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
	/// RBuffer is a base class for representing a binary
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
	public class RBuffer: RIBuffer
	{
		//-------------------------------------------
		//----------- Public Constants --------------
		//-------------------------------------------

		//*************
		/// <summary>
		/// Predetermined keys that occur frequently in the buffer meta data
		/// </summary>
		//*************
		public enum Key
		{
			/// <summary>
			/// The meta data for kBounds is a Rectangle object
			/// </summary>
			kBounds = 1,

			/// <summary>
			/// The meta data for kTimeStamp is a DateTime object
			/// </summary>
			kTimeStamp,

			/// <summary>
			/// The meta data for kBitmap is a Bitmap object.
			/// Mime-type should be set to ".net/bitmap"
			/// </summary>
			kBitmap,

			/// <summary>
			/// The meta data for kMimeType is a string which has a mime-type
			/// like syntax (i.e. category/subtype). When possible existing mime type
			/// strings are used.
			/// 
			/// The possible following values are:
			/// - raw : undefined bunch of bytes. Used mostly for testing.
			/// - image/jpeg : a jpeg file is present in the binary data.
			/// - rgba/32 : an RGBA 32-bpp binary block. Endian-dependant.
			/// - .net/bitmap: a .Net Bitmap object is present in the kBitmap meta-data.
			/// </summary>
			kMimeType
		}


		//-------------------------------------------
		//----------- Public Properties -------------
		//-------------------------------------------


		//****************
		/// <summary>
		/// The underlying binary data storage.
		/// </summary>
		//****************
		public byte[] Data
		{
			get
			{
				return mData;
			}
		}


		//***********************
		/// <summary>
		/// The metadata describing this buffer.
		/// </summary>
		//***********************
		public Hashtable Metadata
		{
			get
			{
				return mMeta;
			}
		}


		//-------------------------------------------
		//----------- Public Methods ----------------
		//-------------------------------------------

		
		//**************************
		/// <summary>
		/// Constructs a new buffer with no data and
		/// no metadata.
		/// </summary>
		//**************************
		public RBuffer(): this(null)
		{
		}

		//*************************
		/// <summary>
		/// Constructs a new buffer with the given
		/// data attached and no meta data.
		/// </summary>
		/// <param name="data">The data to "attach" to this buffer.
		/// The buffer does not duplicate the data.</param>
		//*************************
		public RBuffer(byte[] data)
		{
			mMeta = new Hashtable();
			mData = data;
		}



		//-------------------------------------------
		//----------- Private Methods ---------------
		//-------------------------------------------


		//-------------------------------------------
		//----------- Private Attributes ------------
		//-------------------------------------------


		private byte[]		mData;
		private Hashtable	mMeta;


	} // class RBuffer
} // namespace Alfray.LibUtils.Buffers


//---------------------------------------------------------------
//	[C# Template RM 20040516]
//	$Log: RBuffer.cs,v $
//	Revision 1.1.1.1  2005/04/28 21:33:48  ralf
//	Moved AppSkeleton.Utils in a separate LibUtils project
//	
//	Revision 1.1  2005/04/27 01:12:01  ralf
//	Updated Utils with files from Xeres
//	
//	Revision 1.1  2005/03/23 06:30:04  ralf
//	Moved RBuffer from Device to Utils. Using RIBuffer.
//	
//	Revision 1.5  2005/03/21 07:17:49  ralf
//	Adding inline doc comments for all classes and public methods
//	
//	Revision 1.4  2005/03/10 22:10:20  ralf
//	Quick test with JPEG encoding
//	
//	Revision 1.3  2005/03/09 20:02:39  ralf
//	Added Format to RBufferVideo
//	Added FromBitmap to RBufferVideo
//	
//	Revision 1.2  2005/02/28 01:15:49  ralf
//	Added RBufferVideo
//	
//	Revision 1.1  2005/02/22 06:11:21  ralf
//	Added Device.RBuffer
//	
//---------------------------------------------------------------
