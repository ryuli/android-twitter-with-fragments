package org.example.twitterappclient;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.twitterappclient.R.layout;
import org.example.twitterappclient.R.string;

import com.squareup.picasso.Picasso;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.User;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.MonthDisplayHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StatusAdapter extends ArrayAdapter<Status> {

	private Context context;
	private List<Status> lists;
	private Map<String, String> timeFilter;
	private Status status;

	public StatusAdapter(Context context, List<Status> lists) {
		super(context, 0, lists);
		this.context = context;
		this.lists = lists;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		status = lists.get(position);
		if (status == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.loading_progress, parent, false);
			return convertView;
		}
		
		if (convertView == null || convertView.findViewById(R.id.ivProfileImage) == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.status,
					parent, false);
		}

		ImageView ivProfileImage = (ImageView) convertView
				.findViewById(R.id.ivProfileImage);
		TextView tvUserName = (TextView) convertView
				.findViewById(R.id.tvUserName);
		TextView tvScreenName = (TextView) convertView
				.findViewById(R.id.tvScreenName);
		TextView tvText = (TextView) convertView.findViewById(R.id.tvText);
		TextView tvCreateTime = (TextView) convertView
				.findViewById(R.id.tvCreateTime);
		TextView tvRetweetCount = (TextView) convertView
				.findViewById(R.id.tvRetweetCount);
		TextView tvFavouriteCount = (TextView) convertView
				.findViewById(R.id.tvFavouriteCount);
		ImageView ivMedia = (ImageView) convertView.findViewById(R.id.ivMedia);
		ImageView ivReply = (ImageView) convertView.findViewById(R.id.ivReply);

		MediaEntity[] medias = status.getMediaEntities();
		
		if (medias.length > 0) {
			if (medias[0].getType().equals("photo")) {
				ivMedia.setVisibility(View.VISIBLE);
				String mediaUrl = medias[0].getMediaURL();
				Picasso.with(context).load(mediaUrl).placeholder(R.drawable.camera_gray).into(ivMedia);
			}
		} else {
			ivMedia.setVisibility(View.GONE);
		}
		User user = status.getUser();

		Picasso.with(context).load(user.getBiggerProfileImageURL())
				.placeholder(R.drawable.human_gray).into(ivProfileImage);
		
		if (context instanceof MainActivity) {
			ivProfileImage.setOnClickListener(new ProfileClickListener(user));
		}

		tvUserName.setText(user.getName());
		tvScreenName.setText("@" + user.getScreenName());
		tvText.setText(status.getText());
		tvFavouriteCount.setText(String.valueOf(status.getFavoriteCount()));
		int retweetCount = status.getRetweetCount();
		if (retweetCount > 0) {
			tvRetweetCount.setText(String.valueOf(retweetCount));
		}

		tvCreateTime.setText(getTimeElapsedDesc(status.getCreatedAt()));
		
		ivReply.setOnClickListener(new ReplyClickListener(user.getScreenName()));

		return convertView;
	}

	private String getTimeElapsedDesc(Date date) {
		long createTime = date.getTime();
		String[] info = ((String) DateUtils.getRelativeDateTimeString(
				context, createTime, DateUtils.MINUTE_IN_MILLIS,
				DateUtils.HOUR_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL)).split(",");
		String timeElapsed = info[0];
		String elapsedDesc = timeElapsed.replace(" ago", "");
		elapsedDesc = elapsedDesc.replace("mins", "m");
		elapsedDesc = elapsedDesc.replace("min", "m");
		elapsedDesc = elapsedDesc.replace("hours", "h");
		elapsedDesc = elapsedDesc.replace("hour", "h");

		return elapsedDesc;
	}
	
	private class ProfileClickListener implements OnClickListener {
		
		private User user;
		
		public ProfileClickListener(User user) {
			this.user = user;
		}

		@Override
		public void onClick(View v) {
			
			Intent intent = new Intent(context, ProfileActivity.class);
			intent.putExtra("user_id", user.getId());
			context.startActivity(intent);
		}
		
	}
	
	private class ReplyClickListener implements OnClickListener {
		
		private String replyScreenName;
		private User user;
		
		public ReplyClickListener(String replyScreenName) {
			MainActivity activity = (MainActivity) context;
			this.user = activity.user;
			this.replyScreenName = replyScreenName;
		}

		@Override
		public void onClick(View v) {
			FragmentActivity activity = (FragmentActivity) context;
			FragmentManager fragmentManager = activity.getSupportFragmentManager();
			ReplyDialog dialog = new ReplyDialog(context, user, status, replyScreenName);
			dialog.show(fragmentManager, "reply_dialog");
		}
		
	}

}
