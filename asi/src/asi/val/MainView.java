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

import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MainView extends AsiActivity {

	private ListView listView;
	
	private boolean gratuit;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		listView = (ListView) findViewById(R.id.listview);
		
		gratuit = this.getIntent().getExtras().getBoolean("gratuit");
		TextView text = (TextView) findViewById(R.id.list_text);
		text.setText("Choix des catégories");

		ImageView v = (ImageView) findViewById(R.id.cat_image);
		v.setImageResource(R.drawable.toutlesite);

		this.loadData();
	}

	private void loadData() {
		// Chargement des catégories
        Cursor c = managedQuery(Category.CATEGORIES_URI, null, null, null, null);
		int[] liste = null;
		if (!gratuit) {
			liste = new int[] { R.array.catT, R.array.catE, R.array.catD,
					R.array.catC, R.array.catV, R.array.catR};
		} else {
			liste = new int[] { R.array.catT, R.array.catE, R.array.catD,
					R.array.catC, R.array.catG, R.array.catR};
		}
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
				String image = c.getString(c.getColumnIndex(Category.IMAGE_NAME));
				String url = c.getString(c.getColumnIndex(Category.URL_NAME));
				String subcat = c.getString(c.getColumnIndex(Category.SUB_CAT));
				int id = c.getInt(c.getColumnIndex(BaseColumns._ID));
				
				if(url.equalsIgnoreCase("recherche"))
					doSearch(title, color, image);
				else if (subcat.equalsIgnoreCase("no"))
					loadPage(id);
				else
					onLongClick(subcat, title, color);
				
			}
		});
	}
	
	protected void doSearch(String titre, String color, String image) {
		new SearchDialog(this, titre, color,image).show();	
	}

	private void loadPage(int idCat) {
		try {
			Intent i = new Intent(this, ArticlesList.class);
			i.putExtra("id", idCat);
			this.startActivity(i);
		} catch (Exception e) {
			new ErrorDialog(this, "Chargement de la liste des articles", e)
					.show();
		}
	}

	private void onLongClick(String subid, String titre, String color) {
		Resources res = getResources();
		// int id = res.getIdentifier(subid, null, null);
		Log.d("ASI", subid);
		Log.d("ASI", this.getPackageName());
		int id = res.getIdentifier(subid, "array", this.getPackageName());
		final String color_fin = color;
		
		ArrayList<HashMap<String, String>> subcatitem = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map;
		
		final String[] subcategorie = res.getStringArray(id);
		for (int i = 0; i < subcategorie.length; i += 3) {
			map = new HashMap<String, String>();
			map.put("titre", subcategorie[i]);
			map.put("logo", "png-"+res.getIdentifier(subcategorie[i+2], "drawable", this.getPackageName()));	
			//map.put("logo", subcategorie[i+2]);
			map.put("url", subcategorie[i+1]);				
			subcatitem.add(map);
		}		
		
		SimpleAdapter mSchedule2 = new SimpleAdapter(this.getBaseContext(),
				subcatitem, R.layout.subcategorie, new String[] { "logo","titre" },
				new int[] { R.id.subcat_image,R.id.subcat_title });
		mSchedule2.setViewBinder(new BindImage());

		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(titre.replace(">", ""));

		builder.setAdapter(mSchedule2, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				MainView.this.loadPage(1);
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
			//main.group.is_autologin(false);
			//return true;
		}

		return super.onKeyDown(keyCode, event);
	}

}
