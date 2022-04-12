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

#---------------------------------1.基本指令区域配置-----------------------
-optimizationpasses 5                       # 指定代码的压缩级别
-dontusemixedcaseclassnames                 # 是否使用大小写混合
-dontskipnonpubliclibraryclasses            # 不去忽略非公共的库类
-dontskipnonpubliclibraryclassmembers       # 不去忽略非公共的类成员变量
-dontpreverify                              # 混淆时是否做预校验
-verbose                                    # 混淆时是否记录日志
-ignorewarnings                             # 忽略警告
-dontoptimize                               # 优化不优化输入的类文件
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*  # 混淆时所采用的算法
-printmapping proguardMapping.txt               # 打印Mapping文件
-keepattributes *Annotation*,InnerClasses   # 保护注解
-keepattributes Signature                   # 避免混淆泛型 如果混淆报错建议关掉
-keepattributes SourceFile,LineNumberTable  # 抛出异常时保留代码行号

#输出文件记录控制，默认会在build/outputs/mapping/release生成
#dump.txt,mapping.txt,resources.txt,seeds.txt,usage.txt
##记录生成的日志数据,gradle build时在本项目根目录输出##
#apk 包内所有 class 的内部结构
#-dump proguard/class_files.txt
#未混淆的类和成员
#-printseeds proguard/seeds.txt
#列出从 apk 中删除的代码
#-printusage proguard/unused.txt
#混淆前后的映射
#-printmapping proguard/mapping.txt
########记录生成的日志数据，gradle build时 在本项目根目录输出-end######
#移除Log类打印各个等级日志的代码，打正式包的时候可以做为禁log使用，这里可以作为禁止log打印的功能使用，另外的一种实现方案是通过BuildConfig.DEBUG的变量来控制
#-assumenosideeffects class android.util.Log {
#    public static *** v(...);
#    public static *** i(...);
#    public static *** d(...);
#    public static *** w(...);
#    public static *** e(...);
#}

#---------------------------------2.Android默认保留指令区-----------------------
-keep public class * extends android.view.View                      # 保持自定义试图类不被混淆
-keep public class * extends android.app.Fragment                   # 保持哪些类不被混淆
-keep public class * extends android.app.Activity                   # 保持哪些类不被混淆
-keep public class * extends android.app.Application                # 保持哪些类不被混淆
-keep public class * extends android.app.Service                    # 保持哪些类不被混淆
-keep public class * extends android.content.BroadcastReceiver      # 保持哪些类不被混淆
-keep public class * extends android.content.ContentProvider        # 保持哪些类不被混淆
-keep public class * extends android.app.backup.BackupAgentHelper   # 保持哪些类不被混淆
-keep public class * extends android.preference.Preference          # 保持哪些类不被混淆
-keep public class org.apache.http.conn.ssl.SSLSocketFactory        # 保持哪些类不被混淆
#-keep public class com.android.vending.licensing.ILicensingService  # 保持哪些类不被混淆

#androidx
-keep class com.google.android.material.** {*;}
-keep class androidx.** {*;}
-keep public class * extends androidx.**
-keep interface androidx.** {*;}
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**
-dontwarn androidx.**

#如果有引用v4,v7-start包可以添加下面内容
#-keep class android.support.** {*;}
#-keep public class * extends android.support.v4.**
#-keep public class * extends android.support.v7.**
#-keep public class * extends android.support.annotation.**
#-keep public class * extends android.support.v4.app.Fragment
#-keep public class * extends android.support.v4.app.FragmentActivity
#end v4,v7包可以添加下面内容
-keepclassmembers class * extends android.app.Activity {            # 保持自定义控件类不被混淆
    public void *(android.view.View);
}
-keepclasseswithmembers class * {                                   # 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {                                   # 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keep public class * extends android.view.View{                     # 保持自定义控件类不被混淆
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
#-keep class android.support.design.widget.**{                       # 保持自定义控件类不被混淆
#    *;
#}
-keepclasseswithmembernames class * {                               # 保持native方法不被混淆
    native <methods>;
}
-keepclassmembers enum * {                                          # 保持枚举enum类不被混淆
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable {                    # 保持Parcelable不被混淆
    public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements java.io.Serializable {         # 保持Serializable不被混淆
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keep class **.R$* {                                                # 保持资源文件R不被混淆
 *;
}
-keepclassmembers class **.R$* {                                    # 保持资源文件R不被混淆
    public static <fields>;
}
-keepclassmembers class * {                                         # 保持OnXXXEvent不被混淆
    void *(**On*Event);
}
-keepattributes *Annotation*

#---------------------------------3.webview混淆指令区-----------------------
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
#-keepclassmembers class * extends android.webkit.webViewClient {
#    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
#    public boolean *(android.webkit.WebView, java.lang.String);
#}
#-keepclassmembers class * extends android.webkit.webViewClient {
#    public void *(android.webkit.webView, java.lang.String);
#}

#---------------------------------4.(反射实体)个人指令区---------------------

-keep class com.afar.osaio.bean.** {*;}
-keep class com.afar.osaio.smart.device.bean.** {*;}
-keep class com.afar.osaio.message.bean.** {*;}
-keep class com.afar.osaio.protocol.** {*;}
-keep class com.afar.osaio.smart.event.** {*;}
-keep class com.afar.osaio.smart.hybrid.** {*;}
-keep class com.afar.osaio.smart.media.bean.** {*;}
-keep class com.afar.osaio.smart.push.bean.** {*;}
-keep class com.afar.osaio.smart.scan.bean.** {*;}
-keep class com.afar.osaio.smart.smartlook.bean.** {*;}
-keep class com.afar.osaio.widget.** {*;}

-keep class com.boredream.** {*;}
-keep class com.bigkoo.** {*;}
-keep class pub.devrel.** {*;}
-keep class me.yokeyword.fragmentation** {*;}
-keep class me.yokeyword.eventbusactivityscope.** {*;}
-keep class me.majiajie.pagerbottomtabstrip.** {*;}
-keep class com.github.chrisbanes.photoview.** {*;}
-keep class com.megabox.android.** {*;}
-keep class com.megabox.** {*;}
-keep class com.aspsine.** {*;}
-keep class com.mcxtzhang.** {*;}
-keep class com.suke.** {*;}
-keep class com.scenery7f.** {*;}
-keep class com.contrarywind.** {*;}
-keep class com.uuzuche.** {*;}
-keep class com.steelkiwi.** {*;}
-keep class com.blog.www.guideview.** {*;}
-keep class com.yalantis.ucrop.** {*;}

-keep class com.google.zxing.** {*;}

#---------------------------------5.反射类指令区-----------------------
-keep class com.afar.osaio.base.BaseApplication {*;}

#---------------------------------6.第三方函数库指令区-----------------------
-dontwarn sun.misc.**
-keep class com.google.protobuf.** {*;}
-keep class org.greenrobot.greendao.** {*;}
-keep class net.sqlcipher.** {*;}
-keep class org.greenrobot.eventbus.** {*;}
-keep class rx.** {*;}
-keep class retrofit2.** {*;}
-keep class okhttp3.** {*;}
-keep class okio.** {*;}
-keep class com.google.gson.** {*;}
-keep class com.tencent.mmkv.** {*;}
-keep class pl.droidsonroids.** {*;}
-keep class com.github.promeg.** {*;}
-keep class com.bumptech.glide.** {*;}
-keep class com.cantalou.dexoptfix.** {*;}
-keep class com.tapadoo.alerter.** {*;}
#-keep class butterknife.** {*;}
-keep class no.nordicsemi.** {*;}
-keep class com.yc.pagerlib.** {*;}
-keep class com.gyf.immersionbar.** {*;}
#-keep class com.orhanobut.** {*;}
-keep class com.google.protobuf.** {*;}
-keep class com.tuya.** {*;}

#Retrofit
# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

#greendao
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
public static java.lang.String TABLENAME;
}
-keep class **$Properties { *; }

# If you DO use SQLCipher:
-keep class org.greenrobot.greendao.database.SqlCipherEncryptedHelper { *; }

#okhttp
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform

#youmeng
-dontwarn com.umeng.**
-dontwarn com.taobao.**
-dontwarn anet.channel.**
-dontwarn anetwork.channel.**
-dontwarn org.android.**
-dontwarn org.apache.thrift.**
-dontwarn com.xiaomi.**
-dontwarn com.huawei.**
-dontwarn com.meizu.**

-keepattributes *Annotation*

-keep class com.taobao.** {*;}
-keep class org.android.** {*;}
-keep class anet.channel.** {*;}
-keep class com.umeng.** {*;}
-keep class com.xiaomi.** {*;}
-keep class com.huawei.** {*;}
-keep class com.meizu.** {*;}
-keep class org.apache.thrift.** {*;}

-keep class com.alibaba.sdk.android.**{*;}
-keep class com.ut.**{*;}
-keep class com.ta.**{*;}

-keep class com.google.firebase.** {*;}
-keep class com.google.android.gms.** {*;}

-keep public class **.R$*{
   public static final int *;
}

-keep class * implements java.io.Serializable

-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

#glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

#eventbus
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# And if you use AsyncExecutor:
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

#android-gif-drawable
-keep public class pl.droidsonroids.gif.GifIOException{<init>(int);}
-keep class pl.droidsonroids.gif.GifInfoHandle{<init>(long,int,int,int);}

#kotlin
-keep class org.jetbrains.** { *; }
-dontwarn org.jetbrains.**
-keep class org.intellij.** { *; }
-dontwarn org.intellij.**
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}
#-keep class kotlinx.coroutines.android.** {*;}
# ServiceLoader support
#-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
#-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
#-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
#-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}

# Most of volatile fields are updated with AFU and should not be mangled
#-keepclassmembernames class kotlinx.** {
#    volatile <fields>;
#}

# Google play service gms
#-keep interface com.google.android.gms.vision.** { *; }
#-keep interface com.google.android.gms.common.** { *; }