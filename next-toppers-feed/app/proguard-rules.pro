# ─────────────────────────────────────────────────────────────────────────────
# Next Toppers Feed — ProGuard / R8 Rules
# Version 2.0.0 — Final Production Release (Prompt 12)
# ─────────────────────────────────────────────────────────────────────────────

# ── Firebase ──────────────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Keep Firebase Firestore data model classes (reflection-based deserialization)
-keep class com.nexttoppers.feed.data.model.** { *; }
-keepclassmembers class com.nexttoppers.feed.data.model.** {
    public <init>();
    <fields>;
}

# ── Hilt / Dagger ─────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @dagger.Module class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @javax.inject.Singleton class * { *; }
-dontwarn dagger.**

# ── Kotlin ────────────────────────────────────────────────────────────────────
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.Lazy { *; }

# ── Kotlin Coroutines ─────────────────────────────────────────────────────────
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ── AndroidX / Jetpack ────────────────────────────────────────────────────────
-keep class androidx.lifecycle.** { *; }
-keep class androidx.navigation.** { *; }
-keep class androidx.datastore.** { *; }
-keep class androidx.work.** { *; }
-dontwarn androidx.lifecycle.**
-dontwarn androidx.navigation.**

# ── Jetpack Compose ───────────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
# Keep Compose runtime internals needed for recomposition
-keepclassmembers class androidx.compose.runtime.** { *; }

# ── Credentials Manager / Google Identity ────────────────────────────────────
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.** { *; }
-dontwarn androidx.credentials.**

# ── Coil (image loading) ──────────────────────────────────────────────────────
-keep class coil.** { *; }
-dontwarn coil.**

# ── Lottie ────────────────────────────────────────────────────────────────────
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# ── OkHttp / Okio (used by Coil and Firebase) ────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-keep class okhttp3.internal.concurrent.TaskRunner { *; }

# ── Reflection & Serialization ────────────────────────────────────────────────
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations

# ── Enum classes ──────────────────────────────────────────────────────────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ── Parcelable ────────────────────────────────────────────────────────────────
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ── Serializable ─────────────────────────────────────────────────────────────
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ── FCM / Messaging Service ────────────────────────────────────────────────────
-keep class com.nexttoppers.feed.service.NtfMessagingService { *; }

# ── Hilt generated components (keep for DI) ───────────────────────────────────
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keepclasseswithmembernames class * { @dagger.hilt.* <methods>; }
-keepclasseswithmembernames class * { @javax.inject.* <methods>; }

# ── Debugging: retain source names for crash reports ─────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Remove verbose logging in release ────────────────────────────────────────
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
-assumenosideeffects class com.nexttoppers.feed.util.AppLogger {
    public static *** d(...);
    public static *** v(...);
}

# ── R8 optimisation hints ─────────────────────────────────────────────────────
-allowaccessmodification
-repackageclasses 'ntf'
