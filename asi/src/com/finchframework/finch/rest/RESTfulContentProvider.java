package com.finchframework.finch.rest;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import asi.val.SharedData;

import com.finchframework.finch.Finch;
import org.apache.http.client.methods.HttpGet;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates functions for asynchronous RESTful requests so that subclass
 * content providers can use them for initiating request while still using
 * custom methods for interpreting REST based content such as, RSS, ATOM,
 * JSON, etc.
 */
public abstract class RESTfulContentProvider extends ContentProvider {
    private Map<String, UriRequestTask> mRequestsInProgress =
            new HashMap<String, UriRequestTask>();

    public abstract Uri insert(Uri uri, ContentValues cv, SQLiteDatabase db);    

    private UriRequestTask getRequestTask(String queryText) {
        return mRequestsInProgress.get(queryText);
    }

    public void requestComplete(String mQueryText) {
        synchronized (mRequestsInProgress) {
            mRequestsInProgress.remove(mQueryText);
        }
    }

    /**
     * Abstract method that allows a subclass to define the type of handler
     * that should be used to parse the response of a given request.
     *
     * @param requestTag unique tag identifying this request.
     * @return The response handler created by a subclass used to parse the
     * request response.
     */
    protected abstract ResponseHandler newResponseHandler(String requestTag);
    
    UriRequestTask newQueryTask(String requestTag, String url, ResponseHandler handler) {
        UriRequestTask requestTask;

        final HttpGet get = new HttpGet(url);
        // XXX: a beautiful hack to add cookies support
		String cookies = SharedData.shared.getCookies();
		get.addHeader("cookie", cookies);
		if (handler == null)
			handler = newResponseHandler(requestTag);
        requestTask = new UriRequestTask(requestTag, this, get,
                handler, getContext());

        mRequestsInProgress.put(requestTag, requestTask);
        return requestTask;
    }

    /**
     * Creates a new worker thread to carry out a RESTful network invocation.
     *
     * @param queryTag unique tag that identifies this request.
     *
     * @param queryUri the complete URI that should be access by this request.
     */
    public void asyncQueryRequest(String queryTag, String queryUri, ResponseHandler handler) {
        synchronized (mRequestsInProgress) {
            UriRequestTask requestTask = getRequestTask(queryTag);
            if (requestTask == null) {
                requestTask = newQueryTask(queryTag, queryUri, handler);
                Thread t = new Thread(requestTask);
                // allows other requests to run in parallel.
                t.start();
            }
        }
    }

    public static String encode(String gDataQuery) {
        try {
            return URLEncoder.encode(gDataQuery, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.d(Finch.LOG_TAG, "could not decode UTF-8," +
                    " this should not happen");
        }
        return null;
    }
}
