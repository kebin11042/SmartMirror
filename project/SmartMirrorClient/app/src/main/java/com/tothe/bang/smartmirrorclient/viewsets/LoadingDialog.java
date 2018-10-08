package com.tothe.bang.smartmirrorclient.viewsets;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by BANG on 2016-04-07.
 */
public class LoadingDialog extends SweetAlertDialog {

    public LoadingDialog(Context context) {
        super(context, SweetAlertDialog.PROGRESS_TYPE);

        this.setTitleText("로딩 중입니다");
        this.setCancelable(false);
        this.getProgressHelper().setBarColor(Color.parseColor("#6788AC"));
    }
}
