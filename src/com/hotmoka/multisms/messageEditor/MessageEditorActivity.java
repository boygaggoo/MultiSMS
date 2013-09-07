package com.hotmoka.multisms.messageEditor;

import java.util.Set;
import java.util.TreeSet;

import com.hotmoka.asimov.app.DetachableHandler;
import com.hotmoka.asimov.app.AsimovActivity;
import com.hotmoka.multisms.R;
import com.hotmoka.multisms.contactSelection.ContactSelectionActivity;
import com.hotmoka.multisms.contactSelection.ContactSelectionActivity.Contact;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class MessageEditorActivity extends AsimovActivity {

	private final static String NAME = "$NAME$";
	private final static String SURNAME = "$SURNAME$";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		configureUI();
	}

	private void configureUI() {
		setContentView(R.layout.activity_message_editor);

		configureSendButton();
		configureAddNameButton();
		configureAddSurnameButton();
	}

	private void configureSendButton() {
		findViewById(R.id.sendMessage).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				submit(new ContactsFetcher(MessageEditorActivity.this));
			}
		});
	}

	private void configureAddNameButton() {
		findViewById(R.id.addName).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				expandTextWith(NAME);
			}
		});
	}

	private void configureAddSurnameButton() {
		findViewById(R.id.addSurname).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				expandTextWith(SURNAME);
			}
		});
	}

	private void expandTextWith(String added) {
		EditText editText = (EditText) findViewById(R.id.message);
		String originalMessage = editText.getText().toString();
		String newMessage;

		if (originalMessage.length() == 0 || originalMessage.endsWith(" "))
			newMessage = originalMessage + added + ' ';
		else
			newMessage = originalMessage + " " + added + ' ';

		editText.setText(newMessage);
		editText.setSelection(newMessage.length());
	}

	private static class ContactsFetcher extends DetachableHandler<MessageEditorActivity, Integer, Contact[]> {

		private final ContentResolver contentResolver;

		/**
		 * The progress bar used to show the progress of the computation.
		 */

		private ProgressDialog progressBar;
		private Cursor contactsCursor;
		private int progress;
		private boolean cancelled;

		private ContactsFetcher(MessageEditorActivity context) {
			this.contentResolver = context.getContentResolver();
		}

		@Override
		protected void onAttach(MessageEditorActivity context) {
			super.onAttach(context);

			progressBar = new ProgressDialog(context);
			progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressBar.setIndeterminate(false);
			progressBar.setMessage("Loading contacts");
			progressBar.setProgress(progress);
			if (contactsCursor != null)
				progressBar.setMax(contactsCursor.getCount());
			//progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_bar));
			progressBar.setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface bar) {
					// if the user cancels the progress bar (for instance through the back key)
					// the task gets cancelled as well
					cancelled = true;
				}
			});

			progressBar.show();
		}

		@Override
		protected void onDetach() {
			progressBar.dismiss();
		}

		@Override
		protected Contact[] run() {
			contactsCursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
			progressBar.setMax(contactsCursor.getCount());
			Set<Contact> contacts = new TreeSet<Contact>();

	        while (contactsCursor.moveToNext() && !cancelled) {
	        	notifyProgress(Integer.valueOf(++progress));
	        	addContactsFor(contactsCursor, contacts);
	        }

	        contactsCursor.close();

	        return contacts.toArray(new Contact[contacts.size()]);
		}

		@Override
		protected void onProgressUpdate(MessageEditorActivity context, Integer progress) {
			progressBar.setProgress(progress);
		}

		@Override
		protected void onPostExecute(MessageEditorActivity context, Contact[] result) {
			progressBar.dismiss();
			if (!cancelled)
				context.call(ContactSelectionActivity.class, result);
		}

		private void addContactsFor(Cursor contactsCursor, Set<Contact> contacts) {
			String contactName = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
	        String contactID = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts._ID));

	    	// iteriamo su tutti i numeri del contatto
	    	Cursor numbers = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
	       		ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { contactID }, null);

	    	if (numbers != null)
	            while (numbers.moveToNext()) {
	            	boolean isMobile = numbers.getInt(numbers.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)) == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
	            	String contactPhone = numbers.getString(numbers.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
	            	contacts.add(new Contact(isMobile, contactName, contactPhone));
	            }

	    	numbers.close();
		}
	}
}