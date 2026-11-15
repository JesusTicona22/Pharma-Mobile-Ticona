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
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# Atributos necesarios para serialización y reflexión de Ktor / kotlinx.serialization
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# Conservar DTOs de transporte y serializadores generados
-keep class pe.edu.upeu.pharmamobil.data.remote.dto.** { *; }
-keepclassmembers class pe.edu.upeu.pharmamobil.data.remote.dto.** {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Conservar ViewModels inyectados por Koin
-keep class pe.edu.upeu.pharmamobil.presentation.**ViewModel { *; }
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# SLF4J usado opcionalmente por Ktor en tiempo de ejecución
-dontwarn org.slf4j.impl.StaticLoggerBinder