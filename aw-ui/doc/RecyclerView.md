# RecyclerView 模块

## AwSimpleAdapter

```kotlin
val adapter = awSimpleAdapter(
    diffCallback = idDiffCallback<User> { it.id },
    inflate = ItemUserBinding::inflate,
) { binding, user, _ ->
    binding.tvName.text = user.name
}
adapter.submitList(users)
```

## AwLoadMoreAdapter

```kotlin
val adapter = awLoadMoreAdapter(...)
adapter.setOnLoadMoreListener { loadPage() }

// 分页结束
adapter.finishPage(newItems, hasMore = true, success = true)
// 失败
adapter.finishPage(emptyList(), hasMore = true, success = false)
```

支持 `LinearLayoutManager`、`GridLayoutManager`、`StaggeredGridLayoutManager`。

## AwMultiTypeAdapter

```kotlin
val adapter = awMultiTypeAdapter {
    register(typeA) { ... }
    register(typeB) { ... }
}
```

## 装饰与动画

- `AwDividerDecoration`：分割线
- `AwItemAnimator`：列表动画
- `AwStickyHeaderDecoration`：吸顶分组 Header

## Diff 工具

`idDiffCallback { it.id }`、`simpleDiffCallback(...)`、`stringDiffCallback()`

## Demo

`SwipeRefreshListDemoActivity`、`ShowcaseActivity`
