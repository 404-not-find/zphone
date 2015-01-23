
package com.zycoo.android.zphone.ui.message;

public class MonitorBean {
    private int duration;
    private String time;
    private String file_formate;
    private String from;
    private String to;
    private String type;
    private String file_name;

    public int getDuration() {
        return duration;
    }

    public String getTime() {
        return time;
    }

    public String getFile_formate() {
        return file_formate;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getType() {
        return type;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setFile_formate(String file_formate) {
        this.file_formate = file_formate;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String insertSql() {
        String sql = String
                .format("INSERT INTO MONITORS(DURATION, TIME, FILE_FORMATE, FROM_EXTENSION, TO_EXTENSION, TYPE, FILE_NAME) VALUES(%d, '%s', '%s', '%s', '%s', '%s', '%s')",
                        duration, time, file_formate, from, to, type, file_name);
        return sql;
    }

}
