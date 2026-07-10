# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified in
# $ANDROID_SDK/tools/proguard/proguard-android.txt

# Keep WebView bridge classes (not used here, but safe to include)
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep MainActivity for reflection-based usage
-keep public class com.posecoach.app.MainActivity
