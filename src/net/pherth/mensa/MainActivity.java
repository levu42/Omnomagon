package net.pherth.mensa;

import java.util.ArrayList;

import net.pherth.mensa.R;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.foound.widget.AmazingListView;
import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitleProvider;

public class MainActivity extends SherlockActivity {
	
    private static int NUM_VIEWS = 7;
    private Context cxt;
    private ProgressDialog m_ProgressDialog = null; 
    private Runnable viewOrders;
    private String[] items;
    AmazingListView lsComposer;
    private MainPagerAdapter adapter;
    private ArrayList<MealAdapter> mAdapterList = new ArrayList<MealAdapter>();
    com.actionbarsherlock.app.ActionBar actionBar;
    private Data data;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        cxt = this;
        items = getResources().getStringArray(R.array.weekDays);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(cxt);
        OnSharedPreferenceChangeListener prefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            			actionBar.setTitle(getCurrentMensa(prefs.getString("mensaPreference", "Mensa")));
            			for(int i=0; i<7; i++){
            	    		mAdapterList.get(i).notifyDataSetChanged();
            	    	}
                }
            };
        sharedPrefs.registerOnSharedPreferenceChangeListener(prefsListener);
        for(int x=0; x<7; x++) {
        	mAdapterList.add(new MealAdapter(cxt));
        }
        adapter = new MainPagerAdapter( this );
        ViewPager pager =
            (ViewPager)findViewById( R.id.mainpager );
        TitlePageIndicator indicator =
            (TitlePageIndicator)findViewById( R.id.indicator );
        pager.setAdapter( adapter );
        indicator.setViewPager( pager );
        
        final Typeface mFont = Typeface.createFromAsset(getAssets(), "fonts/Bitter-Regular.otf"); 
		final ViewGroup mContainer = (ViewGroup) findViewById(
		android.R.id.content).getRootView();
		setAppFont(mContainer, mFont);
        
	    actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        //actionBar.setHomeAction(new IntentAction(this, createIntent(this), R.drawable.ic_title_home_demo));
        
	    actionBar.setTitle(getCurrentMensa(sharedPrefs.getString("mensaPreference", "Mensa")));
	    if (sharedPrefs.getBoolean("automaticUpdate", true)) {
	    viewOrders = new Runnable(){
            @Override
            public void run() {
                getPlan();
            }
        };
        Thread thread =  new Thread(null, viewOrders, "MagentoBackground");
        thread.start();
        m_ProgressDialog = ProgressDialog.show(MainActivity.this,    
              "Please wait...", "Retrieving data ...", true);
	    }
    }
    
    private class MainPagerAdapter extends PagerAdapter implements TitleProvider{
    	
    	private final Context context;
        
    	public MainPagerAdapter( Context context )
        {
            this.context = context;
        }
    	
        @Override
        public int getCount() {
                return NUM_VIEWS;
        }

    /**
     * Create the page for the given position.  The adapter is responsible
     * for adding the view to the container given here, although it only
     * must ensure this is done by the time it returns from
     * {@link #finishUpdate()}.
     *
     * @param container The containing View in which the page will be shown.
     * @param position The page position to be instantiated.
     * @return Returns an Object representing the new page.  This does not
     * need to be a View, but can be some other container of the page.
     */
        @Override
        public Object instantiateItem(View collection, int position) {
        	AmazingListView v = new AmazingListView( context );
            ( (ViewPager) collection ).addView( v, 0 );
            v.setPinnedHeaderView(getLayoutInflater().inflate(R.layout.item_composer_header, v, false));
            System.out.println(data);
            
            v.setAdapter(mAdapterList.get(position));
            v.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View res,
						int arg2, long arg3) {
					ListView listView = (ListView) res.findViewById(R.id.additionsListView);
					if (listView.getVisibility() == View.GONE) {
						Animation rollDownAnimation = AnimationUtils.loadAnimation(cxt, R.anim.rolldown);
						listView.setVisibility(View.VISIBLE);
						listView.startAnimation(rollDownAnimation);
					} else {
						listView.setVisibility(View.GONE);
					}
					System.out.println(listView.getCount());
				}
              });
            
            return v;
        }

    /**
     * Remove a page for the given position.  The adapter is responsible
     * for removing the view from its container, although it only must ensure
     * this is done by the time it returns from {@link #finishUpdate()}.
     *
     * @param container The containing View from which the page will be removed.
     * @param position The page position to be removed.
     * @param object The same object that was returned by
     * {@link #instantiateItem(View, int)}.
     */
        @Override
        public void destroyItem(View collection, int position, Object view) {
                ((ViewPager) collection).removeView((ListView) view);
        }

        
        
        @Override
        public boolean isViewFromObject(View view, Object object) {
                return view==((ListView)object);
        }

        
    /**
     * Called when the a change in the shown pages has been completed.  At this
     * point you must ensure that all of the pages have actually been added or
     * removed from the container as appropriate.
     * @param container The containing View which is displaying this adapter's
     * page views.
     */
        @Override
        public void finishUpdate(View arg0) {
        }
        

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {}

        @Override
        public Parcelable saveState() {
                return null;
        }

        @Override
        public void startUpdate(View arg0) {}
        
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
        
        @Override
        public String getTitle( int position )
        {
            return items[ position ];
        }
    }
    
    private void getPlan(){
    	if (isNetworkAvailable()) {
    	data = new Data(cxt);
    	data.getAllData();
    	for(int i=0; i<data.getDayCount(); i++){
    		MealAdapter mealAdapter = mAdapterList.get(i);
    		mealAdapter.setData(data.getCurrentDay(i));
    		mAdapterList.set(i, mealAdapter);
    	}
    	}
          runOnUiThread(returnRes);
    }
    
    private Runnable returnRes = new Runnable() {

        @Override
        public void run() {
            m_ProgressDialog.dismiss();
            for(int i=0; i<7; i++){
	    		mAdapterList.get(i).notifyDataSetChanged();
	    	}
        }
    };
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.optionmenu, menu);
        return true;
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	System.out.println(item);
    	System.out.println(item.getItemId());
        switch (item.getItemId()) {
            case R.id.menu_settings:
            	Intent settingsActivity = new Intent(getBaseContext(),
                        MainPreference.class);
            		startActivity(settingsActivity);
                   	break;
            case R.id.menu_refresh:
            	Thread thread =  new Thread(null, viewOrders, "MagentoBackground");
                thread.start();
                m_ProgressDialog = ProgressDialog.show(MainActivity.this,    
                      "Please wait...", "Retrieving data ...", true);
                break;
            	
        }
        return true;
    }
    
    public String getCurrentMensa(String key) {
    	String currentMensa = "Mensa";
        Resources res = getResources();
        String[] mensaKeys = res.getStringArray(R.array.MensenShort);
        int mensaIndex = java.util.Arrays.asList(mensaKeys).indexOf(key);
        if (mensaIndex >= 0) {
        	currentMensa = res.getStringArray(R.array.Mensen)[mensaIndex];
        }
        return currentMensa;
    }
    
    @Override
    public void onPause() {
    	m_ProgressDialog.dismiss();
    	super.onPause();
    }
    
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager 
              = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
    
    public static final void setAppFont(ViewGroup mContainer, Typeface mFont)
    {
        if (mContainer == null || mFont == null) return;

        final int mCount = mContainer.getChildCount();

        // Loop through all of the children.
        for (int i = 0; i < mCount; ++i)
        {
            final View mChild = mContainer.getChildAt(i);
            if (mChild instanceof TextView)
            {
                // Set the font if it is a TextView.
                ((TextView) mChild).setTypeface(mFont);
            }
            else if (mChild instanceof ViewGroup)
            {
                // Recursively attempt another ViewGroup.
                setAppFont((ViewGroup) mChild, mFont);
            }
        }
    }
}
