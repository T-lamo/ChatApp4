package com.example.chatapp4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp4.Notifications.Client;
import com.example.chatapp4.Notifications.Data;
import com.example.chatapp4.Notifications.MyResponse;
import com.example.chatapp4.Notifications.Sender;
import com.example.chatapp4.Notifications.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DisplayMessage extends AppCompatActivity {


    DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chats");
    TextView message ;
    ImageButton send;
    String receiver;

    private MessageAdapter adapter;
    List <MessageModel>  mChats ;
    RecyclerView recyclerView;
    FirebaseUser user;

    //Service Notification
    APIService apiService;
    Boolean notify=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);
        mChats = new ArrayList<>();
        //Creating Api service
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);




        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            receiver = extras.getString("receiver");

            //The key argument here must match that used in the other activity
        }

        user = FirebaseAuth.getInstance().getCurrentUser();
        message = findViewById(R.id.textMessage);
        send = findViewById(R.id.btnSend);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify =true;
                sendingMessage(receiver,user.getUid(),message.getText().toString());
                message.setText("");

            }
        });

        setDisplayMessage(receiver,user.getUid());
    }

    public void sendingMessage(final String receiver, String sender, String mes){
       Map<String, String> datetime = ServerValue.TIMESTAMP;
        MessageModel chatMessage = new MessageModel(receiver, sender,mes);
        chatMessage.InsertMessage(chatMessage);
       final String message = mes;
       DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Tokens").child(user.getUid());
       reference.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               User user1 = snapshot.getValue(User.class);
               if(notify) {
                   sendNotification(receiver, user.getDisplayName(), message);
               }

               notify =false;
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });


        /*DocumentReference ref = FirebaseFirestore.getInstance().collection("Users").document(user.getUid());
        ref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                   // Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                   // Log.d(TAG, "Current data: " + snapshot.getData());
                    User user1 = snapshot.toObject(User.class);
                    sendNotification(receiver,user1.getUserName(), message);
                } else {
                   // Log.d(TAG, "Current data: null");
                }
            }

        });*/
    }

    private void sendNotification(final String receiver, final String userName, final String message) {
       // CollectionReference token= FirebaseFirestore.getInstance().collection("Tokens");
        DatabaseReference tok= FirebaseDatabase.getInstance().getReference("Tokens");
        Query query =tok.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Token token = dataSnapshot.getValue(Token.class);
                    Data data = new Data(user.getUid(),R.mipmap.ic_launcher , userName+": "+ message , "new message", receiver);

                    Sender sender = new Sender(data,token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.code()==200){
                                        if(response.body().success != 1){
                                            Toast.makeText(DisplayMessage.this, "Sending notification failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void setDisplayMessage(final String receiver, final String sender) {
        recyclerView = findViewById(R.id.displayMessage);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);


        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mChats.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    MessageModel model = dataSnapshot.getValue(MessageModel.class);
                    if((model.getReceiver().equals(receiver) && model.getSender().equals(sender)) || (model.getReceiver().equals(sender) && model.getSender().equals(receiver))){
                        mChats.add(model);
                    }
                }
                adapter= new MessageAdapter(DisplayMessage.this, mChats);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void userStatus(String status){
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        reference.updateChildren(hashMap);

    }



    @Override
    protected void onResume() {
        super.onResume();
        userStatus("online");
    }
    @Override
    protected void onPause() {
        super.onPause();
        userStatus("offline");
    }

}