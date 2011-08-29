package asi.val;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class widget_receiver extends AppWidgetProvider {

	public static final String PREFERENCE = "asi_pref";

	public static final String SHOW_CURRENT = "asi.val.action.SHOW_CURRENT";

	public static final String SHOW_NEXT = "asi.val.action.SHOW_NEXT";

	public static final String CHECK_CURRENT = "asi.val.action.CHECK_CURRENT";

	public static final String UPDATE_WIDGET = "asi.val.action.UPDATE_WIDGET";

	public Cursor queryUnreadWithUpdate(Context context) {
		Uri articlesUri = ContentUris.withAppendedId(Article.ARTICLES_URI, 1);
		return queryUnread(context, articlesUri);
	}

	public Cursor queryUnreadWithoutUdate(Context context) {
		Uri articlesUri = Uri.parse(ContentUris.withAppendedId(Article.ARTICLES_URI, 1) +"?"+ Article.UPDATE_PARAM +"=1");
		return queryUnread(context, articlesUri);
	}

	protected Cursor queryUnread(Context context, Uri uri) {
		String where = Article.READ_NAME+" IS NULL";
		return context.getContentResolver().query(uri, null, where, null, null);
	}

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

			views.setTextColor(R.id.widget_color, R.color.color_text);
			views.setTextViewText(R.id.widget_next_texte, "0/0");
			views.setViewVisibility(R.id.widget_check, View.INVISIBLE);
			appWidgetManager.updateAppWidget(appWidgetId, views);
			Cursor c = queryUnreadWithUpdate(context);

			try {
				Log.d("ASI", "widget téléchargement");

				if (c.getCount() == 0)
					throw new StopException("Pas de nouvel article");

				c.moveToFirst();
				views.setTextViewText(R.id.widget_message, c.getString(c.getColumnIndex(Article.TITLE_NAME)));

				views.setTextColor(R.id.widget_color,
						Color.parseColor(c.getString(c.getColumnIndex(Article.COLOR_NAME))));
				views.setTextViewText(R.id.widget_next_texte, "1/" + c.getCount());
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
				c.close();
				this.getData(context).saveWidgetPosi(0);
			}
		}
	}

	private void defined_intent(Context context, RemoteViews views,
			int[] appWidgetIds) {
		// Create an Intent to launch asi main
		Intent intent = new Intent(context, main.class);
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
		intent = new Intent(context, widget_receiver.class);
		// intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		// intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
		intent.setAction(UPDATE_WIDGET);
		intent.putExtra("IDS", appWidgetIds);
		pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT // no flags
				);
		views.setOnClickPendingIntent(R.id.widget_chro, pendingIntent);

		// Check de l'article en cours
		intent = new Intent(context, widget_receiver.class);
		intent.setAction(CHECK_CURRENT);
		pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.widget_vite, pendingIntent);

		intent = new Intent(context, widget_receiver.class);
		intent.setAction(SHOW_CURRENT);
		pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.widget_mes, pendingIntent);

		intent = new Intent(context, widget_receiver.class);
		intent.setAction(SHOW_NEXT);
		pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.widget_next, pendingIntent);
	}

	private void defined_article(RemoteViews views,Context context, Cursor articles, int posi) {
		if (articles.getCount() != 0) {
			articles.moveToPosition(posi);
			views.setTextViewText(R.id.widget_message,
						articles.getString(articles.getColumnIndex(Article.TITLE_NAME)));
			views.setTextColor(R.id.widget_color,
					Color.parseColor(articles.getString(articles.getColumnIndex(Article.COLOR_NAME))));
			this.getData(context).saveWidgetPosi(posi);
			views.setTextViewText(R.id.widget_next_texte, (posi + 1) + "/"
					+ articles.getCount());
			if (articles.getInt(articles.getColumnIndex(Article.READ_NAME)) == 1)
				views.setViewVisibility(R.id.widget_check, View.VISIBLE);
			else
				views.setViewVisibility(R.id.widget_check, View.INVISIBLE);
		} else {
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
			Cursor c = queryUnreadWithoutUdate(context);
			int posi = this.getData(context).getWidgetPosi();
			intent = new Intent(context, Page.class);

			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if (posi < c.getCount()) {
				c.moveToPosition(posi);
				intent.putExtra("id", c.getLong(c.getColumnIndex(BaseColumns._ID)));
				intent.putExtra("title", c.getString(c.getColumnIndex(Article.TITLE_NAME)));
				context.startActivity(intent);
			}
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_asi);
			// On met l'article courant lu et on rend visible l'image check
			this.defined_article(views, context, c, posi);
			c.close();
			views.setViewVisibility(R.id.widget_check, View.VISIBLE);

			ComponentName thisWidget = new ComponentName(context,
					widget_receiver.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(context);
			// On redéfinit les actions sur les éléments du widget
			this.defined_intent(context, views,
					manager.getAppWidgetIds(thisWidget));
			manager.updateAppWidget(thisWidget, views);
		} else if (SHOW_NEXT.equals(action)) {
			Cursor c = queryUnreadWithoutUdate(context);
			int posi = this.getData(context).getWidgetPosi();

			if ((posi + 1) == c.getCount())
				posi = 0;
			else
				posi++;
			Log.d("ASI", "position widget;" + posi);
			Log.d("ASI", "save_articles:" + c.getCount());
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_asi);
			this.defined_article(views, context,c, posi);
			c.close();
			ComponentName thisWidget = new ComponentName(context,
					widget_receiver.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(context);
			// On redéfinit les actions sur les éléments du widget
			this.defined_intent(context, views,
					manager.getAppWidgetIds(thisWidget));
			manager.updateAppWidget(thisWidget, views);
			// appWidgetManager.updateAppWidget(appWidgetId, views);
		} else if (CHECK_CURRENT.equals(action)) {
			Cursor c = queryUnreadWithoutUdate(context);
			int posi = this.getData(context).getWidgetPosi();
			if (posi < c.getCount()) {
				// mark as read
				c.moveToPosition(posi);
				final long id = c.getLong(c.getColumnIndex(BaseColumns._ID));
				Uri articleUri = ContentUris.withAppendedId(Article.ARTICLE_URI, id);
				ContentValues values = new ContentValues();
				values.put(Article.READ_NAME, 1);
			    context.getContentResolver().update(articleUri, values, null, null);
			}
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget_asi);
			this.defined_article(views, context, c, posi);
			c.close();
			ComponentName thisWidget = new ComponentName(context,
					widget_receiver.class);
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

	public SharedData getData(Context c) {
		SharedData data = SharedData.shared;
		if (data == null)
			return (new SharedData(c));
		data.setContext(c);
		return data;
	}

}
