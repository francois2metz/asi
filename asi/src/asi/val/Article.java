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
import java.util.Locale;

import android.content.ContentUris;
import android.net.Uri;
import android.util.Log;

public class Article {

	/**
	 * Get one article by id
	 */
	public static final Uri ARTICLE_URI =
            Uri.parse("content://"+ ArticleProvider.AUTHORITY +"/article");

	/**
	 * Get articles by category
	 */
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

	/**
	 * Disable update
	 */
	public static final String UPDATE_PARAM = "update";
	
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
			// only available in the main rss
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

	/**
	 * TODO: Merge with parseDescription
	 */
	public static String parseDescriptionFromSearch(String html) {
		html = html.replaceAll("\n", "");
		html = html.replaceAll("\\s+", " ");

		int fin = html.length();
		if (fin > 100)
			fin = 100;
		html = html.substring(0, fin);
		html = html.replaceFirst(" \\w+$", "");
		html = html + " ...";
		return html;
	}
	
	public static String parseColor(String des) {
		String[] parse = des.split("<br />");
		if(parse.length > 1) {
			return determinedColor(parse[0]);
		}
		return null;
	}

	/**
	 * TODO: merge with determinedColor ?
	 */
	public static String parseColorFromSearch(String rec) {
		if (rec.contains("vite"))
			return "#FEC763";
		else if (rec.contains("chro"))
			return "#FF398E";
		else if (rec.contains("emi"))
			return "#3A36FF";
		else if(rec.contains("doss"))
			return "#3399FF";
		return null;
	}
	
	protected static String determinedColor(String title) {
		if (title.contains("Vite dit"))
			return "#FEC763";
		else if (title.contains("chronique"))
			return "#FF398E";
		else if (title.contains("mission"))
			return "#3A36FF";
		else if (title.contains("Article"))
			return "#3399FF";
		return null;
	}

	public static long parseDate(String date) {
		// <pubDate>Tue, 31 Aug 2010 19:37:08 +0200</pubDate>
		return parseDateWith("EEE, dd MMM yyyy HH:mm:ss zzz", date);
	}

	public static long parseDateFromSearch(String date) {
		// 13/05/2009
		return parseDateWith("dd/MM/yyyy", date);
	}
	
	protected static long parseDateWith(String formater, String date) {
		SimpleDateFormat formatter = new SimpleDateFormat(formater, Locale.US);
		try {
			return formatter.parse(date).getTime();
		} catch (ParseException e) {
			Log.e("ASI", "error when parsing date "+ e.getMessage());
			return 0;
		}
	}
}
