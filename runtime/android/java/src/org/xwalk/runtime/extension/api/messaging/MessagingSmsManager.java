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
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import android.content.ContentValues;
import android.util.Log;

import org.xwalk.runtime.extension.api.messaging.Messaging;

public class MessagingSmsManager {
    private final static String TAG = "MessagingSmsManager";
    private final static String EXTRA_MSGID = "_promise_id";
    private final static String EXTRA_MSGTEXT = "message";
    private final static String EXTRA_MSGTO = "to";
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
        registerIntentFilters();
    }

    public void Uninit() {
        unregisterIntentFilters();
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
        intentSmsSent.putExtra(EXTRA_MSGTEXT, smsMessage);
        intentSmsSent.putExtra(EXTRA_MSGTO, phone);
        PendingIntent piSent=PendingIntent.getBroadcast(mainActivity, PendingIntent.FLAG_ONE_SHOT, intentSmsSent, PendingIntent.FLAG_ONE_SHOT);
        Intent intentSmsDelivered = new Intent("SMS_DELIVERED");
        intentSmsDelivered.putExtra(EXTRA_MSGID, _promise_id);
        intentSmsSent.putExtra(EXTRA_MSGTEXT, smsMessage);
        PendingIntent piDelivered=PendingIntent.getBroadcast(mainActivity, 0, intentSmsDelivered, 0);
        sms.sendTextMessage(phone, null, smsMessage, piSent, piDelivered);
    }

    public void onSmsSegmentInfo(JSONObject jsonMsg) {
        String _promise_id = null;
        JSONObject eventBody = null;
        String text = null;
        String serviceID = null;
        try {
            _promise_id = jsonMsg.getString("_promise_id");
            eventBody = jsonMsg.getJSONObject("data");
            text = eventBody.getString("text");
            if (eventBody.has("serviceID"))
                serviceID = eventBody.getString("serviceID");
            if (null != serviceID) {
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        if (null != text) {
            SmsManager sms=SmsManager.getDefault();
            ArrayList<String> segs = sms.divideMessage(text);
            try {
                JSONObject jsonMsgRet = new JSONObject();
                jsonMsgRet.put("cmd", "msg_smsSegmentInfo_ret");
                jsonMsgRet.put("_promise_id", _promise_id);
                JSONObject jsData = new JSONObject();
                jsonMsgRet.put("data", jsData);
                jsData.put("error", false);
                JSONObject jsBody = new JSONObject();
                jsData.put("body", jsBody);
                jsBody.put("segments", segs.size());
                jsBody.put("charsPerSegment", segs.get(0).length());
                jsBody.put("charsAvailableInLastSegment", segs.get(segs.size()-1).length());
                
                messagingHandler.broadcastMessage(jsonMsgRet.toString());

             } catch (JSONException e) {
                Log.e(TAG, e.toString());
                return;
            }
        }
        
    }

    private void registerIntentFilters() {
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
                            JSONObject jsMsg = new JSONObject();
                            jsData.put("message", jsMsg);
                            jsMsg.put("messageID", "");
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
                String smsMessage = intent.getStringExtra(EXTRA_MSGTEXT);
                String to = intent.getStringExtra(EXTRA_MSGTO);
                try {
                    JSONObject jsonMsg = new JSONObject();
                    jsonMsg.put("_promise_id", _promise_id);
                    jsonMsg.put("cmd", "msg_smsSend_ret");
                    JSONObject jsData = new JSONObject();
                    jsonMsg.put("data", jsData);
                    if (getResultCode() == Activity.RESULT_OK) {
                        jsData.put("error", false);
                    }
                    else {
                        jsData.put("error", true);
                    }
                    JSONObject jsBody = new JSONObject();
                    jsData.put("body", jsBody);
                    jsBody.put("type", "sms");
                    jsBody.put("from", "");
                    jsBody.put("read", true);
                    jsBody.put("to", to);
                    jsBody.put("body", smsMessage);
                    jsBody.put("messageClass", "class1");
                    jsBody.put("state", "sending");
                    jsBody.put("deliveryStatus", "pending");
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
                String smsMessage = intent.getStringExtra(EXTRA_MSGTEXT);
                String to = intent.getStringExtra(EXTRA_MSGTO);
                try {
                    JSONObject jsonMsg = new JSONObject();
                    jsonMsg.put("_promise_id", _promise_id);
                    jsonMsg.put("cmd", "msg_smsDeliver");
                    JSONObject jsData = new JSONObject();
                    jsonMsg.put("data", jsData);
                    JSONObject jsEvent = new JSONObject();
                    jsData.put("event", jsEvent);
                    jsEvent.put("serviceID", "");
                    jsEvent.put("messageID", "");
                    jsEvent.put("recipients", "");
                    SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    jsEvent.put("deliveryTimestamps", sDateFormat.format(new java.util.Date()));
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

    private void unregisterIntentFilters(){
        mainActivity.unregisterReceiver(smsReceiveReceiver);
        mainActivity.unregisterReceiver(smsSentReceiver);
        mainActivity.unregisterReceiver(smsDeliveredReceiver);
    }
}
