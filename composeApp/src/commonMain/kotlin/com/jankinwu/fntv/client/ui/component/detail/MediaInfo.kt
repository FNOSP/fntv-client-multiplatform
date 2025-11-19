package com.jankinwu.fntv.client.ui.component.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jankinwu.fntv.client.icons.Audio
import com.jankinwu.fntv.client.icons.Subtitle
import com.jankinwu.fntv.client.icons.Video
import io.github.composefluent.FluentTheme

// 文件信息数据
data class FileInfoData(
    val location: String,
    val size: String,
    val createdDate: String,
    val addedDate: String
)

// 媒体轨道信息 (用于视频、音频、字幕)
data class MediaTrackInfo(
    val type: String, // "视频", "音频", "字幕"
    val details: String,
    val icon: ImageVector
)

// 聚合数据
data class MediaDetails(
    val fileInfo: FileInfoData,
    val videoTrack: MediaTrackInfo,
    val audioTrack: MediaTrackInfo,
    val subtitleTrack: MediaTrackInfo,
    val imdbLink: String
)

// 定义颜色常量以匹配截图风格
object DarkThemeColors {
    val Background = Color.Transparent // 整体背景
    val Surface = Color(0xFF1E1E1E)    // 卡片背景
    val TextPrimary = Color(0xFFE0E0E0) // 主要文字 (亮白)
    val TextSecondary = Color(0xFFAAAAAA) // 次要文字 (灰色标签)
    val TextLink = Color(0xFF64B5F6) // 链接颜色 (可选)
}

@Composable
fun MediaInfoScreen(modifier: Modifier = Modifier) {
    // 模拟数据
    val sampleData = MediaDetails(
        fileInfo = FileInfoData(
            location = "存储空间2/admin 的文件/files/video/电影/【高清影视之家发布 www.HDBTHD.com】查理和巧克力工厂[粤英多音轨+简繁英字幕].2005.1080p.BluRay.x265.10bit.DTS.3Audio-SONYHD/Charlie.and.the.Chocolate.Factory.2005.1080p.BluRay.x265.10bit.DTS.3Audio-SONYHD.mkv",
            size = "6.53 GB",
            createdDate = "2025-11-08 15:26",
            addedDate = "2025-11-08 15:26"
        ),
        videoTrack = MediaTrackInfo("视频", "1080P HEVC 5.65 Mbps • 10 bit", Video),
        audioTrack = MediaTrackInfo("音频", "英语 DTS 5.1(side) • 48000 Hz", Audio),
        subtitleTrack = MediaTrackInfo("字幕", "中文 HDMV_PGS_SUBTITLE", Subtitle),
        imdbLink = "IMDB链接"
    )

    // 整体容器
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkThemeColors.Background)
            .padding(vertical = 24.dp)
    ) {
        // 1. 文件信息部分
        FileInfoSection(sampleData.fileInfo)

        Spacer(modifier = Modifier.height(24.dp))

        // 2. 视频信息部分
        VideoInfoSection(
            video = sampleData.videoTrack,
            audio = sampleData.audioTrack,
            subtitle = sampleData.subtitleTrack
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. 底部链接
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "链接:  ",
                color = DarkThemeColors.TextSecondary,
                fontSize = 14.sp
            )
            Text(
                text = sampleData.imdbLink,
                color = DarkThemeColors.TextPrimary, // 或者用 TextLink
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { /* Handle click */ }
            )
        }
    }
}

// --- 组件：文件信息区域 ---
@Composable
fun FileInfoSection(info: FileInfoData) {
    SectionHeader(title = "文件信息")

    Surface(
        color = FluentTheme.colors.stroke.control.default,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 文件位置 (可能很长，需要换行)
            InfoRow(label = "文件位置:", value = info.location, isLongText = true)

            Spacer(modifier = Modifier.height(12.dp))

            // 第二行：大小、日期等
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoLabelValue(label = "文件大小:", value = info.size)
                Spacer(modifier = Modifier.width(24.dp))
                InfoLabelValue(label = "文件创建日期:", value = info.createdDate)
                Spacer(modifier = Modifier.width(24.dp))
                InfoLabelValue(label = "添加日期:", value = info.addedDate)
            }
        }
    }
}

// --- 组件：视频/媒体信息区域 ---
@Composable
fun VideoInfoSection(
    video: MediaTrackInfo,
    audio: MediaTrackInfo,
    subtitle: MediaTrackInfo
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "视频信息",
            color = DarkThemeColors.TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { /* 查看全部 */ }
        ) {
            Text(
                text = "查看全部",
                color = DarkThemeColors.TextSecondary,
                fontSize = 12.sp
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = DarkThemeColors.TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }

    Surface(
        color = FluentTheme.colors.stroke.control.default,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // 使用 Weight 平分三列
            Box(modifier = Modifier.weight(1f)) {
                TrackItem(video)
            }
            Box(modifier = Modifier.weight(1f)) {
                TrackItem(audio)
            }
            Box(modifier = Modifier.weight(1f)) {
                TrackItem(subtitle)
            }
        }
    }
}

// --- 辅助小组件 ---

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = DarkThemeColors.TextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun InfoRow(label: String, value: String, isLongText: Boolean = false) {
    Row(verticalAlignment = if(isLongText) Alignment.Top else Alignment.CenterVertically) {
        Text(
            text = label,
            color = DarkThemeColors.TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = value,
            color = DarkThemeColors.TextPrimary,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun InfoLabelValue(label: String, value: String) {
    Row {
        Text(
            text = "$label ",
            color = DarkThemeColors.TextSecondary,
            fontSize = 13.sp
        )
        Text(
            text = value,
            color = DarkThemeColors.TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun TrackItem(track: MediaTrackInfo) {
    Column(horizontalAlignment = Alignment.Start) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = track.icon,
                contentDescription = null,
                tint = DarkThemeColors.TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = track.type,
                color = DarkThemeColors.TextSecondary,
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = track.details,
            color = DarkThemeColors.TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}