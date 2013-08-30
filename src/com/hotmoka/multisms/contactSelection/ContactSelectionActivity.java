package com.hotmoka.multisms.contactSelection;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import com.hotmoka.asimov.app.AsimovActivity;
import com.hotmoka.asimov.app.State;
import com.hotmoka.multisms.R;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.ImageView;

public class ContactSelectionActivity extends AsimovActivity {

	/**
	 * The token holding the contacts in the intent that started this activity.
	 */

	public final static String CONTACTS = "contacts";

	@State
	private final Set<Contact> selectedContacts = new HashSet<Contact>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_contact_selection);

		setAdaptor(getContactsFromIntent());
	}

	private Contact[] getContactsFromIntent() {
		Set<Contact> contacts = new TreeSet<Contact>();
		Parcelable[] extra = getIntent().getParcelableArrayExtra(CONTACTS);
		if (extra != null)
			for (Parcelable p: extra)
				if (p instanceof Contact)
					contacts.add((Contact) p);

		return contacts.toArray(new Contact[contacts.size()]);
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

	public static class Contact implements Comparable<Contact>, Parcelable {
		private final boolean isMobile;
		private final String name;
		private final String phone;
	
		public Contact(boolean isMobile, String name, String phone) {
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
}