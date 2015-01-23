package com.zycoo.android.zphone;

import org.doubango.ngn.events.NgnRegistrationEventTypes;

public interface UpdateOnlineStatus {
    public void  statusChange(NgnRegistrationEventTypes type);
}
