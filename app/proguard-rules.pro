# 웹 화면에서 부르는 백업 저장 함수는 이름을 바꾸지 않아요.
-keepattributes JavascriptInterface
-keepclassmembers class com.nia.mylog.MainActivity$Bridge {
    @android.webkit.JavascriptInterface <methods>;
}
