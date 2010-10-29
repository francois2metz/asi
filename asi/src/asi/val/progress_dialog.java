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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.KeyEvent;

public class progress_dialog extends ProgressDialog{
	
	public progress_dialog(Context arg0,final AsyncTask<?, ?, ?> async) {
		super(arg0);
		this.setOnKeyListener(new OnKeyListener(){
			public boolean onKey(DialogInterface arg0, int arg1,
					KeyEvent arg2) {
				if (arg1 == KeyEvent.KEYCODE_BACK && arg2.getRepeatCount() == 0) {
					arg0.dismiss();
					if((async!=null)&&(!async.getStatus().toString().equals("FINISHED")))
						async.cancel(true);
					return true;
				}
				// TODO Auto-generated method stub
				return false;
			}
			
		});
	}
}
