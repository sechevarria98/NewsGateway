package com.example.gateway;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;


public class MainActivity extends AppCompatActivity {


    /*
    NOTE: not sure if its known since it wasn't mentioned
    in the document. The api will limit the amount of calls
    that can be done. Its 50 calls per 12 hours. If the limit
    is reached then the nothing will be returned.
     */

    private static final String TAG = "MaTAG";

    private NewsLoaderRunnable newsLoaderRunnable;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private ArrayAdapter<String> arrayAdapter;

    private ArrayList<String> sourcesDisplayed = new ArrayList<>();
    private ArrayList<News> newsMap = new ArrayList<>();
    private ArrayList<String> categoryMap = new ArrayList<>();

    private Menu opt_menu;

    private List<Fragment> fragments;
    private ViewPager pager;
    private MyPageAdapter pageAdapter;

    static final String SOURCE_FROM_SERVICE = "SOURCE_FROM_SERVICE";
    static final String MESSAGE_FROM_SERVICE = "MESSAGE_FROM_SERVICE";
    static final String SOURCE_DATA = "SOURCE_DATA";
    static final String MESSAGE_DATA = "MESSAGE_DATA";
    private SampleReceiver sampleReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.left_drawer);

        mDrawerList.setOnItemClickListener(
                new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String source_id = "";
                        selectItem(i);
                        for(News n : newsMap) {
                            if (sourcesDisplayed.get(i).equals(n.getName()))
                                source_id = n.getId();
                        }

                        startServiceProcess(source_id);
                        mDrawerLayout.closeDrawer(mDrawerList);
                    }
                }
        );

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        );

        fragments = new ArrayList<>();

        pageAdapter = new MyPageAdapter(getSupportFragmentManager());
        pager = findViewById(R.id.viewpager);
        pager.setAdapter(pageAdapter);

        if(sourcesDisplayed.isEmpty()) {
            newsLoaderRunnable = new NewsLoaderRunnable(this, "");
            new Thread(newsLoaderRunnable).start();
        }

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    private void selectItem(int pos) {
        String currentSource = sourcesDisplayed.get(pos);
        setTitle(currentSource);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    //////////////////////////////////////   RUNNABLE   //////////////////////////////////////

    public void setUpSources(ArrayList<News> tmp, HashSet<String> categories) {
        sourcesDisplayed.clear();

        ArrayList<String> nameSorter = new ArrayList<>();
        newsMap.addAll(tmp);
        for(int i = 0; i < tmp.size(); i++)
            nameSorter.add(tmp.get(i).getName());

        if (categoryMap.isEmpty())
            opt_menu.add("all");

//        ArrayList<String> colorHolder = new ArrayList<>();

        //int i = 0;
        for (String menu : categories) {
            if (!categoryMap.contains(menu)) {
//                colorHolder.add(randomColorPicker());
//                SpannableString s = new SpannableString(menu);
//                s.setSpan(new ForegroundColorSpan(Color.parseColor(colorHolder.get(i))), 0, s.length(), 0);
                opt_menu.add(menu);
                categoryMap.add(menu);
            }
            //i++;
        }

        Collections.sort(nameSorter);
        sourcesDisplayed.addAll(nameSorter);


//        for (int j = 0; j < nameSorter.size(); j++) {
//            for (String c : categoryMap) {
//                if (newsMap.get(j).getName().equals(nameSorter.get(j))) {
//                    if(newsMap.get(j).getCategory().equals(c)) {
//                        sourcesDisplayed.add(nameSorter.get(i) + " - " + c);
//                    }
//                }
//            }
//        }

        arrayAdapter = new ArrayAdapter<>(this, R.layout.drawer_list_item, sourcesDisplayed);
        mDrawerList.setAdapter(arrayAdapter);
    }

    public void setUpStories(ArrayList<Source> sourceList) {
        //already fetched stories so service not needed
        stopServiceProcess();
        for (int i = 0; i < pageAdapter.getCount(); i++) {
            pageAdapter.notifyChangeInPosition(i);
        }

        fragments.clear();

        for (int i = 0; i < sourceList.size(); i++) {
            fragments.add(
              StoryFragment.newInstance(sourceList.get(i), i+1, sourceList.size()));
        }
        pageAdapter.notifyDataSetChanged();
        pager.setCurrentItem(0);

    }
    //////////////////////////////////////   END   //////////////////////////////////////

    //////////////////////////////////////   DRAWER   //////////////////////////////////////

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    //////////////////////////////////////   END   //////////////////////////////////////

    //////////////////////////////////////   MISC.   //////////////////////////////////////

//    private String randomColorPicker() {
//        Random r = new Random();
//        int rand = r.nextInt(0xffffff + 1);
//        String color = String.format("#%06x", rand);
//        return color;
//    }

    private void startServiceProcess(String source_id) {
        Intent intent = new Intent(MainActivity.this, StoriesService.class);
        intent.putExtra("SOURCE", source_id);
        startService(intent);

        IntentFilter filter1 = new IntentFilter(SOURCE_FROM_SERVICE);
        IntentFilter filter2 = new IntentFilter(MESSAGE_FROM_SERVICE);

        sampleReceiver = new SampleReceiver(this);

        registerReceiver(sampleReceiver, filter1);
        registerReceiver(sampleReceiver, filter2);
    }

    private void stopServiceProcess() {
        Intent intent = new Intent(MainActivity.this, StoriesService.class);
        stopService(intent);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(sampleReceiver);
        stopServiceProcess();
        super.onDestroy();
    }

    //////////////////////////////////////   END   //////////////////////////////////////

    //////////////////////////////////////   MENU   //////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        opt_menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        if(item.getTitle().toString().equals("all")) {
            newsLoaderRunnable = new NewsLoaderRunnable(this, "");
        } else {
            newsLoaderRunnable = new NewsLoaderRunnable(this, item.getTitle().toString());
        }
        new Thread(newsLoaderRunnable).start();
        return super.onOptionsItemSelected(item);
    }

    //////////////////////////////////////   END   //////////////////////////////////////

    private class MyPageAdapter extends FragmentPagerAdapter {
        private long baseId = 0;

        MyPageAdapter(FragmentManager fm) { super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT); }

        @Override
        public int getItemPosition(@NonNull Object object) { return POSITION_NONE; }

        @NonNull
        @Override
        public Fragment getItem(int position) { return fragments.get(position); }

        @Override
        public int getCount() { return fragments.size(); }

        @Override
        public long getItemId(int position) { return baseId + position; }

        void notifyChangeInPosition(int n) { baseId += getCount() + n; }
    }
}