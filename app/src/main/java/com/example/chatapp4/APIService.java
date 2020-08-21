package com.example.chatapp4;

import com.example.chatapp4.Notifications.MyResponse;
import com.example.chatapp4.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "content-type:application/json",
            "Authorization:key=AAAAHWkoPIM:APA91bF44ROzakjqN2VK6GkKUl0lwTsLIeUcbxTDZ4dDjUPAUjlvKn3_OD49efX2wD4X305LAm0VKV2RnxJBD7934mOKdg71sYlzrIisZx9lHFwqb4AkvBqshTUpd_81Z2Ie0lsC0nBt"
    })
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
