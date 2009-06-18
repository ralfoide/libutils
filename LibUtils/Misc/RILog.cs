//*******************************************************************
/*

	Solution:	LibUtils
	Project:	LibUtils
	File:		RILog.cs

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
	//****************************************************
	/// <summary>
	/// RILog is an interface for logging strings.
	/// See RVoidLog for a dummy implementation.
	/// </summary>
	//****************************************************
	public interface RILog
	{
		// ------- public properties ------

		// ------- public methods --------

		
		//*****************
		/// <summary>
		/// Add a string to the log.
		/// A new line will be added if the string doesn't end
		/// with one.
		/// </summary>
		//*****************
		void Log(string s);


		//*****************
		/// <summary>
		/// Logs an object by calling its ToString() method.
		/// A new line will be added if the string doesn't end
		/// with one.
		/// </summary>
		//*****************
		void Log(object o);

		// ------- private methods ------- 

		// ------- private properties ------- 
	} // class RILog
} // namespace Alfray.LibUtils.Misc
