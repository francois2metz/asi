package asi.val;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask.Status;
import android.util.Log;

public class SharedData {

	public static SharedData shared;

	private String Cookies;

	private static final String FILENAME_WIDGET = "widget_articles";

	public static final String PREFERENCE = "asi_pref";

	private Vector<DownloadVideo> downloading;

	private Context activity;

	private boolean autologin;
	
	private boolean dlsync;

	public SharedData(Context a) {
		Log.d("ASI", "create shared");
		this.downloading = new Vector<DownloadVideo>();
		SharedData.shared = this;
		activity = a;
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		Cookies = settings.getString("cookies", "phorum_session_v5=deleted");
		autologin = settings.getBoolean("autologin", true);
		dlsync = settings.getBoolean("dlsync", false);
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

	public void downloadvideo(VideoUrl url) {
		DownloadVideo d = new DownloadVideo(this, url);
		if(!dlsync){
			boolean has_running = false;
			for(DownloadVideo dv : downloading){
				if(dv.getStatus() == Status.RUNNING){
					has_running = true;
					break;
				}
			}
			if(!has_running)
				d.execute("");
			
		} else{
			d.execute("");
		}
		this.downloading.add(d);
	}

	public void downloadNextVideo() {
		//lorsque le téléchargement est en série, on lance la vidéo en attente suivante
		if(!dlsync){
			for(DownloadVideo dv : downloading){
				if(dv.getStatus() == Status.PENDING){
					dv.execute("");
					break;
				}
			}
		}
	}
	
	public Vector<DownloadVideo> get_download_video() {
		return (this.downloading);
	}
	
	public void setDlSync(boolean dlsync) {
		this.dlsync = dlsync;
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		Editor editor = settings.edit();
		editor.putBoolean("dlsync", dlsync);
		editor.commit();
	}

	public boolean isDlSync() {
		return dlsync;
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
	
	public void setZoomLevel(int posi) {
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		Editor editor = settings.edit();
		editor.putInt("zoom_level", posi);
		editor.commit();
	}

	public int getZoomLevel() {
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		return settings.getInt("zoom_level", 100);
	}
	
	protected void stopAllDownload() {
		for (int i = 0; i < downloading.size(); i++) {
			DownloadVideo vid = downloading.elementAt(i);
			String status = vid.getStatus().toString();
			if (!status.equals("FINISHED"))
				vid.stopDownload();
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

	public void save_widget_article(Vector<Article> arts) {
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
			Log.d("ASI", "sauver données partagées" + e.getMessage());
		} catch (Exception e) {
			new ErrorDialog(this.activity, "ACCÈS aux données partagées", e)
					.show();
			Log.e("ASI", "ACCES aux données partagées " + e.getMessage());
		}
	}

	@SuppressWarnings("finally")
	public Vector<Article> get_widget_article() {
		Vector<Article> temp = new Vector<Article>();
		try {
			FileInputStream fos = activity.openFileInput(FILENAME_WIDGET);
			InputStreamReader isr = new InputStreamReader(fos);
			BufferedReader objBufferReader = new BufferedReader(isr);
			String strLine;
			int value = 0;
			Article ar = new Article();
			while ((strLine = objBufferReader.readLine()) != null) {
				value++;
				if (value == 1) {
					ar.setTitle(strLine);
				} else if (value == 2) {
					ar.setUri(strLine);
				} else {
					ar.setColor(strLine);
					temp.add(ar);
					ar = new Article();
					value = 0;
				}
			}
			;
			fos.close();
		} catch (java.io.FileNotFoundException e) {
			Log.d("ASI", "sauver données partagées" + e.getMessage());
		} catch (Exception e) {
			new ErrorDialog(this.activity, "ACCÈS aux données partagées", e)
					.show();
			Log.e("ASI", "ACCÈS aux données partagées " + e.getMessage());
		} finally {
			return (temp);
		}
	}

}
