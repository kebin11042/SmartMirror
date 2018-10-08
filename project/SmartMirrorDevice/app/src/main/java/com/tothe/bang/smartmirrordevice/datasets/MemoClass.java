package com.tothe.bang.smartmirrordevice.datasets;

import java.io.Serializable;

/**
 * Created by BANG on 2016-05-09.
 */
public class MemoClass implements Serializable {

    private String id;
    private String subject;
    private String date;
    private String created;

    public MemoClass() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }
}
