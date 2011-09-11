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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class SearchDialog extends AlertDialog.Builder {
	
	private long catId;
	
	private EditText searchField;
	private CheckBox includeEmissions;
	private CheckBox includeFolders;
	private CheckBox includeChro;
	private CheckBox includeVite;
	
	private Context mContext;

	public SearchDialog(Context context, long catId) {
		super(context);
		this.catId = catId;
		this.mContext = context;
		this.showContent();
	}

	private void showContent() {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.recherche_dialog,null);
		includeEmissions = (CheckBox) layout.findViewById(R.id.check_emi);
		includeFolders = (CheckBox) layout.findViewById(R.id.check_doss);
		includeChro = (CheckBox) layout.findViewById(R.id.check_chro);
		includeVite = (CheckBox) layout.findViewById(R.id.check_vite);
		searchField = (EditText) layout.findViewById(R.id.recherche_data);

		this.setView(layout);
		//this.setMessage(message+"\n\n"+error);
		this.setTitle(R.string.search);
		this.setPositiveButton(R.string.search_launch, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				if ("".equalsIgnoreCase(searchField.getText().toString())) {
					return;
				}
				try {
					Intent i = new Intent(mContext, ArticlesListSearch.class);
					Uri searchUri = SearchDialog.this.getSearchUri();
					Log.d("ASI","url-"+searchUri);
					i.putExtra("id", catId);
					i.putExtra("uri", searchUri.toString());
					mContext.startActivity(i);
				} catch (UnsupportedEncodingException e) {
					new ErrorDialog(mContext, "Chargement de la recherche d'articles", e)
							.show();
					dialog.cancel();
				}
				Log.d("ASI","search "+searchField.getText());
				dialog.cancel();
			}
		});
	}
	
	protected Uri getSearchUri() throws UnsupportedEncodingException {
		Uri searchUri = Search.SEARCH_URI;
		String queryString;
		queryString = Search.QUERY_PARAM +"="+ URLEncoder.encode(searchField.getText().toString(),
					"UTF-8")+"&";
		if(includeEmissions.isChecked())
			queryString += Search.EMISSION_PARAM +"=true&";
		if(includeFolders.isChecked())
			queryString += Search.FOLDER_PARAM +"=true&";
		if(includeChro.isChecked())
			queryString += Search.CHRONIQUE_PARAM +"=true&";
		if(includeVite.isChecked())
			queryString += Search.VITE_PARAM +"=true&";
		return Uri.parse(searchUri +"?"+ queryString);
	}
}
