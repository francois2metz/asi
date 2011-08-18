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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class DownloadVideo extends AsyncTask<String, Void, String> {

	private VideoUrl vid;

	private FileOutputStream out;

	private InputStream in;

	private int totalsize;

	private int size;

	private String error;

	private boolean cancel;
	
	private SharedData share;
	
	public DownloadVideo(SharedData share, VideoUrl v){
		this.share = share;
		vid = v;
		error = null;
		cancel = false;
	}

	protected void onPreExecute() {
		in = null;
		out = null;
		totalsize = -1;
		size = 0;
//		Toast.makeText(share.getContext(), "Démarrage du téléchargement",
//				Toast.LENGTH_LONG).show();
	}

	protected String doInBackground(String... arg) {
		// TODO Auto-generated method stub
		try {
			// on vérifie que l'on peut enregistrer
			String state = Environment.getExternalStorageState();
			if (!Environment.MEDIA_MOUNTED.equals(state))
				throw new StopException(
						"La carte SD n'est pas montée, impossible d'enregistrer");

			HttpURLConnection.getFollowRedirects();
			URL url = new URL(vid.getDownloadUrl());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			in = conn.getInputStream();
			this.totalsize = conn.getContentLength();
			Log.d("ASI", "Download " + this.totalsize / 1000);
			if (in == null)
				throw new RuntimeException("stream is null");

			File temp = this.getDownloadPath();
			temp.createNewFile();
			Log.d("ASI", "vid save=" + temp.getAbsolutePath());

			out = new FileOutputStream(temp);
			byte buf[] = new byte[128];
			do {
				int numread = in.read(buf);
				if (numread <= 0)
					break;
				out.write(buf, 0, numread);
				// comptage du nombre de bytes
				size += buf.length;
				// Lancer une erreur quand le thread est stoppé
				if (cancel)
					throw new StopException("Stop");
			} while (true);
			conn.disconnect();
			Log.d("ASI", "Final-download=" + size / 1000);

			// Ajout de la vidéo au système de lecture
			try {
				MediaScannerConnection medconn = new MediaScannerConnection(
						share.getContext(), null);
				medconn.connect();
				medconn.scanFile(this.getDownloadPath().getAbsolutePath(),
						null);
				medconn.disconnect();
			} catch (Exception e) {
				Log.e("ASI", "ERREUR d'ajout de la vidéo\n" + e.toString());
			}

		} catch (StopException e) {
			return (e.toString());
		} catch (Exception e) {
			String er = e.toString() + "\n" + e.getStackTrace()[0] + "\n"
					+ e.getStackTrace()[1];
			return (er);
		} finally {
			// Dans tous les cas on ferme le bufferedReader s'il n'est pas null
			this.stopBuffer();
		}
		return null;
	}

	private void stopBuffer() {
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

	public void stopDownload() {
		Log.d("ASI", "Cancelled_video");
		cancel = true;
		this.setError("Stop");
	}

	protected void onPostExecute(String er) {
		share.downloadNextVideo();
		this.setError(er);
		if (error == null) {
			String text = "Téléchargement terminé de :\n" + vid.getTitle()
					+ " - " + vid.getNumber();
			Toast.makeText(share.getContext(), text, Toast.LENGTH_LONG).show();
			Log.d("ASI", "Téléchargement terminé avec succes");

		} else {
			Log.e("ASI", "Problème de téléchargement \n" + error);
			Toast.makeText(
					share.getContext(),
					"Problème de téléchargement de :\n" + vid.getTitle()
							+ " - " + vid.getNumber(), Toast.LENGTH_LONG)
					.show();
			// effacer le fichier contenant des erreurs de téléchargement
			File down = this.getDownloadPath();
			if (down.exists())
				down.delete();
		}
	}

	public File getDownloadPath() {
		File path = new File(Environment.getExternalStorageDirectory() + "/ASI");
		if (!path.exists())
			path.mkdir();
		String correctpath = vid.getTitle();
		correctpath = correctpath.replaceAll("\\W", "_");
		correctpath = correctpath.replaceAll("_+", "_");
		correctpath = correctpath.replaceAll("_$", "");
		File temp = new File(path.getAbsolutePath() + "/" + "ASI-"
				+ correctpath + "-" + vid.getNumber() + ".mp4");
		// + "ASI-"+vid.getTitle()+"-"+vid.getNumber()+".mp4");
		return (temp);
	}

	public String getDownloadPercent() {
		if (totalsize == -1)
			return ("En préparation");
		int psize = this.size / 1000;
		int ptot = this.totalsize / 1000;
		String pour = psize + " / " + ptot + " ko";
		return (pour);
	}

	public boolean isComplete() {
		if (this.size < this.totalsize)
			return false;
		return true;
	}

	public String getError() {
		return (this.error);
	}

	private void setError(String er) {
		this.error = er;
	}

	public VideoUrl getDownloadVideo() {
		return this.vid;
	}

}
