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

import org.xwalk.runtime.extension.api.messaging.MessagingSmsManager;
//import org.xwalk.runtime.extension.SmsConsts;
//import org.xwalk.runtime.extension.api.MMSMonitor;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import java.util.Map;
import java.util.HashMap;

import android.content.ContentValues;

import android.util.Log; 

public class Messaging extends XWalkExtension {
    public static final String NAME = "navigator.messaging";
    public static final String JS_API_PATH = "jsapi/messaging_api.js";
    private static final String TAG = "Messaging";

    private MessagingSmsManager smsManager;


    public Messaging(String jsApiContent, XWalkExtensionContext context) {
        super(NAME, jsApiContent, context);
        smsManager = new MessagingSmsManager(mExtensionContext.getActivity(), this);
        smsManager.Init(); //TDOD:(shawn.gao) When onStart and OnStop are ready. This should be moved to onStart.
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

            if (cmd.equals("msg_smsSend")) {
                smsManager.onSmsSend(jsonMsg);
            }
        }
    //     if (!message.isEmpty()) {
    //         try {
    //             JSONObject jsonInput = new JSONObject(message);
    //             String cmd = jsonInput.getString("cmd");

    //             JSONObject jsonOutput = new JSONObject();
    //             jsonOutput.put("_promise_id", jsonInput.getString("_promise_id"));
    //             if (cmd.equals("save")) {
    //                 jsonOutput.put("data", mBuilder.init(jsonInput.getString("contact")).build());
    //             }
    //             this.postMessage(instanceID, jsonOutput.toString());
    //         } catch (JSONException e) {
    //             Log.e(TAG, e.toString());
    //         }
    //     }
    }
}