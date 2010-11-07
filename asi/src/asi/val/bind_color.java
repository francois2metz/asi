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

import android.graphics.Color;
import android.view.View;
import android.widget.SimpleAdapter.ViewBinder;

public class bind_color implements ViewBinder {

	public boolean setViewValue(View arg0, Object arg1, String arg2) {
		// TODO Auto-generated method stub
		
		if (arg2.matches("#\\w+")) {
			arg0.setBackgroundColor(Color.parseColor(arg2));
			return (true);
		} else if (arg2.contains("enabled-")) {
			//Log.d("ASI","bind_color_enabled"+" "+arg2);
			if (arg2.contains("true")){
				//Log.d("ASI","bind_color_enabled"+" "+arg2);
				arg0.setBackgroundColor(Color.parseColor("#e7e7e7"));
			}
			else{
				arg0.setBackgroundColor(Color.parseColor("#ffffff"));
			}
			return (true);
		}
		return false;
	}

}
