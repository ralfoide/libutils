//*******************************************************************
/*

	Solution:	AppSkeleton
	Project:	ClientApp
	File:		RMainForm.cs

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
	/// Summary description for RMainForm.
	/// </summary>
	public class RMainForm : System.Windows.Forms.Form, RILog
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

		
		//****************
		public RMainForm()
		{
			//
			// Required for Windows Form Designer support
			//
			InitializeComponent();

			//
			// Add any constructor code after InitializeComponent call
			//

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
			if (mDebugForm == null)
				createDebugWindow(false);
			if (mDebugForm != null)
				mDebugForm.Log(s);
		}

		#endregion


		//***********************
		public void ReloadPrefs()
		{
			reloadPrefs();
		}



		//-------------------------------------------
		//----------- Private Methods ---------------
		//-------------------------------------------


		//*****************
		private void init()
		{
			// load all settings
			loadSettings();

			// apply defaults
			reloadPrefs();
		}


		//**********************
		private void terminate()
		{
			// close windows
			closePrefWindow();
			closeDebugWindow();

			// save settings
			saveSettings();
		}


		//*************************
		/// <summary>
		/// Loads settings specific to this window.
		/// Done only once when the window is created.
		/// </summary>
		//*************************
		private void loadSettings()
		{
			// load settings
			RMainModule.Pref.Load();

			// tell Windows not to change this position automatically
			this.StartPosition = FormStartPosition.Manual;

			// load position of this window
			Rectangle r;
			if (RMainModule.Pref.GetRect(RPrefConstants.kMainForm, out r))
			{
				// RM 20050307 No longer change the size of the window.
				// This is because the window cannot be resized manually,
				// instead it adapts to the size of the inner video preview.
				this.Location = r.Location;
			}

			// <insert other setting stuff here>

		}


		//*************************
		private void saveSettings()
		{
			// save position & size of this window
			RMainModule.Pref.SetRect(RPrefConstants.kMainForm, this.Bounds);

			// save settings
			RMainModule.Pref.Save();
		}


		//************************
		/// <summary>
		/// (Re)Loads app-wide preferences.
		/// Done anytime the user applies changes to the preference window
		/// or once at startup.
		/// </summary>
		//************************
		private void reloadPrefs()
		{
			// updateButtons();

			Log("Prefs reloaded");
		}


		//******************************************
		private void createDebugWindow(bool visible)
		{
			if (mDebugForm == null)
			{
				mDebugForm = new RDebugForm();
				mDebugForm.Show();
			}
		}


		//******************************
		private void closeDebugWindow()
		{
			if (mDebugForm != null)
			{
				mDebugForm.CanClose = true;
				mDebugForm.Close();
				mDebugForm = null;
			}
		}


		//********************************
		private void showHideDebugWindow()
		{
			if (mDebugForm == null)
				createDebugWindow(true);
			else
				mDebugForm.Visible = !mDebugForm.Visible;
		}


		//******************************************
		private void createPrefWindow(bool visible)
		{
			if (mPrefForm == null)
			{
				mPrefForm = new RPrefForm();
				mPrefForm.Show();
			}
		}


		//******************************
		private void closePrefWindow()
		{
			if (mPrefForm != null)
			{
				mPrefForm.CanClose = true;
				mPrefForm.Close();
				mPrefForm = null;
			}
		}


		//********************************
		private void showHidePrefWindow()
		{
			if (mPrefForm == null)
				createPrefWindow(true);
			else
				mPrefForm.Visible = !mPrefForm.Visible;
		}


		//**************************
		private void updateButtons()
		{
		}


		//-------------------------------------------
		//----------- Private Callbacks -------------
		//-------------------------------------------


		//******************************************************************
		private void mMenuItemQuit_Click(object sender, System.EventArgs e)
		{
			this.Close();
		}

		//******************************************************************
		private void mMenuItemDebug_Click(object sender, System.EventArgs e)
		{
			showHideDebugWindow();
		}


		//******************************************************************
		private void RMainForm_Closing(object sender, System.ComponentModel.CancelEventArgs e)
		{
			terminate();
		}

		//******************************************************************
		private void mMenuItemPreferences_Click(object sender, System.EventArgs e)
		{
			showHidePrefWindow();
		}
		
		//-------------------------------------------
		//----------- Private WinForms --------------
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
			this.mMenuMain = new System.Windows.Forms.MainMenu();
			this.menuItem1 = new System.Windows.Forms.MenuItem();
			this.mMenuItemConnect = new System.Windows.Forms.MenuItem();
			this.mMenuItemDisconnect = new System.Windows.Forms.MenuItem();
			this.menuItem4 = new System.Windows.Forms.MenuItem();
			this.mMenuItemQuit = new System.Windows.Forms.MenuItem();
			this.mMenuHelp = new System.Windows.Forms.MenuItem();
			this.mMenuItemUpdate = new System.Windows.Forms.MenuItem();
			this.mMenuItemDebug = new System.Windows.Forms.MenuItem();
			this.menuItem10 = new System.Windows.Forms.MenuItem();
			this.mMenuItemAbout = new System.Windows.Forms.MenuItem();
			this.mStatusBar = new System.Windows.Forms.StatusBar();
			this.mMenuItemPreferences = new System.Windows.Forms.MenuItem();
			this.menuItem3 = new System.Windows.Forms.MenuItem();
			this.SuspendLayout();
			// 
			// mMenuMain
			// 
			this.mMenuMain.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
																					  this.menuItem1,
																					  this.mMenuHelp});
			// 
			// menuItem1
			// 
			this.menuItem1.Index = 0;
			this.menuItem1.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
																					  this.mMenuItemConnect,
																					  this.mMenuItemDisconnect,
																					  this.menuItem3,
																					  this.mMenuItemPreferences,
																					  this.menuItem4,
																					  this.mMenuItemQuit});
			this.menuItem1.Text = "File";
			// 
			// mMenuItemConnect
			// 
			this.mMenuItemConnect.Index = 0;
			this.mMenuItemConnect.Text = "Item 1";
			// 
			// mMenuItemDisconnect
			// 
			this.mMenuItemDisconnect.Index = 1;
			this.mMenuItemDisconnect.Text = "Item 2";
			// 
			// menuItem4
			// 
			this.menuItem4.Index = 4;
			this.menuItem4.Text = "-";
			// 
			// mMenuItemQuit
			// 
			this.mMenuItemQuit.Index = 5;
			this.mMenuItemQuit.Text = "Quit";
			this.mMenuItemQuit.Click += new System.EventHandler(this.mMenuItemQuit_Click);
			// 
			// mMenuHelp
			// 
			this.mMenuHelp.Index = 1;
			this.mMenuHelp.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
																					  this.mMenuItemUpdate,
																					  this.mMenuItemDebug,
																					  this.menuItem10,
																					  this.mMenuItemAbout});
			this.mMenuHelp.Text = "Help";
			// 
			// mMenuItemUpdate
			// 
			this.mMenuItemUpdate.Index = 0;
			this.mMenuItemUpdate.Text = "Update...";
			// 
			// mMenuItemDebug
			// 
			this.mMenuItemDebug.Index = 1;
			this.mMenuItemDebug.Text = "Debug";
			this.mMenuItemDebug.Click += new System.EventHandler(this.mMenuItemDebug_Click);
			// 
			// menuItem10
			// 
			this.menuItem10.Index = 2;
			this.menuItem10.Text = "-";
			// 
			// mMenuItemAbout
			// 
			this.mMenuItemAbout.Index = 3;
			this.mMenuItemAbout.Text = "About...";
			// 
			// mStatusBar
			// 
			this.mStatusBar.ImeMode = System.Windows.Forms.ImeMode.NoControl;
			this.mStatusBar.Location = new System.Drawing.Point(0, 219);
			this.mStatusBar.Name = "mStatusBar";
			this.mStatusBar.Size = new System.Drawing.Size(424, 22);
			this.mStatusBar.TabIndex = 0;
			// 
			// mMenuItemPreferences
			// 
			this.mMenuItemPreferences.Index = 3;
			this.mMenuItemPreferences.Text = "Preferences...";
			this.mMenuItemPreferences.Click += new System.EventHandler(this.mMenuItemPreferences_Click);
			// 
			// menuItem3
			// 
			this.menuItem3.Index = 2;
			this.menuItem3.Text = "-";
			// 
			// RMainForm
			// 
			this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
			this.ClientSize = new System.Drawing.Size(424, 241);
			this.Controls.Add(this.mStatusBar);
			this.Menu = this.mMenuMain;
			this.Name = "RMainForm";
			this.Text = "RMainForm";
			this.Closing += new System.ComponentModel.CancelEventHandler(this.RMainForm_Closing);
			this.ResumeLayout(false);

		}

		#endregion

		private System.ComponentModel.IContainer components;

		private System.Windows.Forms.MainMenu mMenuMain;
		private System.Windows.Forms.MenuItem menuItem1;
		private System.Windows.Forms.MenuItem menuItem4;
		private System.Windows.Forms.MenuItem menuItem10;
		private System.Windows.Forms.StatusBar mStatusBar;
		private System.Windows.Forms.MenuItem mMenuItemConnect;
		private System.Windows.Forms.MenuItem mMenuItemDisconnect;
		private System.Windows.Forms.MenuItem mMenuItemQuit;
		private System.Windows.Forms.MenuItem mMenuItemUpdate;
		private System.Windows.Forms.MenuItem mMenuItemDebug;
		private System.Windows.Forms.MenuItem mMenuItemAbout;
		private System.Windows.Forms.MenuItem mMenuHelp;


		//-------------------------------------------
		//----------- Private Attributes ------------
		//-------------------------------------------

		// forms
		private RDebugForm		mDebugForm;
		private System.Windows.Forms.MenuItem mMenuItemPreferences;
		private System.Windows.Forms.MenuItem menuItem3;
		private RPrefForm		mPrefForm;


	} // class RMainForm
} // namespace Alfray.AppSkeletonNs.ClientApp


//---------------------------------------------------------------
//	[C# Template RM 20040516]
//	$Log: RMainForm.cs,v $
//	Revision 1.4  2005/05/23 02:13:57  ralf
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

