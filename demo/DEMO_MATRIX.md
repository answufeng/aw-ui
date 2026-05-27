# aw-ui Demo 功能矩阵



与库内公开组件对照，用于检查演示是否覆盖核心路径。Demo App 首页 **「清单」** 可速览摘要。



## 主入口（MainActivity）



按 **7 大分类** 组织，共 **27** 个独立演示页，支持搜索与分类浏览。



| 分类 | 演示数 | 覆盖组件 |

|------|--------|----------|

| 导航与标题 | 3 | AwTitleBar、AwBottomTabBar、AwSegmentedControl |

| 输入与表单 | 3 | AwSearchView、AwCodeInputView、AwFormValidator |

| 列表与数据 | 3 | AwSimpleAdapter、AwMultiTypeAdapter、AwDividerDecoration、AwItemAnimator、AwSwipeRefreshLayout、AwLoadMoreAdapter、AwStickyHeaderDecoration、AwIndexBar |

| 布局与状态 | 4 | AwFlowLayout、AwTagView、AwExpandableLayout、AwSkeletonView、AwStateLayout |

| 弹窗与反馈 | 4 | AwDialog、AwLoadingDialog、AwActionSheetDialog、AwDialogExt、AwNoticeBar、AwStepView、AwRatingBar |

| 进度与加载 | 3 | AwCircleProgressBar、AwLoadingView、AwCountDownView |

| 视觉与动效 | 7 | AwBannerView、AwRoundImageView、AwRoundLayout、AwBadgeView、AwSwitchButton、AwMarqueeTextView、Anim 扩展 |



## 重点演示页



| Activity | 建议验证 |

|----------|----------|

| `RecyclerViewDemoActivity` | SimpleAdapter 增删、MultiType 切换、DividerDecoration 缩进分割线、ItemAnimator |

| `SwipeRefreshListDemoActivity` | 多种 Refresh Header、上拉分页 / 无更多、下拉回第一页 |

| `StateDemoActivity` | AwStateLayout 四态与重试；点按循环 StateTransition |

| `ShowcaseActivity` | AwDialog / AwLoadingDialog / AwActionSheetDialog / Context 扩展 |

| `AnimDemoActivity` | fadeIn、fadeOut、slideInFromBottom、slideOutToBottom、fadeSlideIn |

| `StickyHeaderDemoActivity` | 吸顶 Header + 侧边索引 |



## 推荐手测（边界与极端场景）



| 场景 | 建议操作 |

|------|----------|

| 下拉刷新 | 快速连续下拉、刷新中途退出 Activity，确认无泄漏且 `isRefreshing` 能关掉 |

| 列表 | 长列表惯性滚动 + 系统「减少动画」开关 |

| 无障碍 | TalkBack 走查首页与各分类入口 |

| 低内存 | 开发者选项不保留活动 + 反复打开 Banner / 列表页 |

更新组件时请同步维护本表与 `DemoData.kt` / `MainActivity` 入口。

