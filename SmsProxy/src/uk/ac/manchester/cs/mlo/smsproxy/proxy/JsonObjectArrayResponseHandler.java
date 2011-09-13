package uk.ac.manchester.cs.mlo.smsproxy.proxy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.util.Log;

final class JsonObjectArrayResponseHandler extends
        AbstractStringParsingResponseHandler<JSONObject[]>
{
    private static final String TAG = JsonObjectArrayResponseHandler.class.getSimpleName();

    private final boolean mSkipInvalidObjects;

    public JsonObjectArrayResponseHandler()
    {
        this(true);
    } // JsonObjectArrayResponseHandler

    public JsonObjectArrayResponseHandler(final boolean ignoreInvalidObjects)
    {
        super();
        mSkipInvalidObjects = ignoreInvalidObjects;
    } // JsonObjectArrayResponseHandler

    @Override
    protected JSONObject[] parseContent(final String content) throws JSONException
    {
        Log.d(TAG, content);
        final JSONArray jsonArray = new JSONArray(content);
        final int numObjects = jsonArray.length();
        final JSONObject[] jsonObjectsBuffer = new JSONObject[numObjects];
        int nextObjectsIndex = 0;

        for (int arrayIndex = 0; arrayIndex < numObjects; arrayIndex++)
        {
            try
            {
                jsonObjectsBuffer[nextObjectsIndex++] = jsonArray.getJSONObject(arrayIndex);
            } // try
            catch (final JSONException e)
            {
                if (!mSkipInvalidObjects)
                {
                    SmsProxyManager.sendMessage("[E] Cannot parse JSON Object from server.");
                    throw e;
                } // if
                Log.d(TAG, "Ignoring invalid JSON object at index " + arrayIndex + ".", e);
            } // catch
        } // for

        // If some invalid JSON objects have been ignored, copy the buffer to a snug fitting array.
        if (nextObjectsIndex < numObjects)
        {
            final JSONObject[] jsonObjects = new JSONObject[nextObjectsIndex];
            System.arraycopy(jsonObjectsBuffer, 0 /* srcPos */, jsonObjects, 0 /* destPos */,
                    nextObjectsIndex);
            return jsonObjects;
        } // if

        return jsonObjectsBuffer;
    } // parseContent

} // JsonObjectArrayResponseHandler
