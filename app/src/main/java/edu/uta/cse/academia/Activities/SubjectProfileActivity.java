package edu.uta.cse.academia.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import edu.uta.cse.academia.Adapters.ReviewsListAdapter;
import edu.uta.cse.academia.BuildConfig;
import edu.uta.cse.academia.Constants.AppConstants;
import edu.uta.cse.academia.Constants.WebServiceConstants;
import edu.uta.cse.academia.Fragments.AddFeedbackFragment;
import edu.uta.cse.academia.Models.Services;
import edu.uta.cse.academia.R;
import edu.uta.cse.academia.RequestObjects.GetAllReviewForServiceResponse;
import edu.uta.cse.academia.helpers.SharedPreferenceHelper;

public class SubjectProfileActivity extends AppCompatActivity {
    String miles, userId;
    String serviceId;
    String tutorUserId;
    ProgressBar progressBar;
    public static String hostname = BuildConfig.HOST;
    public static String method = "GetServiceByServiceId/";
    public static String getAllReviewsForService = "GetAllReviewsForService/";
    public static String updateHelpfulCount = "UpdateHelpfulCount/";
    public static String updateUnHelpfulCount = "UpdateUnHelpfulCount/";
    public Services services = new Services();
    ProgressBar likeordislikeprogress;
    public GetAllReviewForServiceResponse getAllReviewForServiceResponse;

    public TextView tvCourseName, tvCategory, tvSubCategory, tvTutorName, tvPricePerHour, tvAddress, tvPhoneNumber, tvEmail, tvLike, tvDislike, overallRatingView, InitialsTextView;
    boolean hasReview = false;
    public FloatingActionButton mAddFeedbackButton;
    public RatingBar ratingBar;
    public CardView nameCardView;

    public int clickposition = -1;
    ReviewsListAdapter rlAdapter;
    RecyclerView rv_reviewlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floating_af_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = (ProgressBar) findViewById(R.id.subjectprofileprogress);
        nameCardView = (CardView) findViewById(R.id.tutorNameCard);
        nameCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w("PTS-Android", "name card clicked->" + tutorUserId);
                Intent viewProfileIntent = new Intent(getApplicationContext(), ViewProfileActivity.class);
                viewProfileIntent.putExtra(AppConstants.PROFILE_VIEW_MODE, WebServiceConstants.YES);
                viewProfileIntent.putExtra(AppConstants.TUTOR_PROFILE_ID, tutorUserId);
                startActivity(viewProfileIntent);
            }
        });
        serviceId = getIntent().getStringExtra("SERVICE_ID");
        SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(getApplicationContext(), BuildConfig.PREF_FILE);
        userId = sharedPreferenceHelper.getValueFromSharedPreferencesForKey(AppConstants.USER_ID, null);
        // miles = getIntent().getStringExtra("DISTANCE");
        tvCourseName = (TextView) findViewById(R.id.service_details_coursename);
        tvTutorName = (TextView) findViewById(R.id.service_details_tutorname);
        overallRatingView = (TextView) findViewById(R.id.fcor_tv_overall_ratings);
        InitialsTextView = (TextView) findViewById(R.id.course_list_item_tv_initials);
        // tvPricePerHour = (TextView) findViewById(R.id.service_details_priceperhour);
        //tvAddress = (TextView) findViewById(R.id.service_details_address);
        //  tvPhoneNumber = (TextView) findViewById(R.id.service_details_phonenumber);
        //  tvEmail = (TextView) findViewById(R.id.service_details_email);
        likeordislikeprogress = (ProgressBar) findViewById(R.id.like_dislike_ProgressBar);
        mAddFeedbackButton = (FloatingActionButton) findViewById(R.id.floating_af_button);
        mAddFeedbackButton.setOnClickListener(mOnAddFeedbackClickListener);

        rv_reviewlist = (RecyclerView) findViewById(R.id.feedback_list);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv_reviewlist.setHasFixedSize(true);
        rv_reviewlist.setLayoutManager(layoutManager);

        ratingBar = (RatingBar) findViewById(R.id.service_details_rating);

        NearbyTutorsAsyncTask task = new NearbyTutorsAsyncTask();
        task.execute();

        getAllReviewsForServiceAsyncTask task_review = new getAllReviewsForServiceAsyncTask();
        task_review.execute();


    }

    public View.OnClickListener mOnAddFeedbackClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            AddFeedbackFragment fragment = AddFeedbackFragment.newInstance(Integer.parseInt(serviceId), Integer.parseInt(userId));

            fragment.show(getFragmentManager(), "missiles");
        }
    };

    private String getServiceDetails() {
        String result = "";
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
            String url = hostname + method + serviceId;
            Log.w("PTS-Android", "URL->" + url);
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
                services = gson.fromJson(result, Services.class);

                // Log.w("PTS-Android", "Services:" + services.getServices().get(0).toString());
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

    private class getAllReviewsForServiceAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            getAllReviewsForService(serviceId);
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            progressBar.setVisibility(View.GONE);
            rlAdapter = new ReviewsListAdapter(getApplicationContext(), getAllReviewForServiceResponse, onItemClickCallback);
            rv_reviewlist.setAdapter(rlAdapter);
          /*  int viewHeight = rlAdapter.getItemCount() * 520;
            rv_reviewlist.getLayoutParams().height = viewHeight;*/
            Log.w("PTS-Android", "Itemcount: " + rlAdapter.getItemCount());

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            progressBar.animate();
            super.onProgressUpdate(values);
        }
    }

    public void getAllReviewsForService(String serviceId) {
        String result = "";
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
            String url = hostname + getAllReviewsForService + serviceId;
            Log.w("PTS-Android", url);
            HttpGet getRequest = new HttpGet(url);
            HttpResponse httpResponse = httpclient.execute(getRequest);
            HttpEntity entity = httpResponse.getEntity();
            Log.w("PTS-Android", httpResponse.getStatusLine().toString());
            if (entity != null) {
                result = EntityUtils.toString(entity);
                Log.w("PTS-Android", "Entity : " + result);
                JsonParser parser = new JsonParser();
                Gson gson = new Gson();// create a gson object
                //  JsonObject obj = (JsonObject) parser.parse(result);
                getAllReviewForServiceResponse = gson.fromJson(result, GetAllReviewForServiceResponse.class);
                Log.w("PTS-Android", "test: " + getAllReviewForServiceResponse.getReviews().get(0).getTitle());
                hasReview = true;
            } else {
                hasReview = false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
        }
    }

    public static class OnItemClickListener implements View.OnClickListener {
        private int position;
        private OnItemClickCallback onItemClickCallback;

        public OnItemClickListener(int position, OnItemClickCallback onItemClickCallback) {
            this.position = position;
            this.onItemClickCallback = onItemClickCallback;
        }

        @Override
        public void onClick(View view) {
            onItemClickCallback.onItemClicked(view, position);
        }

        public interface OnItemClickCallback {
            void onItemClicked(View view, int position);
        }
    }

    private OnItemClickListener.OnItemClickCallback onItemClickCallback = new OnItemClickListener.OnItemClickCallback() {
        @Override
        public void onItemClicked(View view, int position) {
            switch (view.getId()) {
                case R.id.fb_list_item_like_feedback:
                    LinearLayout parentView1 = (LinearLayout) view.getParent();
                    tvLike = (TextView) parentView1.findViewById(R.id.fb_list_item_num_like);
                    tvLike.setText((Integer.parseInt(tvLike.getText().toString()) + 1) + "");

                    while (clickposition != position) {
                        clickposition = position;
                        updateHelpfulCountAsyncTask task = new updateHelpfulCountAsyncTask();
                        task.execute();
                    }
                    break;
                case R.id.fb_list_item_dislike_feedback:
                    LinearLayout parentView2 = (LinearLayout) view.getParent();
                    tvDislike = (TextView) parentView2.findViewById(R.id.fb_list_item_num_dislike);
                    tvDislike.setText((Integer.parseInt(tvDislike.getText().toString()) + 1) + "");
                    while (clickposition != position) {
                        clickposition = position;
                        updateUnHelpfulCountAsyncTask task = new updateUnHelpfulCountAsyncTask();
                        task.execute();
                    }
                    break;
            }
        }
    };


    private String updateHelpfulCount(int position) {
        String result = "";
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
            String url = hostname + updateHelpfulCount + getAllReviewForServiceResponse.getReviews().get(position).getReviewId();
            HttpGet getRequest = new HttpGet(url);
            HttpResponse httpResponse = httpclient.execute(getRequest);

            Log.w("PTS-Android", httpResponse.getStatusLine().toString());

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

    private String updateUnHelpfulCount(int position) {
        String result = "";
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
            String url = hostname + updateUnHelpfulCount + getAllReviewForServiceResponse.getReviews().get(position).getReviewId();
            HttpGet getRequest = new HttpGet(url);
            HttpResponse httpResponse = httpclient.execute(getRequest);

            Log.w("PTS-Android", httpResponse.getStatusLine().toString());

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

    private class updateHelpfulCountAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            likeordislikeprogress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            updateHelpfulCount(clickposition);
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            likeordislikeprogress.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), "Like +1", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            likeordislikeprogress.animate();
        }
    }

    private class updateUnHelpfulCountAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            likeordislikeprogress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            updateUnHelpfulCount(clickposition);
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            likeordislikeprogress.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), "Dislike +1", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            likeordislikeprogress.animate();
        }
    }

    private class NearbyTutorsAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            getServiceDetails();

            return null;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();

        }

        @Override
        protected void onPostExecute(Void aVoid) {

            progressBar.setVisibility(View.GONE);
            Log.w("PTS-Android", services.toString());
            if (services.getServices() != null) {
                tvCourseName.setText(services.getServices().get(0).getSubCategory().getSubCategoryName() + " by" + services.getServices().get(0).getUser().getLastName());

                tvTutorName.setText(services.getServices().get(0).getUser().getLastName() + "," + services.getServices().get(0).getUser().getFirstName());
                String initials = services.getServices().get(0).getUser().getFirstName().charAt(0) + "," + services.getServices().get(0).getUser().getLastName().charAt(0);
                InitialsTextView.setText(initials);
                ratingBar.setRating((float) services.getServices().get(0).getAvgRating());
                overallRatingView.setText(String.format("%.1f", services.getServices().get(0).getAvgRating()));
                tutorUserId = services.getServices().get(0).getUser().getUserId() + "";
            }
            super.onPostExecute(aVoid);
            // tvTutorName.setText(services.getServices().get(0).getPricePerHour());
          /*  String address ="";
            address+=services.getServices().get(0).getAddress().getAddressLine1();
            if(services.getServices().get(0).getAddress().getAddressLine2()!=""){
                address+=", "+services.getServices().get(0).getAddress().getAddressLine2();
            }
            address+=","+services.getServices().get(0).getAddress().getCity();
            address+=","+services.getServices().get(0).getAddress().getState();
            address+=","+services.getServices().get(0).getAddress().getZipCode();

            tvAddress.setText(address);
            tvPhoneNumber.setText(services.getServices().get(0).getUser().getPhoneNumber());
            tvPricePerHour.setText(services.getServices().get(0).getPricePerHour());
            ratingBar.setRating((float)services.getServices().get(0).getAvgRating());
            tvEmail.setText(services.getServices().get(0).getUser().getEmail());*/


        }

        @Override
        protected void onProgressUpdate(Void... values) {
            progressBar.animate();
            super.onProgressUpdate(values);

        }
    }

}
