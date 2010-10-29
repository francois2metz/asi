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

public class StopException extends Exception {

	private static final long serialVersionUID = -8349075134826655919L;

	private String error;
	
	public StopException(){
		super();
	}
	public StopException(String S){
		super(S);
		error=S;
	}
	public String toString(){
		return(error);
	}
}
