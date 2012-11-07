package com.fairfax.test.waleed;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class NewsFeed implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@SerializedName("title")
	public String title;
	
	@SerializedName("rows")
	public List<NewsItem> items;
}
