package org.fooshare;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class MainTabActivity extends TabActivity {
    
	protected FooshareApplication _fooshare;

	public static final String TAB_SEARCH    = "Search";
	public static final String TAB_DOWNLOADS = "Downloads";
	public static final String TAB_SETTINGS  = "Settings";
	public static final String TAB_DEMO      = "Demo";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _fooshare = (FooshareApplication) getApplication();

        setContentView(R.layout.main);

        if ((_fooshare.storage().isRegistrationNeeded())) {
            Intent intentS = new Intent(MainTabActivity.this,RegistrationActivity.class);
        	startActivity(intentS);
        }

        Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Reusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab

        // Create an Intent to launch an Activity for the tab (to be reused)
        // Initialize a TabSpec for each tab and add it to the TabHost
        intent = new Intent().setClass(this, SearchActivity.class);
        spec = tabHost.newTabSpec(TAB_SEARCH).
                setIndicator("Search", res.getDrawable(R.drawable.ic_tab_search))
                .setContent(intent);
        tabHost.addTab(spec);

        // Do the same for the other tabs
        intent = new Intent().setClass(this, DownloadsActivity.class);
        spec = tabHost.newTabSpec(TAB_DOWNLOADS)
                .setIndicator("Downloads", res.getDrawable(R.drawable.ic_tab_downloads))
                .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, SettingsActivity.class);
        spec = tabHost.newTabSpec(TAB_SETTINGS)
                .setIndicator("Settings", res.getDrawable(R.drawable.ic_tab_settings))
                .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, DemoActivity.class);
        spec = tabHost.newTabSpec(TAB_DEMO)
                .setIndicator("Demo", res.getDrawable(R.drawable.ic_launcher))
                .setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTabByTag(TAB_DEMO);
    }
}
