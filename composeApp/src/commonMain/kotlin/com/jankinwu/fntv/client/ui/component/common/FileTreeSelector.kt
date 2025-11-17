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
import com.jankinwu.fntv.client.data.network.impl.FnOfficialApiImpl
import com.jankinwu.fntv.client.ui.component.common.DirectoryContentFetcher.fetchDirectoryContents
import io.github.composefluent.component.Icon
import io.github.composefluent.component.Text
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// --- 1. 数据模型 ---

/**
 * 模拟从 API 接收到的文件/目录项
 */
data class ApiFileItem(
    val filename: String,
    val isDir: Boolean
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
 * 通过API获取指定路径下的文件和目录列表
 */
object DirectoryContentFetcher : KoinComponent {
    private val fnOfficialApi: FnOfficialApiImpl by inject()
    
    suspend fun fetchDirectoryContents(path: String): List<ApiFileItem> {
        return try {
            // 使用 withContext 确保在正确的上下文中执行
            val serverPathResponses = with(kotlinx.coroutines.Dispatchers.IO) {
                fnOfficialApi.getFilesByServerPath(path)
            }

            // 将 ServerPathResponse 转换为 ApiFileItem
            serverPathResponses.map { response ->
                ApiFileItem(
                    filename = response.filename,
                    isDir = response.isDir
                )
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            // 正常处理协程取消
            throw e
        } catch (e: Exception) {
            // 处理其他异常
            emptyList()
        }
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
    rootPath: String,
    selectionMode: SelectionMode,
    allowedExtensions: List<String>,
    onSelectionChanged: (selectedPaths: Set<String>) -> Unit // **[修改点]** 回调返回 Set
) {
    FileTreeSelector(
        rootPaths = listOf(rootPath),
        selectionMode = selectionMode,
        allowedExtensions = allowedExtensions,
        onSelectionChanged = onSelectionChanged
    )
}

/**
 * 文件树选择器的主 Composable (支持多选)
 *
 * @param rootPaths 启动时加载的根路径列表
 * @param selectionMode 允许选择的类型 (文件 / 文件夹 / 两者)
 * @param allowedExtensions 允许显示的文件后缀名列表 (小写)。如果为空，则显示所有文件。
 * @param onSelectionChanged 当选择项发生变化时调用的回调，返回一个包含所有选中路径的 Set
 */
@Composable
fun FileTreeSelector(
    rootPaths: List<String>,
    selectionMode: SelectionMode,
    allowedExtensions: List<String>,
    onSelectionChanged: (selectedPaths: Set<String>) -> Unit // **[修改点]** 回调返回 Set
) {
    // 树的根节点状态 - 支持多个根节点
    var roots by remember {
        mutableStateOf(
            rootPaths.map { rootPath ->
                TreeNode(
                    name = rootPath.substringAfterLast("/", rootPath),
                    path = rootPath,
                    isDirectory = true,
                    isExpanded = true, // 默认展开根目录
                    isLoading = true, // 初始设置为正在加载
                    children = null // 初始为 null，表示未加载
                )
            }
        )
    }

    // **[修改点]** 当前选择的项的路径集合 (Set)
    var selectedPaths by remember { mutableStateOf(emptySet<String>()) }

    // 用于执行异步加载的协程作用域
    val scope = rememberCoroutineScope()

    // 初始加载所有根节点的内容
    LaunchedEffect(roots) {
        roots.forEach { root ->
            if (root.children == null) {
                scope.launch {
                    val apiChildren = fetchDirectoryContents(root.path)
                    val newChildren = apiChildren.map {
                        TreeNode(
                            name = it.filename,
                            path = "${root.path}/${it.filename}",
                            isDirectory = it.isDir,
                            children = if (it.isDir) null else emptyList()
                        )
                    }
                    roots = roots.map {
                        if (it.path == root.path) {
                            it.copy(
                                isLoading = false,
                                children = newChildren
                            )
                        } else {
                            it
                        }
                    }
                }
            }
        }
    }

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
     * 更新所有根节点中的指定节点
     */
    fun updateNodeInRoots(targetPath: String, action: (TreeNode) -> TreeNode): List<TreeNode> {
        return roots.map { root ->
            root.updateNode(targetPath, action)
        }
    }

    /**
     * 当一个目录节点被点击时触发
     */
    fun onDirectoryClick(node: TreeNode) {
        val targetPath = node.path

        if (node.isExpanded) {
            // 如果已展开 -> 折叠
            roots = updateNodeInRoots(targetPath) { it.copy(isExpanded = false) }
        } else {
            // 如果已折叠 -> 展开
            if (node.children == null) {
                // 1. 未加载 -> 加载数据
                roots = updateNodeInRoots(targetPath) { it.copy(isLoading = true) }

                scope.launch {
                    val apiChildren = fetchDirectoryContents(targetPath)
                    val newChildren = apiChildren.map {
                        TreeNode(
                            name = it.filename,
                            path = "$targetPath/${it.filename}",
                            isDirectory = it.isDir,
                            children = if (it.isDir) null else emptyList()
                        )
                    }
                    roots = updateNodeInRoots(targetPath) {
                        it.copy(
                            isLoading = false,
                            isExpanded = true,
                            children = newChildren
                        )
                    }
                }
            } else {
                // 2. 已加载 -> 直接展开
                roots = updateNodeInRoots(targetPath) { it.copy(isExpanded = true) }
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
        if (roots.any { it.children == null && !it.isLoading }) {
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
            }
        } else {
            // 递归渲染所有根节点下的文件树项
            roots.forEach { root ->
                // 显示根节点名称
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 8.dp)
                    ) {
                        Text(
                            text = root.name,
                            color = Color.White,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                }
                
                // 递归渲染文件树项
                if (root.children != null) {
                    fileTreeItems(
                        nodes = root.children,
                        depth = 0,
                        selectionMode = selectionMode,
                        allowedExtensions = allowedExtensions.map { it.lowercase() },
                        selectedPaths = selectedPaths, // **[修改点]** 传入 Set
                        onDirectoryClick = { onDirectoryClick(it) },
                        onToggleSelection = onToggleSelection // **[修改点]** 传入 toggle 处理器
                    )
                } else if (root.isLoading) {
                    // 显示单个根节点的加载状态
                    item {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Loading ${root.name}...", color = Color.White)
                        }
                    }
                }
            }
        }
    }
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