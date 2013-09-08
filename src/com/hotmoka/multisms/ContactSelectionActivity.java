package com.hotmoka.multisms;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import com.hotmoka.asimov.app.AsimovCallableActivity;
import com.hotmoka.asimov.app.State;
import com.hotmoka.multisms.R;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.ImageView;

public class ContactSelectionActivity extends AsimovCallableActivity<SortedSet<Contact>> {

	@State
	private final Set<Contact> selectedContacts = new HashSet<Contact>();

	@Override
	protected void onCreate(SortedSet<Contact> contacts) {
		setContentView(R.layout.activity_contact_selection);

		setAdaptor(contacts);
	}

	private void setAdaptor(SortedSet<Contact> contacts) {
		ArrayAdapter<Contact> adapter = new ArrayAdapter<Contact>(this, R.layout.single_contact, contacts.toArray(new Contact[contacts.size()])) {

			private final Button sendButton = (Button) findViewById(R.id.send);

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

						if (selectedContacts.isEmpty()) {
							sendButton.setEnabled(false);
							sendButton.setText("Send to the selected contacts");
						}
						else if (selectedContacts.size() == 1) {
							sendButton.setEnabled(true);
							sendButton.setText("Send to the selected contact");
						}
						else {
							sendButton.setEnabled(true);
							sendButton.setText("Send to the selected " + selectedContacts.size() + " contacts");
						}
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
}