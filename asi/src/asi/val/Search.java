package asi.val;

import android.net.Uri;
import android.provider.BaseColumns;
import asi.val.provider.ArticleProvider;

public class Search implements BaseColumns {
	/**
	 * Search articles
	 */
	public static final Uri SEARCH_URI =
            Uri.parse("content://"+ ArticleProvider.AUTHORITY +"/search");

	public static final String URI_NAME = "uri";
	
	public static final String NB_RESULT_NAME = "results";
	
	/**
	 * Query param
	 */
	public static final String QUERY_PARAM = "chaine";
	
	/**
	 * Allow emissions
	 */
	public static final String EMISSION_PARAM = "in_emission";
	
	/**
	 * Allow folder
	 */
	public static final String FOLDER_PARAM = "in_dossiers";
	
	/**
	 * Allow Chroniques
	 */
	public static final String CHRONIQUE_PARAM = "in_chroniques";
	
	/**
	 * Allow vites
	 */
	public static final String VITE_PARAM = "in_vites";
}
