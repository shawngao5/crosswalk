// Copyright (c) 2014 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "xwalk/runtime/browser/android/xwalk_path_helper.h"

#include "base/android/jni_android.h"
#include "base/android/jni_weak_ref.h"
#include "base/bind.h"
#include "jni/XWalkPathHelper_jni.h"
#include "xwalk/extensions/common/xwalk_extension.h"
#include "xwalk/runtime/browser/xwalk_browser_main_parts.h"
#include "xwalk/runtime/browser/xwalk_content_browser_client.h"

namespace xwalk {

typedef std::map<std::string, base::FilePath> VirtualRootMap;
VirtualRootMap XWalkPathHelper::virtual_root_map_;

XWalkPathHelper::XWalkPathHelper(JNIEnv* env, jobject obj) {
}

XWalkPathHelper::~XWalkPathHelper() {
}

VirtualRootMap XWalkPathHelper::GetVirtualRootMap() {
  return virtual_root_map_;
}

void XWalkPathHelper::SetDirectory(JNIEnv* env,
    jstring virtualRoot, jstring path) {
  const char* strVirtualRoot = env->GetStringUTFChars(virtualRoot, NULL);
  const char* strPath = env->GetStringUTFChars(path, NULL);
  virtual_root_map_[std::string(strVirtualRoot)] =
      base::FilePath::FromUTF8Unsafe(std::string(strPath));
  env->ReleaseStringUTFChars(virtualRoot, strVirtualRoot);
  env->ReleaseStringUTFChars(path, strPath);
}

jlong GetOrCreatePathHelper(JNIEnv* env, jobject obj) {
  XWalkPathHelper* helper = new XWalkPathHelper(env, obj);
  return reinterpret_cast<jint>(helper);
}

bool RegisterXWalkPathHelper(JNIEnv* env) {
  return RegisterNativesImpl(env) >= 0;
}

}  // namespace xwalk
