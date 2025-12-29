package x.shei.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow
import androidx.core.net.toUri

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: android.graphics.drawable.Drawable?,
    val versionName: String,
    val versionCode: Long,
    val size: Long,
    val sourceDir: String
)

// 扩展函数：简化 Intent 创建
private fun Context.createIntent(action: String, uri: String): Intent =
    Intent(action).apply {
        data = uri.toUri()
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

// 扩展函数：安全启动 Activity
private fun Context.startActivitySafely(intent: Intent, errorMessage: String) {
    runCatching {
        startActivity(intent)
    }.onFailure {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }
}

class AppsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupImmersiveStatusBar()
        setContent {
            AppsScreen()
        }
    }

    private fun setupImmersiveStatusBar() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        // 使用新的 WindowInsetsController API 设置状态栏图标颜色
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true // 状态栏图标为深色
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen() {
    val context = LocalContext.current
    val view = LocalView.current
    var apps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showSystemApps by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 设置状态栏图标颜色为深色（因为背景是浅色）
    LaunchedEffect(Unit) {
        (view.context as? android.app.Activity)?.window?.let { window ->
            WindowInsetsControllerCompat(window, window.decorView).apply {
                isAppearanceLightStatusBars = true
            }
        }
    }

    LaunchedEffect(showSystemApps) {
        isLoading = true
        apps = withContext(Dispatchers.IO) {
            loadInstalledApps(context, showSystemApps)
        }
        isLoading = false
    }

    Scaffold(
//        topBar = {
//            SmallTopAppBar(
//                title = {
//                    Text(
//                        "已安装应用 (${apps.size})",
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.Bold
//                    )
//                },
//                actions = {
//                    IconButton(
//                        onClick = {
//                            showSystemApps = !showSystemApps
//                        }
//                    ) {
//                        Icon(
//                            imageVector = Icons.Filled.Settings,
//                            contentDescription = if (showSystemApps) "隐藏系统应用" else "显示系统应用",
//                            tint = if (showSystemApps) {
//                                MaterialTheme.colorScheme.primary
//                            } else {
//                                MaterialTheme.colorScheme.onPrimaryContainer
//                            }
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.smallTopAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.primaryContainer,
//                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
//                )
//            )
//        },
//        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        // 应用列表
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "正在加载应用列表...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            if (apps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "📱",
                            fontSize = 64.sp
                        )
                        Text(
                            "暂无应用",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(
                        start = 10.dp,
                        top = 10.dp,
                        end = 10.dp,
                        bottom = 10.dp
                    ),
                    modifier = Modifier.padding(paddingValues),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(apps) { app ->
                        AppCard(app = app, context = context)
                    }
                }
            }
        }
    }
}

@Composable
fun AppCard(app: AppInfo, context: Context) {
    var isExporting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val elevation by animateFloatAsState(
        targetValue = if (isPressed) 2f else 4f,
        animationSpec = tween(100), label = "elevation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                openAppDetails(context, app.packageName)
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation.dp,
            pressedElevation = elevation.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 应用图标
            val density = LocalDensity.current
            val iconShape = RoundedCornerShape(12.dp)
            Card(
                modifier = Modifier.size(56.dp),
                shape = iconShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                app.icon?.let { icon ->
                    val bitmap = remember(icon, density) {
                        val iconSizePx = with(density) { (60.dp * 3).toPx().toInt() }
                        icon.toBitmap(iconSizePx, iconSizePx)
                    }
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = app.appName,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(iconShape)
                    )
                } ?: run {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant, iconShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📱", fontSize = 22.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 应用名称
            Text(
                text = app.appName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 版本号和大小信息卡片
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 版本号
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "版本",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            app.versionName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 大小
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "大小",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            formatFileSize(app.size),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 按钮区域
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 详情按钮
//                Button(
//                    onClick = {
//                        openAppDetails(context, app.packageName)
//                    },
//                    modifier = Modifier
//                        .weight(1f)
//                        .height(36.dp),
//                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
//                    shape = RoundedCornerShape(12.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = MaterialTheme.colorScheme.primary
//                    ),
//                    elevation = ButtonDefaults.buttonElevation(
//                        defaultElevation = 2.dp,
//                        pressedElevation = 4.dp
//                    )
//                ) {
//                    Text(
//                        "详情",
//                        fontSize = 11.sp,
//                        fontWeight = FontWeight.Medium
//                    )
//                }

                // 应用商店按钮
                OutlinedButton(
                    onClick = {
                        openAppStore(context, app.packageName)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp
                    )
                ) {
                    Text(
                        "商店",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 导出按钮
                Button(
                    onClick = {
                        if (isExporting) return@Button
                        isExporting = true
                        scope.launch {
                            exportApk(context, app)
                            isExporting = false
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    enabled = !isExporting,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 1.dp
                    )
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            "导出",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

fun formatFileSize(size: Long): String = when {
    size <= 0 -> "0 B"
    else -> {
        val units = listOf("B", "KB", "MB", "GB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt().coerceAtMost(units.size - 1)
        val formattedSize = DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups))
        "$formattedSize ${units[digitGroups]}"
    }
}

fun openAppDetails(context: Context, packageName: String) {
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }.let { context.startActivitySafely(it, "无法打开应用详情") }
}

fun openAppStore(context: Context, packageName: String) {
    val marketIntent = context.createIntent(Intent.ACTION_VIEW, "market://details?id=$packageName")
    val webIntent = context.createIntent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$packageName")

    when {
        marketIntent.resolveActivity(context.packageManager) != null ->
            context.startActivitySafely(marketIntent, "无法打开应用商店")
        webIntent.resolveActivity(context.packageManager) != null ->
            context.startActivitySafely(webIntent, "无法打开应用商店")
        else ->
            Toast.makeText(context, "无法打开应用商店", Toast.LENGTH_SHORT).show()
    }
}

suspend fun exportApk(context: Context, app: AppInfo) = withContext(Dispatchers.IO) {
    val sourceFile = File(app.sourceDir)

    if (!sourceFile.exists()) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "APK文件不存在", Toast.LENGTH_SHORT).show()
        }
        return@withContext
    }

    runCatching {
        // 创建导出目录 - 使用外置公共目录
        val exportDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "ExportedAPKs"
        ).apply { mkdirs() }

        // 目标文件
        val destFile = File(exportDir, "${app.appName}_${app.versionName}.apk")

        // 复制文件
        FileInputStream(sourceFile).use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }

        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                "APK已导出到: ${destFile.absolutePath}",
                Toast.LENGTH_LONG
            ).show()
        }
    }.onFailure { e ->
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

suspend fun loadInstalledApps(context: Context, includeSystemApps: Boolean = false): List<AppInfo> =
    withContext(Dispatchers.IO) {
        val packageManager = context.packageManager

        runCatching {
            val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstalledPackages(0)
            }

            packages
                .mapNotNull { packageInfo ->
                    runCatching {
                        val appInfo = packageInfo.applicationInfo ?: return@runCatching null

                        // 根据设置决定是否跳过系统应用
                        if (!includeSystemApps && (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0) {
                            return@runCatching null
                        }

                        val appName = packageManager.getApplicationLabel(appInfo).toString()
                        val icon = packageManager.getApplicationIcon(appInfo.packageName)
                        val versionName = packageInfo.versionName ?: "未知"
                        @Suppress("DEPRECATION")
                        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            packageInfo.longVersionCode
                        } else {
                            packageInfo.versionCode.toLong()
                        }

                        val sourceDir = appInfo.sourceDir
                        val size = File(sourceDir).length()

                        AppInfo(
                            packageName = appInfo.packageName,
                            appName = appName,
                            icon = icon,
                            versionName = versionName,
                            versionCode = versionCode,
                            size = size,
                            sourceDir = sourceDir
                        )
                    }.getOrNull()
                }
                .sortedBy { it.appName }
        }.getOrElse { emptyList() }
    }

