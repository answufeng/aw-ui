# aw-ui consumer ProGuard rules

# Custom Views (XML inflation requires exact class names)
-keep public class com.answufeng.ui.widget.** { public *; }
-keep public class com.answufeng.ui.statelayout.** { public *; }
-keep public class com.answufeng.ui.titlebar.** { public *; }
-keep public class com.answufeng.ui.dialog.** { public *; }

# RecyclerView adapters
-keep class com.answufeng.ui.recyclerview.IdDiffCallback { *; }
-keep class com.answufeng.ui.recyclerview.SimpleDiffCallback { *; }
-keep class com.answufeng.ui.recyclerview.AwLoadMoreAdapter { *; }
-keep class com.answufeng.ui.recyclerview.AwLoadMoreAdapter$* { *; }
-keep class com.answufeng.ui.recyclerview.AwSimpleAdapter { *; }
-keep class com.answufeng.ui.recyclerview.AwMultiTypeAdapter { *; }

# Form validation
-keep class com.answufeng.ui.form.AwFormValidator { *; }
-keep class com.answufeng.ui.form.AwFormValidator$* { *; }

# Kotlin metadata
-keepattributes Signature, *Annotation*
-keep class kotlin.Metadata { *; }
