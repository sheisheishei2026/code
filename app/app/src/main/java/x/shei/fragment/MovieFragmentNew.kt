package x.shei.fragment
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import x.shei.R
import x.shei.adapter.VideoAdapter
import x.shei.databinding.FragmentMovieBinding
import x.shei.db.Bean
import x.shei.util.GridSpacingItemDecoration
import x.shei.viewmodel.MovieViewModel
import x.shei.viewmodel.UiState
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.Collections
import java.util.Locale
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.MutableList
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.max
import kotlin.math.min

// 类型别名，简化复杂类型
private typealias FilterPredicate = (Bean) -> Boolean
private typealias DropdownListener = (String?) -> Unit

// 扩展函数：简化 View 可见性设置（内联优化）
private inline fun View.setVisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

// 扩展函数：安全获取 SearchView 查询文本（内联优化）
private inline fun SearchView.getQueryText(): String = query?.toString() ?: ""

// 扩展函数：Bean 的可搜索字段（内联优化）
private inline fun Bean.getSearchableFields(): List<String> = listOfNotNull(
    title,
    mainActor,
    director,
    country,
    updateTime,
    intro
)

// 扩展函数：安全执行操作，避免在 view 销毁后执行
private inline fun <T> FragmentMovieBinding?.safe(block: (FragmentMovieBinding) -> T): T? {
    return this?.let(block)
}

// 扩展函数：批量设置可见性
private inline fun View.setVisibleIf(condition: Boolean) = setVisible(condition)

// 扩展函数：安全获取或返回默认值
private inline fun <T> T?.or(default: T): T = this ?: default

class MovieFragmentNew : Fragment() {
    var type: Int = 0

    // 使用 ViewModel
    private val viewModel: MovieViewModel by viewModels()

    private var _binding: FragmentMovieBinding? = null

    // 使用 viewLifecycleOwner 确保 binding 在 view 销毁时自动清理
    private val binding get() = _binding ?: throw IllegalStateException(
        "Binding should only be accessed when the view is attached."
    )

    // 跟踪是否是首次加载数据
    private var isFirstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMovieBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()
        setupButtons()
        setupObservers()
        loadFile()
    }

    /**
     * 设置 LiveData 观察者
     */
    private fun setupObservers() {
        // 观察筛选后的电影列表
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredMovieList.observe(viewLifecycleOwner) { movieList ->
                    Log.d(TAG, "LiveData 观察者触发: 列表大小=${movieList.size}")
                    updateMovieList(movieList)
                }
            }
        }

        // 观察 UI 状态
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.observe(viewLifecycleOwner) { state ->
                    handleUiState(state)
                }
            }
        }

        // 观察筛选状态
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filterState.observe(viewLifecycleOwner) { filterState ->
                    updateFilterButtons(filterState)
                }
            }
        }

        // 观察国家列表
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.countryList.observe(viewLifecycleOwner) { countryList ->
                    // 国家列表已更新，可以用于下拉菜单
                }
            }
        }

        // 观察年份列表
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.yearList.observe(viewLifecycleOwner) { yearList ->
                    // 年份列表已更新，可以用于下拉菜单
                }
            }
        }
    }

    /**
     * 更新电影列表到 UI
     * 使用平滑的更新方式，避免闪烁
     */
    private fun updateMovieList(movieList: List<Bean>) {
        _binding?.let { binding ->
            // VideoAdapter 期望 List<Bean?>，所以需要转换
            val newList = movieList.map { it as Bean? }.toMutableList()
            val oldList = beanList
            val oldSize = oldList?.size ?: 0
            val newSize = newList.size

            Log.d(TAG, "updateMovieList 被调用: 新列表大小=$newSize, 旧列表大小=$oldSize")

            // 确保 beanList 已初始化
            if (beanList == null) {
                beanList = mutableListOf()
            }

            // 更新数据
            beanList?.clear()
            beanList?.addAll(newList)

            // 根据情况选择合适的更新方式
            when {
                oldList.isNullOrEmpty() || oldSize != newSize -> {
                    // 首次加载或列表大小变化，使用完整更新
                    adapter?.notifyDataSetChanged()
                    Log.d(TAG, "使用 notifyDataSetChanged() 更新")
                }
                else -> {
                    // 列表大小相同，使用范围更新（更高效）
                    adapter?.notifyItemRangeChanged(0, newSize)
                    Log.d(TAG, "使用 notifyItemRangeChanged() 更新")
                }
            }

            // 使用动画显示/隐藏空状态
            animateEmptyState(newList.isEmpty())

            Log.d(TAG, "列表更新完成: adapter=${adapter != null}, beanList大小=${beanList?.size}")
        } ?: Log.w(TAG, "updateMovieList: binding 为 null")
    }

    /**
     * 动画显示/隐藏空状态
     */
    private fun animateEmptyState(isEmpty: Boolean) {
        _binding?.let { binding ->
            if (isEmpty) {
                binding.emptyView.alpha = 0f
                binding.emptyView.setVisibleIf(true)
                binding.emptyView.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setInterpolator(android.view.animation.DecelerateInterpolator())
                    .start()
            } else {
                binding.emptyView.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        binding.emptyView.setVisibleIf(false)
                    }
                    .start()
            }
            // RecyclerView 始终可见，只是可能被空状态覆盖
            binding.recyclerView.setVisibleIf(true)
        }
    }

    /**
     * 处理 UI 状态
     * 优化 UI 反馈和用户体验
     */
    private fun handleUiState(state: UiState) {
        _binding?.let { binding ->
            when (state) {
                is UiState.Loading -> {
                    showLoadingState(binding, true)
                    hideErrorState(binding)
                }
                is UiState.Success -> {
                    showLoadingState(binding, false)
                    hideErrorState(binding)
                    // 只在首次加载时滚动到顶部，避免从其他页面返回时自动滚动
                    if (state.count > 0 && isFirstLoad) {
                        binding.recyclerView.post {
                            binding.recyclerView.smoothScrollToPosition(0)
                        }
                        isFirstLoad = false
                    }
                }
                is UiState.Error -> {
                    showLoadingState(binding, false)
                    showErrorState(binding, state.message)
                }
                is UiState.Empty -> {
                    showLoadingState(binding, false)
                    hideErrorState(binding)
                    showEmptyState(binding, "文件为空或不存在")
                }
            }
        }
    }

    /**
     * 显示加载状态
     */
    private fun showLoadingState(binding: FragmentMovieBinding, show: Boolean) {
        // 显示/隐藏加载指示器
        binding.progressBar.setVisibleIf(show)
        // 调整 RecyclerView 透明度
        binding.recyclerView.alpha = if (show) 0.3f else 1f
        binding.recyclerView.isEnabled = !show
        // 隐藏空状态视图
        if (show) {
            binding.emptyView.setVisibleIf(false)
        }
    }

    /**
     * 显示错误状态
     */
    private fun showErrorState(binding: FragmentMovieBinding, message: String) {
        Toast.makeText(context, "错误: $message", Toast.LENGTH_LONG).show()
        // 更新空状态文本
        val emptyText = binding.emptyView.findViewById<TextView>(R.id.emptyText)
        emptyText?.text = "加载失败: $message\n点击重试"
        // 设置点击重试
        binding.emptyView.setOnClickListener {
            val context = context ?: return@setOnClickListener
            viewModel.loadDataFromAssets(context)
        }
        animateEmptyState(true)
    }

    /**
     * 隐藏错误状态
     */
    private fun hideErrorState(binding: FragmentMovieBinding) {
        binding.emptyView.setOnClickListener(null)
    }

    /**
     * 显示空状态
     */
    private fun showEmptyState(binding: FragmentMovieBinding, message: String) {
        val emptyText = binding.emptyView.findViewById<TextView>(R.id.emptyText)
        emptyText?.text = message
        animateEmptyState(true)
    }

    /**
     * 更新筛选按钮状态
     */
    private fun updateFilterButtons(filterState: x.shei.viewmodel.FilterState) {
        _binding?.let { binding ->
            updateFilterButtonStyle(
                binding.btnFilterCountry,
                filterState.country,
                FILTER_COUNTRY_TEXT
            )
            updateFilterButtonStyle(
                binding.btnFilterYear,
                filterState.year,
                FILTER_YEAR_TEXT
            )
        }
    }


    private fun setupRecyclerView() {
        val recyclerView = binding.recyclerView
        recyclerView.apply {
            layoutManager = GridLayoutManager(activity, GRID_SPAN_COUNT)
            // 添加列表项间距（减小间距）
            val spacing = (3 * resources.displayMetrics.density).toInt()
            addItemDecoration(GridSpacingItemDecoration(GRID_SPAN_COUNT, spacing, true))

            // 优化列表动画
            itemAnimator = DefaultItemAnimator().apply {
                // 启用更改动画
                supportsChangeAnimations = true
                // 设置动画时长（更流畅的动画）
                changeDuration = 300
                addDuration = 350
                removeDuration = 300
                moveDuration = 300
            }

            // 优化性能：设置固定大小（如果列表项大小固定）
            setHasFixedSize(true)

            // 添加淡入效果和轻微缩放
            alpha = 0f
            scaleX = 0.98f
            scaleY = 0.98f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setStartDelay(150)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        }

        beanList = mutableListOf()
        adapter = VideoAdapter(activity, beanList,true)
        recyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        val searchView = binding.searchView

        // 设置搜索框样式（延迟执行，确保SearchView已完全初始化）
        searchView.post {
            val currentBinding = _binding ?: return@post
            val currentSearchView = currentBinding.searchView
            try {
                // 设置EditText的文字颜色和背景
                val searchEditTextId = currentSearchView.context.resources
                    .getIdentifier("android:id/search_src_text", null, null)
                if (searchEditTextId != 0) {
                    currentSearchView.findViewById<EditText>(searchEditTextId)?.apply {
                        // 设置文字颜色为深色，确保可见
                        setTextColor(0xFF333333.toInt())
                        // 设置提示文字颜色
                        setHintTextColor(0xFF999999.toInt())
                        // 移除背景，使用透明
                        background = null
                        setBackgroundResource(android.R.color.transparent)
                    }
                }
                // 移除search_plate的背景
                val searchPlateId = currentSearchView.context.resources
                    .getIdentifier("android:id/search_plate", null, null)
                if (searchPlateId != 0) {
                    currentSearchView.findViewById<View>(searchPlateId)?.apply {
                        background = null
                        setBackgroundResource(android.R.color.transparent)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "设置搜索框样式失败", e)
            }
        }

        // 设置搜索框不自动获取焦点
        searchView.apply {
            clearFocus()

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    clearFocus()
                    // 取消之前的搜索任务
                    searchJob?.cancel()
                    // 立即执行搜索
                    viewModel.updateKeyword(query ?: "")
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    // 取消之前的搜索任务（防抖优化）
                    searchJob?.cancel()
                    // 延迟执行搜索，减少频繁搜索
                    searchJob = viewLifecycleOwner.lifecycleScope.launch {
                        delay(300) // 300ms 防抖
                        viewModel.updateKeyword(newText ?: "")
                    }
                    return true
                }
            })
        }
    }

    private fun setupButtons() {
        with(binding) {
            // 添加按钮点击动画效果的辅助函数
            fun addClickAnimation(view: View, action: () -> Unit) {
                view.setOnClickListener {
                    // 添加点击缩放动画
                    it.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(100)
                        .withEndAction {
                            it.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start()
                        }
                        .start()
                    action()
                }
            }

            addClickAnimation(btnSort) { viewModel.reverseList() }
            addClickAnimation(btnFilterCountry) { showCountryFilterDialog() }
            addClickAnimation(btnFilterYear) { showYearFilterDialog() }
            addClickAnimation(btnReset) {
                viewModel.resetFilters()
                resetAllFiltersUI()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 取消搜索任务
        searchJob?.cancel()
        searchJob = null
        // viewLifecycleOwner.lifecycleScope 会自动取消，不需要手动取消
        _binding = null
    }

    private fun loadFile() {
        val context = context ?: return
        viewModel.loadDataFromAssets(context)
    }


    private var adapter: VideoAdapter? = null
    private var beanList: MutableList<Bean?>? = null

    // 搜索防抖任务
    private var searchJob: Job? = null

    /**
     * 检查列表是否为空并更新 UI
     * 使用内联函数优化
     */
    private inline fun checkEmpty() {
        val isEmpty = beanList.isNullOrEmpty()
        animateEmptyState(isEmpty)
    }


    /**
     * 重置所有筛选条件 UI（业务逻辑在 ViewModel 中）
     */
    private fun resetAllFiltersUI() {
        _binding?.let { binding ->
            // 重置按钮文本和样式
            with(binding) {
                updateFilterButtonStyle(btnFilterCountry, null, FILTER_COUNTRY_TEXT)
                updateFilterButtonStyle(btnFilterYear, null, FILTER_YEAR_TEXT)

                // 清空搜索框并清除焦点
                searchView.run {
                    setQuery("", false)
                    clearFocus()
                }
                btnFilterCountry.clearFocus()
                btnFilterYear.clearFocus()
            }
        }
    }

    /**
     * 更新筛选按钮的样式和文本
     */
    private fun updateFilterButtonStyle(
        button: Button,
        selectedValue: String?,
        defaultText: String
    ) {
        if (selectedValue == null) {
            // 未选中状态
            button.text = defaultText
            button.setTextColor(COLOR_TEXT_DARK)
            button.setBackgroundResource(R.drawable.btn_filter_background)
        } else {
            // 选中状态
            button.text = when {
                FILTER_COUNTRY_TEXT.contains("国家") -> selectedValue ?: "国家筛选"
                FILTER_YEAR_TEXT.contains("年份") -> {
                    // 如果是年份范围（包含-），只显示-前的年份
                    if (selectedValue?.contains("-") == true) {
                        val yearPrefix = selectedValue.substringBefore("-")
                        yearPrefix
                    } else {
                        selectedValue ?: "年份筛选"
                    }
                }
                else -> selectedValue
            }
            button.setTextColor(COLOR_TEXT_PRIMARY)
            button.setBackgroundResource(R.drawable.btn_filter_background_selected)
        }
    }


    /**
     * 显示国家筛选下拉菜单
     * 使用 ViewModel 管理数据
     */
    private fun showCountryFilterDialog() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 构建缓存
                viewModel.buildFilterOptionsCache()

                // 获取国家列表
                val countryList = viewModel.countryList.value?.toMutableList() ?: return@launch
                
                // 处理国家列表，分割包含"/"或","的选项，并合并重复项
                val processedCountryList = processCountryList(countryList)

                // 检查 binding 是否仍然有效
                _binding?.let { binding ->
                    val currentCountry = viewModel.filterState.value?.country
                    val listener: DropdownListener = { selectedValue ->
                        _binding?.let { b ->
                            val country = selectedValue?.takeUnless { it == FILTER_ALL }
                            viewModel.updateCountryFilter(country)
                            val keyword = b.searchView.getQueryText()
                            viewModel.updateKeyword(keyword)
                            b.btnFilterCountry.clearFocus()
                            b.searchView.clearFocus()
                        }
                    }

                    showDropdownPopup(
                        binding.btnFilterCountry,
                        processedCountryList.toMutableList(),
                        currentCountry,
                        listener
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "显示国家筛选对话框失败", e)
            }
        }
    }
    
    /**
     * 处理国家列表，分割包含"/"或","或空格的选项，并合并重复项
     */
    private fun processCountryList(originalList: List<String>): List<String> {
        val processedList = mutableListOf<String>()
        val seenCountries = mutableSetOf<String>()
        
        for (item in originalList) {
            if (item == FILTER_ALL) {
                // "全部"选项保持不变
                if (seenCountries.add(item)) {
                    processedList.add(item)
                }
            } else {
                // 检查是否包含"/"或","或空格分隔符
                val countries = when {
                    item.contains("/") -> item.split("/").map { it.trim() }
                    item.contains(",") -> item.split(",").map { it.trim() }
                    item.contains(" ") -> {
                        // 对于空格分隔，只取第一个
                        val parts = item.split(" ").map { it.trim() }
                        listOf(parts.firstOrNull()?.trim() ?: item.trim())
                    }
                    else -> {
                        // 如果没有分隔符，作为单个国家处理
                        listOf(item.trim())
                    }
                }
                
                // 添加去重后的国家到结果列表
                for (country in countries) {
                    if (country.isNotEmpty() && seenCountries.add(country)) {
                        processedList.add(country)
                    }
                }
            }
        }
        
        return processedList
    }

    /**
     * 显示年份筛选下拉菜单
     * 使用 ViewModel 管理数据
     */
    private fun showYearFilterDialog() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 构建缓存
                viewModel.buildFilterOptionsCache()
                
                // 等待年份列表更新
                var yearList = viewModel.yearList.value
                var attempts = 0
                val maxAttempts = 10 // 最多重试10次
                
                while ((yearList == null || yearList.isEmpty()) && attempts < maxAttempts) {
                    delay(50) // 等待50毫秒
                    yearList = viewModel.yearList.value
                    attempts++
                }
                
                // 获取年份列表
                val yearListFinal = yearList?.toMutableList() ?: return@launch
                
                // 将年份按5年一组进行分组
                val groupedYearList = groupYearsByFive(yearListFinal)

                // 检查 binding 是否仍然有效
                _binding?.let { binding ->
                    val currentYear = viewModel.filterState.value?.year
                    val listener: DropdownListener = { selectedValue ->
                        _binding?.let { b ->
                            val year = selectedValue?.takeUnless { it == FILTER_ALL }
                            viewModel.updateYearFilter(year)
                            val keyword = b.searchView.getQueryText()
                            viewModel.updateKeyword(keyword)
                            b.btnFilterYear.clearFocus()
                            b.searchView.clearFocus()
                        }
                    }

                    showDropdownPopup(
                        binding.btnFilterYear,
                        groupedYearList.toMutableList(),
                        currentYear,
                        listener
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "显示年份筛选对话框失败", e)
            }
        }
    }
    
    /**
     * 将年份按5年一组进行分组
     */
    private fun groupYearsByFive(yearList: List<String>): List<String> {
        if (yearList.isEmpty()) return emptyList()
        
        // 过滤掉 "全部" 选项和其他非年份项
        val allOption = yearList.firstOrNull { it == FILTER_ALL }
        val yearOptions = yearList.filter { it != FILTER_ALL }
        
        // 尝试将年份转换为整数并排序
        val sortedYears = yearOptions.mapNotNull { it.toIntOrNull() }.sortedDescending()
        
        if (sortedYears.isEmpty()) return yearList
        
        // 按5年一组进行分组
        val groupedYears = mutableListOf<String>()
        var i = 0
        while (i < sortedYears.size) {
            val startYear = sortedYears[i]
            
            // 找到与startYear相差不超过4年的连续年份（形成5年范围）
            var endYear = startYear
            var count = 1
            var j = i + 1
            while (j < sortedYears.size && count < 5 && startYear - sortedYears[j] <= 4) {
                endYear = sortedYears[j]
                count++
                j++
            }
            
            // 创建年份范围
            if (startYear == endYear) {
                groupedYears.add(startYear.toString())
            } else {
                groupedYears.add("$startYear-$endYear")
            }
            
            // 移动索引，跳过已分组的年份
            i += count
        }
        
        // 如果有 "全部" 选项，添加到列表开头
        return if (allOption != null) {
            mutableListOf(allOption).apply { addAll(groupedYears) }
        } else {
            groupedYears
        }
    }

    /**
     * 显示下拉选择器（类似 React Select）
     * 使用类型别名优化参数类型
     */
    private fun showDropdownPopup(
        anchorView: View,
        items: MutableList<String>,
        selectedValue: String?,
        listener: DropdownListener
    ) {
        activity ?: return
        val context = requireContext()
        val resources = context.resources
        val displayMetrics = resources.displayMetrics

        // 如果已有 PopupWindow 显示，先关闭
        popupWindow?.takeIf { it.isShowing }?.dismiss()

        // 创建 PopupWindow 的布局
        val popupView = LayoutInflater.from(context)
            .inflate(R.layout.popup_dropdown_list, null)
        val recyclerView = popupView.findViewById<RecyclerView>(R.id.recyclerView)

        // 设置 RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )

        // 创建适配器
        val adapter = DropdownAdapter(
            items,
            selectedValue
        ) { _, item ->
            listener(item)
            popupWindow?.dismiss()
        }
        recyclerView.adapter = adapter

        // 计算最大高度（不超过屏幕高度的指定比例）
        val maxHeight = (displayMetrics.heightPixels * POPUP_MAX_HEIGHT_RATIO).toInt()
        val itemHeight = (POPUP_ITEM_HEIGHT_DP * displayMetrics.density).toInt()
        val calculatedHeight = min(items.size * itemHeight, maxHeight)

        // 设置 RecyclerView 高度
        recyclerView.layoutParams = recyclerView.layoutParams.apply {
            height = calculatedHeight
        }

        // 计算下拉菜单宽度（比按钮宽，确保文本完整显示）
        val buttonWidth = anchorView.width
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        // 最小宽度为按钮宽度的指定倍数，最大不超过屏幕宽度的指定比例
        val minWidth = (buttonWidth * POPUP_MIN_WIDTH_RATIO).toInt()
        val maxWidth = (screenWidth * POPUP_MAX_WIDTH_RATIO).toInt()
        val popupWidth = max(minWidth, min(maxWidth, (buttonWidth * POPUP_MAX_WIDTH_MULTIPLIER).toInt()))

        // 创建 PopupWindow
        popupWindow = PopupWindow(popupView, popupWidth, calculatedHeight, true).apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            elevation = 12f
            isOutsideTouchable = true
            isFocusable = true
            // 添加淡入动画
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                animationStyle = android.R.style.Animation_Dialog
            }
        }

        // 计算显示位置（在按钮下方，居中对齐）
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)
        var x = location[0]
        // 如果下拉菜单宽度大于按钮，可以稍微左移以居中显示
        if (popupWidth > buttonWidth) {
            x = location[0] - (popupWidth - buttonWidth) / 2
            // 确保不会超出屏幕边界
            x = max(0, min(x, screenWidth - popupWidth))
        }
        var y = location[1] + anchorView.height

        // 如果下拉菜单超出屏幕底部，则显示在按钮上方
        if (y + calculatedHeight > screenHeight) {
            y = location[1] - calculatedHeight
        }

        // 显示 PopupWindow
        popupWindow?.showAtLocation(anchorView, Gravity.NO_GRAVITY, x, y)
    }

    private var popupWindow: PopupWindow? = null

    /**
     * 下拉选择器适配器
     */
    private class DropdownAdapter(
        private val items: List<String>,
        private val selectedValue: String?,
        private val listener: (Int, String?) -> Unit
    ) : RecyclerView.Adapter<DropdownAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_dropdown_option, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.textView.text = item

            // 高亮选中的项
            val isSelected = item == selectedValue || (selectedValue == null && item == MovieFragmentNew.FILTER_ALL)
            if (isSelected) {
                holder.textView.setTextColor(MovieFragmentNew.COLOR_TEXT_SELECTED)
                holder.textView.setBackgroundColor(MovieFragmentNew.COLOR_BG_SELECTED)
                holder.textView.textSize = 15.5f
                holder.textView.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                holder.textView.setTextColor(MovieFragmentNew.COLOR_TEXT_DARK)
                holder.textView.setBackgroundColor(MovieFragmentNew.COLOR_BG_WHITE)
                holder.textView.textSize = 15f
                holder.textView.setTypeface(null, android.graphics.Typeface.NORMAL)
            }

            holder.itemView.setOnClickListener {
                listener(position, item)
            }
        }

        override fun getItemCount() = items.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView as TextView
        }
    }

    internal interface OnItemSelectedListener {
        fun onItemSelected(selectedValue: String?)
    }

    // 不能删除
    fun copy(v: View?) {
    }

    // 不能删除
    @RequiresApi(api = Build.VERSION_CODES.N)
    fun takeScreenshot(v: View?) {
    }

    // 不能删除
    fun kuaijin(v: View?) {
    }

    // 不能删除
    fun download(v: View?) {
    }

    // 不能删除
    fun favorite(v: View?) {
    }

    companion object {
        private const val TAG = "MovieParser"
        private const val GRID_SPAN_COUNT = 4
        private const val GRID_SPACING_DP = 2
        private const val FILE_NAME_MOVIE2 = "movie2.json"

        // 数据量阈值：超过此值使用后台线程处理
        private const val LARGE_DATA_THRESHOLD = 100

        // 年份范围
        private const val MIN_YEAR = 1900
        private const val MAX_YEAR = 2100

        // PopupWindow 尺寸比例
        private const val POPUP_MAX_HEIGHT_RATIO = 0.6f
        private const val POPUP_MAX_WIDTH_RATIO = 0.8f
        private const val POPUP_MIN_WIDTH_RATIO = 1.5f
        private const val POPUP_MAX_WIDTH_MULTIPLIER = 2.0f
        private const val POPUP_ITEM_HEIGHT_DP = 56

        // 颜色常量
        private const val COLOR_TEXT_DARK = 0xFF333333.toInt()
        private const val COLOR_TEXT_PRIMARY = 0xFF189DF4.toInt()
        private const val COLOR_TEXT_SELECTED = 0xFF1976D2.toInt()
        private const val COLOR_BG_SELECTED = 0xFFE3F2FD.toInt()
        private const val COLOR_BG_WHITE = 0xFFFFFFFF.toInt()

        // 筛选选项默认文本
        private const val FILTER_ALL = "全部"
        private const val FILTER_COUNTRY_TEXT = "国家筛选"
        private const val FILTER_YEAR_TEXT = "年份筛选"

        // 年份正则表达式
        private val YEAR_PATTERN = Pattern.compile("\\d{4}")
    }
}
