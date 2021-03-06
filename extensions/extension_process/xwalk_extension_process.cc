// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "xwalk/extensions/extension_process/xwalk_extension_process.h"

#include <string>

#include "base/command_line.h"
#include "base/files/file_path.h"
#include "base/message_loop/message_loop.h"
#include "ipc/ipc_switches.h"
#include "ipc/ipc_message_macros.h"
#include "ipc/ipc_sync_channel.h"
#include "xwalk/extensions/common/xwalk_extension_messages.h"

namespace xwalk {
namespace extensions {

XWalkExtensionProcess::XWalkExtensionProcess()
    : shutdown_event_(false, false),
      io_thread_("XWalkExtensionProcess_IOThread") {
  io_thread_.StartWithOptions(
      base::Thread::Options(base::MessageLoop::TYPE_IO, 0));

  CreateBrowserProcessChannel();
}

XWalkExtensionProcess::~XWalkExtensionProcess() {
  // FIXME(jeez): Move this to OnChannelClosing/Error/Disconnected when we have
  // our MessageFilter set.
  extensions_server_.Invalidate();

  shutdown_event_.Signal();
  io_thread_.Stop();
}

bool XWalkExtensionProcess::OnMessageReceived(const IPC::Message& message) {
  bool handled = true;
  IPC_BEGIN_MESSAGE_MAP(XWalkExtensionProcess, message)
    IPC_MESSAGE_HANDLER(XWalkExtensionProcessMsg_RegisterExtensions,
                        OnRegisterExtensions)
    IPC_MESSAGE_UNHANDLED(handled = false)
  IPC_END_MESSAGE_MAP()
  return handled;
}

namespace {

void ToValueMap(base::ListValue* lv, base::ValueMap* vm) {
  vm->clear();

  for (base::ListValue::iterator it = lv->begin(); it != lv->end(); it++) {
    DictionaryValue* dv;
    if (!(*it)->GetAsDictionary(&dv))
      continue;
    for (DictionaryValue::Iterator dit(*dv); !dit.IsAtEnd(); dit.Advance())
      (*vm)[dit.key()] = dit.value().DeepCopy();
  }
}

}  // namespace

void XWalkExtensionProcess::OnRegisterExtensions(
    const base::FilePath& path, const base::ListValue& browser_variables_lv) {
  if (!path.empty()) {
    base::ValueMap browser_variables;

    ToValueMap(&const_cast<base::ListValue&>(browser_variables_lv),
          &browser_variables);

    RegisterExternalExtensionsInDirectory(&extensions_server_, path,
                                          browser_variables);
  }
  CreateRenderProcessChannel();
}

void XWalkExtensionProcess::CreateBrowserProcessChannel() {
  std::string channel_id =
      CommandLine::ForCurrentProcess()->GetSwitchValueASCII(
          switches::kProcessChannelID);
  browser_process_channel_.reset(new IPC::SyncChannel(channel_id,
      IPC::Channel::MODE_CLIENT, this, io_thread_.message_loop_proxy(),
      true, &shutdown_event_));
}

void XWalkExtensionProcess::CreateRenderProcessChannel() {
  IPC::ChannelHandle handle(IPC::Channel::GenerateVerifiedChannelID(
      std::string()));
  rp_channel_handle_ = handle;

  render_process_channel_.reset(new IPC::SyncChannel(rp_channel_handle_,
      IPC::Channel::MODE_SERVER, &extensions_server_,
      io_thread_.message_loop_proxy(), true, &shutdown_event_));

#if defined(OS_POSIX)
    // On POSIX, pass the server-side file descriptor. We use
    // TakeClientFileDescriptor() instead of GetClientFileDescriptor()
    // since the client-side channel will take ownership of the fd.
    rp_channel_handle_.socket =
       base::FileDescriptor(render_process_channel_->TakeClientFileDescriptor(),
          true);
#endif

  extensions_server_.Initialize(render_process_channel_.get());

  browser_process_channel_->Send(
      new XWalkExtensionProcessHostMsg_RenderProcessChannelCreated(
          rp_channel_handle_));
}

}  // namespace extensions
}  // namespace xwalk
