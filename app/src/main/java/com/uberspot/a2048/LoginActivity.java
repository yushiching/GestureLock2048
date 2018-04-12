package com.uberspot.a2048;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.opencsv.CSVWriter;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import pipi.win.a2048.activity.LockScreenActivity;
import pipi.win.a2048.utility.LogUtil;

/**
 * A login screen that offers login via name/age.
 */
public class LoginActivity extends Activity {

    /*
     * Id to identity READ_CONTACTS permission request.
     */
    //private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and ages.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mAgeView;
    private EditText mUserNameView;
    private View mProgressView;
    private View mLoginFormView;

    // Shared variables
    public static String mTouchFilePath = null;
    public static String mSensorFilePath = null;

    private ArrayList<String[]> mUserInfo = new ArrayList<String[]>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUserNameView = (EditText) findViewById(R.id.username);
        mAgeView = (EditText) findViewById(R.id.age);


        SensorService.startService(this);

        /* Added by Xiaopeng. Check the availability of external storage. */
        boolean isExternalStorageAvailable = isExternalStorageWritable();
        if (isExternalStorageAvailable) {
            //Log.d(DEBUG_TAG, "External storage is available");
            String touchBaseDir = getAlbumStorageDir(this, "Touch").getAbsolutePath();
            String sensorsBaseDir = getAlbumStorageDir(this, "Sensors").getAbsolutePath();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            Date now = new Date();

            String touchFileName = getUniqueID() + formatter.format(now) + "Touch.csv";
            mTouchFilePath = touchBaseDir + File.separator + touchFileName;
            LogUtil.i(mTouchFilePath);
            String sensorFileName = getUniqueID() + formatter.format(now) + "Sensor.csv";
            LogUtil.i(sensorFileName);
            mSensorFilePath = sensorsBaseDir + File.separator + sensorFileName;
        } else {
            Log.e("error msg", "External storage is not available");
        }
        /* End. Check the availability of external storage. */

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    @Override
    protected void onDestroy() {
        SensorService.stopService(this);
        super.onDestroy();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUserNameView.setError(null);
        mAgeView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUserNameView.getText().toString();
        String age = mAgeView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid age, if the user entered one.
        if (TextUtils.isEmpty(age) || !isAgeValid(age)) {
            int ecolor = Color.RED; // whatever color you want
            String estring = getString(R.string.error_invalid_age);
            ForegroundColorSpan fgcspan = new ForegroundColorSpan(ecolor);
            SpannableStringBuilder ssbuilder = new SpannableStringBuilder(estring);
            ssbuilder.setSpan(fgcspan, 0, estring.length(), 0);

            mAgeView.setError(ssbuilder);
            focusView = mAgeView;
            cancel = true;
        }

        // Check for a valid name.
        if (TextUtils.isEmpty(username)) {
            int ecolor = Color.RED; // whatever color you want
            String estring = getString(R.string.error_field_required);
            ForegroundColorSpan fgcspan = new ForegroundColorSpan(ecolor);
            SpannableStringBuilder ssbuilder = new SpannableStringBuilder(estring);
            ssbuilder.setSpan(fgcspan, 0, estring.length(), 0);

            mUserNameView.setError(ssbuilder);
            focusView = mUserNameView;
            cancel = true;
        } else if (!isNameValid(username)) {
            int ecolor = Color.RED; // whatever color you want
            String estring = getString(R.string.error_invalid_name);
            ForegroundColorSpan fgcspan = new ForegroundColorSpan(ecolor);
            SpannableStringBuilder ssbuilder = new SpannableStringBuilder(estring);
            ssbuilder.setSpan(fgcspan, 0, estring.length(), 0);

            mUserNameView.setError(ssbuilder);
            focusView = mUserNameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Write user information to the touch file and sensor file
            String[] userInfo = new String[3];
            userInfo[0] = "UserInfo_Tag";
            userInfo[1] = username;
            userInfo[2] = age;
            mUserInfo.add(userInfo);
            CSVWriter writer = null;
            writeToFile(writer, mTouchFilePath, mUserInfo);
            writeToFile(writer, mSensorFilePath, mUserInfo);

            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(username, age);
            mAuthTask.execute((Void) null);
            //Log.i("FLAG", "Login successful" + " ");
            //PinEntryActivity.startActivity(this);
             LockScreenActivity.startActivity(this);
        }
    }

    private boolean isNameValid(String username) {
        //TODO: Replace this with your own logic
        if(username.matches("[ a-zA-Z]*") && username.length() >=2) {
            return true;
        }
        return false;
    }

    private boolean isAgeValid(String age) {
        //TODO: Replace this with your own logic
        if( Integer.parseInt(age) >= 2 && Integer.parseInt(age) <= 80) {
            return true;
        }
        return false;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUserName;
        private final String mAge;

        UserLoginTask(String username, String age) {
            mUserName = username;
            mAge = age;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mUserName)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mAge);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mAgeView.setError(getString(R.string.error_incorrect_age));
                mAgeView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    /* Get the device id */
    public String getUniqueID() {
        String myAndroidDeviceId = "";
        //TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        myAndroidDeviceId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return myAndroidDeviceId;
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static File getAlbumStorageDir(Context context, String albumName) {
        // Get the directory for the app's private pictures directory.
        File file = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_DOCUMENTS), albumName);
        if (!file.mkdirs()) {
            Log.e("error msg: ", "Directory not created");
        }
        return file;
    }

    /* Write an array list of strings to a specific path */
    public static void writeToFile(CSVWriter writer, String path, ArrayList<String[]> data) {
        try {
            writer = new CSVWriter(new FileWriter(path, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
        writer.writeAll(data);
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

