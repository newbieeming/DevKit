package com.newbieeming.devkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
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
import com.newbieeming.devkit.core.designsystem.theme.DevKitTheme
import com.newbieeming.devkit.core.ui.FeatureEntry
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
 * - **自适应列数**：每列至少 [MIN_TILE_WIDTH]，屏幕越宽列越多（最少 2 列）
 * - **不固定行高**：每个磁贴高度由自身内容决定，形成真正的瀑布流
 */
@Composable
private fun FeatureDashboard(
    entries: List<FeatureEntry>,
    onNavigate: (String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = MIN_TILE_WIDTH),
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

private const val DASHBOARD_ROUTE = "dashboard"

// 每列最小宽度：屏幕宽度 ÷ MIN_TILE_WIDTH ≥ 2 列（手机典型宽度 360dp）
// 360dp - 2×16dp(padding) - 12dp(间距) = 316dp → 316÷2 = 158dp → ≥ 150dp → 2 列
// 平板 600dp → (600-44)÷150 = 3.7 → 3 列，以此类推
private val MIN_TILE_WIDTH = 150.dp
private val GRID_PADDING  = 16.dp
private val TILE_SPACING  = 12.dp
