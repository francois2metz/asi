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

package asi.val.provider;

import java.io.IOException;
import java.util.ArrayList;

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
import asi.val.Article;
import asi.val.Category;
import asi.val.R;
import asi.val.Search;

/**
 * ContentProvider for Articles and categories
 * TODO: cap number of articles
 */
public class ArticleProvider extends RESTfulContentProvider {

	public static final String AUTHORITY = "asi.val.provider.articleprovider";

	private static final String ARTICLES_TABLE_NAME = "articles";

	private static final String CATEGORIES_TABLE_NAME = "categories";

	private static final String CATEGORIES_ARTICLES_TABLE_NAME = "categories_articles";

	private static final String SEARCH_TABLE_NAME = "search";

	private static final String SEARCH_ARTICLES_TABLE_NAME = "search_articles";

	private static final int ARTICLE = 1;

	private static final int ARTICLES_BY_CATEGORY = 2;

	private static final int CATEGORIES = 3;

	private static final int CATEGORY_ID = 4;

	private static final int SEARCH = 5;

	static int DATABASE_VERSION = 20;

	private static final UriMatcher sUriMatcher;

	private DatabaseHelper mOpenHelper;
	private SQLiteDatabase mDb;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, "article/#", ARTICLE);
		sUriMatcher.addURI(AUTHORITY, "articles/#", ARTICLES_BY_CATEGORY);
		sUriMatcher.addURI(AUTHORITY, "categories", CATEGORIES);
		sUriMatcher.addURI(AUTHORITY, "categories/#", CATEGORY_ID);
		sUriMatcher.addURI(AUTHORITY, "search", SEARCH);
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
	        case SEARCH:
				queryCursor = querySearch(uri, projection, where, whereArgs, sortOrder);
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
		String select = "SELECT a."+ BaseColumns._ID +", a."+
		        		Article.TITLE_NAME +", a."+
		        		Article.DESCRIPTION_NAME +", a."+
		        		Article.DATE_NAME + ", a."+
		        		Article.COLOR_NAME + ", a."+
		        		Article.URL_NAME + ", a."+
		        		Article.READ_NAME +
		        		" FROM " + ARTICLES_TABLE_NAME + " as a JOIN "+
		         CATEGORIES_ARTICLES_TABLE_NAME + " as c ON c.article = a._id WHERE c.category="+catId;
		if (where != null) {
			select += " and a."+ where;
		}
		if (sortOrder != null)
			select += " ORDER BY a."+ sortOrder;
		Cursor queryCursor = mDb.rawQuery(select, null);
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

	/**
	 * Do search
	 */
	private Cursor querySearch(Uri uri, String[] projection, String where,
			String[] whereArgs, String sortOrder) {
		long searchId = insertSearch(uri);
		String select = "SELECT a."+ BaseColumns._ID +", a."+
				Article.TITLE_NAME +", a."+
				Article.DESCRIPTION_NAME +", a."+
				Article.DATE_NAME + ", a."+
				Article.COLOR_NAME + ", a."+
				Article.URL_NAME + ", a."+
				Article.READ_NAME +
		" FROM " + ARTICLES_TABLE_NAME + " as a JOIN "+
         SEARCH_ARTICLES_TABLE_NAME + " as c ON c.article = a._id WHERE c.search="+searchId;

		if (where != null) {
			select += " and a."+ where;
		}
		if (sortOrder != null)
			select += " ORDER BY a."+ sortOrder;
		Cursor queryCursor = mDb.rawQuery(select, null);
		// make the cursor observe the requested query
		queryCursor.setNotificationUri(
		        getContext().getContentResolver(), uri);
		// Part 2: get the latest version
		asyncQueryRequest("search_"+ uri, getSearchUrl(uri), this.createSearchResponseHandler(uri, searchId));
		return queryCursor;
	}

	/**
	 * Create the search URL
	 */
	private String getSearchUrl(Uri uri) {
		return "http://www.arretsurimages.net/recherche.php?" + uri.getQuery() +
				"t=0&periode=0&jour1=00&mois1=00&annee1=0&jour2=00&mois2=00&annee2=0&orderby=num";
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

	public void insertEntryForSearch(Uri uri, ArrayList<ContentValues> values, long searchId) {
		ContentValues searchArticle;
		for (ContentValues value: values) {
			searchArticle = new ContentValues();
			searchArticle.put("search", searchId);
			searchArticle.put("article", value.getAsInteger(BaseColumns._ID));
			try {
				mDb.insertOrThrow(SEARCH_ARTICLES_TABLE_NAME, null, searchArticle);
			} catch (SQLException e) {
				// do nothing
			}
		}
		// notify cursor
        getContext().getContentResolver().notifyChange(uri, null);
	}

	/**
	 * Create a new Search entry and return the id
	 */
	public long insertSearch(Uri uri) {
		ContentValues values = new ContentValues();
		values.put(Search.URI_NAME, uri.toString());
		return mDb.insertOrThrow(SEARCH_TABLE_NAME, null, values);
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
	             // categories - articles table
	             qs = "CREATE TABLE "+ CATEGORIES_ARTICLES_TABLE_NAME +" ("+
	            		 "category INT, article INT, UNIQUE (category, article));";
	             sqLiteDatabase.execSQL(qs);
	             // search table
				qs = "CREATE TABLE "+ SEARCH_TABLE_NAME +" ("+
						Search._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
						Search.URI_NAME + " TEXT, " +
						Search.NB_RESULT_NAME +" INTEGER);";
	             sqLiteDatabase.execSQL(qs);
	             // search - articles table
	             qs = "CREATE TABLE "+ SEARCH_ARTICLES_TABLE_NAME +" ("+
	            		 "search INT, article INT, UNIQUE (search, article));";
	             sqLiteDatabase.execSQL(qs);
	             insertCategories(sqLiteDatabase);
	        }

	        @Override
	        public void onUpgrade(SQLiteDatabase sqLiteDatabase,
	                              int oldv, int newv)
	        {
	        	Log.d("ASI", "upgrade table");
	        	String[] tables = new String[] { ARTICLES_TABLE_NAME, CATEGORIES_TABLE_NAME,
	        			CATEGORIES_ARTICLES_TABLE_NAME, SEARCH_TABLE_NAME, SEARCH_ARTICLES_TABLE_NAME };
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

	protected ResponseHandler createSearchResponseHandler(Uri uri, long searchId) {
		return new SearchHandler(this, uri, searchId);
	}
}
