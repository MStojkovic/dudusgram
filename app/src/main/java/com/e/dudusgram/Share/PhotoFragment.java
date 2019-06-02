package com.e.dudusgram.Share;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.e.dudusgram.Profile.AccountSettingsActivity;
import com.e.dudusgram.R;
import com.e.dudusgram.Utils.Permissions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static android.app.Activity.RESULT_OK;

public class PhotoFragment extends Fragment {
    private static final String TAG = "PhotoFragment";

    private static final int PHOTO_FRAGMENT_NUM = 1;
    private static final String EXTRA_FILENAME = "com.e.dudusgram.EXTRA_FILENAME";
    private static final String FILENAME = "Photo";
    private static final int CONTENT_REQUEST = 1337;
    private static final String FILE_STRING = "file://";
    private File output=null;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        Log.d(TAG, "onCreateView: started.");

        Button btnLaunchCamera = view.findViewById(R.id.btnLaunchCamera);
        btnLaunchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: launching camera.");

                if(((ShareActivity)getActivity()).getCurrentTabNumber() == PHOTO_FRAGMENT_NUM){

                    if (((ShareActivity)getActivity()).checkPermissions(Permissions.CAMERA_PERMISSION)){

                        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                        StrictMode.setVmPolicy(builder.build());

                        Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                        if (savedInstanceState==null) {
                            File dir= new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Dudusgram");

                            if (!dir.exists()) {
                                dir.mkdirs();
                            }
                            output=new File(dir, FILENAME + getTimestamp() + ".jpeg");
                        }
                        else {
                            output=(File)savedInstanceState.getSerializable(EXTRA_FILENAME);
                        }

                        i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output));

                        startActivityForResult(i, CONTENT_REQUEST);

                    } else {

                        Intent intent = new Intent(getActivity(), ShareActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    }

                }

            }
        });

        return view;
    }

    private String getTimestamp(){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.UK);
        sdf.setTimeZone(TimeZone.getTimeZone(getString(R.string.timezone)));

        return sdf.format(new Date());
    }

    private boolean isRootTask(){

        return ((ShareActivity) getActivity()).getTask() == 0;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(EXTRA_FILENAME, output);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == CONTENT_REQUEST){

            Log.d(TAG, "onActivityResult: done taking a Photo.");
            Log.d(TAG, "onActivityResult: attempting to navigate to final screen.");

            if (resultCode == RESULT_OK){

                if (isRootTask()) {

                    try{
                        String imageUri = Uri.fromFile(output).toString();

                        if (imageUri.contains(FILE_STRING)) {
                            imageUri = imageUri.replace(FILE_STRING, "");
                        }

                        Intent intent = new Intent(getActivity(), NextActivity.class);
                        intent.putExtra(getString(R.string.selected_image), imageUri);
                        startActivity(intent);

                    } catch (NullPointerException e){
                        Log.e(TAG, "onActivityResult: NullPointerException" + e.getMessage());
                    }

                } else {

                    try{

                        String imageUri = Uri.fromFile(output).toString();
                        if (imageUri.contains(FILE_STRING)) {
                            imageUri = imageUri.replace(FILE_STRING, "");
                        }

                        Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                        intent.putExtra(getString(R.string.selected_image), imageUri);
                        intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment));
                        startActivity(intent);
                        getActivity().finish();

                    } catch (NullPointerException e){
                        Log.e(TAG, "onActivityResult: NullPointerException" + e.getMessage());
                    }


                }
            } else {
                Log.d(TAG, "onActivityResult: Camera closed by user.");
            }

        }
    }
}
