package org.example.twitterappclient;

import java.util.List;

import com.squareup.picasso.Picasso;

import twitter4j.User;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UserAdapter extends ArrayAdapter<User> {
	
	private List<User> users;
	private Context context;

	public UserAdapter(Context context, int resource, List<User> users) {
		super(context, resource, users);
		this.context = context;
		this.users = users;
	}	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.user, parent, false);
		}
		
		ImageView ivProfileImage = (ImageView) convertView.findViewById(R.id.ivProfileImage);
		TextView tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
		TextView tvScreenName = (TextView) convertView.findViewById(R.id.tvScreenName);
		
		User user = users.get(position);
		
		Picasso.with(context).load(user.getBiggerProfileImageURL()).placeholder(R.drawable.human_gray).into(ivProfileImage);
		tvUserName.setText(user.getName());
		tvScreenName.setText("@" + user.getScreenName());
		
		return convertView;
	}



}
