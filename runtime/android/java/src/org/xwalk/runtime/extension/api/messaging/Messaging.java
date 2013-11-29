// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.xwalk.runtime.extension.api.messaging;

import org.xwalk.runtime.extension.XWalkExtension;
import org.xwalk.runtime.extension.XWalkExtensionContext;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import android.content.ContentResolver;  
import android.database.Cursor;  
import android.net.Uri; 
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Map;
import java.util.HashMap;
import android.content.ContentValues;
import android.util.*; 

import org.xwalk.runtime.extension.api.messaging.MessagingSmsManager;
import org.xwalk.runtime.extension.api.messaging.MessagingManager;

interface Command {
    void runCommand(JSONObject jsonMsg);
}

public class Messaging extends XWalkExtension {
    public static final String NAME = "xwalk.experimental.messaging";
    public static final String JS_API_PATH = "jsapi/messaging_api.js";
    private static final String TAG = "Messaging";
    private static HashMap<String, Command> methodMap = new HashMap<String, Command>();

    private MessagingSmsManager smsManager;
    private MessagingManager messagingManager;

    private void initMethodMap() {
        try {
            methodMap.put("msg_smsSend", new Command() {
                public void runCommand(JSONObject jsonMsg) { smsManager.onSmsSend(jsonMsg); };
            });
            methodMap.put("msg_smsSegmentInfo", new Command() {
                public void runCommand(JSONObject jsonMsg) { smsManager.onSmsSegmentInfo(jsonMsg); };
            });
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }


    public Messaging(String jsApiContent, XWalkExtensionContext context) {
        super(NAME, jsApiContent, context);
        smsManager = new MessagingSmsManager(mExtensionContext.getActivity(), this);
        smsManager.Init(); //TDOD:(shawn.gao) When onStart and OnStop are ready. This should be moved to onStart.
        messagingManager = new MessagingManager(mExtensionContext.getActivity(), this);

        initMethodMap();
    }

    @Override
    public void onDestroy() {
        smsManager.Uninit(); //TDOD:(shawn.gao) When onStart and OnStop are ready. This should be moved to onStop.
    }

    @Override
    public void onMessage(int instanceID, String message) {
        if (!message.isEmpty()) {
            JSONObject jsonMsg = null;
            String cmd = null;

            try {
                jsonMsg  = new JSONObject(message);
                cmd = jsonMsg.getString("cmd");
                
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
                return;
            }

            try {
                Command command = methodMap.get(cmd);
                if (null != command) {
                    command.runCommand(jsonMsg);
                }
            } catch(Exception e) {
              throw new RuntimeException(e);
            }

            

            // if (cmd.equals("msg_smsSend")) {
            //     smsManager.onSmsSend(jsonMsg);
            // }
            // else if (cmd.equals("msg_smsSegmentInfo")) {
            //     smsManager.onSmsSegmentInfo(jsonMsg);
            // }
        }
    }
}