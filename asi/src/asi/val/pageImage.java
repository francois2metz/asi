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

import android.os.Bundle;
import android.webkit.WebView;

public class pageImage extends asi_activity {
	/** Called when the activity is first created. */

	private WebView mywebview;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.pageview);

		// Récupération de la listview créée dans le fichier main.xml
		mywebview = (WebView) this.findViewById(R.id.WebViewperso);
		
		this.load_page();
	}

	private void load_page() {
		// ac.replaceView(R.layout.pageview);
		try {
			// on charge l'URL de l'image dans la webview
			mywebview.loadUrl(this.getIntent().getExtras()
			.getString("url"));
			mywebview.getSettings().setBuiltInZoomControls(true);

		} catch (Exception e) {
			new erreur_dialog(this, "Chargement de l'image", e).show();
		}

	}


}
