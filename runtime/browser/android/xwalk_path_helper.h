// Copyright (c) 2014 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#ifndef XWALK_RUNTIME_BROWSER_ANDROID_XWALK_PATH_HELPER_H_
#define XWALK_RUNTIME_BROWSER_ANDROID_XWALK_PATH_HELPER_H_

#include <jni.h>
#include <map>
#include <string>

#include "base/files/file_path.h"

namespace xwalk {

typedef std::map<std::string, base::FilePath> VirtualRootMap;

class XWalkPathHelper {
 public:
  XWalkPathHelper(JNIEnv* env, jobject obj);
  virtual ~XWalkPathHelper();

  void SetDirectory(JNIEnv* env,
                    jstring virutalRoot, jstring path);
  static VirtualRootMap GetVirtualRootMap();

 private:
  static VirtualRootMap virtual_root_map_;

  DISALLOW_COPY_AND_ASSIGN(XWalkPathHelper);
};

bool RegisterXWalkPathHelper(JNIEnv* env);

}  // namespace xwalk

#endif  // XWALK_RUNTIME_BROWSER_ANDROID_XWALK_PATH_HELPER_H_
