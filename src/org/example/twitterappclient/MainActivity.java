package org.example.twitterappclient;

import java.util.ArrayList;
import java.util.List;

import org.example.twitterappclient.FragmentHome.onHomeViewReadyListener;
import org.example.twitterappclient.FragmentMentions.onMentionsViewReadyListener;
import org.example.twitterappclient.FragmentProfile.onProfileViewReadyListener;
import org.example.twitterappclient.ReplyDialog.OnReplyCompletedListener;
import org.example.twitterappclient.TwitterHelper.TwitterAuthListener;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;

public class MainActivity extends ActionBarActivity implements
		TwitterAuthListener, onHomeViewReadyListener,
		onMentionsViewReadyListener, onProfileViewReadyListener,
		OnReplyCompletedListener {
	
	public static final String KEY_FOR_EXTRA_USER = "user";
	public static final int INIT_PAGE = 1;
	public static final int NUMBER_OF_TWEETS_PER_PAGE = 25;
	private static final int REQUEST_CODE_FOR_COMPOSE = 100;
	private static final int HOME_TIMELINE_TYPE = 1;
	private static final int MENTIONS_TIMELINE_TYPE = 2;
	private static final int USER_TIMELINE_TYPE = 3;
	
	public User user;
	public Twitter twitter;
	public ProgressDialog progressDialog;
	
	private SwipeRefreshLayout swipeHomeContainer;
	private SwipeRefreshLayout swipeMentionsContainer; 
	private SwipeRefreshLayout swipeUserContainer;
	private ListView lvHomeTimeline;
	private ListView lvMentionsTimeline;
	private ListView lvUserTimeline;
	private Paging homePaging;
	private Paging mentionsPaging;
	private Paging userPaging;
	private StatusAdapter homeAdapter;
	private StatusAdapter mentionsAdapter;
	private StatusAdapter userAdapter;
	private List<twitter4j.Status> homeStatuses;
	private List<twitter4j.Status> mentionsStatuses;
	private List<twitter4j.Status> userStatuses;
	private int posOfHome = 0;
	private int topOfHome = 0;
	private int posOfMentions = 0;
	private int topOfMentions = 0;
	private int posOfUser = 0;
	private int topOfUser = 0;
	
	private AlertDialog.Builder alertDialog;
	private TwitterHelper twitterHelper;
	
	
	
	
	private boolean isLoading = false;
	private int currentTimelineType;
	

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }
    
    private void setupTabs() {
    	ActionBar actionBar = getSupportActionBar();
    	actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    	actionBar.setDisplayShowTitleEnabled(true);
    	
    	Tab tab1 = actionBar
    			.newTab()
    			.setIcon(R.drawable.home_white)
    			.setText("Home")
    			.setTabListener(new SupportFragmentTabListener<FragmentHome>(R.id.flContainer, this, "Home", FragmentHome.class));
    	actionBar.addTab(tab1);
    	actionBar.selectTab(tab1);
    	
    	Tab tab2 = actionBar
    			.newTab()
    			.setIcon(R.drawable.arroba_white)
    			.setText("Mentions")
    			.setTabListener(new SupportFragmentTabListener<FragmentMentions>(R.id.flContainer, this, "Mentions", FragmentMentions.class));
    	actionBar.addTab(tab2);
    	
    	Tab tab3 = actionBar
    			.newTab()
    			.setIcon(R.drawable.profile_white)
    			.setText("Profile")
    			.setTabListener(new SupportFragmentTabListener<FragmentProfile>(R.id.flContainer, this, "Profile", FragmentProfile.class));
    	actionBar.addTab(tab3);
    			
		
	}
    	
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.activity_main, menu);
    	return true;
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
    	if (itemId == R.id.mCompose) {
    		//Log.i("mCompose", user.getBiggerProfileImageURL());
    		
			Intent intent = new Intent(this, ComposeTweetActivity.class);
			intent.putExtra(KEY_FOR_EXTRA_USER, user);
			startActivityForResult(intent, REQUEST_CODE_FOR_COMPOSE);
			
		}
    	return true;
    }    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
    	if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_FOR_COMPOSE) {
			String composeText = intent.getStringExtra(ComposeTweetActivity.KEY_FOR_EXTRA_COMPOSE_TEXT);
			addNewTweet(composeText);
		}
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
    }
    
    
    private void init() {
    	userStatuses = new ArrayList<Status>();
    	mentionsStatuses = new ArrayList<Status>();
    	
    	homePaging = new Paging(INIT_PAGE, NUMBER_OF_TWEETS_PER_PAGE);
    	mentionsPaging = new Paging(INIT_PAGE, NUMBER_OF_TWEETS_PER_PAGE);
    	userPaging = new Paging(INIT_PAGE, NUMBER_OF_TWEETS_PER_PAGE);
    	
    	alertDialog = new AlertDialog.Builder(this);
    	alertDialog.setTitle(R.string.message);
    	alertDialog.setPositiveButton(R.string.ok, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {				
			}
		});
    	
    	progressDialog = new ProgressDialog(this);
    	progressDialog.setMessage(getString(R.string.processing_hint));
    	
    	twitterHelper = new TwitterHelper(this);
    	
    	setupTabs();
    }
    
    
	private void initHomeSwipeRefresh() {
        swipeHomeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeHomeContainer);
        swipeHomeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
        		android.R.color.holo_green_light,
        		android.R.color.holo_orange_light,
        		android.R.color.holo_red_light);
        
        swipeHomeContainer.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				resetHomeStatuses();
				loadHomeTimeline();
			}
		});
	}
	
	private void initMentionsSwipeRefresh() {
		swipeMentionsContainer = (SwipeRefreshLayout) findViewById(R.id.swipeMentionsContainer);
		swipeMentionsContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
        		android.R.color.holo_green_light,
        		android.R.color.holo_orange_light,
        		android.R.color.holo_red_light);
        
		swipeMentionsContainer.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				resetMentionsStatuses();
				loadMentionsTimeline();
			}
		});
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


	private void stopSwipeRefreshing() {
		if (swipeHomeContainer != null && swipeHomeContainer.isRefreshing()) {
			swipeHomeContainer.setRefreshing(false);
		}
		
		if (swipeMentionsContainer != null && swipeMentionsContainer.isRefreshing()) {
			swipeMentionsContainer.setRefreshing(false);
		}
		
		if (swipeUserContainer != null && swipeUserContainer.isRefreshing()) {
			swipeUserContainer.setRefreshing(false);
		}
	}
    
    private void process() throws TwitterException {
    	progressDialog.show();
    	
    	if (!NetworkHelper.isNetworkConnected(this)) {
    		showAlertMessage(getString(R.string.network_not_ready));
    		return;
    	}
    	
    	if (twitterHelper.isLogin()) {
			prepareTwitter();

		} else {
			Uri uri = getIntent().getData();
			if (uri != null && uri.toString().startsWith(TwitterHelper.CALLBACK_URL)) {
				twitterHelper.saveAccessToken(uri);
			} else {
				showLoginDialog();
			}
		}
    }
    
    private void showLoginDialog() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(R.string.message)
				.setMessage(R.string.login_hint)
				.setPositiveButton(R.string.login_now,
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								twitterHelper.startLoginProcess();
							}
						}).show();
    }

	private void prepareTwitter() throws TwitterException {
		twitter = twitterHelper.getAuthorizedTwitter();
		twitterHelper.prepareUser();
	}
    
    private void loadHomeTimeline() {
    	if (homePaging.getPage() > INIT_PAGE) {
    		showLoadingProgress();
		}
    	isLoading = true;

    	
    	new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				String errorMessage = null;
				try {
					if (homePaging.getPage() == INIT_PAGE && homeStatuses == null) {
						homeStatuses = twitter.getHomeTimeline(homePaging);
					} else {
						if (homeStatuses.size() == 0) {
							
							homeStatuses.addAll(twitter.getHomeTimeline(homePaging));
						} else {
							appendHomeStatuses(twitter.getHomeTimeline(homePaging));
						}
					}
					if (homeStatuses == null) {
						throw new TwitterException(getString(R.string.get_home_timeline_error));
					}
				} catch (TwitterException e) {
					errorMessage = e.getMessage();
				}
				
				return errorMessage;
			}
			
			protected void onPostExecute(String errorMessage) {
				
				if (errorMessage == null) {
					if (homePaging.getPage() == INIT_PAGE && homeAdapter == null) {
						homeAdapter = new StatusAdapter(MainActivity.this, homeStatuses);
						lvHomeTimeline.setAdapter(homeAdapter);
					} else {
						homeAdapter.notifyDataSetChanged();
					}
					
				} else {
					if (homePaging.getPage() > INIT_PAGE) {						
						hideLoadingProgress();
					}
					showAlertMessage(errorMessage);
				}
				hideProgressDialog();
				stopSwipeRefreshing();
				
				isLoading = false;
			};
			
		}.execute();
    	    	
    }
    
    private void loadMentionsTimeline() {
    	showLoadingProgress();
    	
    	isLoading = true;
    	
    	new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				String errorMessage = null;
				try {
					if (mentionsStatuses.size() == 0) {
						mentionsStatuses.addAll(twitter.getMentionsTimeline(mentionsPaging));
					} else {
						appendMentionsStatuses(twitter.getMentionsTimeline(mentionsPaging));
					}
					
					if (mentionsStatuses == null) {
						throw new TwitterException(getString(R.string.get_mentions_timeline_error));
					}
				} catch (TwitterException e) {
					errorMessage = e.getMessage();
				}
				
				return errorMessage;
			}
			
			protected void onPostExecute(String errorMessage) {
				
				if (errorMessage == null) {					
					mentionsAdapter.notifyDataSetChanged();
					
				} else {
					if (mentionsPaging.getPage() > INIT_PAGE) {						
						hideLoadingProgress();
					}
					showAlertMessage(errorMessage);
				}
				hideProgressDialog();
				stopSwipeRefreshing();
				
				isLoading = false;
			};
			
		}.execute();
    	    	
    }
    
    private void loadUserTimeline() {
    	showLoadingProgress();
    	
    	isLoading = true;
    	
    	new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				String errorMessage = null;
				try {					
					if (userStatuses.size() == 0) {
						userStatuses.addAll(twitter.getUserTimeline(userPaging));
					} else {
						appendUserStatuses(twitter.getUserTimeline(userPaging));
					}
					if (userStatuses == null) {
						throw new TwitterException(getString(R.string.get_user_timeline_error));
					}
				} catch (TwitterException e) {
					errorMessage = e.getMessage();
				}
				
				return errorMessage;
			}
			
			protected void onPostExecute(String errorMessage) {
				
				if (errorMessage == null) {
					userAdapter.notifyDataSetChanged();
				} else {
					if (userPaging.getPage() > INIT_PAGE) {						
						hideLoadingProgress();
					}
					showAlertMessage(errorMessage);
				}
				hideProgressDialog();
				stopSwipeRefreshing();
				
				isLoading = false;
			};
			
		}.execute();
    	    	
    }
    
    private void addNewTweet(String content) {
    	new AsyncTask<String, Void, String>() {

			@Override
			protected String doInBackground(String... argv) {
				String errorMessage = null;
				String content = argv[0];
				try {
					twitter.updateStatus(content);
				} catch (TwitterException e) {
					errorMessage = e.getMessage();
				}
				
				return errorMessage;
			}
			
			protected void onPostExecute(String errorMessage) {
				if (errorMessage != null) {
					showAlertMessage(errorMessage);
				} else {
					//Log.i("addNewTweet", "resetStatuses");
					progressDialog.show();
					switch (currentTimelineType) {
					case HOME_TIMELINE_TYPE:
						resetHomeStatuses();
						lvHomeTimeline.setSelection(0);
						loadHomeTimeline();
						break;
					case MENTIONS_TIMELINE_TYPE:
						resetMentionsStatuses();
						lvMentionsTimeline.setSelection(0);
						loadMentionsTimeline();
						break;
					case USER_TIMELINE_TYPE:
						resetUserStatuses();
						lvUserTimeline.setSelection(0);
						loadUserTimeline();
						break;
					default:
						break;
					}
				}
			};
    		
    	}.execute(content);
    }
    
    private void resetHomeStatuses() {
    	//adapter.clear();
    	homeStatuses.clear();
    	homePaging.setPage(INIT_PAGE);
    }
    
	private void resetMentionsStatuses() {
    	mentionsStatuses.clear();
    	mentionsPaging.setPage(INIT_PAGE);
	}
	
	private void resetUserStatuses() {
    	userStatuses.clear();
    	userPaging.setPage(INIT_PAGE);
	}
    
    private void hideProgressDialog() {
		if (progressDialog.isShowing()) {					
			progressDialog.hide();
		}
    }

	private void showLoadingProgress() {
		Status nullStatus = null;
		switch (currentTimelineType) {
		case HOME_TIMELINE_TYPE:
			homeStatuses.add(nullStatus);
			homeAdapter.notifyDataSetChanged();			
			break;
		case MENTIONS_TIMELINE_TYPE:
			mentionsStatuses.add(nullStatus);
			mentionsAdapter.notifyDataSetChanged();	
			break;
		case USER_TIMELINE_TYPE:
			userStatuses.add(nullStatus);
			userAdapter.notifyDataSetChanged();	
			break;
		default:
			break;
		}
	}

	private void removeNullStatus() {
		switch (currentTimelineType) {
		case HOME_TIMELINE_TYPE:			
			if (homeStatuses != null) {
				int nullStatusIndex = homeStatuses.size() - 1;
				homeStatuses.remove(nullStatusIndex);			
			}
			break;
		case MENTIONS_TIMELINE_TYPE:
			if (mentionsStatuses != null) {
				int nullStatusIndex = mentionsStatuses.size() - 1;
				mentionsStatuses.remove(nullStatusIndex);			
			}
			break;
		case USER_TIMELINE_TYPE:
			if (userStatuses != null) {
				int nullStatusIndex = userStatuses.size() - 1;
				userStatuses.remove(nullStatusIndex);			
			}
			break;
		default:
			break;
		}
	}
	
	private void hideLoadingProgress() {
		removeNullStatus();
		switch (currentTimelineType) {
		case HOME_TIMELINE_TYPE:			
			homeAdapter.notifyDataSetChanged();
			break;
		case MENTIONS_TIMELINE_TYPE:
			mentionsAdapter.notifyDataSetChanged();
			break;
		case USER_TIMELINE_TYPE:
			userAdapter.notifyDataSetChanged();
			break;
		default:
			break;
		}
		
	}
    
    private void appendHomeStatuses(ResponseList<Status> lists) {
    	removeNullStatus();
    	for (Status status : lists) {
			homeStatuses.add(status);
		}
    }
    
    private void appendMentionsStatuses(ResponseList<Status> lists) {
    	removeNullStatus();
    	for (Status status : lists) {
			mentionsStatuses.add(status);
		}
    }
    
    private void appendUserStatuses(ResponseList<Status> lists) {
    	removeNullStatus();
    	for (Status status : lists) {
			userStatuses.add(status);
		}
    }
    
    public void showAlertMessage(String message) {
    	alertDialog.setMessage(message);
    	alertDialog.show();
    }
    
	@Override
	public void onUserReadyListener(String errorMessage) {
		if (errorMessage != null) {
			showAlertMessage(errorMessage);
			return;
		}
		
		user = twitterHelper.getUser();
		loadHomeTimeline();
	}

	@Override
	public void onTokenSavedListener(String errorMessage) {
		if (errorMessage != null) {
			showAlertMessage(errorMessage);
			return;
		}
		
		try {
			prepareTwitter();
		} catch (TwitterException e) {
			showAlertMessage(e.getMessage());
		}
	}

	@Override
	public void onLoginStartedListener(String errorMessage) {
		if (progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		
		if (errorMessage != null) {
			showAlertMessage(errorMessage);
			return;
		}
		
		finish();
	}
	
	private class CustomScrollListener implements OnScrollListener {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			
			int viewId = view.getId();

			if (isStatusesNull(viewId) || getStatusesSize(viewId) == 0
					|| totalItemCount == 0
					|| (visibleItemCount == totalItemCount)
					|| totalItemCount < NUMBER_OF_TWEETS_PER_PAGE || isLoading) {
				return;
			} 
			
			ListView lv = (ListView) view;
			if (viewId == R.id.lvHomeTimeline) {
				posOfHome = firstVisibleItem;
				topOfHome = lv.getChildAt(0).getTop();
			} else if (viewId == R.id.lvMentionsTimeline) {
				posOfMentions = firstVisibleItem;
				topOfMentions = lv.getChildAt(0).getTop();
			}
			
			if ((firstVisibleItem + visibleItemCount) == totalItemCount) {
				if (viewId == R.id.lvHomeTimeline) {
					homePaging.setPage(homePaging.getPage() + 1);
					loadHomeTimeline();					
				} else if(viewId == R.id.lvMentionsTimeline) {
					mentionsPaging.setPage(mentionsPaging.getPage() + 1);
					loadMentionsTimeline();	
				}
			}
		}
		
		private boolean isStatusesNull(int viewId) {
			boolean result = true;
			switch (viewId) {
			case R.id.lvHomeTimeline:
				if (homeStatuses != null) {
					result = false;
				}
				break;
			case R.id.lvMentionsTimeline:
				if (mentionsStatuses != null) {
					result = false;
				}
				break;
			default:
				break;
			}
			
			return result;
		}
		
		private int getStatusesSize(int viewId) {
			int size = 0;
			switch (viewId) {
			case R.id.lvHomeTimeline:
				size = homeStatuses.size();
				break;
			case R.id.lvMentionsTimeline:
				size = mentionsStatuses.size();
				break;
			default:
				break;
			}
			
			return size;
		}
		
	}

	@Override
	public void onHomeViewReady() {
		currentTimelineType = HOME_TIMELINE_TYPE;
		initHomeSwipeRefresh();
    	lvHomeTimeline = (ListView) findViewById(R.id.lvHomeTimeline);
    	//lvHomeTimeline.setOnItemSelectedListener(new CustomHomeStatusSelectedListener());
    	lvHomeTimeline.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
			}
		});
    	lvHomeTimeline.setOnScrollListener(new CustomScrollListener());
    	
    	if (homeStatuses == null) {
            try {
    			process();
    		} catch (TwitterException e) {
    			showAlertMessage(e.getMessage());
    		}
		} else {
			lvHomeTimeline.setAdapter(homeAdapter);
			lvHomeTimeline.setSelectionFromTop(posOfHome, topOfHome);
		}
		
	}

	@Override
	public void onMentionsViewReady() {
		currentTimelineType = MENTIONS_TIMELINE_TYPE;
		initMentionsSwipeRefresh();
		lvMentionsTimeline = (ListView) findViewById(R.id.lvMentionsTimeline);
		lvMentionsTimeline.setOnScrollListener(new CustomScrollListener());
		
		if (mentionsStatuses.size() == 0) {
			mentionsAdapter = new StatusAdapter(MainActivity.this, mentionsStatuses);
			lvMentionsTimeline.setAdapter(mentionsAdapter);
			//Log.i("onMentionsViewReady", "mentionsStatuses is null");
			loadMentionsTimeline();
		} else {
			lvMentionsTimeline.setAdapter(mentionsAdapter);
			lvMentionsTimeline.setSelectionFromTop(posOfMentions, topOfMentions);
		}
	}

	@Override
	public void onProfileViewReady() {
		currentTimelineType = USER_TIMELINE_TYPE;
		initUserSwipeRefresh();
		lvUserTimeline = (ListView) findViewById(R.id.lvUserTimeline);

		//lvUserTimeline.setOnScrollListener(new CustomScrollListener());
		if (userStatuses.size() == 0) {
			userAdapter = new StatusAdapter(MainActivity.this, userStatuses);
			lvUserTimeline.setAdapter(userAdapter);
			loadUserTimeline();
		} else {
			lvUserTimeline.setAdapter(userAdapter);
			lvUserTimeline.setSelectionFromTop(posOfUser, topOfUser);
		}
	}

	private class CustomHomeStatusSelectedListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			Intent intent = new Intent(MainActivity.this, StatusDetailActivity.class);
			startActivity(intent);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			
		}
		
	}

	@Override
	public void onReplyCompleted() {
		resetHomeStatuses();
		loadHomeTimeline();
	}

    
}
