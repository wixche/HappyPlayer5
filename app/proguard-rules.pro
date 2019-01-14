# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#1.基本指令区
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose
#-ignorewarning
# 混淆前后的映射
-printmapping mapping.txt
# apk 包内所有 class 的内部结构
-dump class_files.txt
# 未混淆的类和成员
-printseeds seeds.txt
# 列出从 apk 中删除的代码
-printusage unused.txt
-optimizations !code/simplification/cast,!field/*,!class/merging/*
-keepattributes *Annotation*,InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable

#2.默认保留区
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService
-keep class android.support.** {*;}

-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclassmembers class * extends android.app.Activity{
    public void *(android.view.View);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keep class **.R$* {
 *;
}
-keepclassmembers class * {
    void *(**On*Event);
}

#3.webview
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}
-keepclassmembers class * extends android.webkit.webViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.webViewClient {
    public void *(android.webkit.webView, jav.lang.String);
}

#5complie包

#SwitchButton
-keep class com.suke.widget.**
-keepclassmembers class com.suke.widget.** {
   public *;
}

#bugly
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

#leakcanary 内存泄露
-keep public class com.squareup.leakcanary.**{*;}

#greendao
-keep class org.greenrobot.greendao.**{*;}
-keep public interface org.greenrobot.greendao.**
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
public static java.lang.String TABLENAME;
}
-keep class **$Properties
-keep class net.sqlcipher.** {*;}
-keep class net.sqlcipher.database.** {*;}
-dontwarn net.sqlcipher.database.**
-dontwarn org.greenrobot.greendao.**


#pinyin
-keep public class com.hp.hpl.sparta.**{*;}
-keep public class net.sourceforge.pinyin4j.**{*;}


#ijkplayer
-keep class com.zlm.player.**
-keepclassmembers class com.zlm.player.** {
   public *;
}
-keep class tv.danmaku.ijk.media.player.** { *; }


#com.github.zhangliangming:SwipeBackLayout、com.github.zhangliangming:RotateLayout
#com.github.zhangliangming:SeekBar、com.github.zhangliangming:SeekBar
-keep class com.zlm.libs.widget.** { *; }

#com.github.zhangliangming:HPLyrics
-keep class com.zlm.hp.lyrics.** { *; }

#com.github.zhangliangming:HPAudio
-dontwarn javax.**
-dontwarn java.awt.**
-keep class org.jaudiotagger.** { *; }
-keep class davaguine.jmac.** { *; }
-keep class com.wavpack.** { *; }
-keep class com.zlm.hp.audio.** { *; }

#com.github.zhangliangming:Register:v1.0
-keep class com.zlm.libs.register.** { *; }

#第三方弹出窗口：
-keep class com.dou361.dialogui.** { *; }

#第三方上拉加载更多，下拉刷新：
-dontwarn com.github.jdsjlzx.**
-keep class com.github.jdsjlzx.progressindicator.indicators.** { *; }

#implementation 'com.github.zhangliangming:Subtitle:v1.0'
-keep class com.zlm.subtitlelibrary.** { *; }

#6混淆项目代码
-keep class com.zlm.**
-keepclassmembers class com.zlm.** {
   public *;
}
-ignorewarnings