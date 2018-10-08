package com.tothe.bang.smartmirrordevice.datasets;

/**
 * Created by BANG on 2016-05-07.
 */
public class VoiceClass {

    public VoiceClass() {

    }

    public static boolean isMusicStart(String strVoiceResult) {
        return strVoiceResult.equals("노래") || strVoiceResult.equals("음악");
    }

    public static boolean isMusicStop(String strVoiceResult) {
        return strVoiceResult.equals("그만") || strVoiceResult.equals("정지");
    }

    public static boolean isMusicNext(String strVoiceResult) {
        return strVoiceResult.equals("다음")
                || strVoiceResult.equals("다음 노래") || strVoiceResult.equals("다음노래");
    }

    public static boolean isMusicPrev(String strVoiceResult) {
        return strVoiceResult.equals("이전") || strVoiceResult.equals("뒤로");
    }

    public static boolean isMusicPlay(String strVoiceResult) {
        return strVoiceResult.equals("재생") || strVoiceResult.equals("다시");
    }

    public static boolean isVolumUp(String strVoiceResult) {
        return strVoiceResult.equals("소리 키워") || strVoiceResult.equals("소리키워");
    }

    public static boolean isVolumDown(String strVoiceResult) {
        return strVoiceResult.equals("소리 줄여") || strVoiceResult.equals("소리줄여");
    }

    public static boolean isManual(String strVoiceResult) {
        return strVoiceResult.equals("도와줘");
    }

    public static boolean isRemoveMsg(String strVoiceResult) { return strVoiceResult.equals("알림 지워"); }

    public static boolean isHello(String strVoiceResult) {
        return strVoiceResult.equals("안녕");
    }
}
