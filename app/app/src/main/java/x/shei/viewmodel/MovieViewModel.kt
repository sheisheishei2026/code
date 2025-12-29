package x.shei.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import kotlinx.coroutines.ensureActive
import x.shei.db.Bean
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.Collections
import java.util.Locale
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.MutableList

/**
 * 筛选状态数据类
 */
data class FilterState(
    val country: String? = null,
    val year: String? = null
) {
    val isEmpty: Boolean get() = country == null && year == null
}

/**
 * UI 状态
 */
sealed class UiState {
    object Loading : UiState()
    data class Success(val count: Int) : UiState()
    data class Error(val message: String) : UiState()
    object Empty : UiState()
}

/**
 * MovieViewModel - 管理电影列表数据和业务逻辑
 */
class MovieViewModel : ViewModel() {

    companion object {
        private const val TAG = "MovieViewModel"
        private const val FILE_NAME_MOVIE2 = "movie2.json"

        // 数据量阈值：超过此值使用后台线程处理
        private const val LARGE_DATA_THRESHOLD = 100

        // 年份范围
        private const val MIN_YEAR = 1900
        private const val MAX_YEAR = 2100

        // 年份正则表达式
        private val YEAR_PATTERN = Pattern.compile("\\d{4}")
    }

    // 原始数据列表
    private val _allMovieList = MutableLiveData<List<Bean>>()
    val allMovieList: LiveData<List<Bean>> = _allMovieList

    // 筛选后的数据列表
    private val _filteredMovieList = MutableLiveData<List<Bean>>()
    val filteredMovieList: LiveData<List<Bean>> = _filteredMovieList

    // UI 状态
    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState

    // 筛选状态
    private val _filterState = MutableLiveData<FilterState>()
    val filterState: LiveData<FilterState> = _filterState

    // 国家列表缓存
    private val _countryList = MutableLiveData<List<String>>()
    val countryList: LiveData<List<String>> = _countryList

    // 年份列表缓存
    private val _yearList = MutableLiveData<List<String>>()
    val yearList: LiveData<List<String>> = _yearList

    // 内部数据
    private val allBeans = mutableListOf<Bean>()
    private var cachedCountryList: MutableList<String>? = null
    private var cachedYearList: MutableList<String>? = null

    // 复用 Gson 实例
    private val gson = Gson()

    // 使用 SupervisorJob 确保一个协程失败不影响其他协程
    private val customScope = viewModelScope + SupervisorJob()

    init {
        _filterState.value = FilterState()
        _filteredMovieList.value = emptyList()
    }

    /**
     * 从 assets 加载数据
     */
    fun loadDataFromAssets(context: Context) {
        customScope.launch {
            _uiState.value = UiState.Loading
            try {
                val movieList = withContext(Dispatchers.IO) {
                    if (!isActive) return@withContext emptyList<Bean>()
                    readDataFromAssets(context, FILE_NAME_MOVIE2)
                }

                if (!isActive) return@launch

                when {
                    movieList.isEmpty() -> {
                        _uiState.value = UiState.Empty
                        _allMovieList.value = emptyList()
                    }
                    else -> {
                        allBeans.clear()
                        allBeans.addAll(movieList)
                        _allMovieList.value = movieList.toList()
                        _uiState.value = UiState.Success(movieList.size)

                        // 清除缓存，需要重新构建
                        invalidateCache()

                        // 应用当前筛选条件（使用当前筛选状态）
                        val currentFilter = _filterState.value ?: FilterState()
                        // 获取当前搜索关键词（如果有的话）
                        applyFilter("", currentFilter.country, currentFilter.year)

                        Log.d(TAG, "从 $FILE_NAME_MOVIE2 加载了 ${movieList.size} 条数据")
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "加载 $FILE_NAME_MOVIE2 失败", e)
                _uiState.value = UiState.Error(e.message ?: "未知错误")
            }
        }
    }

    /**
     * 从 assets 目录读取 JSON 文件
     */
    private suspend fun readDataFromAssets(context: Context, fileName: String): List<Bean> = withContext(Dispatchers.IO) {
        try {
            context.assets.open(fileName).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
                    val json = reader.readText()
                    val listType = object : TypeToken<List<Bean>>() {}.type
                    gson.fromJson<List<Bean>>(json, listType) ?: emptyList()
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "读取 $fileName 失败", e)
            emptyList()
        }
    }

    /**
     * 应用筛选条件
     */
    fun applyFilter(keyword: String = "", country: String? = null, year: String? = null) {
        // 更新筛选状态
        val newFilterState = FilterState(country, year)
        _filterState.value = newFilterState

        val lowerKeyword = keyword.lowercase(Locale.getDefault()).takeIf { it.isNotEmpty() }.orEmpty()

        customScope.launch {
            try {
                val filteredList = if (allBeans.size > LARGE_DATA_THRESHOLD) {
                    withContext(Dispatchers.Default) {
                        if (!isActive) return@withContext emptyList<Bean>()
                        allBeans.filter { matchesFilter(it, newFilterState, lowerKeyword) }
                    }
                } else {
                    allBeans.filter { matchesFilter(it, newFilterState, lowerKeyword) }
                }

                if (isActive) {
                    _filteredMovieList.value = filteredList
                    Log.d(TAG, "筛选完成，结果数量: ${filteredList.size}, 前3项: ${filteredList.take(3).map { it.title }}")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "筛选数据失败", e)
            }
        }
    }

    /**
     * 更新筛选关键词
     */
    fun updateKeyword(keyword: String) {
        val currentFilter = _filterState.value ?: FilterState()
        applyFilter(keyword, currentFilter.country, currentFilter.year)
    }

    /**
     * 更新国家筛选
     */
    fun updateCountryFilter(country: String?) {
        val currentFilter = _filterState.value ?: FilterState()
        applyFilter("", country, currentFilter.year)
    }

    /**
     * 更新年份筛选
     */
    fun updateYearFilter(year: String?) {
        val currentFilter = _filterState.value ?: FilterState()
        applyFilter("", currentFilter.country, year)
    }

    /**
     * 重置所有筛选条件
     */
    fun resetFilters() {
        _filterState.value = FilterState()
        applyFilter("", null, null)
    }

    /**
     * 检查 bean 是否匹配筛选条件
     */
    private fun matchesFilter(
        bean: Bean,
        filter: FilterState,
        keyword: String
    ): Boolean {
        // 国家筛选（完全匹配）
        val countryMatch = filter.country?.let { bean.country == it } ?: true

        // 年份筛选（支持单个年份或年份范围，如 "2020-2024"）
        val yearMatch = filter.year?.let { yearFilter ->
            if (yearFilter.contains("-")) {
                // 如果是年份范围格式，例如 "2020-2024"
                val yearRange = yearFilter.split("-")
                if (yearRange.size == 2) {
                    val startYear = yearRange[0].toIntOrNull()
                    val endYear = yearRange[1].toIntOrNull()
                    if (startYear != null && endYear != null) {
                        // 检查bean的年份是否在范围内
                        val beanYears = extractYears(bean.updateTime)
                        beanYears.any { year ->
                            year.toIntOrNull()?.let { it in kotlin.math.min(startYear, endYear)..kotlin.math.max(startYear, endYear) } ?: false
                        }
                    } else {
                        // 如果范围解析失败，回退到包含匹配
                        bean.updateTime?.contains(yearFilter) == true
                    }
                } else {
                    // 如果格式不是范围，回退到包含匹配
                    bean.updateTime?.contains(yearFilter) == true
                }
            } else {
                // 单个年份，使用包含匹配
                bean.updateTime?.contains(yearFilter) == true
            }
        } ?: true

        // 关键词搜索 - 使用序列优化性能
        val keywordMatch = keyword.isEmpty() || bean.getSearchableFields()
            .asSequence()
            .map { it.lowercase(Locale.getDefault()) }
            .any { it.contains(keyword) }

        return countryMatch && yearMatch && keywordMatch
    }

    /**
     * 获取 Bean 的可搜索字段列表
     */
    private fun Bean.getSearchableFields(): List<String> = listOfNotNull(
        title,
        mainActor,
        director,
        country,
        updateTime,
        intro
    )

    /**
     * 构建筛选选项缓存
     */
    suspend fun buildFilterOptionsCache() {
        // 检查协程是否被取消，如果被取消会抛出 CancellationException
        // 在 suspend 函数中，coroutineContext 是隐式可用的
        coroutineContext.ensureActive()

        if (cachedCountryList == null) {
            cachedCountryList = buildCountryList()
            cachedCountryList?.let { _countryList.postValue(it) }
        }

        // 再次检查协程是否被取消
        coroutineContext.ensureActive()

        if (cachedYearList == null) {
            cachedYearList = buildYearList()
            cachedYearList?.let { _yearList.postValue(it) }
        }
    }

    /**
     * 构建国家列表
     */
    private suspend fun buildCountryList(): MutableList<String> {
        val buildFunction: () -> MutableList<String> = {
            allBeans
                .asSequence()
                .mapNotNull { it.country?.trim() }
                .filter { it.isNotEmpty() }
                .toSortedSet()
                .let { countrySet ->
                    mutableListOf<String>("全部").apply {
                        addAll(countrySet)
                    }
                }
        }

        return if (allBeans.size > LARGE_DATA_THRESHOLD) {
            withContext(Dispatchers.Default) {
                if (!isActive) return@withContext mutableListOf("全部")
                buildFunction()
            }
        } else {
            buildFunction()
        }
    }

    /**
     * 构建年份列表
     */
    private suspend fun buildYearList(): MutableList<String> {
        val buildFunction: () -> MutableList<String> = {
            allBeans
                .asSequence()
                .mapNotNull { it.updateTime }
                .flatMap { extractYears(it) }
                .toSortedSet(Collections.reverseOrder())
                .let { yearSet ->
                    mutableListOf<String>("全部").apply {
                        addAll(yearSet)
                    }
                }
        }

        return if (allBeans.size > LARGE_DATA_THRESHOLD) {
            withContext(Dispatchers.Default) {
                if (!isActive) return@withContext mutableListOf("全部")
                buildFunction()
            }
        } else {
            buildFunction()
        }
    }

    /**
     * 从updateTime中提取所有可能的年份（4位数字）
     */
    private fun extractYears(updateTime: String?): Set<String> {
        if (updateTime.isNullOrEmpty()) return emptySet()

        return YEAR_PATTERN.matcher(updateTime)
            .let { matcher ->
                generateSequence { if (matcher.find()) matcher.group() else null }
                    .mapNotNull { year ->
                        year.toIntOrNull()?.takeIf { it in MIN_YEAR..MAX_YEAR }?.toString()
                    }
                    .toSet()
            }
    }

    /**
     * 使缓存失效
     */
    private fun invalidateCache() {
        cachedCountryList = null
        cachedYearList = null
    }

    /**
     * 倒序排列当前列表
     */
    fun reverseList() {
        val currentList = _filteredMovieList.value?.toMutableList() ?: return
        if (currentList.isNotEmpty()) {
            Collections.reverse(currentList)
            _filteredMovieList.value = currentList
        }
    }

    override fun onCleared() {
        super.onCleared()
        // viewModelScope 会自动取消，不需要手动处理
    }
}

