package com.tothe.bang.smartmirrordevice.datasets;

import java.io.Serializable;

/**
 * Created by BANG on 2016-05-02.
 */
public class SmartDeviceClass implements Serializable{

    private String id;
    private final static String SerialNumber = "1234";
    private String gcmToken;

    public SmartDeviceClass() {

    }

    public static String getSerialNumber() {
        return SerialNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGcmToken() {
        return gcmToken;
    }

    public void setGcmToken(String gcmToken) {
        this.gcmToken = gcmToken;
    }
}
