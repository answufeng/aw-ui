# aw-ui consumer ProGuard rules
# 仅保留反射、序列化、自定义 View XML inflation 必要入口
# 避免整包 -keep ... { *; }，优先精准 -keepclassmembers

# ===========================================================
# 自定义 View 构造函数（XML 布局 inflation 需要）
# ===========================================================

-keepclassmembers class com.answufeng.ui.widget.** {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class com.answufeng.ui.statelayout.** {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class com.answufeng.ui.titlebar.** {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# ===========================================================
# RecyclerView 适配器（反射调用内部类/方法）
# ===========================================================

-keepclassmembers class com.answufeng.ui.recyclerview.AwLoadMoreAdapter$* {
    *;
}

-keepclassmembers class com.answufeng.ui.recyclerview.AwMultiTypeAdapter$* {
    *;
}

-keepclassmembers class com.answufeng.ui.recyclerview.** {
    public <init>(...);
}

# ===========================================================
# 表单验证（反射调用内部类）
# ===========================================================

-keepclassmembers class com.answufeng.ui.form.AwFormValidator$* {
    *;
}

# ===========================================================
# BottomSheet / Dialog 组件（BottomSheetBehavior 回调）
# ===========================================================

-keepclassmembers class com.answufeng.ui.widget.AwBottomSheet$* {
    *;
}

-keepclassmembers class com.answufeng.ui.dialog.AwActionSheetDialog {
    public <init>(...);
    public void setTitle(java.lang.String);
    public void setItems(java.util.List);
    public void show();
}

# ===========================================================
# Banner / ViewPager2 / Lifecycle 相关
# ===========================================================

-keepclassmembers class com.answufeng.ui.widget.AwBannerView {
    public <init>(...);
    public void setAdapter(...);
    public void setData(...);
}

# ===========================================================
# BottomTabBar / ViewPager2 / Fragment 相关
# ===========================================================

-keepclassmembers class com.answufeng.ui.widget.AwBottomTabBar {
    public <init>(...);
    public void setItems(...);
    public void bindViewPager(...);
    public void bindFragments(...);
}

-keepclassmembers class com.answufeng.ui.widget.AwBottomTabBar$* {
    *;
}

# ===========================================================
# StateLayout / TitleBar 自定义属性
# ===========================================================

-keepclassmembers class com.answufeng.ui.statelayout.AwStateLayout {
    public void showLoading();
    public void showContent();
    public void showEmpty();
    public void showError(...);
}

-keepclassmembers class com.answufeng.ui.titlebar.AwTitleBar {
    public void setOnBackClickListener(...);
    public void setRightText(...);
    public void setRightIcon(...);
    public void applyImmersivePadding();
}
