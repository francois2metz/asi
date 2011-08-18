package asi.val;

import java.util.Vector;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
public class ArticlesListSearch extends ArticlesList {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void loadContent() {

		// État de la liste view
		state = null;

		// récuperation des articles via l'URL
		String url = this.getIntent().getExtras().getString("url");
		new SearchUrl().execute(url);
	}

	protected void onSearchItem(String url) {
		// à faire uniquement dans les recherches
		new SearchUrl().execute(url);
	}

	public void setArticles(Vector<Article> art) {
		if (articles == null)
			this.articles = art;
		else {
			if (articles.size() > 0)
				this.articles.removeElementAt(articles.size() - 1);
			for (int i = 0; i < art.size(); i++) {
				articles.add(art.elementAt(i));
			}
		}
	}

	private class SearchUrl extends AsyncTask<String, Void, String> {
		private final ProgressDialog dialog = new ProgressDialog(
				ArticlesListSearch.this, this);

		// can use UI thread here
		protected void onPreExecute() {
			this.dialog.setMessage("Chargement...");
			this.dialog.show();
		}

		// automatically done on worker thread (separate from UI thread)
		protected String doInBackground(String... args) {
			// List<String> names =
			// Main.this.application.getDataHelper().selectAll();
			try {
				SearchPage re = new SearchPage(args[0]);
				ArticlesListSearch.this.setArticles(re.getArticles());
			} catch (Exception e) {
				String error = e.toString() + "\n" + e.getStackTrace()[0]
						+ "\n" + e.getStackTrace()[1];
				return (error);
			}
			return null;
		}

		protected void onPostExecute(String error) {
			if (this.dialog.isShowing()) {
				try {
					this.dialog.dismiss();
				} catch (Exception e) {
					Log.e("ASI", "Erreur d'arrêt de la boîte de dialogue");
				}
			}
			if (error == null)
				ArticlesListSearch.this.loadData();
			else {
				//new erreur_dialog(liste_articles_recherche.this,"Chargement des articles", error).show();
				ArticlesListSearch.this.onLoadError(error);
			}
			// Main.this.output.setText(result);
		}
	}

}
