package com.jankinwu.fntv.client.ui.component.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jankinwu.fntv.client.ui.component.common.FileTreeSelector
import com.jankinwu.fntv.client.ui.component.common.SelectionMode
import io.github.composefluent.FluentTheme
import io.github.composefluent.LocalContentColor
import io.github.composefluent.LocalTextStyle
import io.github.composefluent.component.AccentButton
import io.github.composefluent.component.Button
import io.github.composefluent.component.ContentDialogButton
import io.github.composefluent.component.DialogSize
import io.github.composefluent.component.FluentDialog
import io.github.composefluent.component.Text

@Composable
fun AddNasSubtitleDialog(
    title: String,
    visible: Boolean,
    primaryButtonText: String,
    secondaryButtonText: String? = null,
    closeButtonText: String? = null,
    onButtonClick: (ContentDialogButton) -> Unit,
    size: DialogSize = DialogSize.Standard
) {
    // 状态：用于保存文件选择器返回的路径 (此处仅用于演示，可以传递给其他组件)
    var selectedFilePaths by remember { mutableStateOf(emptySet<String>()) }
    FluentDialog(visible, size) {
        Column {
            Column(
                Modifier
                    .fillMaxWidth()
//                    .background(FluentTheme.colors.background.layer.alt)
                    .padding(24.dp)
            ) {
                Text(
                    style = FluentTheme.typography.subtitle,
                    text = title,
                )
                Spacer(Modifier.height(12.dp))
                CompositionLocalProvider(
                    LocalTextStyle provides FluentTheme.typography.body,
                    LocalContentColor provides FluentTheme.colors.text.text.primary
                ) {
                    AddNasSubtitleBox(onSelectionChanged = { selectedFilePaths = it })
                }
            }
            // Divider
//            Box(Modifier.height(1.dp).background(FluentTheme.colors.stroke.surface.default))
            // Button Grid
            Box(Modifier.height(80.dp).padding(horizontal = 25.dp), Alignment.CenterEnd) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccentButton(
                        modifier = Modifier.weight(1f),
                        onClick = { onButtonClick(ContentDialogButton.Primary) }
                    ) {
                        Text(primaryButtonText)
                    }
                    if (secondaryButtonText != null) Button(
                        modifier = Modifier.weight(1f),
                        onClick = { onButtonClick(ContentDialogButton.Secondary) }
                    ) {
                        Text(secondaryButtonText)
                    }
                    if (closeButtonText != null) Button(
                        modifier = Modifier.weight(1f),
                        onClick = { onButtonClick(ContentDialogButton.Close) }
                    ) {
                        Text(closeButtonText)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNasSubtitleBox(
    onSelectionChanged: (Set<String>) -> Unit,
) {
    // 状态：用于跟踪侧边栏的当前选择
    var selectedSidebarItem by remember { mutableStateOf("存储空间 2") }

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // 1. 顶部标题栏
            TopBarBox(
                title = selectedSidebarItem,
                contentColor = Color.White
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.Black.copy(alpha = 0.5f))
            )
            // 2. 主内容区域 (侧边栏 + 文件树)
            Row(modifier = Modifier.fillMaxSize()) {

                // 2a. 侧边栏
                Sidebar(
                    selectedItem = selectedSidebarItem,
                    onItemSelected = { selectedSidebarItem = it },
                    modifier = Modifier.fillMaxHeight().width(200.dp)
                )

                // 2. 主内容区域 (侧边栏 + 文件树)
                // **修改：使用 weight(1f) 占据剩余空间，而不是 fillMaxSize()**
                Row(modifier = Modifier.weight(1f)) {

                    // 2a. 侧边栏
                    Sidebar(
                        selectedItem = selectedSidebarItem,
                        onItemSelected = { selectedSidebarItem = it },
                        modifier = Modifier.fillMaxHeight().width(200.dp)
                    )

                    // 2b. 垂直分割线
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(Color.Gray.copy(alpha = 0.5f))
                    )

                    // 2c. 主内容 (调用文件选择器)
                    MainContent(
                        onSelectionChanged = { paths ->
                            onSelectionChanged(paths)
                            // 调试：打印选中的路径
                            println("Selected Paths: $paths")
                        },
                        modifier = Modifier.fillMaxSize() // 占据剩余空间
                    )
                }
            }
        }
    }
}

@Composable
fun TopBarBox(title: String, contentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = contentColor)
    }
}

/**
 * 渲染左侧的侧边栏
 */
@Composable
fun Sidebar(
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 侧边栏项目列表
    val items = listOf("视频所在位置", "存储空间 2")

    Column(
        modifier = modifier
            .background(Color(0xFF2B2B2B)) // 侧边栏背景
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEach { item ->
            SidebarItem(
                text = item,
                isSelected = (item == selectedItem),
                onClick = { onItemSelected(item) }
            )
        }
    }
}

/**
 * 渲染侧边栏中的单个项目
 */
@Composable
fun SidebarItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // 根据是否选中，切换背景和文字颜色
    val backgroundColor = if (isSelected) Color(0xFF3A3F4B) else Color.Transparent
    val textColor = if (isSelected) Color.White else Color.LightGray

    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .padding(12.dp),
        color = textColor,
        fontSize = 14.sp
    )
}

/**
 * 渲染右侧的主内容区域，这里是**调用**文件选择器的地方
 */
@Composable
fun MainContent(
    onSelectionChanged: (Set<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF2B2B2B)) // 主内容区域背景
    ) {
        // ---
        // --- 这是您所要求的文件选择器的 "调用代码" ---
        // ---
        FileTreeSelector(
            rootPath = "root", // 初始加载路径
            selectionMode = SelectionMode.FilesOnly, // 选择模式
            allowedExtensions = listOf("ass", "mkv", "mp4", "jpg", "nfo"), // 允许的后缀
            onSelectionChanged = onSelectionChanged // 状态回调
        )
    }
}