package edu.uta.cse.academia.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import edu.uta.cse.academia.Adapters.ServicesListAdapter;
import edu.uta.cse.academia.BuildConfig;
import edu.uta.cse.academia.Constants.AppConstants;
import edu.uta.cse.academia.Constants.WebServiceConstants;
import edu.uta.cse.academia.Models.Services;
import edu.uta.cse.academia.Models.User;
import edu.uta.cse.academia.R;
import edu.uta.cse.academia.helpers.HttpHelper;
import edu.uta.cse.academia.helpers.SharedPreferenceHelper;

public class ViewProfileActivity extends AppCompatActivity {

    EditText firstNameView, lastNameView, emailView;
    TextView noCoursesView;
    EditText addressLine1View, addressLine2View, cityEditView, zipEditView, phoneNumberView;
    AutoCompleteTextView stateTextView;
    SharedPreferenceHelper sharedPreferenceHelper;
    RecyclerView rv_servicelist;
    Services services;
    ServicesListAdapter slAdapeter;
    String tutorProfileId;
    User user;
    CardView emailCardView,phoneCardView,smsCardView;
    ProgressBar progressBar;
    ImageView profilePhotoImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        progressBar = (ProgressBar) findViewById(R.id.viewprofileprogressbar);
        emailCardView = (CardView) findViewById(R.id.EmailCardView);
        phoneCardView = (CardView) findViewById(R.id.PhoneCardView);
        smsCardView = (CardView) findViewById(R.id.smsCardView);

        smsCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.putExtra("address", phoneNumberView.getText().toString());
                sendIntent.putExtra("sms_body", "");
                sendIntent.setType("vnd.android-dir/mms-sms");
                startActivity(sendIntent);
            }
        });

        phoneCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phoneNumberView.getText().toString()));
                startActivity(callIntent);
            }
        });

        emailCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto",emailView.getText().toString(), null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject:");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        });

        profilePhotoImageView = (ImageView) findViewById(R.id.profilePhoto);
        noCoursesView = (TextView) findViewById(R.id.NoCoursesYet);
        firstNameView = (EditText) findViewById(R.id.FirstName);
        lastNameView = (EditText) findViewById(R.id.LastName);
        emailView = (EditText) findViewById(R.id.Email);
        addressLine1View = (EditText) findViewById(R.id.AddressLine1);
        addressLine2View = (EditText) findViewById(R.id.AddressLine2);
        stateTextView = (AutoCompleteTextView) findViewById(R.id.State);
        cityEditView = (EditText) findViewById(R.id.City);
        zipEditView = (EditText) findViewById(R.id.ZipCode);
        phoneNumberView = (EditText) findViewById(R.id.PhoneNumber);

        if ((getIntent().getStringExtra(AppConstants.PROFILE_VIEW_MODE))!=null  && getIntent().getStringExtra(AppConstants.PROFILE_VIEW_MODE).equals(WebServiceConstants.YES)) {
            fab.hide();
            tutorProfileId = getIntent().getStringExtra(AppConstants.TUTOR_PROFILE_ID);
        } else {
            sharedPreferenceHelper = new SharedPreferenceHelper(getApplicationContext(), BuildConfig.PREF_FILE);

            firstNameView.setText(sharedPreferenceHelper.getValueFromSharedPreferencesForKey(AppConstants.FIRST_NAME, ""));
            lastNameView.setText(sharedPreferenceHelper.getValueFromSharedPreferencesForKey(AppConstants.LAST_NAME, ""));
            emailView.setText(sharedPreferenceHelper.getValueFromSharedPreferencesForKey(AppConstants.EMAIL, ""));
            addressLine1View.setText(sharedPreferenceHelper.getValueFromSharedPreferencesForKey(AppConstants.ADDRESS_LINE_1, ""));
            addressLine2View.setText(sharedPreferenceHelper.getValueFromSharedPreferencesForKey(AppConstants.ADDRESS_LINE_2, ""));
            stateTextView.setText(sharedPreferenceHelper.getValueFromSharedPreferencesForKey(AppConstants.STATE, ""));
            cityEditView.setText(sharedPreferenceHelper.getValueFromSharedPreferencesForKey(AppConstants.CITY, ""));
            zipEditView.setText(sharedPreferenceHelper.getValueFromSharedPreferencesForKey(AppConstants.ZIPCODE, ""));
            phoneNumberView.setText(sharedPreferenceHelper.getValueFromSharedPreferencesForKey(AppConstants.PHONE_NUMBER, ""));
            if(emailView.getText().toString().equals("arun.gopinathan@mavs.uta.edu"))
                profilePhotoImageView.setVisibility(View.VISIBLE);

        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        rv_servicelist = (RecyclerView) findViewById(R.id.frag_updateprofile_service_list);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv_servicelist.setHasFixedSize(true);
        rv_servicelist.setLayoutManager(layoutManager);
        rv_servicelist.addOnItemTouchListener(new RecyclerTouchListener(this, rv_servicelist, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                onListItemClicked(view, position);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        getAllServicesAsyncTask task = new getAllServicesAsyncTask();
        task.execute();

    }

    private void onListItemClicked(View view, int position) {
        Log.w("PTS-Android", services.getServices().get(position).getServiceId());

        Log.w("PTS-Android", "Service Id -->" + services.getServices().get(position).getServiceId());
        //TODO implement the subject profile and remove this
        Intent intent = new Intent(getApplicationContext(), SubjectProfileActivity.class);
        intent.putExtra("SERVICE_ID",services.getServices().get(position).getServiceId());
        startActivity(intent);
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

    public void getAllServicesByUsername(String Email) {
        String result = "";
        String url = BuildConfig.HOST + WebServiceConstants.GET_ALL_SERVICES_BY_USERNAME_METHOD + AppConstants.FORWARD_SLASH + Email;
        HttpHelper httpHelper = new HttpHelper();
        String entity = httpHelper.executeHttpGetRequest(url);
        if (!TextUtils.isEmpty(entity)) {
            JsonParser parser = new JsonParser();
            Gson gson = new Gson();// create a gson object
            //  JsonObject obj = (JsonObject) parser.parse(result);
            services = gson.fromJson(entity, Services.class);
//            Log.w("PTS-Android", "test: " + services.getServices().get(0).getAddress().toString());
        }
    }

    public void getUserProfile(String userId) {
        String result = "";
        String url = BuildConfig.HOST + WebServiceConstants.getUserProfileMethod + userId;
        HttpHelper httpHelper = new HttpHelper();
        String entity = httpHelper.executeHttpGetRequest(url);
        if (!TextUtils.isEmpty(entity)) {
            JsonParser parser = new JsonParser();
            Gson gson = new Gson();// create a gson object
            //  JsonObject obj = (JsonObject) parser.parse(result);
            user = User.parseUserjsonToJavaObject(entity);
            Log.w("PTS-Android", "user->" + user.toString());
//            Log.w("PTS-Android", "test: " + services.getServices().get(0).getAddress().toString());
        }
    }

    private class getAllServicesAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (!TextUtils.isEmpty(tutorProfileId)) {
                Log.w("PTS-Android","tutorProfleNotEMpty");
                getUserProfile(tutorProfileId);
                getAllServicesByUsername(user.getEmail());
                if(user.getEmail().equals("arun.gopinathan@mavs.uta.edu")){

                }
            } else {

                getAllServicesByUsername(sharedPreferenceHelper.getValueFromSharedPreferencesForKey("Email", null));
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            progressBar.animate();
            super.onProgressUpdate(values);
        }

        protected void onPostExecute(Void aVoid) {
            progressBar.setVisibility(View.GONE);
            if (services != null) {
                noCoursesView.setVisibility(View.GONE);
                slAdapeter = new ServicesListAdapter(getApplicationContext(), services);
                rv_servicelist.setAdapter(slAdapeter);
               /* LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View inflatedView = inflater.inflate(R.layout.services_list_item, null);
                CardView cv = (CardView) inflatedView.findViewById(R.id.frag_course_cv_all_service);*/
                if (slAdapeter.getItemCount() == 0) {
                    noCoursesView.setVisibility(View.VISIBLE);
                }
                int viewHeight = slAdapeter.getItemCount() * 225;
                rv_servicelist.getLayoutParams().height = viewHeight;
                Log.w("PTS-Android", "cardView Height " + viewHeight);
                Log.w("PTS-Android", "Itemcount: " + slAdapeter.getItemCount());

            }

            if(user!=null){
                firstNameView.setText(user.getFirstName());
                lastNameView.setText(user.getLastName());
                emailView.setText(user.getEmail());
                addressLine1View.setText(user.getAddress().getAddressLine1());
                addressLine2View.setText(user.getAddress().getAddressLine2());
                stateTextView.setText(user.getAddress().getState());
                cityEditView.setText(user.getAddress().getCity());
                zipEditView.setText(user.getAddress().getZipCode());
                phoneNumberView.setText(user.getPhoneNumber());
                if(user.getEmail().equals("arun.gopinathan@mavs.uta.edu"))
                    profilePhotoImageView.setVisibility(View.VISIBLE);
            }


        }
    }
}
