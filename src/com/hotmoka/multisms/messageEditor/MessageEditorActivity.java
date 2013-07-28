package com.hotmoka.multisms.messageEditor;

import com.hotmoka.multisms.R;
import com.hotmoka.multisms.contactSelection.ContactSelectionActivity;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MessageEditorActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_message_editor);

		configureSendButton();
	}

	private void configureSendButton() {
		((Button) findViewById(R.id.sendMessage)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(MessageEditorActivity.this, ContactSelectionActivity.class));
			}
		});
	}
}