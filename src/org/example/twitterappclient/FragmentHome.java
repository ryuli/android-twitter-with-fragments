package org.example.twitterappclient;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class FragmentHome extends Fragment {
	
	private onHomeViewReadyListener mListener;
	
	public interface onHomeViewReadyListener {
		public void onHomeViewReady();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof onHomeViewReadyListener) {
			mListener = (onHomeViewReadyListener) activity;
		}
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_home, container, false);
		
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mListener.onHomeViewReady();
	}
	
}
