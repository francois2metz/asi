package asi.val;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class Configuration extends AsiActivity {

	int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	private ListView maListViewPerso;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Récupération de la listview créée dans le fichier main.xml
		maListViewPerso = (ListView) findViewById(R.id.listview);

		// gratuit = this.getIntent().getExtras().getBoolean("gratuit");
		TextView text = (TextView) findViewById(R.id.list_text);
		// TextView color = (TextView) findViewById(R.id.cat_color);
		text.setText("Paramètres");

		ImageView v = (ImageView) findViewById(R.id.cat_image);
		v.setImageResource(R.drawable.toutlesite);

		this.loadData();

	}

	private void loadData() {
		ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map;
		// Chargement des catégories
		int[] liste = new int[] { R.array.conf_autologin, R.array.conf_dlsync, R.array.conf_zoom};

		Resources res = getResources();
		for (int i = 0; i < liste.length; i++) {
			String[] categorie = res.getStringArray(liste[i]);
			map = new HashMap<String, String>();
			map.put("titre", categorie[1]);
			map.put("description", categorie[2]);
			map.put("type", categorie[0]);
			listItem.add(map);
		}

		// Création d'un SimpleAdapter qui se chargera de mettre les items
		// présent dans notre list (listItem) dans la vue affichageitem
		SimpleAdapter mSchedule = new SimpleAdapter(this.getBaseContext(),
				listItem, R.layout.listview, new String[] { "titre", "description" }, new int[] { R.id.titre, R.id.description });
		// on ajoute le viewbinder
		//mSchedule.setViewBinder(new bind_color());

		// On attribue à notre listView l'adapter que l'on vient de créer
		maListViewPerso.setAdapter(mSchedule);

		// Enfin on met un écouteur d'évènement sur notre listView
		maListViewPerso.setOnItemClickListener(new OnItemClickListener() {
			@SuppressWarnings("unchecked")
			public void onItemClick(AdapterView<?> a, View v, int position,
					long id) {
				// on récupère la HashMap contenant les infos de notre item
				// (titre, description, img)
				HashMap<String, String> map = (HashMap<String, String>) maListViewPerso
						.getItemAtPosition(position);
				if (map.get("type").equalsIgnoreCase("autologin"))
					Configuration.this.onAutologin(map.get("titre"));
				if (map.get("type").equalsIgnoreCase("dlsync"))
					Configuration.this.onDlsync(map.get("titre"));
				if (map.get("type").equalsIgnoreCase("zoom"))
					Configuration.this.onZoom(map.get("titre"));
			}
		});
	}

	private void onAutologin(String titre) {
		final CharSequence[] items = { "Oui", "Non"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(titre);
		int posi = 0;
		if(!this.getData().isAutologin())
			posi=1;
		builder.setSingleChoiceItems(items, posi, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		       // Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
				if (items[item].equals("Oui")) {
					Configuration.this.getData().setAutologin(true);
				} else {
					Configuration.this.getData().setAutologin(false);
				}
				dialog.dismiss();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void onDlsync(String titre) {
		final CharSequence[] items = {"Parallèle", "Série"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(titre);
		int posi = 0;
		if(!this.getData().isDlSync())
			posi=1;
		builder.setSingleChoiceItems(items, posi, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		       // Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
				if (items[item].equals("Parallèle")) {
					Configuration.this.getData().setDlSync(true);
				} else {
					Configuration.this.getData().setDlSync(false);
				}
				dialog.dismiss();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void onZoom(String titre) {
		final CharSequence[] items = new CharSequence[8];
		final int[] zoomlevel = new int[8];
		int posi = 0;
		for(int i = 0; i<8;i++){
			int value = (i*10+80);
			items[i] = value+" %";
			zoomlevel[i]=value;
			if(value==this.getData().getZoomLevel())
				posi=i;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(titre);
		builder.setSingleChoiceItems(items, posi, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		       // Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
				Configuration.this.getData().setZoomLevel(zoomlevel[item]);
				dialog.dismiss();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
}
