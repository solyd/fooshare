package org.fooshare;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class MainActivity extends TabActivity {

	protected FooshareApplication _fooshare;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _fooshare = (FooshareApplication) getApplication();

        setContentView(R.layout.main);
/*
        if ((_fooshare.storage().isRegistrationNeeded())) {
            Intent intentS = new Intent(MainActivity.this,RegistrationActivity.class);
        	startActivity(intentS);
        }
*/
        Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Reusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab

        // Create an Intent to launch an Activity for the tab (to be reused)
        // Initialize a TabSpec for each tab and add it to the TabHost
        intent = new Intent().setClass(this, SearchActivity.class);
        spec = tabHost.newTabSpec("Search").setIndicator("Search",
                          res.getDrawable(R.drawable.ic_tab_search))
                      .setContent(intent);
        tabHost.addTab(spec);

        // Do the same for the other tabs
        intent = new Intent().setClass(this, DownloadsActivity.class);
        spec = tabHost.newTabSpec("Downloads").setIndicator("Downloads",
                          res.getDrawable(R.drawable.ic_tab_downloads))
                      .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, SettingsActivity.class);
        spec = tabHost.newTabSpec("settings").setIndicator("Settings",
                          res.getDrawable(R.drawable.ic_tab_settings))
                      .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, DemoActivity.class);
        spec = tabHost.newTabSpec("Demo").setIndicator("Demo",
                          res.getDrawable(R.drawable.ic_launcher))
                      .setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(0);
    }
}
