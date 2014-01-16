// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#ifndef XWALK_SYSAPPS_SHARED_MEMORY_SHARED_MEMORY_EXTENSION_H_
#define XWALK_SYSAPPS_SHARED_MEMORY_SHARED_MEMORY_EXTENSION_H_

#include <string>
#include "base/values.h"
#include "xwalk/sysapps/common/binding_object_store.h"

namespace xwalk {
namespace sysapps {

using extensions::XWalkExtension;
using extensions::XWalkExtensionFunctionHandler;
using extensions::XWalkExtensionFunctionInfo;
using extensions::XWalkExtensionInstance;

class SharedMemoryExtension : public XWalkExtension {
 public:
  explicit SharedMemoryExtension();
  virtual ~SharedMemoryExtension();

  // XWalkExtension implementation.
  virtual XWalkExtensionInstance* CreateInstance() OVERRIDE;
};

class SharedMemoryInstance : public XWalkExtensionInstance {
 public:
  SharedMemoryInstance();

  // XWalkExtensionInstance implementation.
  virtual void HandleMessage(scoped_ptr<base::Value> msg) OVERRIDE;

 private:
  void OnRequestSharedMemoryAsync(
      scoped_ptr<XWalkExtensionFunctionInfo> info);
  void OnRequestSharedMemorySync(
      scoped_ptr<XWalkExtensionFunctionInfo> info);

  XWalkExtensionFunctionHandler handler_;
};

}  // namespace sysapps
}  // namespace xwalk

#endif  // XWALK_SYSAPPS_SHARED_MEMORY_SHARED_MEMORY_EXTENSION_H_
