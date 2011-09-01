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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MainView extends AsiActivity {

	private ListView listView;
	
	private boolean free;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		listView = (ListView) findViewById(R.id.listview);
		
		free = this.getIntent().getExtras().getBoolean("free");
		TextView text = (TextView) findViewById(R.id.list_text);
		text.setText(R.string.select_categories);

		ImageView v = (ImageView) findViewById(R.id.cat_image);
		v.setImageResource(R.drawable.toutlesite);

		this.loadData();
	}

	private void loadData() {
		// load categories
		String selection = Category.PARENT_NAME+" IS NULL";
		if (free) {
			selection += " and "+ Category.FREE_NAME +"!=2";
		} else {
			selection += " and "+ Category.FREE_NAME +"!=1";
		}
		Cursor c = managedQuery(Category.CATEGORIES_URI, null, selection, null, null);
		// Création d'un SimpleAdapter qui se chargera de mettre les items
		// présents dans notre list (listItem) dans la vue affichageitem
		SimpleCursorAdapter mSchedule = new SimpleCursorAdapter(this,
				R.layout.categorie_listes,
				c,
				new String[] {Category.COLOR_NAME, Category.TITLE_NAME },
				new int[] { R.id.cat_color, R.id.cat_title });

		mSchedule.setViewBinder(new BindColor());
		listView.setAdapter(mSchedule);

		// Enfin on met un écouteur d'évènement sur notre listView
		listView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> adapterView, View view, int position,
					long arg3) {
				SimpleCursorAdapter adapter = (SimpleCursorAdapter) listView.getAdapter();
				Cursor c = adapter.getCursor();
				c.moveToPosition(position);
				
				String title = c.getString(c.getColumnIndex(Category.TITLE_NAME));
				String color = c.getString(c.getColumnIndex(Category.COLOR_NAME));
				String url = c.getString(c.getColumnIndex(Category.URL_NAME));
				long id = c.getLong(c.getColumnIndex(BaseColumns._ID));

				if("recherche".equalsIgnoreCase(url)) {
					String image = c.getString(c.getColumnIndex(Category.IMAGE_NAME));
					doSearch(title, color, image);
				} else {
					// has subcat ?
					Cursor subcats = getContentResolver().query(Category.CATEGORIES_URI, null, Category.PARENT_NAME+"="+id, null, null);
					if (subcats.getCount() == 0) {
						subcats.close();
						loadPage(id);
					} else {
						openSubCategories(subcats, title);
					}
				}
			}
		});
	}
	
	protected void doSearch(String titre, String color, String image) {
		new SearchDialog(this, titre, color,image).show();	
	}

	private void loadPage(long idCat) {
		Intent i = new Intent(this, ArticlesList.class);
		i.putExtra("id", idCat);
		this.startActivity(i);
	}

	private class BindSubCategoryImage implements ViewBinder {
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if (columnIndex == cursor.getColumnIndex(Category.IMAGE_NAME)) {
				ImageView vi = (ImageView) view;
				String value = cursor.getString(columnIndex);
				if (value != null) {
					vi.setImageResource(getResources().getIdentifier(value, "drawable", "asi.val"));
					return true;
				}
			}
			return false;
		}
	}

	private void openSubCategories(final Cursor subcategories, String title) {
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.subcategorie,
				subcategories,
				new String[] { Category.IMAGE_NAME, Category.TITLE_NAME },
				new int[] { R.id.subcat_image, R.id.subcat_title });
		adapter.setViewBinder(new BindSubCategoryImage());

		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);

		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				subcategories.moveToPosition(item);
				long id = subcategories.getLong(subcategories.getColumnIndex(BaseColumns._ID));
				subcategories.close();
				MainView.this.loadPage(id);
			}
		});

		AlertDialog alert = builder.create();
		alert.show();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if(this.getData().isAutologin()){
				this.finish();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

}
