package com.example.chatapp4;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        insertUserToDatabase();
    }
    public void insertUserToDatabase(){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        User iUser = new User(user.getUid(), user.getPhotoUrl().toString(), user.getDisplayName(), "offline");
        iUser.InsertUsers(iUser);
    }
}