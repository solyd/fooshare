package org.fooshare;

import org.fooshare.R;
import org.fooshare.R.drawable;
import org.fooshare.R.layout;
import org.fooshare.R.string;
import org.fooshare.storage.IStorage.RegistrationItem;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class MainTabActivity extends TabActivity {

	protected FooshareApplication _fooshare;

	// TODO remove
	public final String TAB_DEMO      = "Demo";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String TAB_SEARCH    = getResources().getString(R.string.SEARCH_TAB_TAG);
        String TAB_DOWNLOADS = getResources().getString(R.string.DOWNLOADS_TAB_TAG);
        String TAB_PEERS     = getResources().getString(R.string.PEERS_TAB_TAG);
        String TAB_SETTINGS  = getResources().getString(R.string.SETTINGS_TAB_TAG);
        _fooshare = (FooshareApplication) getApplication();

        setContentView(R.layout.main);

        if (_fooshare.storage().isRegistrationNeeded() != null) {
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
        spec = tabHost.newTabSpec(TAB_SEARCH)
                //.setIndicator(TAB_SEARCH, res.getDrawable(R.drawable.ic_tab_search))
                .setIndicator(TAB_SEARCH, res.getDrawable(android.R.drawable.ic_menu_search))
                .setContent(intent);
        tabHost.addTab(spec);

        // Do the same for the other tabs
        intent = new Intent().setClass(this, DownloadsActivity.class);
        spec = tabHost.newTabSpec(TAB_DOWNLOADS)
                //.setIndicator(TAB_DOWNLOADS, res.getDrawable(R.drawable.ic_tab_downloads))
                .setIndicator(TAB_DOWNLOADS, res.getDrawable(android.R.drawable.ic_menu_upload))
                .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, SettingsActivity.class);
        spec = tabHost.newTabSpec(TAB_SETTINGS)
                //.setIndicator(TAB_SETTINGS,res.getDrawable(R.drawable.ic_tab_settings))
                .setIndicator(TAB_SETTINGS,res.getDrawable(android.R.drawable.ic_menu_preferences))
                .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, PeersActivity.class);
        spec = tabHost.newTabSpec(TAB_PEERS)
                //.setIndicator(TAB_PEERS, res.getDrawable(R.drawable.ic_tab_settings))
                .setIndicator(TAB_PEERS, res.getDrawable(android.R.drawable.ic_menu_myplaces))
                .setContent(intent);
        tabHost.addTab(spec);

//        intent = new Intent().setClass(this, DemoActivity.class);
//        spec = tabHost.newTabSpec(TAB_DEMO)
//                .setIndicator("Demo", res.getDrawable(R.drawable.ic_launcher))
//                .setContent(intent);
//        tabHost.addTab(spec);

        tabHost.setCurrentTabByTag(TAB_PEERS);
    }

}
