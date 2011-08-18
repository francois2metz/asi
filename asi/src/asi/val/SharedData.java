package asi.val;

import java.util.Vector;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask.Status;
import android.util.Log;

public class SharedData {

	public static SharedData shared;

	private String Cookies;

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

	public void saveWidgetPosi(int posi) {
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		Editor editor = settings.edit();
		editor.putInt("posi_widget", posi);
		editor.commit();
	}

	public int getWidgetPosi() {
		SharedPreferences settings = activity.getSharedPreferences(PREFERENCE,
				0);
		return settings.getInt("posi_widget", 0);
	}

}
