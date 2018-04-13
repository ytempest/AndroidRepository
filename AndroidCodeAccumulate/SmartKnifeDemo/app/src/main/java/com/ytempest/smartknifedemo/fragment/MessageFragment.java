package com.ytempest.smartknifedemo.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ytempest.smartknife.SmartKnife;
import com.ytempest.smartknife_annotations.LinkView;
import com.ytempest.smartknifedemo.R;


/**
 * @author ytempest
 *         Description:
 */
public class MessageFragment extends Fragment {


    @LinkView(R.id.tv_message)
    TextView mTextView;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return View.inflate(getActivity(), R.layout.fragment_message, null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SmartKnife.bind(this);
    }



}
