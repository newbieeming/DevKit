package com.newbieeming.devkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.newbieeming.devkit.core.ui.FeatureEntry
import com.newbieeming.devkit.ui.theme.DevKitTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * Hilt 自动聚合所有通过 @IntoSet 注册的 FeatureEntry。
     * 每个 feature 模块在自己的 XxxEntryModule 中提供绑定，
     * 无需在此处手动声明——添加新功能只需在 feature 模块内注册即可。
     */
    @Inject
    lateinit var featureEntries: Set<@JvmSuppressWildcards FeatureEntry>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DevKitTheme {
                // 按 priority 排序；相同 priority 保持 Hilt 聚合顺序
                val sortedEntries = remember(featureEntries) {
                    featureEntries.sortedBy { it.priority }
                }
                val navController = rememberNavController()
                DevKitApp(entries = sortedEntries, navController = navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DevKitApp(
    entries: List<FeatureEntry>,
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = DASHBOARD_ROUTE,
    ) {
        composable(DASHBOARD_ROUTE) {
            Scaffold(
                topBar = { TopAppBar(title = { Text("DevKit") }) },
                contentWindowInsets = WindowInsets(0),
            ) { innerPadding ->
                FeatureDashboard(
                    entries = entries,
                    onNavigate = { route -> navController.navigate(route) },
                    contentPadding = innerPadding,
                )
            }
        }
        // 各 feature 二级界面由 FeatureEntry.registerNavigation 自行注册
        entries.forEach { entry ->
            entry.registerNavigation(this, navController)
        }
    }
}

/**
 * 瀑布流仪表盘
 *
 * 使用 [LazyVerticalStaggeredGrid] + [StaggeredGridCells.Adaptive]：
 * - **等宽分列**：所有列宽相等，平分可用宽度
 * - **自适应列数**：目标宽度由可用屏幕宽度按比例计算，且不小于 [MIN_TILE_WIDTH]
 * - **不固定行高**：每个磁贴高度由自身内容决定，形成真正的瀑布流
 */
@Composable
private fun FeatureDashboard(
    entries: List<FeatureEntry>,
    onNavigate: (String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val extraWidth = maxOf(0.dp, maxWidth - MIN_TILE_WIDTH)
        val tileWidth = MIN_TILE_WIDTH + extraWidth * TILE_WIDTH_REMAINDER_RATIO
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(minSize = tileWidth),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = GRID_PADDING,
                end = GRID_PADDING,
                top = contentPadding.calculateTopPadding() + GRID_PADDING,
                bottom = contentPadding.calculateBottomPadding() + GRID_PADDING,
            ),
            horizontalArrangement = Arrangement.spacedBy(TILE_SPACING),
            verticalItemSpacing = TILE_SPACING,
        ) {
            items(
                items = entries,
                key = { it.featureId },
            ) { entry ->
                entry.Tile(
                    modifier = Modifier.fillMaxWidth(),
                    onNavigate = onNavigate,
                )
            }
        }
    }
}

private const val DASHBOARD_ROUTE = "dashboard"

// 以 160dp 为基线，仅按比例吸收额外屏宽，避免宽屏卡片突然变得过宽。
private const val TILE_WIDTH_REMAINDER_RATIO = 0.10f
private val MIN_TILE_WIDTH = 130.dp
private val GRID_PADDING  = 16.dp
private val TILE_SPACING  = 12.dp
