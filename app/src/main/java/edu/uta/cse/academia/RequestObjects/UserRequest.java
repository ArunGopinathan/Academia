package edu.uta.cse.academia.RequestObjects;


import edu.uta.cse.academia.Models.User;

/**
 * Created by Arun on 2/22/2016.
 */
public class UserRequest {
    private edu.uta.cse.academia.Models.User User;

    public User getUser() {
        return User;
    }

    public void setUser(User user) {
        this.User = user;
    }
}
