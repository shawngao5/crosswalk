// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "base/json/json_reader.h"
#include "base/json/json_writer.h"
#include "content/public/browser/browser_thread.h"
#include "content/public/browser/child_process_security_policy.h"
#include "grit/xwalk_sysapps_resources.h"
#include "ui/base/resource/resource_bundle.h"
#include "webkit/browser/fileapi/isolated_context.h"
#include "xwalk/sysapps/native_file_system/native_file_system_extension.h"

namespace xwalk {
namespace sysapps {
namespace experimental {

NativeFileSystemExtension::NativeFileSystemExtension(content::RenderProcessHost* host) {
  host_ = host;
  set_name("xwalk.experimental.native_file_system");
  set_javascript_api(ResourceBundle::GetSharedInstance().GetRawDataResource(
      IDR_XWALK_NATIVE_FILE_SYSTEM_API).as_string());
}

NativeFileSystemExtension::~NativeFileSystemExtension() {}

XWalkExtensionInstance* NativeFileSystemExtension::CreateInstance() {
  return new NativeFileSystemInstance(host_);
}

NativeFileSystemInstance::NativeFileSystemInstance(content::RenderProcessHost* host)
  : handler_(this) {
  host_ = host;
}

void NativeFileSystemInstance::HandleMessage(scoped_ptr<base::Value> msg) {
  base::DictionaryValue* msg_value = NULL;
  if (!msg->GetAsDictionary(&msg_value) || NULL == msg_value){
    LOG(ERROR) << "Message object should be a dictionary.";
    return;
  }
  std::string promise_id_string;
  if (!msg_value->GetString("_promise_id", &promise_id_string)) {
    LOG(ERROR) << "Invalide promise id.";
  }
  std::string cmd_string;
  if (!msg_value->GetString("cmd", &cmd_string) || "requestNativeFileSystem" != cmd_string) {
    LOG(ERROR) << "Invalide cmd: " << cmd_string;
    return;
  }
  std::string path_string;
  if (!msg_value->GetString("data.path", &path_string)) {
    LOG(ERROR) << "Invalide path: " << path_string;
    return;
  }

  WritableFileChecker* p = new WritableFileChecker(host_, this);
  p->path_ = path_string;
  p->promise_id_ = promise_id_string;
  p->DoTask();

}

WritableFileChecker::WritableFileChecker(content::RenderProcessHost* host, XWalkExtensionInstance* instance) {
  host_ = host;
  instance_ = instance;
}

void WritableFileChecker::DoTask( ) {
  content::BrowserThread::PostTask(
    content::BrowserThread::UI,
    FROM_HERE,
    base::Bind(&WritableFileChecker::RegisterFileSystemsAndSendResponse, this));
}

void WritableFileChecker::RegisterFileSystemsAndSendResponse() {
  DCHECK(content::BrowserThread::CurrentlyOn(content::BrowserThread::UI));
  fileapi::IsolatedContext* isolated_context =
      fileapi::IsolatedContext::GetInstance();
  DCHECK(isolated_context);
  
  std::string registered_name="root";
  std::string filesystem_id = isolated_context->RegisterFileSystemForPath(
      fileapi::kFileSystemTypeNativeForPlatformApp, base::FilePath::FromUTF8Unsafe(path_),
      &registered_name);
  LOG(ERROR) << "registered_name:" << registered_name;

  content::ChildProcessSecurityPolicy* policy =
      content::ChildProcessSecurityPolicy::GetInstance();
  policy->GrantCreateReadWriteFileSystem(host_->GetID(), filesystem_id);

  const scoped_ptr<base::DictionaryValue> res(new base::DictionaryValue());
  res->SetString("_promise_id", promise_id_);
  res->SetString("cmd", "requestNativeFileSystem_ret");
  res->SetString("data.file_system_id", filesystem_id);
  std::string msg_string;
  base::JSONWriter::Write(res.get(), &msg_string);
  instance_->PostMessageToJS(scoped_ptr<base::Value>(new base::StringValue(msg_string)));

}
}  // namespace experimental
}  // namespace sysapps
}  // namespace xwalk
