package edu.uta.cse.academia.helpers;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

import edu.uta.cse.academia.Constants.AppConstants;

/**
 * Created by Arun on 4/20/2016.
 */
public class HttpHelper {
    public String executeHttpGetRequest(String url) {

        String result = "";
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
            HttpGet getRequest = new HttpGet(url);
            Log.w(AppConstants.LOG_TAG, "URL->" + url);
            HttpResponse httpResponse = httpclient.execute(getRequest);
            HttpEntity entity = null;
            entity = httpResponse.getEntity();
            result = EntityUtils.toString(entity);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        return result;
    }

    public String executeHttpPostRequest(String url, String jsonRequest) {
        String result = "";
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
            HttpPost httpPost = new HttpPost(url);
            Log.w(AppConstants.LOG_TAG, "URL->" + url);
            httpPost.addHeader("Content-Type", "application/json");
            StringEntity postentity = new StringEntity(jsonRequest, "UTF-8");
            postentity.setContentType("application/json");
            httpPost.setEntity(postentity);

            HttpResponse httpResponse = httpclient.execute(httpPost);

            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
                Log.w(AppConstants.LOG_TAG, "Entity : " + result);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        return result;
    }
}
