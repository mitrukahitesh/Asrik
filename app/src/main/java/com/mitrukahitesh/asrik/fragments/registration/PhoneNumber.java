package com.mitrukahitesh.asrik.fragments.registration;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;
import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.utility.Constants;

public class PhoneNumber extends Fragment {

    private TextInputEditText phone;

    public PhoneNumber() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_phone_number, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView signIn = view.findViewById(R.id.sign_in_tv);
        Button next = view.findViewById(R.id.next);
        phone = view.findViewById(R.id.phone);
        String text = "Already have a account? Sign-In";
        SpannableString ss = new SpannableString(text);
        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                //onclick
            }
        };
        ss.setSpan(clickableSpan1, 24, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        signIn.setText(ss);
        signIn.setMovementMethod(LinkMovementMethod.getInstance());
        next.setOnClickListener(v -> {
            if (phone.getText() == null) {
                phone.requestFocus();
                return;
            }
            if (phone.getText().toString().length() != 10) {
                phone.requestFocus();
                return;
            }
            String num = phone.getText().toString();
            num = "+91" + num;
            Bundle bundle = new Bundle();
            bundle.putString(Constants.NUMBER, num);
            bundle.putBoolean(Constants.REGISTER, true);
            Navigation.findNavController(view).navigate(R.id.action_phoneNumber_to_otp, bundle);
        });
    }
}