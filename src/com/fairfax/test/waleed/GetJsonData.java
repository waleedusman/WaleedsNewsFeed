package com.fairfax.test.waleed;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Base class for asynchronous tasks that communicate with the server using JSON request/response
 * @author wusman
 */
public abstract class GetJsonData<Params, Progress, Result> extends AsyncTask<String, Void, JsonElement> {
	private String responseEncoding = "UTF-8";
	
	@Override
	protected JsonElement doInBackground(String... params)
	{
		if (!isCancelled()) {
			InputStream source = retrieveStream(params[0]);
			if (source != null){
				try {
					JsonParser jParser = new JsonParser();
		    		return jParser.parse(new InputStreamReader(source, responseEncoding));
				} catch (UnsupportedEncodingException ex) {
					return null;
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	private InputStream retrieveStream(String url)
	{
		DefaultHttpClient client = new DefaultHttpClient();

		HttpGet getRequest = new HttpGet(url);

		try {
			HttpResponse response = client.execute(getRequest);
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
				throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
			}
			
			HttpEntity responseEntity = response.getEntity();
			responseEncoding = EntityUtils.getContentCharSet(responseEntity);
			return responseEntity == null ? null : responseEntity.getContent();
		} catch (Exception e) {
			getRequest.abort();
			Log.w(getClass().getSimpleName(), "Error for URL " + url, e);
		}

		return null;
	}
}
