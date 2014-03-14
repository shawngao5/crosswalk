// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "xwalk/sysapps/device_capabilities/device_capabilities_extension.h"

#include "grit/xwalk_sysapps_resources.h"
#include "ui/base/resource/resource_bundle.h"
#include "xwalk/sysapps/device_capabilities/device_capabilities.h"
#include "xwalk/sysapps/device_capabilities/device_capabilities_object.h"

#include "content/public/browser/browser_thread.h"
#include "content/public/browser/child_process_security_policy.h"
#include "webkit/browser/fileapi/isolated_context.h"

namespace xwalk {
namespace sysapps {
namespace experimental {

using jsapi::device_capabilities::DeviceCapabilitiesConstructor::Params;

DeviceCapabilitiesExtension::DeviceCapabilitiesExtension(content::RenderProcessHost* host) {
  host_ = host;
  set_name("xwalk.experimental.system");
  set_javascript_api(ResourceBundle::GetSharedInstance().GetRawDataResource(
      IDR_XWALK_SYSAPPS_DEVICE_CAPABILITIES_API).as_string());
}

DeviceCapabilitiesExtension::~DeviceCapabilitiesExtension() {}

XWalkExtensionInstance* DeviceCapabilitiesExtension::CreateInstance() {
  return new DeviceCapabilitiesInstance(host_);
}

DeviceCapabilitiesInstance::DeviceCapabilitiesInstance(content::RenderProcessHost* host)
  : handler_(this),
    store_(&handler_) {
  host_ = host;
  //handler_.Register("deviceCapabilitiesConstructor",
  //    base::Bind(&DeviceCapabilitiesInstance::OnDeviceCapabilitiesConstructor,
  //               base::Unretained(this)));
}

void DeviceCapabilitiesInstance::HandleMessage(scoped_ptr<base::Value> msg) {
//  handler_.HandleMessage(msg.Pass());
//}

//void DeviceCapabilitiesInstance::HandleSyncMessage(scoped_ptr<base::Value> msg) {
  base::ListValue* args;
  LOG(WARNING) << "[ccccccccccccyyyyyyyyyyyyyyyyyyyyyy] GetType: " << (int)msg->GetType();
  std::string method;
  if (!msg->GetAsString(&method)) {
    LOG(WARNING) << "The method is not a string.";
    return;
  }
  LOG(WARNING) << "[xxxxxxxxxxxxxxxxxxxx]The method is "<<method;

/*
  if (!msg->GetAsList(&args) || args->GetSize() < 1) {
    // FIXME(tmpsantos): This warning could be better if the Context had a
    // pointer to the Extension. We could tell what extension sent the
      fileapi::kFileSystemTypeIsolated, base::FilePath::FromUTF8Unsafe(path),
    // invalid message.
    LOG(WARNING) << "Invalid number of arguments."<<args->GetSize();
    return;
  }

  LOG(WARNING) << "[yyyyyyyyyyyyyyyyyyyyyy]The method is ";
  // The first parameter stands for the function signature.
  std::string function_name;
  if (!args->GetString(0, &function_name)) {
    LOG(WARNING) << "The function name is not a string.";
    return;
  }

  LOG(WARNING) << "[dddddddddddddyyyyyyyyyyyy]The method is ";
  // The second parameter stands for callback id, the remaining
  // ones are the function arguments.
  std::string callback_id;
  if (!args->GetString(1, &callback_id)) {
    LOG(WARNING) << "The callback id is not a string.";
    return;
  }
*/
}



void DeviceCapabilitiesInstance::OnDeviceCapabilitiesConstructor(
    scoped_ptr<XWalkExtensionFunctionInfo> info) {
  scoped_ptr<Params> params(Params::Create(*info->arguments()));

  scoped_ptr<BindingObject> obj(new DeviceCapabilitiesObject());
  store_.AddBindingObject(params->object_id, obj.Pass());
}


}  // namespace experimental
}  // namespace sysapps
}  // namespace xwalk
