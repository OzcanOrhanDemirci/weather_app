# Ktor selects an engine through a service loader, and the class that names it
# is never referenced directly.
-keep class io.ktor.client.engine.android.** { *; }
-dontwarn org.slf4j.**
-dontwarn io.ktor.network.**

# kotlinx.serialization generates a serializer as a nested class and reaches it
# by name. The plugin ships rules for annotated classes; these cover the
# reflective lookup that resolves them.
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Names that appear in a crash report are worth keeping readable. The mapping
# file is produced either way, but a stack trace that needs no translation is
# faster to act on.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
