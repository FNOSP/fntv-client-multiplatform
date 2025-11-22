package com.jankinwu.fntv.client.ui.component.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedScrollbarLazyColumn(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit
) {
    Box(modifier = modifier) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(end = 8.dp)
        ) {
            content()
        }

        // 自定义滚动条 (覆盖在右侧)
        AnimatedVisibility(
            visible = listState.isScrollInProgress,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            val layoutInfo = listState.layoutInfo
            if (layoutInfo.visibleItemsInfo.isNotEmpty() && layoutInfo.totalItemsCount > 0) {
                val firstVisibleIndex = layoutInfo.visibleItemsInfo.first().index
                val visibleItemCount = layoutInfo.visibleItemsInfo.size
                val totalItemCount = layoutInfo.totalItemsCount
                val totalHeight = layoutInfo.viewportSize.height

                // 动态计算滚动条高度比例
                val scrollbarHeightRatio = visibleItemCount.toFloat() / totalItemCount.toFloat()
                val scrollbarHeight = (scrollbarHeightRatio * totalHeight).dp

                // 计算滚动条位置比例
                val scrollRatio =
                    firstVisibleIndex.toFloat() / (totalItemCount - visibleItemCount).coerceAtLeast(
                        1
                    ).toFloat()
                val maxOffset = (totalHeight - scrollbarHeight.value).coerceAtLeast(0f)
                val offset = (scrollRatio * maxOffset).dp

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                        .background(Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .height(scrollbarHeight)
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .offset(y = offset)
                            .background(Color.Gray, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedScrollbarColumn(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(end = 8.dp)
        ) {
            content()
        }

        // 自定义滚动条 (覆盖在右侧)
        AnimatedVisibility(
            visible = scrollState.isScrollInProgress,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            // 对于普通滚动，需要不同的计算方式
            val scrollTop = scrollState.value
            val scrollRange = scrollState.maxValue

            if (scrollRange > 0) {
                // 这里需要根据实际内容高度和容器高度计算
                val scrollbarHeight = 40.dp // 或根据比例计算
                val offset = (scrollTop.toFloat() / scrollRange.toFloat() * 100).dp // 示例计算

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                        .background(Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .height(scrollbarHeight)
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .offset(y = offset)
                            .background(Color.Gray, CircleShape)
                    )
                }
            }
        }
    }
}