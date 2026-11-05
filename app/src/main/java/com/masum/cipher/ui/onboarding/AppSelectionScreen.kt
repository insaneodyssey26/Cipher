package com.masum.cipher.ui.onboarding

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.collection.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Color
import compose.icons.lucideicons.Undo2
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.masum.cipher.R
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.Check
import compose.icons.lucideicons.Search
import compose.icons.lucideicons.X
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

@androidx.compose.runtime.Immutable
data class InstalledApp(
    val packageName: String,
    val appName: String
)

private val appIconCache = LruCache<String, ImageBitmap>(200)
private const val APP_ICON_DECODE_SIZE_PX = 96

@Composable
fun AppSelectionScreen(
    initialSelectedApps: Set<String>,
    onComplete: (Set<String>) -> Unit
) {
    val context = LocalContext.current
    val view = androidx.compose.ui.platform.LocalView.current
    var installedApps by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    var selectedApps by remember { mutableStateOf(initialSelectedApps) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var showSelectedAppsDialog by remember { mutableStateOf(false) }

    val filteredApps = remember(installedApps, searchQuery) {
        if (searchQuery.isBlank()) installedApps
        else installedApps.filter { it.appName.contains(searchQuery, ignoreCase = true) }
    }

    val heroAlpha = remember { Animatable(0f) }
    val gridAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        heroAlpha.animateTo(1f, tween(340))
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val intent = android.content.Intent(android.content.Intent.ACTION_MAIN, null).apply {
                addCategory(android.content.Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
            } else {
                pm.queryIntentActivities(intent, 0)
            }
            val apps = resolveInfos
                .asSequence()
                .map { it.activityInfo.applicationInfo }
                .distinctBy { it.packageName }
                .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
                .mapNotNull { info ->
                    try {
                        InstalledApp(
                            packageName = info.packageName,
                            appName = pm.getApplicationLabel(info).toString()
                        )
                    } catch (_: Exception) { null }
                }
                .sortedBy { it.appName }
                .toList()

            withContext(Dispatchers.Main) {
                installedApps = apps
                val installedPackageNames = apps.map { it.packageName }.toSet()
                selectedApps = if (initialSelectedApps.isEmpty()) {
                    val financeKeywords = listOf(
                        "pay", "bank", "upi", "finance", "money", "wallet", "cash", "crypto",
                        "trade", "cred", "gpay", "phonepe", "paytm", "slice", "jupiter", "fi",
                        "amazon", "mobikwik", "freecharge", "bhim", "groww", "zerodha", "navi"
                    )
                    apps.filter { app ->
                        val lowerPkg = app.packageName.lowercase()
                        val lowerName = app.appName.lowercase()
                        financeKeywords.any { lowerPkg.contains(it) || lowerName.contains(it) }
                    }.map { it.packageName }.toSet()
                } else {
                    selectedApps.intersect(installedPackageNames)
                }
                isLoading = false
            }
        }
        delay(120.milliseconds)
        gridAlpha.animateTo(1f, tween(380))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(horizontal = 22.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.graphicsLayer { alpha = heroAlpha.value }) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.09f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.onboarding_apps_step_label),
                            style = Typography.labelSmall.copy(
                                fontFamily = Lato,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                fontSize = 9.5.sp,
                                letterSpacing = 1.2.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (!isLoading && selectedApps.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        view.performVibrate(true, isLongPress = false)
                                        showSelectedAppsDialog = true
                                    }
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.onboarding_apps_selected_count, selectedApps.size),
                                style = Typography.labelSmall.copy(
                                    fontFamily = DMSans,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.2.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.onboarding_apps_title),
                    style = Typography.displaySmall.copy(
                        fontFamily = DMSans,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        fontSize = 30.sp,
                        lineHeight = 36.sp,
                        letterSpacing = (-0.8).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.onboarding_apps_subtitle),
                    style = Typography.bodyMedium.copy(
                        fontFamily = Lato,
                        fontSize = 13.sp,
                        lineHeight = 18.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f),
                    RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = LucideIcons.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = stringResource(R.string.onboarding_apps_search_hint),
                            style = Typography.bodyMedium.copy(fontFamily = Lato, fontSize = 14.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = Typography.bodyMedium.copy(
                            fontFamily = Lato,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (searchQuery.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            .clickable { searchQuery = "" },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.X,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = stringResource(R.string.onboarding_apps_loading),
                        style = Typography.bodySmall.copy(fontFamily = Lato, fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            Box(modifier = Modifier
                .weight(1f)
                .graphicsLayer { alpha = gridAlpha.value }
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredApps, key = { it.packageName }, contentType = { "app" }) { app ->
                        val isSelected = selectedApps.contains(app.packageName)
                        AppGridItem(
                            app = app,
                            packageManager = context.packageManager,
                            isSelected = isSelected,
                            onClick = {
                                view.performVibrate(true, isLongPress = true)
                                selectedApps = if (isSelected) selectedApps - app.packageName
                                else selectedApps + app.packageName
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val btnInteractionSource = remember { MutableInteractionSource() }
        val btnIsPressed by btnInteractionSource.collectIsPressedAsState()
        val btnScale by animateFloatAsState(
            targetValue = if (btnIsPressed) 0.97f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "finish_btn_scale"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(btnScale)
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primary)
                .clickable(interactionSource = btnInteractionSource, indication = null) {
                    view.performVibrate(true, isLongPress = false)
                    onComplete(selectedApps)
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.onboarding_apps_finish),
                style = Typography.titleMedium.copy(
                    fontFamily = Lato,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = (-0.2).sp
                ),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
    }

    var removedHistory by remember { mutableStateOf<List<String>>(emptyList()) }

    if (showSelectedAppsDialog) {
        SelectedAppsDialog(
            selectedApps = selectedApps,
            allApps = installedApps,
            packageManager = context.packageManager,
            removedHistory = removedHistory,
            onDeselect = { pkg ->
                removedHistory = removedHistory + pkg
                selectedApps = selectedApps - pkg
            },
            onUndo = {
                val last = removedHistory.lastOrNull()
                if (last != null) {
                    removedHistory = removedHistory.dropLast(1)
                    selectedApps = selectedApps + last
                }
            },
            onClearAll = {
                val currentList = selectedApps.toList()
                if (currentList.isNotEmpty()) {
                    removedHistory = removedHistory + currentList
                }
                selectedApps = emptySet()
            },
            onDismiss = {
                removedHistory = emptyList()
                showSelectedAppsDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectedAppsDialog(
    selectedApps: Set<String>,
    allApps: List<InstalledApp>,
    packageManager: PackageManager,
    removedHistory: List<String>,
    onDeselect: (String) -> Unit,
    onUndo: () -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val appMap = remember(allApps) { allApps.associateBy { it.packageName } }
    val selectedAppList = remember(selectedApps, allApps) {
        selectedApps.mapNotNull { appMap[it] }.sortedBy { it.appName }
    }
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val closeSheet: () -> Unit = {
        coroutineScope.launch {
            sheetState.hide()
            onDismiss()
        }
    }

    val sheetBg = if (isDark) Color(0xFF13161C) else MaterialTheme.colorScheme.surface
    val itemBg = if (isDark) Color(0xFF1B1F27) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    val itemBorder = if (isDark) Color(0xFF282E3A) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        sheetGesturesEnabled = false,
        containerColor = sheetBg,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(top = 18.dp, bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_selected_apps_title),
                        style = Typography.titleLarge.copy(
                            fontFamily = DMSans,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            fontSize = 19.sp,
                            letterSpacing = (-0.4).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = selectedApps.size.toString(),
                            style = Typography.labelSmall.copy(
                                fontFamily = DMSans,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                fontSize = 11.5.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedApps.isNotEmpty()) {
                        TextButton(
                            onClick = onClearAll,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.onboarding_selected_apps_clear),
                                style = Typography.labelSmall.copy(
                                    fontFamily = Lato,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF202530) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = closeSheet
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.X,
                            contentDescription = stringResource(R.string.action_cancel),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (removedHistory.isNotEmpty()) {
                val lastPkg = removedHistory.lastOrNull()
                val lastAppName = lastPkg?.let { appMap[it]?.appName } ?: ""
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color(0xFF1E2430) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                        .border(1.dp, itemBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lastAppName.isNotBlank()) "Removed $lastAppName" else "Removed app",
                        style = Typography.bodySmall.copy(
                            fontFamily = Lato,
                            fontSize = 12.5.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .clickable(onClick = onUndo)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = LucideIcons.Undo2,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = stringResource(R.string.action_undo),
                                style = Typography.labelSmall.copy(
                                    fontFamily = Lato,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    fontSize = 11.5.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (selectedAppList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_selected_apps_empty),
                        style = Typography.bodyMedium.copy(
                            fontFamily = Lato,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedAppList, key = { it.packageName }) { app ->
                        var icon by remember(app.packageName) { mutableStateOf(appIconCache.get(app.packageName)) }
                        LaunchedEffect(app.packageName) {
                            if (icon == null) {
                                val decoded = withContext(Dispatchers.IO) {
                                    try {
                                        packageManager.getApplicationIcon(app.packageName)
                                            .toBitmap(APP_ICON_DECODE_SIZE_PX, APP_ICON_DECODE_SIZE_PX)
                                            .asImageBitmap()
                                    } catch (_: Exception) {
                                        null
                                    }
                                }
                                if (decoded != null) {
                                    appIconCache.put(app.packageName, decoded)
                                    icon = decoded
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(
                                    fadeInSpec = androidx.compose.animation.core.tween(150),
                                    fadeOutSpec = androidx.compose.animation.core.tween(150),
                                    placementSpec = androidx.compose.animation.core.tween(
                                        durationMillis = 200,
                                        easing = androidx.compose.animation.core.FastOutSlowInEasing
                                    )
                                )
                                .clip(RoundedCornerShape(14.dp))
                                .background(itemBg)
                                .border(1.dp, itemBorder, RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                icon?.let { bitmap ->
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = app.appName,
                                        modifier = Modifier.size(38.dp).clip(RoundedCornerShape(9.dp))
                                    )
                                } ?: Box(modifier = Modifier.size(38.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.appName,
                                        style = Typography.titleSmall.copy(
                                            fontFamily = Lato,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                            fontSize = 14.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = app.packageName,
                                        style = Typography.bodySmall.copy(
                                            fontFamily = Lato,
                                            fontSize = 11.sp,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Normal
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF242A36) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { onDeselect(app.packageName) }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = LucideIcons.X,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = closeSheet
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.action_done),
                    style = Typography.labelMedium.copy(
                        fontFamily = Lato,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        fontSize = 14.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun AppGridItem(
    app: InstalledApp,
    packageManager: PackageManager,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "app_item_scale"
    )

    var icon by remember(app.packageName) { mutableStateOf(appIconCache.get(app.packageName)) }
    LaunchedEffect(app.packageName) {
        if (icon == null) {
            val decoded = withContext(Dispatchers.IO) {
                try {
                    packageManager.getApplicationIcon(app.packageName)
                        .toBitmap(APP_ICON_DECODE_SIZE_PX, APP_ICON_DECODE_SIZE_PX)
                        .asImageBitmap()
                } catch (_: Exception) {
                    null
                }
            }
            if (decoded != null) {
                appIconCache.put(app.packageName, decoded)
                icon = decoded
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier.size(60.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f)
                    )
                    .border(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.40f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                icon?.let { bitmap ->
                    Image(
                        bitmap = bitmap,
                        contentDescription = app.appName,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(11.dp))
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LucideIcons.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = app.appName,
            style = Typography.labelSmall.copy(
                fontFamily = Lato,
                fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                fontSize = 10.5.sp,
                lineHeight = 13.sp
            ),
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
