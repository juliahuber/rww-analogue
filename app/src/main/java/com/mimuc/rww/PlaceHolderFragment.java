package com.mimuc.rww;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mimuc.rww.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.sentry.Sentry;
import io.sentry.SentryEvent;

public class PlaceHolderFragment extends Fragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.current_challenge_placeholder, container, false);

        Button button = (Button) view.findViewById(R.id.btnChallengeMe);
        button.setOnClickListener(this);

        return view;
    }


    @Override
    public void onClick(View view) {
        try {
            ((MainActivity) getActivity()).giveChallenge();
        } catch (Exception e) {
            System.out.println("e");
            String timeNow = new SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(new Date());
            System.out.println(timeNow + ": In MainActivity giveChallenge() method: " + e);
        }
    }
}




