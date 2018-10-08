package com.tothe.bang.smartmirrorclient.datasets;

import java.io.Serializable;

/**
 * Created by BANG on 2016-04-06.
 */
public class UserClass implements Serializable {

    private String id;
    private String email;
    private String name;
    private String password;
    private String lat;
    private String lng;
    private String addr;
    private String facebook_id;

    private DeviceClass deviceClass;

    public UserClass() {
        deviceClass = new DeviceClass();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getFacebook_id() {
        return facebook_id;
    }

    public void setFacebook_id(String facebook_id) {
        this.facebook_id = facebook_id;
    }

    public DeviceClass getDeviceClass() {
        return deviceClass;
    }

    public void setDeviceClass(DeviceClass deviceClass) {
        this.deviceClass = deviceClass;
    }

    //스마트 거울 기기 정보 클래스
    public class DeviceClass implements Serializable{

        private String id;
        private String Serial_Number;
        private String token;

        public DeviceClass() {

        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSerial_Number() {
            return Serial_Number;
        }

        public void setSerial_Number(String serial_Number) {
            Serial_Number = serial_Number;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
