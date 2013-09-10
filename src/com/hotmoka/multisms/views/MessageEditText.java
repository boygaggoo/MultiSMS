package com.hotmoka.multisms.views;

import com.hotmoka.multisms.Contact;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * An edit text that allows one to insert place-holders for name and surname
 * of a contact and to replace them with a contact.
 * 
 * @author Fausto Spoto <fausto.spoto@hotmoka.com>
 */

public class MessageEditText extends EditText {

	private final static String NAME = "$NAME$";
	private final static String SURNAME = "$SURNAME$";

	public MessageEditText(Context context) {
		super(context);
	}

	public MessageEditText(Context context, AttributeSet attributes) {
		super(context, attributes);
	}

	public MessageEditText(Context context, AttributeSet attributes, int defStyle) {
		super(context, attributes, defStyle);
	}

	public void addName() {
		expandTextWith(NAME);
	}

	public void addSurname() {
		expandTextWith(SURNAME);
	}

	public Message getMessage() {
		return new Message(getText().toString());
	}

	public final static class Message implements Parcelable {
		private final String message;

		private Message(String message) {
			this.message = message;
		}

		private Message(Parcel in) {
			this.message = in.readString();
		}

		public String personalizeFor(Contact contact) {
			return message.replace(NAME, contact.name).replace(SURNAME, contact.surname);
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			out.writeString(message);
		}

		public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {

			@Override
	    	public Message createFromParcel(Parcel in) {
	            return new Message(in);
	        }

	    	@Override
	        public Message[] newArray(int size) {
	            return new Message[size];
	        }
		};
	};

	private void expandTextWith(String added) {
		String originalMessage = getText().toString();
		String newMessage;

		if (originalMessage.length() == 0 || originalMessage.endsWith(" "))
			newMessage = originalMessage + added + ' ';
		else
			newMessage = originalMessage + " " + added + ' ';

		setText(newMessage);

		// we move the cursor at the end of the new message
		setSelection(newMessage.length());
	}
}