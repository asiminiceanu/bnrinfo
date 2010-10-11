package com.android.bnrinfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

public class BnrInfo extends Activity {

    /*
     * The goodies
     */
    private String httpResponse = new String("");

    /*
     * The error message
     */
    private String errorMessage = new String("");

    /*
     * The app version number
     */
    static String appVersion = new String("");

    /*
     * Need handler for call backs to the UI thread
     */
    final Handler uiHandler = new Handler();

    /*
     * The list of rates types
     */
    static Map<Integer, Map<String, String>> currencies = new HashMap<Integer, Map<String, String>>();
   
    /*
     * The ListView in main layout
     */
    private ListView listView;

    /*
     * The date of the goodies
     */
    static String sendingDate = new String("");

    /*
     * The header TextView
     */
    private TextView header;

    /*
     * Create runnable for posting signals to UI
     */
    final Runnable signalUi = new Runnable() {
        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Runnable#run()
         */
        @Override
		public void run() {
            updateUi();
        }
    };

    /*
     * Called when the activity is first created.(non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if already launched the application
        if (currencies.size() > 0) {
            updateUi();
            return;
        }

        // Get the data
        getGoodies();
    }

    /*
     * Execute the HTTP request to get the goodies
     */
    protected void executeRequest() {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(getString(R.string.serverUrl));

        try {
            // Add the device id for statistics data
            TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String uid = tManager.getDeviceId();

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("DeviceId", uid.toString()));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);

            HttpEntity resEntity = response.getEntity();
            httpResponse = EntityUtils.toString(resEntity);
        } catch (ClientProtocolException e) {
            errorMessage = new String(getString(R.string.serverErr));
        } catch (IOException e) {
            errorMessage = new String(getString(R.string.serverErr));
        }
    }

    /*
     * Parse the JSON response
     */
    protected void parseResponse() {
        try {
            JSONObject jObject = new JSONObject(httpResponse);
            JSONArray ratesArray = jObject.getJSONArray("Rates");

            sendingDate = new String(jObject.getString("SendingDate").toString());

            for (int i = 0; i < ratesArray.length(); i++) {
            	Map<String, String> rateProps = new HashMap<String, String>(); 
            	
            	rateProps.put("multiplier", ratesArray.getJSONObject(i).getString("multiplier").toString());
            	rateProps.put("diff", ratesArray.getJSONObject(i).getString("diff").toString());
            	rateProps.put("value", ratesArray.getJSONObject(i).getString("rate").toString());
            	rateProps.put("currency", ratesArray.getJSONObject(i).getString("currency").toString());
            	
            	currencies.put(i, rateProps);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            errorMessage = new String(getString(R.string.serverErr));
        }
    }

    /*
     * Update the UI with the retrieved goodies
     */
    protected void updateUi() {

        if (errorMessage.compareTo(new String(getString(R.string.serverErr))) == 0) {
            AlertDialog errorDialog = new AlertDialog.Builder(this).setIcon(R.drawable.logo)
                    .setTitle(getString(R.string.app_name) + " " + appVersion).setMessage(errorMessage.toString())
                    .setNeutralButton(R.string.app_about_button_ok, new DialogInterface.OnClickListener() {
                        @Override
						public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                        }
                    }).create();

            errorDialog.show();
            return;
        }

        setContentView(R.layout.main);

        header = (TextView) findViewById(R.id.header);
        header.setText(getString(R.string.header) + " " + sendingDate.toString());

        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(new DisplayAdapter(this));
    }

    /*
     * Open the BNR web site in the browser
     */
    protected void openBrowser() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(getString(R.string.webUrl)));
        startActivity(i);
    }

    /*
     * Open the about dialog
     */
    protected void openAbout() {
        AlertDialog aboutDialog = new AlertDialog.Builder(this).setIcon(R.drawable.logo)
                .setTitle(getString(R.string.app_name) + " " + appVersion).setMessage(R.string.app_about)
                .setPositiveButton(R.string.app_about_button_ok, new DialogInterface.OnClickListener() {
                    @Override
					public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.app_about_button_feedback, new DialogInterface.OnClickListener() {
                    @Override
					public void onClick(DialogInterface dialog, int whichButton) {
                        // Send the email with the feedback
                        Intent i = new Intent(Intent.ACTION_SEND);

                        i.setType("message/rfc822");
                        i.putExtra(Intent.EXTRA_EMAIL, new String[] { getString(R.string.email_address) });
                        i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));

                        startActivity(Intent.createChooser(i, getString(R.string.email_select_app)));
                    }
                }).create();

        aboutDialog.show();
    }

    /**
     * Execute http request and update ui
     */
    protected void getGoodies() {
        // Reset the stored data
        /*ratesType.clear();
        ratesDiff.clear();
        ratesMultiplier.clear();
        ratesValues.clear();
        */
    	currencies.clear();

        // Get the app version number and store it
        PackageInfo packageInfo;

        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            appVersion = packageInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        // Create the loading dialog
        // Retrieving the goodies might take some time
        final ProgressDialog dialog = ProgressDialog.show(this, getString(R.string.loading_title),
                getString(R.string.loading_desc), true);

        // Run the HTTP request in a thread so that the dialog will appear
        Thread httpThread = new Thread(new Runnable() {
            /*
             * (non-Javadoc)
             * 
             * @see java.lang.Runnable#run()
             */
            @Override
			public void run() {
                executeRequest();
                parseResponse();
                dialog.dismiss();
                uiHandler.post(signalUi);
            }
        });

        httpThread.start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bnrinfo, menu);

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                // About myself
                openAbout();
                break;

            case R.id.bnrurl:
                // Open the browser
                openBrowser();
                break;

            case R.id.refresh:
                // Get the goodies
                getGoodies();
                break;

            case R.id.exit:
                // Hide in the dark
                finish();
                break;
        }

        return true;
    }
}