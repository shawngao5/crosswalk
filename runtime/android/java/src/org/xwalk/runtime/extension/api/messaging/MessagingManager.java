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
import android.content.ContentResolver;  
import android.database.Cursor;  
import android.net.Uri; 
import android.content.ContentValues;
import android.util.Log; 
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import org.xwalk.runtime.extension.api.messaging.Messaging;
import org.xwalk.runtime.extension.api.messaging.MessagingHelpers;
import org.xwalk.runtime.extension.api.messaging.MessagingSmsConsts;
import org.xwalk.runtime.extension.api.messaging.MessagingSmsConstMaps;

public class MessagingManager {
    private static final String TAG = "Messaging";
    private Activity mainActivity;
    private Messaging messagingHandler;

    MessagingManager(Activity activity, Messaging messaging) {
        Log.d(TAG, "1");
        mainActivity = activity;
        Log.d(TAG, "2");
        messagingHandler = messaging;
        Log.d(TAG, "3");
    }

    private void onMsgGetMessage(JSONObject jsonMsg) {
        String id = null;
        JSONObject eventBody = null;
        String msgType = null;
        String messageID = null;

        Log.d("xwalk", "onMsgGetMessage");
        
        try {
            id = jsonMsg.getString("id");
            eventBody = jsonMsg.getJSONObject("body");
            messageID = eventBody.getString("messageID");
            msgType = eventBody.getString("type");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        String selString = String.format("%s = %s", MessagingSmsConsts.ID, messageID);

        ContentResolver cr = mainActivity.getContentResolver();
        Uri contentUri = null;
        if (msgType.equals("sms")) {
            contentUri = Uri.parse("content://sms");
        }
        else if (msgType.equals("mms")) {
            contentUri = Uri.parse("content://mms");
        }
        else
        {
            Log.e("xwalk", "msgType:"+msgType);
            contentUri = Uri.parse("content://sms");
        }

        Log.d("xwalk", "selString:"+selString);

        Cursor cursor = cr.query(contentUri, null, selString, null, null);

        Log.d("xwalk", String.format("Count = %d", cursor.getCount()));

        JSONObject jsonMsgRet = null;
        JSONArray results = null;
        try {
            jsonMsgRet = new JSONObject();
            results = new JSONArray(); 
            jsonMsgRet.put("id", id);
            jsonMsgRet.put("method", "msg_getMessage_ret");
            JSONObject jsBody = new JSONObject();
            jsonMsgRet.put("body", jsBody);
            jsBody.put("results", results);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        
        if (msgType.equals("mms")) {
            //FIXME(shawn)
        }
        else {
            if (cursor.getCount() > 0) {
                String count = Integer.toString(cursor.getCount());
                while (cursor.moveToNext()){
                    JSONObject jsonSmsObj = MessagingHelpers.SmsMessageCursor2Json(cursor);
                    if (null != jsonSmsObj) {
                        results.put(jsonSmsObj);
                    }
                }
            }
        }
        

        cursor.close();

        try {
            jsonMsgRet.put("resultCode", "RESULT_OK");
        } catch (JSONException e) {
         e.printStackTrace();
        }

        messagingHandler.broadcastMessage(jsonMsgRet.toString());
    }

    private void onMsgDeleteMessage(JSONObject jsonMsg) {
        String id = null;
        JSONObject eventBody = null;
        String msgType = null;
        String messageID = null;

        Log.d("xwalk", "onMsgDeleteMessage");
        
        try {
            id = jsonMsg.getString("id");
            eventBody = jsonMsg.getJSONObject("body");
            messageID = eventBody.getString("messageID");
            msgType = eventBody.getString("type");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        String selString = String.format("%s = %s", MessagingSmsConsts.ID, messageID);

        ContentResolver cr = mainActivity.getContentResolver();
        Uri contentUri = null;
        if (msgType.equals("sms")) {
            contentUri = Uri.parse("content://sms");
        }
        else if (msgType.equals("mms")) {
            contentUri = Uri.parse("content://mms");
        }
        else
        {
            Log.e("xwalk", "msgType:"+msgType);
            contentUri = Uri.parse("content://sms");
        }

        Log.d("xwalk", "selString:"+selString);

        int deleteRows = cr.delete(contentUri, selString, null);

        Log.d("xwalk", "deleteRows:"+deleteRows);

        JSONObject jsonMsgRet = null;
        try {
            jsonMsgRet = new JSONObject();
            jsonMsgRet.put("id", id);
            jsonMsgRet.put("method", "msg_deleteMessage_ret");
            JSONObject jsBody = new JSONObject();
            jsonMsgRet.put("body", jsBody);
            jsBody.put("deleteRows", deleteRows);
            jsonMsgRet.put("resultCode", "RESULT_OK");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        messagingHandler.broadcastMessage(jsonMsgRet.toString());

    }

    private void onMsgDeleteConversation(JSONObject jsonMsg) {
        String id = null;
        JSONObject eventBody = null;
        String msgType = null;
        String conversationID = null;

        Log.d("xwalk", "onMsgDeleteConversation");
        
        try {
            id = jsonMsg.getString("id");
            eventBody = jsonMsg.getJSONObject("body");
            conversationID = eventBody.getString("conversationID");
            msgType = eventBody.getString("type");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        String selString = String.format("%s = %s", MessagingSmsConsts.THREAD_ID, conversationID);

        ContentResolver cr = mainActivity.getContentResolver();
        Uri contentUri = null;
        if (msgType.equals("sms")) {
            contentUri = Uri.parse("content://sms");
        }
        else if (msgType.equals("mms")) {
            contentUri = Uri.parse("content://mms");
        }
        else
        {
            Log.e("xwalk", "msgType:"+msgType);
            contentUri = Uri.parse("content://sms");
        }

        Log.d("xwalk", "selString:"+selString);

        int deleteRows = cr.delete(contentUri, selString, null);

        Log.d("xwalk", "deleteRows:"+deleteRows);

        JSONObject jsonMsgRet = null;
        try {
            jsonMsgRet = new JSONObject();
            jsonMsgRet.put("id", id);
            jsonMsgRet.put("method", "msg_deleteConversation_ret");
            JSONObject jsBody = new JSONObject();
            jsonMsgRet.put("body", jsBody);
            jsBody.put("deleteRows", deleteRows);
            jsonMsgRet.put("resultCode", "RESULT_OK");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        messagingHandler.broadcastMessage(jsonMsgRet.toString());

    }

    private void onMsgMarkMessageRead(JSONObject jsonMsg) {
        String id = null;
        JSONObject eventBody = null;
        String msgType = null;
        String messageID = null;
        boolean isRead = false;

        Log.d("xwalk", "onMsgMarkMessageRead");
        
        try {
            id = jsonMsg.getString("id");
            eventBody = jsonMsg.getJSONObject("body");
            messageID = eventBody.getString("messageID");
            msgType = eventBody.getString("type");
            isRead = eventBody.getBoolean("value");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        String selString = String.format("%s = %s", MessagingSmsConsts.ID, messageID);

        ContentResolver cr = mainActivity.getContentResolver();
        Uri contentUri = null;
        if (msgType.equals("sms")) {
            contentUri = Uri.parse("content://sms");
        }
        else if (msgType.equals("mms")) {
            contentUri = Uri.parse("content://mms");
        }
        else
        {
            Log.e("xwalk", "msgType:"+msgType);
            contentUri = Uri.parse("content://sms");
        }

        Log.d("xwalk", "selString:"+selString);

        ContentValues values = new ContentValues();
        values.put("read", isRead?"1":"0"); 

        int updateRows = cr.update(contentUri, values, selString, null);
        Log.d("xwalk", "updateRows:"+updateRows);

        JSONObject jsonMsgRet = null;
        try {
            jsonMsgRet = new JSONObject();
            jsonMsgRet.put("id", id);
            jsonMsgRet.put("method", "msg_markMessageRead_ret");
            JSONObject jsBody = new JSONObject();
            jsonMsgRet.put("body", jsBody);
            jsBody.put("updateRows", updateRows);
            jsonMsgRet.put("resultCode", "RESULT_OK");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        messagingHandler.broadcastMessage(jsonMsgRet.toString());
    }

    private void onMsgMarkConversationRead(JSONObject jsonMsg) {
        String id = null;
        JSONObject eventBody = null;
        String msgType = null;
        String conversationID = null;
        boolean isRead = false;

        Log.d("xwalk", "onMsgMarkConversationRead");
        
        try {
            id = jsonMsg.getString("id");
            eventBody = jsonMsg.getJSONObject("body");
            conversationID = eventBody.getString("conversationID");
            msgType = eventBody.getString("type");
            isRead = eventBody.getBoolean("value");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        String selString = String.format("%s = %s", MessagingSmsConsts.THREAD_ID, conversationID);

        ContentResolver cr = mainActivity.getContentResolver();
        Uri contentUri = null;
        if (msgType.equals("sms")) {
            contentUri = Uri.parse("content://sms");
        }
        else if (msgType.equals("mms")) {
            contentUri = Uri.parse("content://mms");
        }
        else
        {
            Log.e("xwalk", "msgType:"+msgType);
            contentUri = Uri.parse("content://sms");
        }

        Log.d("xwalk", "selString:"+selString);

        ContentValues values = new ContentValues();
        values.put("read", isRead?"1":"0"); 

        int updateRows = cr.update(contentUri, values, selString, null);
        Log.d("xwalk", "updateRows:"+updateRows);

        JSONObject jsonMsgRet = null;
        try {
            jsonMsgRet = new JSONObject();
            jsonMsgRet.put("id", id);
            jsonMsgRet.put("method", "msg_markConversationRead_ret");
            JSONObject jsBody = new JSONObject();
            jsonMsgRet.put("body", jsBody);
            jsBody.put("updateRows", updateRows);
            jsonMsgRet.put("resultCode", "RESULT_OK");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        messagingHandler.broadcastMessage(jsonMsgRet.toString());
    }
}