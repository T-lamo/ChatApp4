package com.example.chatapp4;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.example.chatapp4.Notifications.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.model.DocumentCollections;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ChatsActivity extends AppCompatActivity {

    DatabaseReference chatRef;

    private UserAdapter adapter;
    List<User> mUsers;
    RecyclerView recyclerView;
    FirebaseUser user;
    List <String> listStringIdUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        chatRef = FirebaseDatabase.getInstance().getReference("Chats");

        mUsers= new ArrayList<>();
        user=FirebaseAuth.getInstance().getCurrentUser();
        listStringIdUser= new ArrayList<>();
        getListChats();
    }

    private void getListChats(){
        recyclerView = findViewById(R.id.listChatUser);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listStringIdUser.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
                    if(messageModel.getSender().equals(user.getUid())){
                        listStringIdUser.add(messageModel.getReceiver());
                    }
                    if(messageModel.getReceiver().equals(user.getUid())){
                        listStringIdUser.add(messageModel.getSender());
                    }
                }

                //Returning single id user
                HashSet<String> hSetIdUser = new HashSet(listStringIdUser);
                listStringIdUser.clear();
                for(String strIdUser: hSetIdUser)
                {
                    listStringIdUser.add(strIdUser);
                }
                readChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        updateToken(FirebaseInstanceId.getInstance().getToken());
    }
    private void readChats() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);
                    for(String id : listStringIdUser ){
                        if(user.getId().equals(id)){
                            mUsers.add(user);
                        }
                    }

                }
                adapter= new UserAdapter(ChatsActivity.this, mUsers,true);
                recyclerView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public  void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(user.getUid()).setValue(token1);


    }
}