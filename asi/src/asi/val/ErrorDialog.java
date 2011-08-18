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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class ErrorDialog extends AlertDialog.Builder {

	private String message;

	private String error;
	
	private Context mContext;

	public ErrorDialog(Context arg0, String message, String error) {
		super(arg0);
		// TODO Auto-generated constructor stub
		this.mContext = arg0;
		this.error = error;
		this.message = message;
		this.defined_interface();
	}

	public ErrorDialog(Context arg0, String message, Exception e) {
		super(arg0);
		// TODO Auto-generated constructor stub
		this.mContext = arg0;
		this.error = e.toString() + "\n" + e.getStackTrace()[0] + "\n"
				+ e.getStackTrace()[1];
		this.message = message;
		this.defined_interface();
	}

	private void defined_interface() {		
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.error_view,null);
		TextView text = (TextView) layout.findViewById(R.id.error_titre);
		text.setText(message);
		TextView text2 = (TextView) layout.findViewById(R.id.error_description);
		text2.setText(error);
		this.setView(layout);
		//this.setMessage(message+"\n\n"+error);
		this.setTitle("Erreur");
		this.setPositiveButton("ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		Log.e("ASI",this.error);
	}
	
	
	

}
