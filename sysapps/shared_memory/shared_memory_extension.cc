// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "xwalk/sysapps/shared_memory/shared_memory_extension.h"

#include "grit/xwalk_sysapps_resources.h"
#include "ui/base/resource/resource_bundle.h"

//using namespace xwalk::jsapi::shared_memory; // NOLINT

namespace xwalk {
namespace sysapps {

SharedMemoryExtension::SharedMemoryExtension() {
	LOG(WARNING) << "SharedMemoryExtension::SharedMemoryExtension() {}";
  set_name("xwalk.experimental.shared_memory");
  set_javascript_api(ResourceBundle::GetSharedInstance().GetRawDataResource(
		  IDR_XWALK_SYSAPPS_SHARED_MEMORY_API).as_string());
}

SharedMemoryExtension::~SharedMemoryExtension() {}

XWalkExtensionInstance* SharedMemoryExtension::CreateInstance() {
  return new SharedMemoryInstance();
}

SharedMemoryInstance::SharedMemoryInstance()
  : handler_(this) {
  handler_.Register("RequestSharedMemoryAsync",
      base::Bind(&SharedMemoryInstance::OnRequestSharedMemoryAsync,
                 base::Unretained(this)));
  handler_.Register("RequestSharedMemorySync",
        base::Bind(&SharedMemoryInstance::OnRequestSharedMemorySync,
                   base::Unretained(this)));
}

void SharedMemoryInstance::HandleMessage(scoped_ptr<base::Value> msg) {
  handler_.HandleMessage(msg.Pass());
}

void SharedMemoryInstance::OnRequestSharedMemoryAsync(
    scoped_ptr<XWalkExtensionFunctionInfo> info) {
	LOG(WARNING) << "void SharedMemoryInstance::OnRequireSharedMemoryAsync(";
}

void SharedMemoryInstance::OnRequestSharedMemorySync(
    scoped_ptr<XWalkExtensionFunctionInfo> info) {
	LOG(WARNING) << "void SharedMemoryInstance::OnRequireSharedMemorySync(";
}

}  // namespace sysapps
}  // namespace xwalk
