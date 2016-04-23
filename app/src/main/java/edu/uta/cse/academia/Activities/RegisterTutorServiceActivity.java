package edu.uta.cse.academia.Activities;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import edu.uta.cse.academia.BuildConfig;
import edu.uta.cse.academia.Constants.AppConstants;
import edu.uta.cse.academia.Constants.WebServiceConstants;
import edu.uta.cse.academia.Models.Availability;
import edu.uta.cse.academia.Models.Categories;
import edu.uta.cse.academia.Models.Category;
import edu.uta.cse.academia.Models.Days;
import edu.uta.cse.academia.Models.SubCategories;
import edu.uta.cse.academia.Models.SubCategory;
import edu.uta.cse.academia.R;
import edu.uta.cse.academia.RequestObjects.RegisterServiceRequestObject;
import edu.uta.cse.academia.helpers.HttpHelper;
import edu.uta.cse.academia.helpers.SharedPreferenceHelper;

public class RegisterTutorServiceActivity extends AppCompatActivity {
    final Calendar dateAndTime = Calendar.getInstance();
    View rootView;
    SeekBar priceSeekBar;
    TextView pricePerHour, textViewStartTime, textViewEndTime, labelAvailability;
    Button btnSetStartTime, btnSetEndTime, btnRegisterTutorService;
    AutoCompleteTextView tvCategory, tvSubCategory;
    String textCategory, textSubCategory, price, startTime, endTime, distance, advertise;
    CheckBox cbMon, cbTue, cbWed, cbThu, cbFri, cbSat, cbSun, cbIsAdvertised;
    TextView labelPrice;

    boolean[] days = {false, false, false, false, false, false, false};
    Categories categories;
    SubCategories subCategories;
    String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    Spinner travelInMilesSpinner;
    ProgressBar progressBar;
    String requestJson = "", registerResult = "";
    int userId;
    int categoryId;
    HashMap<String, Integer> categoriesHM = new HashMap<String, Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_tutor_service);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(getApplicationContext(), BuildConfig.PREF_FILE);
        String userIdint = sharedPreferenceHelper.getValueFromSharedPreferencesForKey(AppConstants.USER_ID, null);
        //invalid session
        if (userIdint == null) {
            onBackPressed();
        }
        userId =Integer.parseInt(userIdint);
        advertise = WebServiceConstants.NO;
        pricePerHour = (TextView) findViewById(R.id.labelPrice);
        priceSeekBar = (SeekBar) findViewById(R.id.priceSeekBar);
        priceSeekBar.setProgress(0);
        textViewStartTime = (TextView) findViewById(R.id.textViewStartTime);
        textViewEndTime = (TextView) findViewById(R.id.textViewEndTime);

        btnRegisterTutorService = (Button) findViewById(R.id.registerTutorServiceBtn);
        tvCategory = (AutoCompleteTextView) findViewById(R.id.categoryAutoCompleteTextView);
        cbIsAdvertised = (CheckBox)findViewById(R.id.isAdvertisedCheckBox);
        progressBar = (ProgressBar) findViewById(R.id.registerServiceProgress);
        getAllCategoriesAsyncTask task = new getAllCategoriesAsyncTask();
        task.execute();

        tvSubCategory = (AutoCompleteTextView) findViewById(R.id.subCategoryAutoCompleteTextView);
        labelPrice = (TextView) findViewById(R.id.labelPrice);
        travelInMilesSpinner = (Spinner) findViewById(R.id.travelInMilesSpinner);


        cbMon = (CheckBox) findViewById(R.id.checkBoxMon);
        cbMon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                days[0] = isChecked;
                if (isAtleastOneDaySelected())
                    labelAvailability.setError(null);


            }
        });
        cbTue = (CheckBox) findViewById(R.id.checkBoxTue);
        cbTue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                days[1] = isChecked;
                if (isAtleastOneDaySelected())
                    labelAvailability.setError(null);
            }
        });
        cbWed = (CheckBox) findViewById(R.id.checkBoxWed);
        cbWed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                days[2] = isChecked;
                if (isAtleastOneDaySelected())
                    labelAvailability.setError(null);
            }
        });
        cbThu = (CheckBox) findViewById(R.id.checkBoxThu);
        cbThu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                days[3] = isChecked;
                if (isAtleastOneDaySelected())
                    labelAvailability.setError(null);
            }
        });
        cbFri = (CheckBox) findViewById(R.id.checkBoxFri);
        cbFri.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                days[4] = isChecked;
                if (isAtleastOneDaySelected())
                    labelAvailability.setError(null);
            }
        });
        cbSat = (CheckBox) findViewById(R.id.checkBoxSat);
        cbSat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                days[5] = isChecked;
                if (isAtleastOneDaySelected())
                    labelAvailability.setError(null);
            }
        });
        cbSun = (CheckBox) findViewById(R.id.checkBoxSun);
        cbSun.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                days[6] = isChecked;
                if (isAtleastOneDaySelected())
                    labelAvailability.setError(null);
            }
        });
        cbIsAdvertised = (CheckBox)findViewById(R.id.isAdvertisedCheckBox);
        cbIsAdvertised.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    advertise = "YES";
                }
                else{
                    advertise = "NO";
                }
            }
        });

        labelAvailability = (TextView) findViewById(R.id.labelAvailability);

        btnRegisterTutorService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (tvCategory.getText().toString().equals("") || tvCategory.getText().toString() == null) {
                    tvCategory.setError("Please Enter Category");
                } else {
                    tvCategory.setError(null);
                    textCategory = tvCategory.getText().toString();
                }

                if (tvSubCategory.getText().toString().equals("") || tvSubCategory.getText().toString() == null) {
                    tvSubCategory.setError("Please Enter Sub Category");
                } else {
                    tvSubCategory.setError(null);
                    textSubCategory = tvSubCategory.getText().toString();
                }

                if (labelPrice.getText().toString().equals("0 USD")) {
                    labelPrice.setError("Price Per Hour should be greater than 0");
                } else {
                    labelPrice.setError(null);
                    price = labelPrice.getText().toString();
                }

                if (cbMon.isChecked() == false && cbTue.isChecked() == false && cbWed.isChecked() == false && cbThu.isChecked() == false && cbFri.isChecked() == false && cbSat.isChecked() == false && cbSun.isChecked() == false) {
                    labelAvailability.setError("At least one day should be selected");
                } else {
                    labelAvailability.setError(null);
                    if (cbMon.isChecked())
                        days[0] = true;

                    if (cbTue.isChecked())
                        days[1] = true;

                    if (cbWed.isChecked())
                        days[2] = true;

                    if (cbThu.isChecked())
                        days[3] = true;

                    if (cbFri.isChecked())
                        days[4] = true;

                    if (cbSat.isChecked())
                        days[5] = true;

                    if (cbSun.isChecked())
                        days[6] = true;

                }

                if (textViewStartTime.getText().toString().equals("")) {
                    textViewStartTime.setError("Start Time is Required");
                } else {
                    textViewStartTime.setError(null);
                    startTime = textViewStartTime.getText().toString();
                }

                if (textViewEndTime.getText().toString().equals("")) {
                    textViewEndTime.setError("End Time is Required");
                } else {
                    textViewEndTime.setError(null);
                    endTime = textViewEndTime.getText().toString();
                }
                View view = travelInMilesSpinner.getSelectedView();
                TextView textView = null;

                if (view != null && view instanceof TextView) {
                    textView = (TextView) view;
                    if (travelInMilesSpinner.getSelectedItemPosition() == 0) {

                        textView.setError("Please Select Distance Willing to travel");
                    } else {
                        textView.setError(null);
                        distance = travelInMilesSpinner.getSelectedItem().toString();
                    }
                }


                //call the async task to
                if (tvCategory.getError() == null && tvSubCategory.getError() == null && labelPrice.getError()== null && labelAvailability.getError() == null && textViewStartTime.getError() == null && textViewEndTime.getError() == null && travelInMilesSpinner.getSelectedItemPosition() > 0) {
                    registerTutorServiceAsyncTask task = new registerTutorServiceAsyncTask();
                    task.execute();
                }
            }

        });

        textViewStartTime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    textViewStartTime.callOnClick();
            }
        });
        textViewStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.requestFocus();
                InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                setStartTime();
                textViewStartTime.setError(null);
            }

        });
        textViewEndTime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    textViewEndTime.callOnClick();
            }
        });
        textViewEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                setEndTime();
                textViewEndTime.setError(null);
            }
        });
        priceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int currentProgress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                currentProgress = progress;
                String priceText = String.valueOf((int) currentProgress) + " USD";
                pricePerHour.setText(priceText.trim());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (currentProgress > 0) {
                    labelPrice.setError(null);
                }
                String priceText = String.valueOf((int) currentProgress) + " USD";
                pricePerHour.setText(priceText.trim());

            }
        });

    }
    public String formatTime() {
        String myFormat = "hh:mm a"; // your own format
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        String formated_time = sdf.format(dateAndTime.getTime());
        return formated_time;
    }

    public void setStartTime() {
        new TimePickerDialog(RegisterTutorServiceActivity.this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        dateAndTime.set(Calendar.MINUTE, minute);
                        //  System.out.print(view.getClass().getName());
                        textViewStartTime.setText(formatTime());
                    }
                },
                dateAndTime.get(Calendar.HOUR_OF_DAY),
                dateAndTime.get(Calendar.MINUTE),
                true).show();
    }

    public void setEndTime() {
        new TimePickerDialog(RegisterTutorServiceActivity.this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        dateAndTime.set(Calendar.MINUTE, minute);
                        //  System.out.print(view.getClass().getName());
                        textViewEndTime.setText(formatTime());
                    }
                },
                dateAndTime.get(Calendar.HOUR_OF_DAY),
                dateAndTime.get(Calendar.MINUTE),
                true).show();
    }

    public boolean isAtleastOneDaySelected() {
        boolean result = false;

        for (int i = 0; i < days.length; i++)
            if (days[i] == true) {
                result = true;
                break;
            }
        return result;
    }

    public void getAllCategories(){
        boolean isavailable = false;
        String url = BuildConfig.HOST + WebServiceConstants.getCategoriesMethod ;
        HttpHelper httpHelper = new HttpHelper();
        String entity = httpHelper.executeHttpGetRequest(url);
        Log.w("PTS-Android", "Entity : " + entity);
        JsonParser parser = new JsonParser();
        Gson gson = new Gson();// create a gson object
        JsonObject obj = (JsonObject) parser.parse(entity);
        categories = gson.fromJson(entity,Categories.class);

    }
    public void getAllCategoriesForCategory(){
        boolean isavailable = false;
        String result = "";
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
            String url = BuildConfig.HOST + WebServiceConstants.getSubCategoriesMethod +"/"+categoryId;
            Log.w("PTS-Android",url);
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
                subCategories = gson.fromJson(result,SubCategories.class);

                if (result.equals("YES")) {
                    isavailable = false;
                } else
                    isavailable = true;
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
    private class getAllSubCategoryForCategoryAsyncTask extends AsyncTask<Void, Void,Void>{
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            //  super.onProgressUpdate(values);
            progressBar.animate();
        }

        @Override
        protected Void doInBackground(Void... params) {
            getAllCategoriesForCategory();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //  super.onPostExecute(aVoid);
            progressBar.setVisibility(View.GONE);
            String[] subcategoriesArray = new String[subCategories.getSubCategories().length];
            for(int i=0;i<subCategories.getSubCategories().length;i++){
                subcategoriesArray[i] = subCategories.getSubCategories()[i].getSubCategoryName();
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, subcategoriesArray);
            tvSubCategory.setAdapter(adapter);
            tvSubCategory.showDropDown();
            tvSubCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tvSubCategory.showDropDown();
                }
            });

        }
    }
    private class getAllCategoriesAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            getAllCategories();
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            //super.onProgressUpdate(values);
            progressBar.animate();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //  super.onPostExecute(aVoid);
            progressBar.setVisibility(View.GONE);
            for(int i=0;i<categories.getCategories().length;i++){
                categoriesHM.put(categories.getCategories()[i].getCategoryName(), Integer.parseInt(categories.getCategories()[i].getCategoryId()));
            }
            String[] categories = new String[categoriesHM.size()];
            categories = (new ArrayList<String>(categoriesHM.keySet())).toArray(categories);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, categories);
            tvCategory.setAdapter(adapter);
            tvCategory.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        tvCategory.showDropDown();
                    }
                }
            });
            tvCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tvCategory.showDropDown();
                }
            });
            tvCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Log.w("PTS-Android", parent.getItemAtPosition(position).toString());
                    String key = parent.getItemAtPosition(position).toString();
                    categoryId = categoriesHM.get(key);
                    getAllSubCategoryForCategoryAsyncTask task = new getAllSubCategoryForCategoryAsyncTask();
                    task.execute();

                }
            });

        }


    }

    private class registerTutorServiceAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            requestJson = generateRegisterTutorServiceRequestJson();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            progressBar.animate();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressBar.setVisibility(View.GONE);

            if(registerResult.equals("YES")){
                Snackbar.make(findViewById(R.id.registerTutorCoordinatedLayout),R.string.success_register,Snackbar.LENGTH_LONG).setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        super.onDismissed(snackbar, event);
                        //go back to the login screen
                        onBackPressed();
                    }
                }).show();
            }else{
                Snackbar.make(findViewById(R.id.registerCoordinatedLayout),R.string.error_register,Snackbar.LENGTH_INDEFINITE);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {

            registerResult =  registerTutorService();
            return null;
        }
    }
    public String generateRegisterTutorServiceRequestJson() {
        String result = "";
        RegisterServiceRequestObject request = new RegisterServiceRequestObject();
        request.setUserId(userId);
        Category category = new Category();
        category.setCategoryName(textCategory);
        request.setCategory(category);

        SubCategory subCategory = new SubCategory();
        subCategory.setSubCategoryName(textSubCategory);
        request.setSubCategory(subCategory);
        ArrayList<Days> list = new ArrayList<Days>();
        for (int i = 0; i < days.length; i++) {
            if (days[i] == true) {
                Days day = new Days();
                day.setName(dayNames[i]);
                day.setStartTime(startTime);
                day.setEndTime(endTime);
                list.add(day);
            }
        }

        Days[] days = new Days[list.size()];
        days = list.toArray(days);

        Availability availability = new Availability();
        availability.setDays(days);

        request.setAvailability(availability);
        request.setPricePerHour(price);
        request.setWillingToTravelInMiles(distance);
        request.setAdvertise(advertise);

        result = RegisterServiceRequestObject.toJsonString(request);
        return result;
    }
    public String registerTutorService(){
        String result = "";
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try{
            String url = BuildConfig.HOST + WebServiceConstants.registerTutorServiceMethod;
            HttpPost postRequest = new HttpPost(url);
            postRequest.addHeader("Content-Type", "application/json");
            StringEntity postentity = new StringEntity(requestJson, "UTF-8");
            postentity.setContentType("application/json");
            postRequest.setEntity(postentity);

            HttpResponse httpResponse = httpclient.execute(postRequest);

            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
                Log.w("PTS-Android", "Entity : " + result);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
        }
        return result;


    }


}
