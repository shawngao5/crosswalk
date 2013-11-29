// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.xwalk.runtime.extension.api.messaging;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;

import org.xwalk.runtime.extension.api.messaging.MessagingUtils.MmsInfomation;

class MessagingMmsManager {
    private static final String TAG = "MessagingMmsManager";

    private ConnectivityManager mConnManager;
    private ContentResolver mResolver;
    private Context mContext;
    private Messaging mMessaging;

    private boolean mIsMmsConnected = false;
    private int mMmsPort;
    private String mMmscUrl;
    private String mMmsProxy;

    public MessagingMmsManager(Messaging instance, Context context) {
        mMessaging = instance;
        mContext = context;
    }

    protected void sendMms(JSONObject jsonInput) {
        JSONObject jsonOutput = new JSONObject();
        JSONObject jsonOutputBody = new JSONObject();
        try {
            String promiseId = jsonInput.getString("_promise_id");
            jsonOutput.put("_promise_id", promiseId);
            jsonOutput.put("cmd", "msg_mmsSend_ret");

            if (!getMmsAPNInfo()) {
                jsonOutputBody.put("body", "get MMS APN failed!!!");
                jsonOutput.put("data", jsonOutputBody);
                mMessaging.broadcastMessage(jsonOutput.toString());
                return;
            }

            if (!beginMmsConnectivity()) {
                jsonOutputBody.put("body", "open wap connection failed!!!");
                jsonOutput.put("data", jsonOutputBody);
                mMessaging.broadcastMessage(jsonOutput.toString());
                return;
            }

            JSONObject jsonData = jsonInput.getJSONObject("data");
            JSONObject jsonMmsContent = jsonData.getJSONObject("mmsContent");
            String subject = jsonMmsContent.getString("subject");
            String to = jsonMmsContent.getString("to");
            String cc = jsonMmsContent.getString("cc");
            String bcc = jsonMmsContent.getString("bcc");
            String smil = jsonMmsContent.getString("smil");

            final MmsInfomation mmsInfo = new MmsInfomation(mContext, subject, to, cc, bcc, smil);
            JSONArray attachments = jsonMmsContent.getJSONArray("attachments");
            for (int i = 0; i < attachments.length(); ++i) {
                JSONObject attachmentUnit = attachments.getJSONObject(i);
                mmsInfo.addImagePart(attachmentUnit.getString("contentLocation"));
                //Log.i(TAG, attachmentUnit.getString("contentLocation"));
            }
            //Log.i(TAG, subject);
            //Log.i(TAG, to);
            //Log.i(TAG, cc);
            //Log.i(TAG, bcc);
            //Log.i(TAG, smil);

            Thread sendThread = new Thread() {
                public void run() {
                    try {
                        byte[] response = MessagingUtils.sendMmsMessaging(
                                mContext, mmsInfo.convertToPduBytes(),
                                mMmscUrl, mMmsProxy, mMmsPort);
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, e.toString());
                    } catch (IOException e) {
                        Log.e(TAG, e.toString());
                    }
                };
            };
            sendThread.start();
            jsonOutputBody.put("body", "mms sending!");
            jsonOutput.put("data", jsonOutputBody);
            mMessaging.broadcastMessage(jsonOutput.toString());
            endMmsConnectivity();
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
    }

    protected boolean beginMmsConnectivity() {
        if (mIsMmsConnected) {
            return true;
        }
        mConnManager =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (mConnManager == null) {
            return false;
        }

        NetworkInfo networkInfo = mConnManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS);
        if (networkInfo == null || !networkInfo.isAvailable()) {
            return false;
        }

        int result = mConnManager.startUsingNetworkFeature(
                ConnectivityManager.TYPE_MOBILE, MessagingUtils.CONNECTION_ENABLE_MMS);
        if (result == 0 || result == 1) {
            mIsMmsConnected = true;
            return true;
        }
        return false;
    }

    protected void endMmsConnectivity() {
        if (mConnManager == null || !mIsMmsConnected) {
            return;
        }
        mConnManager.stopUsingNetworkFeature(
                ConnectivityManager.TYPE_MOBILE, MessagingUtils.CONNECTION_ENABLE_MMS);
        mIsMmsConnected = false;
    }

    private boolean getMmsAPNInfo() {
        mResolver = mContext.getContentResolver();
        Cursor cursor = mResolver.query(
                Uri.parse(MessagingUtils.CURRENT_APN_URI), null, null, null, null);
        if (cursor == null) {
            Log.e(TAG, "Current APN is not found in database!");
            return false;
        }

        while (cursor.moveToNext()) {
            if (cursor.getString(MessagingUtils.COLUMN_TYPE).equals(MessagingUtils.APN_TYPE_MMS)) {
                String mmsc = cursor.getString(MessagingUtils.COLUMN_MMSC);
                if (mmsc == null) {
                    continue;
                }

                mMmscUrl = MessagingUtils.trimV4AddrZeros(mmsc.trim());
                mMmsProxy = MessagingUtils.trimV4AddrZeros(
                        cursor.getString(MessagingUtils.COLUMN_MMSPROXY));
                if (MessagingUtils.isValidString(mMmsProxy)) {
                    String port = cursor.getString(MessagingUtils.COLUMN_MMSPORT);
                    try {
                        mMmsPort = Integer.parseInt(port);
                    } catch (NumberFormatException e) {
                        Log.e(TAG, e.toString());
                        return false;
                    }
                }
            }
        }
        cursor.close();

        if (!MessagingUtils.isValidString(mMmscUrl)) {
            Log.e(TAG, "Current MMSC is not valid!");
            return false;
        }

        return true;
    }
}
