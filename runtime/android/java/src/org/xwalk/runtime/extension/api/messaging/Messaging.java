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
import android.util.Log; 

import org.xwalk.runtime.extension.api.messaging.MessagingSmsManager;

public class Messaging extends XWalkExtension {
    public static final String NAME = "xwalk.experimental.messaging";
    public static final String JS_API_PATH = "jsapi/messaging_api.js";
    private static final String TAG = "Messaging";

    private MessagingMmsManager mMmsManager;
    private MessagingSmsManager mSmsManager;

    public Messaging(String jsApiContent, XWalkExtensionContext context) {
        super(NAME, jsApiContent, context);
        mSmsManager = new MessagingSmsManager(context.getActivity(), this);
        mSmsManager.Init(); //TDOD:(shawn.gao) When onStart and OnStop are ready. This should be moved to onStart.
        mMmsManager = new MessagingMmsManager(this, context.getContext());
    }

    @Override
    public void onDestroy() {
        mSmsManager.Uninit(); //TDOD:(shawn.gao) When onStart and OnStop are ready. This should be moved to onStop.
        mMmsManager.endMmsConnectivity();
    }

    @Override
    public void onMessage(int instanceID, String message) {
        if (message.isEmpty()) {
            return;
        }

        try {
            JSONObject jsonMsg  = new JSONObject(message);
            String cmd = jsonMsg.getString("cmd");
            if (cmd.equals("msg_smsSend")) {
                mSmsManager.onSmsSend(jsonMsg);
            } else if (cmd.equals("msg_smsSegmentInfo")) {
                mSmsManager.onSmsSegmentInfo(jsonMsg);
            } else if (cmd.equals("msg_mmsSend")) {
                mMmsManager.sendMms(jsonMsg);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
    }
}
