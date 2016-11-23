/* 
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/

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

/**
 * Request an Amr ad.  This call should not be made until after the banner
 * view has been successfully created.
 *
 * @param {!Object} options The options used to request an ad.  They should
 *        be specified similar to the following.
 *
 *        {
 *          'isTesting': true|false,
 *          'extras': {
 *            'key': 'value'
 *          }
 *        }
 *
 *        isTesting is a boolean determining whether or not to request a
 *        test ad on an emulator, and extras represents the extras to pass
 *        into the request. If no options are passed, the request will have
 *        testing set to false and an empty extras.
 * @param {function()} successCallback The function to call if an ad was
 *        requested successfully.
 * @param {function()} failureCallback The function to call if an ad failed
 *        to be requested.
 */

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

/**
 * Request an Amr interstitial ad.  This call should not be made until after the banner
 * view has been successfully created.
 *
 * @param {!Object} options The options used to request an ad.  They should
 *        be specified similar to the following.
 *
 *        {
 *          'isTesting': true|false,
 *          'extras': {
 *            'key': 'value'
 *          }
 *        }
 *
 *        isTesting is a boolean determining whether or not to request a
 *        test ad on an emulator, and extras represents the extras to pass
 *        into the request. If no options are passed, the request will have
 *        testing set to false and an empty extras.
 * @param {function()} successCallback The function to call if an ad was
 *        requested successfully.
 * @param {function()} failureCallback The function to call if an ad failed
 *        to be requested.
 */

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

/*
 * Show or hide Ad.
 * 
 * @param {boolean} show true to show, false to hide.  
 * @param {function()} successCallback The function to call if an ad was
 *        requested successfully.
 * @param {function()} failureCallback The function to call if an ad failed
 *        to be requested.
 */
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
