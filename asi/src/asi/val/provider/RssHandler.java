package asi.val.provider;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;
import asi.val.Article;

import com.finchframework.finch.rest.RESTfulContentProvider;
import com.finchframework.finch.rest.ResponseHandler;

public class RssHandler implements ResponseHandler {
	private ArticleProvider provider;

	private long catId;

	private String defaultColor;

	public RssHandler(RESTfulContentProvider restfulProvider, long catId, String defaultColor) {
		this.provider = (ArticleProvider) restfulProvider;
		this.catId = catId;
		this.defaultColor = defaultColor;
	}

	public void handleResponse(HttpResponse response, Uri uri)
			throws IOException {
		try {
			Log.d("ASI", "handle rss response "+ uri);
			ArrayList<ContentValues> values = parseContent(response.getEntity());
			for (ContentValues value: values) {
				provider.insert(Article.createUriFor(value.getAsString(Article.URL_NAME)), value);
			}
			provider.insertEntryForCategory(values, catId);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("ASI", "ERROR "+ e.toString());
		}
	}

	protected ArrayList<ContentValues> parseContent(HttpEntity entity) throws ParserConfigurationException, IllegalStateException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document dom = db.parse(entity.getContent());
		NodeList items = dom.getElementsByTagName("item");
		ArrayList<ContentValues> values = new ArrayList<ContentValues>();
		ContentValues value;
		for (int i = 0; i < items.getLength(); i++) {
			value = new ContentValues();
			Node item = items.item(i);
			NodeList artis = item.getChildNodes();
			for (int j = 0; j < artis.getLength(); j++) {
				Node arti = artis.item(j);
				if (arti.getNodeName().equalsIgnoreCase("title"))
					value.put(Article.TITLE_NAME, arti.getFirstChild().getNodeValue());
				if (arti.getNodeName().equalsIgnoreCase("description")) {
					value.put(Article.DESCRIPTION_NAME, Article.parseDescription(arti.getFirstChild().getNodeValue()));
					String color = Article.parseColor(arti.getFirstChild().getNodeValue());
					if (color == null)
						color = this.defaultColor;
					value.put(Article.COLOR_NAME, color);
				}
				if (arti.getNodeName().equalsIgnoreCase("link"))
					value.put(Article.URL_NAME, arti.getFirstChild().getNodeValue());
				if (arti.getNodeName().equalsIgnoreCase("pubDate"))
					value.put(Article.DATE_NAME, Article.parseDate(arti.getFirstChild().getNodeValue()));
			}
			values.add(value);
		}
		return values;
	}
}
