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
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class asi_activity extends Activity{
	
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
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
}
