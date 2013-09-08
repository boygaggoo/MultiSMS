package com.hotmoka.multisms;

import android.os.Parcel;
import android.os.Parcelable;

public class Contact implements Comparable<Contact>, Parcelable {
	final boolean isMobile;
	final String name;
	final String surname;
	final String phone;

	public Contact(boolean isMobile, String name, String surname, String phone) {
		this.isMobile = isMobile;
		this.name = name;
		this.surname = surname;
		this.phone = phone;
	}

	private Contact(Parcel parcel) {
		this.isMobile = parcel.readInt() == 1 ? true : false;
		this.name = parcel.readString();
		this.surname = parcel.readString();
		this.phone = parcel.readString();
	}

	@Override
	public int compareTo(Contact another) {
		int comp = name.compareTo(another.name);
		if (comp != 0)
			return comp;

		comp = surname.compareTo(another.surname);
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

			return isMobile == otherAsContact.isMobile && name.equals(otherAsContact.name)
				&& surname.equals(otherAsContact.surname) && phone.equals(otherAsContact.phone);
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
		return "[" + (isMobile ? "Mobile" : "Landline") + "] " + name + " " + surname + " " + phone; 
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeInt(isMobile ? 1 : 0);
		parcel.writeString(name);
		parcel.writeString(surname);
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