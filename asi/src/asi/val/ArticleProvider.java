/***************************************************************************
    begin                : aug 08 2011
    copyright            : (C) 2011 by François de Metz
    email                : francois@2metz.fr
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import com.finchframework.finch.rest.RESTfulContentProvider;
import com.finchframework.finch.rest.ResponseHandler;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class ArticleProvider extends RESTfulContentProvider {

	public static final String AUTHORITY = "asi.val.articleprovider";

	private static final String ARTICLES_TABLE_NAME = "articles";

    private static final int ARTICLE = 1;

    private static final int ARTICLES = 2;

    static int DATABASE_VERSION = 2;

	private static final UriMatcher sUriMatcher;

	private DatabaseHelper mOpenHelper;
	private SQLiteDatabase mDb;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, "article", ARTICLE);
		sUriMatcher.addURI(AUTHORITY, "articles", ARTICLES);

	}

	@Override
	public boolean onCreate() {
		init();
		return true;
	}

	private void init() {
        mOpenHelper = new DatabaseHelper(getContext(), ARTICLES_TABLE_NAME, null);
        mDb = mOpenHelper.getWritableDatabase();
    }

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (sUriMatcher.match(uri) != ARTICLE) {
			throw new IllegalArgumentException("Unknown URI " + uri); }

		Log.d("ArticleProvider", "insert new result "+ uri);
		String url = values.get(Article.URL_NAME).toString();
		String id = Uri.parse(url).getQueryParameter("id");
		Log.d("ArticleProvider", "id "+ id);
		values.put(BaseColumns._ID, id);

        long rowId = mDb.insert(ARTICLES_TABLE_NAME, null, values);
        if (rowId > 0) {
        	Log.d("ArticleProvider", "rowId h"+ rowId);
            getContext().getContentResolver().notifyChange(uri, null);
            return uri;
        } else {
        	Log.d("ASI", "cannot insert "+ uri);
			return null;
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String where, String[] whereArgs,
			String sortOrder) {
		Cursor queryCursor;

	    int match = sUriMatcher.match(uri);
	    switch (match) {
	        case ARTICLE:
	        	// extract the requested url
	            String articleUrl = uri.
	                    getQueryParameter(Article.URL_PARAM_NAME);

	            // Part 1: get the cached version on the database
	            String select = Article.URL_NAME+
	                    " = '" +  articleUrl + "'";

	            Log.d("ArticleProvider", "url "+ articleUrl);

	            // quickly return already matching data
	            queryCursor =
	                    mDb.query(ARTICLES_TABLE_NAME, projection,
	                            select,
	                            whereArgs,
	                            null,
	                            null, sortOrder);

	            // make the cursor observe the requested query
	            queryCursor.setNotificationUri(
	                    getContext().getContentResolver(), uri);
	            // Part 2: get the latest version
	            asyncQueryRequest("article", articleUrl);
	            break;
	        default:
	            throw new IllegalArgumentException("unsupported uri: " + uri);
	    }
		return queryCursor;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Simple Helper to manipulate the database.
	 * This is mostly a boilerplate
	 */
	 private static class DatabaseHelper extends SQLiteOpenHelper {
	        private DatabaseHelper(Context context, String name,
	                                    SQLiteDatabase.CursorFactory factory)
	        {
	            super(context, name, factory, DATABASE_VERSION);
	        }

	        @Override
	        public void onCreate(SQLiteDatabase sqLiteDatabase) {
	            createTable(sqLiteDatabase);
	        }

	        private void createTable(SQLiteDatabase sqLiteDatabase) {
	            String qs = "CREATE TABLE " + ARTICLES_TABLE_NAME + " (" +
	                    BaseColumns._ID +
	                    " INTEGER PRIMARY KEY, " +
	                    Article.TITLE_NAME + " TEXT, " +
	                    Article.DESCRIPTION_NAME + " TEXT, " +
	                    Article.CONTENT_NAME + " TEXT, " +
	                    Article.URL_NAME + " TEXT);";
	            sqLiteDatabase.execSQL(qs);
	        }

	        @Override
	        public void onUpgrade(SQLiteDatabase sqLiteDatabase,
	                              int oldv, int newv)
	        {
	            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " +
	                    ARTICLES_TABLE_NAME + ";");
	            createTable(sqLiteDatabase);
	        }
	    }

	@Override
	public Uri insert(Uri uri, ContentValues cv, SQLiteDatabase db) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ResponseHandler newResponseHandler(String url) {
		return new AsiHandler(this, url);
	}

	class AsiHandler implements ResponseHandler {

		private RESTfulContentProvider provider;

		public AsiHandler(RESTfulContentProvider restfulProvider, String url) {
			this.provider = restfulProvider;
		}

		public void handleResponse(HttpResponse response, Uri uri)
				throws IOException {
			Log.d("ArticleProvider", "handle response "+ uri);
			// parse the result
            String content = parseContent(response.getEntity());
            // insert in the database
            String queryString = Article.URL_PARAM_NAME + "=" +
                    Uri.encode(uri.toString());
            Uri queryUri =
                Uri.parse(Article.ARTICLE_URI + "?" +
                    queryString);
			ContentValues c = new ContentValues();
			// TODO: add title, description, date and more ...
			c.put(Article.CONTENT_NAME, content);
			c.put(Article.URL_NAME, uri.toString());
			provider.insert(queryUri, c);
		}
		/**
		 * TODO: Copy all the mess of PageLoad
		 */
		private String parseContent(HttpEntity entity) {
			StringBuffer sb = new StringBuffer();
			BufferedReader in = null;
			try {
				in = new BufferedReader(
						new InputStreamReader(entity.getContent()));
				String ligneCodeHTML;
				boolean data = false;
				boolean start = false;
				// TODOS: manage videos
				int video_count = 0;

				while ((ligneCodeHTML = in.readLine()) != null) {
					ligneCodeHTML = " " + ligneCodeHTML;
					if (ligneCodeHTML.matches(".*class\\=\"contenu\\-html.*"))
						data = true;
					//Pour le forum
					// on ajoute les lignes typo contenant des informations +
					if (ligneCodeHTML.contains("bloc-bande-contenu"))
						start = true;
					if (ligneCodeHTML.contains("bloc-bande-vite"))
						start = true;
					// modifications
					if ((ligneCodeHTML.matches(".*class\\=\"typo-.*")) & (start)) {
						ligneCodeHTML = ligneCodeHTML.replaceAll("h1", "h2");
						if (ligneCodeHTML.contains("typo-titre"))
							ligneCodeHTML = "<br /><br />" + ligneCodeHTML;
						if (ligneCodeHTML.contains("typo-vite-titre"))
							ligneCodeHTML = ligneCodeHTML.replaceFirst("</a>",
									"</h2>");
						ligneCodeHTML = ligneCodeHTML.replaceFirst(
								"<a href=\".*typo-vite-titre\">",
								"<h2 class=\"typo-titre\">");
						sb.append(ligneCodeHTML);
						sb.append("\n");
					}
					// if (ligneCodeHTML.matches(".*class\\=\"bloc\\-contenu.*"))
					// data = true;
					if (ligneCodeHTML.matches(".*fin T.l.chargement.*"))
						data = false;
					if (ligneCodeHTML.matches(".*id\\=\"lire-suite-abo.*")) {
						data = false;
						sb.append(this.center("&gt; Pour lire la suite de cet article, vous devez vous <a href=\"http://www.arretsurimages.net/abonnements.php\">abonner à @si<a> &lt;"));
					}
					if (ligneCodeHTML
							.matches(".*action\\=\"\\/recherche\\.php\".*"))
						data = false;
					if (ligneCodeHTML
							.matches(".*\\<div id\\=\"footer-contenu\"\\>.*"))
						data = false;

					if (data) {
						// on arrête de prendre les contenus typos
						if (start) {
							start = false;
							// sb.append("<div style=\"text-align:justify;\">\n");
						}
						ligneCodeHTML = ligneCodeHTML.replaceAll("(<br />)+",
								"<br />");
						// ligneCodeHTML = ligneCodeHTML.replaceAll("<br />",
						// "<br />--");
						ligneCodeHTML = ligneCodeHTML.replaceAll("<td.*?>", "<p>");
						ligneCodeHTML = ligneCodeHTML.replaceAll("</td>", "</p>");

						// on remplace le lien dailymotion vers celui pour l'iphone
						ligneCodeHTML = ligneCodeHTML.replaceAll(
								"www.dailymotion.com/video",
								"iphone.dailymotion.com/video");

						// on enlève les animations flash et recupère les vidéos
						// iphone
						if (ligneCodeHTML.matches(".*iphone\\.dailymotion\\.com.*")) {
							VideoUrl video = new VideoUrl();
							String s = video.parseToUrl(ligneCodeHTML);
							if (s == null)
								;
							// ligneCodeHTML = this
							// .center("<span\">&gt; Problème de lecture de la balise vidéo &lt;</span>");
							else {
								video_count++;
								video.setNumber(video_count);
								//videos.add(video);
								ligneCodeHTML = video.getHrefLinkUrl();
							}
						}
						// on cherche les fichiers mp3
						// on enlève la vidéo flash
						// <object type="application/x-shockwave-flash" </object>
						if (ligneCodeHTML.matches(".*\\<object.*\\<\\/object\\>.*")) {
							// lecture des extraits audios
							Pattern p = Pattern
									.compile(".*value\\=\"mp3\\=(.*?)\\&.*");
							Matcher m = p.matcher(ligneCodeHTML);
							if (m.matches()) {
								String mp3 = m.group(1).replaceAll("%2F", "/");
								ligneCodeHTML = this
										.center("<a href=\""
												+ mp3
												+ "\" target=\"_blank\">&gt; Écouter l'extrait audio &lt;</a>");
							} else {
								ligneCodeHTML = this
										.center("<span>&gt; Cette vidéo n'est pas visible sur Android &lt;</span>");
							}

						}

						// on enlève le bouton télécharger
						if (ligneCodeHTML
								.matches(".*boutons\\/bouton-telecharger\\.png.*"))
							ligneCodeHTML = "";

						// on réduit la fenêtre vide vidéo flash<object width="480"
						// height="360"><param name="movie"
						ligneCodeHTML = ligneCodeHTML.replaceAll(
								"width=\"680\" height=\"\\d+\"",
								"width=\"20\" height=\"20\"");
						// on indique que la vidéo de l'émission est en acte
						// <a href="/faq.php?id=7#7" target="_blank">nos
						// conseils</a>.</em></p>
						if (ligneCodeHTML.matches(".*faq\\.php.*nos conseils.*"))
							ligneCodeHTML = this
									.center("&gt; La vidéo de l'émission est accessible en actes en bas de l'article &lt;");
						// On enlève les grosses flèches et la structure des tables
						ligneCodeHTML = ligneCodeHTML.replaceAll(
								"<span class=\"regardez.*</span>", "");
						ligneCodeHTML = ligneCodeHTML.replaceAll(
								"<p class=\"regardez.*</p>", "");
						ligneCodeHTML = ligneCodeHTML
								.replaceFirst(
										"<img class=\"asiPictoFleche.*alt=\"picto\" />",
										"");
						ligneCodeHTML = ligneCodeHTML.replaceAll("<table.*>", "");
						ligneCodeHTML = ligneCodeHTML.replaceAll("</table>", "");
						ligneCodeHTML = ligneCodeHTML.replaceAll("<tbody>", "");
						ligneCodeHTML = ligneCodeHTML.replaceAll("</tbody>", "");
						ligneCodeHTML = ligneCodeHTML.replaceAll("</tr>", "");
						ligneCodeHTML = ligneCodeHTML.replaceAll("<tr>", "");

						sb.append(ligneCodeHTML);
						sb.append("\n");
						// Log.d("code",ligneCodeHTML);
					}
					// ligneCodeHTML = in.readLine();
				}
			} catch (Exception e) {
				e.printStackTrace();
				this.center(e.getMessage());
			} finally {
				// always close the input stream
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			if (sb.toString().equalsIgnoreCase(""))
				return (this.center("Problème de connexion au serveur : essayez de recharger l'article"));
			return sb.toString();
		}

		private String center(String S) {
			String S2 = "<p style=\"text-align: center;\">";
			S2 = S2 + S + "</p>";
			return (S2);
		}
	}
}
