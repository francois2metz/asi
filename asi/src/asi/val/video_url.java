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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class video_url {

	private String dailymotion;
	 
	private String title;

	private int number;
	
	private String image;
	
	public video_url(String url) {
		this.set_dailymotion_url(url);
		this.image="";
	}

	public video_url() {
		this.dailymotion = "";
		this.title = "ASI";
		this.image="";
	}

	public String parse_to_url(String asi) {
		Log.d("ASI","Recherche de video");
		Pattern p = Pattern
				.compile(".*\\<a href\\=\"(http\\:\\/\\/iphone\\.dailymotion\\.com.*)\" title=\"voir.*");
		Matcher m = p.matcher(asi);
		if (m.matches()) {
			Log.d("ASI","Recherche de video trouve");
			String s = m.group(1);
			this.set_dailymotion_url(s);

			Pattern p2 = Pattern.compile(".*\\<img src\\=\"(\\/media.*)\" alt=\"voir.*");
			Matcher m2= p2.matcher(asi);
			if (m2.matches()) {
				this.image=m2.group(1);
				this.image=this.image.replace("player_s", "player_l");
			}
			//<a href="http://iphone.dailymotion.com/video/k5IEm7VP5FwY0m1BoPS" title="voir la vidéo"><img src="/media//library/s290/id28917/player_s.png" alt="voir la vidéo"></a>
			return (s);
		} else
			return (null);
	}

	public String get_href_link_url() throws Exception {
		//String link = this.get_relink_adress();
		//String link = this.get_download_url();
		String href = "<p style=\"text-align: center;\"><a href=\"" + dailymotion+ "&vidnum="+number
				+ "\" target=\"_blank\">" + "<img src=\""+this.image+"\" alt=\"voir la vidéo\">"
				+ "<span><br/>&gt; Cliquez pour voir la vidéo &lt;</span></a></p>";
		// <a href="http://www.bernard-mabille.com/" target="_blank">Bernard
		// Mabille</a>
		return (href);
	}

	public String get_relink_adress() throws Exception {
	BufferedReader in = null;
	String relink = "";
		try {
			HttpURLConnection.getFollowRedirects();
			URL url = new URL(this.get_download_url());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			//conn.setRequestProperty("User-agent", "iPhone");
			in = new BufferedReader(
				new InputStreamReader(conn.getInputStream()));
			relink = conn.getURL().toString();
			conn.disconnect();
		} catch (java.net.ProtocolException e) {
			throw new StopException("Probleme de connection");
		} catch (Exception e) {
			throw e;
		}finally {
			// Dans tous les cas on ferme le bufferedReader s'il n'est pas null
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
		return(relink);
	}

	public String get_download_url() throws Exception {
		StringBuffer sb = new StringBuffer("");
		BufferedReader in = null;
		try {
			URL url = new URL(dailymotion);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);

			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			String ligneCodeHTML;
			Pattern p = Pattern
					.compile(".*type=\"video/x-m4v\" href=\"(.*)\" src=.*");

			// boolean data = false;
			while ((ligneCodeHTML = in.readLine()) != null) {

				Matcher m = p.matcher(ligneCodeHTML);
				if (m.matches())
					sb.append(m.group(1));
			}
			conn.disconnect();
		} catch (java.net.ProtocolException e) {
			throw new StopException("Probleme de connection");
		} catch (Exception e) {
			throw e;
		} finally {
			// Dans tous les cas on ferme le bufferedReader s'il n'est pas null
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
		// On retourne le stringBuffer
		return sb.toString();
	}

	public void set_dailymotion_url(String url) {
		String[] parse = url.split("&vidnum=");
		if(parse.length>1){
			this.setNumber(Integer.parseInt(parse[1]));
			this.dailymotion = parse[0];
		} else {
			this.setNumber(0);
			this.dailymotion = url;
		}
		Log.d("ASI","vidurl="+this.dailymotion);
		Log.d("ASI","vidnum="+this.number);
	}

	public void setTitle(String page_title) {
		this.title = page_title;
	}

	public String getTitle() {
		return title;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}
	public String getTitle_and_number(){
		return(title+" - "+number);
	}
	
}
