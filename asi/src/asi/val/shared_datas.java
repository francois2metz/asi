package asi.val;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.widget.Toast;

public class shared_datas {

	public static shared_datas shared;

	private String Cookies;

	private static final String FILENAME = "article_lus";

	private static final String FILENAME_WIDGET = "widget_articles";

	public static final String PREFERENCE = "asi_pref";

	private Vector<download_video> downloading;

	private Vector<String> articles_lues;

	private Context activity;

	private boolean autologin;

	public shared_datas(Context a) {
		Log.d("ASI", "create shared");
		this.downloading = new Vector<download_video>();
		this.articles_lues = new Vector<String>();
		shared_datas.shared = this;
		activity = a;
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		Cookies = settings.getString("cookies", "phorum_session_v5=deleted");
		autologin = settings.getBoolean("autologin", true);

		this.set_articles_lues();
	}

	public void setContext(Context a) {
		activity = a;
	}

	public Context getContext() {
		return activity;
	}

	public void setCookies(String cookies) {
		Cookies = cookies;
		Log.d("ASI", "set_cookies " + cookies);
	}

	public String getCookies() {
		Log.d("ASI", "get_cookies " + Cookies);
		return Cookies;
	}

	public void downloadvideo(video_url url) {
		download_video d = new download_video(activity);
		d.execute(url);
		this.downloading.add(d);
	}

	public Vector<download_video> get_download_video() {
		return (this.downloading);
	}

	private void set_articles_lues() {
		try {
			FileInputStream fos = activity.openFileInput(FILENAME);
			InputStreamReader isr = new InputStreamReader(fos);
			BufferedReader objBufferReader = new BufferedReader(isr);
			String strLine;
			while ((strLine = objBufferReader.readLine()) != null) {
				this.articles_lues.add(strLine);
			}
			;
			fos.close();
			this.test_length_article_lu();
		} catch (java.io.FileNotFoundException e) {
			Toast.makeText(activity, "Création du fichier de sauvegarde",
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			new erreur_dialog(this.activity, "ACCES aux donnees partagé", e)
					.show();
			Log.e("ASI", "ACCES aux donnees partagé " + e.getMessage());
		}
	}

	private void test_length_article_lu() {
		try {
			if (this.articles_lues.size() > 2000) {
				Vector<String> temp = new Vector<String>();
				Log.d("ASI", "diminue la longeur de la sauvegarde");
				FileOutputStream fos = activity.openFileOutput(FILENAME,
						Context.MODE_PRIVATE);
				for (int i = 1000; i < this.articles_lues.size(); i++) {
					String url_article = this.articles_lues.elementAt(i) + "\n";
					temp.add(this.articles_lues.elementAt(i));
					fos.write(url_article.getBytes());
				}
				fos.flush();
				fos.close();
				this.articles_lues = temp;
			}
		} catch (Exception e) {
			new erreur_dialog(this.activity, "ACCES aux donnees partagé", e)
					.show();
			Log.e("ASI", "ACCES aux donnees partagé " + e.getMessage());
		}
	}

	public void add_articles_lues(String url_article) {
		try {
			if (!this.articles_lues.contains(url_article)) {
				this.articles_lues.add(url_article);
				FileOutputStream fos = activity.openFileOutput(FILENAME,
						Context.MODE_APPEND);
				url_article = url_article + "\n";
				fos.write(url_article.getBytes());
				fos.flush();
				fos.close();
			}
		} catch (Exception e) {
			new erreur_dialog(this.activity, "ACCES aux donnees partagé", e)
					.show();
			Log.e("ASI", "ACCES aux donnees partagé " + e.getMessage());
		}
		// Log.d("ASI","add_url_lu="+url_article);
	}

	public boolean contain_articles_lues(String url_article) {
		if (this.articles_lues.contains(url_article)) {
			// Log.d("ASI","ok_url_lu="+url_article);
			return (true);
		} else {
			// Log.d("ASI","no_url_lu="+url_article);
			return (false);
		}
	}

	public void setAutologin(boolean autologin) {
		this.autologin = autologin;
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		Editor editor = settings.edit();
		editor.putBoolean("autologin", autologin);
		editor.commit();
	}

	public boolean isAutologin() {
		return autologin;
	}

	protected void stop_all_download() {
		for (int i = 0; i < downloading.size(); i++) {
			download_video vid = downloading.elementAt(i);
			String status = vid.getStatus().toString();
			if (!status.equals("FINISHED"))
				vid.Stop_download();
		}
	}

	public void save_widget_posi(int posi) {
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		Editor editor = settings.edit();
		editor.putInt("posi_widget", posi);
		editor.commit();
	}

	public int get_widget_posi() {
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		return settings.getInt("posi_widget", 0);
	}

	public void save_widget_article(Vector<article> arts) {
		try {
			FileOutputStream fos = activity.openFileOutput(FILENAME_WIDGET,
					Context.MODE_PRIVATE);
			String temp = "";
			for (int i = 0; i < arts.size(); i++) {
				temp = arts.elementAt(i).getTitle() + "\n"
						+ arts.elementAt(i).getUri() + "\n"
						+ arts.elementAt(i).getColor() + "\n";
				fos.write(temp.getBytes());
			}
			fos.flush();
			fos.close();
		} catch (java.io.FileNotFoundException e) {
			Log.d("ASI", "sauver donner partager" + e.getMessage());
		} catch (Exception e) {
			new erreur_dialog(this.activity, "ACCES aux donnees partagé", e)
					.show();
			Log.e("ASI", "ACCES aux donnees partagé " + e.getMessage());
		}
	}

	public Vector<article> get_widget_article() {
		Vector<article> temp = new Vector<article>();
		try {
			FileInputStream fos = activity.openFileInput(FILENAME_WIDGET);
			InputStreamReader isr = new InputStreamReader(fos);
			BufferedReader objBufferReader = new BufferedReader(isr);
			String strLine;
			int value = 0;
			article ar = new article();
			while ((strLine = objBufferReader.readLine()) != null) {
				value++;
				if (value == 1) {
					ar.setTitle(strLine);
				} else if (value == 2) {
					ar.setUri(strLine);
				} else {
					ar.setColor(strLine);
					temp.add(ar);
					ar = new article();
					value = 0;
				}
			}
			;
			fos.close();
			this.test_length_article_lu();
		} catch (java.io.FileNotFoundException e) {
			Log.d("ASI", "sauver donner partager" + e.getMessage());
		} catch (Exception e) {
			new erreur_dialog(this.activity, "ACCES aux donnees partagé", e)
					.show();
			Log.e("ASI", "ACCES aux donnees partagé " + e.getMessage());
		} finally {
			return (temp);
		}
	}
}
