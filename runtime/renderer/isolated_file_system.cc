// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "xwalk/runtime/renderer/isolated_file_system.h"

#include "base/logging.h"
#include "content/public/renderer/render_view.h"
#include "content/public/renderer/v8_value_converter.h"
#include "third_party/WebKit/public/platform/WebFileSystem.h"
#include "third_party/WebKit/public/platform/WebFileSystemType.h"
#include "third_party/WebKit/public/platform/WebString.h"
#include "third_party/WebKit/public/web/WebDataSource.h"
#include "third_party/WebKit/public/web/WebDocument.h"
#include "third_party/WebKit/public/web/WebDOMError.h"
#include "third_party/WebKit/public/web/WebDOMFileSystem.h"
#include "third_party/WebKit/public/web/WebFrame.h"
#include "third_party/WebKit/public/web/WebView.h"
#include "webkit/common/fileapi/file_system_types.h"
#include "webkit/common/fileapi/file_system_util.h"


#include "third_party/WebKit/public/web/WebFrame.h"
#include "third_party/WebKit/public/web/WebScopedMicrotaskSuppression.h"
#include "third_party/WebKit/public/web/WebView.h"

using content::RenderView;
using blink::WebFrame;
using blink::WebView;

v8::Persistent<v8::Context> xwalk::extensions::IsolatedFileSystem::v8_context_;

namespace xwalk {
namespace extensions{

namespace {

// This is the key used in the data object passed to our callbacks to store a
// pointer back to IsolatedFileSystem.
const char* kIsolatedFileSystemModule = "kIsolatedFileSystemModule";


}  // namespace


void IsolatedFileSystem::GetIsolatedFileSystem(
    const v8::FunctionCallbackInfo<v8::Value>& info) {
  DCHECK(info.Length() == 1 || info.Length() == 2);
  DCHECK(info[0]->IsString());
  std::string file_system_id(*v8::String::Utf8Value(info[0]));
  blink::WebFrame* webframe =
      blink::WebFrame::frameForContext(GetV8Context());
  DCHECK(webframe);

  blink::WebDataSource* data_source = webframe->provisionalDataSource() ?
      webframe->provisionalDataSource() : webframe->dataSource();
  CHECK(data_source);
  GURL context_url(data_source->request().url());

  std::string name(fileapi::GetIsolatedFileSystemName(context_url.GetOrigin(),
                                                      file_system_id));


  // The optional second argument is the subfolder within the isolated file
  // system at which to root the DOMFileSystem we're returning to the caller.
  std::string optional_root_name = "";

  blink::WebURL root(GURL(fileapi::GetIsolatedFileSystemRootURIString(
      context_url.GetOrigin(),
      file_system_id,
      optional_root_name)));

  info.GetReturnValue().Set(blink::WebDOMFileSystem::create(webframe,
      blink::WebFileSystemTypeIsolated,
      blink::WebString::fromUTF8(name),
      root).toV8Value());
/*  info.GetReturnValue().Set(webframe->createFileSystem(
      blink::WebFileSystemTypeIsolated,
      blink::WebString::fromUTF8(name),
      blink::WebString::fromUTF8(root)));
      */
}

void IsolatedFileSystem::GetCurrentRoutingIDCallback(
    const v8::FunctionCallbackInfo<v8::Value>& info) {
  blink::WebFrame* webframe = blink::WebFrame::frameForCurrentContext();
  DCHECK(webframe);

  blink::WebView* webview = webframe->view();
  DCHECK(webview);

  content::RenderView* render_view = content::RenderView::FromWebView(webview);
  info.GetReturnValue().Set(render_view->GetRoutingID());
}

IsolatedFileSystem::IsolatedFileSystem(v8::Handle<v8::Context> context) {
  v8::Isolate* isolate = context->GetIsolate();
  v8_context_.Reset(isolate, context);
  v8::HandleScope handle_scope(isolate);
  v8::Handle<v8::Object> function_data = v8::Object::New(isolate);
  function_data->Set(
      v8::String::NewFromUtf8(isolate, kIsolatedFileSystemModule),
      v8::External::New(isolate, this));

  // Register native function templates to object template here.
  v8::Handle<v8::ObjectTemplate> object_template = v8::ObjectTemplate::New();
  object_template->Set(
      isolate,
      "getIsolatedFileSystem",
      v8::FunctionTemplate::New(isolate,
                                &IsolatedFileSystem::GetIsolatedFileSystem,
                                function_data));

  function_data_.Reset(isolate, function_data);
  object_template_.Reset(isolate, object_template);
}

IsolatedFileSystem::~IsolatedFileSystem() {
  object_template_.Reset();
  function_data_.Reset();
  v8_context_.Reset();
}

v8::Handle<v8::Object> IsolatedFileSystem::NewInstance() {
  v8::Isolate* isolate = v8::Isolate::GetCurrent();
  v8::EscapableHandleScope handle_scope(isolate);
  v8::Handle<v8::ObjectTemplate> object_template =
      v8::Local<v8::ObjectTemplate>::New(isolate, object_template_);
  return handle_scope.Escape(object_template->NewInstance());
}

v8::Handle<v8::Context> IsolatedFileSystem::GetV8Context() {
  return v8::Local<v8::Context>::New(v8::Isolate::GetCurrent(), v8_context_);
}

}  // namespace application
}  // namespace xwalk
