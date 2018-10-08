package com.tothe.bang.smartmirrordevice.datasets;

import java.io.Serializable;

/**
 * Created by BANG on 2016-05-03.
 */
public class UserClass implements Serializable {

    private String id;
    private String name;
    private String addr;


    public UserClass() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }
}
