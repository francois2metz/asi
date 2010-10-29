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
import android.util.Log;

public class reload_activity extends Activity {
	
	private auto_updated update;
	
//	public boolean onCreateOptionsMenu(Menu menu) {
//		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.layout.download_menu, menu);
//		return true;
//	}
//
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle item selection
//		switch (item.getItemId()) {
//		case R.id.download_item:
//			this.load_data();
//			return true;
//		default:
//			return super.onOptionsItemSelected(item);
//		}
//	}
	
	
	public void onPause(){
		super.onPause();
		update.stop_update();
		Log.d("ASI","onpause reload_activity");
	}
	
	public void onResume(){
		super.onResume();
		update=new auto_updated();
		update.start();
		Log.d("ASI","onresume reload_activity");
	}

	protected void load_data() {
		// TODO Auto-generated method stub
		
	}
	
	class auto_updated extends Thread {

		private Boolean stop;
		
		private Runnable run;

		public auto_updated() {
			this.stop = false;
			run = new Runnable() {
			    public void run() {
					reload_activity.this.load_data();
			    }
			};
		}

		public void run() {
			// Code exécuté dans le nouveau thread
			Log.d("ASI", "update_start");
			while (!stop) {
				try {
					Thread.sleep(1500);
					runOnUiThread(run);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Log.e("ASI", "Error_thread" + e.toString());
				}
			}
			Log.d("ASI", "update_stop");

		}

		public void stop_update() {
			this.stop = true;
		}
	}
	
}
