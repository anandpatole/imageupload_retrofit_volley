package com.learn.myapplication;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.VolleyError;
import com.learn.myapplication.volley.Volley;
import com.learn.myapplication.volley.VolleyNetworkRequest;

import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements ProgressRequestBody.UploadCallbacks {
    Context context;
    Uri picUri;
    String filePath;
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();

    private final static int ALL_PERMISSIONS_RESULT = 107;
    private final static int IMAGE_RESULT = 200;
    ProgressBar bar;
    int totalPer=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FloatingActionButton fab = findViewById(R.id.fab);
        Button button=findViewById(R.id.upload);
        Button b1=findViewById(R.id.uploadV);
        bar=(ProgressBar)findViewById(R.id.progressStatus);
        context= this;
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                uploadFile();

            }
        });

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                callWs();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(getPickImageChooserIntent(), IMAGE_RESULT);
            }
        });


        permissions.add(CAMERA);
        permissions.add(WRITE_EXTERNAL_STORAGE);
        permissions.add(READ_EXTERNAL_STORAGE);
        permissionsToRequest = findUnAskedPermissions(permissions);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }

    }

    public Intent getPickImageChooserIntent() {

        Uri outputFileUri = getCaptureImageOutputUri();

        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getPackageManager();

        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

        Intent mainIntent = allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

        Intent chooserIntent = Intent.createChooser(mainIntent, "Select source");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }


    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = getExternalFilesDir("");
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "profile.png"));
        }
        picUri=outputFileUri;
        return outputFileUri;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (resultCode == Activity.RESULT_OK) {

            ImageView imageView = findViewById(R.id.imageView);

            if (requestCode == IMAGE_RESULT) {
                filePath = getImageFilePath(data);
                if (filePath != null) {
                    Bitmap selectedImage = BitmapFactory.decodeFile(filePath);
                    imageView.setImageBitmap(selectedImage);
                }
            }

        }

    }


    private String getImageFromFilePath(Intent data) {
        boolean isCamera = data == null || data.getData() == null;
        picUri=data.getData();
        if (isCamera) return getCaptureImageOutputUri().getPath();
        else return com.learn.myapplication.PathUtils.getPath(getBaseContext(),data.getData());
//getPathFromURI(data.getData());
    }

    public String getImageFilePath(Intent data) {
        return getImageFromFilePath(data);
    }

    private String getPathFromURI(Uri contentUri)  {



        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        cursor.moveToFirst();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        return cursor.getString(column_index);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("pic_uri", picUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        picUri = savedInstanceState.getParcelable("pic_uri");
    }

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;
        }

    }


    @Override
    public void onProgressUpdate(int percentage) {

        bar.setProgress(percentage);

    }

    @Override
    public void onFinish() {
        bar.setVisibility(View.GONE);
    }

    public void uploadFile()
    {
        File a= new File(filePath);
        ProgressRequestBody albumImageBody = new ProgressRequestBody(a,this,context.getContentResolver().getType(picUri));
        MultipartBody.Part coverImagePart = MultipartBody.Part.createFormData("fileToUpload",
                a.getName(), albumImageBody);
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        String imageName = "filename";
        RequestBody description =
                RequestBody.create(
                        okhttp3.MultipartBody.FORM, imageName);
        // MediaType.parse(getContentResolver().getType(picUri))

        Call<UserProfile> result =  apiInterface.uploadImage(description, coverImagePart);
        bar.setVisibility(View.VISIBLE);
        result.enqueue( new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                showMessage("uploaded successfully");
                bar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                showMessage("Failed");
            }
        });

    }
    public void showMessage(String message) {
        try {
            android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Message");
            alertDialog.setMessage(message);
            alertDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void callWs()
    {

        HashMap<String, File> mFileParams = new HashMap<>();
        HashMap<String,String> body=new HashMap<>();
        body.put("filename","abc.jpg");
        mFileParams.put("fileToUpload",new File(filePath));

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest("https://appsgit.com/appsgit-service/fileupload.php"
                , mCallCancelTaskWSErrorListener
                , mCallCancelTaskWSResponseListener
                , null
                , body
                , mFileParams);
        Volley.getInstance(getBaseContext()).addToRequestQueue(mVolleyNetworkRequest);
    }

    com.android.volley.Response.Listener mCallCancelTaskWSResponseListener =new com.android.volley.Response.Listener() {
        @Override
        public void onResponse(Object response) {
            try {
                showMessage("uploaded");

            } catch (Exception e) {

            }
        }
    };

    com.android.volley.Response.ErrorListener mCallCancelTaskWSErrorListener =new com.android.volley.Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error)
        {
            showMessage("failed");
            System.out.println("-----------"+error);
        }
    };

}
