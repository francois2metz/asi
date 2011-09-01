package asi.val;

import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class ArticlesListSearch extends ArticlesList {
	/**
	 * Return the cursor for the current search
	 */
	@Override
	protected Cursor createCursor() {
		Uri uri = Uri.parse(this.getIntent().getExtras().getString("uri"));
		Log.d("ASI", "search with "+ uri);
		return managedQuery(uri, null, null, null, null);
	}
}
