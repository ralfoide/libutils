//*******************************************************************
/*

	Solution:	AppSkeleton
	Project:	ClientApp
	File:		RPrefForm.cs

	Copyright 2003, 2004, Raphael MOLL.

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


//*********************************
namespace Alfray.AppSkeletonNs.ClientApp
{

	//**************************************
	/// <summary>
	/// Summary description for RPrefForm.
	/// </summary>
	public class RPrefForm : System.Windows.Forms.Form
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
		public RPrefForm()
		{
			//
			// Required for Windows Form Designer support
			//
			InitializeComponent();

			// Inits

			init();
		}




		//-------------------------------------------
		//----------- Private Methods ---------------
		//-------------------------------------------



		//*****************
		private void init()
		{
			mCanClose = false;

			// load all settings
			loadSettings();

			// init ...

			// check UI

			mNeedApply = false;
			validateUi();
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
			if (RMainModule.Pref.GetRect(RPrefConstants.kPrefForm, out r))
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
			RMainModule.Pref.SetRect(RPrefConstants.kPrefForm, this.Bounds);

			// save settings
			RMainModule.Pref.Save();
		}


		//***********************
		private void validateUi()
		{
			mButtonApply.Enabled = mNeedApply;
		}


		//******************
		private void apply()
		{
			// update prefs

			// ...
		
			// tell main form to reload prefs

			RMainModule.MainForm.ReloadPrefs();

			mNeedApply = false;
			validateUi();
		}


		//-------------------------------------------
		//----------- Private Callback ---------------
		//-------------------------------------------


		//****************************************************************
		private void RPrefForm_Closing(object sender, System.ComponentModel.CancelEventArgs e)
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

		//****************************************************************
		private void mButtonApply_Click(object sender, System.EventArgs e)
		{
			apply();
		}

		//****************************************************************
		private void mButtonOk_Click(object sender, System.EventArgs e)
		{
			apply();
			this.Close();
		}

		//****************************************************************
		private void mButtonCancel_Click(object sender, System.EventArgs e)
		{
			this.Close();
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
			this.mButtonApply = new System.Windows.Forms.Button();
			this.mButtonOk = new System.Windows.Forms.Button();
			this.mButtonCancel = new System.Windows.Forms.Button();
			this.SuspendLayout();
			// 
			// mButtonApply
			// 
			this.mButtonApply.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
			this.mButtonApply.ImeMode = System.Windows.Forms.ImeMode.NoControl;
			this.mButtonApply.Location = new System.Drawing.Point(32, 140);
			this.mButtonApply.Name = "mButtonApply";
			this.mButtonApply.TabIndex = 6;
			this.mButtonApply.Text = "Apply";
			this.mButtonApply.Click += new System.EventHandler(this.mButtonApply_Click);
			// 
			// mButtonOk
			// 
			this.mButtonOk.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
			this.mButtonOk.ImeMode = System.Windows.Forms.ImeMode.NoControl;
			this.mButtonOk.Location = new System.Drawing.Point(120, 140);
			this.mButtonOk.Name = "mButtonOk";
			this.mButtonOk.TabIndex = 5;
			this.mButtonOk.Text = "OK";
			this.mButtonOk.Click += new System.EventHandler(this.mButtonOk_Click);
			// 
			// mButtonCancel
			// 
			this.mButtonCancel.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
			this.mButtonCancel.DialogResult = System.Windows.Forms.DialogResult.Cancel;
			this.mButtonCancel.ImeMode = System.Windows.Forms.ImeMode.NoControl;
			this.mButtonCancel.Location = new System.Drawing.Point(208, 140);
			this.mButtonCancel.Name = "mButtonCancel";
			this.mButtonCancel.TabIndex = 4;
			this.mButtonCancel.Text = "Cancel";
			this.mButtonCancel.Click += new System.EventHandler(this.mButtonCancel_Click);
			// 
			// RPrefForm
			// 
			this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
			this.ClientSize = new System.Drawing.Size(292, 174);
			this.Controls.Add(this.mButtonApply);
			this.Controls.Add(this.mButtonOk);
			this.Controls.Add(this.mButtonCancel);
			this.MinimumSize = new System.Drawing.Size(300, 208);
			this.Name = "RPrefForm";
			this.Text = "Preferences";
			this.Closing += new System.ComponentModel.CancelEventHandler(this.RPrefForm_Closing);
			this.ResumeLayout(false);

		}

		#endregion

		private System.Windows.Forms.Button mButtonApply;
		private System.Windows.Forms.Button mButtonOk;
		private System.Windows.Forms.Button mButtonCancel;



		//*****************************
		/// <summary>
		/// Required designer variable.
		/// </summary>
		private System.ComponentModel.Container components = null;

		

		//-------------------------------------------
		//----------- Private Attributes ------------
		//-------------------------------------------

		private bool	mCanClose;
		private bool	mNeedApply;




	} // class RPrefForm
} // namespace Alfray.AppSkeletonNs.ClientApp


//---------------------------------------------------------------
//	[C# Template RM 20040516]
//	$Log: RPrefForm.cs,v $
//	Revision 1.1  2005/05/23 02:13:57  ralf
//	Added pref window skeleton.
//	Added load/save window settings for pref & debug windows.
//	
//---------------------------------------------------------------

