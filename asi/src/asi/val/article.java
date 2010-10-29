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

public class article {

	private String title;

	private String description;

	private String uri;

	private String date;

	private String color;

	public article(String t, String d, String u) {
		this.title = t;
		this.description = d;
		this.uri = u;
		this.color=null;
	}

	public article() {
		this.title = "T";
		this.description = "d";
		this.uri = "";
		this.date = "";
		this.color=null;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String des) {
		des = des.replaceAll("\n", "");
		//des = des.replaceAll("<br />", "");
		String[] parse = des.split("<br />");
		if(parse.length>1){
			this.determined_color(parse[0]);
			des=parse[1];
		}
		int fin = des.length();
		if (fin > 100)
			fin = 100;
		des = des.substring(0, fin);
		des = des.replaceFirst(" \\w+$", "");
		des = des + " ...";
		this.description = des;
	}

	private void determined_color(String title) {
		// TODO Auto-generated method stub
		//Log.d("ASI","cat= "+title);
		if(title.contains("Vite dit"))
			this.color="#FEC763";
		else if(title.contains("chronique"))
			this.color="#FF398E";
		else if(title.contains("mission"))
			this.color="#3A36FF";
		else if(title.contains("Article"))
			this.color="#3399FF";	
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public void setDate(String dat) {
		// <pubDate>Tue, 31 Aug 2010 19:37:08 +0200</pubDate>
		dat = dat.replaceAll("\\+0\\d+", "");
		this.date = dat.replaceFirst("^.*, ", "");
	}

	public String getDate() {
		return date;
	}

	public void setColor(String color2) {
		if (this.color == null)
			this.color = color2;
	}

	public String getColor() {
		if(this.color==null)
			return("#ACB7C6");
		return color;
	}

}
