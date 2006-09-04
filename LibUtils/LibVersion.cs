//*******************************************************************
/*

	Solution:	LibUtils
	Project:	LibUtils
	File:		LibVersion.cs

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
using System.Reflection;

//*************************************
namespace Alfray.LibUtils
{
	//***************************************************
	/// <summary>
	/// Provides a simple way to get the version of this library.
	/// </summary>
	//***************************************************
	public class LibVersion
	{
		//-------------------------------------------
		//----------- Public Constants --------------
		//-------------------------------------------


		//-------------------------------------------
		//----------- Public Properties -------------
		//-------------------------------------------


		//***************************
		/// <summary>
		/// This static method returns a copy of the version
		/// information of this assembly, which is to be interpreted
		/// as the version of this specific class library.
		/// </summary>
		//***************************
		public static Version Version
		{
			get
			{
				return Assembly.GetExecutingAssembly().GetName().Version.Clone() as Version;
			}
		}


		//-------------------------------------------
		//----------- Public Methods ----------------
		//-------------------------------------------

		

		//-------------------------------------------
		//----------- Private Methods ---------------
		//-------------------------------------------


		//-------------------------------------------
		//----------- Private Attributes ------------
		//-------------------------------------------

	} // class LibVersion
} // namespace Alfray.LibUtils


//---------------------------------------------------------------
//	[C# Template RM 20040516]
//	$Log: LibVersion.cs,v $
//	Revision 1.1.1.1  2005/04/28 21:33:48  ralf
//	Moved AppSkeleton.Utils in a separate LibUtils project
//	
//---------------------------------------------------------------
