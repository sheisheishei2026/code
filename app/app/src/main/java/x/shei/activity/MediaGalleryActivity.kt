package x.shei.activity

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import x.shei.R
import x.shei.adapter.MediaGroupAdapter
import x.shei.adapter.MediaGridAdapter
import x.shei.util.ImmersedUtil
import java.io.File

class MediaGalleryActivity : BaseActivity() {
    companion object {
        private const val REQUEST_CODE_SELECT_DIRECTORY = 1001
        private const val PREFS_NAME = "media_gallery_prefs"
        private const val KEY_DIRECTORY_URI = "selected_directory_uri"
        private const val KEY_DIRECTORY_PATH = "selected_directory_path"
        private const val FIRST_BATCH_SIZE = 20
        private const val BATCH_SIZE = 20
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnChangeDirectory: View
    private lateinit var loadingLayout: FrameLayout
    private var selectedDirectoryUri: Uri? = null
    private var selectedDirectory: File? = null

    private var groupAdapter: MediaGroupAdapter? = null
    private var gridAdapter: MediaGridAdapter? = null

    private val mediaGroups = mutableListOf<MediaGroup>()
    private val mediaFiles = mutableListOf<MediaItem>()
    private var hasSubDirectories = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_gallery)
        ImmersedUtil.setImmersedMode(this, false)

        initViews()
        setupClickListeners()
        loadSavedDirectory()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        btnChangeDirectory = findViewById(R.id.btnChangeDirectory)
        loadingLayout = findViewById(R.id.loadingLayout)
    }

    private fun setupClickListeners() {
        btnChangeDirectory.setOnClickListener { selectDirectory() }
    }

    private fun showLoading() {
        loadingLayout.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        loadingLayout.visibility = View.GONE
    }

    private fun loadSavedDirectory() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedUri = prefs.getString(KEY_DIRECTORY_URI, null)
        val savedPath = prefs.getString(KEY_DIRECTORY_PATH, null)

        if (savedUri != null) {
            try {
                selectedDirectoryUri = Uri.parse(savedUri)
                // 恢复持久化权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && selectedDirectoryUri != null) {
                    try {
                        contentResolver.takePersistableUriPermission(
                            selectedDirectoryUri!!,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    } catch (e: SecurityException) {
                        // 权限可能已失效，需要重新选择
                        selectedDirectoryUri = null
                    }
                }
            } catch (e: Exception) {
                selectedDirectoryUri = null
            }
        }

        if (savedPath != null && selectedDirectoryUri == null) {
            selectedDirectory = File(savedPath)
            if (!selectedDirectory!!.exists()) {
                selectedDirectory = null
            }
        }

        if (selectedDirectoryUri != null || (selectedDirectory != null && selectedDirectory!!.exists())) {
            recyclerView.postDelayed({ scanDirectory() }, 100)
        }
    }

    private fun saveDirectory() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = prefs.edit()

        if (selectedDirectoryUri != null) {
            editor.putString(KEY_DIRECTORY_URI, selectedDirectoryUri.toString())
        } else {
            editor.remove(KEY_DIRECTORY_URI)
        }

        if (selectedDirectory != null) {
            editor.putString(KEY_DIRECTORY_PATH, selectedDirectory!!.absolutePath)
        } else {
            editor.remove(KEY_DIRECTORY_PATH)
        }

        editor.apply()
    }

    private fun selectDirectory() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            startActivityForResult(intent, REQUEST_CODE_SELECT_DIRECTORY)
        } else {
            Toast.makeText(this, "请使用Android 5.0以上版本", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_DIRECTORY && resultCode == RESULT_OK && data != null) {
            // 清除旧数据
            mediaGroups.clear()
            mediaFiles.clear()
            groupAdapter?.updateData(emptyList())
            gridAdapter?.updateData(emptyList())

            selectedDirectoryUri = data.data
            if (selectedDirectoryUri != null) {
                // 获取持久化权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    try {
                        contentResolver.takePersistableUriPermission(
                            selectedDirectoryUri!!,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // 转换为File对象（如果可能）
                val path = getPathFromUri(selectedDirectoryUri!!)
                selectedDirectory = if (path != null) File(path) else null

                // 保存新目录并立即扫描
                saveDirectory()
                scanDirectory()
            }
        }
    }

    private fun getPathFromUri(uri: Uri): String? {
        if (DocumentsContract.isDocumentUri(this, uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            if ("primary".equals(uri.authority, ignoreCase = true)) {
                return Environment.getExternalStorageDirectory().toString() + "/" + docId.split(":")[1]
            }
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    private fun scanDirectory() {
        showLoading()
        lifecycleScope.launch {
            try {
                mediaGroups.clear()
                mediaFiles.clear()
                hasSubDirectories = false

                if (selectedDirectoryUri != null) {
                    scanDirectoryWithUriOptimized(selectedDirectoryUri!!)
                } else if (selectedDirectory != null && selectedDirectory!!.exists()) {
                    scanDirectoryWithFileOptimized(selectedDirectory!!)
                } else {
                    Toast.makeText(this@MediaGalleryActivity, "无法访问所选目录", Toast.LENGTH_SHORT).show()
                    hideLoading()
                }

                // 如果没有文件，隐藏loading
                if (mediaFiles.isEmpty() && mediaGroups.isEmpty()) {
                    hideLoading()
                }
            } catch (e: Exception) {
                e.printStackTrace()
//                Toast.makeText(this@MediaGalleryActivity, "扫描目录出错: ${e.message}", Toast.LENGTH_SHORT).show()
                hideLoading()
            }
        }
    }

    private suspend fun scanDirectoryWithUriOptimized(directoryUri: Uri) = withContext(Dispatchers.IO) {
        val documentFile = DocumentFile.fromTreeUri(this@MediaGalleryActivity, directoryUri)
        if (documentFile == null || !documentFile.exists()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MediaGalleryActivity, "无法访问所选目录", Toast.LENGTH_SHORT).show()
            }
            return@withContext
        }

        val groupMap = mutableMapOf<String, MutableList<MediaItem>>()
        val directFiles = mutableListOf<MediaItem>()
        val subDirectories = mutableListOf<DocumentFile>()

        val files = documentFile.listFiles()
        if (files == null) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MediaGalleryActivity, "目录为空或无法读取", Toast.LENGTH_SHORT).show()
            }
            return@withContext
        }

        // 第一步：边扫描边显示，一旦收集到20个就立即显示
        val firstBatch = mutableListOf<MediaItem>()
        var hasShownFirstBatch = false

        for (file in files) {
            val fileName = file.name
            if (fileName == null || isHiddenFile(fileName)) {
                continue
            }

            if (file.isDirectory) {
                subDirectories.add(file)
            } else if (isMediaFile(fileName)) {
                val mediaItem = MediaItem(file.uri, fileName)
                directFiles.add(mediaItem)

                // 如果还没有显示第一批，且收集到20个，立即显示
                if (!hasShownFirstBatch && subDirectories.isEmpty()) {
                    firstBatch.add(mediaItem)

                    if (firstBatch.size >= FIRST_BATCH_SIZE) {
                        // 立即显示第一批
                        withContext(Dispatchers.Main) {
                            mediaFiles.clear()
                            mediaFiles.addAll(firstBatch)

                            if (gridAdapter == null) {
                                gridAdapter = MediaGridAdapter(this@MediaGalleryActivity, mutableListOf())
                                val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                                recyclerView.layoutManager = layoutManager
                                recyclerView.adapter = gridAdapter
                            }

                            gridAdapter!!.setInitialData(mediaFiles.toMutableList())
                            hideLoading() // 立即隐藏loading
                        }
                        hasShownFirstBatch = true
                    }
                }
            }
        }

        // 如果有直接文件但没有二级目录
        if (directFiles.isNotEmpty() && subDirectories.isEmpty()) {
            // 如果还没有显示第一批（文件少于20个），现在显示
            if (!hasShownFirstBatch) {
                withContext(Dispatchers.Main) {
                    mediaFiles.clear()
                    mediaFiles.addAll(directFiles)

                    if (gridAdapter == null) {
                        gridAdapter = MediaGridAdapter(this@MediaGalleryActivity, mutableListOf())
                        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                        recyclerView.layoutManager = layoutManager
                        recyclerView.adapter = gridAdapter
                    }

                    gridAdapter!!.setInitialData(mediaFiles.toMutableList())
                    hideLoading() // 立即隐藏loading
                }
            } else {
                // 已经显示了第一批，继续增量加载剩余文件
                if (directFiles.size > FIRST_BATCH_SIZE) {
                    val remainingFiles = directFiles.subList(FIRST_BATCH_SIZE, directFiles.size).toMutableList()

                    // 分批增量加载剩余文件
                    for (i in remainingFiles.indices step BATCH_SIZE) {
                        val end = (i + BATCH_SIZE).coerceAtMost(remainingFiles.size)
                        val batch = remainingFiles.subList(i, end).toMutableList()

                        withContext(Dispatchers.Main) {
                            // 增量添加，使用addItems避免闪烁
                            mediaFiles.addAll(batch)
                            gridAdapter!!.addItems(batch)
                        }

                        // 短暂延迟，避免UI更新过于频繁
                        if (end < remainingFiles.size) {
                            delay(100)
                        }
                    }
                }
            }
        }

        // 第二步：扫描二级目录（如果有），边扫描边显示
        if (subDirectories.isNotEmpty()) {
            hasSubDirectories = true
            var hasShownFirstGroup = false

            for (subDir in subDirectories) {
                val dirName = subDir.name ?: continue
                val dirFiles = scanSubDirectory(subDir)
                if (dirFiles.isNotEmpty()) {
                    groupMap[dirName] = dirFiles

                    // 扫描到第一个有内容的目录就立即显示
                    if (!hasShownFirstGroup) {
                        withContext(Dispatchers.Main) {
                            mediaGroups.clear()
                            for ((name, files) in groupMap) {
                                mediaGroups.add(MediaGroup(name, ArrayList(files)))
                            }
                            // 不排序以提升性能
                            setupRecyclerView()
                            hideLoading() // 立即隐藏loading
                        }
                        hasShownFirstGroup = true
                    } else {
                        // 增量添加新目录，不排序以提升性能
                        withContext(Dispatchers.Main) {
                            mediaGroups.add(MediaGroup(dirName, ArrayList(dirFiles)))
                            groupAdapter?.notifyDataSetChanged()
                        }
                    }
                }
            }

            // 如果所有目录都扫描完了但还没有显示（所有目录都为空），隐藏loading
            if (!hasShownFirstGroup) {
                withContext(Dispatchers.Main) {
                    hideLoading()
                }
            }
        }
    }

    private suspend fun scanSubDirectory(directory: DocumentFile): MutableList<MediaItem> = withContext(Dispatchers.IO) {
        val files = mutableListOf<MediaItem>()
        val dirFiles = directory.listFiles() ?: return@withContext files

        for (file in dirFiles) {
            val fileName = file.name
            // 跳过隐藏文件和文件夹
            if (fileName == null || isHiddenFile(fileName) || file.isDirectory) {
                continue
            }

            // 快速检查文件扩展名
            if (isMediaFile(fileName)) {
                files.add(MediaItem(file.uri, fileName))
            }
        }
        // 不排序，直接返回以提升性能
        files
    }

    private suspend fun scanDirectoryWithFileOptimized(directory: File) = withContext(Dispatchers.IO) {
        if (!directory.exists() || !directory.isDirectory) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MediaGalleryActivity, "无法访问所选目录", Toast.LENGTH_SHORT).show()
            }
            return@withContext
        }

        val groupMap = mutableMapOf<String, MutableList<MediaItem>>()
        val directFiles = mutableListOf<MediaItem>()
        val subDirectories = mutableListOf<File>()

        val files = directory.listFiles()
        if (files == null) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MediaGalleryActivity, "目录为空或无法读取", Toast.LENGTH_SHORT).show()
            }
            return@withContext
        }

        // 第一步：边扫描边显示，一旦收集到20个就立即显示
        val firstBatch = mutableListOf<MediaItem>()
        var hasShownFirstBatch = false

        for (file in files) {
            val fileName = file.name
            if (fileName == null || isHiddenFile(fileName)) {
                continue
            }

            if (file.isDirectory) {
                subDirectories.add(file)
            } else if (isMediaFile(fileName)) {
                val mediaItem = MediaItem(file)
                directFiles.add(mediaItem)

                // 如果还没有显示第一批，且收集到20个，立即显示
                if (!hasShownFirstBatch && subDirectories.isEmpty()) {
                    firstBatch.add(mediaItem)

                    if (firstBatch.size >= FIRST_BATCH_SIZE) {
                        // 立即显示第一批
                        withContext(Dispatchers.Main) {
                            mediaFiles.clear()
                            mediaFiles.addAll(firstBatch)

                            if (gridAdapter == null) {
                                gridAdapter = MediaGridAdapter(this@MediaGalleryActivity, mutableListOf())
                                val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                                recyclerView.layoutManager = layoutManager
                                recyclerView.adapter = gridAdapter
                            }

                            gridAdapter!!.setInitialData(mediaFiles.toMutableList())
                            hideLoading() // 立即隐藏loading
                        }
                        hasShownFirstBatch = true
                    }
                }
            }
        }

        // 如果有直接文件但没有二级目录
        if (directFiles.isNotEmpty() && subDirectories.isEmpty()) {
            // 如果还没有显示第一批（文件少于20个），现在显示
            if (!hasShownFirstBatch) {
                withContext(Dispatchers.Main) {
                    mediaFiles.clear()
                    mediaFiles.addAll(directFiles)

                    if (gridAdapter == null) {
                        gridAdapter = MediaGridAdapter(this@MediaGalleryActivity, mutableListOf())
                        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                        recyclerView.layoutManager = layoutManager
                        recyclerView.adapter = gridAdapter
                    }

                    gridAdapter!!.setInitialData(mediaFiles.toMutableList())
                    hideLoading() // 立即隐藏loading
                }
            } else {
                // 已经显示了第一批，继续增量加载剩余文件
                if (directFiles.size > FIRST_BATCH_SIZE) {
                    val remainingFiles = directFiles.subList(FIRST_BATCH_SIZE, directFiles.size).toMutableList()

                    // 分批增量加载剩余文件
                    for (i in remainingFiles.indices step BATCH_SIZE) {
                        val end = (i + BATCH_SIZE).coerceAtMost(remainingFiles.size)
                        val batch = remainingFiles.subList(i, end).toMutableList()

                        withContext(Dispatchers.Main) {
                            // 增量添加，使用addItems避免闪烁
                            mediaFiles.addAll(batch)
                            gridAdapter!!.addItems(batch)
                        }

                        // 短暂延迟，避免UI更新过于频繁
                        if (end < remainingFiles.size) {
                            delay(100)
                        }
                    }
                }
            }
        }

        // 第二步：扫描二级目录（如果有），边扫描边显示
        if (subDirectories.isNotEmpty()) {
            hasSubDirectories = true
            var hasShownFirstGroup = false

            for (subDir in subDirectories) {
                val dirName = subDir.name
                val dirFiles = scanSubDirectory(subDir)
                if (dirFiles.isNotEmpty()) {
                    groupMap[dirName] = dirFiles

                    // 扫描到第一个有内容的目录就立即显示
                    if (!hasShownFirstGroup) {
                        withContext(Dispatchers.Main) {
                            mediaGroups.clear()
                            for ((name, files) in groupMap) {
                                mediaGroups.add(MediaGroup(name, ArrayList(files)))
                            }
                            // 不排序以提升性能
                            setupRecyclerView()
                            hideLoading() // 立即隐藏loading
                        }
                        hasShownFirstGroup = true
                    } else {
                        // 增量添加新目录，不排序以提升性能
                        withContext(Dispatchers.Main) {
                            mediaGroups.add(MediaGroup(dirName, ArrayList(dirFiles)))
                            groupAdapter?.notifyDataSetChanged()
                        }
                    }
                }
            }

            // 如果所有目录都扫描完了但还没有显示（所有目录都为空），隐藏loading
            if (!hasShownFirstGroup) {
                withContext(Dispatchers.Main) {
                    hideLoading()
                }
            }
        }
    }

    private suspend fun scanSubDirectory(directory: File): MutableList<MediaItem> = withContext(Dispatchers.IO) {
        val files = mutableListOf<MediaItem>()
        val dirFiles = directory.listFiles() ?: return@withContext files

        for (file in dirFiles) {
            val fileName = file.name
            // 跳过隐藏文件和文件夹
            if (fileName == null || isHiddenFile(fileName) || file.isDirectory) {
                continue
            }

            // 快速检查文件扩展名
            if (isMediaFile(fileName)) {
                files.add(MediaItem(file))
            }
        }
        // 不排序，直接返回以提升性能
        files
    }

    private fun isMediaFile(fileName: String): Boolean {
        if (fileName.length < 4) return false
        // 优化：使用charAt检查最后一个字符，避免创建新字符串
        val len = fileName.length
        val c = fileName[len - 1].lowercaseChar()
        if (c != 'g' && c != 'p' && c != '4' && c != 'i' && c != 'v' && c != 'k') {
            return false
        }
        // 只对可能的扩展名进行完整检查
        val lowerName = fileName.lowercase()
        return lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") ||
               lowerName.endsWith(".png") || lowerName.endsWith(".gif") ||
               lowerName.endsWith(".bmp") || lowerName.endsWith(".webp") ||
               lowerName.endsWith(".mp4") || lowerName.endsWith(".avi") ||
               lowerName.endsWith(".mov") || lowerName.endsWith(".mkv") ||
               lowerName.endsWith(".3gp") || lowerName.endsWith(".wmv")
    }

    private fun isHiddenFile(fileName: String): Boolean {
        return fileName.isNotEmpty() && fileName[0] == '.'
    }

    private fun setupRecyclerView() {
        if (hasSubDirectories) {
            // 有二级目录：使用分组布局
            if (groupAdapter == null) {
                groupAdapter = MediaGroupAdapter(this, mediaGroups)
                recyclerView.layoutManager = LinearLayoutManager(this)
                recyclerView.adapter = groupAdapter
            } else {
                groupAdapter!!.updateData(mediaGroups)
            }
        } else {
            // 无二级目录：使用瀑布流布局
            if (gridAdapter == null) {
                gridAdapter = MediaGridAdapter(this, mediaFiles)
                val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                recyclerView.layoutManager = layoutManager
                recyclerView.adapter = gridAdapter
            } else {
                gridAdapter!!.updateData(mediaFiles)
            }
        }
    }

    class MediaGroup(
        @JvmField var name: String,
        @JvmField var files: MutableList<MediaItem>
    )
}

