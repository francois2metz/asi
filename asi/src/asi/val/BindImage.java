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

import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter.ViewBinder;

public class BindImage implements ViewBinder {

	public boolean setViewValue(View arg0, Object arg1, String arg2) {
		// TODO Auto-generated method stub	
		if(arg2.contains("png-")){
			//Log.d("ASI","bind_image_arg"+" "+arg2);
			ImageView vi = (ImageView) arg0;
			arg2 = arg2.replaceFirst("png-", "");
			vi.setImageResource(Integer.parseInt(arg2));
			return(true);
		}
		return false;
	}

}
