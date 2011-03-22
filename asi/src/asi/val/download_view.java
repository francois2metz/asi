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
import java.util.Vector;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.AsyncTask.Status;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class download_view extends reload_activity {
	private ListView maListViewPerso;

	private Vector<download_video> video_download;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// Récupération de la listview créée dans le fichier main.xml
		maListViewPerso = (ListView) findViewById(R.id.listviewperso);

		ImageView v = (ImageView) findViewById(R.id.cat_image);
		v.setImageResource(R.drawable.telechargement);

		TextView text = (TextView) findViewById(R.id.list_text);

		text.setText("Téléchargements en cours");
		// recuperation de la liste de telechargement
		this.video_download = this.get_datas().get_download_video();

		//this.load_data();
	}

	protected void load_data() {
		// try {
		// Thread.sleep(100);
		// } catch (Exception e) {
		//
		// }
		// Création de la ArrayList qui nous permettra de remplire la listView
		ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map;
		download_video dvid;
		//
		// //teste de remplissage
		// for(int j=0;j<10;j++){
		// map = new HashMap<String, String>();
		// map.put("titre", "Coucou");
		// map.put("description", "bizarre telechargement ");
		// listItem.add(map);
		// }

		for (int i = 0; i < video_download.size(); i++) {
			// les vidéos finis de télécharger sont cachées
			boolean visible = true;
			dvid = video_download.elementAt(i);
			map = new HashMap<String, String>();
			map.put("titre", dvid.get_download_video().getTitle());
			Status status = dvid.getStatus();
			if (status == Status.RUNNING)
				map.put("description",
						"Téléchargement - " + dvid.get_pourcentage_download());
			else if (status == Status.PENDING)
				map.put("description", "Téléchargement - En préparation");
			else {
				if (dvid.get_error() == null) {
					if (dvid.is_video_complete())
						visible = false;
					else
						map.put("description", "Téléchargement interrompu - "
								+ dvid.get_pourcentage_download());
				} else
					map.put("description", "ERREUR lors du téléchargement");// -
																			// "
																			// +
																			// dvid.get_error());

			}
			map.put("int", "" + i);
						
			if (visible)
				listItem.add(map);
		}
		
		
		if(!this.get_datas().isDlSync())
		{
			boolean has_running = false;
			for(download_video dv : video_download)
			{
				if(dv.getStatus() == Status.RUNNING){
					has_running = true;
					break;
				}
			}
			
			if(!has_running){
				for(download_video dv : video_download)
				{
					if(dv.getStatus() == Status.PENDING){
						dv.execute(dv.get_download_video());
						break;
					}
				}
			}
		}

		// Si tout les téléchargements sont terminés ou aucun de lancé
		if (listItem.size() == 0) {
			map = new HashMap<String, String>();
			if (video_download.size() == 0){
				map.put("titre", "Aucun téléchargement en cours");
				map.put("description", "");
			}
			else{
				map.put("titre", "Tout les téléchargements sont terminés");
				map.put("description", "Accédé aux vidéos via le menu <Vidéos téléchargées>");
			}
			map.put("int", "null");
			listItem.add(map);
		}

		// on sauve l'état de la liste
		Parcelable state = maListViewPerso.onSaveInstanceState();

		// Création d'un SimpleAdapter qui se chargera de mettre les items
		// présent dans notre list (listItem) dans la vue affichageitem
		SimpleAdapter mSchedule = new SimpleAdapter(this.getBaseContext(),
				listItem, R.layout.listview, new String[] { "titre",
						"description" }, new int[] { R.id.titre,
						R.id.description });

		// On attribut à notre listView l'adapter que l'on vient de créer
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
					download_view.this.traitement_video(map.get("int"));
			}
		});

		// on restore la position
		maListViewPerso.onRestoreInstanceState(state);
	}

	private void do_on_video_error(final download_video vid) throws Exception {
		final CharSequence[] items = { "Relancer", "Effacer", "Erreur" };
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(vid.get_download_video().getTitle());
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if (items[item].equals("Relancer")) {
					download_view.this.get_datas().get_download_video().remove(vid);
					download_view.this.get_datas().downloadvideo(vid.get_download_video());
				} else if (items[item].equals("Effacer")) {
					download_view.this.get_datas().get_download_video().remove(vid);
				} else {
					new erreur_dialog(download_view.this, vid.get_download_video().getTitle(), vid
							.get_error()).show();
				}
				// download_view.this.load_data();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void do_on_video_running(final download_video vid) throws Exception {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(vid.get_download_video().getTitle());
		builder.setMessage("Arrêter le téléchargement en cours?")
				.setCancelable(false)
				.setPositiveButton("Oui",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								vid.Stop_download();
							}
						})
				.setNegativeButton("Non",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		AlertDialog stop = builder.create();
		stop.show();
	}

	private void do_on_video_finished(final download_video vid)
			throws Exception {
		final CharSequence[] items = { "Visualiser", "Effacer", "Relancer" };
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(vid.get_download_video().getTitle());
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if (items[item].equals("Visualiser")) {
					// demarrer la video
					Intent intent = new Intent();
					intent.setAction(android.content.Intent.ACTION_VIEW);
					intent.setDataAndType(
							Uri.fromFile(vid.get_download_path()), "video/*");
					download_view.this.startActivity(intent);
				} else if (items[item].equals("Effacer")) {
					download_view.this.get_datas().get_download_video().remove(vid);
				} else if (items[item].equals("Relancer")) {
					download_view.this.get_datas().get_download_video().remove(vid);
					download_view.this.get_datas().downloadvideo(vid.get_download_video());
				}
				// download_view.this.load_data();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void traitement_video(String num) {
		try {
			Log.d("ASI", "Num=" + num);
			download_video vid = video_download
					.elementAt(Integer.parseInt(num));
			Status status = vid.getStatus();
			Log.d("ASI", "Status=" + status.toString());
			if (status == Status.FINISHED) {
				if (vid.get_error() != null)
					this.do_on_video_error(vid);
				else
					this.do_on_video_finished(vid);
			} else {
				// proposer d'arreter le thread
				this.do_on_video_running(vid);
			}

		} catch (Exception e) {
			new erreur_dialog(this, "Traitement de la vidéo", e).show();
		}

	}

}
