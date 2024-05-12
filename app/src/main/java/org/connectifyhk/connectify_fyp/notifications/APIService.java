package org.connectifyhk.connectify_fyp.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAec8pvis:APA91bESNeINQJKO13spNAMN1zIWbh86p_7nnEQa9J_SYJk3OZ1VzOm8aFDSLku0gKYz7PbC5KzFvAWrzuB57Vo0VsZOP0RzoBvZerHHh81wwAquiJJeLiGc3jrfjX2YK62yT6dJTSy9"
    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);

}
