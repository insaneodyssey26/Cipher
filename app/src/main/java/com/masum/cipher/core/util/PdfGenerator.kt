package com.masum.cipher.core.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.res.ResourcesCompat
import com.masum.cipher.R
import com.masum.cipher.core.data.local.entity.TransactionEntity
import java.io.OutputStream
import java.util.Date
import java.util.Locale

object PdfGenerator {
    private val paletteColors = listOf(
        Color.rgb(79, 70, 229),
        Color.rgb(225, 29, 72),
        Color.rgb(5, 150, 105),
        Color.rgb(217, 119, 6),
        Color.rgb(8, 145, 178),
        Color.rgb(147, 51, 234),
        Color.rgb(236, 72, 153),
        Color.rgb(100, 116, 139)
    )

    fun generateStatement(
        context: Context,
        transactions: List<TransactionEntity>,
        outputStream: OutputStream,
        currencySymbol: String = com.masum.cipher.core.domain.model.AppCurrency.detectDefault().symbol,
        splitsMap: Map<Long, List<com.masum.cipher.core.data.local.entity.TransactionSplitEntity>> = emptyMap(),
        accountName: String? = null
    ) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        
        val colorTextPrimary = Color.rgb(17, 24, 39)
        val colorTextSecondary = Color.rgb(107, 114, 128)
        val colorBackgroundMuted = Color.rgb(249, 250, 251)
        val colorBorder = Color.rgb(229, 231, 235)
        val colorIncome = Color.rgb(5, 150, 105)
        val colorExpense = Color.rgb(225, 29, 72)
        val colorNeutralBar = Color.rgb(243, 244, 246)

        val logoTypeface = try {
            ResourcesCompat.getFont(context, R.font.dmsans_variable)
        } catch (_: Exception) {
            Typeface.DEFAULT
        }
        
        val normalTypeface = try {
            ResourcesCompat.getFont(context, R.font.dmsans_variable)
        } catch (_: Exception) {
            Typeface.DEFAULT
        }

        val logoPaint = Paint().apply {
            color = colorTextPrimary
            textSize = 26f
            typeface = Typeface.create(logoTypeface, Typeface.BOLD)
            isAntiAlias = true
            letterSpacing = -0.02f
        }
        
        val titlePaint = Paint().apply {
            color = colorTextPrimary
            textSize = 10.5f
            typeface = Typeface.create(normalTypeface, Typeface.BOLD)
            isAntiAlias = true
            letterSpacing = 0.12f
        }
        
        val sectionTitlePaint = Paint().apply {
            color = colorTextSecondary
            textSize = 8.5f
            typeface = Typeface.create(normalTypeface, Typeface.BOLD)
            isAntiAlias = true
            letterSpacing = 0.1f
        }

        val labelPaint = Paint().apply {
            color = colorTextSecondary
            textSize = 9f
            typeface = Typeface.create(normalTypeface, Typeface.NORMAL)
            isAntiAlias = true
            letterSpacing = 0.03f
        }
        
        val valuePaint = Paint().apply {
            color = colorTextPrimary
            textSize = 12.5f
            typeface = Typeface.create(normalTypeface, Typeface.BOLD)
            isAntiAlias = true
        }
        
        val headerBackgroundPaint = Paint().apply {
            color = colorBackgroundMuted
            style = Paint.Style.FILL
        }
        
        val tableHeaderPaint = Paint().apply {
            color = colorTextSecondary
            textSize = 8.5f
            typeface = Typeface.create(normalTypeface, Typeface.BOLD)
            isAntiAlias = true
            letterSpacing = 0.08f
        }
        
        val rowTextPaint = Paint().apply {
            color = colorTextPrimary
            textSize = 10f
            typeface = Typeface.create(normalTypeface, Typeface.NORMAL)
            isAntiAlias = true
        }
        
        val incomePaint = Paint().apply {
            color = colorIncome
            textSize = 10.5f
            typeface = Typeface.create(normalTypeface, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        
        val expensePaint = Paint().apply {
            color = colorExpense
            textSize = 10.5f
            typeface = Typeface.create(normalTypeface, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        
        val linePaint = Paint().apply {
            color = colorBorder
            strokeWidth = 1f
        }

        val rowBackgroundPaint = Paint().apply {
            color = Color.rgb(251, 251, 253)
            style = Paint.Style.FILL
        }

        val donutPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 22f
            isAntiAlias = true
            strokeCap = Paint.Cap.BUTT
        }

        val barFillPaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val barTrackPaint = Paint().apply {
            color = colorNeutralBar
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val marginX = 45f
        val rightMargin = pageInfo.pageWidth - marginX
        val contentWidth = rightMargin - marginX

        val colDate = marginX + 10f
        val colMerchant = colDate + 85f
        val colCategory = colMerchant + 185f
        val colAmount = rightMargin - 10f
        
        val locale = Locale.getDefault()
        val dateFormatter = AppFormatters.getFullDate(locale)
        val rowDateFormatter = AppFormatters.getDay(locale)
        
        fun formatMoney(amount: Double): String = AppFormatters.formatCurrency(amount, currencySymbol, locale, decimals = 2)

        if (transactions.isEmpty()) {
            val page = document.startPage(pageInfo)
            page.canvas.drawText("No transactions found.", marginX, 100f, titlePaint)
            document.finishPage(page)
            document.writeTo(outputStream)
            document.close()
            return
        }

        val expenseTransactions = transactions.filter { !it.isIncome }
        val incomeTransactions = transactions.filter { it.isIncome }
        val totalExpense = expenseTransactions.sumOf { it.amount }
        val totalIncome = incomeTransactions.sumOf { it.amount }
        val net = totalIncome - totalExpense

        val categorySpendingList = if (totalExpense > 0) {
            val grouped = expenseTransactions
                .groupBy { it.category.trim().ifEmpty { "Other" }.replaceFirstChar { char -> char.uppercase() } }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
                .toList()
                .sortedByDescending { it.second }
            
            if (grouped.size <= 5) {
                grouped
            } else {
                val top4 = grouped.take(4)
                val otherSum = grouped.drop(4).sumOf { it.second }
                top4 + listOf("Other" to otherSum)
            }
        } else {
            emptyList()
        }

        val topMerchantsList = if (totalExpense > 0) {
            expenseTransactions
                .groupBy { it.merchant.trim().ifEmpty { "Unknown" } }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
                .toList()
                .sortedByDescending { it.second }
                .take(4)
        } else {
            emptyList()
        }

        val largestExpense = expenseTransactions.maxByOrNull { it.amount }
        val avgExpense = if (expenseTransactions.isNotEmpty()) totalExpense / expenseTransactions.size else 0.0
        val savingsRate = if (totalIncome > 0) ((net.coerceAtLeast(0.0) / totalIncome) * 100).toInt() else 0

        val itemsPerPage = 16
        val transactionChunks = transactions.chunked(itemsPerPage)
        val totalPages = 1 + transactionChunks.size

        val dashPage = document.startPage(pageInfo)
        val dashCanvas = dashPage.canvas
        var currentY = 42f

        dashCanvas.drawText("cipher.", marginX, currentY, logoPaint)
        titlePaint.textAlign = Paint.Align.RIGHT
        val reportTitle = if (!accountName.isNullOrBlank()) "ACCOUNT STATEMENT — ${accountName.uppercase()}" else "EXECUTIVE FINANCIAL REPORT"
        dashCanvas.drawText(reportTitle, rightMargin, currentY - 8f, titlePaint)
        titlePaint.textAlign = Paint.Align.LEFT

        currentY += 26f
        dashCanvas.drawLine(marginX, currentY, rightMargin, currentY, linePaint)
        currentY += 18f

        val generatedDate = dateFormatter.format(Date())
        val startDate = dateFormatter.format(Date(transactions.last().timestamp))
        val endDate = dateFormatter.format(Date(transactions.first().timestamp))

        dashCanvas.drawText("STATEMENT PERIOD", marginX, currentY, labelPaint)
        dashCanvas.drawText("$startDate — $endDate", marginX, currentY + 15f, valuePaint)

        labelPaint.textAlign = Paint.Align.RIGHT
        valuePaint.textAlign = Paint.Align.RIGHT
        dashCanvas.drawText("GENERATED ON", rightMargin, currentY, labelPaint)
        dashCanvas.drawText(generatedDate, rightMargin, currentY + 15f, valuePaint)
        labelPaint.textAlign = Paint.Align.LEFT
        valuePaint.textAlign = Paint.Align.LEFT

        currentY += 36f

        val cardSpacing = 8f
        val cardWidth = (contentWidth - (cardSpacing * 2)) / 3f
        val cardHeight = 46f
        val card2X = marginX + cardWidth + cardSpacing
        val card3X = card2X + cardWidth + cardSpacing

        dashCanvas.drawRoundRect(marginX, currentY, marginX + cardWidth, currentY + cardHeight, 6f, 6f, headerBackgroundPaint)
        dashCanvas.drawText("TOTAL INCOME", marginX + 10f, currentY + 18f, labelPaint)
        incomePaint.textAlign = Paint.Align.LEFT
        incomePaint.textSize = 12f
        dashCanvas.drawText(formatMoney(totalIncome), marginX + 10f, currentY + 36f, incomePaint)
        incomePaint.textAlign = Paint.Align.RIGHT
        incomePaint.textSize = 10.5f

        dashCanvas.drawRoundRect(card2X, currentY, card2X + cardWidth, currentY + cardHeight, 6f, 6f, headerBackgroundPaint)
        dashCanvas.drawText("TOTAL EXPENSE", card2X + 10f, currentY + 18f, labelPaint)
        expensePaint.textAlign = Paint.Align.LEFT
        expensePaint.textSize = 12f
        dashCanvas.drawText(formatMoney(totalExpense), card2X + 10f, currentY + 36f, expensePaint)
        expensePaint.textAlign = Paint.Align.RIGHT
        expensePaint.textSize = 10.5f

        dashCanvas.drawRoundRect(card3X, currentY, card3X + cardWidth, currentY + cardHeight, 6f, 6f, headerBackgroundPaint)
        dashCanvas.drawText("NET SAVINGS ($savingsRate%)", card3X + 10f, currentY + 18f, labelPaint)
        val netPaint = if (net >= 0) incomePaint else expensePaint
        netPaint.textAlign = Paint.Align.LEFT
        netPaint.textSize = 12f
        val netStr = formatMoney(kotlin.math.abs(net))
        val sign = if (net > 0) "+" else if (net < 0) "-" else ""
        dashCanvas.drawText("$sign$netStr", card3X + 10f, currentY + 36f, netPaint)
        netPaint.textAlign = Paint.Align.RIGHT
        netPaint.textSize = 10.5f

        currentY += cardHeight + 24f

        dashCanvas.drawText("SPENDING BY CATEGORY", marginX, currentY, sectionTitlePaint)
        currentY += 12f

        val donutBoxHeight = 150f
        dashCanvas.drawRoundRect(marginX, currentY, rightMargin, currentY + donutBoxHeight, 8f, 8f, headerBackgroundPaint)

        if (totalExpense > 0 && categorySpendingList.isNotEmpty()) {
            val donutCenterX = marginX + 85f
            val donutCenterY = currentY + (donutBoxHeight / 2f)
            val donutRadius = 45f
            val donutRect = android.graphics.RectF(
                donutCenterX - donutRadius,
                donutCenterY - donutRadius,
                donutCenterX + donutRadius,
                donutCenterY + donutRadius
            )

            var startAngle = -90f
            categorySpendingList.forEachIndexed { catIdx, item ->
                val sweepAngle = ((item.second / totalExpense) * 360f).toFloat()
                donutPaint.color = paletteColors[catIdx % paletteColors.size]
                dashCanvas.drawArc(donutRect, startAngle, sweepAngle, false, donutPaint)
                startAngle += sweepAngle
            }

            val centerLabelPaint = Paint(labelPaint).apply {
                textSize = 7.5f
                textAlign = Paint.Align.CENTER
            }
            val centerValPaint = Paint(valuePaint).apply {
                textSize = 10.5f
                textAlign = Paint.Align.CENTER
            }
            dashCanvas.drawText("TOTAL SPENT", donutCenterX, donutCenterY - 4f, centerLabelPaint)
            dashCanvas.drawText(formatMoney(totalExpense), donutCenterX, donutCenterY + 11f, centerValPaint)

            val legendStartX = marginX + 180f
            val legendWidth = rightMargin - legendStartX - 16f
            val maxRows = categorySpendingList.size.coerceAtMost(5)
            val rowSpacing = (donutBoxHeight - 20f) / maxRows

            categorySpendingList.take(maxRows).forEachIndexed { catIdx, item ->
                val rowY = currentY + 20f + (catIdx * rowSpacing)
                val catColor = paletteColors[catIdx % paletteColors.size]
                val dotRadius = 3.5f

                barFillPaint.color = catColor
                dashCanvas.drawCircle(legendStartX + dotRadius, rowY - 3f, dotRadius, barFillPaint)

                val percent = ((item.second / totalExpense) * 100).toInt()
                val catName = if (item.first.length > 18) item.first.take(16) + ".." else item.first
                
                val catNamePaint = Paint(rowTextPaint).apply {
                    textSize = 9f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
                dashCanvas.drawText(catName, legendStartX + 14f, rowY, catNamePaint)

                val amtText = "${formatMoney(item.second)} ($percent%)"
                val amtPaint = Paint(labelPaint).apply {
                    textSize = 8.5f
                    textAlign = Paint.Align.RIGHT
                    color = colorTextPrimary
                }
                dashCanvas.drawText(amtText, rightMargin - 16f, rowY, amtPaint)

                val barTrackY = rowY + 5f
                val barTrackHeight = 3f
                dashCanvas.drawRoundRect(legendStartX + 14f, barTrackY, rightMargin - 16f, barTrackY + barTrackHeight, 1.5f, 1.5f, barTrackPaint)
                val fillWidth = ((item.second / totalExpense) * (legendWidth - 14f)).toFloat()
                dashCanvas.drawRoundRect(legendStartX + 14f, barTrackY, legendStartX + 14f + fillWidth, barTrackY + barTrackHeight, 1.5f, 1.5f, barFillPaint)
            }
        } else {
            val emptyPaint = Paint(labelPaint).apply {
                textAlign = Paint.Align.CENTER
                textSize = 10f
            }
            dashCanvas.drawText("No expense records in this statement period.", marginX + (contentWidth / 2f), currentY + (donutBoxHeight / 2f), emptyPaint)
        }

        currentY += donutBoxHeight + 20f

        dashCanvas.drawText("TOP SPENDING MERCHANTS & OUTFLOWS", marginX, currentY, sectionTitlePaint)
        currentY += 12f

        val merchantBoxHeight = 120f
        dashCanvas.drawRoundRect(marginX, currentY, rightMargin, currentY + merchantBoxHeight, 8f, 8f, headerBackgroundPaint)

        if (topMerchantsList.isNotEmpty() && totalExpense > 0) {
            val maxMerchantRows = topMerchantsList.size.coerceAtMost(4)
            val merchantRowSpacing = (merchantBoxHeight - 16f) / maxMerchantRows
            val maxMerchantAmount = topMerchantsList.first().second

            topMerchantsList.forEachIndexed { mIdx, (merchant, amount) ->
                val rowY = currentY + 18f + (mIdx * merchantRowSpacing)
                val merchantName = if (merchant.length > 24) merchant.take(22) + ".." else merchant
                
                val mNamePaint = Paint(rowTextPaint).apply {
                    textSize = 9f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
                dashCanvas.drawText("${mIdx + 1}. $merchantName", marginX + 16f, rowY, mNamePaint)

                val amtPaint = Paint(labelPaint).apply {
                    textSize = 8.5f
                    textAlign = Paint.Align.RIGHT
                    color = colorTextPrimary
                }
                dashCanvas.drawText(formatMoney(amount), rightMargin - 16f, rowY, amtPaint)

                val barStartX = marginX + 16f
                val barEndX = rightMargin - 16f
                val barAvailableWidth = barEndX - barStartX
                val barTrackY = rowY + 5f
                val barTrackHeight = 3.5f

                dashCanvas.drawRoundRect(barStartX, barTrackY, barEndX, barTrackY + barTrackHeight, 2f, 2f, barTrackPaint)
                barFillPaint.color = Color.rgb(79, 70, 229)
                val fillWidth = ((amount / maxMerchantAmount) * barAvailableWidth).toFloat()
                dashCanvas.drawRoundRect(barStartX, barTrackY, barStartX + fillWidth, barTrackY + barTrackHeight, 2f, 2f, barFillPaint)
            }
        } else {
            val emptyPaint = Paint(labelPaint).apply {
                textAlign = Paint.Align.CENTER
                textSize = 10f
            }
            dashCanvas.drawText("No merchant expenses recorded.", marginX + (contentWidth / 2f), currentY + (merchantBoxHeight / 2f), emptyPaint)
        }

        currentY += merchantBoxHeight + 20f

        dashCanvas.drawText("FINANCIAL HEALTH & KEY METRICS", marginX, currentY, sectionTitlePaint)
        currentY += 12f

        val gridCols = 2
        val gridRows = 2
        val gridSpacing = 8f
        val gridBoxWidth = (contentWidth - gridSpacing) / 2f
        val gridBoxHeight = 44f

        val metrics = listOf(
            "LARGEST EXPENSE" to (largestExpense?.let { "${it.merchant} (${formatMoney(it.amount)})" } ?: "None"),
            "AVG EXPENSE / TX" to formatMoney(avgExpense),
            "TOTAL TRANSACTIONS" to "${transactions.size} (${expenseTransactions.size} out / ${incomeTransactions.size} in)",
            "SAVINGS RATIO" to "$savingsRate% of inflow retained"
        )

        metrics.forEachIndexed { index, (label, valText) ->
            val col = index % gridCols
            val row = index / gridCols
            val boxX = marginX + (col * (gridBoxWidth + gridSpacing))
            val boxY = currentY + (row * (gridBoxHeight + gridSpacing))

            dashCanvas.drawRoundRect(boxX, boxY, boxX + gridBoxWidth, boxY + gridBoxHeight, 6f, 6f, headerBackgroundPaint)
            dashCanvas.drawText(label, boxX + 10f, boxY + 16f, labelPaint)
            
            val metricValPaint = Paint(valuePaint).apply {
                textSize = 10.5f
            }
            val snippet = if (valText.length > 28) valText.take(26) + ".." else valText
            dashCanvas.drawText(snippet, boxX + 10f, boxY + 33f, metricValPaint)
        }

        currentY += (gridRows * (gridBoxHeight + gridSpacing)) + 14f

        val ledgerNoticePaint = Paint(labelPaint).apply {
            color = Color.rgb(79, 70, 229)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textSize = 9f
            textAlign = Paint.Align.CENTER
        }
        dashCanvas.drawText("→ Detailed Transaction Ledger starts on Page 2", pageInfo.pageWidth / 2f, currentY + 10f, ledgerNoticePaint)

        labelPaint.textAlign = Paint.Align.CENTER
        dashCanvas.drawText("Page 1 of $totalPages · Executive Summary", pageInfo.pageWidth / 2f, pageInfo.pageHeight - 35f, labelPaint)
        labelPaint.textAlign = Paint.Align.LEFT

        document.finishPage(dashPage)

        transactionChunks.forEachIndexed { chunkIndex, chunk ->
            val pageNumber = chunkIndex + 2
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            var ledgerY = 45f
            canvas.drawText("cipher.", marginX, ledgerY, logoPaint.apply { textSize = 18f })
            logoPaint.textSize = 26f

            titlePaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("TRANSACTION LEDGER", rightMargin, ledgerY - 4f, titlePaint)
            titlePaint.textAlign = Paint.Align.LEFT

            ledgerY += 16f
            canvas.drawLine(marginX, ledgerY, rightMargin, ledgerY, linePaint)
            ledgerY += 18f

            canvas.drawRoundRect(marginX, ledgerY, rightMargin, ledgerY + 22f, 4f, 4f, headerBackgroundPaint)
            val tableHeaderY = ledgerY + 14f

            canvas.drawText("DATE", colDate, tableHeaderY, tableHeaderPaint)
            canvas.drawText("MERCHANT", colMerchant, tableHeaderY, tableHeaderPaint)
            canvas.drawText("CATEGORY", colCategory, tableHeaderY, tableHeaderPaint)
            tableHeaderPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("AMOUNT", colAmount, tableHeaderY, tableHeaderPaint)
            tableHeaderPaint.textAlign = Paint.Align.LEFT

            ledgerY += 34f

            val rowHeight = 38f
            chunk.forEachIndexed { rowIndex, tx ->
                if (rowIndex % 2 != 0) {
                    canvas.drawRect(marginX, ledgerY, rightMargin, ledgerY + rowHeight, rowBackgroundPaint)
                }

                val date = rowDateFormatter.format(Date(tx.timestamp))
                val merchant = if (tx.merchant.length > 28) tx.merchant.take(25) + "..." else tx.merchant
                val category = tx.category.uppercase()
                val splits = splitsMap[tx.id]

                if (splits != null && splits.size > 1) {
                    val myShare = splits.find { it.isCurrentUser }?.amount ?: tx.amount
                    val subText = "Split (${splits.size}) · Share: ${formatMoney(myShare)}"
                    canvas.drawText(date, colDate, ledgerY + 18f, rowTextPaint)
                    canvas.drawText(merchant, colMerchant, ledgerY + 16f, rowTextPaint.apply { typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD) })
                    rowTextPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

                    val subPaint = Paint(labelPaint).apply {
                        textSize = 7.5f
                        color = colorTextSecondary
                    }
                    canvas.drawText(subText, colMerchant, ledgerY + 29f, subPaint)

                    labelPaint.textAlign = Paint.Align.LEFT
                    canvas.drawText(category, colCategory, ledgerY + 18f, labelPaint)

                    val amountStr = formatMoney(tx.amount)
                    val paint = if (tx.isIncome) incomePaint else expensePaint
                    canvas.drawText(amountStr, colAmount, ledgerY + 18f, paint)
                } else if (!tx.note.isNullOrBlank()) {
                    val noteSnippet = if (tx.note.length > 25) tx.note.take(22) + "..." else tx.note
                    canvas.drawText(date, colDate, ledgerY + 18f, rowTextPaint)
                    canvas.drawText(merchant, colMerchant, ledgerY + 16f, rowTextPaint.apply { typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD) })
                    rowTextPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

                    val subPaint = Paint(labelPaint).apply {
                        textSize = 7.5f
                        color = colorTextSecondary
                    }
                    canvas.drawText(noteSnippet, colMerchant, ledgerY + 29f, subPaint)

                    labelPaint.textAlign = Paint.Align.LEFT
                    canvas.drawText(category, colCategory, ledgerY + 18f, labelPaint)

                    val amountStr = formatMoney(tx.amount)
                    val paint = if (tx.isIncome) incomePaint else expensePaint
                    canvas.drawText(amountStr, colAmount, ledgerY + 18f, paint)
                } else {
                    val textY = ledgerY + 23f
                    canvas.drawText(date, colDate, textY, rowTextPaint)
                    canvas.drawText(merchant, colMerchant, textY, rowTextPaint.apply { typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD) })
                    rowTextPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

                    labelPaint.textAlign = Paint.Align.LEFT
                    canvas.drawText(category, colCategory, textY, labelPaint)

                    val amountStr = formatMoney(tx.amount)
                    val paint = if (tx.isIncome) incomePaint else expensePaint
                    canvas.drawText(amountStr, colAmount, textY, paint)
                }

                canvas.drawLine(marginX, ledgerY + rowHeight, rightMargin, ledgerY + rowHeight, linePaint)
                ledgerY += rowHeight
            }

            labelPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("Page $pageNumber of $totalPages · Computer-generated document. No signature required.", pageInfo.pageWidth / 2f, pageInfo.pageHeight - 35f, labelPaint)
            labelPaint.textAlign = Paint.Align.LEFT

            document.finishPage(page)
        }

        document.writeTo(outputStream)
        document.close()
    }
}
