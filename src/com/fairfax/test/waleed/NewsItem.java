package com.fairfax.test.waleed;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public class NewsItem implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@SerializedName("title")
	public String title;
	
	@SerializedName("description")
	public String description;
	
	@SerializedName("imageHref")
	public String imageHref;
}
