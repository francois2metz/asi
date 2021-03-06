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
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.StringTokenizer;

import android.app.AlertDialog;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class main extends AsiActivity {

	public static final String PREFERENCE = "asi_pref";

	private EditText txt_username;

	private EditText txt_password;

	private boolean autologin;
	
	// private NotificationManager mNotificationManager;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		Log.d("ASI", "Main create");

		txt_username = (EditText) findViewById(R.id.txt_username);
		txt_password = (EditText) findViewById(R.id.txt_password);

		// récupération des préférences
		SharedPreferences settings = getSharedPreferences(PREFERENCE, 0);
		txt_username.setText(settings.getString("user", ""));
		txt_password.setText(settings.getString("pass", ""));

		this.buttonLoad();

		// autologin activé
		this.autologin = this.getData().isAutologin();

		// on teste la version de l'application, si mise à jour, alors ajou
		// d'un message sur les nouveautés
		int old_version = settings.getInt("old_version", 34);
		if (old_version < 41) {
			this.showNewsDialog();
			SharedPreferences.Editor editor = settings.edit();
			editor.putInt("old_version", 41);
			editor.commit();
			this.autologin=false;
		}
	}

	private void showNewsDialog() {
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
		// démarrage de l'autologin
		if (!this.getData().getCookies().equals("phorum_session_v5=deleted") && this.autologin) {
			this.loadPage(false);
			Toast.makeText(this, txt_username.getText() + " connecté.",
					Toast.LENGTH_SHORT).show();
		}
	}

	private void connectAsMember() {
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
			new GetCookiesValue().execute(donneeStr);
		} catch (StopException e) {
			new ErrorDialog(main.this, "Connexion au site", e.toString())
					.show();
		} catch (Exception e) {
			new ErrorDialog(main.this, "Connexion au site", e).show();
		}
	}

	protected void saveLoginPassword(String cookies) {
		// on sauve les préférences car le login/pass ok
		SharedPreferences settings = getSharedPreferences(PREFERENCE, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("user", txt_username.getText().toString());
		editor.putString("pass", txt_password.getText().toString());
		editor.putString("cookies", cookies);
		// Commit the edits!
		editor.commit();
		this.getData().setCookies(cookies);
	}

	private class GetCookiesValue extends AsyncTask<String, Void, String> {
		private final ProgressDialog dialog = new ProgressDialog(main.this,
				this);

		private BufferedReader in;
		private OutputStreamWriter out;

		// can use UI thread here
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
				// récuperation des cookies
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
				Log.e("ASI", "Erreur d'arrêt de la boîte de dialogue");
			}
			if (mess.matches(".*phorum_session_v5.*")) {
				//main.this.setCookies(mess);
				main.this.saveLoginPassword(mess);
				main.this.loadPage(false);
			} else {
				new ErrorDialog(main.this, "Connexion au site", mess).show();
			}
		}
	};

	private void connectToFreeVersion() {
		this.getData().setCookies("phorum_session_v5=deleted");
		this.loadPage(true);
	}

	private void loadPage(boolean free) {
		try {
			Intent i = new Intent(this, MainView.class);
			i.putExtra("free", free);
			this.startActivity(i);
			if(this.data.isAutologin())
				this.finish();

		} catch (Exception e) {
			new ErrorDialog(main.this, "Chargement des catégories", e).show();
		}
		// new page(main.this);
	}

	private void buttonLoad() {
		Button button_login = (Button) findViewById(R.id.login_button);
		// this is the action listener
		button_login.setOnClickListener(new OnClickListener() {
			public void onClick(View viewParam) {
				main.this.connectAsMember();
			}
		}); // end of launch.setOnclickListener

		Button button_gratuit = (Button) findViewById(R.id.gratuit_button);

		button_gratuit.setOnClickListener(new OnClickListener() {
			public void onClick(View viewParam) {
				main.this.connectToFreeVersion();
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

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			// do something on back.
			Log.d("ASI", "main_BackPressed");
			//this.closed_application();
			this.finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.info_menu, menu);
		return true;
	}
}
