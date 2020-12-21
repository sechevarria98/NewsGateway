package com.example.gateway;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class SourceLoaderRunnable implements Runnable {
    private static final String TAG = "SourceLoaderRunnable";

    private SampleReceiver sampleReceiver;
    private String source;

    private static final String dataURL = "https://newsapi.org/v2/top-headlines?sources=";
    private static final String API_KEY = "";

    public SourceLoaderRunnable(SampleReceiver sampleReceiver, String source) {
        this.sampleReceiver = sampleReceiver;
        this.source = source;
    }

    @Override
    public void run() {
        String URL = dataURL + source + "&language=en&apiKey=" + API_KEY;

        Uri dataUri = Uri.parse(URL);
        String urlToUse = dataUri.toString();
        StringBuilder sb = new StringBuilder();

        try {
            java.net.URL url = new URL(urlToUse);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("User-Agent","");
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                processResults(null);
                return;
            }

            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

        } catch (Exception e) {
            processResults(null);
            return;
        }
        processResults(sb.toString());
    }

    private void processResults(final String jsonString) {
        final ArrayList<Source> tmp = parseJSON(jsonString);

        if(tmp != null)
            sampleReceiver.receiveList(tmp);
    }

    private ArrayList<Source> parseJSON(String s) {
        ArrayList<Source> tmp = new ArrayList<>();
        try {
            JSONObject jObjMain = new JSONObject(s);
            JSONArray sources = jObjMain.getJSONArray("articles");

            for (int i = 0; i < sources.length(); i++) {
                JSONObject jObj = (JSONObject) sources.get(i);

                String author = null;
                String title = null;
                String description = null;
                String url = null;
                String publishedAt = null;
                String urlToImage = null;

                if (jObj.has("author"))
                    author = jObj.getString("author");
                if (jObj.has("title"))
                    title = jObj.getString("title");
                if (jObj.has("description"))
                    description = jObj.getString("description");
                if (jObj.has("url"))
                    url = jObj.getString("url");
                if (jObj.has("publishedAt"))
                    publishedAt = jObj.getString("publishedAt");
                if (jObj.has("urlToImage"))
                    urlToImage = jObj.getString("urlToImage");

                //Log.d(TAG, "parseJSON: " + title + " | " + publishedAt);

                tmp.add(new Source(author, title, description, url, urlToImage, publishedAt));
            }
            return tmp;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
