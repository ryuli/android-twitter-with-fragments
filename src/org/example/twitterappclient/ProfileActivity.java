package org.example.twitterappclient;

import java.util.ArrayList;
import java.util.List;

import org.example.twitterappclient.FragmentProfile.onProfileViewReadyListener;
import org.example.twitterappclient.R.string;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;

public class ProfileActivity extends FragmentActivity implements onProfileViewReadyListener {
	
	private SwipeRefreshLayout swipeUserContainer;
	private Twitter twitter;
	private User user;
	private long userId;
	private ListView lvUserTimeline;
	private List<twitter4j.Status> userStatuses;
	private StatusAdapter userAdapter;
	private Paging userPaging;
	private boolean isLoading = false;
	private AlertDialog.Builder alertDialog;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		
		Intent intent = getIntent();
		userId = intent.getLongExtra("user_id", 0);
		
		init();
		
	}

	private void process() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		FragmentProfile fragmentProfile = new FragmentProfile();
		fragmentProfile.setUser(user);
		transaction.replace(R.id.flProfileContainer, fragmentProfile);
		transaction.commit();
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

	@Override
	public void onProfileViewReady() {
		initUserSwipeRefresh();
		lvUserTimeline = (ListView) findViewById(R.id.lvUserTimeline);
		lvUserTimeline.setOnScrollListener(new CustomScrollListener());
		userAdapter = new StatusAdapter(ProfileActivity.this, userStatuses);
		lvUserTimeline.setAdapter(userAdapter);
		loadUserTimeline();
		
	}
	
	private void init() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		userStatuses = new ArrayList<Status>();
    	userPaging = new Paging(MainActivity.INIT_PAGE, MainActivity.NUMBER_OF_TWEETS_PER_PAGE);
    	
    	alertDialog = new AlertDialog.Builder(this);
    	alertDialog.setTitle(R.string.message);
    	alertDialog.setPositiveButton(R.string.ok, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {				
			}
		});
    	
    	progressDialog = new ProgressDialog(this);
    	progressDialog.setMessage(getString(R.string.processing_hint));
    	
		TwitterHelper twitterHelper = new TwitterHelper(this);
		twitter = twitterHelper.getAuthorizedTwitter();
		
		initUser();
	}
	
	private void initUser() {
		progressDialog.show();
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				String errorMessage = null;
				try {
					user = twitter.showUser(userId);
				} catch (TwitterException e) {
					errorMessage = e.getMessage();
				}
				return errorMessage;
			}
			
			protected void onPostExecute(String errorMessage) {
				if (errorMessage != null) {
					showAlertMessage(errorMessage);
				} else {
					process();
				}
				
				progressDialog.hide();
			};
			
		}.execute();
	}
	
	private void showLoadingProgress() {
		Status nullStatus = null;
		userStatuses.add(nullStatus);
		userAdapter.notifyDataSetChanged();
	}
	
    private void appendUserStatuses(ResponseList<Status> lists) {
    	removeNullStatus();
    	for (Status status : lists) {
			userStatuses.add(status);
		}
    }
    
	private void removeNullStatus() {
		if (userStatuses != null) {
			int nullStatusIndex = userStatuses.size() - 1;
			userStatuses.remove(nullStatusIndex);			
		}
	}
	
    private void showAlertMessage(String message) {
    	alertDialog.setMessage(message);
    	alertDialog.show();
    }
    
	private void initUserSwipeRefresh() {
		swipeUserContainer = (SwipeRefreshLayout) findViewById(R.id.swipeUserContainer);
		swipeUserContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
        		android.R.color.holo_green_light,
        		android.R.color.holo_orange_light,
        		android.R.color.holo_red_light);
        
		swipeUserContainer.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				resetUserStatuses();
				loadUserTimeline();
			}
		});
	}
	
	private void resetUserStatuses() {
    	userStatuses.clear();
    	userPaging.setPage(MainActivity.INIT_PAGE);
	}
	
	private void stopSwipeRefreshing() {		
		if (swipeUserContainer != null && swipeUserContainer.isRefreshing()) {
			swipeUserContainer.setRefreshing(false);
		}
	}
	
    private void loadUserTimeline() {
    	//Log.i("loadMentionsTimeline", "trigger");
    	showLoadingProgress();
    	
    	isLoading = true;
    	
    	new AsyncTask<Void, Void, String>() { 

			@Override
			protected String doInBackground(Void... params) {
				String errorMessage = null;
				String screenName = user.getScreenName();
				try {
					if (userStatuses.size() == 0) {
						userStatuses.addAll(twitter.getUserTimeline(screenName, userPaging));
					} else {
						//Log.i("loadMentionsTimeline", "append");
						appendUserStatuses(twitter.getUserTimeline(screenName, userPaging));
					}
					
					if (userStatuses == null) {
						//Log.i("loadMentionsTimeline", "status is still null");
						throw new TwitterException(getString(R.string.get_user_timeline_error));
					}
				} catch (TwitterException e) {
					//Log.i("loadMentionsTimeline", "twitter exception: " + e.getMessage());
					errorMessage = e.getMessage();
				}
				
				return errorMessage;
			}
			
			protected void onPostExecute(String errorMessage) {
				
				if (errorMessage == null) {					
					userAdapter.notifyDataSetChanged();
				} else {
					//Log.i("loadMentionsTimeline", "error message: " + errorMessage);
					if (userPaging.getPage() > MainActivity.INIT_PAGE) {						
						hideLoadingProgress();
					}
					showAlertMessage(errorMessage);
				}
				stopSwipeRefreshing();
				
				isLoading = false;
			};
			
		}.execute();
    	    	
    }
	
	private void hideLoadingProgress() {
		removeNullStatus();
		userAdapter.notifyDataSetChanged();
	}
    
	private class CustomScrollListener implements OnScrollListener {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			
			int viewId = view.getId();

			if (userStatuses == null || userStatuses.size() == 0 || totalItemCount == 0 || (visibleItemCount == totalItemCount) || isLoading) {
				return;
			}
			
			if ((firstVisibleItem + visibleItemCount) == totalItemCount && userStatuses.size() >= MainActivity.NUMBER_OF_TWEETS_PER_PAGE) {
					//Log.i("onScroll", "trigger mentions");
				userPaging.setPage(userPaging.getPage() + 1);
				loadUserTimeline();			

			}
		}
		
	}
}
