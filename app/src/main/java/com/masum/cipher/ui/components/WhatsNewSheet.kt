package com.masum.cipher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import com.masum.cipher.ui.components.CipherSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.BuildConfig
import com.masum.cipher.R
import compose.icons.LucideIcons
import compose.icons.lucideicons.ChevronDown
import compose.icons.lucideicons.Sparkles
import androidx.compose.material3.Icon
import com.masum.cipher.ui.components.clickable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.animation.animateContentSize
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.Typography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsNewSheet(
    onDismiss: () -> Unit,
    onContinue: () -> Unit,
    onExplorePro: (() -> Unit)? = null,
    isPro: Boolean = false,
    versionName: String = BuildConfig.VERSION_NAME
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    val consumeOverscrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset = Offset.Zero
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset = available
            override suspend fun onPreFling(available: Velocity): Velocity = Velocity.Zero
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity = available
        }
    }

    CipherSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.82f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.whats_new_header_title),
                        style = androidx.compose.ui.text.TextStyle(fontFamily = DMSans, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, letterSpacing = (-0.9).sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "v$versionName",
                        style = androidx.compose.ui.text.TextStyle(fontFamily = DMSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.whats_new_header_desc),
                    style = androidx.compose.ui.text.TextStyle(fontFamily = DMSans, fontSize = 13.5.sp, lineHeight = 19.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .nestedScroll(consumeOverscrollConnection)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WhatsNewVersionSection(
                    versionTitle = "v6.5.0",
                    isLatest = true
                ) {
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v650_look_title),
                        description = stringResource(R.string.whats_new_v650_look_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v650_textsize_title),
                        description = stringResource(R.string.whats_new_v650_textsize_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v650_goals_title),
                        description = stringResource(R.string.whats_new_v650_goals_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v650_search_title),
                        description = stringResource(R.string.whats_new_v650_search_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v650_fixes_title),
                        description = stringResource(R.string.whats_new_v650_fixes_desc)
                    )
                }

                WhatsNewVersionSection(
                    versionTitle = "v6.4.1"
                ) {
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v641_migration_title),
                        description = stringResource(R.string.whats_new_v641_migration_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v641_password_title),
                        description = stringResource(R.string.whats_new_v641_password_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v641_performance_title),
                        description = stringResource(R.string.whats_new_v641_performance_desc)
                    )
                }

                WhatsNewVersionSection(
                    versionTitle = "v6.4.0"
                ) {
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v640_billing_title),
                        description = stringResource(R.string.whats_new_v640_billing_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v640_onboarding_title),
                        description = stringResource(R.string.whats_new_v640_onboarding_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v640_duplicates_title),
                        description = stringResource(R.string.whats_new_v640_duplicates_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v640_notifications_title),
                        description = stringResource(R.string.whats_new_v640_notifications_desc)
                    )
                }

                WhatsNewVersionSection(
                    versionTitle = "v6.3.0"
                ) {
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v630_splash_title),
                        description = stringResource(R.string.whats_new_v630_splash_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v630_updater_title),
                        description = stringResource(R.string.whats_new_v630_updater_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v630_restore_title),
                        description = stringResource(R.string.whats_new_v630_restore_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v630_performance_title),
                        description = stringResource(R.string.whats_new_v630_performance_desc)
                    )
                }

                WhatsNewVersionSection(
                    versionTitle = "v6.2.8"
                ) {
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v628_bento_title),
                        description = stringResource(R.string.whats_new_v628_bento_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v628_icons_title),
                        description = stringResource(R.string.whats_new_v628_icons_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v628_performance_title),
                        description = stringResource(R.string.whats_new_v628_performance_desc)
                    )
                }

                WhatsNewVersionSection(
                    versionTitle = "v6.2.6"
                ) {
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v626_terms_title),
                        description = stringResource(R.string.whats_new_v626_terms_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v626_layout_title),
                        description = stringResource(R.string.whats_new_v626_layout_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v626_reliability_title),
                        description = stringResource(R.string.whats_new_v626_reliability_desc)
                    )
                }

                WhatsNewVersionSection(
                    versionTitle = "v6.2.5"
                ) {
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v625_font_title),
                        description = stringResource(R.string.whats_new_v625_font_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v625_promo_title),
                        description = stringResource(R.string.whats_new_v625_promo_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v625_anim_title),
                        description = stringResource(R.string.whats_new_v625_anim_desc)
                    )
                }

                WhatsNewVersionSection(
                    versionTitle = "v6.2.0"
                ) {
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v620_debts_title),
                        description = stringResource(R.string.whats_new_v620_debts_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v620_account_budgets_title),
                        description = stringResource(R.string.whats_new_v620_account_budgets_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v620_widgets_title),
                        description = stringResource(R.string.whats_new_v620_widgets_desc)
                    )
                }

                WhatsNewVersionSection(
                    versionTitle = "v6.1.6"
                ) {
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v616_manage_title),
                        description = stringResource(R.string.whats_new_v616_manage_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v616_license_title),
                        description = stringResource(R.string.whats_new_v616_license_desc)
                    )
                }

                WhatsNewVersionSection(
                    versionTitle = "v6.1.5"
                ) {
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v615_restore_title),
                        description = stringResource(R.string.whats_new_v615_restore_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v615_password_title),
                        description = stringResource(R.string.whats_new_v615_password_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v615_welcome_title),
                        description = stringResource(R.string.whats_new_v615_welcome_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v615_reliable_title),
                        description = stringResource(R.string.whats_new_v615_reliable_desc)
                    )
                }

                WhatsNewVersionSection(
                    versionTitle = "v6.1.2"
                ) {
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v612_battery_title),
                        description = stringResource(R.string.whats_new_v612_battery_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v612_speed_title),
                        description = stringResource(R.string.whats_new_v612_speed_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v612_updates_title),
                        description = stringResource(R.string.whats_new_v612_updates_desc)
                    )
                }

                WhatsNewVersionSection(
                    versionTitle = "v6.1.1"
                ) {
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v611_category_btn_title),
                        description = stringResource(R.string.whats_new_v611_category_btn_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v611_quick_edit_title),
                        description = stringResource(R.string.whats_new_v611_quick_edit_desc)
                    )
                }

                WhatsNewVersionSection(
                    versionTitle = "v6.1.0"
                ) {
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v610_transfers_title),
                        description = stringResource(R.string.whats_new_v610_transfers_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v610_transfer_names_title),
                        description = stringResource(R.string.whats_new_v610_transfer_names_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v610_palette_title),
                        description = stringResource(R.string.whats_new_v610_palette_desc)
                    )
                }

                WhatsNewVersionSection(
                    versionTitle = "v6.0.8"
                ) {
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v608_screen_title),
                        description = stringResource(R.string.whats_new_v608_screen_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v608_button_title),
                        description = stringResource(R.string.whats_new_v608_button_desc)
                    )
                }

                WhatsNewVersionSection(
                    versionTitle = "v6.0.7"
                ) {
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v607_goals_title),
                        description = stringResource(R.string.whats_new_v607_goals_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v607_original_message_title),
                        description = stringResource(R.string.whats_new_v607_original_message_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v607_category_palette_title),
                        description = stringResource(R.string.whats_new_v607_category_palette_desc)
                    )
                }

                WhatsNewVersionSection(
                    versionTitle = "v6.0.2"
                ) {
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v60_widgets_title),
                        description = stringResource(R.string.whats_new_v60_widgets_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v60_statements_title),
                        description = stringResource(R.string.whats_new_v60_statements_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v60_analytics_title),
                        description = stringResource(R.string.whats_new_v60_analytics_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v60_pro_title),
                        description = stringResource(R.string.whats_new_v60_pro_desc)
                    )
                    WhatsNewFeatureEntry(
                        title = stringResource(R.string.whats_new_v60_onboarding_title),
                        description = stringResource(R.string.whats_new_v60_onboarding_desc)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (onExplorePro != null && !isPro) {
                    Button(
                        onClick = { com.masum.cipher.ui.components.AppHaptics.tick();
                            coroutineScope.launch {
                                sheetState.hide()
                            }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    onExplorePro()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = "Explore Cipher Pro ✨",
                            style = Typography.titleMedium.copy(
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Button(
                        onClick = { com.masum.cipher.ui.components.AppHaptics.tick();
                            coroutineScope.launch {
                                sheetState.hide()
                            }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    onContinue()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.whats_new_btn_ack),
                            style = Typography.titleSmall.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Button(
                        onClick = { com.masum.cipher.ui.components.AppHaptics.tick();
                            coroutineScope.launch {
                                sheetState.hide()
                            }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    onContinue()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = stringResource(R.string.whats_new_btn_ack),
                            style = Typography.titleMedium.copy(
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WhatsNewVersionSection(
    versionTitle: String,
    isLatest: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by androidx.compose.runtime.saveable.rememberSaveable(versionTitle) { mutableStateOf(isLatest) }
    val chevron by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "whats_new_chevron"
    )
    val shape = RoundedCornerShape(26.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isLatest) Modifier else Modifier.clickable { expanded = !expanded })
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = versionTitle,
                style = androidx.compose.ui.text.TextStyle(fontFamily = DMSans, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, letterSpacing = (-0.3).sp),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (isLatest) {
                Text(
                    text = stringResource(R.string.whats_new_badge_latest),
                    style = androidx.compose.ui.text.TextStyle(fontFamily = DMSans, fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            } else {
                Icon(
                    imageVector = LucideIcons.ChevronDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer { rotationZ = chevron }
                )
            }
        }
        if (expanded) {
            Column(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun WhatsNewFeatureEntry(
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                style = androidx.compose.ui.text.TextStyle(fontFamily = DMSans, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = androidx.compose.ui.text.TextStyle(fontFamily = DMSans, fontSize = 13.sp, lineHeight = 19.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
