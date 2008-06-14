//*******************************************************************
/*

	Solution:	AppSkeleton
	Project:	ClientApp
	File:		RDebugForm.cs

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
using System.Collections;
using System.ComponentModel;
using System.Windows.Forms;

using Alfray.LibUtils.Misc;

//*********************************
namespace Alfray.AppSkeletonNs.ClientApp
{
	//**************************************
	/// <summary>
	/// Summary description for RDebugForm.
	/// </summary>
	public class RDebugForm: System.Windows.Forms.Form, RILog
	{
		//-------------------------------------------
		//----------- Public Constants --------------
		//-------------------------------------------


		//-------------------------------------------
		//----------- Public Properties -------------
		//-------------------------------------------


		//******************
		public bool CanClose
		{
			get
			{
				return mCanClose;
			}
			set
			{
				mCanClose = value;
			}
		}



		//-------------------------------------------
		//----------- Public Methods ----------------
		//-------------------------------------------

		
		//****************
		public RDebugForm()
		{
			// Required for Windows Form Designer support

			InitializeComponent();

			// Inits

			init();
		}


		#region RILog Members

		//***********************
		public void Log(object o)
		{
			Log(o.ToString());
		}

		//***********************
		public void Log(string s)
		{
			System.Diagnostics.Debug.WriteLine(s);

			if (!s.EndsWith("\r\n"))
				s += "\r\n";

			mTextLog.Text += s;

			mTextLog.Select(mTextLog.TextLength, 0);
			mTextLog.ScrollToCaret();
			// mTextLog.Refresh();
		}

		#endregion


		//-------------------------------------------
		//----------- Private Methods ---------------
		//-------------------------------------------


		//*****************
		private void init()
		{
			mTextLog.Text = "";
			mCanClose = false;

			// load all settings
			loadSettings();
		}


		//**********************
		private void terminate()
		{
			// save settings
			saveSettings();
		}


		//*************************
		private void loadSettings()
		{
			// load position & size of this window
			Rectangle r;
			if (RMainModule.Pref.GetRect(RPrefConstants.kDebugForm, out r))
			{
				this.Bounds = r;
				// tell Windows not to change this position
				this.StartPosition = FormStartPosition.Manual;
			}
		}


		//*************************
		private void saveSettings()
		{
			// save position & size of this window
			RMainModule.Pref.SetRect(RPrefConstants.kDebugForm, this.Bounds);

			// save settings
			RMainModule.Pref.Save();
		}


		//-------------------------------------------
		//----------- Private Callbacks -------------
		//-------------------------------------------

		//****************************************************************
		private void mButtonClear_Click(object sender, System.EventArgs e)
		{
			mTextLog.Clear();
		}


		//****************************************************************
		private void RDebugForm_Closing(object sender, System.ComponentModel.CancelEventArgs e)
		{
			if (!mCanClose)
			{
				// Simply hide it
				e.Cancel = true;
				this.Visible = false;
			}
			else
			{
				terminate();
			}
		}


		//-------------------------------------------
		//----------- Private Methods ---------------
		//-------------------------------------------

		//***********************************
		/// <summary>
		/// Clean up any resources being used.
		/// </summary>
		protected override void Dispose( bool disposing )
		{
			if( disposing )
			{
				if(components != null)
				{
					components.Dispose();
				}
			}
			base.Dispose( disposing );
		}


		#region Windows Form Designer generated code

		//********************************
		/// <summary>
		/// Required method for Designer support - do not modify
		/// the contents of this method with the code editor.
		/// </summary>
		private void InitializeComponent()
		{
			this.mTextLog = new System.Windows.Forms.TextBox();
			this.mButtonClear = new System.Windows.Forms.Button();
			this.SuspendLayout();
			// 
			// mTextLog
			// 
			this.mTextLog.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
				| System.Windows.Forms.AnchorStyles.Left) 
				| System.Windows.Forms.AnchorStyles.Right)));
			this.mTextLog.Location = new System.Drawing.Point(8, 8);
			this.mTextLog.Multiline = true;
			this.mTextLog.Name = "mTextLog";
			this.mTextLog.ScrollBars = System.Windows.Forms.ScrollBars.Both;
			this.mTextLog.Size = new System.Drawing.Size(416, 248);
			this.mTextLog.TabIndex = 0;
			this.mTextLog.Text = "mTextLog";
			// 
			// mButtonClear
			// 
			this.mButtonClear.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
			this.mButtonClear.Location = new System.Drawing.Point(352, 264);
			this.mButtonClear.Name = "mButtonClear";
			this.mButtonClear.TabIndex = 1;
			this.mButtonClear.Text = "Clear";
			this.mButtonClear.Click += new System.EventHandler(this.mButtonClear_Click);
			// 
			// RDebugForm
			// 
			this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
			this.ClientSize = new System.Drawing.Size(432, 294);
			this.Controls.Add(this.mButtonClear);
			this.Controls.Add(this.mTextLog);
			this.MinimumSize = new System.Drawing.Size(272, 160);
			this.Name = "RDebugForm";
			this.Text = "RDebugForm";
			this.Closing += new System.ComponentModel.CancelEventHandler(this.RDebugForm_Closing);
			this.ResumeLayout(false);

		}

		#endregion

		//-------------------------------------------
		//----------- Private Attributes ------------
		//-------------------------------------------

		private System.ComponentModel.Container components = null;
		private System.Windows.Forms.TextBox mTextLog;
		private System.Windows.Forms.Button mButtonClear;


		//-------------------------------------------
		//----------- Private Attributes ------------
		//-------------------------------------------


		private bool	mCanClose;


	} // class RDebugForm
} // namespace Alfray.AppSkeletonNs.ClientApp


//---------------------------------------------------------------
//	[C# Template RM 20040516]
//	$Log: RDebugForm.cs,v $
//	Revision 1.4  2005/05/23 02:13:56  ralf
//	Added pref window skeleton.
//	Added load/save window settings for pref & debug windows.
//	
//	Revision 1.3  2005/04/28 21:31:14  ralf
//	Using new LibUtils project
//	
//	Revision 1.2  2005/03/20 19:48:39  ralf
//	Added GPL headers.
//	
//	Revision 1.1  2005/02/18 23:21:52  ralf
//	Creating both an App and a Class Lib
//	
//	Revision 1.1.1.1  2005/02/18 22:54:53  ralf
//	A skeleton application template, with NUnit testing
//	
//---------------------------------------------------------------

