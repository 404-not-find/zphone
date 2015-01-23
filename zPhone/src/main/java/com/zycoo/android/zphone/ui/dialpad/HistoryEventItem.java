
package com.zycoo.android.zphone.ui.dialpad;

import org.doubango.ngn.model.NgnHistoryEvent;

public class HistoryEventItem implements ContactItemInterface {

    private NgnHistoryEvent event;

    public HistoryEventItem(NgnHistoryEvent event)
    {
        this.event = event;
    }

    public NgnHistoryEvent getEvent() {
        return event;
    }

    public void setEvent(NgnHistoryEvent event) {
        this.event = event;
    }

    @Override
    public String getItemForIndex() {

        return event.getDisplayName();
    }

}
