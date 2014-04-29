// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#ifndef XWALK_EXPERIMENTAL_NATIVE_FILE_SYSTEM_NATIVE_FILE_SYSTEM_EXTENSION_H_
#define XWALK_EXPERIMENTAL_NATIVE_FILE_SYSTEM_NATIVE_FILE_SYSTEM_EXTENSION_H_

#include <string>

#include "base/values.h"
#include "content/public/browser/render_process_host.h"
#include "xwalk/extensions/browser/xwalk_extension_function_handler.h"
#include "xwalk/extensions/common/xwalk_extension.h"

namespace xwalk {
namespace sysapps {
namespace experimental {

using extensions::XWalkExtension;
using extensions::XWalkExtensionFunctionHandler;
using extensions::XWalkExtensionFunctionInfo;
using extensions::XWalkExtensionInstance;

class NativeFileSystemExtension : public XWalkExtension {
 public:
  explicit NativeFileSystemExtension(content::RenderProcessHost* host);
  virtual ~NativeFileSystemExtension();

  // XWalkExtension implementation.
  virtual XWalkExtensionInstance* CreateInstance() OVERRIDE;
  content::RenderProcessHost* host_;
};

class NativeFileSystemInstance : public XWalkExtensionInstance {
 public:
  NativeFileSystemInstance(content::RenderProcessHost* host);

  // XWalkExtensionInstance implementation.
  virtual void HandleMessage(scoped_ptr<base::Value> msg) OVERRIDE;

 private:
  XWalkExtensionFunctionHandler handler_;
  content::RenderProcessHost* host_;
};

class WritableFileChecker
    : public base::RefCountedThreadSafe<WritableFileChecker> {
  public:
    WritableFileChecker(content::RenderProcessHost* host, XWalkExtensionInstance* instance);
    void DoTask();
    void RegisterFileSystemsAndSendResponse();

    content::RenderProcessHost* host_;
    std::string promise_id_;
    std::string path_;
    XWalkExtensionInstance* instance_;
};
}  // namespace experimental
}  // namespace sysapps
}  // namespace xwalk

#endif  //XWALK_EXPERIMENTAL_NATIVE_FILE_SYSTEM_NATIVE_FILE_SYSTEM_EXTENSION_H_
