package com.jankinwu.fntv.client.ui.component.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jankinwu.fntv.client.data.constants.Colors
import io.github.composefluent.component.Icon
import io.github.composefluent.component.Text
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- 1. 数据模型 ---

/**
 * 模拟从 API 接收到的文件/目录项
 */
data class ApiFileItem(
    val filename: String,
    val is_dir: Boolean
    // 其他字段 (trim_name, trim_id) 在此示例中未使用
)

/**
 * 选择模式：只选文件、只选文件夹、或两者皆可
 */
enum class SelectionMode {
    FilesOnly,
    FoldersOnly,
    FilesAndFolders
}

/**
 * 内部用于UI状态的树节点模型
 * @param path 节点的完整路径，用作唯一ID
 * @param children 首次加载后，目录的子节点列表；null 表示尚未加载
 * @param isExpanded 目录是否已展开
 * @param isLoading 目录是否正在加载子节点
 */
data class TreeNode(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val children: List<TreeNode>? = null,
    val isExpanded: Boolean = false,
    val isLoading: Boolean = false
)

// --- 2. 模拟的 API 请求 ---

/**
 * 模拟的网络请求。
 * 接收一个路径，返回该路径下的文件和目录列表。
 */
suspend fun fetchDirectoryContents(path: String): List<ApiFileItem> {
    // 模拟网络延迟
    delay(500)

    // 模拟的 API 响应数据
    return when (path) {
        "root" -> listOf(ApiFileItem("video", true))
        "root/video" -> listOf(ApiFileItem("BiliSync", true))
        "root/video/BiliSync" -> listOf(
            ApiFileItem("21世纪电影百排行，和你想的一样吗？", true),
            ApiFileItem("“大陆缺什么，我就去做什么。”中国半导体...", true),
            ApiFileItem("【激荡四十年】系列", true)
        )
        "root/video/BiliSync/21世纪电影百排行，和你想的一样吗？" -> listOf(
            ApiFileItem("BV1hV3uzRE31.zh-CN.default.ass", false),
            ApiFileItem("movie.mp4", false),
            ApiFileItem("image.jpg", false)
        )
        // 模拟一个包含 JSON 中示例数据的文件夹
        "root/video/BiliSync/【激荡四十年】系列" -> listOf(
            ApiFileItem("Bohemian.Rhapsody.2018.mkv", false),
            ApiFileItem("Bohemian.Rhapsody.2018.nfo", false),
            ApiFileItem("folder.jpg", false),
            ApiFileItem("movie.nfo", false),
            ApiFileItem("其他子目录", true)
        )
        else -> emptyList() // 默认返回空列表
    }
}

// --- 3. 核心 Composable：文件树选择器 ---

/**
 * 文件树选择器的主 Composable (支持多选)
 *
 * @param rootPath 启动时加载的根路径
 * @param selectionMode 允许选择的类型 (文件 / 文件夹 / 两者)
 * @param allowedExtensions 允许显示的文件后缀名列表 (小写)。如果为空，则显示所有文件。
 * @param onSelectionChanged 当选择项发生变化时调用的回调，返回一个包含所有选中路径的 Set
 */
@Composable
fun FileTreeSelector(
    rootPath: String = "root",
    selectionMode: SelectionMode = SelectionMode.FilesOnly,
    allowedExtensions: List<String> = emptyList(),
    onSelectionChanged: (selectedPaths: Set<String>) -> Unit // **[修改点]** 回调返回 Set
) {
    // 树的根节点状态
    var root by remember {
        mutableStateOf(TreeNode(
            name = rootPath,
            path = rootPath,
            isDirectory = true,
            isExpanded = true, // 默认展开根目录
            children = null // 初始为 null，表示未加载
        ))
    }

    // **[修改点]** 当前选择的项的路径集合 (Set)
    var selectedPaths by remember { mutableStateOf(emptySet<String>()) }

    // 用于执行异步加载的协程作用域
    val scope = rememberCoroutineScope()

    // --- 状态更新辅助函数 ---

    /**
     * 递归更新树状态的辅助函数（保持不可变性）
     */
    fun TreeNode.updateNode(targetPath: String, action: (TreeNode) -> TreeNode): TreeNode {
        if (this.path == targetPath) {
            return action(this)
        }
        return this.copy(
            children = this.children?.map { it.updateNode(targetPath, action) }
        )
    }

    /**
     * 当一个目录节点被点击时触发
     */
    fun onDirectoryClick(node: TreeNode) {
        val targetPath = node.path

        if (node.isExpanded) {
            // 如果已展开 -> 折叠
            root = root.updateNode(targetPath) { it.copy(isExpanded = false) }
        } else {
            // 如果已折叠 -> 展开
            if (node.children == null) {
                // 1. 未加载 -> 加载数据
                root = root.updateNode(targetPath) { it.copy(isLoading = true) }

                scope.launch {
                    val apiChildren = fetchDirectoryContents(targetPath)
                    val newChildren = apiChildren.map {
                        TreeNode(
                            name = it.filename,
                            path = "$targetPath/${it.filename}",
                            isDirectory = it.is_dir,
                            children = if (it.is_dir) null else emptyList()
                        )
                    }
                    root = root.updateNode(targetPath) {
                        it.copy(
                            isLoading = false,
                            isExpanded = true,
                            children = newChildren
                        )
                    }
                }
            } else {
                // 2. 已加载 -> 直接展开
                root = root.updateNode(targetPath) { it.copy(isExpanded = true) }
            }
        }
    }

    // **[修改点]** 处理选择状态切换的逻辑
    val onToggleSelection = { path: String ->
        val newPaths = if (path in selectedPaths) {
            selectedPaths - path // 从 Set 中移除
        } else {
            selectedPaths + path // 添加到 Set 中
        }
        selectedPaths = newPaths // 更新内部状态
        onSelectionChanged(newPaths) // 调用外部回调
    }

    // --- UI 渲染 ---

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2B2B2B))
    ) {
        // 启动递归渲染
        if (root.children == null) {
            // 根目录的初始加载
            item {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Loading root...", color = Color.White)
                }
                LaunchedEffect(rootPath) {
                    onDirectoryClick(root)
                }
            }
        } else {
            // 递归渲染文件树项
            fileTreeItems(
                nodes = root.children ?: emptyList(),
                depth = 0,
                selectionMode = selectionMode,
                allowedExtensions = allowedExtensions.map { it.lowercase() },
                selectedPaths = selectedPaths, // **[修改点]** 传入 Set
                onDirectoryClick = { onDirectoryClick(it) },
                onToggleSelection = onToggleSelection // **[修改点]** 传入 toggle 处理器
            )
        }
    }
//    MaterialTheme(colors = darkColors()) {
//    }
}

/**
 * `LazyListScope` 扩展函数，递归构建列表项
 */
private fun LazyListScope.fileTreeItems(
    nodes: List<TreeNode>,
    depth: Int,
    selectionMode: SelectionMode,
    allowedExtensions: List<String>,
    selectedPaths: Set<String>, // **[修改点]** 接收 Set
    onDirectoryClick: (TreeNode) -> Unit,
    onToggleSelection: (String) -> Unit // **[修改点]** 接收 toggle 处理器
) {
    nodes.forEach { node ->
        // --- 过滤逻辑 ---
        val extension = if (!node.isDirectory) node.name.substringAfterLast('.', "") else ""
        val isAllowed = when {
            node.isDirectory -> true
            allowedExtensions.isEmpty() -> true
            extension in allowedExtensions -> true
            else -> false
        }

        // --- 可选择逻辑 ---
        val isSelectable = when (selectionMode) {
            SelectionMode.FilesOnly -> !node.isDirectory
            SelectionMode.FoldersOnly -> node.isDirectory
            SelectionMode.FilesAndFolders -> true
        }

        if (isAllowed) {
            // 1. 渲染当前节点
            item {
                FileNodeItem(
                    node = node,
                    depth = depth,
                    isSelectable = isSelectable,
                    isSelected = node.path in selectedPaths, // **[修改点]** 检查 Set 中是否存在
                    onNodeClick = {
                        if (node.isDirectory) {
                            onDirectoryClick(node)
                        } else if (isSelectable) {
                            // **[修改点]** 点击文件行也触发 toggle
                            onToggleSelection(node.path)
                        }
                    },
                    onCheckboxChange = {
                        // **[修改点]** 点击复选框触发 toggle
                        onToggleSelection(node.path)
                    }
                )
            }

            // 2. 递归渲染子节点
            if (node.isExpanded && node.children != null) {
                fileTreeItems(
                    nodes = node.children,
                    depth = depth + 1,
                    selectionMode = selectionMode,
                    allowedExtensions = allowedExtensions,
                    selectedPaths = selectedPaths, // **[修改点]** 向下传递 Set
                    onDirectoryClick = onDirectoryClick,
                    onToggleSelection = onToggleSelection // **[修改点]** 向下传递处理器
                )
            }
        }
    }
}

/**
 * 渲染单个文件/目录行
 */
@Composable
private fun FileNodeItem(
    node: TreeNode,
    depth: Int,
    isSelectable: Boolean,
    isSelected: Boolean,
    onNodeClick: () -> Unit,
    onCheckboxChange: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNodeClick)
            .padding(vertical = 4.dp)
            .padding(start = (depth * 24 + 8).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // 1. 展开/折叠箭头 (或加载指示器)
        Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
            if (node.isDirectory) {
                if (node.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    val arrowIcon = if (node.isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight
                    Icon(imageVector = arrowIcon, contentDescription = "Expand", tint = Color.LightGray)
                }
            }
        }

        // 2. 复选框 (如果可选择)
        Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
            if (isSelectable) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onCheckboxChange() }, // onCheckedChange lambda 已是 () -> Unit
                    colors = CheckboxDefaults.colors(
                        checkedColor = Colors.PrimaryColor,
                        uncheckedColor = Color.Gray
                    )
                )
            }
        }

        // 3. 文件/目录图标
        Icon(
            imageVector = if (node.isDirectory) Icons.Default.Folder else Icons.Default.Description,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (node.isDirectory) Color(0xFFFACC15) else Color.LightGray
        )

        Spacer(Modifier.width(8.dp))

        // 4. 文件名
        Text(
            text = node.name,
            color = if (isSelected) Colors.PrimaryColor else Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// --- 4. 示例用法 ---
/*
// 在你的 main 函数中这样使用：
fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        // **[修改点]** 使用 mutableStateOf(emptySet<String>()) 来存储多选结果
        var selectedFiles by remember { mutableStateOf(emptySet<String>()) }

        Column(Modifier.fillMaxSize()) {
            // **[修改点]** 显示所有选中的文件
            Text(
                "Selected (${selectedFiles.size}):",
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
            LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                items(selectedFiles.toList()) { path ->
                    Text(path, color = Color.Gray, fontSize = 12.sp)
                }
            }

            Divider()

            // **[修改点]** onSelectionChanged 现在返回一个 Set
            FileTreeSelector(
                modifier = Modifier.weight(3f), // 分配更多空间给选择器
                selectionMode = SelectionMode.FilesOnly,
                allowedExtensions = listOf("ass", "mkv", "mp4"),
                onSelectionChanged = { paths ->
                    selectedFiles = paths
                }
            )
        }
    }
}
*/