package edu.uta.cse.academia.helpers;

import android.telephony.PhoneNumberUtils;

/**
 * Created by Arun on 4/20/2016.
 */
public class ValidationHelper {
    public static boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
    public static boolean isValidPhoneNumber(CharSequence target){
        return target !=null && PhoneNumberUtils.isGlobalPhoneNumber(target.toString());
    }
}
