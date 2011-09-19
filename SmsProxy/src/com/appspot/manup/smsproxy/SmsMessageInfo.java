package com.appspot.manup.smsproxy;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.telephony.SmsMessage;

public final class SmsMessageInfo
{
    private static final String JSON_MESSAGE = "message";
    private static final String JSON_NUMBER = "number";

    public static SmsMessageInfo fromPdu(final byte[] pdu) throws IOException
    {
        return fromSmsMessage(SmsMessage.createFromPdu(pdu));
    } // fromPdu

    public static SmsMessageInfo fromSmsMessage(final SmsMessage message) throws IOException
    {
        try
        {
            return new SmsMessageInfo(message);
        } // try
        catch (final NullPointerException e)
        {
            throw new IOException("message is not valid");
        } // catch
    } // fromSmsMessage

    private final String mMessage;
    private final String mNumber;

    private SmsMessageInfo(final SmsMessage message) throws NullPointerException
    {
        this(message.getMessageBody(), message.getOriginatingAddress());
    } // SmsMessageInfo

    public SmsMessageInfo(final JSONObject jsonReply) throws JSONException
    {
       this(jsonReply.getString(JSON_MESSAGE), jsonReply.getString(JSON_NUMBER));
    } // SmsMessageInfo

    public SmsMessageInfo(final String message, final String number)
    {
        super();
        if (message == null || number == null)
        {
            throw new IllegalArgumentException("message and number must not be null");
        } // if
        mMessage = message;
        mNumber = number;
    } // SmsMessageInfo

    public String getMessage()
    {
        return mMessage;
    } // getMessage

    public String getNumber()
    {
        return mNumber;
    } // getNumber

} // SmsMessageInfo

