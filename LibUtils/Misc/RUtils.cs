//*******************************************************************
/*
 *	Project:	Alfray.LibUtils.Misc
 * 
 *	File:		RUtils.cs
 * 
 *	RM (c) 2005
 * 
 */
//*******************************************************************

using System;
using System.Drawing;

//*************************************
namespace Alfray.LibUtils.Misc
{
	//****************************************************
	/// <summary>
	/// Some miscellaneous utilities that do no fit anywhere else.
	/// This class only contains static methods and can never
	/// be instantiated.
	/// </summary>
	//****************************************************
	public class RUtils
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

		//*******************************************
		/// <summary>
		/// Computes aspect ratio to fit size "sz" into "target" pixels
		/// </summary>
		//*******************************************
		public static Size AspectRatio(Size sz, int target)
		{
			// Code extracted from rig/rig/thumbnail/rig_thumbnail.cpp > rig_resize_image

			int wsrc = sz.Width;
			int hsrc = sz.Height;
			int wdst = wsrc;
			int hdst = hsrc;

			double aspect = (double)wsrc / (double)hsrc;

			if (wsrc >= hsrc && wdst != target)
			{
				wdst = target;
				hdst = (int)((double)target / aspect);
			}
			else if (hsrc > wsrc && hdst != target)
			{
				hdst = target;
				wdst = (int)((double)target * aspect);
			}

			return new Size(wdst, hdst);
		}


		//-------------------------------------------
		//----------- Private Methods ---------------
		//-------------------------------------------

		//**************
		/// <summary>
		/// Private constructor. This class can never
		/// be instantiated.
		/// </summary>
		//**************
		private RUtils()
		{
		}

		//-------------------------------------------
		//----------- Private Attributes ------------
		//-------------------------------------------

	} // class RUtils
} // namespace Alfray.LibUtils.Misc

//*******************************************************************
/*
 *	$Log: RUtils.cs,v $
 *	Revision 1.1  2005/07/22 14:51:13  ralf
 *	Reorganizes LibUtilsTests in subdirs.
 *	Added RUtil.AspectRatio.
 *	
 * 
 */
//*******************************************************************

