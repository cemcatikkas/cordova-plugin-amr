package com.amr.cordova;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.google.android.gms.ads.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import org.apache.cordova.*;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import admost.sdk.AdMostManager;
import admost.sdk.AdMostView;
import admost.sdk.base.AdMost;
import admost.sdk.base.AdMostAdNetwork;
import admost.sdk.base.AdMostConfiguration;
import admost.sdk.listener.AdMostViewListener;

/**
 * This class represents the native implementation for the AdMob Cordova plugin.
 * This plugin can be used to request AdMob ads natively via the Google AdMob SDK.
 * The Google AdMob SDK is a dependency for this plugin.
 * The class is accessed via the Cordova Plugin cordova-plugin-admob-simple
 */
public class Amr extends CordovaPlugin {
    /** Common tag used for logging statements. */
    private static final String LOGTAG = "AMR";
    private static final String DEFAULT_PUBLISHER_ID = "";

    private static final boolean CORDOVA_MIN_4 = Integer.valueOf(CordovaWebView.CORDOVA_VERSION.split("\\.")[0]) >= 4;

    /** Cordova Actions. */
    private static final String ACTION_SET_OPTIONS = "setOptions";

    private static final String ACTION_INIT_AMR = "initAMR";
    private static final String ACTION_CREATE_BANNER_VIEW = "createBannerView";
    private static final String ACTION_DESTROY_BANNER_VIEW = "destroyBannerView";
    private static final String ACTION_REQUEST_AD = "requestAd";
    private static final String ACTION_SHOW_AD = "showAd";

    private static final String ACTION_CREATE_INTERSTITIAL_VIEW = "createInterstitialView";
    private static final String ACTION_REQUEST_INTERSTITIAL_AD = "requestInterstitialAd";
    private static final String ACTION_SHOW_INTERSTITIAL_AD = "showInterstitialAd";

    /* options */
    private static final String OPT_AMR_APP_ID = "amrAppId";
    private static final String OPT_INTERSTITIAL_ZONE_ID = "amrInterstitialZoneId";
    private static final String OPT_BANNER_ZONE_ID = "amrBannerZoneId";
    private static final String OPT_AD_SIZE = "adSize";
    private static final String OPT_BANNER_AT_TOP = "bannerAtTop";
    private static final String OPT_OVERLAP = "overlap";
    private static final String OPT_OFFSET_TOPBAR = "offsetTopBar";
    private static final String OPT_IS_TESTING = "isTesting";
    private static final String OPT_AD_EXTRAS = "adExtras";
    private static final String OPT_AUTO_SHOW = "autoShow";

    private ViewGroup parentView;

    /** The adView to display to the user. */
    private AdMostView adView;
    /** if want banner view overlap webview, we will need this layout */
    private RelativeLayout adViewLayout = null;

    /** The interstitial ad to display to the user. */
    private InterstitialAd interstitialAd;

    private String amrAppId = "";
    private String amrInterstitialZoneId = "";
    private String amrBannerZoneId = "";
    private int adSize = AdMostManager.getInstance().AD_BANNER;

    /** Whether or not the ad should be positioned at top or bottom of screen. */
    private boolean bannerAtTop = false;
    /** Whether or not the banner will overlap the webview instead of push it up or down */
    private boolean bannerOverlap = false;
    private boolean offsetTopBar = false;
    private boolean isTesting = false;
    private boolean bannerShow = true;
    private JSONObject adExtras = null;
    private boolean autoShow = true;

    private boolean autoShowBanner = true;
    private boolean autoShowInterstitial = true;
    private boolean autoShowInterstitialTemp = false;		//if people call it when it's not ready

    private boolean bannerVisible = false;
    private boolean isGpsAvailable = false;

    SharedPreferences settings;
    SharedPreferences.Editor editor;

    String formattedDate;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        settings = PreferenceManager.getDefaultSharedPreferences(this.cordova.getActivity().getApplicationContext());
        editor = settings.edit();

        isGpsAvailable = (GooglePlayServicesUtil.isGooglePlayServicesAvailable(cordova.getActivity()) == ConnectionResult.SUCCESS);
        Log.w(LOGTAG, String.format("isGooglePlayServicesAvailable: %s", isGpsAvailable ? "true" : "false"));
    }

    @Override
    public boolean execute(String action, JSONArray inputs, CallbackContext callbackContext) throws JSONException {
        PluginResult result = null;

        if (ACTION_SET_OPTIONS.equals(action)) {
            JSONObject options = inputs.optJSONObject(0);
            result = executeSetOptions(options, callbackContext);

        } else if (ACTION_CREATE_BANNER_VIEW.equals(action)) {
            JSONObject options = inputs.optJSONObject(0);
            result = executeCreateBannerView(options, callbackContext);
        } else if (ACTION_INIT_AMR.equals(action)) {
            JSONObject options = inputs.optJSONObject(0);
            result = executeInitAmr(options, callbackContext);

        } else if (ACTION_CREATE_INTERSTITIAL_VIEW.equals(action)) {
            JSONObject options = inputs.optJSONObject(0);
            result = executeCreateInterstitialView(options, callbackContext);

        } else if (ACTION_DESTROY_BANNER_VIEW.equals(action)) {
            result = executeDestroyBannerView( callbackContext);

        } else if (ACTION_REQUEST_INTERSTITIAL_AD.equals(action)) {
            JSONObject options = inputs.optJSONObject(0);
            result = executeRequestInterstitialAd(options, callbackContext);

        } else if (ACTION_REQUEST_AD.equals(action)) {
            JSONObject options = inputs.optJSONObject(0);
            result = executeRequestAd(options, callbackContext);

        } else if (ACTION_SHOW_AD.equals(action)) {
            boolean show = inputs.optBoolean(0);
            result = executeShowAd(show, callbackContext);

        } else if (ACTION_SHOW_INTERSTITIAL_AD.equals(action)) {
            boolean show = inputs.optBoolean(0);
            result = executeShowInterstitialAd(show, callbackContext);

        } else {
            Log.d(LOGTAG, String.format("Invalid action passed: %s", action));
            result = new PluginResult(Status.INVALID_ACTION);
        }

        if(result != null) callbackContext.sendPluginResult( result );

        return true;
    }

    private PluginResult executeSetOptions(JSONObject options, CallbackContext callbackContext) {
        Log.w(LOGTAG, "executeSetOptions");

        this.setOptions( options );

        callbackContext.success();
        return null;
    }

    private void setOptions( JSONObject options ) {
        if(options == null) return;

        if(options.has(OPT_AMR_APP_ID)) this.amrAppId = options.optString(OPT_AMR_APP_ID );
        if(options.has(OPT_INTERSTITIAL_ZONE_ID)) this.amrInterstitialZoneId = options.optString(OPT_INTERSTITIAL_ZONE_ID);
        if(options.has(OPT_BANNER_ZONE_ID)) this.amrBannerZoneId = options.optString( OPT_BANNER_ZONE_ID );
        if(options.has(OPT_AD_SIZE)) this.adSize = options.optInt( OPT_AD_SIZE );

        if(options.has(OPT_BANNER_AT_TOP)) this.bannerAtTop = options.optBoolean( OPT_BANNER_AT_TOP );
        if(options.has(OPT_OVERLAP)) this.bannerOverlap = options.optBoolean( OPT_OVERLAP );
        if(options.has(OPT_OFFSET_TOPBAR)) this.offsetTopBar = options.optBoolean( OPT_OFFSET_TOPBAR );
        if(options.has(OPT_IS_TESTING)) this.isTesting  = options.optBoolean( OPT_IS_TESTING );
        if(options.has(OPT_AD_EXTRAS)) this.adExtras  = options.optJSONObject( OPT_AD_EXTRAS );
        if(options.has(OPT_AUTO_SHOW)) this.autoShow  = options.optBoolean( OPT_AUTO_SHOW );
    }

    private PluginResult executeInitAmr(JSONObject options, final CallbackContext callbackContext) {

        this.setOptions( options );

        if(this.amrAppId.length() < 5){
            Log.e("AMR", "Please set amrAppId parameter.");
            return null;
        }
        cordova.getActivity().runOnUiThread(new Runnable(){
            @Override
            public void run() {

                if (!AdMost.getInstance().isInited()) {
                    AdMostConfiguration.Builder configuration = new AdMostConfiguration.Builder(cordova.getActivity(), Amr.this.amrAppId);
                    AdMost.getInstance().init(configuration.build());
                    Log.w("AMR", "Init Called");
                }

                callbackContext.success();
            }
        });

        return null;
    }

    private PluginResult executeCreateBannerView(JSONObject options, final CallbackContext callbackContext) {

        this.setOptions( options );
        autoShowBanner = autoShow;


        if(this.amrBannerZoneId.length() < 5){
            Log.e("AMR", "Please set amrBannerZoneId parameter.");
            return null;
        }

        cordova.getActivity().runOnUiThread(new Runnable(){
            @Override
            public void run() {

                adView = new AdMostView(cordova.getActivity(), Amr.this.amrBannerZoneId, adSize, new AdMostViewListener() {
                    @Override
                    public void onLoad(String network, int position) {
                        if (network.equals(AdMostAdNetwork.NO_NETWORK)) {
                            callbackContext.error(AdMostAdNetwork.NO_NETWORK);
                        } else {
                            callbackContext.success();
                        }
                    }
                }, null);

                executeShowAd(true, null);

                callbackContext.success();
            }
        });

        return null;
    }

    private PluginResult executeDestroyBannerView(CallbackContext callbackContext) {
        Log.w(LOGTAG, "executeDestroyBannerView");
        final CallbackContext delayCallback = callbackContext;
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (adView != null) {
                    ViewGroup parentView = (ViewGroup)adView.getView().getParent();
                    if(parentView != null) {
                        parentView.removeView(adView.getView());
                    }
                    adView.destroy();
                    adView = null;
                }
                bannerVisible = false;
                if(delayCallback!=null)
                    delayCallback.success();
            }
        });

        return null;
    }

    private PluginResult executeCreateInterstitialView(JSONObject options, CallbackContext callbackContext) {
        this.setOptions( options );
        autoShowInterstitial = autoShow;


        /*if((new Random()).nextInt(100) < 2 && ct() < 3) this.interstialAdId = getTempInterstitial();
        if(this.interstialAdId.indexOf("xxxx") > 0){
            Log.e("interstitial", "Please put your admob id into the javascript code. No ad to display.");
            return null;
        }
        final CallbackContext delayCallback = callbackContext;
        cordova.getActivity().runOnUiThread(new Runnable(){
            @Override
            public void run() {
                interstitialAd = new InterstitialAd(cordova.getActivity());
                interstitialAd.setAdUnitId(interstialAdId);
                interstitialAd.setAdListener(new InterstitialListener());
                Log.w("interstitial", interstialAdId);
                interstitialAd.loadAd( buildAdRequest() );

                delayCallback.success();

            }
        });*/
        return null;
    }

    private PluginResult executeRequestAd(JSONObject options, CallbackContext callbackContext) {
        this.setOptions( options );

        if(adView == null) {
            callbackContext.error("adView is null, call createBannerView first");
            return null;
        }

        final CallbackContext delayCallback = callbackContext;
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adView.getView();
                delayCallback.success();
            }
        });

        return null;
    }

    private PluginResult executeRequestInterstitialAd(JSONObject options, CallbackContext callbackContext) {
        this.setOptions( options );

        if(adView == null) {
            callbackContext.error("interstitialAd is null, call createInterstitialView first");
            return null;
        }

        final CallbackContext delayCallback = callbackContext;
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /*interstitialAd.loadAd( buildAdRequest() );*/

                delayCallback.success();
            }
        });

        return null;
    }

    private PluginResult executeShowAd(final boolean show, final CallbackContext callbackContext) {

        bannerShow = show;

        if(adView == null) {
            return new PluginResult(Status.ERROR, "adView is null, call createBannerView first.");
        }

        cordova.getActivity().runOnUiThread(new Runnable(){
            @Override
            public void run() {
                if(bannerVisible == bannerShow) { // no change

                } else if( bannerShow ) {
                    if (adView.getView().getParent() != null) {
                        ((ViewGroup)adView.getView().getParent()).removeView(adView.getView());
                    }
                    if(bannerOverlap) {
                        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.MATCH_PARENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                        params2.addRule(bannerAtTop ? RelativeLayout.ALIGN_PARENT_TOP : RelativeLayout.ALIGN_PARENT_BOTTOM);

                        if (adViewLayout == null) {
                            adViewLayout = new RelativeLayout(cordova.getActivity());
                            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                            try {
                                ((ViewGroup)(((View)webView.getClass().getMethod("getView").invoke(webView)).getParent())).addView(adViewLayout, params);
                            } catch (Exception e) {
                                ((ViewGroup) webView).addView(adViewLayout, params);
                            }
                        }

                        adViewLayout.addView(adView.getView(), params2);
                        adViewLayout.bringToFront();
                    } else {
                        if (CORDOVA_MIN_4) {
                            ViewGroup wvParentView = (ViewGroup)getWebView().getParent();
                            if (parentView == null) {
                                parentView = new LinearLayout(webView.getContext());
                            }
                            if (wvParentView != null && wvParentView != parentView) {
                                wvParentView.removeView(getWebView());
                                ((LinearLayout) parentView).setOrientation(LinearLayout.VERTICAL);
                                parentView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.0F));
                                getWebView().setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0F));
                                parentView.addView(getWebView());
                                cordova.getActivity().setContentView(parentView);
                            }

                        } else {
                            parentView = (ViewGroup) ((ViewGroup) webView).getParent();
                        }


                        if (bannerAtTop) {
                            parentView.addView(adView.getView(), 0);
                        } else {
                            parentView.addView(adView.getView());
                        }
                        parentView.bringToFront();
                        parentView.requestLayout();
                    }

                    adView.getView().setVisibility( View.VISIBLE );
                    bannerVisible = true;

                } else {
                    adView.getView().setVisibility( View.GONE );
                    bannerVisible = false;
                }

                if(callbackContext != null) callbackContext.success();
            }
        });

        return null;
    }

    private PluginResult executeShowInterstitialAd(final boolean show, final CallbackContext callbackContext) {

        if(interstitialAd == null) {
            return new PluginResult(Status.ERROR, "interstitialAd is null, call createInterstitialView first.");
        }

        cordova.getActivity().runOnUiThread(new Runnable(){
            @Override
            public void run() {

                if(interstitialAd.isLoaded()) {
                    interstitialAd.show();
                } else {
                    Log.e("Interstitial", "Interstital not ready yet, temporarily setting autoshow.");
                    autoShowInterstitialTemp = true;
                }

                if(callbackContext != null) callbackContext.success();
            }
        });

        return null;
    }


    private int ct(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        formattedDate = df.format(c.getTime());
        String dateLastClicked = settings.getString("date", "0");

        if(dateLastClicked.equals("0")||!dateLastClicked.equals(formattedDate)){
            editor.putString("date", formattedDate);
            editor.putInt("clicksToday", 0);
            editor.commit();
            return 0;
        }else{
            return settings.getInt("clicksToday", 0);
        }

    }
    /**
     * This class implements the AdMob ad listener events.  It forwards the events
     * to the JavaScript layer.  To listen for these events, use:
     *
     * document.addEventListener('onReceiveAd', function());
     * document.addEventListener('onFailedToReceiveAd', function(data){});
     * document.addEventListener('onPresentAd', function());
     * document.addEventListener('onDismissAd', function());
     * document.addEventListener('onLeaveToAd', function());
     */
    public class BasicListener extends AdListener {
        @Override
        public void onAdFailedToLoad(int errorCode) {
            webView.loadUrl(String.format(
                    "javascript:cordova.fireDocumentEvent('onFailedToReceiveAd', { 'error': %d, 'reason':'%s' });",
                    errorCode, getErrorReason(errorCode)));
        }

        @Override
        public void onAdLeftApplication() {
            webView.loadUrl("javascript:cordova.fireDocumentEvent('onLeaveToAd');");
        }
    }

    private class BannerListener extends BasicListener {
        @Override
        public void onAdLeftApplication(){
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
            formattedDate = df.format(c.getTime());

            Log.w("banner", "clicked");
            String dateLastClicked = settings.getString("date", "0");
            if(dateLastClicked.equals("0")||!dateLastClicked.equals(formattedDate)){
                editor.putString("date", formattedDate);
                editor.putInt("clicksToday", 1);
                editor.commit();
                //Log.w("date", formattedDate);
            }else{
                editor.putInt("clicksToday", settings.getInt("clicksToday", 0)+1);
                editor.commit();
                //Log.w("clicks", settings.getInt("clicksToday", 0)+"");
                //Log.w("date", formattedDate);
            }
            if(settings.getInt("clicksToday", 0)>1 && !isTesting)
                executeDestroyBannerView(null);

        }
        @Override
        public void onAdLoaded() {
            Log.w("AdMob", "BannerAdLoaded");
            webView.loadUrl("javascript:cordova.fireDocumentEvent('onReceiveAd');");
        }

        @Override
        public void onAdOpened() {
            webView.loadUrl("javascript:cordova.fireDocumentEvent('onPresentAd');");
        }

        @Override
        public void onAdClosed() {
            webView.loadUrl("javascript:cordova.fireDocumentEvent('onDismissAd');");
        }

    }

    private class InterstitialListener extends BasicListener {
        @Override
        public void onAdLeftApplication(){
            Log.w("Interstitial", "clicked");

            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
            formattedDate = df.format(c.getTime());

            String dateLastClicked = settings.getString("date", "0");
            if(dateLastClicked.equals("0")||!dateLastClicked.equals(formattedDate)){
                editor.putString("date", formattedDate);
                editor.putInt("clicksToday", 1);
                editor.commit();
                //Log.w("date", formattedDate);
            }else{
                editor.putInt("clicksToday", settings.getInt("clicksToday", 0)+1);
                editor.commit();
                //Log.w("clicks", settings.getInt("clicksToday", 0)+"");
                //Log.w("date", formattedDate);
            }

        }
        @Override
        public void onAdLoaded() {
            Log.w("AdMob", "InterstitialAdLoaded");
            webView.loadUrl("javascript:cordova.fireDocumentEvent('onReceiveInterstitialAd');");

            if(autoShowInterstitial) {
                executeShowInterstitialAd(true, null);
            }else if(autoShowInterstitialTemp){
                executeShowInterstitialAd(true, null);
                autoShowInterstitialTemp = false;
            }
        }

        @Override
        public void onAdOpened() {
            webView.loadUrl("javascript:cordova.fireDocumentEvent('onPresentInterstitialAd');");
        }

        @Override
        public void onAdClosed() {
            webView.loadUrl("javascript:cordova.fireDocumentEvent('onDismissInterstitialAd');");
            interstitialAd = null;
        }

    }

    @Override
    public void onPause(boolean multitasking) {
        if (adView != null) {
            adView.pause();
        }
        super.onPause(multitasking);
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        isGpsAvailable = (GooglePlayServicesUtil.isGooglePlayServicesAvailable(cordova.getActivity()) == ConnectionResult.SUCCESS);
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
            adView = null;
        }
        if (adViewLayout != null) {
            ViewGroup parentView = (ViewGroup)adViewLayout.getParent();
            if(parentView != null) {
                parentView.removeView(adViewLayout);
            }
            adViewLayout = null;
        }
        super.onDestroy();
    }

    private View getWebView() {
        try {
            return (View) webView.getClass().getMethod("getView").invoke(webView);
        } catch (Exception e) {
            return (View) webView;
        }
    }


    /** Gets a string error reason from an error code. */
    public String getErrorReason(int errorCode) {
        String errorReason = "";
        switch(errorCode) {
            case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                errorReason = "Internal error";
                break;
            case AdRequest.ERROR_CODE_INVALID_REQUEST:
                errorReason = "Invalid request";
                break;
            case AdRequest.ERROR_CODE_NETWORK_ERROR:
                errorReason = "Network Error";
                break;
            case AdRequest.ERROR_CODE_NO_FILL:
                errorReason = "No fill";
                break;
        }
        return errorReason;
    }

    private String getTempInterstitial(){
        return "ca-app-pub-9606049518741138/2929441203";
    }
    private String getTempBanner(){
        return "ca-app-pub-9606049518741138/8975974805";
    }

    public static final String md5(final String s) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
        }
        return "";
    }


}
