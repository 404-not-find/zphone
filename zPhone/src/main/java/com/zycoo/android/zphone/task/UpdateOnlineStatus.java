package com.zycoo.android.zphone.task;

import org.doubango.ngn.events.NgnRegistrationEventTypes;

public interface UpdateOnlineStatus {
    public void  statusChange(NgnRegistrationEventTypes type);
}
