package net.ddns.lokaur.photogallery.network;

import android.net.Uri;
import android.util.Log;

import net.ddns.lokaur.photogallery.model.GalleryItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FlickrFetchr {

    private static final String TAG = "FlickrFetch";

    private static final String API_KEY = "3b3ecf6b3feae448e965554ea27246be";

    private static final String FETCH_RECENTS_METHOD = "flickr.photos.getRecent";

    private static final String SEARCH_METHOD = "flickr.photos.search";

    private static final Uri ENDPOINT = Uri
            .parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .build();

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            int bytesRead;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }

            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchRecentPhotos(int page) {
        return downloadGalleryItems(buildUrl(FETCH_RECENTS_METHOD, null, page));
    }

    public List<GalleryItem> searchPhotos(String query, int page) {
        return downloadGalleryItems(buildUrl(SEARCH_METHOD, query, page));
    }

    private String buildUrl(String method, String query, int page) {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method", method)
                .appendQueryParameter("page", Integer.toString(page));

        if (method.equals(SEARCH_METHOD)) {
            uriBuilder.appendQueryParameter("text", query);
        }

        return uriBuilder.build().toString();
    }

    private List<GalleryItem> downloadGalleryItems(String url) {
        try {
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            return parseItems(jsonBody);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        }

        return new ArrayList<>();
    }

    private List<GalleryItem> parseItems(JSONObject jsonBody) throws IOException, JSONException {
        List<GalleryItem> items = new ArrayList<>();
        JSONObject photosJSONObject = jsonBody.getJSONObject("photos");
        JSONArray photosJSONArray = photosJSONObject.getJSONArray("photo");

        for (int i = 0; i < photosJSONArray.length(); i++) {
            JSONObject photoJSONObject = photosJSONArray.getJSONObject(i);

            GalleryItem item = new GalleryItem();
            item.setId(photoJSONObject.getString("id"));
            item.setCaption(photoJSONObject.getString("title"));

            if (!photoJSONObject.has("url_s"))
                continue;

            item.setUrl(photoJSONObject.getString("url_s"));
            items.add(item);
        }

        return items;
    }
}
