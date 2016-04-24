package edu.uta.cse.academia.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.HashMap;

import edu.uta.cse.academia.Activities.HomeActivity;
import edu.uta.cse.academia.BuildConfig;
import edu.uta.cse.academia.Constants.WebServiceConstants;
import edu.uta.cse.academia.Models.Availability;
import edu.uta.cse.academia.Models.Categories;
import edu.uta.cse.academia.Models.Category;
import edu.uta.cse.academia.Models.Days;
import edu.uta.cse.academia.Models.SubCategories;
import edu.uta.cse.academia.Models.SubCategory;
import edu.uta.cse.academia.R;
import edu.uta.cse.academia.RequestObjects.AdvancedSearchRequest;
import edu.uta.cse.academia.helpers.HttpHelper;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AdvancedSearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AdvancedSearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdvancedSearchFragment extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    View mView;
    Categories categories;
    CheckBox cbMon, cbTue, cbWed, cbThu, cbFri, cbSat, cbSun;
    SeekBar priceSeekBar;
    AutoCompleteTextView tvCategory, tvSubCategory;
    TextView pricePerHour;
    SubCategories subCategories;
    String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    Spinner travelInMilesSpinner;
    TextView labelPrice;
    ProgressBar progressBar;
    boolean[] days = {false, false, false, false, false, false, false};
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    int categoryId;
    HashMap<String, Integer> categoriesHM = new HashMap<String, Integer>();
    private OnFragmentInteractionListener mListener;

    public AdvancedSearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AdvancedSearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AdvancedSearchFragment newInstance(String param1, String param2) {
        AdvancedSearchFragment fragment = new AdvancedSearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_advanced_search, container, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mView = inflater.inflate(R.layout.fragment_advanced_search, null);
        initView(mView);
        builder.setView(mView).setPositiveButton("Search", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                AdvancedSearchRequest request = new AdvancedSearchRequest();
                if (!TextUtils.isEmpty(tvCategory.getText())) {
                    Category category = new Category();
                    category.setCategoryName(tvCategory.getText().toString());
                    request.setCategory(category);
                }
                if (!TextUtils.isEmpty(tvSubCategory.getText())) {
                    SubCategory category = new SubCategory();
                    category.setSubCategoryName(tvSubCategory.getText().toString());
                    request.setSubCategory(category);
                }
                if (priceSeekBar.getProgress() > 0) {
                    request.setPriceBeloworEqual(priceSeekBar.getProgress());
                }
                if (isAtleastOneDaySelected()) {
                    Availability availability = new Availability();
                    ArrayList<Days> daysList = new ArrayList<Days>();
                    for (int index = 0; index < days.length; index++) {
                        if (days[index]) {
                            Days day = new Days();
                            day.setName(dayNames[index]);
                            daysList.add(day);
                        }
                    }
                    Days[] daysarray = new Days[daysList.size()];
                    daysarray = daysList.toArray(daysarray);
                    availability.setDays(daysarray);
                }
                if (travelInMilesSpinner.getSelectedItemPosition() > 0) {
                    request.setDistanceWillingToTravel(Integer.parseInt(travelInMilesSpinner.getSelectedItem().toString()));
                }

                String json = request.toJsonString();
                HomeActivity callingActivity = (HomeActivity) getActivity();
                callingActivity.onUserSelectValue(json);
                dismiss();

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        return builder.create();

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

    private void initView(View view) {
        pricePerHour = (TextView) view.findViewById(R.id.labelPrice);
        priceSeekBar = (SeekBar) view.findViewById(R.id.priceSeekBar);
        priceSeekBar.setProgress(0);
        tvCategory = (AutoCompleteTextView) view.findViewById(R.id.categoryAutoCompleteTextView);
        progressBar = (ProgressBar) view.findViewById(R.id.registerServiceProgress);
        tvSubCategory = (AutoCompleteTextView) view.findViewById(R.id.subCategoryAutoCompleteTextView);
        getAllCategoriesAsyncTask task = new getAllCategoriesAsyncTask();
        task.execute();
        labelPrice = (TextView) view.findViewById(R.id.labelPrice);
        travelInMilesSpinner = (Spinner) view.findViewById(R.id.travelInMilesSpinner);
        cbMon = (CheckBox) view.findViewById(R.id.checkBoxMon);
        cbMon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                days[0] = isChecked;
             /*   if (isAtleastOneDaySelected())
                    labelAvailability.setError(null);*/


            }
        });
        cbTue = (CheckBox) view.findViewById(R.id.checkBoxTue);
        cbTue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                days[1] = isChecked;
                /*if (isAtleastOneDaySelected())
                    labelAvailability.setError(null);*/
            }
        });
        cbWed = (CheckBox) view.findViewById(R.id.checkBoxWed);
        cbWed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                days[2] = isChecked;
              /*  if (isAtleastOneDaySelected())
                    labelAvailability.setError(null);*/
            }
        });
        cbThu = (CheckBox) view.findViewById(R.id.checkBoxThu);
        cbThu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                days[3] = isChecked;
               /* if (isAtleastOneDaySelected())
                    labelAvailability.setError(null);*/
            }
        });
        cbFri = (CheckBox) view.findViewById(R.id.checkBoxFri);
        cbFri.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                days[4] = isChecked;
              /*  if (isAtleastOneDaySelected())
                    labelAvailability.setError(null);*/
            }
        });
        cbSat = (CheckBox) view.findViewById(R.id.checkBoxSat);
        cbSat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                days[5] = isChecked;
              /*  if (isAtleastOneDaySelected())
                    labelAvailability.setError(null);*/
            }
        });
        cbSun = (CheckBox) view.findViewById(R.id.checkBoxSun);
        cbSun.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                days[6] = isChecked;
               /* if (isAtleastOneDaySelected())
                    labelAvailability.setError(null);*/
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

    public void getAllCategories() {
        boolean isavailable = false;
        String url = BuildConfig.HOST + WebServiceConstants.getCategoriesMethod;
        HttpHelper httpHelper = new HttpHelper();
        String entity = httpHelper.executeHttpGetRequest(url);
        Log.w("PTS-Android", "Entity : " + entity);
        JsonParser parser = new JsonParser();
        Gson gson = new Gson();// create a gson object
        JsonObject obj = (JsonObject) parser.parse(entity);
        categories = gson.fromJson(entity, Categories.class);

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
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
            for (int i = 0; i < categories.getCategories().length; i++) {
                categoriesHM.put(categories.getCategories()[i].getCategoryName(), Integer.parseInt(categories.getCategories()[i].getCategoryId()));
            }
            String[] categories = new String[categoriesHM.size()];
            categories = (new ArrayList<String>(categoriesHM.keySet())).toArray(categories);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getBaseContext(), android.R.layout.simple_list_item_1, categories);
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

    private class getAllSubCategoryForCategoryAsyncTask extends AsyncTask<Void, Void, Void> {
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
            for (int i = 0; i < subCategories.getSubCategories().length; i++) {
                subcategoriesArray[i] = subCategories.getSubCategories()[i].getSubCategoryName();
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getBaseContext(), android.R.layout.simple_list_item_1, subcategoriesArray);
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

    public void getAllCategoriesForCategory() {
        boolean isavailable = false;
        String result = "";
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
            String url = BuildConfig.HOST + WebServiceConstants.getSubCategoriesMethod + "/" + categoryId;
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
                subCategories = gson.fromJson(result, SubCategories.class);

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
}
