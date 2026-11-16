package com.masum.cipher.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.masum.cipher.ui.components.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.masum.cipher.ui.components.CipherSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.R
import androidx.compose.ui.text.style.TextOverflow
import com.masum.cipher.core.data.local.entity.AccountEntity
import com.masum.cipher.core.domain.model.AccountItem
import com.masum.cipher.core.util.AppFormatters
import com.masum.cipher.core.util.MathEvaluator
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.accounts.getAccountIconVector
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.ArrowLeftRight
import compose.icons.lucideicons.Check
import compose.icons.lucideicons.ChevronDown
import compose.icons.lucideicons.X
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import java.util.Locale
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransferFundsSheet(
    accounts: List<AccountItem>,
    initialSourceAccountId: Long? = null,
    currencySymbol: String = "₹",
    locale: Locale = Locale.getDefault(),
    isHapticsEnabled: Boolean = true,
    onDismiss: () -> Unit,
    onConfirmTransfer: (fromAccount: AccountEntity, toAccount: AccountEntity, amount: Double, note: String?) -> Unit
) {
    if (accounts.size < 2) {
        return
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val initialSource = accounts.find { it.id == initialSourceAccountId } ?: accounts.first()
    val initialDest = accounts.find { it.id != initialSource.id } ?: accounts.last()

    var sourceAccount by remember { mutableStateOf(initialSource) }
    var destAccount by remember { mutableStateOf(initialDest) }
    var amountInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val amountFocusRequester = remember { FocusRequester() }

    val dm = DMSans
    val accent = MaterialTheme.colorScheme.primary
    val errSameAccount = stringResource(R.string.transfer_error_same_account)
    val errInvalidAmount = stringResource(R.string.transfer_error_invalid_amount)

    CipherSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier
            .statusBarsPadding()
            .padding(top = 16.dp),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FormEyebrow(text = stringResource(R.string.transfer_subtitle))
                Text(
                    text = stringResource(R.string.transfer_title),
                    style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, letterSpacing = (-0.5).sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(16.dp))

            FormHeroAmount(
                value = amountInput,
                onValueChange = { input ->
                    amountInput = input.filter { it.isDigit() || it == '.' || it == '+' || it == '-' || it == '*' || it == '/' }
                    errorMessage = null
                },
                currencySymbol = currencySymbol,
                accent = accent,
                placeholder = "0",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                focusRequester = amountFocusRequester
            )

            Spacer(Modifier.height(22.dp))

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    Triple(stringResource(R.string.transfer_from), sourceAccount, true),
                    Triple(stringResource(R.string.transfer_to), destAccount, false)
                ).forEachIndexed { index, (label, selected, isSource) ->
                    if (index == 1) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(accent.copy(alpha = 0.14f))
                                    .clickable {
                                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                                        val tmp = sourceAccount
                                        sourceAccount = destAccount
                                        destAccount = tmp
                                        errorMessage = null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = LucideIcons.ArrowLeftRight,
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                    val tint = Color(selected.entity.colorHex)
                    FormGroup {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(15.dp))
                                    .background(tint.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = com.masum.cipher.ui.accounts.getAccountIconVector(selected.entity.iconName),
                                    contentDescription = null,
                                    tint = tint,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                FormEyebrow(text = label)
                                Text(
                                    text = selected.name,
                                    style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = AppFormatters.formatCompactCurrency(selected.currentBalance, currencySymbol, locale),
                                style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontWeight = FontWeight.Medium, fontSize = 13.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(start = 18.dp, end = 18.dp, bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            accounts.forEach { acc ->
                                FormSelectableChip(
                                    label = acc.name,
                                    selected = acc.id == selected.id,
                                    accent = Color(acc.entity.colorHex),
                                    onClick = {
                                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                                        if (isSource) sourceAccount = acc else destAccount = acc
                                        errorMessage = null
                                    }
                                )
                            }
                        }
                    }
                }

                FormGroup {
                    FormTextRow(
                        icon = LucideIcons.Check,
                        label = stringResource(R.string.transfer_note_label),
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        placeholder = stringResource(R.string.transfer_note_placeholder),
                        accent = accent,
                        keyboardOptions = KeyboardOptions(capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage.orEmpty(),
                        style = androidx.compose.ui.text.TextStyle(fontFamily = dm, fontSize = 12.5.sp),
                        color = RoseExpense,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            FormPrimaryButton(
                text = stringResource(R.string.transfer_action_btn),
                enabled = true,
                accent = accent,
                background = Color.Transparent,
                onClick = {
                    view.performVibrate(isHapticsEnabled, isLongPress = true)
                    val parsedAmount = MathEvaluator.evaluate(amountInput)
                    if (sourceAccount.id == destAccount.id) {
                        errorMessage = errSameAccount
                        return@FormPrimaryButton
                    }
                    if (parsedAmount == null || parsedAmount <= 0.0) {
                        errorMessage = errInvalidAmount
                        return@FormPrimaryButton
                    }
                    keyboardController?.hide()
                    onConfirmTransfer(
                        sourceAccount.entity,
                        destAccount.entity,
                        parsedAmount,
                        noteInput.trim().ifBlank { null }
                    )
                    onDismiss()
                }
            )
        }
    }
}
