package com.hotmoka.multisms;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import com.hotmoka.asimov.app.AsimovCallableActivity;
import com.hotmoka.asimov.app.State;
import com.hotmoka.multisms.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
		configureSendButton();
	}

	private void configureSendButton() {
		findViewById(R.id.send).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View button) {
				new AlertDialog.Builder(ContactSelectionActivity.this)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.confirmation)
					.setMessage(mkConfirmationMessage())
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			            @Override
			            public void onClick(DialogInterface dialog, int which) {
			            }
					})
					.setNegativeButton(R.string.cancel, null)
					.show();
			}

			private CharSequence mkConfirmationMessage() {
				Contact firstRecipient = getFirstRecipient();

				String message = "You are going to send " + selectedContacts.size() + " personalized SMS.";
				message += " For instance, the following message:\n\n";
				message += "\"" + personalizeMessageFor(firstRecipient) + "\"";
				message += "\n\nis going to be sent to " + firstRecipient.name + " " + firstRecipient.surname;

				return message;
			}

			private Contact getFirstRecipient() {
				Contact result = null;
				for (Contact contact: selectedContacts)
					if (result == null || contact.compareTo(result) < 0)
						result = contact;

				return result;
			}

			private String personalizeMessageFor(Contact recipient) {
				return "message";
			}
		});
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
				contactText.setText(contact.name + " " + contact.surname);
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