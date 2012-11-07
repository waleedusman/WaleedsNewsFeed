package com.fairfax.test.waleed;

import java.io.Reader;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class Main extends ListActivity {
	private static final String FEED_URL = "http://dl.dropbox.com/u/10168342/facts.json";
	private static final String NEWS_FEED = "DATA";
	
	/** stores the data to be displayed so that it doesn't need to be re-initialised during
	 *  a context switch (orientation change, incoming phone call, etc). */
	private NewsFeed newsFeed = null;
	
	private GetNewsFeed newsRetriever = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Only show the splash screen when the activity is first opened.
        // Otherwise initialise any 'saved state'
        if (savedInstanceState == null) {
        	showDialog(R.id.dialog_progress);
        } else {
        	newsFeed = (NewsFeed) savedInstanceState.get(NEWS_FEED);
        }
        
        if (getLastNonConfigurationInstance() != null) {
        	BitmapManager.setInstance((BitmapManager) getLastNonConfigurationInstance());
        }

        if (newsFeed == null)
        {
        	// Initiate background worker thread to retrieve <code>NewsFeed</code> data.
        	newsRetriever = new GetNewsFeed();
            newsRetriever.execute(FEED_URL);
        } else {
        	setupView();
        }
    }

    private void setupView() {
    	if (newsFeed != null) {
    		setTitle(newsFeed.title);
    		NewsArrayAdapter adapter = new NewsArrayAdapter(this, newsFeed.items);
    		// filter out any news item that don't meet the display criteria
    		// e.g. if title, description, and imageHref are all null.
    		adapter.getFilter().filter("");
    		setListAdapter(adapter);
    	}
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	if (newsRetriever != null && newsRetriever.getStatus() == AsyncTask.Status.RUNNING) {
    		newsRetriever.cancel(true);
    		newsRetriever = null;
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (newsFeed != null) {
        	outState.putSerializable(NEWS_FEED, newsFeed);
        }
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {
    	// Retain the image cache during a context switch
    	return BitmapManager.getInstance();
    }
    
    @Override
    protected Dialog onCreateDialog(int id)
    {
    	Dialog dialog = null;
    	switch(id) {
    	case R.id.dialog_progress:
    		dialog = ProgressDialog.show(this, "Please wait", "Updating news list...");
           	dialog.setCancelable(false);
    		break;
    	default:
    		dialog = null;
    	}
    	return dialog;
    }

    // Asynchronous task for retrieving and parsing JSON data.
    private final class GetNewsFeed extends GetJsonData<String, Void, Reader>
    {
    	@Override
    	protected void onPreExecute()
    	{
    		// If a response has already been processed (i.e. a NewsFeed object has already been retrieved or an error has been recorded)
    		// then there is no need to execute this task.
    		if (newsFeed != null) {
				cancel(true);
    		}
    	}

    	@Override
    	protected void onPostExecute(JsonElement jResponse)
    	{
    		if (jResponse == null)
    		{
    			Toast.makeText(getApplicationContext(), "Unable to fetch event details from server", Toast.LENGTH_SHORT).show();
    			return;
    		}
    		
    		Gson gson = new Gson();
    		newsFeed = gson.fromJson(jResponse, NewsFeed.class);
        	setupView();
        	removeDialog(R.id.dialog_progress);
        }
    }
}

