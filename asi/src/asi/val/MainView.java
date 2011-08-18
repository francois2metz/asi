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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MainView extends AsiActivity {
	private ListView maListViewPerso;

	private boolean gratuit;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// Récupération de la listview créée dans le fichier main.xml
		maListViewPerso = (ListView) findViewById(R.id.listviewperso);

		gratuit = this.getIntent().getExtras().getBoolean("gratuit");
		TextView text = (TextView) findViewById(R.id.list_text);
		// TextView color = (TextView) findViewById(R.id.cat_color);
		text.setText("Choix des catégories");
		
		ImageView v = (ImageView) findViewById(R.id.cat_image);
		v.setImageResource(R.drawable.toutlesite);
		
		this.loadData();

	}

	private void loadData() {

		// Création de la ArrayList qui nous permettra de remplir la listView
		ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map;
		// Chargement des catégories
		int[] liste = null;
		if (!gratuit) {
			liste = new int[] { R.array.catT, R.array.catE, R.array.catD,
					R.array.catC, R.array.catV, R.array.catR};
		} else {
			liste = new int[] { R.array.catT, R.array.catE, R.array.catD,
					R.array.catC, R.array.catG, R.array.catR};
		}

		Resources res = getResources();
		for (int i = 0; i < liste.length; i++) {
			String[] categorie = res.getStringArray(liste[i]);
			map = new HashMap<String, String>();
			map.put("titre", categorie[0]);
			map.put("image", categorie[1]);
			map.put("url", categorie[2]);
			map.put("subcat", categorie[3]);
			map.put("color", categorie[4]);
			listItem.add(map);
		}

		// Création d'un SimpleAdapter qui se chargera de mettre les items
		// présents dans notre list (listItem) dans la vue affichageitem
		SimpleAdapter mSchedule = new SimpleAdapter(this.getBaseContext(),
				listItem, R.layout.categorie_listes, new String[] { "color","titre" },
				new int[] { R.id.cat_color, R.id.cat_title });
		//on ajoute le viewbinder 
		mSchedule.setViewBinder(new BindColor());

		// On attribue à notre listView l'adapter que l'on vient de créer
		maListViewPerso.setAdapter(mSchedule);

		// Enfin on met un écouteur d'évènement sur notre listView
		maListViewPerso.setOnItemClickListener(new OnItemClickListener() {
			@SuppressWarnings("unchecked")
			public void onItemClick(AdapterView<?> a, View v, int position,
					long id) {
				// on récupère la HashMap contenant les infos de notre item
				// (titre, description, img)
				HashMap<String, String> map = (HashMap<String, String>) maListViewPerso
						.getItemAtPosition(position);
				if(map.get("url").equalsIgnoreCase("recherche"))
					MainView.this.doSearch(map.get("titre"),
							map.get("color"),map.get("image"));
				else if (map.get("subcat").equalsIgnoreCase("no"))
					MainView.this.loadPage(map.get("url"), map.get("titre"),
							map.get("color"),map.get("image"));
				else
					MainView.this.onLongClick(map.get("subcat"), map
							.get("titre"),map.get("color"));
			}
		});
	}

	protected void doSearch(String titre, String color, String image) {
		new SearchDialog(this, titre,color,image)
		.show();
		
	}

	private void loadPage(String url, String titre, String color, String image) {
		try {
			Intent i = new Intent(this, ArticlesList.class);
			i.putExtra("url", url);
			i.putExtra("titre", titre);
			i.putExtra("color", color);
			i.putExtra("image", image);
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
		
//		final CharSequence[] subcate = new CharSequence[subcategorie.length / 3];
//		for (int i = 0; i < subcategorie.length; i += 3) {
//			subcate[(i / 3)] = subcategorie[i];
//		}
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(titre.replace(">", ""));
		
		builder.setAdapter(mSchedule2, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				MainView.this.loadPage(subcategorie[item * 3 + 1],
						subcategorie[item * 3], color_fin,subcategorie[item * 3 + 2]);
			}
			});

//		builder.setItems(subcate, new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int item) {
//				main_view.this.load_page(subcategorie[item * 3 + 1],
//						subcategorie[item * 3], color_fin);
//			}
//		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	// public void onBackPressed(){
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if(this.get_datas().isAutologin()){
				//this.closed_application();
				this.finish();
				return true;
			}
			//main.group.is_autologin(false);
			//return true;
		}

		return super.onKeyDown(keyCode, event);
	}

}
