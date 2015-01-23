
package com.zycoo.android.zphone.ui.dialpad;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.text.method.DialerKeyListener;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.zycoo.android.zphone.Engine;
import com.zycoo.android.zphone.ui.MainActivity;
import com.zycoo.android.zphone.R;
import com.zycoo.android.zphone.UpdateOnlineStatus;
import com.zycoo.android.zphone.ZphoneApplication;
import com.zycoo.android.zphone.ui.dialpad.DialerCallBar.OnDialActionListener;
import com.zycoo.android.zphone.ui.dialpad.DialerLayout.OnAutoCompleteListVisibilityChangedListener;
import com.zycoo.android.zphone.ui.dialpad.Dialpad.OnDialKeyListener;
import com.zycoo.android.zphone.utils.DialingFeedback;
import com.zycoo.android.zphone.widget.DialDialpadButton;
import com.zycoo.android.zphone.widget.SuperAwesomeCardFragment;

import org.doubango.ngn.events.NgnRegistrationEventTypes;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.model.NgnHistoryAVCallEvent.HistoryEventAVFilter;
import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.services.INgnHistoryService;
import org.doubango.ngn.utils.NgnUriUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class DialerFragment extends SuperAwesomeCardFragment implements OnClickListener,
        OnLongClickListener,
        OnDialKeyListener, TextWatcher, OnDialActionListener, OnKeyListener,
        OnAutoCompleteListVisibilityChangedListener, Observer, OnItemClickListener,
        UpdateOnlineStatus {
    private static final int UNIQUE_FRAGMENT_GROUP_ID = 3;
    public static final int REQUEST_CODE_EDIT_CONTACTS = 0;
    protected static final int PICKUP_PHONE = 0;
    private final static String TEXT_MODE_KEY = "text_mode";
    private List<ContactItemInterface> historyEvents;
    private DigitsEditText digits;
    private Spinner mSpinner;
    private String initText = null;
    private Boolean isDigit = false;
    private Handler handler;
    private HistoryEventItem historyEventItem;
    private SimpleAdapter mSimpleAdapter;
    private List<Map<String, Object>> mAccountMap;
    // 拨号按键反馈
    private DialingFeedback dialFeedback;
    // 拨号盘1和0
    private final int[] buttonsToLongAttach = new int[]{
            R.id.button0, R.id.button1
    };
    // 拨号盘
    private Dialpad dialPad;
    private AlertDialog missingVoicemailDialog;

    // Auto completion for text mode
    private ListView autoCompleteList;
    protected ArrayAdapter<ContactItemInterface> itemAdapter;

    // 拨号盘下方的语音拨号和视频拨号
    private DialerCallBar callBar;
    private ImageButton dialText;
    // 标示双拨号盘
    private boolean mDualPane;
    // TODO 自动补全拨号
    // private DialerAutocompleteDetailsFragment autoCompleteFragment;
    // TODO 监听Item
    //private OnAutoCompleteListItemClicked autoCompleteListItemListener;
    private PhoneNumberFormattingTextWatcher digitFormater;
    private DialerLayout dialerLayout;
    private MenuItem accountChooserFilterItem;
    // 帐号状态提示
    private TextView rewriteTextInfo;

    private final INgnHistoryService mHistorytService;
    // 标识当前是否为搜索模式
    private boolean iSsearchMode = false;

    private Logger mLogger = LoggerFactory.getLogger(DialerFragment.class.getCanonicalName());

    public static DialerFragment newInstance(int position) {
        DialerFragment f = new DialerFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    public DialerFragment() {
        handler = new Handler();
        mHistorytService = getEngine().getHistoryService();
        mHistorytService.getObservableEvents().addObserver(this);
        historyEvents = new ArrayList<ContactItemInterface>();


        mAccountMap = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("account_extension_tv", ZphoneApplication.getUserName());
        map.put("account_photo_iv", R.drawable.ic_action_user);
        map.put("account_status_iv",
                ZphoneApplication.getSipService().isRegistered() ? R.drawable.ic_status_dot_green
                        : R.drawable.ic_status_dot_red);
        mAccountMap.add(map);
    }

    private OnEditorActionListener keyboardActionListener = new OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView tv, int action, KeyEvent arg2) {
            if (action == EditorInfo.IME_ACTION_GO) {
                placeCall();
                return true;
            }
            return false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mDualPane = getResources().getBoolean(R.bool.use_dual_panes);
        digitFormater = new PhoneNumberFormattingTextWatcher();

        // Auto complete list in case of text
        itemAdapter = new HistoryEventItemAdapter(historyEvents, getActivity(),
                R.layout.fragment_dialer_call_log_list_item);
        //autoCompleteListItemListener = new OnAutoCompleteListItemClicked(autoCompleteAdapter);

        /*
         * if (isDigit == null) { isDigit = !prefsWrapper
         * .getPreferenceBooleanValue(SipConfigManager.START_WITH_TEXT_DIALER);
         * }
         */
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(TEXT_MODE_KEY, isDigit);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v;
        v = inflater.inflate(R.layout.fragment_dial, null);
        mSpinner = (Spinner) v.findViewById(R.id.spinnerAdapter);
        digits = (DigitsEditText) v.findViewById(R.id.digitsText);
        dialPad = (Dialpad) v.findViewById(R.id.dialPad);
        callBar = (DialerCallBar) v.findViewById(R.id.dialerCallBar);
        dialText = (ImageButton) v.findViewById(R.id.dialTextDigitButton);
        autoCompleteList = (ListView) v.findViewById(R.id.autoCompleteList);
        //关闭快速滚动
        autoCompleteList.setFastScrollEnabled(false);
        if (Build.VERSION.SDK_INT >= 11) {
            autoCompleteList.setFastScrollAlwaysVisible(false);
        }
        rewriteTextInfo = (TextView) v.findViewById(R.id.rewriteTextInfo);

        //声明一个SimpleAdapter独享，设置数据与对应关系

        mSimpleAdapter = new SimpleAdapter(
                getActivity(), mAccountMap, R.layout.accounts_chooser_list_dropdown,
                new String[]{
                        "account_photo_iv", "account_extension_tv", "account_status_iv"
                }, new int[]{
                R.id.account_photo_iv, R.id.account_extension_tv, R.id.account_status_iv
        });
        //绑定Adapter到Spinner中
        mSpinner.setAdapter(mSimpleAdapter);
        mSimpleAdapter.setDropDownViewResource(R.layout.accounts_chooser_list_item);
        mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                //parent为一个Map结构的和数据
                //@SuppressWarnings("unchecked")
                //Map<String, Object> map = (Map<String, Object>) parent.getItemAtPosition(position);
                //view.findViewById(R.id.account_choice_tv).setVisibility(View.GONE);
                /*if (null != view)
                {
                    if (mSipService.isRegistered())
                    {
                        ((ImageView) view.findViewById(R.id.account_choice_iv))
                                .setImageDrawable(getResources().getDrawable(
                                        R.drawable.ic_status_dot_green));
                    }
                    else
                    {
                        ((ImageView) view.findViewById(R.id.account_choice_iv))
                                .setImageDrawable(getResources().getDrawable(
                                        R.drawable.ic_status_dot_red));
                    }
                }*/

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        // accountChooserButton =
        // (AccountChooserButton)v.findViewById(R.id.accountChooserButton);

        // accountChooserFilterItem =
        // accountChooserButton.addExtraMenuItem(R.string.apply_rewrite);

        // accountChooserFilterItem.setCheckable(true);
        // accountChooserFilterItem.setOnMenuItemClickListener(new
        // OnMenuItemClickListener() {
        // @Override public boolean onMenuItemClick(MenuItem item) { //
        // setRewritingFeature(!accountChooserFilterItem.isChecked()); return
        // true; } });

        // setRewritingFeature(prefsWrapper
        // .getPreferenceBooleanValue(SipConfigManager.REWRITE_RULES_DIALER));

        dialerLayout = (DialerLayout) v.findViewById(R.id.top_digit_dialer);
        // Digits field setup
        if (savedInstanceState != null) {
            isDigit = savedInstanceState.getBoolean(TEXT_MODE_KEY, isDigit);
        }

        digits.setOnEditorActionListener(keyboardActionListener);

        // Layout
        dialerLayout.setForceNoList(mDualPane);
        dialerLayout.setAutoCompleteListVisibiltyChangedListener(this);

        // Account chooser button setup
        // accountChooserButton.setShowExternals(true);
        // accountChooserButton.setOnAccountChangeListener(accountButtonChangeListener);

        // Dialpad
        dialPad.setOnDialKeyListener(this);

        // We only need to add the autocomplete list if we
        autoCompleteList.setAdapter(itemAdapter);
        // add menu
        autoCompleteList.setOnCreateContextMenuListener(this);
        //autoCompleteList.setOnItemClickListener(autoCompleteListItemListener);
        autoCompleteList.setOnItemClickListener(this);
        //autoCompleteList.setChoiceMode((ListView.CHOICE_MODE_MULTIPLE_MODAL));
        //autoCompleteList.setMultiChoiceModeListener(this);
        autoCompleteList.setFastScrollEnabled(true);

        // Bottom bar setup
        callBar.setOnDialActionListener(this);
        // callBar.setVideoEnabled(prefsWrapper.getPreferenceBooleanValue(SipConfigManager.USE_VIDEO));
        // Init other buttons
        initButtons(v);
        // Ensure that current mode (text/digit) is applied
        setTextDialing(!isDigit, true);
        if (initText != null) {
            digits.setText(initText);
            initText = null;
        }

        // Apply third party theme if any
        // applyTheme(v);
        v.setOnKeyListener(this);
        // applyTextToAutoComplete();
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*
         * Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE); //
         * Optional, but here we bundle so just ensure we are using csipsimple
         * package serviceIntent.setPackage(activity.getPackageName());
         * getActivity().bindService(serviceIntent, connection,
         * Context.BIND_AUTO_CREATE); // timings.addSplit("Bind asked for two");
         * if (prefsWrapper == null) { prefsWrapper = new
         * PreferencesWrapper(getActivity()); } if (dialFeedback == null) {
         * dialFeedback = new DialingFeedback(getActivity(), false); }
         * dialFeedback.resume();
         */

    }

    @Override
    public void onDetach() {
        /*
         * try { getActivity().unbindService(connection); } catch (Exception e)
         * { // Just ignore that Log.w(THIS_FILE, "Unable to un bind", e); }
         * dialFeedback.pause();
         */
        super.onDetach();
    }

    private void attachButtonListener(View v, int id, boolean longAttach) {
        DialDialpadButton button = (DialDialpadButton) v.findViewById(id);
        if (button == null) {
            // Log.w(THIS_FILE, "Not found button " + id);
            return;
        }
        if (longAttach) {
            button.setOnLongClickListener(this);
        } else {
            button.setOnClickListener(this);
        }
    }

    private void initButtons(View v) {
        /*
         * for (int buttonId : buttonsToAttach) { attachButtonListener(v,
         * buttonId, false); }
         */
        for (int buttonId : buttonsToLongAttach) {
            attachButtonListener(v, buttonId, true);
        }

        digits.setOnClickListener(this);
        digits.setKeyListener(DialerKeyListener.getInstance());
        digits.addTextChangedListener(this);
        digits.setCursorVisible(false);
        afterTextChanged(digits.getText());
    }

    public void onColorClicked(View v) {

        int color = Color.parseColor(v.getTag().toString());
        ((MainActivity) getActivity()).changeColor(color);

    }

    @Override
    public void onAutoCompleteListVisibiltyChanged() {
        applyTextToAutoComplete();

    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        KeyEvent e = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        return digits.onKeyDown(keyCode, e);
    }

    @Override
    public void placeCall() {

        if (mSipService.isRegistered() && 0 != digits.getText().toString().length()) {
            mLogger.debug("call number " + digits.getText().toString());
            mLogger.debug("start audio call");


            new Thread(
                    // voice call
                    new Runnable() {
                        @Override
                        public void run() {
                            ScreenAV.makeCall(digits.getText().toString(), NgnMediaType.Audio,
                                    ZphoneApplication.getContext());
                        }
                    }).start();
        }
        if(!mSipService.isRegistered())
        {
            Toast.makeText(getActivity(), R.string.not_register, Toast.LENGTH_SHORT).show();
        }
        else
        {
            //Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void placeVideoCall() {
        new Thread(
                new Runnable() {

                    @Override
                    public void run() {
                        // voice call
                        if (mSipService.isRegistered()) {
                            mLogger.debug("start video call");
                            ScreenAV.makeCall(digits.getText().toString(), NgnMediaType.AudioVideo,
                                    ZphoneApplication.getContext());
                        }
                    }

                }

        ).start();
    }

    @Override
    public void deleteChar() {
        keyPressed(KeyEvent.KEYCODE_DEL);

    }

    @Override
    public void placeTextDigit(View v) {
        setTextDialing(isDigit);
        ((ImageButton) v).setImageResource(isDigit ? R.drawable.ic_translate_grey600
                : R.drawable.ic_dialpad_grey600);

    }

    @Override
    public void deleteAll() {
        digits.getText().clear();

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        afterTextChanged(digits.getText());
        String newText = digits.getText().toString();
        if ("".equals(newText)) {
            ((HistoryEventItemAdapter) itemAdapter).setInsearchMode(false);
        } else {
            ((HistoryEventItemAdapter) itemAdapter).setInsearchMode(true);
        }
        itemAdapter.getFilter().filter(newText);
        // Allow account chooser button to automatically change again as we have
        // clear field
        // TODO 当输入字符为空时，帐号选择按钮可改变
        // accountChooserButton.setChangeable(TextUtils.isEmpty(newText));
        // applyRewritingInfo();

    }

    /**
     * @return true if the widget with the phone number digits is empty.
     */
    private boolean isDigitsEmpty() {
        return digits.length() == 0;
    }

    @Override
    public void afterTextChanged(Editable s) {

        // Change state of digit dialer
        final boolean notEmpty = digits.length() != 0;
        // digitsWrapper.setBackgroundDrawable(notEmpty ? digitsBackground :
        // digitsEmptyBackground);
        //callBar.setEnabled(notEmpty);

        if (!notEmpty && isDigit) {
            digits.setCursorVisible(false);
        }
        applyTextToAutoComplete();

    }

    @Override
    public void onTrigger(int keyCode, int dialTone) {
        // dialFeedback.giveFeedback(dialTone);
        keyPressed(keyCode);

    }

    private void keyPressed(int keyCode) {
        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        digits.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onLongClick(View v) {
        int vId = v.getId();
        if (vId == R.id.button0) {
            // dialFeedback.hapticFeedback();
            keyPressed(KeyEvent.KEYCODE_PLUS);
            return true;
        } else if (vId == R.id.button1) {
            if (digits.length() == 0) {
                // TODO 语音邮件拨号
                // placeVMCall();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        /*
         * if (view_id == R.id.switchTextView) { // Set as text dialing if we
         * are currently digit dialing setTextDialing(isDigit); } else
         */
        if (viewId == digits.getId()) {
            if (digits.length() != 0) {
                digits.setCursorVisible(true);
            }
        }

    }

    private boolean hasAutocompleteList() {
        if (!isDigit) {
            return true;
        }
        return dialerLayout.canShowList();
    }

    private void applyTextToAutoComplete() {
        /*
         * // If single pane for smartphone use autocomplete list if
         * (hasAutocompleteList()) { String filter =
         * digits.getText().toString();
         * autoCompleteAdapter.setSelectedText(filter); //else { //
         * autoCompleteAdapter.swapCursor(null); //} } // Dual pane : always use
         * autocomplete list if (mDualPane && autoCompleteFragment != null) {
         * autoCompleteFragment.filter(digits.getText().toString()); }
         */
    }

    /**
     * Set the mode of the text/digit input.
     *
     * @param textMode True if text mode. False if digit mode
     */
    public void setTextDialing(boolean textMode) {
        // Log.d(THIS_FILE, "Switch to mode " + textMode);
        setTextDialing(textMode, false);
    }

    /**
     * Set the mode of the text/digit input.
     *
     * @param textMode True if text mode. False if digit mode
     */
    public void setTextDialing(boolean textMode, boolean forceRefresh) {
        if (!forceRefresh && (isDigit != null && isDigit == !textMode)) {
            // Nothing to do
            return;
        }
        isDigit = !textMode;
        if (digits == null) {
            return;
        }
        if (isDigit) {
            // We need to clear the field because the formatter will now
            // apply and unapply to this field which could lead to wrong values
            // when unapplied
            digits.getText().clear();
            digits.addTextChangedListener(digitFormater);
        } else {
            digits.removeTextChangedListener(digitFormater);
        }
        digits.setCursorVisible(!isDigit);
        digits.setIsDigit(isDigit, true);

        // Update views visibility
        dialPad.setVisibility(isDigit ? View.VISIBLE : View.GONE);
        autoCompleteList.setVisibility(hasAutocompleteList() ? View.VISIBLE : View.GONE);

        // Invalidate to ask to require the text button to a digit button
        //getSherlockActivity().supportInvalidateOptionsMenu();
    }

    /**
     * Set the value of the text field and put caret at the end 文本加入闪烁字符
     *
     * @param value the new text to see in the text field
     */
    public void setTextFieldValue(CharSequence value) {
        if (digits == null) {
            initText = value.toString();
            return;
        }
        digits.setText(value);
        // make sure we keep the caret at the end of the text view
        Editable spannable = digits.getText();
        Selection.setSelection(spannable, spannable.length());
    }

    @Override
    public void update(Observable observable, Object data) {
        historyEvents.clear();
        List<NgnHistoryEvent> listsEvents = mHistorytService.getObservableEvents().filter(
                new HistoryEventAVFilter());
        for (NgnHistoryEvent ngnHistoryEvent : listsEvents) {
            historyEvents.add(new HistoryEventItem(ngnHistoryEvent));
        }
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            itemAdapter.notifyDataSetChanged();
        } else {
            handler.post(new Runnable() {

                @Override
                public void run() {
                    itemAdapter.notifyDataSetChanged();

                }
            });
        }
    }

    @Override
    public void onResume() {
        ((HistoryEventItemAdapter) itemAdapter).setInsearchMode(false);
        itemAdapter.getFilter().filter("");
        setTextDialing(true);
        dialText.setImageResource(isDigit ? R.drawable.ic_translate_grey600
                : R.drawable.ic_dialpad_grey600);
        super.onResume();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

        menu.setHeaderTitle(getResources().getString(R.string.choice));
        //添加菜单项
        if (!((HistoryEventItemAdapter) itemAdapter).isInSearchMode()) {
            menu.add(UNIQUE_FRAGMENT_GROUP_ID, 1, 0, getResources().getString(R.string.remove_from_call_log));
            menu.add(UNIQUE_FRAGMENT_GROUP_ID, 2, 0, getResources().getString(R.string.add_to_contacts));
            menu.add(UNIQUE_FRAGMENT_GROUP_ID, 3, 0, getResources().getString(R.string.clear_all_call_log));
        }
        super.onCreateContextMenu(menu, v, menuInfo);

    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        if (item.getGroupId() != UNIQUE_FRAGMENT_GROUP_ID) {
            mLogger.error("UNIQUE_FRAGMENT_GROUP_ID " + UNIQUE_FRAGMENT_GROUP_ID + " != "
                    + item.getGroupId());
            return false;
        }

        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        historyEventItem = (HistoryEventItem) ((HistoryEventItemAdapter) itemAdapter)
                .getItem(info.position);
        switch (item.getItemId()) {
            case 1:
                Engine.getInstance().getHistoryService().deleteEvent(historyEventItem.getEvent());
                itemAdapter.getFilter().filter("");
                break;
            case 2:
                /*Intent it = new Intent(Intent.ACTION_INSERT_OR_EDIT, Uri.withAppendedPath(
                        Uri.parse("content://com.android.contacts"), "contacts"));*/
                Intent it = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                it.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
                /*it.setType("vnd.android.cursor.dir/person");
                it.setType("vnd.android.cursor.dir/contact");
                it.setType("vnd.android.cursor.dir/raw_contact");*/
                // 联系人姓名
                it.putExtra(android.provider.ContactsContract.Intents.Insert.NAME, historyEventItem
                        .getEvent().getDisplayName());
                // 公司
                //it.putExtra(android.provider.ContactsContract.Intents.Insert.COMPANY,
                //"北京XXXXXX公司");
                // email
                //it.putExtra(android.provider.ContactsContract.Intents.Insert.EMAIL,
                //"123456@qq.com");
                // 手机号码
                it.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE,
                        NgnUriUtils.getValidPhoneNumber(historyEventItem.getEvent()
                                .getRemoteParty()));
                // 单位电话
                //it.putExtra(
                //android.provider.ContactsContract.Intents.Insert.SECONDARY_PHONE,
                //"18600001111");
                // 住宅电话
                //it.putExtra(
                //android.provider.ContactsContract.Intents.Insert.TERTIARY_PHONE,
                //"010-7654321");
                // 备注信息
                //it.putExtra(android.provider.ContactsContract.Intents.Insert.JOB_TITLE,
                //"名片");
                startActivityForResult(it, REQUEST_CODE_EDIT_CONTACTS);

                break;

            case 3:
                Engine.getInstance().getHistoryService().clear();
                itemAdapter.getFilter().filter("");
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (((HistoryEventItemAdapter) itemAdapter).isInSearchMode()) {
            String number = ((ContactItem) ((HistoryEventItemAdapter) itemAdapter)
                    .getItem(position)).getContact().getPrimaryNumber();
            setCallText(number);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Engine.getInstance().getHistoryService().updateEvent(historyEventItem.getEvent());
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void statusChange(NgnRegistrationEventTypes type) {
        //TODO NullPoint 
        if (mAccountMap.size() > 0) {
            mAccountMap.get(0).put("account_extension_tv", ZphoneApplication.getUserName());
            switch (type) {
                case REGISTRATION_OK:
                    mAccountMap.get(0).put("account_status_iv", R.drawable.ic_status_dot_green);
                    break;
                case REGISTRATION_INPROGRESS:
                    mAccountMap.get(0).put("account_status_iv", R.drawable.ic_status_dot_yellow);
                    break;
                default:
                    mAccountMap.get(0).put("account_status_iv", R.drawable.ic_status_dot_red);
                    break;
            }
            mSimpleAdapter.notifyDataSetChanged();
        }
    }

    public void setCallText(String str) {
        digits.setText(str);
        digits.setCursorVisible(true);
        digits.setSelection(digits.getText().length());
    }

    private class GetDataFromDBTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPostExecute(Void result) {
            itemAdapter.notifyDataSetInvalidated();
            //setLoadRlVisible(false);
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            //setLoadRlVisible(true);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            List<NgnHistoryEvent> historys = mHistorytService.getEvents();
            for (NgnHistoryEvent event : historys) {
                HistoryEventItem historyEventItem = new HistoryEventItem(event);
                historyEvents.add(historyEventItem);
            }
            return null;
        }
    }
/*
    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, android.view.MenuItem item) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }*/
}
