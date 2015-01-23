
package com.zycoo.android.zphone.ui.message;

public class VoiceMailBean {

    private int priority;
    private int duration;
    private int wd;
    private String origmailbox;
    private String context;
    private String macrocontext;
    private String exten;
    private String rdnis;
    private String callerchan;
    private String callerid;
    private String origdate;
    private String origtime;
    private String category;
    private String msg_id;
    private String flag;
    private String type;
    private String file_name;

    public String getOrigmailbox() {
        return origmailbox;
    }

    public String getContext() {
        return context;
    }

    public String getMacrocontext() {
        return macrocontext;
    }

    public String getExten() {
        return exten;
    }

    public String getRdnis() {
        return rdnis;
    }

    public int getPriority() {
        return priority;
    }

    public String getCallerchan() {
        return callerchan;
    }

    public String getCallerid() {
        return callerid;
    }

    public String getOrigdate() {
        return origdate;
    }

    public String getOrigtime() {
        return origtime;
    }

    public String getCategory() {
        return category;
    }

    public String getMsg_id() {
        return msg_id;
    }

    public String getFlag() {
        return flag;
    }

    public int getDuration() {
        return duration;
    }

    public int getWd() {
        return wd;
    }

    public String getType() {
        return type;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setOrigmailbox(String origmailbox) {
        this.origmailbox = origmailbox;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void setMacrocontext(String macrocontext) {
        this.macrocontext = macrocontext;
    }

    public void setExten(String exten) {
        this.exten = exten;
    }

    public void setRdnis(String rdnis) {
        this.rdnis = rdnis;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setCallerchan(String callerchan) {
        this.callerchan = callerchan;
    }

    public void setCallerid(String callerid) {
        this.callerid = callerid;
    }

    public void setOrigdate(String origdate) {
        this.origdate = origdate;
    }

    public void setOrigtime(String origtime) {
        this.origtime = origtime;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setMsg_id(String msg_id) {
        this.msg_id = msg_id;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setWd(int wd) {
        this.wd = wd;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String insertSql()
    {
        String sql = String
                .format("INSERT INTO INFOS(ORIGMAILBOX, CONTEXT, MACROCONTEXT, EXTEN, rdnis, PRIORITY, CALLERCHAN, CALLERID, ORIGDATE, ORIGTIME, CATEGORY, MSG_ID, FLAG, DURATION, FILE_NAME,TYPE, WD ) VALUES('%s','%s','%s', '%s', '%s', '%d', '%s','%s','%s','%s', '%s', '%s', '%s', '%d', '%s', '%s', '%d');",
                        origmailbox, context, macrocontext, exten, rdnis, priority, callerchan,
                        callerid, origdate, origtime, category, msg_id, flag, duration, file_name,
                        type, wd);
        return sql;
    }
}
