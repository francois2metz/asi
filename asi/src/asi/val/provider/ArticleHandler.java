package asi.val.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;
import asi.val.Article;
import asi.val.VideoUrl;

import com.finchframework.finch.rest.RESTfulContentProvider;
import com.finchframework.finch.rest.ResponseHandler;

public class ArticleHandler implements ResponseHandler {

	private RESTfulContentProvider provider;

	public ArticleHandler(RESTfulContentProvider restfulProvider) {
		this.provider = restfulProvider;
	}

	public void handleResponse(HttpResponse response, Uri uri)
			throws IOException {
		Log.d("ArticleProvider", "handle response "+ uri);
		// parse the result
        String content = parseContent(response.getEntity());
        Uri queryUri = Article.createUriFor(uri.toString());
		ContentValues c = new ContentValues();
		c.put(Article.CONTENT_NAME, content);
		c.put(Article.URL_NAME, uri.toString());
		provider.insert(queryUri, c);
	}

	private String parseContent(HttpEntity entity) {
		StringBuffer sb = new StringBuffer();
		BufferedReader in = null;
		try {
			in = new BufferedReader(
					new InputStreamReader(entity.getContent()));
			String ligneCodeHTML;
			boolean data = false;
			boolean start = false;
			// TODOS: manage videos
			int video_count = 0;

			while ((ligneCodeHTML = in.readLine()) != null) {
				ligneCodeHTML = " " + ligneCodeHTML;
				if (ligneCodeHTML.matches(".*class\\=\"contenu\\-html.*"))
					data = true;
				//Pour le forum
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
					sb.append(ligneCodeHTML);
					sb.append("\n");
				}
				// if (ligneCodeHTML.matches(".*class\\=\"bloc\\-contenu.*"))
				// data = true;
				if (ligneCodeHTML.matches(".*fin T.l.chargement.*"))
					data = false;
				if (ligneCodeHTML.matches(".*id\\=\"lire-suite-abo.*")) {
					data = false;
					sb.append(this.center("&gt; Pour lire la suite de cet article, vous devez vous <a href=\"http://www.arretsurimages.net/abonnements.php\">abonner à @si<a> &lt;"));
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

					// on remplace le lien dailymotion par celui pour l'iphone
					ligneCodeHTML = ligneCodeHTML.replaceAll(
							"www.dailymotion.com/video",
							"iphone.dailymotion.com/video");

					// on enlève les animations flash et recupère les vidéos
					// iphone
					if (ligneCodeHTML.matches(".*iphone\\.dailymotion\\.com.*")) {
						VideoUrl video = new VideoUrl();
						String s = video.parseToUrl(ligneCodeHTML);
						if (s == null)
							;
						// ligneCodeHTML = this
						// .center("<span\">&gt; Problème de lecture de la balise vidéo &lt;</span>");
						else {
							video_count++;
							video.setNumber(video_count);
							//videos.add(video);
							ligneCodeHTML = video.getHrefLinkUrl();
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
		} catch (Exception e) {
			e.printStackTrace();
			this.center(e.getMessage());
		} finally {
			// always close the input stream
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (sb.toString().equalsIgnoreCase(""))
			return (this.center("Problème de connexion au serveur : essayez de recharger l'article"));
		return sb.toString();
	}

	private String center(String S) {
		String S2 = "<p style=\"text-align: center;\">";
		S2 = S2 + S + "</p>";
		return (S2);
	}
}
