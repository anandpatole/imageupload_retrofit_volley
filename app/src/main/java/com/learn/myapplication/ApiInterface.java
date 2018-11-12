package com.learn.myapplication;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiInterface

{
    @Multipart
    @POST("/appsgit-service/fileupload.php")
    Call<UserProfile>  uploadImage(@Part("filename") RequestBody fileName, @Part MultipartBody.Part file);
}
