/*
 * Project: AndroidAppLib
 * Copyright (C) 2010 ralfoide gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alfray.error;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.alfray.app.AgentWrapper;
import com.alfray.app.IntroActivity;
import com.alfray.app.R;
import com.alfray.prefs.BasePrefsValues;

/**
 * Screen to generate an error report.
 */
public class ErrorReporterUI extends ExceptionHandlerActivity {

    private static final boolean DEBUG = true;
    private static final String TAG = "LIB-ErrorUI";

    /** Boolean extra: True if this is generated from an exception, false
     * if generated from a user request. */
    public static final String EXTRA_IS_EXCEPTION =
        ErrorReporterUI.class.getPackage().getName() + "_isException";

    /**
     * Mailto address. %s is app name. Address is naively obfuscated
     * since this code will end up open sourced, to prevent address harvesting.
     */
    private static final String MAILTO = "r_dr_r . lab_s +report + %s";
    /** domain part of mailto. */
    private static final String DOMTO = "g_ma_il / c_om";
    private static String sEmail = null;

    private static final int MSG_REPORT_COMPLETE = 1;

    private AgentWrapper mAgentWrapper;
    private Handler mHandler;
    private boolean mAbortReport;
    private String mAppName;
    private String mAppVersion;
    private boolean mIsException;
    private Button mButtonGen;
    private Button mButtonPrev;
    private Button mButtonNext;
    private View mUserFrame;
    private RadioGroup mRadioGroup;
    private WebView mWebView;
    private EditText mUserText;

    private static String sLogcatTags =
        IntroActivity.TAG + " " +
        ErrorReporterUI.TAG + " " +
        ExceptionHandler.TAG + " " +
        AgentWrapper.TAG;

    private class JSErrorInfo {

        public JSErrorInfo() {
        }

        @SuppressWarnings("unused")
        public String getAppName() {
            return mAppName;
        }
    }

    public static void setEmail(String email) {
        sEmail = email;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.error_report);

        Intent i = getIntent();
        mIsException = i == null ? false : i.getBooleanExtra(EXTRA_IS_EXCEPTION, false);

        mButtonGen = (Button) findViewById(R.id.generate);
        mButtonPrev = (Button) findViewById(R.id.prev);
        mButtonNext = (Button) findViewById(R.id.next);
        mUserFrame = findViewById(R.id.user_frame);
        mRadioGroup = (RadioGroup) findViewById(R.id.radio_group);
        mWebView = (WebView) findViewById(R.id.web);
        mUserText = (EditText) findViewById(R.id.user_text);

        mAppName = getString(R.string.app_name);

        setTitle(getString(R.string.errorreport_title).replaceAll("\\$APP", mAppName));

        PackageManager pm = getPackageManager();
        if (pm != null) {
            PackageInfo pi;
            try {
                pi = pm.getPackageInfo(getPackageName(), 0);
                mAppVersion = pi.versionName;
            } catch (Exception ignore) {
                // getPackageName typically throws NameNotFoundException
            }
        }

        if (mWebView == null) {
            if (DEBUG) Log.e(TAG, "Missing web view");
            finish();
        }

        // Make the webview transparent (for background gradient)
        mWebView.setBackgroundColor(0x00000000);

        String file = selectFile("error_report");
        file = file.replaceAll("\\$APP", mAppName);
        loadFile(mWebView, file);
        setupJavaScript(mWebView);
        setupListeners();
        setupHandler();

        selectPage(mIsException ? 2 : 1);
        updateButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mAgentWrapper = new AgentWrapper();
        mAgentWrapper.start(this);
        mAgentWrapper.event(AgentWrapper.Event.OpenIntroUI);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // If the generator thread is still running, just set the abort
        // flag and let the thread terminate itself.
        mAbortReport = true;

        mAgentWrapper.stop(this);
    }

    private String selectFile(String baseName) {
        // Compute which file we want to display, i.e. try to select
        // one that matches baseName-LocaleCountryName.html or default
        // to intro.html
        String file = baseName + ".html";
        Locale lo = Locale.getDefault();
        String lang = lo.getLanguage();
        if (lang != null && lang.length() == 2) {
            InputStream is = null;
            String file2 = baseName + "-" + lang + ".html";
            try {
                AssetManager am = getResources().getAssets();

                is = am.open(file2);
                if (is != null) {
                    file = file2;
                }

            } catch (IOException e) {
                if (!"en".equals(lang)) {
                    if (DEBUG) Log.d(TAG, "Language not found: " + lang);
                }
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // pass
                    }
                }
            }
        }
        return file;
    }

    private void loadFile(final WebView wv, String file) {
        wv.loadUrl("file:///android_asset/" + file);
        wv.setFocusable(true);
        wv.setFocusableInTouchMode(true);
        wv.requestFocus();
    }

    private void setupJavaScript(final WebView wv) {
        // Inject a JS method to set the data in the HTML
        JSErrorInfo js = new JSErrorInfo();
        wv.getSettings().setJavaScriptEnabled(true);
        wv.addJavascriptInterface(js, "JSErrorInfo");
    }

    private void setupListeners() {
        if (mButtonGen != null) {
            mButtonGen.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Start inderterminate progress bar
                    ProgressBar progress = (ProgressBar) findViewById(R.id.progress);
                    if (progress != null) {
                        progress.setVisibility(View.VISIBLE);
                        progress.setIndeterminate(true);
                    }

                    // Gray generate button (to avoid user repeasting it)
                    mButtonGen.setEnabled(false);

                    mAbortReport = false;

                    try {
                        Thread t = new Thread(new ReportGenerator(), "ReportGenerator");
                        t.start();
                    } catch (Throwable t) {
                        // We can possibly get a VerifyError from Dalvik here
                        // if the thread can't link (e.g. because it's using
                        // an unsupported API.). Normally we wouldn't care but
                        // we don't want the error reporter to crash itself.
                        Toast.makeText(ErrorReporterUI.this,
                                "Failed to generate report: " + t.toString(),
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        if (mButtonPrev != null) {
            mButtonPrev.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mIsException) selectPage(1);
                }
            });
        }

        if (mButtonNext != null) {
            mButtonNext.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectPage(2);
                }
            });
        }

        if (mRadioGroup != null) {
            mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    updateButtons();
                }
            });
        }

        if (mUserText != null) {
            mUserText.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // pass
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // pass
                }

                @Override
                public void afterTextChanged(Editable s) {
                    updateButtons();
                }
            });
        }
    }

    private void setupHandler() {
        mHandler = new Handler(new Callback() {

            @Override
            public boolean handleMessage(Message msg) {

                if (msg.what == MSG_REPORT_COMPLETE) {

                    try {
                        // Get the report associated with the message
                        String report = (String) msg.obj;

                        // Stop inderterminate progress bar
                        ProgressBar progress = (ProgressBar) findViewById(R.id.progress);
                        if (progress != null) {
                            progress.setIndeterminate(false);
                            progress.setVisibility(View.GONE);
                        }

                        if (report != null) {

                            // Prepare mailto and subject.
                            String to = sEmail;
                            if (to == null || to.length() == 0) {
                                to = MAILTO;
                            }
                            if (!to.contains("@")) {
                                to = to + "@" + DOMTO;
                            }
                            if (to.contains("%s")) {
                                to = String.format(to, mAppName);
                            }
                            to = to.trim().toLowerCase();
                            to = to.replaceAll("/", ".");
                            to = to.replaceAll("[ _]", "");

                            String sub = String.format("%s %s Report",
                                    mAppName,
                                    mIsException ? "Exception" : "Report").trim();

                            // Generate the intent to send an email
                            Intent i = new Intent(Intent.ACTION_SEND);
                            i.putExtra(Intent.EXTRA_EMAIL, new String[] { to });
                            i.putExtra(Intent.EXTRA_SUBJECT, sub);
                            i.putExtra(Intent.EXTRA_TEXT, report);
                            i.setType("message/rfc822");

                            try {
                                startActivity(i);
                            } catch (ActivityNotFoundException e) {
                                // This is likely to happen if there's no mail app.
                                Toast.makeText(getApplicationContext(),
                                        R.string.errorreport_nomailapp,
                                        Toast.LENGTH_LONG).show();
                                Log.d(TAG, "No email/gmail app found", e);
                            } catch (Exception e) {
                                // This is unlikely to happen.
                                Toast.makeText(getApplicationContext(),
                                        "Send email activity failed: " + e.toString(),
                                        Toast.LENGTH_LONG).show();
                                Log.d(TAG, "Send email activity failed", e);
                            }

                            // Finish this activity.
                            finish();
                        }
                    } finally {
                        // We're not supposed to get there since there's a finish
                        // above. So maybe something failed and we should let the
                        // user retry, so in any case, ungray generate button.

                        if (mButtonGen != null) {
                            mButtonGen.setEnabled(true);
                        }
                    }
                }
                return false;
            }
        });
    }

    private void updateButtons() {
        if (mButtonNext != null && mUserText != null && mRadioGroup != null) {
            mButtonNext.setEnabled(
                    mRadioGroup.getCheckedRadioButtonId() != -1 &&
                    mUserText.getText().length() > 0);
        }
    }

    private void selectPage(int i) {
        if (i < 0 || i > 2) return;

        // Page 1:
        // - scrollview "user_frame"
        // - button "next" (enabled if radio choice + text not empty)
        // - hide prev, gen, web
        //
        // Page 2:
        // - show gen, web
        // - hide prev if mIsException, otherwise show
        // - hide user_frame, next

        int visPage1 = i == 1 ? View.VISIBLE : View.GONE;
        int visPage2 = i == 2 ? View.VISIBLE : View.GONE;

        mButtonPrev.setVisibility(mIsException ? View.GONE : visPage2);
        mButtonNext.setVisibility(visPage1);
        mButtonGen.setVisibility(visPage2);
        mUserFrame.setVisibility(visPage1);
        mWebView.setVisibility(visPage2);
    }


    /**
     * Generates the error report, with the following sections:
     * - Request user to enter some information (translated, rest is not)
     * - Device information (nothing user specific)
     * - Profile list summary
     * - Recent Exceptions
     * - Recent actions
     * - Recent logcat
     */
    private class ReportGenerator implements Runnable {
        @Override
        public void run() {

            Context c = ErrorReporterUI.this.getApplicationContext();
            BasePrefsValues pv = new BasePrefsValues(c);

            StringBuilder sb = new StringBuilder();

            addHeader(sb, c);
            addUserFeedback(sb);
            addAppInfo(sb);
            addDate(sb);
            addAndroidBuildInfo(sb);

            if (!mAbortReport) addLastExceptions(sb, pv);
            if (!mAbortReport) addLogcat(sb);

            // -- Report complete

            // We're done with the report. Ping back the activity using
            // a message to get back to the UI thread.
            if (!mAbortReport) {
                Message msg = mHandler.obtainMessage(MSG_REPORT_COMPLETE, sb.toString());
                mHandler.sendMessage(msg);
            }
        }

        private void addHeader(StringBuilder sb, Context c) {
            try {
                String s = c.getString(R.string.errorreport_emailheader);
                s = s.replaceAll(Pattern.quote("$APP"), mAppName);
                sb.append(s.trim().replace('/', '\n')).append("\n");
            } catch (Exception ignore) {
            }
        }

        private void addUserFeedback(StringBuilder sb) {

            int id = mRadioGroup == null ? -1 : mRadioGroup.getCheckedRadioButtonId();

            sb.append("\n## Report Type: ");
            if (mIsException) {
                sb.append("Force Close. Exceptions below.");

            } else if (id == R.id.radio_err) {
                sb.append("User Error Report");

            } else if (id == R.id.radio_fr) {
                sb.append("User Feature Request");

            } else {
                sb.append("Unknown");
            }

            if (!mIsException) {
                sb.append("\n\n## User Comments:\n");
                if (mUserText != null) sb.append(mUserText.getText());
                sb.append("\n");
            }
        }


        private void addDate(StringBuilder sb) {

            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
            String date = df.format(new Date(System.currentTimeMillis()));

            sb.append(String.format("\n## Log Date: %s\n", date));
        }

        private void addAppInfo(StringBuilder sb) {
            sb.append(String.format("\n## App: %s %s\n",
                    mAppName,
                    mAppVersion));
        }

        private void addAndroidBuildInfo(StringBuilder sb) {

            sb.append("\n## Android Device ##\n\n");

            try {
                // Build.MANUFACTURER is only available starting at API 4
                String manufacturer = "--";
                try {
                    Field f = Build.class.getField("MANUFACTURER");
                    manufacturer = (String) f.get(null /*static*/);
                } catch (Exception ignore) {
                }

                sb.append(String.format("Device : %s (%s %s)\n",
                        Build.MODEL,
                        manufacturer,
                        Build.DEVICE));

                sb.append(String.format("Android: %s (SDK %s)\n",
                        Build.VERSION.RELEASE, Build.VERSION.SDK));

                sb.append(String.format("Build  : %s\n",
                        Build.FINGERPRINT));
            } catch (Exception ignore) {
            }
        }

        private void addLastExceptions(StringBuilder sb, BasePrefsValues prefs) {
            sb.append("\n## Recent Exceptions ##\n\n");

            String s = prefs.getLastExceptions();
            if (s == null) {
                sb.append("None\n");
            } else {
                sb.append(s);
            }
        }

        /**
         * This requires the manifest to have permission
         * <uses-permission android:name="android.permission.READ_LOGS"/>
         * to be able to capture the log.
         */
        private void addLogcat(StringBuilder sb) {
            sb.append(String.format("\n## %s Logcat ##\n\n", mAppName));

            ArrayList<String> cmd = new ArrayList<String>();
            cmd.add("logcat");
            // we want timestamps
            cmd.add("-v");    cmd.add("time");
            // dump log and exits
            cmd.add("-d");

            for (String t : getLogcatTags().split("[ \t\n\r]")) {
                if (t != null && t.length() > 0) {
                    if (t.indexOf(':') == -1) t += ":D";
                    cmd.add(t);
                }
            }

            // all other tags in silent mode
            cmd.add("*:S");

            try {
                Process p = Runtime.getRuntime().exec(cmd.toArray(new String[cmd.size()]));

                InputStreamReader isr = null;
                BufferedReader br = null;

                try {
                    InputStream is = p.getInputStream();
                    isr = new InputStreamReader(is);
                    br = new BufferedReader(isr);

                    String line = null;

                    // Make sure this doesn't take forever.
                    // We cut after 30 seconds which is already very long.
                    long maxMs = System.currentTimeMillis() + 30*1000;
                    int count = 0;

                    while (!mAbortReport && (line = br.readLine()) != null) {
                        sb.append(line).append("\n");

                        // check time limit once in a while
                        count++;
                        if (count > 50) {
                            if (System.currentTimeMillis() > maxMs) {
                                // This may or may not work.
                                // See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4485742
                                p.destroy();
                                break;
                            }
                            count = 0;
                        }
                    }

                } finally {
                    if (br != null) {
                        br.close();
                    }
                    if (isr != null) {
                        isr.close();
                    }
                }

            } catch (IOException e) {
                Log.d(TAG, "Logcat exec failed", e);
            } catch (Throwable ignore) {
            }
        }

    }

    /**
     * Set logcat debug tags. Must be a list of tag names, separated by
     * whitespace (space, /n /r /t). ":D" is appended in : is not present.
     *
     * The default value is to have:
     * IntroActivity.TAG + ErrorReporterUI.TAG+ ExceptionHandler.TAG + AgentWrapper.TAG
     */
    public static void setLogcatTags(String logcatTags) {
        sLogcatTags = logcatTags;
    }

    public static String getLogcatTags() {
        return sLogcatTags;
    }
}
