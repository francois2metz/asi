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

public class DownloadView extends ReloadActivity {
	private ListView maListViewPerso;

	private Vector<DownloadVideo> video_download;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// Récupération de la listview créée dans le fichier main.xml
		maListViewPerso = (ListView) findViewById(R.id.listview);

		ImageView v = (ImageView) findViewById(R.id.cat_image);
		v.setImageResource(R.drawable.telechargement);

		TextView text = (TextView) findViewById(R.id.list_text);

		text.setText("Téléchargements en cours");
		// récuperation de la liste de téléchargements
		this.video_download = this.getData().get_download_video();

		//this.load_data();
	}

	protected void loadData() {
		// try {
		// Thread.sleep(100);
		// } catch (Exception e) {
		//
		// }
		// Création de la ArrayList qui nous permettra de remplir la listView
		ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map;
		DownloadVideo dvid;
		//
		// teste de remplissage
		// for(int j=0;j<10;j++){
		// map = new HashMap<String, String>();
		// map.put("titre", "Coucou");
		// map.put("description", "bizarre téléchargement ");
		// listItem.add(map);
		// }

		for (int i = 0; i < video_download.size(); i++) {
			// les vidéos téléchargées sont cachées
			boolean visible = true;
			dvid = video_download.elementAt(i);
			map = new HashMap<String, String>();
			map.put("titre", dvid.getDownloadVideo().getTitleAndNumber());
			Status status = dvid.getStatus();
			if (status == Status.RUNNING)
				map.put("description",
						"Téléchargement - " + dvid.getDownloadPercent());
			else if (status == Status.PENDING)
				map.put("description", "Téléchargement - En attente");
			else {
				if (dvid.getError() == null) {
					if (dvid.isComplete())
						visible = false;
					else
						map.put("description", "Téléchargement interrompu - "
								+ dvid.getDownloadPercent());
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

		// Si tous les téléchargements sont terminés ou aucun n'est lancé
		if (listItem.size() == 0) {
			map = new HashMap<String, String>();
			if (video_download.size() == 0){
				map.put("titre", "Aucun téléchargement en cours");
				map.put("description", "");
			}
			else{
				map.put("titre", "Tous les téléchargements sont terminés");
				map.put("description", "Accéder aux vidéos via le menu <Vidéos téléchargées>");
			}
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
					DownloadView.this.traitement_video(map.get("int"));
			}
		});

		// on restore la position
		maListViewPerso.onRestoreInstanceState(state);
	}

	private void onVideoError(final DownloadVideo vid) throws Exception {
		final CharSequence[] items = { "Relancer", "Effacer", "Erreur" };
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(vid.getDownloadVideo().getTitleAndNumber());
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if (items[item].equals("Relancer")) {
					DownloadView.this.getData().get_download_video().remove(vid);
					DownloadView.this.getData().downloadvideo(vid.getDownloadVideo());
				} else if (items[item].equals("Effacer")) {
					DownloadView.this.getData().get_download_video().remove(vid);
				} else {
					new ErrorDialog(DownloadView.this, vid.getDownloadVideo().getTitleAndNumber(), vid
							.getError()).show();
				}
				// download_view.this.load_data();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void onVideoRunning(final DownloadVideo vid) throws Exception {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(vid.getDownloadVideo().getTitleAndNumber());
		builder.setMessage("Arrêter le téléchargement en cours ?")
				.setCancelable(false)
				.setPositiveButton("Oui",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								vid.stopDownload();
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

	private void onVideoFinished(final DownloadVideo vid)
			throws Exception {
		final CharSequence[] items = { "Visualiser", "Effacer", "Relancer" };
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(vid.getDownloadVideo().getTitleAndNumber());
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if (items[item].equals("Visualiser")) {
					// démarrer la video
					Intent intent = new Intent();
					intent.setAction(android.content.Intent.ACTION_VIEW);
					intent.setDataAndType(
							Uri.fromFile(vid.getDownloadPath()), "video/*");
					DownloadView.this.startActivity(intent);
				} else if (items[item].equals("Effacer")) {
					DownloadView.this.getData().get_download_video().remove(vid);
				} else if (items[item].equals("Relancer")) {
					DownloadView.this.getData().get_download_video().remove(vid);
					DownloadView.this.getData().downloadvideo(vid.getDownloadVideo());
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
			DownloadVideo vid = video_download
					.elementAt(Integer.parseInt(num));
			Status status = vid.getStatus();
			Log.d("ASI", "Status=" + status.toString());
			if (status == Status.FINISHED) {
				if (vid.getError() != null)
					this.onVideoError(vid);
				else
					this.onVideoFinished(vid);
			} else {
				// proposer d'arrêter le thread
				this.onVideoRunning(vid);
			}

		} catch (Exception e) {
			new ErrorDialog(this, "Traitement de la vidéo", e).show();
		}

	}

}
