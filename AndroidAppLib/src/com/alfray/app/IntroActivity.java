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

package com.alfray.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.alfray.error.ExceptionHandlerActivity;
import com.alfray.prefs.BasePrefsValues;

/**
 * Screen with the introduction text.
 */
public class IntroActivity extends ExceptionHandlerActivity {

    private static final boolean DEBUG = true;
    public static final String TAG = "LIB-IntroUI";

    public static final String EXTRA_HIDE_CONTROLS = "no-controls";

    private AgentWrapper mAgentWrapper;

    private class JSAppVersion {

        private String mVersion;

        public String longVersion() {
            if (mVersion == null) {
                PackageManager pm = getPackageManager();
                PackageInfo pi;
                try {
                    pi = pm.getPackageInfo(getPackageName(), 0);
                    mVersion = pi.versionName;
                    if (mVersion == null) mVersion = "";
                } catch (NameNotFoundException e) {
                    mVersion = ""; // failed, ignored
                }
            }
            return mVersion;
        }

        public String shortVersion() {
            String v = longVersion();
            if (v != null) {
                v = v.substring(0, v.lastIndexOf('.'));
            }
            return v;
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.intro);
        JSAppVersion jsVersion = new JSAppVersion();

        String appName = getString(R.string.app_name);

        String title = getString(R.string.intro_title, jsVersion.shortVersion());
        title = title.replaceAll("\\$APP", appName);
        setTitle(title);

        final WebView wv = (WebView) findViewById(R.id.web);
        if (wv == null) {
            if (DEBUG) Log.d(TAG, "Missing web view");
            finish();
        }

        // Make the webview transparent (for background gradient)
        wv.setBackgroundColor(0x00000000);

        // Inject a JS method to set the version
        wv.getSettings().setJavaScriptEnabled(true);
        wv.addJavascriptInterface(jsVersion, "JSAppVersion");

        String file = selectFile("intro");
        loadFile(wv, file);
        setupProgressBar(wv);
        setupWebViewClient(wv);
        setupButtons();
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

    private void setupProgressBar(final WebView wv) {
        final ProgressBar progress = (ProgressBar) findViewById(R.id.progress);
        if (progress != null) {
            wv.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    progress.setProgress(newProgress);
                    progress.setVisibility(newProgress == 100 ? View.GONE : View.VISIBLE);
                }
            });
        }
    }

    private void setupWebViewClient(final WebView wv) {
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.endsWith("/#new")) {
                    wv.loadUrl("javascript:location.href=\"#new\"");
                    return true;

                } else if (url.endsWith("/#known")) {
                    wv.loadUrl("javascript:location.href=\"#known\"");
                    return true;

                } else {
                    // For URLs that are not ours, including market: URLs
                    // just invoke the default view activity (e.g. Browser
                    // or Market app)
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        // ignore. just means this device has no Market or
                        // Browser app... ignore it.
                    }
                    return true;
                }
            }
        });
    }

    private void setupButtons() {
        boolean hideControls = false;
        Intent i = getIntent();
        if (i != null) hideControls = i.getBooleanExtra(EXTRA_HIDE_CONTROLS, false);

        CheckBox dismiss = (CheckBox) findViewById(R.id.dismiss);
        if (dismiss != null) {
            if (hideControls) {
                dismiss.setVisibility(View.GONE);
            } else {
                final BasePrefsValues pv = new BasePrefsValues(this);
                dismiss.setChecked(pv.isIntroDismissed());

                dismiss.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                            boolean isChecked) {
                        pv.setIntroDismissed(isChecked);
                    }
                });
            }
        }

        Button cont = (Button) findViewById(R.id.cont);
        if (cont != null) {
            if (hideControls) {
                cont.setVisibility(View.GONE);
            } else {
                cont.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // close activity
                        finish();
                    }
                });
            }
        }
    }
}
