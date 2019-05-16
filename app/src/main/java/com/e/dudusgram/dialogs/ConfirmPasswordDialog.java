package com.e.dudusgram.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.e.dudusgram.R;
import com.e.dudusgram.Utils.CustomText;

public class ConfirmPasswordDialog extends DialogFragment {

    private static final String TAG = "ConfirmPasswordDialogs";

    public interface OnConfirmPasswordListener{
        void onConfirmPassword(String password);
    }

    OnConfirmPasswordListener mOnConfirmPasswordListener;

    CustomText mPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_confirm_password, container, false);

        mPassword = view.findViewById(R.id.confirm_password);

        Log.d(TAG, "onCreateView: started.");

        TextView confirmDialog = view.findViewById(R.id.dialogConfirm);
        confirmDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: confirming the password");

                String password = mPassword.getText().toString();
                if (!password.equals("")){

                    mOnConfirmPasswordListener.onConfirmPassword(password);
                    getDialog().dismiss();

                } else {

                    Toast.makeText(getActivity(), "You must enter a password", Toast.LENGTH_SHORT).show();

                }

            }
        });

        TextView cancelDialog = view.findViewById(R.id.dialogCancel);
        cancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: closing the dialog");
                getDialog().dismiss();
                
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{

            mOnConfirmPasswordListener = (OnConfirmPasswordListener) getTargetFragment();

        } catch (ClassCastException e){

            Log.e(TAG, "onAttach: ClassCastException" + e.getMessage());

        }
    }
}
