package com.e.dudusgram.Profile;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.e.dudusgram.R;
import com.e.dudusgram.Utils.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;

public class EditProfileFragment extends Fragment {

    private static final String TAG = "EditProfileFragment";

    private ImageView mProfilePhoto;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editprofile, container, false);
        mProfilePhoto = view.findViewById(R.id.profile_photo);

        setProfileImage();

        //back arrow for navigating back to "Profile Activity"
        ImageView backArrow = view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Navigating back to ProfileActivity");
                getActivity().finish();
            }
        });

        return view;
    }



    private void setProfileImage(){
        Log.d(TAG, "setProfileImage: setting profile image.");
        String imgURL = "www.google.com/search?q=conrad+von+h%C3%B6tzendorf&source=lnms&tbm=isch&sa=X&ved=0ahUKEwje-qbTo8HhAhUHVhoKHTv5C8wQ_AUIDigB&biw=958&bih=927#imgrc=YEf-yiDuXoPh4M:";
        UniversalImageLoader.setImage(imgURL, mProfilePhoto, null, "https://");
    }
}
