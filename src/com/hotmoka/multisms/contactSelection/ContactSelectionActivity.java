package com.hotmoka.multisms.contactSelection;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import com.hotmoka.asimov.tasks.DetachableAsyncTask;
import com.hotmoka.asimov.tasks.TaskLauncherActivity;
import com.hotmoka.multisms.R;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;

public class ContactSelectionActivity extends TaskLauncherActivity {

	/* saved state */
	private final Set<Contact> selectedContacts = new HashSet<Contact>();

	private final static Contact[] noContacts = new Contact[0];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_contact_selection);

		recoverSavedState(savedInstanceState);

		submit(new MyTask(this));
	}

	private void setAdaptor(Contact[] contacts) {
		ArrayAdapter<Contact> adapter = new ArrayAdapter<Contact>(this, R.layout.single_contact, contacts) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				convertView = ensureThatConvertViewExists(convertView);
				initViewFromContact(convertView, getItem(position));

				return convertView;
			}

			private View ensureThatConvertViewExists(View convertView) {
				return convertView == null ? getLayoutInflater().inflate(R.layout.single_contact, null) : convertView;
			}

			private void initViewFromContact(View view, final Contact contact) {
				TextView contactText = (TextView) view.findViewById(R.id.contact_name);
				contactText.setText(contact.name);
				TextView contactPhone = (TextView) view.findViewById(R.id.contact_phone);
				contactPhone.setText(contact.phone);
				ImageView phoneImage = (ImageView) view.findViewById(R.id.phone_type);
				phoneImage.setImageResource(contact.isMobile ? R.drawable.smart_phone : R.drawable.phone);

				final CheckBox checkBox = (CheckBox) view.findViewById(R.id.contact_selector);
				checkBox.setChecked(selectedContacts.contains(contact));

				OnClickListener listener = new OnClickListener() {

					@Override
					public void onClick(View view) {
						if (view != checkBox)
							checkBox.setChecked(!checkBox.isChecked());

						if (checkBox.isChecked())
							selectedContacts.add(contact);
						else
							selectedContacts.remove(contact);

						((Button) findViewById(R.id.send)).setEnabled(!selectedContacts.isEmpty());
					}
					
				};

				contactText.setOnClickListener(listener);
				contactPhone.setOnClickListener(listener);
				phoneImage.setOnClickListener(listener);
				checkBox.setOnClickListener(listener);
			}
		};

		((ListView) findViewById(R.id.list_of_contacts)).setAdapter(adapter);
	}

	@Override
	protected void onSaveInstanceState(Bundle instanceState) {
		super.onSaveInstanceState(instanceState);

		instanceState.putParcelableArray("selected", selectedContacts.toArray(noContacts));
	}

	private void recoverSavedState(Bundle savedInstanceState) {
		if (savedInstanceState != null)
			for (Contact contact: (Contact[]) savedInstanceState.getParcelableArray("selected"))
				selectedContacts.add(contact);
	}

	public static class Contact implements Comparable<Contact>, Parcelable {
		private final boolean isMobile;
		private final String name;
		private final String phone;
	
		private Contact(boolean isMobile, String name, String phone) {
			this.isMobile = isMobile;
			this.name = name;
			this.phone = phone;
		}

		private Contact(Parcel parcel) {
			this.isMobile = parcel.readInt() == 1 ? true : false;
			this.name = parcel.readString();
			this.phone = parcel.readString();
		}

		@Override
		public int compareTo(Contact another) {
			int comp = name.compareTo(another.name);
			if (comp != 0)
				return comp;
	
			if (isMobile != another.isMobile)
				return isMobile ? -1 : 1;
	
			return phone.compareTo(another.phone);
		}
	
		@Override
		public boolean equals(Object other) {
			if (other instanceof Contact) {
				Contact otherAsContact = (Contact) other;
	
				return isMobile == otherAsContact.isMobile && name.equals(otherAsContact.name) && phone.equals(otherAsContact.phone);
			}
			else
				return false;
		}
	
		@Override
		public int hashCode() {
			return name.hashCode();
		}

		@Override
		public String toString() {
			return "[" + (isMobile ? "Mobile" : "Landline") + "] " + name + " " + phone; 
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel parcel, int flags) {
			parcel.writeInt(isMobile ? 1 : 0);
			parcel.writeString(name);
			parcel.writeString(phone);
		}

		public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {

			@Override
			public Contact createFromParcel(Parcel parcel) {
				return new Contact(parcel);
			}

			@Override
			public Contact[] newArray(int size) {
				return new Contact[size];
			}
		};
	}

	private static class MyTask extends DetachableAsyncTask<ContactSelectionActivity, Void, Integer, Contact[]> {

		private final ContentResolver contentResolver;

		/**
		 * The progress bar used to show the progress of the computation.
		 */

		private ProgressDialog progressBar;
		private Cursor contactsCursor;
		private int progress;

		private MyTask(ContactSelectionActivity context) {
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_FOREGROUND);
			//+ android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
			this.contentResolver = context.getContentResolver();
		}

		@Override
		protected void onAttach(ContactSelectionActivity context) {
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
					cancel(true);
				}
			});

			progressBar.show();
		}

		@Override
		protected void onDetach() {
			super.onDetach();

			progressBar.dismiss();
		}

		@Override
		protected Contact[] doInBackground(Void... arg0) {
			contactsCursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
			progressBar.setMax(contactsCursor.getCount());
			Set<Contact> contacts = new TreeSet<Contact>();

	        while (contactsCursor.moveToNext()) {
	        	publishProgress(++progress);
	        	addContactsFor(contactsCursor, contacts);
	        }

	        contactsCursor.close();

	        return contacts.toArray(noContacts);
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

		@Override
		protected void onProgressUpdate(ContactSelectionActivity context, Integer... progress) {
			progressBar.setProgress(progress[0]);
		}

		@Override
		protected void onPostExecute(ContactSelectionActivity context, Contact[] result) {
			progressBar.dismiss();
			context.setAdaptor(result);
		}

		@Override
		protected void onCancelled(ContactSelectionActivity context, Contact[] result) {
			if (contactsCursor != null)
				contactsCursor.close();
			progressBar.dismiss();
			context.setAdaptor(result);
		}

		@Override
		protected void onCancelled(ContactSelectionActivity context) {
			if (contactsCursor != null)
				contactsCursor.close();
			progressBar.dismiss();
		}
	}
}