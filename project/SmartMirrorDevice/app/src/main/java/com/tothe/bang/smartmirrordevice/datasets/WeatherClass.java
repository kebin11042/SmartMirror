package com.tothe.bang.smartmirrordevice.datasets;

import com.tothe.bang.smartmirrordevice.R;

import java.io.Serializable;

/**
 * Created by BANG on 2016-05-05.
 */
public class WeatherClass implements Serializable{
    private String temp;    //온도 float
    private String icon;    //아이콘 정보

    public WeatherClass() {

    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getIconDrawableId() {

        if(this.icon.equals("01d") || this.icon.equals("01n")){
            return R.drawable.img_sunny;
        }
        else if(this.icon.equals("02d") || this.icon.equals("02n")){
            return R.drawable.img_partly_clouds;
        }
        else if(this.icon.equals("03d") || this.icon.equals("03n")
                || this.icon.equals("04d") || this.icon.equals("04n")){
            return R.drawable.img_clouds;
        }
        else if(this.icon.equals("09d") || this.icon.equals("09n")
                || this.icon.equals("10d") || this.icon.equals("10n")){
            return R.drawable.img_rain;
        }
        else if(this.icon.equals("11d") || this.icon.equals("11n")){
            return R.drawable.img_storm;
        }
        else if(this.icon.equals("13d") || this.icon.equals("13n")){
            return R.drawable.img_snow;
        }
        else if(this.icon.equals("50d") || this.icon.equals("50n")){
            return R.drawable.img_fog;
        }

        return R.drawable.img_not_info;
    }
}
