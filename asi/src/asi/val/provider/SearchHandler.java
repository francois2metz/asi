package asi.val.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import com.finchframework.finch.rest.RESTfulContentProvider;
import com.finchframework.finch.rest.ResponseHandler;

import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;
import asi.val.Article;
import asi.val.HTMLEntities;
import asi.val.StopException;

public class SearchHandler implements ResponseHandler {

	private ArticleProvider provider;
	
	private Uri uri;
	
	private long searchId;
	
	private HTMLEntities convert = new HTMLEntities();

	public SearchHandler(RESTfulContentProvider restfulProvider, Uri uri, long searchId) {
		this.provider = (ArticleProvider) restfulProvider;
		this.uri = uri;
		this.searchId = searchId;
	}

	public void handleResponse(HttpResponse response, Uri url)
			throws IOException {
		Log.d("ASI", "handle search response "+ url);
		try {
			ArrayList<ContentValues> values = getArticles(response.getEntity());
			for (ContentValues value: values) {
				provider.insert(Article.createUriFor(value.getAsString(Article.URL_NAME)), value);
			}
			provider.insertEntryForSearch(this.uri, values, this.searchId);
		} catch (Exception e) {
			Log.e("ASI", e.getMessage());
			e.printStackTrace();
		}
	}
	
	public ArrayList<ContentValues> getArticles(HttpEntity entity) throws Exception {
		BufferedReader in = null;
		ArrayList<ContentValues> articles = new ArrayList<ContentValues>();
		try {
			in = new BufferedReader(
					new InputStreamReader(entity.getContent()));

			String ligneCodeHTML;
			boolean desc = false;
			StringBuffer description = new StringBuffer("");
			boolean start = false;
			boolean nextpage = false;
			ContentValues article2 = null;
			//élément de recherche
			Matcher m ;
			Pattern date = Pattern.compile(".*\\<span class\\=\"typo-date\"\\>(.*?)\\<\\/span\\>.*");
			Pattern url = Pattern.compile(".*\\<a href\\=\"(.*?)\".*");
			Pattern title = Pattern.compile(".*class\\=\"typo-titre\"\\>(.*?)\\<\\/a\\>.*");
			Pattern link_next = Pattern.compile("\\<a href\\=\"(.*?)\"\\>(.*?)\\<\\/a\\>");
			
			while ((ligneCodeHTML = in.readLine()) != null) {
				ligneCodeHTML = " " + ligneCodeHTML;
				
				if(articles.size()>30&&ligneCodeHTML.contains("rech-filtres-droite")){
					nextpage=true;
					//article = new Article();
					//article.setTitle("Plus de résultats");
					//article.setDate(">");
					start = false;
				}
				
				if (nextpage && ligneCodeHTML.contains("</div>")){
					nextpage=false;
				}
				//récupérer le lien des pages suivantes et le nombre de résultats comme un article ???
				if (nextpage) {
					if(ligneCodeHTML.contains("typo-info")){
						//article.setDescription_on_recherche(this.convertHtmlToString(ligneCodeHTML));
					}
					m = link_next.matcher(ligneCodeHTML);
					while(m.find()){
						Log.d("ASI", "link-"+m.group(1));//lien
						Log.d("ASI", "num-"+m.group(2));//nom du lien
						if(m.group(2).equalsIgnoreCase("&gt;")){
							Log.d("ASI", "ok");
							//article.setUri("http://www.arretsurimages.net"+m.group(1));
							//articles.add(article);
						}
					}		
				}
					
				//on récupère les informations de chaque article;
				if (ligneCodeHTML.contains("bloc-contenu-5")||ligneCodeHTML.contains("bloc-contenu-6")){//bloc-rech
					start = true;
					article2 = new ContentValues();
					article2.put(Article.COLOR_NAME, Article.parseColorFromSearch(ligneCodeHTML));
				}
				if (ligneCodeHTML.contains("rech-filtres-gauche"))//recherche.php
					start=false;
				if (ligneCodeHTML.contains("\"col_right\""))//dossier
					start=false;

				if(start){
					//recherche des éléments
					m = date.matcher(ligneCodeHTML);
					if(m.find())
						article2.put(Article.DATE_NAME, Article.parseDateFromSearch(m.group(1)));
					
					if(ligneCodeHTML.contains("typo-titre")){
						m = url.matcher(ligneCodeHTML);
						if(m.find())
							article2.put(Article.URL_NAME, "http://www.arretsurimages.net"+m.group(1));
						else
							Log.e("ASI","Pas d'URL");
						m = title.matcher(ligneCodeHTML);
						if(m.find()) {
							String d = m.group(1).replaceFirst("<span.*?</span>", "");
							article2.put(Article.TITLE_NAME, this.convertHtmlToString(d));
						}
						else
							article2.put(Article.TITLE_NAME, ligneCodeHTML);
						//enlever les parties HTML du titre
					}
					
					if (ligneCodeHTML.contains("typo-description")) {
						desc = true;
					}
					if (desc) {
						description.append(ligneCodeHTML);
					}
					
					if(desc && ligneCodeHTML.contains("</div>")){
						desc = false;
						article2.put(Article.DESCRIPTION_NAME, Article.parseDescriptionFromSearch(this.convertHtmlToString(description.toString())));
						description  = new StringBuffer("");
						articles.add(article2);
					}
						
				}

			}
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
		return articles;
	}
	
	private String convertHtmlToString(String html){
		html = html.replaceAll("<.*?>", "");
		html = html.replaceAll("\\s+", " ");
		return (convert.unhtmlentities(html));
	}
}
