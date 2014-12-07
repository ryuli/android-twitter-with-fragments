package org.example.twitterappclient;

import org.example.twitterappclient.R.string;

import com.squareup.picasso.Picasso;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.User;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class FragmentProfile extends Fragment {
	
	private onProfileViewReadyListener mListener;
	private Context context;
	private User user;
	
	public interface onProfileViewReadyListener {
		public void onProfileViewReady();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof onProfileViewReadyListener) {
			mListener = (onProfileViewReadyListener) activity;
			if (user == null) {
				MainActivity mainActivity = (MainActivity) activity;
				user = mainActivity.user;
			}
			context = activity;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_profile, container, false);
		ImageView ivProfileImage = (ImageView) view.findViewById(R.id.ivProfileImage);
		TextView tvUserName = (TextView) view.findViewById(R.id.tvUserName);
		TextView tvScreenName = (TextView) view.findViewById(R.id.tvScreenName);
		TextView tvFollowingCount = (TextView) view.findViewById(R.id.tvFollowingCount);
		TextView tvFollowerCount = (TextView) view.findViewById(R.id.tvFollowerCount);
		TextView tvUserDesc = (TextView) view.findViewById(R.id.tvUserDesc);
		TextView tvLocation = (TextView) view.findViewById(R.id.tvLocation);
		TextView tvWebsite = (TextView) view.findViewById(R.id.tvWebsite);
		
		Picasso.with(context).load(user.getBiggerProfileImageURL()).placeholder(R.drawable.human_gray).into(ivProfileImage);
		tvUserName.setText(user.getName());
		tvScreenName.setText("@" + user.getScreenName());
		
		long userId = user.getId();
		
		tvFollowingCount.setOnClickListener(new CustomCountClickListener(userId, UserListActivity.FOLLOWING_USER_LIST_TYPE));
		tvFollowingCount.setText(String.valueOf(user.getFriendsCount()) + "  " + getString(R.string.following));
		
		tvFollowerCount.setOnClickListener(new CustomCountClickListener(userId, UserListActivity.FOLLOWER_USER_LIST_TYPE));
		tvFollowerCount.setText(String.valueOf(user.getFollowersCount()) + "  " + getString(R.string.follower));
		
		String userDesc = user.getDescription();
		if (userDesc == null || userDesc.equals("")) {
			tvUserDesc.setVisibility(View.GONE);
		} else {			
			tvUserDesc.setText(user.getDescription());
		}
		
		String userLocation = user.getLocation();
		if (userLocation == null || userLocation.equals("")) {
			tvLocation.setVisibility(View.GONE);
		} else {
			tvLocation.setText(userLocation);
		}
		
		String userWebsite = user.getURL();
		if (userWebsite == null || userWebsite.equals("")) {
			tvWebsite.setVisibility(View.GONE);
		} else {
			tvWebsite.setText(userWebsite);
		}
		
		return view;
		
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (mListener != null) {			
			mListener.onProfileViewReady();
		}
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	private class CustomCountClickListener implements OnClickListener {
		
		private long userId;
		private int listType;
		
		public CustomCountClickListener(long userId, int listType) {
			this.userId = userId;
			this.listType = listType;
		}

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(context, UserListActivity.class);
			intent.putExtra("user_id", userId);
			intent.putExtra("list_type", listType);
			startActivity(intent);
		}
		
		
	}
}
