
package com.zycoo.android.zphone.ui.dialpad;

import org.doubango.ngn.model.NgnContact;

public class ContactItem implements ContactItemInterface {

    private NgnContact contact;

    public ContactItem(NgnContact contact)
    {
        this.contact = contact;
    }

    public NgnContact getContact()
    {
        return contact;
    }

    public void setContact(NgnContact contact)
    {
        this.contact = contact;
    }

    @Override
    public String getItemForIndex() {

        return contact.getDisplayName();
    }

}
