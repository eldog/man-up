package com.appspot.manup.smsproxy.proxy;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.BasicResponseHandler;

abstract class AbstractStringParsingResponseHandler<T> implements ResponseHandler<T>
{
    private final BasicResponseHandler mBasicResponseHandler;

    public AbstractStringParsingResponseHandler()
    {
        super();
        mBasicResponseHandler = new BasicResponseHandler();
    } // JsonResponseHandler

    @Override
    public final T handleResponse(final HttpResponse response) throws HttpResponseException,
            IOException
    {
        try
        {
            return parseContent(getContent(response));
        }// catch
        catch (final HttpResponseException e)
        {
            throw e;
        }// catch
        catch (final IOException e)
        {
            throw e;
        }// catch
        catch (final Exception e)
        {
            throw new IOException(e);
        } // catch
    } // handleResponse

    private String getContent(final HttpResponse response) throws HttpResponseException,
            IOException
    {
        return mBasicResponseHandler.handleResponse(response);
    } // getContent

    protected abstract T parseContent(String content) throws Exception;

} // JsonResponseHandler
