package asi.val;

import java.util.Vector;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class WidgetReceiver extends AppWidgetProvider {

	public static final String PREFERENCE = "asi_pref";

	public static final String SHOW_CURRENT = "asi.val.action.SHOW_CURRENT";

	public static final String SHOW_NEXT = "asi.val.action.SHOW_NEXT";

	public static final String CHECK_CURRENT = "asi.val.action.CHECK_CURRENT";

	public static final String UPDATE_WIDGET = "asi.val.action.UPDATE_WIDGET";

	private Vector<Article> articles;

	private String url = "http://www.arretsurimages.net/tous-les-contenus.rss";

	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		final int N = appWidgetIds.length;

		for (int i = 0; i < N; i++) {
			Log.d("ASI", "Widget update:" + appWidgetIds[i]);
			int appWidgetId = appWidgetIds[i];
			// Lien vers la page courante d'ASI
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_asi);
			// On définit les actions sur les éléments du widget
			this.defined_intent(context, views, appWidgetIds);

			views.setTextViewText(R.id.widget_message, "Mise à jour en cours");
			// views.setInt(R.id.widget_message, "setBackgroundResource",
			// R.color.color_text);
			views.setTextColor(R.id.widget_color, R.color.color_text);
			views.setTextViewText(R.id.widget_next_texte, "0/0");
			views.setViewVisibility(R.id.widget_check, View.INVISIBLE);
			appWidgetManager.updateAppWidget(appWidgetId, views);

			try {
				RssDownload d = new RssDownload(url);
				Log.d("ASI", "widget téléchargement");
				if (i == 0) {
					d.getRssArticles();
					articles = d.getArticles();
					// on recherche si ils sont déjà lus
					articles = this.get_new_articles(articles, context);
					Log.d("ASI", "download_articles:" + articles.size());
				} else
					articles = this.get_datas(context).get_widget_article();
				if (articles.size() == 0)
					throw new StopException("Pas de nouvel article");
				views.setTextViewText(R.id.widget_message, articles
						.elementAt(0).getTitle());
				// views.setInt(R.id.widget_message, "setBackgroundResource",
				// Color.parseColor(articles.elementAt(0).getColor()));
				views.setTextColor(R.id.widget_color,
						Color.parseColor(articles.elementAt(0).getColor()));
				views.setTextViewText(R.id.widget_next_texte,
						"1/" + articles.size());

				// Tell the AppWidgetManager to perform an update on the current
				// App Widget
				appWidgetManager.updateAppWidget(appWidgetId, views);
				Toast.makeText(context, "ASI widget à jour", Toast.LENGTH_SHORT)
						.show();
			} catch (StopException e) {
				views.setTextViewText(R.id.widget_message,
						"Aucun article non lu");
				appWidgetManager.updateAppWidget(appWidgetId, views);
				Log.e("ASI", "Error widget " + e.getMessage());
			} catch (Exception e) {
				views.setTextViewText(R.id.widget_message,
						"Erreur de mise à jour");
				appWidgetManager.updateAppWidget(appWidgetId, views);
				Log.e("ASI", "Error widget " + e.getMessage());
			} finally {
				if (articles == null) {
					articles = new Vector<Article>();
				}
				this.get_datas(context).save_widget_article(articles);
				this.get_datas(context).save_widget_posi(0);
			}
		}
	}

	private void defined_intent(Context context, RemoteViews views,
			int[] appWidgetIds) {
		// Create an Intent to launch asi main
		Intent intent = new Intent(context, Main.class);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setAction(Intent.ACTION_MAIN);
		intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		views.setOnClickPendingIntent(R.id.widget_asi, pendingIntent);

		// lien vers la page des vidéos
		intent = new Intent(context, DownloadView.class);
		pendingIntent = PendingIntent.getActivity(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.widget_art, pendingIntent);

		// lien vers la page des téléchargements
		intent = new Intent(context, VideoViewSD.class);
		pendingIntent = PendingIntent.getActivity(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.widget_emi, pendingIntent);

		// update du widget
		intent = new Intent(context, WidgetReceiver.class);
		// intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		// intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
		intent.setAction(UPDATE_WIDGET);
		intent.putExtra("IDS", appWidgetIds);
		pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT // no flags
				);
		views.setOnClickPendingIntent(R.id.widget_chro, pendingIntent);

		// Check de l'article en cours
		intent = new Intent(context, WidgetReceiver.class);
		intent.setAction(CHECK_CURRENT);
		pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.widget_vite, pendingIntent);

		intent = new Intent(context, WidgetReceiver.class);
		intent.setAction(SHOW_CURRENT);
		pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.widget_mes, pendingIntent);

		intent = new Intent(context, WidgetReceiver.class);
		intent.setAction(SHOW_NEXT);
		pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.widget_next, pendingIntent);
	}

	private void defined_article(RemoteViews views,Context context,int posi){
		if (articles.size() != 0) {
			views.setTextViewText(R.id.widget_message,
					articles.elementAt(posi).getTitle());
			// views.setInt(R.id.widget_message, "setBackgroundResource",
			// Color.parseColor(articles.elementAt(posi).getColor()));
			views.setTextColor(R.id.widget_color,
					Color.parseColor(articles.elementAt(posi).getColor()));
			this.get_datas(context).save_widget_posi(posi);
			views.setTextViewText(R.id.widget_next_texte, (posi + 1) + "/"
					+ articles.size());
			if (this.get_datas(context).containArticlesRead(
					articles.elementAt(posi).getUri()))
				views.setViewVisibility(R.id.widget_check, View.VISIBLE);
			else
				views.setViewVisibility(R.id.widget_check, View.INVISIBLE);
		}else{
			views.setTextViewText(R.id.widget_message,"Aucun article non lu");
			views.setTextColor(R.id.widget_color, R.color.color_text);
			views.setTextViewText(R.id.widget_next_texte, "0/0");
			views.setViewVisibility(R.id.widget_check, View.INVISIBLE);
		}
	}
	
	public void onReceive(Context context, Intent intent) {
		// v1.5 fix that doesn't call onDelete Action
		final String action = intent.getAction();

		Log.d("ASI", "Action=" + action);
		if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
			final int appWidgetId = intent.getExtras().getInt(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				this.onDeleted(context, new int[] { appWidgetId });
			}
		} else if (SHOW_CURRENT.equals(action)) {
			articles = this.get_datas(context)
					.get_widget_article();
			int posi = this.get_datas(context).get_widget_posi();
			intent = new Intent(context, Page.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if (posi < articles.size()) {
				intent.putExtra("url", articles.elementAt(posi).getUri());
				intent.putExtra("titre", articles.elementAt(posi).getTitle());
				context.startActivity(intent);
			}
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_asi);
			// On met l'article courant lu et on rend visible l'image check
			this.defined_article(views, context, posi);
			views.setViewVisibility(R.id.widget_check, View.VISIBLE);
			
			ComponentName thisWidget = new ComponentName(context,
					WidgetReceiver.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(context);
			// On redéfinit les actions sur les éléments du widget
			this.defined_intent(context, views,
					manager.getAppWidgetIds(thisWidget));
			manager.updateAppWidget(thisWidget, views);		
		} else if (SHOW_NEXT.equals(action)) {
			articles = this.get_datas(context)
					.get_widget_article();
			int posi = this.get_datas(context).get_widget_posi();

			if ((posi + 1) == articles.size())
				posi = 0;
			else
				posi++;
			Log.d("ASI", "position widget;" + posi);
			Log.d("ASI", "save_articles:" + articles.size());
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_asi);
			this.defined_article(views, context, posi);
//			views.setViewVisibility(R.id.widget_check, View.INVISIBLE);
//			if (articles.size() != 0) {
//				views.setTextViewText(R.id.widget_message,
//						articles.elementAt(posi).getTitle());
//				// views.setInt(R.id.widget_message, "setBackgroundResource",
//				// Color.parseColor(articles.elementAt(posi).getColor()));
//				views.setTextColor(R.id.widget_color,
//						Color.parseColor(articles.elementAt(posi).getColor()));
//				this.get_datas(context).save_widget_posi(posi);
//				views.setTextViewText(R.id.widget_next_texte, (posi + 1) + "/"
//						+ articles.size());
//				if (this.get_datas(context).contain_articles_lues(
//						articles.elementAt(posi).getUri()))
//					views.setViewVisibility(R.id.widget_check, View.VISIBLE);
//			}

			ComponentName thisWidget = new ComponentName(context,
					WidgetReceiver.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(context);
			// On redéfinit les actions sur les éléments du widget
			this.defined_intent(context, views,
					manager.getAppWidgetIds(thisWidget));
			// int[] temp = manager.getAppWidgetIds(thisWidget);
			// for(int z=0;z<temp.length;z++)
			// Log.d("ASI","intent update of:"+temp[z]);
			manager.updateAppWidget(thisWidget, views);
			// appWidgetManager.updateAppWidget(appWidgetId, views);
		} else if (CHECK_CURRENT.equals(action)) {
			articles = this.get_datas(context)
					.get_widget_article();
			int posi = this.get_datas(context).get_widget_posi();
			if (posi < articles.size())
				this.get_datas(context).addArticlesRead(
						articles.elementAt(posi).getUri());
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_asi);
			// On met l'article courant lu et on rend visible l'image check
			//views.setViewVisibility(R.id.widget_check, View.VISIBLE);
			this.defined_article(views, context, posi);
			
			ComponentName thisWidget = new ComponentName(context,
					WidgetReceiver.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(context);
			// On redéfinit les actions sur les éléments du widget
			this.defined_intent(context, views,
					manager.getAppWidgetIds(thisWidget));
			manager.updateAppWidget(thisWidget, views);
		} else if (UPDATE_WIDGET.equals(action)) {
			int[] ids = intent.getIntArrayExtra("IDS");
			this.onUpdate(context, AppWidgetManager.getInstance(context), ids);
		} else {
			super.onReceive(context, intent);
		}
	}

	public void onDisabled(Context context) {
		super.onDisabled(context);
		Log.d("ASI", "disabled widget");
	}

	public void onEnabled(Context context) {
		super.onEnabled(context);
		Log.d("ASI", "enabled widget");
	}

	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		Log.d("ASI", "deleted widget");
	}

	private Vector<Article> get_new_articles(Vector<Article> articles2,
			Context c) {
		Vector<Article> ar = new Vector<Article>();
		for (int i = 0; i < articles2.size(); i++) {
			if (!this.get_datas(c).containArticlesRead(
					articles2.elementAt(i).getUri()))
				ar.add(articles2.elementAt(i));
		}
		return (ar);
	}

	public SharedData get_datas(Context c) {
		SharedData datas = SharedData.shared;
		if (datas == null)
			return (new SharedData(c));
		datas.setContext(c);
		return datas;
	}

}
