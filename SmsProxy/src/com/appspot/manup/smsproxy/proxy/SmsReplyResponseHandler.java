package com.appspot.manup.smsproxy.proxy;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.json.JSONException;
import org.json.JSONObject;

import com.appspot.manup.smsproxy.SmsMessageInfo;



final class SmsReplyResponseHandler implements ResponseHandler<SmsMessageInfo[]>
{
    private final JsonObjectArrayResponseHandler mJsonObjectArrayResponseHandler =
            new JsonObjectArrayResponseHandler(false);

    public SmsReplyResponseHandler()
    {
        super();
    } // SmsReplyResponseHandler

    @Override
    public SmsMessageInfo[] handleResponse(final HttpResponse response) throws IOException
    {
        final JSONObject[] jsonReplies = mJsonObjectArrayResponseHandler.handleResponse(response);
        final int numReplies = jsonReplies.length;
        final SmsMessageInfo[] replies = new SmsMessageInfo[numReplies];
        for (int replyIndex = 0; replyIndex < numReplies; replyIndex++)
        {
            try
            {
                replies[replyIndex] = new SmsMessageInfo(jsonReplies[replyIndex]);
            } // try
            catch (final JSONException e)
            {
                throw new IOException("Invalid JSON from server.");
            } // catch
        } // for
        return replies;
    } // handleResponse

} // SmsReplyResponseHandler
