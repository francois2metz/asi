/***************************************************************************
    begin                : aug 01 2010
    copyright            : (C) 2010 by Benoit Valot
    email                : benvalot@gmail.com
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 23 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

package asi.val;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.content.ContentUris;
import android.net.Uri;

public class Article {

	public static final Uri ARTICLE_URI =
            Uri.parse("content://"+ ArticleProvider.AUTHORITY +"/article");

	public static final Uri ARTICLES_URI = Uri.parse("content://"+ ArticleProvider.AUTHORITY +"/articles");

	/**
	 * Title of the article
	 */
	public static final String TITLE_NAME = "name";
	
	/**
	 * Color of the article
	 */
	public static final String COLOR_NAME = "color";
	
	/**
	 * Date of the article
	 */
	public static final String DATE_NAME = "date";

	/**
	 * Sample/description of the content. Provided by the RSS feed.
	 */
	public static final String DESCRIPTION_NAME = "description";

	/**
	 * Article content
	 */
	public static final String CONTENT_NAME = "content";

	/**
	 * URL of this article
	 */
	public static final String URL_NAME = "url";
	
	/**
	 * Read/unread
	 */
	public static final String READ_NAME = "read";

	private String title;

	private String description;

	private String uri;

	private String date;

	private String color;

	public Article(String t, String d, String u) {
		this.title = t;
		this.description = d;
		this.uri = u;
		this.color=null;
	}

	public Article() {
		this.title = "T";
		this.description = "d";
		this.uri = "";
		this.date = "";
		this.color=null;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}
	
	public static Uri createUriFor(long id) {
        return ContentUris.withAppendedId(Article.ARTICLE_URI, id);
	}
	
	public static Uri createUriFor(String url) {
		long id = Integer.parseInt(Uri.parse(url).getQueryParameter("id"));
		return createUriFor(id);
	}
	
	public static String parseDescription(String des) {
		des = des.replaceAll("\n", "");
		String[] parse = des.split("<br />");
		if(parse.length > 1) {
			des = parse[1];
		}
		int fin = des.length();
		if (fin > 100)
			fin = 100;
		des = des.substring(0, fin);
		des = des.replaceFirst(" \\w+$", "");
		des = des + " ...";
		return des;
	}
	
	public static String parseColor(String des) {
		String[] parse = des.split("<br />");
		if(parse.length > 1) {
			return determinedColor(parse[0]);
		}
		return null;
	}

	public void setDescription(String des) {
		des = des.replaceAll("\n", "");
		String[] parse = des.split("<br />");
		if(parse.length>1){
			this.determined_color(parse[0]);
			des=parse[1];
		}
		int fin = des.length();
		if (fin > 100)
			fin = 100;
		des = des.substring(0, fin);
		des = des.replaceFirst(" \\w+$", "");
		des = des + " ...";
		this.description = des;
	}
	
	protected static String determinedColor(String title) {
		if(title.contains("Vite dit"))
			return "#FEC763";
		else if(title.contains("chronique"))
			return "#FF398E";
		else if(title.contains("mission"))
			return "#3A36FF";
		else if(title.contains("Article"))
			return "#3399FF";
		return null;
	}

	private void determined_color(String title) {
		this.color = Article.determinedColor(title);
	}

	public void setDescription_on_recherche(String html) {
		html = html.replaceAll("\n", "");
		html = html.replaceAll("\\s+", " ");

		int fin = html.length();
		if (fin > 100)
			fin = 100;
		html = html.substring(0, fin);
		html = html.replaceFirst(" \\w+$", "");
		html = html + " ...";
		this.description = html;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public static long parseDate(String date) {
		// <pubDate>Tue, 31 Aug 2010 19:37:08 +0200</pubDate>
		SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		try {
			return formatter.parse(date).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public void setDate(String dat) {
		// <pubDate>Tue, 31 Aug 2010 19:37:08 +0200</pubDate>
		dat = dat.replaceAll("\\+0\\d+", "");
		this.date = dat.replaceFirst("^.*, ", "");
	}

	public String getDate() {
		return date;
	}

	public void setColor(String color2) {
		if (this.color == null)
			this.color = color2;
	}

	public String getColor() {
		if(this.color == null)
			return("#ACB7C6");
		return color;
	}

	public void setColorFromSearch(String rec) {
		if(rec.contains("vite"))
			this.color = "#FEC763";
		else if(rec.contains("chro"))
			this.color = "#FF398E";
		else if(rec.contains("emi"))
			this.color = "#3A36FF";
		else if(rec.contains("doss"))
			this.color = "#3399FF";
	}

}
