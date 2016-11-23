var argscheck = require('cordova/argscheck'),
    exec = require('cordova/exec');

var amrExport = {};

/**
 * This enum represents Amr's supported ad sizes.  Use one of these
 * constants as the adSize when calling createBannerView.
 * @const
 */
amrExport.AD_SIZE = {
  BANNER: 50,
  LEADERBOARD: 90,
  MEDIUM_RECTANGLE: 250
};

amrExport.setOptions =
	function(options, successCallback, failureCallback) {
	  if(typeof options === 'object' 
		  && typeof options.amrAppId === 'string'
	      && options.amrAppId.length > 0) {
		  cordova.exec(
			      successCallback,
			      failureCallback,
			      'Amr',
			      'setOptions',
			      [options]
			  );
	  } else {
		  if(typeof failureCallback === 'function') {
			  failureCallback('options.amrAppId should be specified.')
		  }
	  }
	};
amrExport.createBannerView =
function(options, successCallback, failureCallback) {
  if(typeof options === 'undefined' || options == null) options = {};
  cordova.exec(
      successCallback,
      failureCallback,
      'Amr',
      'createBannerView',
      [ options ]
  );
};

amrExport.createInterstitialView =
function(options, successCallback, failureCallback) {
  cordova.exec(
      successCallback,
      failureCallback,
      'Amr',
      'createInterstitialView',
      [ options ]
  );
};

amrExport.initAMR =
function(options, successCallback, failureCallback) {
  cordova.exec(
      successCallback,
      failureCallback,
      'Amr',
      'initAMR',
      [ options ]
  );
};



amrExport.destroyBannerView =
function(options, successCallback, failureCallback) {
  if(typeof options === 'undefined' || options == null) options = {};
  cordova.exec(
	      successCallback,
	      failureCallback,
	      'Amr',
	      'destroyBannerView',
	      []
	  );
};

amrExport.requestAd =
function(options, successCallback, failureCallback) {
	  if(typeof options === 'undefined' || options == null) options = {};
  cordova.exec(
      successCallback,
      failureCallback,
      'Amr',
      'requestAd',
      [ options ]
  );
};

amrExport.requestInterstitialAd =
function(options, successCallback, failureCallback) {
	  if(typeof options === 'undefined' || options == null) options = {};
  cordova.exec(
      successCallback,
      failureCallback,
      'Amr',
      'requestInterstitialAd',
      [ options ]
  );
};

amrExport.showAd = 
function( show, successCallback, failureCallback) {
	if (show === undefined) {
		show = true;
	}

	cordova.exec(
		successCallback,
		failureCallback, 
		'Amr', 
		'showAd', 
		[ show ]
	);
};

amrExport.showInterstitialAd = 
	function( show, successCallback, failureCallback) {
		if (show === undefined) {
			show = true;
		}

		cordova.exec(
			successCallback,
			failureCallback, 
			'Amr', 
			'showInterstitialAd', 
			[ show ]
		);
	};

module.exports = amrExport;