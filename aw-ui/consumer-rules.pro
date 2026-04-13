# aw-ui consumer ProGuard rules

# Custom views referenced from XML need all constructors
-keep class com.answufeng.ui.statelayout.StateLayout { <init>(...); }
-keep class com.answufeng.ui.titlebar.TitleBar { <init>(...); }
-keep class com.answufeng.ui.widget.RoundLayout { <init>(...); }
-keep class com.answufeng.ui.widget.FlowLayout { <init>(...); }
-keep class com.answufeng.ui.widget.BadgeView { <init>(...); }

# Keep custom attributes
-keepclassmembers class **.R$styleable { *; }
