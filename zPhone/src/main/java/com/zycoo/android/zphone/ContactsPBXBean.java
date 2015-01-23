
package com.zycoo.android.zphone;

import java.util.Date;

public class ContactsPBXBean {
    public enum ContatcsPBXStatus implements EnumInterface
    {
        UNKONW("UNKNOW", 1), OK("OK", 2), UNREACHABLE("UNREACHABLE",3);
        // 成员变量
        private String name;
        private int index;

        // 构造方法
        private ContatcsPBXStatus(String name, int index) {
            this.name = name;
            this.index = index;
        }

        // 普通方法
        public String getName(int index) {
            for (ContatcsPBXStatus c : ContatcsPBXStatus.values()) {
                if (c.getIndex() == index) {
                    return c.name;
                }
            }
            return null;
        }

        // get set 方法
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }

    public enum ContactsPBXType implements EnumInterface
    {
        UNKONW("UNKNOW", 1),SIP("SIP", 2), IAX("IAX", 3);

        // 成员变量
        private String name;
        private int index;

        // 构造方法
        private ContactsPBXType(String name, int index) {
            this.name = name;
            this.index = index;
        }

        // 普通方法
        public String getName(int index) {
            for (ContactsPBXType c : ContactsPBXType.values()) {
                if (c.getIndex() == index) {
                    return c.name;
                }
            }
            return null;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String name) {
            this.name = name;

        }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public void setIndex(int index) {
            this.index = index;
        }
    }

    private String photoUrl;
    private String name;
    private String number;
    private Date date;
    private ContactsPBXType type;
    private ContatcsPBXStatus status;

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public ContactsPBXType getType() {
        return type;
    }

    public void setType(ContactsPBXType type) {
        this.type = type;
    }

    public ContatcsPBXStatus getStatus() {
        return status;
    }

    public void setStatus(ContatcsPBXStatus status) {
        this.status = status;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
