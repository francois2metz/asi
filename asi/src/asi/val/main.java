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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.StringTokenizer;
import java.util.Vector;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class main extends asi_activity {

	private String Cookies;

	public static main group;

	public static final String PREFERENCE = "asi_pref";

	private static final String FILENAME = "article_lus";

	private EditText txt_username;

	private EditText txt_password;

	private Vector<download_video> downloading;

	private Vector<String> articles_lues;

	private boolean autologin;

	// private NotificationManager mNotificationManager;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		Log.d("ASI", "Main create");
		// Récupération de la listview créée dans le fichier main.xml
		txt_username = (EditText) findViewById(R.id.txt_username);

		txt_password = (EditText) findViewById(R.id.txt_password);

		// recuperation des preferences
		SharedPreferences settings = getSharedPreferences(PREFERENCE, 0);
		txt_username.setText(settings.getString("user", ""));
		txt_password.setText(settings.getString("pass", ""));

		this.button_load();

		// On lie l'activity
		main.group = this;

		// on initialise le vector de telechargement et on lis les anciens
		// fichiers
		downloading = new Vector<download_video>();
		this.set_articles_lues();

		// autologin activé
		this.autologin = true;

		// on recupere l'ancien cookie
		Cookies = settings.getString("cookies", "");

		// on teste la version de l'application, si mise à jour, alors ajout
		// d'un message sur les nouveautés
		int old_version = settings.getInt("old_version", 29);
		if (old_version < 30) {
			this.show_news_dialog();
			SharedPreferences.Editor editor = settings.edit();
			editor.putInt("old_version", 30);
			editor.commit();
			this.autologin=false;
		}
	}

	private void show_news_dialog() {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Nouveautés");
		builder.setMessage(R.string.news);
		builder.setCancelable(false);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		builder.create().show();
	}

	public void onStart() {
		super.onStart();

		// demarage de l'autologin
		if (!Cookies.equals("") && this.autologin) {
			this.load_page(false);
			Toast.makeText(this, txt_username.getText() + " connecté.",
					Toast.LENGTH_SHORT).show();
		}
	}

	private void connect_to_abonne() {
		try {
			if (txt_username.getText().toString().equalsIgnoreCase(""))
				throw new StopException("Login vide");
			if (txt_password.getText().toString().equalsIgnoreCase(""))
				throw new StopException("Mot de passe vide");

			StringBuilder donnees = new StringBuilder("");
			donnees.append(URLEncoder.encode("username", "UTF-8"));
			donnees.append("="
					+ URLEncoder.encode(txt_username.getText().toString(),
							"UTF-8") + "&");
			donnees.append(URLEncoder.encode("password", "UTF-8"));
			donnees.append("="
					+ URLEncoder.encode(txt_password.getText().toString(),
							"UTF-8"));
			String donneeStr = donnees.toString();
			new get_cookies_value().execute(donneeStr);
		} catch (StopException e) {
			new erreur_dialog(main.this, "Connexion au site", e.toString())
					.show();
		} catch (Exception e) {
			new erreur_dialog(main.this, "Connexion au site", e).show();
		}
	}

	protected void save_login_password() {
		// on sauve les preferences car le login/pass ok
		SharedPreferences settings = getSharedPreferences(PREFERENCE, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("user", txt_username.getText().toString());
		editor.putString("pass", txt_password.getText().toString());
		editor.putString("cookies", Cookies);
		this.autologin = true;
		// Commit the edits!
		editor.commit();
	}

	private class get_cookies_value extends AsyncTask<String, Void, String> {
		private final progress_dialog dialog = new progress_dialog(main.this,
				this);

		private BufferedReader in;
		private OutputStreamWriter out;

		// // can use UI thread here
		protected void onPreExecute() {
			in = null;
			out = null;
			this.dialog.setMessage("Connexion...");
			this.dialog.show();
		}

		// automatically done on worker thread (separate from UI thread)
		protected String doInBackground(String... args) {
			try {
				URL url_login = new URL(
						"http://www.arretsurimages.net/forum/login.php");
				HttpURLConnection connection = (HttpURLConnection) url_login
						.openConnection();
				connection.setDoOutput(true);
				connection.setInstanceFollowRedirects(false);

				// On écrit les données via l'objet OutputStream
				out = new OutputStreamWriter(connection.getOutputStream());
				out.write(args[0]);
				out.flush();

				in = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));
				// sb.append("<p>"+connection.getHeaderField("Location")+"</p>");
				// recuperation des cookies
				String cookies = connection.getHeaderField("Set-Cookie");
				if (cookies == null)
					throw new StopException("Problème de cookies");
				else {
					StringTokenizer st = new StringTokenizer(cookies, ";");
					if (st.hasMoreTokens()) {
						String token = st.nextToken();

						String name = token.substring(0, token.indexOf("="))
								.trim();

						String value = token.substring(token.indexOf("=") + 1,
								token.length()).trim();
						if (name.equalsIgnoreCase("phorum_session_v5")) {
							if (value.equalsIgnoreCase("deleted"))
								throw new StopException(
										"Login / mot de passe incorrect");
							// else
							// throw new
							// StopException("Login / mot de passe ok");
						}

					}
				}
				connection.disconnect();
				return cookies;
			} catch (StopException e) {
				return (e.toString());
			} catch (Exception e) {
				String error = e.toString() + "\n" + e.getStackTrace()[0]
						+ "\n" + e.getStackTrace()[1];
				return (error);
			} finally {
				// Dans tous les cas on ferme le bufferedReader s'il n'est pas
				// null
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
					}
				}
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
					}
				}
			}

		}

		protected void onPostExecute(String mess) {
			try {
				if (dialog.isShowing())
					dialog.dismiss();
			} catch (Exception e) {
				Log.e("ASI", "Erreur d'arret de la boite de dialog");
			}
			if (mess.matches(".*phorum_session_v5.*")) {
				main.this.setCookies(mess);
				main.this.save_login_password();
				main.this.load_page(false);
			} else {
				new erreur_dialog(main.this, "Connexion au site", mess).show();
			}
		}
	};

	private void connect_to_gratuit() {
		this.setCookies("phorum_session_v5=deleted");
		this.load_page(true);
	}

	private void load_page(boolean gratuit) {
		try {
			Intent i = new Intent(this, main_view.class);
			i.putExtra("gratuit", gratuit);
			this.startActivity(i);

		} catch (Exception e) {
			new erreur_dialog(main.this, "Chargement des catégories", e).show();
		}
		// new page(main.this);
	}

	private void button_load() {
		Button button_login = (Button) findViewById(R.id.login_button);
		// this is the action listener
		button_login.setOnClickListener(new OnClickListener() {
			public void onClick(View viewParam) {
				main.this.connect_to_abonne();
			}
		}); // end of launch.setOnclickListener

		Button button_gratuit = (Button) findViewById(R.id.gratuit_button);

		button_gratuit.setOnClickListener(new OnClickListener() {
			public void onClick(View viewParam) {
				main.this.connect_to_gratuit();
			}
		});

		Button button_abonnement = (Button) findViewById(R.id.abonnement_button);

		button_abonnement.setOnClickListener(new OnClickListener() {
			public void onClick(View viewParam) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				Uri u = Uri
						.parse("http://www.arretsurimages.net/abonnements.php");
				i.setData(u);
				startActivity(i);
			}
		});
	}

	public void setCookies(String cookies) {
		Cookies = cookies;
	}

	public String getCookies() {
		return Cookies;
	}

	public void downloadvideo(video_url url) {
		download_video d = new download_video();
		d.execute(url);
		this.downloading.add(d);
	}

	public Vector<download_video> get_download_video() {
		return (this.downloading);
	}

	private void set_articles_lues() {
		try {
			this.articles_lues = new Vector<String>();
			FileInputStream fos = openFileInput(FILENAME);
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
			Toast.makeText(this, "Création du fichier de sauvegarde",
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			new erreur_dialog(main.this, "Lecture des articles lus", e).show();
		}
	}

	private void test_length_article_lu() {
		try {
			if (this.articles_lues.size() > 2000) {
				Vector<String> temp = new Vector<String>();
				Log.d("ASI", "diminue la longeur de la sauvegarde");
				FileOutputStream fos = openFileOutput(FILENAME,
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
			new erreur_dialog(main.this, "Sauvegarde des articles lus", e)
					.show();
		}

	}

	public void add_articles_lues(String url_article) {
		try {
			if (!this.articles_lues.contains(url_article)) {
				this.articles_lues.add(url_article);
				FileOutputStream fos = openFileOutput(FILENAME,
						Context.MODE_APPEND);
				url_article = url_article + "\n";
				fos.write(url_article.getBytes());
				fos.flush();
				fos.close();
			}
		} catch (Exception e) {
			new erreur_dialog(main.this, "Sauvegarde des articles lus", e)
					.show();
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

	// public void onBackPressed(){
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			// do something on back.
			Log.d("ASI", "main_BackPressed");
			this.closed_application();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	private void closed_application() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Quitter?");
		builder.setMessage("Tous les téléchargements en cours seront arrêtés")
				.setCancelable(false)
				.setPositiveButton("Oui",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								main.this.stop_all_download();
								main.this.finish();
							}
						})
				.setNegativeButton("Non",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		AlertDialog quitte = builder.create();
		quitte.show();
	}

	protected void stop_all_download() {
		for (int i = 0; i < downloading.size(); i++) {
			download_video vid = downloading.elementAt(i);
			String status = vid.getStatus().toString();
			if (!status.equals("FINISHED"))
				vid.Stop_download();
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.info_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.info_item:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("A propos");
			builder.setMessage(R.string.apropos);
			// "Application Non officielle réalisée par un asinaute.\n\n"
			// + "Permet d'accéder aux contenus du site d'Arrêt sur Images.\n\n"
			// +
			// "v1.2.6 : Historique des articles lus, icones, mise en forme des articles, accès aux contenus audio des articles\n\n"
			// + "Les retours de bugs sont les bienvenues")
			builder.setPositiveButton("ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			builder.show();
			return true;
		case R.id.close_item:
			this.closed_application();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void is_autologin(boolean b) {
		// TODO Auto-generated method stub
		this.autologin = b;
	}
}