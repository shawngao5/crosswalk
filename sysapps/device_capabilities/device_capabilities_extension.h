// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#ifndef XWALK_SYSAPPS_DEVICE_CAPABILITIES_DEVICE_CAPABILITIES_EXTENSION_H_
#define XWALK_SYSAPPS_DEVICE_CAPABILITIES_DEVICE_CAPABILITIES_EXTENSION_H_

#include <string>

#include "base/values.h"
#include "content/public/browser/render_process_host.h"
#include "xwalk/extensions/browser/xwalk_extension_function_handler.h"
#include "xwalk/extensions/common/xwalk_extension.h"
#include "xwalk/sysapps/common/binding_object_store.h"

namespace xwalk {
namespace sysapps {
namespace experimental {

using extensions::XWalkExtension;
using extensions::XWalkExtensionFunctionHandler;
using extensions::XWalkExtensionFunctionInfo;
using extensions::XWalkExtensionInstance;

class DeviceCapabilitiesExtension : public XWalkExtension {
 public:
  explicit DeviceCapabilitiesExtension(content::RenderProcessHost* host);
  virtual ~DeviceCapabilitiesExtension();

  // XWalkExtension implementation.
  virtual XWalkExtensionInstance* CreateInstance() OVERRIDE;
  content::RenderProcessHost* host_;
};

class DeviceCapabilitiesInstance : public XWalkExtensionInstance {
 public:
  DeviceCapabilitiesInstance(content::RenderProcessHost* host);

  // XWalkExtensionInstance implementation.
  virtual void HandleMessage(scoped_ptr<base::Value> msg) OVERRIDE;
  //virtual void HandleSyncMessage(scoped_ptr<base::Value> msg);

 private:
  void OnDeviceCapabilitiesConstructor(
      scoped_ptr<XWalkExtensionFunctionInfo> info);

  XWalkExtensionFunctionHandler handler_;
  BindingObjectStore store_;
  content::RenderProcessHost* host_;
};


}  // namespace experimental
}  // namespace sysapps
}  // namespace xwalk

#endif  // XWALK_SYSAPPS_DEVICE_CAPABILITIES_DEVICE_CAPABILITIES_EXTENSION_H_
