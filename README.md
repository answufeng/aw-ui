# aw-ui

Common UI component library for Android. Provides StateLayout, TitleBar, RecyclerView adapters, dialogs, animations, and custom layouts.

## Installation

Add the dependency in your module-level `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.answufeng:aw-ui:1.0.0")
}
```

Make sure you have the JitPack repository in your root `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

## Features

- StateLayout: 4-state container (Content/Loading/Empty/Error) with lazy inflate and animation
- TitleBar: Common toolbar with immersion support
- SimpleAdapter / MultiTypeAdapter: RecyclerView adapters with DiffUtil
- BrickDialog / LoadingDialog: Dialog utilities
- BrickAnim: View animation extensions (fadeIn, fadeOut, slideIn, slideOut)
- RoundLayout / FlowLayout / BadgeView: Custom layouts
- DividerDecoration: RecyclerView divider

## Usage

```kotlin
// StateLayout
stateLayout.showLoading()
stateLayout.showContent()
stateLayout.showEmpty()
stateLayout.showError { retryLoad() }

// SimpleAdapter
val adapter = SimpleAdapter(ItemBinding::inflate,
    diffCallback = object : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(old: Item, new: Item) = old.id == new.id
        override fun areContentsTheSame(old: Item, new: Item) = old == new
    }
) { binding, item, _ -> binding.tvName.text = item.name }

// Animations
view.fadeIn()
view.fadeOut()
view.slideInFromBottom()
```

## License

Apache License 2.0. See [LICENSE](LICENSE) for details.
