package com.tothe.bang.smartmirrorclient.datasets;

import java.io.Serializable;

/**
 * Created by BANG on 2016-04-07.
 */
public class LocationClass implements Serializable {

    private String title;       //전체 주소
    private String[] localName; //분할된 주소 3개
    private String lat;         //위도
    private String lng;         //경도

    public LocationClass() {
        localName = new String[3];
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[] getLocalName() {
        return localName;
    }

    public String getTotalLocalName(){
        String ret = localName[0] + " " + localName[1] + " " + localName[2];

        return ret;
    }

    public void setLocalName(String[] localName) {
        this.localName = localName;
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
}
