//*******************************************************************
/*

	Solution:	AppSkeleton
	Project:	TestsConsole
	File:		RMain.cs

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
using System.IO;
using System.Text;
using System.Reflection;
using System.Collections;
using System.Collections.Specialized;
using System.Text.RegularExpressions;

using NUnit.Core;
using NUnit.Util;


//*************************************
namespace Alfray.AppSkeletonNs.TestsConsole
{
	//***************************************************
	/// <summary>
	/// RMain is the entry point of the test application.
	/// </summary>
	public class RMain
	{
		//-------------------------------------------
		//----------- Public Constants --------------
		//-------------------------------------------


		//-------------------------------------------
		//----------- Public Properties -------------
		//-------------------------------------------

		//***********************************
		/// <summary>
		/// List referenced libraries that must be automatically
		/// added to the project.
		/// </summary>
		//***********************************
		public static Type[] RefLibVersions =
		{
			typeof(Alfray.LibUtils.Tests.LibVersion)
		};


		//-------------------------------------------
		//----------- Public Methods ----------------
		//-------------------------------------------

		//*******************************************
		/// <summary>
		/// The main entry point for the application.
		/// </summary>
		//*******************************************
		[STAThread]
		static int Main(string[] args)
		{
			Console.WriteLine("AppSkeleton TestsConsole Main");
			System.Diagnostics.Trace.WriteLine("AppSkeleton TestsConsole Main");

			System.Console.Out.WriteLine("Running NUnit tests, please wait.");

			string assembly_path = System.Reflection.Assembly.GetExecutingAssembly().Location;

 #if NUNIT11
			TestDomain test_domain = new TestDomain();
 
			NUnitProject project = NUnitProject.FromAssembly(assembly_path);

			Test test = project.LoadTest(test_domain);
 
			// Extracted from NUnit.ConsoleUI.Execute()
			EventListener collector = null;
			collector = new NullListener(); 
 
			ConsoleWriter outStream = new ConsoleWriter(Console.Out);
			ConsoleWriter errorStream = new ConsoleWriter(Console.Error);
 
			string savedDirectory = Environment.CurrentDirectory;
			TestResult result = test_domain.Run(collector, outStream, errorStream);
			Directory.SetCurrentDirectory(savedDirectory);

			Console.WriteLine();
 
			StringBuilder builder = new StringBuilder();
			XmlResultVisitor resultVisitor = new XmlResultVisitor(new StringWriter( builder ), result);
			result.Accept(resultVisitor);
			resultVisitor.Write();
#elif NUNIT22
 
			// Extracted from NUnit.ConsoleUI.Execute()

			ConsoleOptions options = new ConsoleOptions(args);

			ConsoleWriter outStream = options.isOut
				? new ConsoleWriter(new StreamWriter(options.output))
				: new ConsoleWriter(Console.Out);

			ConsoleWriter errorStream = options.isErr
				? new ConsoleWriter(new StreamWriter(options.err))
				: new ConsoleWriter(Console.Error);

			TestDomain testDomain = new TestDomain(outStream, errorStream);
			if (options.noshadow)
				testDomain.ShadowCopyFiles = false;

			// MakeTestFromCommandLine
			NUnitProject project;
			

			// RM 20050426
			if (RefLibVersions.Length > 0)
			{
				ArrayList list = new ArrayList();

				// Get current assembly
				Assembly current = Assembly.GetExecutingAssembly();
				list.Add(current.Location);


				// RM 20050426 automatically add referenced 
				// assemblies to the list of tested assemblies

				foreach(Type t in RefLibVersions)
				{
					// make sure its' unique
					if (list.Contains(t.Assembly.Location))
						continue;

					// get version from LibVersion of AssemblyName.Version
					object o = null;
					
					try
					{
						o = t.InvokeMember("Version", 
							BindingFlags.Default | BindingFlags.Public | BindingFlags.GetProperty | BindingFlags.Static,
							null,
							null,
							new object[] { });
					}
					catch(Exception ex)
					{
						System.Diagnostics.Debug.WriteLine(ex.Message);
					}

					if (o == null)
						o = t.Assembly.GetName().Version;

					string s = "** Testing assembly: " + t.Namespace;

					if (o != null && o is Version)
						s += String.Format(", v{0}", o as Version);

					System.Console.Out.WriteLine(s);

					// add to list
					list.Add(t.Assembly.Location);
				} // for each

				// Create project
				string[] array = (string[])list.ToArray(typeof(string));
				project = NUnitProject.FromAssemblies(array);

			}
			else if (options.ParameterCount > 0 && options.IsTestProject)
			{
				project = NUnitProject.LoadProject((string)options.Parameters[0]);
				string configName = (string) options.config;
				if (configName != null)
					project.SetActiveConfig(configName);
			}
			else if (options.ParameterCount > 0)
			{
				project = NUnitProject.FromAssemblies((string[])options.Parameters.ToArray(typeof(string)));
			}
			else
			{
				project = NUnitProject.FromAssembly(assembly_path);
			}


			Test test = testDomain.Load(project, options.fixture);

			System.Diagnostics.Debug.Assert(test != null);
			if (test == null)
			{
				System.Console.Out.WriteLine("Error: Can't load test from assembly " + assembly_path);
				return 2;
			}

			// RM 20050320 fix: Original ConsoleUI.Execute saves the current dir *after* changing it...
			string savedDirectory = Environment.CurrentDirectory;

			// RM 20050320 fix: Original ConsoleUI.Execute does not check to see if there's at least one parameter
			if (options.ParameterCount > 0)
				Directory.SetCurrentDirectory(new FileInfo((string)options.Parameters[0]).DirectoryName);
		
			// RM 20050320 Create a collector... I used the NullListener before but
			// NUnit2.2's ConsoleUI.Execute uses something else so just mirror it here
			// EventListener collector = new NullListener(); 
			EventListener collector = new EventCollector(options, outStream);

			TestResult result = null;
			if (options.thread)
			{
				testDomain.RunTest(collector);
				testDomain.Wait();
				result = testDomain.Result;
			}
			else
			{
				result = testDomain.Run(collector);
			}

			Directory.SetCurrentDirectory(savedDirectory);
			
			if (testDomain != null)
				testDomain.Unload();

			return result.IsFailure ? 1 : 0;
#endif
		}

		
		//*****************
		public RMain()
		{
		}


		//-------------------------------------------
		//----------- Private Methods ---------------
		//-------------------------------------------


		//-------------------------------------------
		//----------- Private Attributes ------------
		//-------------------------------------------


		//-------------------------------------------
		//----------- Private Class ------------
		//-------------------------------------------

		#region Nested Class to Handle Events

		// RM 20050320: EventCollector is directly extracted from ConsoleUI.

		private class EventCollector : LongLivingMarshalByRefObject, EventListener
		{
			private int testRunCount;
			private int testIgnoreCount;
			private int failureCount;
			private int level;

			private ConsoleOptions options;
			private ConsoleWriter writer;

			StringCollection messages;
		
			private bool fancy_output = true;	// RM 20050320
			private bool debugger = false;
			private string currentTestName;

			public EventCollector(ConsoleOptions options,
				ConsoleWriter writer)
			{
				debugger = System.Diagnostics.Debugger.IsAttached;
				level = 0;
				this.options = options;
				this.writer = writer;
				this.currentTestName = string.Empty;
			}

			// RM 20050320
			private void WriteLine(string s)
			{
				if (debugger)
					System.Diagnostics.Trace.WriteLine(s);
				else
					Console.WriteLine(s);
			}

			public void RunStarted(Test[] tests)
			{
			}

			public void RunFinished(TestResult[] results)
			{
			}

			public void RunFinished(Exception exception)
			{
			}

			public void TestFinished(TestCaseResult testResult)
			{
				if ( !options.xmlConsole && !options.labels )
				{
					if(testResult.Executed)
					{
						testRunCount++;
						
						if(testResult.IsFailure)
						{	
							failureCount++;
							Console.Write("F");
							if (fancy_output)
							{
								// Output expected by VS.Net would look like this:
								// d:\ralfdev\csharp\appskeleton\testsconsole\rmain.cs(320,69): error CS1026: ) expected


								messages.Add( string.Format( "{0}) {1} :", failureCount, testResult.Test.FullName ) );

								string stackTrace = StackTraceFilter.Filter( testResult.StackTrace );
								string[] trace = stackTrace.Split( System.Environment.NewLine.ToCharArray() );
								foreach( string s in trace )
								{
									if (s != string.Empty)
									{
										string link = Regex.Replace( s.Trim(), @".* in (.*):line (.*)", "$1($2)");
										// -- messages.Add( string.Format( "at\n{0}", link ) );
										messages.Add(link);
									}
								}

								messages.Add( testResult.Message.Trim( Environment.NewLine.ToCharArray() ) );
							}
						}
					}
					else
					{
						testIgnoreCount++;
						Console.Write("N");
					}
				}

				currentTestName = string.Empty;
			}

			public void TestStarted(TestCase testCase)
			{
				currentTestName = testCase.FullName;

				if ( options.labels )
					writer.WriteLine("***** {0}", testCase.FullName );
				else if ( !options.xmlConsole )
					Console.Write(".");
			}

			public void SuiteStarted(TestSuite suite) 
			{
				if (fancy_output && level++ == 0 )
				{
					messages = new StringCollection();
					testRunCount = 0;
					testIgnoreCount = 0;
					failureCount = 0;
					// -- Trace.WriteLine( "################################ UNIT TESTS ################################" );
					WriteLine( "Running tests in '" + suite.FullName + "'..." );
				}
			}

			public void SuiteFinished(TestSuiteResult suiteResult) 
			{
				if (fancy_output && --level == 0) 
				{
					// -- Trace.WriteLine( "############################################################################" );

					if (messages.Count == 0) 
					{
						// -- Trace.WriteLine( "##############                 S U C C E S S               #################" );
						
						WriteLine("\n****** TestsConsole: SUCCESS");
					}
					else 
					{
						// -- Trace.WriteLine( "##############                F A I L U R E S              #################" );

						WriteLine("\n****** TestsConsole: FAILURES *******\n");
						
						foreach(string s in messages) 
							WriteLine(s);

						WriteLine("");
					}

					/*
					Trace.WriteLine( "############################################################################" );
					Trace.WriteLine( "Executed tests : " + testRunCount );
					Trace.WriteLine( "Ignored tests  : " + testIgnoreCount );
					Trace.WriteLine( "Failed tests   : " + failureCount );
					Trace.WriteLine( "Total time     : " + suiteResult.Time + " seconds" );
					Trace.WriteLine( "############################################################################");
					*/

					WriteLine(String.Format("****** Tests Results: {0} Passed, {1} Ignored, {2} Failed. Total time: {3} s ******",
						testRunCount, testIgnoreCount, failureCount, suiteResult.Time));

				}
			}

			public void UnhandledException( Exception exception )
			{
				string msg = string.Format("****** Unhandled Exception while running {0}", currentTestName );

				// If we do labels, we already have a newline
				if ( !options.labels ) writer.WriteLine();
				writer.WriteLine( msg );
				writer.WriteLine( exception.ToString() );

				if (fancy_output)
				{
					WriteLine( msg );
					WriteLine( exception.ToString() );
				}
			}
		}

		#endregion

	} // class RMain
} // namespace Alfray.AppSkeletonNs.TestsConsole


//---------------------------------------------------------------
//
//	$Log: RMain.cs,v $
//	Revision 1.5  2005/05/23 02:13:57  ralf
//	Added pref window skeleton.
//	Added load/save window settings for pref & debug windows.
//	
//	Revision 1.4  2005/04/30 22:20:05  ralf
//	Using separate test lib
//	
//	Revision 1.3  2005/04/28 21:31:14  ralf
//	Using new LibUtils project
//	
//	Revision 1.2  2005/03/20 19:48:30  ralf
//	Updated for NUnit 2.2.
//	Added GPL headers.
//	
//	Revision 1.1.1.1  2005/02/18 22:54:53  ralf
//	A skeleton application template, with NUnit testing
//	
//	
//---------------------------------------------------------------
