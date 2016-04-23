package edu.uta.cse.academia.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.net.URLEncoder;
import java.util.ArrayList;

import edu.uta.cse.academia.Adapters.ServicesListAdapter;
import edu.uta.cse.academia.BuildConfig;
import edu.uta.cse.academia.Constants.AppConstants;
import edu.uta.cse.academia.Models.Service;
import edu.uta.cse.academia.Models.Services;
import edu.uta.cse.academia.R;
import edu.uta.cse.academia.helpers.SharedPreferenceHelper;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    TextView userNameText, emailText;
    SearchBox searchBox;
    RecyclerView searchResultsRecycleView;
    public static String hostname = BuildConfig.HOST;
    public static String method = "BasicSearch/";
    public Services services = new Services();
    ServicesListAdapter slAdapeter;
    LinearLayoutManager layoutManager;
    LinearLayout serviceListLinearLayout;
    TextView noCoursesView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SharedPreferenceHelper helper = new SharedPreferenceHelper(getApplicationContext(), BuildConfig.PREF_FILE);
        String userId = helper.getValueFromSharedPreferencesForKey(AppConstants.USER_ID, null);
        //if this is null invalid authentication
        if (userId == null)
            finish();

        View header = navigationView.getHeaderView(0);
        noCoursesView = (TextView) findViewById(R.id.noCourseFoundTextView);
        userNameText = (TextView) header.findViewById(R.id.HomeUserName);
        String name = helper.getValueFromSharedPreferencesForKey(AppConstants.LAST_NAME, null) + "," + helper.getValueFromSharedPreferencesForKey(AppConstants.FIRST_NAME, null);
        userNameText.setText(name);

        emailText = (TextView) header.findViewById(R.id.HomeEmail);
        String email = helper.getValueFromSharedPreferencesForKey(AppConstants.EMAIL, null);
        emailText.setText(email);

        searchResultsRecycleView = (RecyclerView) findViewById(R.id.search_results_list);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        searchResultsRecycleView.setHasFixedSize(true);
        searchResultsRecycleView.setLayoutManager(layoutManager);
        searchResultsRecycleView.addOnItemTouchListener(new RecyclerTouchListener(HomeActivity.this, searchResultsRecycleView, new HomeActivity.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                onListItemClicked(view, position);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        searchBox = (SearchBox) findViewById(R.id.searchbox);
        searchBox.enableVoiceRecognition(this);

        ((EditText) searchBox.findViewById(R.id.search)).setTextColor(Color.parseColor("#000000"));

        searchBox.setSearchListener(new SearchBox.SearchListener() {
            @Override
            public void onSearchOpened() {

            }

            @Override
            public void onSearchCleared() {

            }

            @Override
            public void onSearchClosed() {

            }

            @Override
            public void onSearchTermChanged(String s) {

            }

            @Override
            public void onSearch(String s) {
              //  Toast.makeText(HomeActivity.this, "searched->" + s, Toast.LENGTH_SHORT).show();
                getAllServicesAsyncTask task = new getAllServicesAsyncTask();
                try {
                    task.execute(URLEncoder.encode(s, "utf-8"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }


            }

            @Override
            public void onResultClick(SearchResult searchResult) {

            }
        });


    }

    private void onListItemClicked(View view, int position) {
        Log.w("PTS-Android", services.getServices().get(position).getServiceId());

        Log.w("PTS-Android", "Service Id -->" + services.getServices().get(position).getServiceId());
        Intent intent = new Intent(getApplicationContext(), SubjectProfileActivity.class);
        intent.putExtra("SERVICE_ID", services.getServices().get(position).getServiceId());
        startActivity(intent);
        //TODO implement the subject profile and remove this
       /* Intent intent = new Intent(getActivity(), SubjectProfileActivity.class);
        intent.putExtra("SERVICE_ID",services.getServices().get(position).getServiceId());
        getActivity().startActivity(intent);*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SearchBox.VOICE_RECOGNITION_CODE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String resultString = "";
            if (matches.size() >= 1) {
                resultString = matches.get(0);
            }
            searchBox.populateEditText(resultString);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void logOutUser() {

        Snackbar.make(findViewById(R.id.drawer_layout), "Logging out...", Snackbar.LENGTH_LONG).setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                //go back to the login screen
                SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(getApplicationContext(), BuildConfig.PREF_FILE);
                sharedPreferenceHelper.clearSharedPreferences();
                finish();
            }
        }).show();


        finish();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_view_profile) {
            Intent viewProfileIntent = new Intent(getApplicationContext(), ViewProfileActivity.class);
            startActivity(viewProfileIntent);

        } else if (id == R.id.nav_offer_services) {
            Intent offerServiceIntent = new Intent(getApplicationContext(), RegisterTutorServiceActivity.class);
            startActivity(offerServiceIntent);

        } else if (id == R.id.nav_logout) {
            logOutUser();

        } else if (id == R.id.hot_courses) {

        } else if (id == R.id.nearby_courses) {
            Intent nearbyTutorIntent = new Intent(getApplicationContext(), NearbyTutorsActivity.class);
            startActivity(nearbyTutorIntent);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private String searchFor(String query) {
        String result = "";
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
            String url = hostname + method + query;
            HttpGet getRequest = new HttpGet(url);
            HttpResponse httpResponse = httpclient.execute(getRequest);
            HttpEntity entity = httpResponse.getEntity();
            Log.w("PTS-Android", httpResponse.getStatusLine().toString());
            if (entity != null) {
                result = EntityUtils.toString(entity);
                Log.w("PTS-Android", "Entity : " + result);
                Gson gson = new Gson();
                JsonParser parser = new JsonParser();
                JsonObject jsonObject = (JsonObject) parser.parse(result);
                JsonElement element = jsonObject.get("Services");
                services = gson.fromJson(jsonObject.toString(), Services.class);


                Log.w("PTS-Android", "Services:" + services.toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
        }


        return result;
    }

    private class getAllServicesAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {
            searchFor(params[0]);
            //getAllServicesByUsername(sharedPreferences.getString("Email", null));
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            Log.w("PTS-Android", services.toString());
            if (services != null) {
               // noCoursesView.setVisibility(View.GONE);
                slAdapeter = new ServicesListAdapter(getApplicationContext(), services);
                searchResultsRecycleView.setAdapter(slAdapeter);
               /* LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View inflatedView = inflater.inflate(R.layout.services_list_item, null);
                CardView cv = (CardView) inflatedView.findViewById(R.id.frag_course_cv_all_service);*/
                if (slAdapeter.getItemCount() == 0) {
                    noCoursesView.setVisibility(View.VISIBLE);
                }
                else{
                    noCoursesView.setVisibility(View.GONE);
                }
                int viewHeight = slAdapeter.getItemCount() * 225;
                searchResultsRecycleView.getLayoutParams().height = viewHeight;
                Log.w("PTS-Android", "cardView Height " + viewHeight);
                Log.w("PTS-Android", "Itemcount: " + slAdapeter.getItemCount());

            }
        }
    }

    public static interface ClickListener {
        public void onClick(View view, int position);

        public void onLongClick(View view, int position);

    }

    static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {
        private GestureDetector gestureDetector;
        private ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());


                    if (child != null && clickListener != null) {
                        Log.w("PTS-Android", "X,Y : " + e.getX() + "," + e.getY());
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }
    }


}
