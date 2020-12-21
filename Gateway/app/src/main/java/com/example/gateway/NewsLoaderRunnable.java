package com.example.gateway;

import android.net.Uri;
//import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

import javax.net.ssl.HttpsURLConnection;

public class NewsLoaderRunnable implements Runnable {

    //private static final String TAG = "NewsLoaderRunnable";


    private MainActivity mainActivity;
    private String category;
    private static final String dataURL = "https://newsapi.org/v2/sources?language=en&country=us";
    private static final String API_KEY = "";

    public NewsLoaderRunnable(MainActivity mainActivity, String category) {
        this.mainActivity = mainActivity;
        this.category = category;
    }

    @Override
    public void run() {
        String URL = dataURL + "&category=" + category + "&apiKey=" + API_KEY;

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

    private void processResults(String s) {
        final ArrayList<News> news = parseJSON(s);
        final HashSet<String> categories = parseCategory(s);

        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                assert news != null;
                mainActivity.setUpSources(news, categories);
            }
        });
    }

    private HashSet<String> parseCategory(String s) {
        HashSet<String> tmp_holder = new HashSet<>();
        try {
            JSONObject jObjMain = new JSONObject(s);
            JSONArray sources = jObjMain.getJSONArray("sources");

            for (int i = 0; i < sources.length(); i++) {
                JSONObject jObj = (JSONObject) sources.get(i);

                String category = jObj.getString("category");

                tmp_holder.add(category);
            }
            return tmp_holder;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private ArrayList<News> parseJSON(String s) {
        ArrayList<News> tmp_clone = new ArrayList<>();

        try {
            JSONObject jObjMain = new JSONObject(s);
            JSONArray sources = jObjMain.getJSONArray("sources");

            for (int i = 0; i < sources.length(); i++) {
                JSONObject jObj = (JSONObject) sources.get(i);

                String id = jObj.getString("id");
                String name = jObj.getString("name");
                String category = jObj.getString("category");

                tmp_clone.add(new News(id, name, category));

            }
            return tmp_clone;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
