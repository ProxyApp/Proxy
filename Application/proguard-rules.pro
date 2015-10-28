# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/Evan/android-sdks/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontwarn okio.**
-dontwarn retrofit.**
-dontwarn butterknife.**
-dontwarn com.squareup.**
-dontwarn com.caverock.**
-dontwarn rx.internal.**
-dontwarn javax.**
-dontwarn io.realm.**
-dontwarn com.twitter.**
-dontwarn com.fasterxml.jackson.databind.**


-keep class io.realm.** { *; }
-keep class com.firebase.** { *; }

-keepnames class com.shaded.fasterxml.jackson.** { *; }
-keepnames public class * extends io.realm.RealmObject

-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

