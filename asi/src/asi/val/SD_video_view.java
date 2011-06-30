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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SD_video_view extends reload_activity {
	private ListView maListViewPerso;

	private Vector<File> video_sd;

	private final File path = new File(
			Environment.getExternalStorageDirectory() + "/ASI");

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// Récupération de la listview créée dans le fichier main.xml
		maListViewPerso = (ListView) findViewById(R.id.listviewperso);

		TextView text = (TextView) findViewById(R.id.list_text);
		// Ajout de l'image
		ImageView v = (ImageView) findViewById(R.id.cat_image);
		v.setImageResource(R.drawable.video);

		text.setText("Vidéos téléchargées");
		video_sd = new Vector<File>();

		//this.load_data();
	}

	protected void load_data() {
		// try {
		// Thread.sleep(100);
		// } catch (Exception e) {
		//
		// }
		// on vide la liste de vidéos
		video_sd.clear();

		try {
			// on vérifie que l'on peut enregistrer
			String state = Environment.getExternalStorageState();
			if (!Environment.MEDIA_MOUNTED.equals(state))
				throw new StopException("La carte SD n'est pas montée");

			// recupération de la liste de vidéos dans le dossier
			if (path.exists()) {
				File[] liste = path.listFiles();
				for (int i = 0; i < liste.length; i++) {
					if (liste[i].getName().endsWith(".mp4"))
						video_sd.add(liste[i]);
				}
			}
		} catch (StopException e) {
			new erreur_dialog(this, "Lecture de la carte SD", e.toString())
					.show();
			this.update.stop_update();
		} catch (Exception e) {
			new erreur_dialog(this, "Lecture de la carte SD", e).show();
			this.update.stop_update();
		}

		// Création de la ArrayList qui nous permettra de remplir la listView
		ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map;
		File dvid;
		for (int i = 0; i < video_sd.size(); i++) {
			dvid = video_sd.elementAt(i);
			map = new HashMap<String, String>();
			map.put("titre", dvid.getName().replaceAll("_", " "));
			int leng = (int) (dvid.length() / 1000);
			map.put("description", leng + " ko");
			map.put("int", "" + i);
			listItem.add(map);

		}
		// Si aucune vidéo sur la carte SD, on ajoute un message
		if (video_sd.size() == 0) {
			map = new HashMap<String, String>();
			map.put("titre", "Aucune vidéo téléchargée sur la carte SD");
			map.put("description", "");
			map.put("int", "null");
			listItem.add(map);
		}

		// on sauve l'état de la liste
		Parcelable state = maListViewPerso.onSaveInstanceState();

		// Création d'un SimpleAdapter qui se chargera de mettre les items
		// présents dans notre list (listItem) dans la vue affichageitem
		SimpleAdapter mSchedule = new SimpleAdapter(this.getBaseContext(),
				listItem, R.layout.listview, new String[] { "titre",
						"description" }, new int[] { R.id.titre,
						R.id.description });

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
				if (!map.get("int").equals("null"))
					SD_video_view.this.traitement_video(map.get("int"));
			}
		});

		// on restore la position
		maListViewPerso.onRestoreInstanceState(state);
	}

	private void do_on_video(final File vid) throws Exception {
		final CharSequence[] items = { "Visualiser", "Supprimer" };
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(vid.getName().replaceAll("_", ""));
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if (items[item].equals("Visualiser")) {
					// démarrer la video
					Intent intent = new Intent();
					intent.setAction(android.content.Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(vid), "video/*");
					SD_video_view.this.startActivity(intent);
				} else if (items[item].equals("Supprimer")) {
					if (vid.exists())
						vid.delete();
					Toast.makeText(SD_video_view.this, "Fichier supprimé",
							Toast.LENGTH_SHORT).show();
				}
				// SD_video_view.this.load_data();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void traitement_video(String num) {
		try {
			Log.d("ASI", "Num=" + num);
			File vid = video_sd.elementAt(Integer.parseInt(num));
			this.do_on_video(vid);

		} catch (Exception e) {
			new erreur_dialog(this, "Traitement de la vidéo", e).show();
		}

	}
}
