/* 
 * Copyright 2014 Niels Snoeck
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * 
 * The CraftARError object encapsulates CraftAR API errors.  
 * 
 * @constructor 
 * @param {number} code
 *     The error code.
 * @param {string} message
 *     The error message.
 */
var CraftARError = function(code, message) {
	this.code = code || null;
	this.message = message || null;
}

/**
 * 
 * Error codes found in the CraftAR API
 */
CraftARError.SERVER_ERROR 				= 1;
CraftARError.TOKEN_WRONG				= 2;
CraftARError.TOKEN_INVALID				= 3;
CraftARError.IMAGE_NOT_LOADED			= 4;
CraftARError.IMAGE_NO_DETAILS			= 5;
CraftARError.IMAGE_TOO_LARGE			= 6;
CraftARError.IMAGE_TOO_SMALL			= 7;
CraftARError.IMAGE_TRANSPARENCY			= 8;
CraftARError.TOKEN_MISSING				= 9;
CraftARError.IMAGE_MISSING				= 10;
CraftARError.CONNECTION_ERROR			= 11;
CraftARError.INVALID_SERVER_RESPONSE	= 12;
CraftARError.INVALID_CRAFTAR_JSON		= 13;
CraftARError.UNKNOWN					= 14;

/**
 * 
 * Error codes added by the Cordova plugin
 */
CraftARError.CAPTURE_ERROR				= 15;
CraftARError.PLUGIN_ERROR				= 16;
CraftARError.CANCELED					= 17;

module.exports = CraftARError;