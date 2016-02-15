# Proguard Configuration for Realm (http://realm.io)
# For detailed discussion see: https://groups.google.com/forum/#!topic/realm-java/umqKCc50JGU
# Additionally you need to keep your Realm Model classes as well
# For example:
# -keep class com.yourcompany.realm.** { *; }

-keep class io.realm.annotations.RealmModule
-keep @io.realm.annotations.RealmModule class *
-keep @io.realm.internal.Keep class * { *; }
-keep class io.realm.internal.Spec
-keep class io.realm.internal.Mixed {*;}
-keep class io.realm.exceptions.* { *; }
-keep class io.realm.internal.async.BadVersionException { *; }
-keep class io.realm.internal.OutOfMemoryError { *; }
-keep class io.realm.internal.TableSpec { *; }
-keep class io.realm.internal.Mixed { *; }
-keep class io.realm.internal.ColumnType { *; }
-dontwarn javax.**
-dontwarn io.realm.**
