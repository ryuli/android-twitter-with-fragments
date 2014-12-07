package org.example.twitterappclient;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentMentions extends Fragment {
	
	private onMentionsViewReadyListener mListener;
	
	public interface onMentionsViewReadyListener {
		public void onMentionsViewReady();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof onMentionsViewReadyListener) {
			mListener = (onMentionsViewReadyListener) activity;
		}
	}
		
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mentions, container, false);
		
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mListener.onMentionsViewReady();
		
	}
	
}
