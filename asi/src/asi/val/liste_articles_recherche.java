package asi.val;

import java.util.Vector;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
public class liste_articles_recherche extends liste_articles {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void load_content() {

		// Etat de la liste view
		state = null;

		// recuperation des artciles via l'url
		String url = this.getIntent().getExtras().getString("url");
		new get_recherche_url().execute(url);
	}

	protected void do_on_recherche_item(String url) {
		// a faire uniquement dans les recherches
		new get_recherche_url().execute(url);
	}

	public void set_articles(Vector<article> art) {
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

	private class get_recherche_url extends AsyncTask<String, Void, String> {
		private final progress_dialog dialog = new progress_dialog(
				liste_articles_recherche.this, this);

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
				page_recherche re = new page_recherche(args[0]);
				liste_articles_recherche.this.set_articles(re.getArticles());
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
					Log.e("ASI", "Erreur d'arret de la boite de dialog");
				}
			}
			if (error == null)
				liste_articles_recherche.this.load_data();
			else {
				//new erreur_dialog(liste_articles_recherche.this,"Chargement des articles", error).show();
				liste_articles_recherche.this.erreur_loading(error);
			}
			// Main.this.output.setText(result);
		}
	}

}
