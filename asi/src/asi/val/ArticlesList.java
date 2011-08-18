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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ArticlesList extends AsiActivity {
	protected ListView maListViewPerso;

	protected String color;

	protected int image;

	protected Parcelable state;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// Récupération de la listview créée dans le fichier main.xml
		maListViewPerso = (ListView) findViewById(R.id.listview);
		
		int catId = this.getIntent().getExtras().getInt("id");
		
		Uri catUri = ContentUris.withAppendedId(Category.CATEGORIES_URI, catId);
		Cursor category = getContentResolver().query(catUri, null, null, null, null);

		category.moveToFirst();
		String title = category.getString(category.getColumnIndex(Category.TITLE_NAME));
		String color = category.getString(category.getColumnIndex(Category.COLOR_NAME));
		String image2 = category.getString(category.getColumnIndex(Category.IMAGE_NAME));
		category.close();
				
		TextView text = (TextView) findViewById(R.id.list_text);
		text.setText(title.replaceFirst(">", ""));
		this.color = color;
		text.setBackgroundColor(Color.parseColor(color));

		// récupération de l'image
		image = this.getResources().getIdentifier(
				image2, "drawable",
				this.getPackageName());
		ImageView v = (ImageView) findViewById(R.id.cat_image);
		if (image != 0) {
			v.setImageResource(image);
		} else
			v.setImageResource(R.drawable.toutlesite);

		this.loadContent();
	}
	
	private class ArticleViewBinder implements ViewBinder {
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if (columnIndex == cursor.getColumnIndex(Article.COLOR_NAME)) {
				view.setBackgroundColor(Color.parseColor(cursor.getString(columnIndex)));
				return true;
			} else if (columnIndex == cursor.getColumnIndex(Article.DATE_NAME)) {
				Date date = new Date(cursor.getLong(columnIndex));
				SimpleDateFormat format = new SimpleDateFormat("E d/M k:m");
			    ((TextView) view).setText(format.format(date));
				return true;
			}
			return false;
		}
	}

	public void loadContent() {
		int catId = this.getIntent().getExtras().getInt("id");
		Cursor c = managedQuery(ContentUris.withAppendedId(Article.ARTICLES_URI, catId), null, null, null, Article.DATE_NAME +" DESC");
		// create adapter
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.listview, c, 
				new String[] { Article.COLOR_NAME, Article.TITLE_NAME, 
							   Article.DESCRIPTION_NAME, Article.DATE_NAME },
				new int[] { R.id.color, R.id.titre, R.id.description,
							R.id.date });

		// on ajoute le binder
		adapter.setViewBinder(new ArticleViewBinder());
		//on sauve
		state = maListViewPerso.onSaveInstanceState();
		maListViewPerso.setAdapter(adapter);
		maListViewPerso.setEmptyView(findViewById(R.id.progress));
		maListViewPerso.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, int position,
					long arg3) {
				SimpleCursorAdapter adapter = (SimpleCursorAdapter) maListViewPerso.getAdapter();
				Cursor c = adapter.getCursor();
				c.moveToPosition(position);
				String url = c.getString(c.getColumnIndex(Article.URL_NAME));
				String title = c.getString(c.getColumnIndex(Article.TITLE_NAME));
				long id = c.getLong(c.getColumnIndex(BaseColumns._ID));
				ArticlesList.this.loadPage(id, url, title);
			}
		});
			
		maListViewPerso
				.setOnItemLongClickListener(new OnItemLongClickListener() {

					@SuppressWarnings("unchecked")
					public boolean onItemLongClick(AdapterView<?> a, View v,
							int position, long id) {
						HashMap<String, String> map = (HashMap<String, String>) maListViewPerso
								.getItemAtPosition(position);
						if (map.get("url").contains("recherche.php"))
							ArticlesList.this.onSearchItem(map.get("url"));
						else
							ArticlesList.this.choice_action_item(
									map.get("url"), map.get("titre"));

						return false;
					}

				});
		maListViewPerso.onRestoreInstanceState(state);
	}

	public void onResume() {
		super.onResume();
		Log.d("ASI", "liste_article onResume");
		//loadContent();
	}

	public void onSaveInstanceState(final Bundle b) {
		Log.d("ASI", "liste_article onSaveInstanceState");
		state = maListViewPerso.onSaveInstanceState();
		super.onSaveInstanceState(b);
	}

	private void choice_action_item(final String url, final String titre) {
		final CharSequence[] items = { "Visualiser", "Partager",
				"Marquer comme lu" };
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(titre);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if (items[item].equals("Visualiser")) {
					//ArticlesList.this.loadPage(url, titre);
				} else if (items[item].equals("Partager")) {
					ArticlesList.this.share(url, titre);
				} else {
					ArticlesList.this.markAsRead(url);
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	protected void onSearchItem(String url){
		//à faire uniquement dans les recherches
	}

	private void share(String url, String titre) {
		try {
			Intent emailIntent = new Intent(Intent.ACTION_SEND);
			emailIntent.putExtra(Intent.EXTRA_TEXT,
					"Un article interessant sur le site arretsurimage.net :\n"
							+ titre + "\n" + url);
			emailIntent.setType("text/plain");
			// emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(Intent.createChooser(emailIntent,
					"Partager cet article"));
		} catch (Exception e) {
			new ErrorDialog(this, "Chargement de la page", e).show();
		}
	}

	private void markAsRead(String url) {
		try {
			this.getData().addArticlesRead(url);
			state = maListViewPerso.onSaveInstanceState();
			this.loadContent();
			maListViewPerso.onRestoreInstanceState(state);
		} catch (Exception e) {
			new ErrorDialog(this, "Chargement de la page", e).show();
		}
		// new page(main.this);
	}

	private void loadPage(long id, String url, String titre) {
		try {
			Intent i = new Intent(this, Page.class);
			i.putExtra("id", id);
			i.putExtra("url", url);
			i.putExtra("titre", titre);
			this.startActivity(i);
		} catch (Exception e) {
			new ErrorDialog(this, "Chargement de la page", e).show();
		}
		// new page(main.this);
	}

	
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.liste_article_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.item4:
			//for(int i =0;i<articles.size();i++)
			//	this.getData().addArticlesRead(articles.elementAt(i).getUri());
			//if(articles.size()>0)
			//	this.markAsRead(articles.elementAt(0).getUri());
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
