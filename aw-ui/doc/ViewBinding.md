# ViewBinding 委托

## Activity

```kotlin
class MainActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivityMainBinding::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // 之后访问 binding 会对已 inflate 的根 View 执行 bind
        binding.tvTitle.text = "Hello"
    }
}
```

或先访问 binding 再 `setContentView(binding.root)`。

## Fragment / DialogFragment

```kotlin
private val binding by viewBinding(FragmentMyBinding::class)

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.tvTitle.text = "Hello"
}
```

视图销毁时自动清空引用。
