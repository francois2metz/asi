/***************************************************************************
    begin                : aug 10 2011
    copyright            : (C) 2011 by François de Metz
    email                : francois@2metz.fr
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

import android.net.Uri;

public class Category {

	/**
	 * Get all categories
	 */
	public static final Uri CATEGORIES_URI =
            Uri.parse("content://"+ ArticleProvider.AUTHORITY +"/categories");
	
	public static final String TITLE_NAME = "title";
	
	public static final String FREE_NAME = "free";
	
	public static final String IMAGE_NAME = "image";
	
	public static final String URL_NAME = "url";
	
	public static final String SUB_CAT = "subcat";
	
	public static final String COLOR_NAME = "color";
}
