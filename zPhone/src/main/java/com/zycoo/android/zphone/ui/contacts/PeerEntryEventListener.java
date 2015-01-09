
package com.zycoo.android.zphone.ui.contacts;

import android.util.Log;

import com.zycoo.android.zphone.ContactsPBXBean;
import com.zycoo.android.zphone.ContactsPBXBean.ContactsPBXType;
import com.zycoo.android.zphone.ContactsPBXBean.ContatcsPBXStatus;

import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.action.IaxPeerListAction;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.event.PeerEntryEvent;
import org.asteriskjava.manager.event.PeerlistCompleteEvent;

import java.util.ArrayList;
import java.util.Collection;

public class PeerEntryEventListener implements ManagerEventListener
{
    private static final String LOG_TAG = PeerEntryEventListener.class.getCanonicalName();
    private boolean mComplete = false;

    private Collection<ContactsPBXBean> mCollection;

    public PeerEntryEventListener()
    {
        mCollection = new ArrayList<ContactsPBXBean>();
    }

    @Override
    public void onManagerEvent(ManagerEvent event)
    {
        if (event instanceof PeerEntryEvent)
        {
            PeerEntryEvent peerEntryEvent = (PeerEntryEvent) event;
            ContactsPBXBean pbxBean = new ContactsPBXBean();
            pbxBean.setName(peerEntryEvent.getObjectUserName());
            pbxBean.setNumber(peerEntryEvent.getObjectName());
            pbxBean.setDate(peerEntryEvent.getDateReceived());
            String type = null == peerEntryEvent.getChannelType() ? "UNKNOW" : peerEntryEvent
                    .getChannelType();
            switch (type) {
                case "IAX":
                    pbxBean.setType(ContactsPBXType.IAX);
                    break;
                case "SIP":
                    pbxBean.setType(ContactsPBXType.SIP);
                    break;
                default:
                    pbxBean.setType(ContactsPBXType.UNKONW);
                    break;
            }
            String status = null == peerEntryEvent.getStatus() ? "UNKNOW" : peerEntryEvent
                    .getStatus();
            if (status.contains("OK"))
            {
                pbxBean.setStatus(ContatcsPBXStatus.OK);
            }
            else
            {
                pbxBean.setStatus(ContatcsPBXStatus.UNKONW);
            }
            mCollection.add(pbxBean);
        }
        if (event instanceof PeerlistCompleteEvent)
        {
            Log.d(LOG_TAG, " receive PeerlistCompleteEvent ");
            mComplete = true;
        }
    }

    public Collection<ContactsPBXBean> getCollectionResult()
    {
        return mCollection;
    }

    public boolean isComplete()
    {
        return mComplete;
    }

    public void setComplete(boolean complete) {
        mComplete = complete;
    }
}
