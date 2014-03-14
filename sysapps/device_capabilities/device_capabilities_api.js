// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

// Implementation of the W3C's Device Capabilities API.
// http://www.w3.org/2012/sysapps/device-capabilities/

var internal = requireNative('internal');
internal.setupInternalExtension(extension);

var file_system_native = requireNative('file_system_natives');

var v8tools = requireNative('v8tools');
var common = requireNative('sysapps_common');
common.setupSysAppsCommon(internal, v8tools);

var Promise = requireNative('sysapps_promise').Promise;

var DeviceCapabilities = function() {
  common.BindingObject.call(this, common.getUniqueId());
  common.EventTarget.call(this);

  internal.postMessage("deviceCapabilitiesConstructor", [this._id]);

  this._addEvent("displayconnect");
  this._addEvent("displaydisconnect");
  this._addEvent("storageattach");
  this._addEvent("storagedetach");

  this._addMethodWithPromise("getAVCodecs", Promise);
  this._addMethodWithPromise("getCPUInfo", Promise);
  this._addMethodWithPromise("getDisplayInfo", Promise);
  this._addMethodWithPromise("getMemoryInfo", Promise);
  this._addMethodWithPromise("getStorageInfo", Promise);
};

DeviceCapabilities.prototype = new common.EventTargetPrototype();
DeviceCapabilities.prototype.constructor = DeviceCapabilities;

exports = new DeviceCapabilities();
/*exports.testFileSystem = function(path) {
  if (file_system_native.GetIsolatedFileSystem ) {
    var fs = file_system_native.GetIsolatedFileSystem(path);
      if (fs) {
        console.log("have fs");
      } else {
        console.log("no fs");
      }
  }
}*/
exports.registerFileSystem = function(path) {
  //extension.internal.sendSyncMessage(path);
  extension.postMessage(path);
  //var id = "sdfasfsfsfsdffsfsfasfsf";
  //var fs = file_system_native.GetIsolatedFileSystem(id);
}
exports.testFileSystem = function(id) {
  var fs = file_system_native.GetIsolatedFileSystem(id);
  return fs;
}
