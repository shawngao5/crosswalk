// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.xwalk.runtime.extension.api.messaging;

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

import org.xwalk.runtime.extension.api.messaging.Messaging;
//import org.xwalk.runtime.extension.SmsConsts;
//import org.xwalk.runtime.extension.api.MMSMonitor;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import java.util.Map;
import java.util.HashMap;

import android.content.ContentValues;

import android.util.Log;

public class MessagingSmsManager {
    private final static String TAG = "MessagingSmsManager";
    private final static String EXTRA_MSGID = "MSGID";
    private Activity mainActivity;
    private Messaging messagingHandler;
    private BroadcastReceiver smsSentReceiver, smsDeliveredReceiver, smsReceiveReceiver;

    private abstract class MessagingReceiver extends BroadcastReceiver {
        public Messaging messaging = null;

        public MessagingReceiver(Messaging _messaging){
            messaging = _messaging;
        }
    }

    MessagingSmsManager(Activity activity, Messaging messaging) {
        mainActivity = activity;
        messagingHandler = messaging;
    }

    public void Init(){
        registerIntentFilter();
    }

    public void Uninit() {
        unregisterIntentFilter();
    }

    public void onSmsSend(JSONObject jsonMsg) {
        String _promise_id = null;
        JSONObject eventBody = null;
        String phone = null;
        String smsMessage = null;
        String serviceID = null;
        try {
            _promise_id = jsonMsg.getString("_promise_id");
            eventBody = jsonMsg.getJSONObject("data");
            phone = eventBody.getString("phone");
            smsMessage = eventBody.getString("message");
            if (eventBody.has("serviceID"))
            serviceID = eventBody.getString("serviceID");
            if (null != serviceID) {
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        
        
        SmsManager sms=SmsManager.getDefault();
        Intent intentSmsSent = new Intent("SMS_SENT");
        intentSmsSent.putExtra(EXTRA_MSGID, _promise_id);
        PendingIntent piSent=PendingIntent.getBroadcast(mainActivity, PendingIntent.FLAG_ONE_SHOT, intentSmsSent, PendingIntent.FLAG_ONE_SHOT);
        Intent intentSmsDelivered = new Intent("SMS_DELIVERED");
        intentSmsDelivered.putExtra(EXTRA_MSGID, _promise_id);
        PendingIntent piDelivered=PendingIntent.getBroadcast(mainActivity, 0, intentSmsDelivered, 0);
        sms.sendTextMessage(phone, null, smsMessage, piSent, piDelivered);
    }

    private void registerIntentFilter() {
        smsReceiveReceiver=new MessagingReceiver(messagingHandler) {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();        
                
                if(null != bundle)
                {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    
                    for (int i=0; i<pdus.length; i++){
                        try {
                            JSONObject jsonMsg = new JSONObject();
                            jsonMsg.put("cmd", "msg_smsReceived");

                            SmsMessage msgs = SmsMessage.createFromPdu((byte[])pdus[i]);
                            
                            JSONObject jsData = new JSONObject();
                            jsonMsg.put("data", jsData);
                            JSONObject jsReceived = new JSONObject();
                            jsData.put("received", jsReceived);
                            JSONObject jsMsg = new JSONObject();
                            jsReceived.put("message", jsMsg);
                            jsMsg.put("type", "sms");
                            jsMsg.put("serviceID", "");
                            jsMsg.put("from", msgs.getOriginatingAddress());
                            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                            jsMsg.put("timestamp", sDateFormat.format(new java.util.Date()));
                            jsMsg.put("body", msgs.getMessageBody().toString());
                            
                            messaging.broadcastMessage(jsonMsg.toString());

                         } catch (JSONException e) {
                            Log.e(TAG, e.toString());
                            return;
                        }
                    }
                } 
            }
        };

        smsSentReceiver=new MessagingReceiver(messagingHandler) {

            @Override
            public void onReceive(Context content, Intent intent) {
                String _promise_id = intent.getStringExtra(EXTRA_MSGID);
                try {
                    JSONObject jsonMsg = new JSONObject();
                    jsonMsg.put("_promise_id", _promise_id);
                    jsonMsg.put("cmd", "msg_smsSend_ret");
                    JSONObject jsData = new JSONObject();
                    jsonMsg.put("data", jsData);
                    SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    jsData.put("timestamp", sDateFormat.format(new java.util.Date()));
                    if (getResultCode() == Activity.RESULT_OK) {
                        jsData.put("error", false);
                    }
                    else {
                        jsData.put("error", true);
                    }
                    messaging.broadcastMessage(jsonMsg.toString());
                } catch (JSONException e) {
                    Log.e(TAG, e.toString());
                    return;
                }
            }
        };

        smsDeliveredReceiver=new MessagingReceiver(messagingHandler) {

            @Override
            public void onReceive(Context content, Intent intent) {
                String _promise_id = intent.getStringExtra(EXTRA_MSGID);
                try {
                    JSONObject jsonMsg = new JSONObject();
                    jsonMsg.put("_promise_id", _promise_id);
                    jsonMsg.put("cmd", "msg_smsDeliver");
                    JSONObject jsData = new JSONObject();
                    jsonMsg.put("data", jsData);
                    SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    jsData.put("timestamp", sDateFormat.format(new java.util.Date()));
                    if (getResultCode() == Activity.RESULT_OK) {
                        jsData.put("error", false);
                    }
                    else {
                        jsData.put("error", true);
                    }
                    messaging.broadcastMessage(jsonMsg.toString());
                } catch (JSONException e) {
                    Log.e(TAG, e.toString());
                    return;
                }
            }
        };

        Intent intentReceived = mainActivity.registerReceiver(smsReceiveReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
        if (null == intentReceived) {
            Log.d(TAG, "register receive success");
        }
        else{
            Log.e(TAG, "register receive failed.");
        }
        Intent intentSent = mainActivity.registerReceiver(smsSentReceiver, new IntentFilter("SMS_SENT"));
        if (null == intentSent) {
            Log.d(TAG, "register sent success");
        }
        else{
            Log.e(TAG, "register sent failed.");
        }
        Intent intentDelivered = mainActivity.registerReceiver(smsDeliveredReceiver, new IntentFilter("SMS_DELIVERED"));
        if (null == intentDelivered) {
            Log.d(TAG, "register delivered success");
        }
        else{
            Log.e(TAG, "register delivered failed.");
        }
    }

    private void unregisterIntentFilter(){
        mainActivity.unregisterReceiver(smsReceiveReceiver);
        mainActivity.unregisterReceiver(smsSentReceiver);
        mainActivity.unregisterReceiver(smsDeliveredReceiver);
    }
}