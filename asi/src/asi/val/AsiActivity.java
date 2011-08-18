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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class AsiActivity extends Activity {

	protected SharedData datas;

	public SharedData get_datas() {
		datas = SharedData.shared;
		if (datas == null)
			return (new SharedData(this));
		datas.setContext(this);
		return datas;
	}
	
	public void loadContent() {
		
	}
	
	protected void onLoadError(String error){
		Log.e("ASI",error);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Une erreur réseau s'est produite lors du chargement.")
				.setCancelable(false)
				.setPositiveButton("Réessayer",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								AsiActivity.this.loadContent();
							}
						})
				.setNegativeButton("Annuler",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								AsiActivity.this.finish();
							}
						});
		AlertDialog quitte = builder.create();
		quitte.show();
	}
	
	public void onCloseApp() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Quitter ?");
		builder.setMessage("Tous les téléchargements en cours seront arrêtés")
				.setCancelable(false)
				.setPositiveButton("Oui",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								AsiActivity.this.get_datas().stopAllDownload();
								AsiActivity.this.finish();
							}
						})
				.setNegativeButton("Non",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		AlertDialog quitte = builder.create();
		quitte.show();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.generic_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.item1:
			Intent i = new Intent(this, DownloadView.class);
			this.startActivity(i);
			return true;
		case R.id.item2:
			Intent i2 = new Intent(this, VideoViewSD.class);
			this.startActivity(i2);
			return true;
		case R.id.item3:
			Intent i3 = new Intent(this, Configuration.class);
			this.startActivity(i3);
			return true;
		case R.id.info_item:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("À propos");
			builder.setMessage(R.string.apropos);

			builder.setPositiveButton("ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			builder.show();
			return true;
		case R.id.close_item:
			this.onCloseApp();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
