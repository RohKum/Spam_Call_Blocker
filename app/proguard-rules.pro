# R8 Optimization & Proguard Keep Rules for Clean Ring

# Room Database Keep Rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep Data Entities & Serialized Models for Gson / Room
-keep class com.dev2drop.cleanring.data.** { *; }
-keepclassmembers class com.dev2drop.cleanring.data.** { *; }

# Google Play Billing Library
-keep class com.android.billingclient.api.** { *; }

# Libphonenumber
-keep class com.google.i18n.phonenumbers.** { *; }

# Gson Keep Rules
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.Unsafe
-keep class com.google.gson.** { *; }
