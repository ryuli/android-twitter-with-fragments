package org.example.twitterappclient;

import java.util.List;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

public class UserListActivity extends Activity {
	
	public static final int FOLLOWING_USER_LIST_TYPE = 1;
	public static final int FOLLOWER_USER_LIST_TYPE = 2;
	private static final int NUMBER_OF_USERS_PER_PAGE = 25;
	
	private TwitterHelper twitterHelper;
	private Twitter twitter;
	private long userId;
	private int listType;
	private List<User> users;
	private ListView lvUserList;
	private AlertDialog.Builder alertDialog;
	private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_list);
		
		Intent intent = getIntent();
		userId = intent.getLongExtra("user_id", 0);
		listType = intent.getIntExtra("list_type", 0);
		
		init();
		process();
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	private void init() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
		twitterHelper = new TwitterHelper(this);
		twitter = twitterHelper.getAuthorizedTwitter();
		
		lvUserList = (ListView) findViewById(R.id.lvUserList);
		
    	alertDialog = new AlertDialog.Builder(this);
    	alertDialog.setTitle(R.string.message);
    	alertDialog.setPositiveButton(R.string.ok, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {				
			}
		});
    	
    	progressDialog = new ProgressDialog(this);
    	progressDialog.setMessage(getString(R.string.processing_hint));
	}
	
	private void process() {
		progressDialog.show();
		
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				String errorMessage = null;
				try {
					if (listType == FOLLOWING_USER_LIST_TYPE) {
						users = twitter.getFriendsList(userId, -1, NUMBER_OF_USERS_PER_PAGE);
					} else if (listType == FOLLOWER_USER_LIST_TYPE) {
						users = twitter.getFollowersList(userId, -1, NUMBER_OF_USERS_PER_PAGE);
					} else {
						throw new Exception(getString(R.string.invalid_user_list_type));
					}
				} catch (TwitterException e) {
					errorMessage = e.getMessage();
				} catch (Exception e) {
					errorMessage = e.getMessage();
				}				
				return errorMessage;
			}
			
			protected void onPostExecute(String errorMessage) {
				if (errorMessage != null) {
					showAlertMessage(errorMessage);
				} else {
					renderList();
				}
				
				progressDialog.hide();
			};
			
		}.execute();
	}
	
    private void showAlertMessage(String message) {
    	alertDialog.setMessage(message);
    	alertDialog.show();
    }
    
    private void renderList() {
    	UserAdapter adapter = new UserAdapter(this, 0, users);
    	lvUserList.setAdapter(adapter);
    }
}
