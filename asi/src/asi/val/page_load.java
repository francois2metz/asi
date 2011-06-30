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
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class page_load {

	private String content;

	private URL url;

	private String cookies;
	
	private ArrayList<video_url> videos;
	
	public ArrayList<video_url> getVideos() {
		return videos;
	}

	public page_load(String u) throws Exception {
		setContent("");
		u.replaceAll(" ", "");
		url = new URL(u);
		//cookies = main.group.getCookies();
		cookies = shared_datas.shared.getCookies();
		videos = new ArrayList<video_url>();
	}

	private String getPage() throws Exception {
		StringBuffer sb = new StringBuffer("");
		BufferedReader in = null;
		try {
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setDoOutput(true);

			// Attach cookies from previous login/user creation so we can use
			// the previous session

			conn.setRequestProperty("Cookie", cookies);
			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));

			String ligneCodeHTML;
			boolean data = false;
			boolean start = false;
			int video_count = 0;

			while ((ligneCodeHTML = in.readLine()) != null) {
				ligneCodeHTML = " " + ligneCodeHTML;
				if (ligneCodeHTML.matches(".*class\\=\"contenu\\-html.*"))
					data = true;
				//Pour le forum
//				if (ligneCodeHTML.matches(".*class\\=\"message prime.*"))
//					data = true;
				
				// on ajoute les lignes typo contenant des informations +
				if (ligneCodeHTML.contains("bloc-bande-contenu"))
					start = true;
				if (ligneCodeHTML.contains("bloc-bande-vite"))
					start = true;
				// modifications
				if ((ligneCodeHTML.matches(".*class\\=\"typo-.*")) & (start)) {
					ligneCodeHTML = ligneCodeHTML.replaceAll("h1", "h2");
					if (ligneCodeHTML.contains("typo-titre"))
						ligneCodeHTML = "<br /><br />" + ligneCodeHTML;
					if (ligneCodeHTML.contains("typo-vite-titre"))
						ligneCodeHTML = ligneCodeHTML.replaceFirst("</a>",
								"</h2>");
					ligneCodeHTML = ligneCodeHTML.replaceFirst(
							"<a href=\".*typo-vite-titre\">",
							"<h2 class=\"typo-titre\">");
					//lien sur les auteurs
//					if (ligneCodeHTML.contains("recherche.php"))
//						ligneCodeHTML = ligneCodeHTML.replaceFirst("</a>",
//								"</span>");
//					ligneCodeHTML = ligneCodeHTML.replaceFirst(
//							"<a href=\"/recherche.php.*?\">", "<span>");
					sb.append(ligneCodeHTML);
					sb.append("\n");
				}

				// if (ligneCodeHTML.matches(".*class\\=\"bloc\\-contenu.*"))
				// data = true;
				if (ligneCodeHTML.matches(".*fin T.l.chargement.*"))
					data = false;
				if (ligneCodeHTML.matches(".*id\\=\"lire-suite-abo.*")) {
					data = false;
					sb.append(this
							.center("&gt; Pour lire la suite de cet article, vous devez vous <a href=\"http://www.arretsurimages.net/abonnements.php\">abonner à @si<a> &lt;"));
				}
				if (ligneCodeHTML
						.matches(".*action\\=\"\\/recherche\\.php\".*"))
					data = false;
				if (ligneCodeHTML
						.matches(".*\\<div id\\=\"footer-contenu\"\\>.*"))
					data = false;
				
				if (data) {
					// on arrête de prendre les contenus typos
					if (start) {
						start = false;
						// sb.append("<div style=\"text-align:justify;\">\n");
					}
					ligneCodeHTML = ligneCodeHTML.replaceAll("(<br />)+",
							"<br />");
					// ligneCodeHTML = ligneCodeHTML.replaceAll("<br />",
					// "<br />--");
					ligneCodeHTML = ligneCodeHTML.replaceAll("<td.*?>", "<p>");
					ligneCodeHTML = ligneCodeHTML.replaceAll("</td>", "</p>");

					// on remplace le lien dailymotion vers celui pour l'iphone
					ligneCodeHTML = ligneCodeHTML.replaceAll(
							"www.dailymotion.com/video",
							"iphone.dailymotion.com/video");

					// on enlève les animations flash et recupère les vidéos
					// iphone
					if (ligneCodeHTML.matches(".*iphone\\.dailymotion\\.com.*")) {
						video_url video = new video_url();
						String s = video.parse_to_url(ligneCodeHTML);
						if (s == null)
							;
						// ligneCodeHTML = this
						// .center("<span\">&gt; Problème de lecture de la balise vidéo &lt;</span>");
						else {
							video_count++;
							video.setNumber(video_count);
							videos.add(video);
							ligneCodeHTML = video.get_href_link_url();
						}
					}
					// on cherche les fichiers mp3
					// on enlève la vidéo flash
					// <object type="application/x-shockwave-flash" </object>
					if (ligneCodeHTML.matches(".*\\<object.*\\<\\/object\\>.*")) {
						// lecture des extraits audios
						Pattern p = Pattern
								.compile(".*value\\=\"mp3\\=(.*?)\\&.*");
						Matcher m = p.matcher(ligneCodeHTML);
						if (m.matches()) {
							String mp3 = m.group(1).replaceAll("%2F", "/");
							ligneCodeHTML = this
									.center("<a href=\""
											+ mp3
											+ "\" target=\"_blank\">&gt; Écouter l'extrait audio &lt;</a>");
						} else {
							// Pattern p2 = Pattern
							// .compile(".*src\\=\"(.*?www.youtube.com.*?)\".*");
							// Matcher m2 = p2.matcher(ligneCodeHTML);
							// if (m2.matches()) {
							// Log.d("ASI","video="+m2.group(1));
							// ligneCodeHTML = this
							// .center("<a href=\""
							// + m2.group(1)
							// +
							// "\" target=\"_blank\">&gt; Regarder la vidéo Youtube &lt;</a>");
							//
							// value="http://www.youtube.com/v/XkRRdth8AHc?fs=1&amp;hl=fr_FR">
							// } else
							ligneCodeHTML = this
									.center("<span>&gt; Cette vidéo n'est pas visible sur Android &lt;</span>");
						}

					}

					// on enlève le bouton télécharger
					if (ligneCodeHTML
							.matches(".*boutons\\/bouton-telecharger\\.png.*"))
						ligneCodeHTML = "";

					// on réduit la fenêtre vide vidéo flash<object width="480"
					// height="360"><param name="movie"
					ligneCodeHTML = ligneCodeHTML.replaceAll(
							"width=\"680\" height=\"\\d+\"",
							"width=\"20\" height=\"20\"");
					// on indique que la vidéo de l'émission est en acte
					// <a href="/faq.php?id=7#7" target="_blank">nos
					// conseils</a>.</em></p>
					if (ligneCodeHTML.matches(".*faq\\.php.*nos conseils.*"))
						ligneCodeHTML = this
								.center("&gt; La vidéo de l'émission est accessible en actes en bas de l'article &lt;");
					// On enlève les grosses flèches et la structure des tables
					ligneCodeHTML = ligneCodeHTML.replaceAll(
							"<span class=\"regardez.*</span>", "");
					ligneCodeHTML = ligneCodeHTML.replaceAll(
							"<p class=\"regardez.*</p>", "");
					ligneCodeHTML = ligneCodeHTML
							.replaceFirst(
									"<img class=\"asiPictoFleche.*alt=\"picto\" />",
									"");
					ligneCodeHTML = ligneCodeHTML.replaceAll("<table.*>", "");
					ligneCodeHTML = ligneCodeHTML.replaceAll("</table>", "");
					ligneCodeHTML = ligneCodeHTML.replaceAll("<tbody>", "");
					ligneCodeHTML = ligneCodeHTML.replaceAll("</tbody>", "");
					ligneCodeHTML = ligneCodeHTML.replaceAll("</tr>", "");
					ligneCodeHTML = ligneCodeHTML.replaceAll("<tr>", "");

					sb.append(ligneCodeHTML);
					sb.append("\n");
					// Log.d("code",ligneCodeHTML);
				}
				// ligneCodeHTML = in.readLine();
			}
			// <div class="contenu-html bg-page-contenu">
			// <!-- Téléchargement des émissions-->
			// <!-- fin Téléchargement -->
			// type="application/x-shockwave-flash"
			if (sb.toString().equalsIgnoreCase(""))
				throw new StopException("La page est vide");
			
		} catch (java.net.ProtocolException e) {
			throw new StopException("Problème de connexion");
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
		if (sb.toString().equalsIgnoreCase(""))
			return (this.center("Problème de connexion au serveur : essayez de recharger l'article"));

		// On retourne le stringBuffer
		// return
		// "<link href=\"/styles/style.css?new=1\" media=\"all\" rel=\"stylesheet\" type=\"text/css\" /> \n"
		return this.get_style() + sb.toString();// + "</div>";
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContent() throws Exception {
		content = this.getPage();
		return content;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public URL getUrl() {
		return url;
	}

	public String center(String S) {
		String S2 = "<p style=\"text-align: center;\">";
		S2 = S2 + S + "</p>";
		return (S2);
	}

	public String get_style() {
		StringBuffer sb2 = new StringBuffer("");
		sb2.append("<style type=\"text/css\">");
		sb2.append(new css_style().get_css_data());
		sb2.append("</style>");
		return (sb2.toString());
	}

}
