# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the Android SDK proguard-android-optimize.txt file.
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.dam.studyfiles.models.** { *; }
-keep class com.dam.studyfiles.database.** { *; }
