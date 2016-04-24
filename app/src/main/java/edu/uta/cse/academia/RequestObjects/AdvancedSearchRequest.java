/**
 * 
 */
package edu.uta.cse.academia.RequestObjects;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.uta.cse.academia.Models.Category;
import edu.uta.cse.academia.Models.SubCategory;
import edu.uta.cse.academia.Models.Availability;

/**
 * @author Arun
 *
 */
public class AdvancedSearchRequest {
	
	//private User user;
	private Category Category;
	private SubCategory SubCategory;
	private int PriceBeloworEqual;
	private Availability Availability;
	private int DistanceWillingToTravel;

/*	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}*/

	public Category getCategory() {
		return Category;
	}

	public void setCategory(Category category) {
		this.Category = category;
	}

	public SubCategory getSubCategory() {
		return SubCategory;
	}

	public void setSubCategory(SubCategory subCategory) {
		this.SubCategory = subCategory;
	}

	public int getPriceBeloworEqual() {
		return PriceBeloworEqual;
	}

	public void setPriceBeloworEqual(int priceBeloworEqual) {
		this.PriceBeloworEqual = priceBeloworEqual;
	}

	public int getDistanceWillingToTravel() {
		return DistanceWillingToTravel;
	}

	public void setDistanceWillingToTravel(int distanceWillingToTravel) {
		this.DistanceWillingToTravel = distanceWillingToTravel;
	}
	
	public static AdvancedSearchRequest parseJsonToObject(String json){
		AdvancedSearchRequest request = new AdvancedSearchRequest();
		JsonParser parser = new JsonParser();
		Gson gson = new Gson();// create a gson object
		JsonObject obj = (JsonObject) parser.parse(json);
		try {//obj.get("AdvancedSearchRequest").toString()
			request = gson.fromJson(json,AdvancedSearchRequest.class);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return request;
		
	}
	
	public  String  toJsonString(){
		String result = "";
		JsonParser parser = new JsonParser();
		Gson gson = new Gson();// create a gson object
		try{
			result = gson.toJson(this, AdvancedSearchRequest.class);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		
		return result;
	}

	public Availability getAvailability() {
		return Availability;
	}

	public void setAvailability(Availability availability) {
		Availability = availability;
	}

}
