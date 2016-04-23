/**
 * 
 */
package edu.uta.cse.academia.RequestObjects;

import edu.uta.cse.academia.Models.Services;


/**
 * @author Arun
 *
 */
public class NearbyServicesResponse
{
    private edu.uta.cse.academia.Models.Services Services;

    public Services getServices ()
    {
        return Services;
    }

    public void setServices (Services Services)
    {
        this.Services = Services;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [Services = "+Services+"]";
    }
}