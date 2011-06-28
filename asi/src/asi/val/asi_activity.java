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

public class asi_activity extends Activity {

	protected shared_datas datas;

	public shared_datas get_datas() {
		datas = shared_datas.shared;
		if (datas == null)
			return (new shared_datas(this));
		datas.setContext(this);
		return datas;
	}
	
	public void load_content() {
		
	}
	
	protected void erreur_loading(String error){
		Log.e("ASI",error);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Une erreur réseau s'est produite lors du chargement.")
				.setCancelable(false)
				.setPositiveButton("Réessayer",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								asi_activity.this.load_content();
							}
						})
				.setNegativeButton("Annuler",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								asi_activity.this.finish();
							}
						});
		AlertDialog quitte = builder.create();
		quitte.show();
	}
	
	public void closed_application() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Quitter?");
		builder.setMessage("Tous les téléchargements en cours seront arrêtés")
				.setCancelable(false)
				.setPositiveButton("Oui",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								asi_activity.this.get_datas().stop_all_download();
								asi_activity.this.finish();
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
			Intent i = new Intent(this, download_view.class);
			this.startActivity(i);
			return true;
		case R.id.item2:
			Intent i2 = new Intent(this, SD_video_view.class);
			this.startActivity(i2);
			return true;
		case R.id.item3:
			Intent i3 = new Intent(this, configuration.class);
			this.startActivity(i3);
			return true;
		case R.id.info_item:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("A propos");
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
			this.closed_application();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
