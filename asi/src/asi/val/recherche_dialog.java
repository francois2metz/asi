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

import java.net.URLEncoder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class recherche_dialog extends AlertDialog.Builder {
	
	private String color;
	
	private String titre;
	
	private String image;
	
	private EditText txt_recherche;
	
	private CheckBox check_emi;
	private CheckBox check_doss;
	private CheckBox check_chro;
	private CheckBox check_vite;
	
	private Context mContext;

	public recherche_dialog(Context arg0, String titre, String color,String image ) {
		super(arg0);
		// TODO Auto-generated constructor stub
		this.color=color;
		this.titre=titre;
		this.image=image;
		this.mContext = arg0;
		this.defined_interface();
	}

	private void defined_interface() {
		
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.recherche_dialog,null);
		check_emi = (CheckBox) layout.findViewById(R.id.check_emi);
		check_doss = (CheckBox) layout.findViewById(R.id.check_doss);
		check_chro = (CheckBox) layout.findViewById(R.id.check_chro);
		check_vite = (CheckBox) layout.findViewById(R.id.check_vite);
		txt_recherche = (EditText) layout.findViewById(R.id.recherche_data);
		this.setView(layout);
		//this.setMessage(message+"\n\n"+error);
		this.setTitle("Recherche");
		this.setPositiveButton("Lancer", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				try {
					String url = recherche_dialog.this.defined_url();
					Log.d("ASI","url-"+url);
					Intent i = new Intent(mContext, liste_articles_recherche.class);
					i.putExtra("titre", titre);
					i.putExtra("color", color);
					i.putExtra("image", image);
					i.putExtra("url", url);
					mContext.startActivity(i);

				} catch (Exception e) {
					new erreur_dialog(mContext, "Chargement de la recherche d'articles", e)
							.show();
					dialog.cancel();
				}
				Log.d("ASI","recherche-"+txt_recherche.getText());
				dialog.cancel();
			}
		});
//		this.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int id) {
//				dialog.cancel();
//			}
//		});

	}
	
	private String defined_url() throws Exception{
		if(txt_recherche.getText().toString().equalsIgnoreCase("")){
			//throw new StopException("Aucun element à rechercher");
			Log.d("ASI","rien d'écris");
			txt_recherche.setText("valot");
		}
		StringBuilder donnees = new StringBuilder("http://www.arretsurimages.net/recherche.php");
		donnees.append("?t=0&");
		donnees.append(URLEncoder.encode("chaine", "UTF-8"));
		donnees.append("="
				+ URLEncoder.encode(txt_recherche.getText().toString(),
						"UTF-8") + "&");
		if(check_emi.isChecked())
			donnees.append("in_emission=true&");		
		if(check_doss.isChecked())
			donnees.append("in_dossiers=true&");
		if(check_chro.isChecked())
			donnees.append("in_chroniques=true&");
		if(check_vite.isChecked())
			donnees.append("in_vites=true&");
		//in_dossiers=true&in_emission=true&in_chroniques=true&in_vites=true&
		donnees.append("periode=0&jour1=00&mois1=00&annee1=0&jour2=00&mois2=00&annee2=0&orderby=num");
		//t=0&chaine=hortefeux&in_dossiers=true&is_emission=true&in_chroniques=true&in_vites=true&periode=0&jour1=00&mois1=00&annee1=0&jour2=00&mois2=00&annee2=0&orderby=num
		
		return donnees.toString();
	}
	

}
