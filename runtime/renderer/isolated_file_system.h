// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#ifndef XWALK_EXTENSIONS_RENDERER_ISOLATE_FILE_SYSTEM_MODULE_H_
#define XWALK_EXTENSIONS_RENDERER_ISOLATE_FILE_SYSTEM_MODULE_H_

#include "xwalk/extensions/renderer/xwalk_module_system.h"

namespace xwalk {
namespace extensions{

class IsolatedFileSystem: public xwalk::extensions::XWalkNativeModule {
 public:
  IsolatedFileSystem(v8::Handle<v8::Context> context);
  virtual ~IsolatedFileSystem();
  static v8::Handle<v8::Context> GetV8Context();
  static v8::Persistent<v8::Context> v8_context_;

 private:
  virtual v8::Handle<v8::Object> NewInstance() OVERRIDE;

  // Return main frame window object according to the routing id.
  static void GetIsolatedFileSystem(const v8::FunctionCallbackInfo<v8::Value>&);
  static void GetFileEntry(const v8::FunctionCallbackInfo<v8::Value>&);

  // Return routing id of current render view according.
  static void GetCurrentRoutingIDCallback(
      const v8::FunctionCallbackInfo<v8::Value>&);

  v8::Persistent<v8::ObjectTemplate> object_template_;
  v8::Persistent<v8::Object> function_data_;
};

}  // namespace extensions
}  // namespace xwalk

#endif  // XWALK_EXTENSIONS_RENDERER_ISOLATE_FILE_SYSTEM_MODULE_H_
