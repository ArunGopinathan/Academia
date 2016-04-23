package edu.uta.cse.academia.Activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import edu.uta.cse.academia.BuildConfig;
import edu.uta.cse.academia.Constants.AppConstants;
import edu.uta.cse.academia.Constants.WebServiceConstants;
import edu.uta.cse.academia.Models.Address;
import edu.uta.cse.academia.Models.User;
import edu.uta.cse.academia.R;
import edu.uta.cse.academia.helpers.AddressValidationHelper;
import edu.uta.cse.academia.helpers.HttpHelper;
import edu.uta.cse.academia.helpers.SecurityUtils;
import edu.uta.cse.academia.helpers.ValidationHelper;

public class RegisterActivity extends AppCompatActivity {

    EditText firstNameView, lastNameView, emailView, passwordView, confirmPasswordView;
    TextView spinnerErrorView, addressView;
    CardView nameCardView;
    Spinner userTypeSpinner;
    EditText addressLine1View, addressLine2View, cityEditView, zipEditView, phoneNumberView;
    AutoCompleteTextView stateTextView;
    Button registerButton;
    ProgressBar progressBar;
    User user;
    Address globAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = (ProgressBar) findViewById(R.id.registerProgressBar);

        firstNameView = (EditText) findViewById(R.id.FirstName);
        lastNameView = (EditText) findViewById(R.id.LastName);
        emailView = (EditText) findViewById(R.id.Email);
        if(!TextUtils.isEmpty(getIntent().getStringExtra(AppConstants.EMAIL))){
            emailView.setText(getIntent().getStringExtra(AppConstants.EMAIL));
        }
        emailView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {

                //if it does not have focus check for availability
                if (!b) {
                    if (!TextUtils.isEmpty(emailView.getText()) && ValidationHelper.isValidEmail(emailView.getText())) {

                        //check availability for the username
                        CheckAvailabilityAsyncTask task = new CheckAvailabilityAsyncTask();
                        task.execute(emailView.getText().toString());

                    } else {
                        emailView.setError(getResources().getString(R.string.error_account_not_available));
                    }
                }
            }
        });
        passwordView = (EditText) findViewById(R.id.Password);
        if(!TextUtils.isEmpty(getIntent().getStringExtra(AppConstants.PASSWORD))){
            passwordView.setText(getIntent().getStringExtra(AppConstants.PASSWORD));
        }
        confirmPasswordView = (EditText) findViewById(R.id.ConfirmPassword);

        userTypeSpinner = (Spinner) findViewById(R.id.userTypeSpinner);


        addressLine1View = (EditText) findViewById(R.id.AddressLine1);
        addressLine2View = (EditText) findViewById(R.id.AddressLine2);

        stateTextView = (AutoCompleteTextView) findViewById(R.id.State);
        String[] states = getResources().getStringArray(R.array.statearray);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, states);
        stateTextView.setAdapter(adapter);

        cityEditView = (EditText) findViewById(R.id.City);
        zipEditView = (EditText) findViewById(R.id.ZipCode);
        phoneNumberView = (EditText) findViewById(R.id.PhoneNumber);
        addressView = (TextView) findViewById(R.id.Address);

        registerButton =  (Button) findViewById(R.id.SignUpButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void attemptRegister() {

        boolean cancel = false;
        View focusView = null;
        //phone number validation
        if (TextUtils.isEmpty(phoneNumberView.getText())) {
            phoneNumberView.setError(getString(R.string.error_field_required));
            focusView = phoneNumberView;
            cancel = true;
        } else {
            phoneNumberView.setError(null);
        }
        //zip code validation
        if (TextUtils.isEmpty(zipEditView.getText())) {
            zipEditView.setError(getString(R.string.error_field_required));
            focusView = zipEditView;
            cancel = true;
        } else if (!ValidationHelper.isValidPhoneNumber(zipEditView.getText())) {
            zipEditView.setError(getString(R.string.error_invalid_phone_number));
            focusView = zipEditView;
            cancel = true;
        } else {
            zipEditView.setError(null);
        }
        //State validation
        if (TextUtils.isEmpty(stateTextView.getText())) {
            stateTextView.setError(getString(R.string.error_field_required));
            focusView = stateTextView;
            cancel = true;
        } else {
            stateTextView.setError(null);
        }
        //City Validation
        if (TextUtils.isEmpty(cityEditView.getText())) {
            cityEditView.setError(getString(R.string.error_field_required));
            focusView = cityEditView;
            cancel = true;
        } else {
            cityEditView.setError(null);
        }
        //Address Line 1 validation
        if (TextUtils.isEmpty(addressLine1View.getText())) {
            addressLine1View.setError(getString(R.string.error_field_required));
            focusView = addressLine1View;
            cancel = true;
        }else{
            addressLine1View.setError(null);
        }
        //user Type validation
        View view = userTypeSpinner.getSelectedView();
        TextView textView = null;
        if(view!=null && view instanceof  TextView){
            textView = (TextView) view;
            if(userTypeSpinner.getSelectedItemPosition()==0){

                textView.setError("Please Select UserType");
                cancel = true;
                focusView = userTypeSpinner;
            }
            else{
                textView.setError(null);

            }
        }

        //confirm password validation
        if (TextUtils.isEmpty(confirmPasswordView.getText())) {
            confirmPasswordView.setError(getString(R.string.error_field_required));
            focusView = confirmPasswordView;
            cancel = true;
        }else{
            confirmPasswordView.setError(null);
        }
        //password validation
        if (TextUtils.isEmpty(passwordView.getText())) {
            passwordView.setError(getString(R.string.error_field_required));
            focusView = passwordView;
            cancel = true;
        }else if(!confirmPasswordView.getText().toString().equals(passwordView.getText().toString())){
            passwordView.setError(getString(R.string.error_password_mismatch));
            focusView = passwordView;
            cancel = true;
        }
        else{
            passwordView.setError(null);
        }

        //email validation
        if (TextUtils.isEmpty(emailView.getText())) {
            emailView.setError(getString(R.string.error_field_required));
            focusView = emailView;
            cancel = true;
        }else if(!ValidationHelper.isValidEmail(emailView.getText())){
            emailView.setError(getString(R.string.error_email_invalid));
            focusView = emailView;
            cancel = true;
        }
        else{
            emailView.setError(null);
        }

        //last Name validation
        if (TextUtils.isEmpty(lastNameView.getText())) {
            lastNameView.setError(getString(R.string.error_field_required));
            focusView = lastNameView;
            cancel = true;
        }
        else{
            lastNameView.setError(null);
        }

        //first Name validation
        if (TextUtils.isEmpty(firstNameView.getText())) {
            firstNameView.setError(getString(R.string.error_field_required));
            focusView = firstNameView;
            cancel = true;
        }
        else{
            firstNameView.setError(null);
        }

        //some error in fields so request focus
        if(cancel){
            focusView.requestFocus();
        } //else we are good to go proceed with registration
        else{

            //first validate the address
            validateAddressAsyncTask validateAddressAsyncTask = new validateAddressAsyncTask();

            Address address = new Address();
            address.setAddressLine1(addressLine1View.getText().toString());
            address.setAddressLine2(addressLine2View.getText().toString());
            address.setCity(cityEditView.getText().toString());
            address.setState(stateTextView.getText().toString());
            address.setZipCode(zipEditView.getText().toString());

            validateAddressAsyncTask.execute(address);

        }

    }

    private class validateAddressAsyncTask extends AsyncTask<Address, Void, Address>{
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            progressBar.animate();
        }

        @Override
        protected void onPostExecute(Address address) {
            super.onPostExecute(address);
            progressBar.setVisibility(View.GONE);
            if(address.getLattitude()==null && address.getLongitude()==null){
                addressView.setError(getString(R.string.address_invalid));
            }
            else{
                globAddress =address;
                addressView.setError(null);
                Log.w(AppConstants.LOG_TAG,globAddress.toString());

                //now address is validated, register the user
                User user= new User();
                user.setFirstName(firstNameView.getText().toString());
                user.setLastName(lastNameView.getText().toString());
                user.setEmail(emailView.getText().toString());
                user.setAddress(globAddress);
                user.setPassword(SecurityUtils.md5(passwordView.getText().toString()));
                user.setUserType(Integer.toString(userTypeSpinner.getSelectedItemPosition()));
                user.setPhoneNumber(phoneNumberView.getText().toString());

                registerAsyncTask registerAsyncTask = new registerAsyncTask();
                registerAsyncTask.execute(user);
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Address doInBackground(Address... addresses) {
            //validate address
            AddressValidationHelper helper = new AddressValidationHelper();
            return helper.validateAddressWithGoogleApi(addresses[0]);
        }
    }

    private boolean registerUser(User user){
        boolean status = false;
        String url =BuildConfig.HOST + WebServiceConstants.REGISTER_METHOD;
        HttpHelper httpHelper = new HttpHelper();
        String entity = httpHelper.executeHttpPostRequest(url,User.toJsonString(user));
        //if yes then register successful
        if(entity.equals(WebServiceConstants.YES))
            status = true;
        return status;
    }

    private class registerAsyncTask extends AsyncTask<User,Void,Boolean>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            progressBar.setVisibility(View.GONE);
            if(aBoolean){
                Snackbar.make(findViewById(R.id.registerCoordinatedLayout),R.string.success_register,Snackbar.LENGTH_LONG).setCallback(new Snackbar.Callback() {
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
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            progressBar.animate();
        }

        @Override
        protected Boolean doInBackground(User... users) {
            return registerUser(users[0]);
        }
    }

    private class CheckAvailabilityAsyncTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        private boolean checkAvailability(String username) {
            boolean isavailable = false;
            String url = BuildConfig.HOST + WebServiceConstants.CHECK_AVAILABILITY_METHOD + username;
            HttpHelper httpHelper = new HttpHelper();
            String entity = httpHelper.executeHttpGetRequest(url);
            if (!TextUtils.isEmpty(entity) && !entity.equals(WebServiceConstants.YES))
                isavailable = true;
            return isavailable;
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            return checkAvailability(strings[0]);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            progressBar.setVisibility(View.GONE);
            if (aBoolean) {
                //the account is available
                emailView.setError(null);
                emailView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_verified_user_black_24dp, 0, 0, 0);

            } else {
                //the account is not available
                emailView.setError(getResources().getString(R.string.error_account_not_available));
                emailView.requestFocus();
            }

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            progressBar.animate();
        }
    }

}
