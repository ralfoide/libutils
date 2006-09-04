//*******************************************************************
/*

	Solution:	LibUtils
	Project:	LibUtils
	File:		RPref.cs

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
using System.IO;
using System.Xml;
using System.Drawing;
using System.Collections;
using System.Text.RegularExpressions;


//*********************************
namespace Alfray.LibUtils.Misc
{
	//***************************************************
	/// <summary>
	/// RPref:
	/// - Maintains a dictionnary of settings.
	/// - Settings are loaded/saved in an XML File.
	/// - The file is located in the app's UserAppData, RPref.xml
	/// </summary>
	//***************************************************
	public class RPref
	{
		//-------------------------------------------
		//----------- Public Constants --------------
		//-------------------------------------------


		//-------------------------------------------
		//----------- Public Properties -------------
		//-------------------------------------------


		//*****************************
		/// <summary>
		/// Sets or gets a setting by name.
		/// All settings are stored as strings.
		/// When trying to get a setting that does not
		/// exists, null is returned.
		/// To unset a setting, affect null to it.
		/// </summary>
		//*****************************
		public string this[string name]
		{
			get
			{
				if (mSettings.ContainsKey(name))
					return (string) mSettings[name];
				else
					return null;
			}

			set
			{
				// RM 20050129 setting an existing key to NULL removes it
				if (value == null)
				{
					if (mSettings.ContainsKey(name))
						mSettings.Remove(name);
				}
				else
				{
					mSettings[name] = value;
				}
			}
		}



		//***********************
		/// <summary>
		/// Returns the setting dictionnary.
		/// </summary>
		//***********************
		public Hashtable Settings
		{
			get
			{
				return mSettings;
			}
		}


		//-------------------------------------------
		//----------- Public Methods ----------------
		//-------------------------------------------

		
		//************
		/// <summary>
		/// Creates a new RPref object with an empty dictionnary.
		/// </summary>
		//************
		public RPref()
		{
			mSettings = new Hashtable();
		}



		//****************
		/// <summary>
		/// Merge in the settings from the pref file.
		/// Note that the current dictionnary is not purged.
		/// New keys will be added or will override existing ones.
		/// The filename depends on System.Windows.Forms.Application.
		/// </summary>
		/// <returns>False if an exception occured, true if the
		/// setting file did not exits or was loaded properly.</returns>
		//****************
		public bool Load()
		{
			string f = settingFileName();

			if (!File.Exists(f))
				return true;

			try
			{
				XmlDocument doc = new XmlDocument();
				doc.Load(f);

				// not an XML?
				if (doc.DocumentElement == null)
					return true;

#if !LIBCF
				foreach (XmlNode node in doc.SelectNodes("/r-prefs/pref"))
#else
				foreach (XmlNode node in doc.GetElementsByTagName("pref"))
#endif
				{
					try
					{
#if !LIBCF
						XmlNode key_node = node.SelectSingleNode("name");
						XmlNode val_node = node.SelectSingleNode("value");
#else
						XmlNode key_node = null;
						XmlNode val_node = null;
						foreach(XmlNode n in node.ChildNodes)
							if (n.Name == "name")
								key_node = n;
							else if (n.Name == "value")
								val_node = n;
#endif

						System.Diagnostics.Debug.Assert(key_node != null && val_node != null);

						if (key_node != null && val_node != null)
						{
							string key = cleanup(key_node.InnerText.ToString());
							string val = cleanup(val_node.InnerText.ToString());

							if (key != null && val != null)
								mSettings[key] = val;
						}
					}
					catch(Exception ex)
					{
						System.Diagnostics.Debug.WriteLine(ex.Message);
					}
				}


				return true;
			}
			catch(Exception ex)
			{
				System.Diagnostics.Debug.WriteLine(ex.Message);
			}

			return false;
		}


		//****************
		/// <summary>
		/// Save the settings as strings in the default XML pref file.
		/// The filename depends on System.Windows.Forms.Application.
		/// </summary>
		/// <returns>Always true</returns>
		//****************
		public bool Save()
		{
			string f = settingFileName();

			// Check if the directory exists, and if not create it
			if (!File.Exists(f))
			{
				string dir = Path.GetDirectoryName(f);
				if (!Directory.Exists(dir))
					Directory.CreateDirectory(dir);
			}

			XmlWriter w = new XmlTextWriter(f, System.Text.Encoding.UTF8);

			w.WriteStartDocument();
			w.WriteStartElement("r-prefs");
			w.WriteAttributeString("version", "1.0");
			
			IDictionaryEnumerator de = mSettings.GetEnumerator();
			while(de.MoveNext())
			{
				w.WriteStartElement("pref");
				
				w.WriteStartElement("name");
				w.WriteString(de.Key.ToString());
				w.WriteEndElement();	// name

				w.WriteStartElement("value");
				w.WriteString(de.Value.ToString());
				w.WriteEndElement();	// value

				w.WriteEndElement();	// pref
			}

			w.WriteEndElement(); // rprefs
			w.WriteEndDocument();
			w.Close();

			return true;
		}


		//**************************************************
		/// <summary>
		/// Helper method to get a rectangle from the dictionnary.
		/// </summary>
		/// <param name="name">The settings name</param>
		/// <param name="rect">The rectangle to fill</param>
		/// <returns>True if a rectangle was found, false otherwise</returns>
		//**************************************************
		public bool GetRect(string name, out Rectangle rect)
		{
			rect = Rectangle.Empty;

			try
			{
				name = "rect_" + name + "_";

				string x = this[name + "x"];
				string y = this[name + "y"];
				string w = this[name + "w"];
				string h = this[name + "h"];

				if (x != null && y != null && w != null && h != null)
				{
					rect = new Rectangle(Convert.ToInt32(x),Convert.ToInt32(y), 
						Convert.ToInt32(w), Convert.ToInt32(h));

					return true;
				}
			}
			catch(Exception ex)
			{
				System.Diagnostics.Debug.WriteLine(ex.ToString());
			}

			return false;
		}


		//**********************************************
		/// <summary>
		/// Sets (or override) a rectangle in the settings.
		/// </summary>
		/// <param name="name">The settings name</param>
		/// <param name="rect">The rectangle to set</param>
		//**********************************************
		public void SetRect(string name, Rectangle rect)
		{
			name = "rect_" + name + "_";
			this[name + "x"] = rect.Left.ToString();
			this[name + "y"] = rect.Top.ToString();
			this[name + "w"] = rect.Width.ToString();
			this[name + "h"] = rect.Height.ToString();
		}


		//*******************************************
		/// <summary>
		/// Helper method to get a size from the dictionnary.
		/// </summary>
		/// <param name="name">The settings name</param>
		/// <param name="rect">The size to fill</param>
		/// <returns>True if a size was found, false otherwise</returns>
		//*******************************************
		public bool GetSize(string name, out Size sz)
		{
			sz = Size.Empty;

			try
			{
				name = "size_" + name + "_";

				string w = this[name + "w"];
				string h = this[name + "h"];

				if (w != null && h != null)
				{
					sz = new Size(Convert.ToInt32(w), Convert.ToInt32(h));

					return true;
				}
			}
			catch(Exception ex)
			{
				System.Diagnostics.Debug.WriteLine(ex.ToString());
			}

			return false;
		}


		//***************************************
		/// <summary>
		/// Sets (or override) a size in the settings.
		/// </summary>
		/// <param name="name">The settings name</param>
		/// <param name="rect">The size to set</param>
		//***************************************
		public void SetSize(string name, Size sz)
		{
			name = "size_" + name + "_";
			this[name + "w"] = sz.Width.ToString();
			this[name + "h"] = sz.Height.ToString();
		}


		//*******************************************
		/// <summary>
		/// Helper method to get an enumeration from the dictionnary.
		/// 
		/// All items are returned as string in an Array object.
		/// 
		/// The array contains as many strings as elements saved
		/// in the enumeration. Null references were converted to
		/// empty strings when writing and are returned as empty
		/// strings.
		/// 
		/// The returned array can be empty (zero elements)
		/// if an empty enumeration was saved.
		/// </summary>
		/// <param name="name">The settings name</param>
		/// <param name="rect">The size to fill</param>
		/// <returns>True if an enumeration was found (even empty), false otherwise</returns>
		//*******************************************
		public bool GetEnumeration(string name, out string[] output)
		{
			try
			{
				name = "enum_" + name + "_";

				// it exists if it has a count
				string s_count = this[name + "count"];
				if (s_count != null)
				{
					int n = Convert.ToInt32(s_count);

					output = new string[n];

					for(int i = 0; i < n; i++)
					{
						string s = this[name + i.ToString()];
						output[i] = (s != null ? s : "");
					}

					return true;
				}
			}
			catch(Exception ex)
			{
				System.Diagnostics.Debug.WriteLine(ex.ToString());
			}

			output = new string[]{};
			return false;
		}


		//***************************************
		/// <summary>
		/// Sets (or override) an enumeration (list, array, etc.).
		/// 
		/// This first removes any current enumeration with the
		/// same name and then writes the new one.
		/// 
		/// All items are stored as string, using object.ToString().
		/// 
		/// Null references in the enumeration are converted to 
		/// empty strings.
		/// </summary>
		/// <param name="name">The settings name</param>
		/// <param name="input">The IEnumerable to set</param>
		//***************************************
		public void SetEnumeration(string name, IEnumerable input)
		{
			// Remove any existing set first

			name = "enum_" + name + "_";

			// it exists if it has a count
			string s_count = this[name + "count"];
			if (s_count != null)
			{
				this[name + "count"] = null;

				int n = Convert.ToInt32(s_count);
				for(int i = 0; i < n; i++)
					this[name + i.ToString()] = null;
			}

			// Now add the new items

			int nb = 0;
			
			try
			{
				IEnumerator en = input.GetEnumerator();
				if (en != null)
				{
					en.Reset();
					while(en.MoveNext())
					{
						string s = "";
						if (en.Current != null)
							s = en.Current.ToString();

						this[name + nb.ToString()] = s;
						
						nb++;
					}
				}
			}
			catch(Exception ex)
			{
				System.Diagnostics.Debug.WriteLine(ex.Message);
			}

			// Add the count

			this[name + "count"] = nb.ToString();
		}


		//-------------------------------------------
		//----------- Private Methods ---------------
		//-------------------------------------------


		//********************************
		protected string settingFileName()
		{
#if !LIBCF
			// UserAppDataPath uses the version number (f.ex 1.0.42.1234)
			// so let's just keep the major.minor numbers
			string path = System.Windows.Forms.Application.UserAppDataPath;

			Regex re = new Regex("^(.*[0-9]+\\.[0-9]+)\\.[0-9]+\\.[0-9]+$");

			Match m = re.Match(path);
			if (m.Success)
			{
				CaptureCollection c1 = m.Groups[1].Captures;
				if (c1 != null && c1[0] != null)
					path = c1[0].Value;
			}


			return Path.Combine(path, kPrefFile);
#else
			return Path.Combine(Path.PathSeparator.ToString(), kPrefFile);
#endif
		}


		//**********************************
		protected string cleanup(string str)
		// cleanup any trailing or leading space, \n \r \t
		{
			if (str == null || str.Length < 1)
				return str;

			const string ws = " \n\r\t\f";

			while(str.Length > 0 && ws.IndexOf(str[0]) >= 0)
				str = str.Remove(0, 1);

			while(str.Length > 0 && ws.IndexOf(str[str.Length-1]) >= 0)
				str = str.Remove(str.Length-1, 1);

			return str;
		}



		//-------------------------------------------
		//----------- Private Attributes ------------
		//-------------------------------------------

		private const string	kPrefFile = "RPref.xml";

		private	Hashtable		mSettings;


	} // class RPref
} // namespace Alfray.LibUtils.Misc


//---------------------------------------------------------------
//	[C# Template RM 20041110]
//	$Log: RPref.cs,v $
//	Revision 1.3  2005/07/26 14:59:02  ralf
//	Compact Framework version: LibUtilsPocket
//	
//	Revision 1.2  2005/05/25 03:52:22  ralf
//	Added Get/SetEnumeration in prefs
//	
//	Revision 1.1.1.1  2005/04/28 21:33:48  ralf
//	Moved AppSkeleton.Utils in a separate LibUtils project
//	
//	Revision 1.5  2005/04/27 01:12:01  ralf
//	Updated Utils with files from Xeres
//	
//	Revision 1.4  2005/03/20 19:55:22  ralf
//	Updated Utils.RPrefs
//	
//	Revision 1.3  2005/03/20 19:48:40  ralf
//	Added GPL headers.
//	
//	Revision 1.2  2005/02/21 03:35:01  ralf
//	New Get/SetRect
//	
//	Revision 1.1.1.1  2005/02/18 22:54:53  ralf
//	A skeleton application template, with NUnit testing
//	
//---------------------------------------------------------------
