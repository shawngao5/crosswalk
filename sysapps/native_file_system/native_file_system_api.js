// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

// Implementation of the W3C's Device Capabilities API.
// http://www.w3.org/2012/sysapps/device-capabilities/

var _promises = {};
var _next_promise_id = 0;

var Promise = requireNative('sysapps_promise').Promise;
var IsolatedFileSystem = requireNative('isolated_file_system');

var postMessage = function(msg) {
  var p = new Promise();

  _promises[_next_promise_id] = p;
  msg._promise_id = _next_promise_id.toString();
  _next_promise_id += 1;

  extension.postMessage(msg);
  return p;
};

function _isFunction(fn) {
  return !!fn && !fn.nodeName && fn.constructor != String
    && fn.constructor != RegExp && fn.constructor != Array
    && /function/i.test( fn + "" );
}

var NativeFileSystem = function() {
};

var requestNativeFileSystem = function(path, success, error) {
  var msg = new Object();
  msg.data = new Object();
  msg.data.path = path;
  msg.cmd = "requestNativeFileSystem";
  var p = postMessage(msg);
  console.log(JSON.stringify(msg));
  function get_file_system_id_sucess(data) {
    var fs = IsolatedFileSystem.getIsolatedFileSystem(data.file_system_id);
    console.log("fs: "+fs);
    console.log("data.file_system_id: "+data.file_system_id);
    /*
    fs.getDirectory = function (path, options, successCallback, errorCallback) {
      fs.root.getDirectory("root/"+path, options, successCallback, errorCallback);
    }
    console.log(fs.getDirectory);
    */
    success(fs);
  }
  p.then(get_file_system_id_sucess, error);
}

exports.testFileSystem = function(id) {
  var fs = file_system_native.GetIsolatedFileSystem(id);
  return fs;
}

NativeFileSystem.prototype = new Object();
NativeFileSystem.prototype.constructor = NativeFileSystem;
NativeFileSystem.prototype.requestNativeFileSystem = requestNativeFileSystem;

exports = new NativeFileSystem();

function handlePromise(msgObj) {
  if (msgObj.data.error) {
    if (_isFunction(_promises[msgObj._promise_id].reject)) {
      _promises[msgObj._promise_id].reject(msgObj.data);
    }
  } else {
    if (_isFunction(_promises[msgObj._promise_id].fulfill)) {
      _promises[msgObj._promise_id].fulfill(msgObj.data);
    }
  }

  delete _promises[msgObj._promise_id];
}

extension.setMessageListener(function(msgStr) {
    console.log("aaaaaaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbb")
    console.log(msgStr);
    console.log(JSON.stringify(msgStr));

  var msgObj = JSON.parse(msgStr);

  switch (msgObj.cmd) {
    case "requestNativeFileSystem_ret":
      handlePromise(msgObj);
    default:
      break;
  }
});
