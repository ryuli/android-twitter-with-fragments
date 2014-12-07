package org.example.twitterappclient;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class ReplyDialog extends DialogFragment {
	
	private Context context;
	private Status status;
	private User user;
	private String replyScreenName;
	private static final int MAX_TWEET_LENGTH = 140;
	private EditText etReplyContent;
	private TextView tvRemainCount;
	private OnReplyCompletedListener mListener;
	
	public interface OnReplyCompletedListener {
		public void onReplyCompleted();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof MainActivity) {
			mListener = (OnReplyCompletedListener) activity;
		}
	}
	
	public ReplyDialog(Context context, User user, Status status, String replyScreenName) {
		this.context = context;
		this.status = status;
		this.user = user;
		this.replyScreenName = replyScreenName;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		Dialog dialog = getDialog();
		dialog.setTitle(R.string.reply);
		
		View view = inflater.inflate(R.layout.fragment_reply, container, false);
		
		tvRemainCount = (TextView) view.findViewById(R.id.tvRemainCount);
		ImageView ivProfileImage = (ImageView) view.findViewById(R.id.ivProfileImage);
		etReplyContent = (EditText) view.findViewById(R.id.etReplyContent);
		Button btnReply = (Button) view.findViewById(R.id.btnReply);
		btnReply.setOnClickListener(new TweetOnClickListener());
		
		Picasso.with(context).load(user.getBiggerProfileImageURL()).placeholder(R.drawable.human_gray).into(ivProfileImage);
		etReplyContent.addTextChangedListener(new ReplyContentTextWatcher());
		etReplyContent.setText("@" + replyScreenName + " ");
		int cursorPos = etReplyContent.length();
		etReplyContent.setSelection(cursorPos);
		
		return view;
	}
	
	private class ReplyContentTextWatcher implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			int remainCount = MAX_TWEET_LENGTH - etReplyContent.length();
			tvRemainCount.setText(String.valueOf(remainCount));
		}

		@Override
		public void afterTextChanged(Editable s) {
			
		}
		
	}
	
	private class TweetOnClickListener implements OnClickListener {
		
		private MainActivity activity;
		private Twitter twitter;

		@Override
		public void onClick(View v) {
			String content = etReplyContent.getText().toString();
			if (content.trim().equals("@" + user.getScreenName())) {
				return;
			}
			
			activity = (MainActivity) context;
			activity.progressDialog.show();
			twitter = activity.twitter;
			reply();
			dismiss();
		}
		
		private void reply() {
			
			new AsyncTask<Void, Void, String>() {

				@Override
				protected String doInBackground(Void... params) {
					String errorMessage = null;
					StatusUpdate statusUpdate = new StatusUpdate(etReplyContent.getText().toString());
					long replyToStatusId = status.getInReplyToStatusId();
					statusUpdate.setInReplyToStatusId(replyToStatusId);
					
					try {
						twitter.updateStatus(statusUpdate);
					} catch (TwitterException e) {
						errorMessage = e.getMessage();
					}
					
					return errorMessage;
				}
				
				protected void onPostExecute(String errorMessage) {
					activity.progressDialog.hide();
					if (errorMessage != null) {
						activity.showAlertMessage(errorMessage);
					} else {
						Toast.makeText(context, context.getString(R.string.reply_completed), Toast.LENGTH_SHORT).show();
						mListener.onReplyCompleted();
					}
				};
				
				
			}.execute();
		}
	}
}
