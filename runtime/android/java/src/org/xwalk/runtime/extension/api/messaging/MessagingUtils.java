// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.xwalk.runtime.extension.api.messaging;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduComposer;
import com.google.android.mms.pdu.PduPart;
import com.google.android.mms.pdu.SendReq;

import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.StatusLine;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.Locale;

class MessagingUtils {
    private static final String TAG = "MessagingUtils";

    protected static final int COLUMN_MMSC = 13;
    protected static final int COLUMN_MMSPORT = 12;
    protected static final int COLUMN_MMSPROXY = 11;
    protected static final int COLUMN_TYPE = 15;
    protected static final int MAX_MESSAGE_SIZE = 300 * 1024;

    protected static final String APN_TYPE_MMS = "mms";
    protected static final String CONNECTION_ENABLE_MMS = "enableMMS";
    protected static final String CURRENT_APN_URI = "content://telephony/carriers/current";

    protected static final String ACCEPT_LANG_FOR_US_LOCALE = "en-US";
    protected static final String HDR_KEY_ACCEPT = "Accept";
    protected static final String HDR_KEY_ACCEPT_LANGUAGE = "Accept-Language";
    protected static final String HDR_KEY_USER_AGENT = "user-agent";
    protected static final String HDR_VALUE_ACCEPT =
            "*/*, application/vnd.wap.mms-message, application/vnd.wap.sic";
    protected static final String HDR_VALUE_ACCEPT_LANGUAGE;
    static {
        HDR_VALUE_ACCEPT_LANGUAGE = getCurrentAcceptLanguage(Locale.getDefault());
    }
    protected static final String HDR_VALUE_DEFUALT_USER_AGENT = "Android-Mms/2.0";

    public MessagingUtils() {
    }

    protected static boolean isValidString(String str) {
        return str != null && str.trim().length() > 0;
    }

    protected static String trimV4AddrZeros(String addr) {
        if (addr == null) {
            return null;
        }

        String[] octets = addr.split("\\.");
        if (octets.length != 4) {
            return addr;
        }

        StringBuilder builder = new StringBuilder(16);
        String result = null;
        for (int i = 0; i < 4; i++) {
            try {
                if (octets[i].length() > 3) {
                    return addr;
                }
                builder.append(Integer.parseInt(octets[i]));
            } catch (NumberFormatException e) {
                Log.e(TAG, e.toString());
                return addr;
            }
            if (i < 3) {
                builder.append('.');
            }
        }
        result = builder.toString();
        return result;
    }

    protected static byte[] sendMmsMessaging(Context context, byte[] pdu, String mmscUrl,
            String proxyHost, int proxyPort) throws IOException {
        if (mmscUrl == null) {
            throw new IllegalArgumentException("URL must not be null.");
        }

        HttpClient client = null;
        try {
            HttpHost httpHost = new HttpHost(proxyHost, proxyPort);
            HttpParams httpParams = new BasicHttpParams();
            httpParams.setParameter(ConnRouteParams.DEFAULT_PROXY, httpHost);
            HttpConnectionParams.setConnectionTimeout(httpParams, 10000);

            client = new DefaultHttpClient(httpParams);
            HttpPost post = new HttpPost(mmscUrl);

            ByteArrayEntity byteArray = new ByteArrayEntity(pdu);
            byteArray.setContentType("application/vnd.wap.mms-message");
            post.setEntity(byteArray);
            post.addHeader(HDR_KEY_ACCEPT, HDR_VALUE_ACCEPT);
            post.addHeader(HDR_KEY_ACCEPT_LANGUAGE, HDR_VALUE_ACCEPT_LANGUAGE);
            post.addHeader(HDR_KEY_USER_AGENT, HDR_VALUE_DEFUALT_USER_AGENT);

            HttpParams params = client.getParams();
            HttpProtocolParams.setContentCharset(params, "UTF-8");

            HttpResponse response = client.execute(post);
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() != 200) {
                Log.d(TAG, "HTTP status 200! failed!");
                throw new IOException("HTTP error: " + status.getReasonPhrase());
            }

            HttpEntity entity = response.getEntity();
            byte[] body = null;
            if (entity != null) {
                try {
                    if (entity.getContentLength() > 0) {
                        body = new byte[(int) entity.getContentLength()];
                        DataInputStream dis = new DataInputStream(entity.getContent());
                        try {
                            dis.readFully(body);
                        } finally {
                            try {
                                dis.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Error closing input stream: " + e.getMessage());
                            }
                        }
                    }
                    if (entity.isChunked()) {
                        Log.v(TAG, "httpConnection: transfer encoding is chunked");
                        int bytesTobeRead = MAX_MESSAGE_SIZE;
                        byte[] tempBody = new byte[bytesTobeRead];
                        DataInputStream dis = new DataInputStream(entity.getContent());
                        try {
                            int bytesRead = 0;
                            int offset = 0;
                            boolean readError = false;
                            do {
                                try {
                                    bytesRead = dis.read(tempBody, offset, bytesTobeRead);
                                } catch (IOException e) {
                                    readError = true;
                                    Log.e(TAG, "httpConnection: error reading input stream"
                                        + e.getMessage());
                                    break;
                                }
                                if (bytesRead > 0) {
                                    bytesTobeRead -= bytesRead;
                                    offset += bytesRead;
                                }
                            } while (bytesRead >= 0 && bytesTobeRead > 0);
                            if (bytesRead == -1 && offset > 0 && !readError) {
                                // offset is same as total number of bytes read
                                // bytesRead will be -1 if the data was read till the eof
                                body = new byte[offset];
                                System.arraycopy(tempBody, 0, body, 0, offset);
                                Log.v(TAG, "httpConnection: Chunked response length ["
                                    + Integer.toString(offset) + "]");
                            } else {
                                Log.e(TAG, "httpConnection: Response entity too large or empty");
                            }
                        } finally {
                            try {
                                dis.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Error closing input stream: " + e.getMessage());
                            }
                        }
                    }
                } finally {
                    if (entity != null) {
                        entity.consumeContent();
                    }
                }
            }
            return body;
        } catch (IllegalStateException e) {
            handleHttpConnectionException(e, mmscUrl);
        } catch (IllegalArgumentException e) {
            handleHttpConnectionException(e, mmscUrl);
        } catch (SocketException e) {
            handleHttpConnectionException(e, mmscUrl);
        } catch (Exception e) {
            handleHttpConnectionException(e, mmscUrl);
        }
        return null;
    }

    protected static void handleHttpConnectionException(
            Exception exception, String url) throws IOException {
        Log.e(TAG, "Url: " + url + "\n" + exception.getMessage());
        IOException e = new IOException(exception.getMessage());
        e.initCause(exception);
        throw e;
    }

    protected static String getCurrentAcceptLanguage(Locale locale) {
        StringBuilder buffer = new StringBuilder();
        addLocaleToHttpAcceptLanguage(buffer, locale);

        if (!Locale.US.equals(locale)) {
            if (buffer.length() > 0) {
                buffer.append(", ");
            }
            buffer.append(ACCEPT_LANG_FOR_US_LOCALE);
        }

        return buffer.toString();
    }

    protected static String convertObsoleteLanguageCodeToNew(String langCode) {
        if (langCode == null) {
            return null;
        }
        if ("iw".equals(langCode)) {
            // Hebrew
            return "he";
        } else if ("in".equals(langCode)) {
            // Indonesian
            return "id";
        } else if ("ji".equals(langCode)) {
            // Yiddish
            return "yi";
        }
        return langCode;
    }

    protected static void addLocaleToHttpAcceptLanguage(StringBuilder builder, Locale locale) {
        String language = convertObsoleteLanguageCodeToNew(locale.getLanguage());
        if (language != null) {
            builder.append(language);
            String country = locale.getCountry();
            if (country != null) {
                builder.append("-");
                builder.append(country);
            }
        }
    }

    static class MmsInfomation {
        private Context mContext;
        private PduBody mPduBody;

        private int mPartCount = 1;
        private String mBccNumber;
        private String mCcNumber;
        private String mSmil;
        private String mSubject;
        private String mToNumber;

        MmsInfomation(Context context, String subject, String toNum,
                String ccNum, String bccNum, String smil) {
            mContext = context;
            mSubject = subject;
            mToNumber = toNum;
            mCcNumber = ccNum;
            mBccNumber = bccNum;
            mSmil = smil;
            mPduBody = new PduBody();
        }

        void addImagePart(String uriString) {
            PduPart part = new PduPart();
            part.setCharset(CharacterSets.UTF_8);
            part.setName(("Image:" + mPartCount++).getBytes());
            part.setContentType("image/jpg".getBytes());
            part.setDataUri(Uri.parse(uriString));
            mPduBody.addPart(part);
        }

        byte[] convertToPduBytes() {
            PduComposer pduComposer = new PduComposer(mContext, initSendReq());
            return pduComposer.make();
        }

        private SendReq initSendReq() {
            SendReq sendReq = new SendReq();

            EncodedStringValue[] subject = EncodedStringValue.extract(mSubject);
            if (subject != null && subject.length > 0) {
                sendReq.setSubject(subject[0]);
            }

            EncodedStringValue[] toNum = EncodedStringValue.extract(mToNumber);
            if (toNum != null && toNum.length > 0) {
                sendReq.setTo(toNum);
            }

            EncodedStringValue[] ccNum = EncodedStringValue.extract(mCcNumber);
            if (ccNum != null && ccNum.length > 0) {
                sendReq.setCc(ccNum);
            }

            EncodedStringValue[] bccNum = EncodedStringValue.extract(mBccNumber);
            if (bccNum != null && bccNum.length > 0) {
                sendReq.setBcc(bccNum);
            }

            byte[] smil = mSmil.getBytes();
            if (smil != null && smil.length > 0) {
                sendReq.setMessageClass(smil);
            }

            sendReq.setBody(mPduBody);
            return sendReq;
        }
    }

}
