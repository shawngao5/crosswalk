// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

// Implementation of the W3C's Raw Socket API.
// http://www.w3.org/2012/sysapps/raw-sockets/

var v8tools = requireNative('v8tools');

var internal = requireNative('internal');
internal.setupInternalExtension(extension);

var common = requireNative('sysapps_common');
common.setupSysAppsCommon(internal, v8tools);

// Exported API.
exports.requestSharedMemory = function(){
	internal.postMessage("RequestSharedMemoryAsync", [], function (){console.log("SharedMemory callback");});
};