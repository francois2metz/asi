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
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.finchframework.finch.rest.RESTfulContentProvider;
import com.finchframework.finch.rest.ResponseHandler;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * ContentProvider for Articles and categories
 * TODO: cap number of articles
 */
public class ArticleProvider extends RESTfulContentProvider {

	public static final String AUTHORITY = "asi.val.articleprovider";

	private static final String ARTICLES_TABLE_NAME = "articles";

	private static final String CATEGORIES_TABLE_NAME = "categories";

	private static final String CATEGORIES_ARTICLES_TABLE_NAME = "articles_categories";

	private static final int ARTICLE = 1;

	private static final int ARTICLES_BY_CATEGORY = 2;

	private static final int CATEGORIES = 3;

	private static final int CATEGORY_ID = 4;

	static int DATABASE_VERSION = 18;

	private static final UriMatcher sUriMatcher;

	private DatabaseHelper mOpenHelper;
	private SQLiteDatabase mDb;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, "article/#", ARTICLE);
		sUriMatcher.addURI(AUTHORITY, "articles/#", ARTICLES_BY_CATEGORY);
		sUriMatcher.addURI(AUTHORITY, "categories", CATEGORIES);
		sUriMatcher.addURI(AUTHORITY, "categories/#", CATEGORY_ID);
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
		throw new UnsupportedOperationException();
	}

	@Override
	public String getType(Uri arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (sUriMatcher.match(uri) != ARTICLE) {
			throw new IllegalArgumentException("Unknown URI " + uri); }
		long rowId = getIdFor(values.getAsString(Article.URL_NAME));
		if (rowId == -1) {
			rowId = mDb.insert(ARTICLES_TABLE_NAME, null, values);
		} else {
			mDb.update(ARTICLES_TABLE_NAME, values, Article.URL_NAME +" = ?", 
     			   new String[] { values.getAsString(Article.URL_NAME) });
		}
		// put the id for later usage
    	values.put(BaseColumns._ID, rowId);
        getContext().getContentResolver().notifyChange(ContentUris.withAppendedId(Article.ARTICLE_URI, rowId), null);
		return uri;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String where, String[] whereArgs,
			String sortOrder) {
		Cursor queryCursor;

	    int match = sUriMatcher.match(uri);
	    switch (match) {
	    	case CATEGORIES:
	    		queryCursor = queryCategories(projection, where, whereArgs, sortOrder);
	    		break;
	    	case CATEGORY_ID:
	    		queryCursor = queryCategory(uri, projection, whereArgs, sortOrder);
	    		break;
	    	case ARTICLES_BY_CATEGORY:
	    		queryCursor = queryArticlesByCategory(uri, where, sortOrder);
	    		break;
	        case ARTICLE:
	        	queryCursor = queryArticle(uri, projection, whereArgs, sortOrder);
	            break;
	        default:
	            throw new IllegalArgumentException("unsupported uri: " + uri);
	    }
		return queryCursor;
	}

	private Cursor queryArticle(Uri uri, String[] projection,
			String[] whereArgs, String sortOrder) {
		String articleId = uri.getPathSegments().get(1);
		// extract the requested URL
		String articleUrl = getArticleUrl(Integer.parseInt(articleId));

		// Part 1: get the cached version on the database
		String select = Article.URL_NAME +" = '" +  articleUrl + "'";

		Log.d("ArticleProvider", "url "+ articleUrl);

		// quickly return already matching data
		Cursor queryCursor =
		        mDb.query(ARTICLES_TABLE_NAME, projection,
		                select,
		                whereArgs,
		                null,
		                null, sortOrder);

		// make the cursor observe the requested query
		queryCursor.setNotificationUri(
		        getContext().getContentResolver(), uri);
		// Part 2: get the latest version
		asyncQueryRequest("article_"+ articleId, articleUrl, this.createArticleResponseHandler());
		return queryCursor;
	}

	private Cursor queryArticlesByCategory(Uri uri, String where, String sortOrder) {
		long catId = Integer.parseInt(uri.getPathSegments().get(1));
		String select2 = "SELECT a."+ BaseColumns._ID +", a."+
		        		Article.TITLE_NAME +", a."+
		        		Article.DESCRIPTION_NAME +", a."+
		        		Article.DATE_NAME + ", a."+
		        		Article.COLOR_NAME + ", a."+
		        		Article.URL_NAME + ", a."+
		        		Article.READ_NAME +
		        		" FROM " + ARTICLES_TABLE_NAME + " as a JOIN "+
		         CATEGORIES_ARTICLES_TABLE_NAME + " as c ON c.article = a._id WHERE c.category="+catId;
		if (where != null) {
			select2 += " and a."+ where;
		}
		if (sortOrder != null)
			select2 += " ORDER BY a."+ sortOrder;
		Cursor queryCursor = mDb.rawQuery(select2, null);
		// make the cursor observe the requested query
		queryCursor.setNotificationUri(
		        getContext().getContentResolver(), uri);
		// Part 2: get the latest version
		Cursor c = getCategory(catId);
		String defaultColor = c.getString(c.getColumnIndex(Category.COLOR_NAME));
		String url = c.getString(c.getColumnIndex(Category.URL_NAME));
		c.close();
		if ("1".equals(uri.getQueryParameter(Article.UPDATE_PARAM))) {
			Log.d("ASI", "quering articles without update");
			return queryCursor;
		}
		asyncQueryRequest("category_"+ catId, url, this.createRssResponseHandler(catId, defaultColor));
		return queryCursor;
	}

	private Cursor queryCategory(Uri uri, String[] projection,
			String[] whereArgs, String sortOrder) {
		String where = BaseColumns._ID + " = " + uri.getPathSegments().get(1);
		return mDb.query(CATEGORIES_TABLE_NAME, projection,
				where, whereArgs, where, null, sortOrder);
	}

	private Cursor queryCategories(String[] projection, String where,
			String[] whereArgs, String sortOrder) {
		return mDb.query(CATEGORIES_TABLE_NAME, projection,
		        where,
		        whereArgs,
		        null,
		        null, sortOrder);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		if (sUriMatcher.match(uri) != ARTICLE) {
			throw new IllegalArgumentException("Unknown URI " + uri); }
		String articleId = uri.getPathSegments().get(1);
		return mDb.update(ARTICLES_TABLE_NAME, values, BaseColumns._ID +" = "+ articleId, null);
	}
	
	public void insertEntryForCategory(ArrayList<ContentValues> values, long catId) {
		ContentValues categoryArticle;
		for (ContentValues value: values) {
			categoryArticle = new ContentValues();
			categoryArticle.put("category", catId);
			categoryArticle.put("article", value.getAsInteger(BaseColumns._ID));
			try {
				mDb.insertOrThrow(CATEGORIES_ARTICLES_TABLE_NAME, null, categoryArticle);
			} catch (SQLException e) {
				// do nothing
			}
		}
		// notify cursor
        getContext().getContentResolver().notifyChange(ContentUris.withAppendedId(Article.ARTICLES_URI, catId), null);
	}
	
	/**
	 * Return the URL of the category
	 */
	protected Cursor getCategory(long id) {
		String where = BaseColumns._ID +" = "+ id;
		Cursor c = mDb.query(CATEGORIES_TABLE_NAME, null, where, null, null, null, null);
		c.moveToFirst();
		return c;
	}
	
	/**
	 * Return the URL of the article
	 */
	protected String getArticleUrl(int id) {
		String where = BaseColumns._ID +" = "+ id;
		Cursor c = mDb.query(ARTICLES_TABLE_NAME, null, where, null, null, null, null);
		c.moveToFirst();
		String url = c.getString(c.getColumnIndex(Article.URL_NAME));
		c.close();
		return url;
	}
	
	/**
	 * Return the id
	 */
	private long getIdFor(String url) {
		String where = Article.URL_NAME +" = ?";
		Cursor c = mDb.query(ARTICLES_TABLE_NAME, null, where, new String[] { url }, null, null, null);
		if (c.getCount() == 0) {
			c.close();
			return -1;
		}
		c.moveToFirst();
		long id = c.getLong(c.getColumnIndex(BaseColumns._ID));
		c.close();
		return id;
	}
	
	/**
	 * Simple Helper to manipulate the database.
	 * This is mostly a boilerplate
	 */
	 private static class DatabaseHelper extends SQLiteOpenHelper {
		 	private Context context;
	        private DatabaseHelper(Context context, String name,
	                                    SQLiteDatabase.CursorFactory factory)
	        {
	            super(context, name, factory, DATABASE_VERSION);
	        	this.context = context;
	        }

	        @Override
	        public void onCreate(SQLiteDatabase sqLiteDatabase) {
	            createTable(sqLiteDatabase);
	        }

	        private void createTable(SQLiteDatabase sqLiteDatabase) {
	        	// articles table
	        	String qs = "CREATE TABLE " + ARTICLES_TABLE_NAME + " (" +
	                    BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
	                    Article.TITLE_NAME + " TEXT, " +
	                    Article.DESCRIPTION_NAME + " TEXT, " +
	                    Article.CONTENT_NAME + " TEXT, " +
	                    Article.DATE_NAME + " INT, " +
	                    Article.COLOR_NAME + " TEXT, " +
	                    Article.READ_NAME + " INT, " +
	                    Article.URL_NAME + " TEXT, UNIQUE("+ Article.URL_NAME +"));";
	            sqLiteDatabase.execSQL(qs);
	            // categories table
	            qs = "CREATE TABLE " + CATEGORIES_TABLE_NAME + " (" +
	                    BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
	                    Category.TITLE_NAME + " TEXT, " +
	                    Category.IMAGE_NAME + " TEXT, " +
	                    Category.URL_NAME + " TEXT, " +
	                    Category.COLOR_NAME + " TEXT, " +
	                    Category.FREE_NAME + " INTEGER, " +
	                    Category.PARENT_NAME + " TEXT);";
	             sqLiteDatabase.execSQL(qs);
	             qs = "CREATE TABLE "+ CATEGORIES_ARTICLES_TABLE_NAME +" ("+
	            		 "category INT, article INT, UNIQUE (category, article));";
	             sqLiteDatabase.execSQL(qs);
	             insertCategories(sqLiteDatabase);
	        }

	        @Override
	        public void onUpgrade(SQLiteDatabase sqLiteDatabase,
	                              int oldv, int newv)
	        {
	        	Log.d("ASI", "upgrade table");
	        	String[] tables = new String[] { ARTICLES_TABLE_NAME, CATEGORIES_TABLE_NAME,
	        				CATEGORIES_ARTICLES_TABLE_NAME };
	        	for (String table: tables) {
	        		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ table + ";");   
	        	}
	            createTable(sqLiteDatabase);
	        }
	        /**
	         * Parse the XML file categories.xml and then insert in the database
	         */
	        private void insertCategories(SQLiteDatabase sqLiteDatabase) {
				XmlResourceParser xpp = this.context.getResources().getXml(R.xml.categories);
				int eventType;
				ContentValues category = null;
				ArrayList<ContentValues> subcategories = null;
				ContentValues subcategory = null;
				boolean inSubcategory = false;
				String name;
				String type = null;
				try {
					eventType = xpp.getEventType();
					do {
						if (eventType == XmlPullParser.START_TAG) {
							name = xpp.getName();
							if ("categories".equals(name)) {
								// nothing
							} else if ("category".equals(name)) {
								category = new ContentValues();
								subcategories = new ArrayList<ContentValues>();
							} else if ("subcategory".equals(name)) {
								inSubcategory = true;
								subcategory = new ContentValues();
							} else if ("name".equals(name)) {
								type = Category.TITLE_NAME;
							} else if ("url".equals(name)) {
								type = Category.URL_NAME;
							} else if ("color".equals(name)) {
								type = Category.COLOR_NAME;
							} else if ("image".equals(name)) {
								type = Category.IMAGE_NAME;
							} else if ("free".equals(name)) {
								type = Category.FREE_NAME;
							} else {
								Log.w("ASI", "unknow tag "+ name +" in categories.xml");
							}
						} else if(eventType == XmlPullParser.END_TAG) {
							name = xpp.getName();
							if ("subcategory".equals(name)) {
								inSubcategory = false;
								subcategories.add(subcategory);
								subcategory = null;
							} else if ("category".equals(name)) {
								long id = sqLiteDatabase.insert(CATEGORIES_TABLE_NAME, null, category);
								for (ContentValues c: subcategories) {
									c.put(Category.PARENT_NAME, id);
									c.put(Category.COLOR_NAME, category.getAsString(Category.COLOR_NAME));
									sqLiteDatabase.insert(CATEGORIES_TABLE_NAME, null, c);
								}
								category = null;
								subcategories = null;
							}
						} else if (eventType == XmlPullParser.TEXT) {
							ContentValues v;
							if (inSubcategory)
								v = subcategory;
							else
								v = category;
							if (type == Category.FREE_NAME)
								v.put(type, Integer.parseInt(xpp.getText()));
							else
								v.put(type, xpp.getText());
						}
					    eventType = xpp.next();
					} while (eventType != XmlPullParser.END_DOCUMENT);
				} catch (XmlPullParserException e) {
					Log.e("ASI", "Error when parsing categories.xml "+ e.getMessage());
				} catch (IOException e) {
					Log.e("ASI", "IO error when reading categories.xml "+ e.getMessage());
				}
	        }
	    }

	@Override
	public Uri insert(Uri uri, ContentValues cv, SQLiteDatabase db) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected ResponseHandler newResponseHandler(String queryTag) {
		return null;
	}
	
	protected ResponseHandler createArticleResponseHandler() {
		return new ArticleHandler(this);
	}
	
	protected ResponseHandler createRssResponseHandler(long catId, String defaultColor) {
		return new RssHandler(this, catId, defaultColor);
	}
	
	class RssHandler implements ResponseHandler {
		private ArticleProvider provider;

		private long catId;
		
		private String defaultColor;
		
		public RssHandler(RESTfulContentProvider restfulProvider, long catId, String defaultColor) {
			this.provider = (ArticleProvider) restfulProvider;
			this.catId = catId;
			this.defaultColor = defaultColor;
		}

		public void handleResponse(HttpResponse response, Uri uri)
				throws IOException {
			try {
				Log.d("ASI", "handle rss response "+ uri);
				ArrayList<ContentValues> values = parseContent(response.getEntity());
				for (ContentValues value: values) {
					provider.insert(Article.createUriFor(value.getAsString(Article.URL_NAME)), value);
				}
				provider.insertEntryForCategory(values, catId);
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("ASI", "ERROR "+ e.toString());
			}
		}
		
		protected ArrayList<ContentValues> parseContent(HttpEntity entity) throws ParserConfigurationException, IllegalStateException, SAXException, IOException {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse(entity.getContent());
			NodeList items = dom.getElementsByTagName("item");
			ArrayList<ContentValues> values = new ArrayList<ContentValues>();
			ContentValues value;
			for (int i = 0; i < items.getLength(); i++) {
				value = new ContentValues();
				Node item = items.item(i);
				NodeList artis = item.getChildNodes();
				for (int j = 0; j < artis.getLength(); j++) {
					Node arti = artis.item(j);
					if (arti.getNodeName().equalsIgnoreCase("title"))
						value.put(Article.TITLE_NAME, arti.getFirstChild().getNodeValue());
					if (arti.getNodeName().equalsIgnoreCase("description")) {
						value.put(Article.DESCRIPTION_NAME, Article.parseDescription(arti.getFirstChild().getNodeValue()));
						String color = Article.parseColor(arti.getFirstChild().getNodeValue());
						if (color == null)
							color = this.defaultColor;
						value.put(Article.COLOR_NAME, color);
					}
					if (arti.getNodeName().equalsIgnoreCase("link"))
						value.put(Article.URL_NAME, arti.getFirstChild().getNodeValue());
					if (arti.getNodeName().equalsIgnoreCase("pubDate"))
						value.put(Article.DATE_NAME, Article.parseDate(arti.getFirstChild().getNodeValue()));
				}
				values.add(value);
			}
			return values;
		}
	}

	class ArticleHandler implements ResponseHandler {

		private RESTfulContentProvider provider;

		public ArticleHandler(RESTfulContentProvider restfulProvider) {
			this.provider = restfulProvider;
		}

		public void handleResponse(HttpResponse response, Uri uri)
				throws IOException {
			Log.d("ArticleProvider", "handle response "+ uri);
			// parse the result
            String content = parseContent(response.getEntity());
            Uri queryUri = Article.createUriFor(uri.toString());
			ContentValues c = new ContentValues();
			c.put(Article.CONTENT_NAME, content);
			c.put(Article.URL_NAME, uri.toString());
			provider.insert(queryUri, c);
		}

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

						// on remplace le lien dailymotion par celui pour l'iphone
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
