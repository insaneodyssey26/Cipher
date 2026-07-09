package com.masum.cipher.core.util

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.masum.cipher.core.data.local.entity.TransactionEntity
import java.io.OutputStream
import java.util.Date
import kotlin.math.max

object PdfGenerator {
    fun generateStatement(context: android.content.Context, transactions: List<TransactionEntity>, outputStream: OutputStream) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 standard size
        
        // Define premium color palette
        val colorBrand = Color.rgb(99, 102, 241) // Indigo
        val colorTextPrimary = Color.rgb(17, 24, 39)
        val colorTextSecondary = Color.rgb(107, 114, 128)
        val colorBackgroundMuted = Color.rgb(249, 250, 251)
        val colorBorder = Color.rgb(229, 231, 235)
        val colorIncome = Color.rgb(5, 150, 105)
        val colorExpense = Color.rgb(225, 29, 72)

        val logoTypeface = try {
            androidx.core.content.res.ResourcesCompat.getFont(context, com.masum.cipher.R.font.spacegrotesk_variable)
        } catch (e: Exception) {
            Typeface.DEFAULT
        }
        
        val normalTypeface = try {
            androidx.core.content.res.ResourcesCompat.getFont(context, com.masum.cipher.R.font.geist_variablefont_weight)
        } catch (e: Exception) {
            Typeface.DEFAULT
        }

        // Define paints
        val logoPaint = Paint().apply {
            color = colorTextPrimary // Black
            textSize = 28f
            typeface = Typeface.create(logoTypeface, Typeface.BOLD)
            isAntiAlias = true
            letterSpacing = -0.02f // Negative letter spacing
        }
        
        val titlePaint = Paint().apply {
            color = colorTextPrimary
            textSize = 12f
            typeface = Typeface.create(normalTypeface, Typeface.BOLD)
            isAntiAlias = true
            letterSpacing = 0.15f
        }
        
        val labelPaint = Paint().apply {
            color = colorTextSecondary
            textSize = 10f
            typeface = Typeface.create(normalTypeface, Typeface.NORMAL)
            isAntiAlias = true
            letterSpacing = 0.05f
        }
        
        val valuePaint = Paint().apply {
            color = colorTextPrimary
            textSize = 14f
            typeface = Typeface.create(normalTypeface, Typeface.BOLD)
            isAntiAlias = true
        }
        
        val headerBackgroundPaint = Paint().apply {
            color = colorBackgroundMuted
            style = Paint.Style.FILL
        }
        
        val tableHeaderPaint = Paint().apply {
            color = colorTextSecondary
            textSize = 9f
            typeface = Typeface.create(normalTypeface, Typeface.BOLD)
            isAntiAlias = true
            letterSpacing = 0.1f
        }
        
        val rowTextPaint = Paint().apply {
            color = colorTextPrimary
            textSize = 11f
            typeface = Typeface.create(normalTypeface, Typeface.NORMAL)
            isAntiAlias = true
        }
        
        val incomePaint = Paint().apply {
            color = colorIncome
            textSize = 11f
            typeface = Typeface.create(normalTypeface, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        
        val expensePaint = Paint().apply {
            color = colorExpense
            textSize = 11f
            typeface = Typeface.create(normalTypeface, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        
        val linePaint = Paint().apply {
            color = colorBorder
            strokeWidth = 1f
        }

        // Layout constants
        val marginX = 50f
        val rightMargin = pageInfo.pageWidth - marginX
        var currentY = 0f
        
        // Define column layout (Date, Merchant, Category, Amount)
        val colDate = marginX + 10f
        val colMerchant = colDate + 90f
        val colCategory = colMerchant + 180f
        val colAmount = rightMargin - 10f
        
        // Split transactions into pages
        val itemsFirstPage = 20
        val itemsNextPages = 28
        
        // Formatter
        val dateFormatter = AppFormatters.getFullDate()
        val rowDateFormatter = AppFormatters.getDay()
        
        fun formatMoney(amount: Double): String = String.format("Rs. %,.2f", amount)

        if (transactions.isEmpty()) {
            val page = document.startPage(pageInfo)
            page.canvas.drawText("No transactions found.", marginX, 100f, titlePaint)
            document.finishPage(page)
            document.writeTo(outputStream)
            document.close()
            return
        }
        
        // Calculate chunks
        val firstChunk = transactions.take(itemsFirstPage)
        val remainingChunks = transactions.drop(itemsFirstPage).chunked(itemsNextPages)
        val allChunks = listOf(firstChunk) + remainingChunks
        val totalPages = allChunks.size

        allChunks.forEachIndexed { index, chunk ->
            if (chunk.isEmpty() && index > 0) return@forEachIndexed
            
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            
            if (index == 0) {
                currentY = 50f
                
                // --- Brand Header ---
                canvas.drawText("cipher.", marginX, currentY, logoPaint)
                
                titlePaint.textAlign = Paint.Align.RIGHT
                canvas.drawText("ACCOUNT STATEMENT", rightMargin, currentY - 10f, titlePaint)
                titlePaint.textAlign = Paint.Align.LEFT
                
                currentY += 25f
                canvas.drawLine(marginX, currentY, rightMargin, currentY, linePaint)
                currentY += 15f
                
                // --- Meta Info ---
                val generatedDate = dateFormatter.format(Date())
                val startDate = if (transactions.isNotEmpty()) dateFormatter.format(Date(transactions.last().timestamp)) else generatedDate
                val endDate = if (transactions.isNotEmpty()) dateFormatter.format(Date(transactions.first().timestamp)) else generatedDate
                
                canvas.drawText("STATEMENT PERIOD", marginX, currentY, labelPaint)
                canvas.drawText("$startDate — $endDate", marginX, currentY + 15f, valuePaint)
                
                labelPaint.textAlign = Paint.Align.RIGHT
                valuePaint.textAlign = Paint.Align.RIGHT
                canvas.drawText("GENERATED ON", rightMargin, currentY, labelPaint)
                canvas.drawText(generatedDate, rightMargin, currentY + 15f, valuePaint)
                labelPaint.textAlign = Paint.Align.LEFT
                valuePaint.textAlign = Paint.Align.LEFT
                
                currentY += 35f
                
                // --- Summary Cards ---
                val totalIncome = transactions.filter { it.isIncome }.sumOf { it.amount }
                val totalExpense = transactions.filter { !it.isIncome }.sumOf { it.amount }
                val net = totalIncome - totalExpense
                
                val cardWidth = (rightMargin - marginX - 20f) / 3f
                val cardHeight = 55f
                
                val incomesX = marginX
                val expensesX = marginX + cardWidth + 10f
                val netX = expensesX + cardWidth + 10f
                
                // Income Card
                canvas.drawRoundRect(incomesX, currentY, incomesX + cardWidth, currentY + cardHeight, 6f, 6f, headerBackgroundPaint)
                canvas.drawText("TOTAL INCOME", incomesX + 15f, currentY + 25f, labelPaint)
                incomePaint.textAlign = Paint.Align.LEFT
                incomePaint.textSize = 14f
                canvas.drawText(formatMoney(totalIncome), incomesX + 15f, currentY + 45f, incomePaint)
                incomePaint.textAlign = Paint.Align.RIGHT
                incomePaint.textSize = 11f
                
                // Expense Card
                canvas.drawRoundRect(expensesX, currentY, expensesX + cardWidth, currentY + cardHeight, 6f, 6f, headerBackgroundPaint)
                canvas.drawText("TOTAL EXPENSE", expensesX + 15f, currentY + 25f, labelPaint)
                expensePaint.textAlign = Paint.Align.LEFT
                expensePaint.textSize = 14f
                canvas.drawText(formatMoney(totalExpense), expensesX + 15f, currentY + 45f, expensePaint)
                expensePaint.textAlign = Paint.Align.RIGHT
                expensePaint.textSize = 11f
                
                // Net Card
                canvas.drawRoundRect(netX, currentY, netX + cardWidth, currentY + cardHeight, 6f, 6f, headerBackgroundPaint)
                canvas.drawText("NET CHANGE", netX + 15f, currentY + 25f, labelPaint)
                val netPaint = if (net >= 0) incomePaint else expensePaint
                netPaint.textAlign = Paint.Align.LEFT
                netPaint.textSize = 14f
                val sign = if (net > 0) "+" else ""
                canvas.drawText("$sign${formatMoney(net)}", netX + 15f, currentY + 45f, netPaint)
                netPaint.textAlign = Paint.Align.RIGHT
                netPaint.textSize = 11f
                
                currentY += cardHeight + 30f
            } else {
                currentY = 60f
                canvas.drawText("cipher.", marginX, currentY, logoPaint.apply { textSize = 20f })
                logoPaint.textSize = 28f // reset
                
                labelPaint.textAlign = Paint.Align.RIGHT
                canvas.drawText("Page ${index + 1} of $totalPages", rightMargin, currentY, labelPaint)
                labelPaint.textAlign = Paint.Align.LEFT
                
                currentY += 20f
                canvas.drawLine(marginX, currentY, rightMargin, currentY, linePaint)
                currentY += 30f
            }
            
            // --- Table Header ---
            canvas.drawRoundRect(marginX, currentY, rightMargin, currentY + 25f, 4f, 4f, headerBackgroundPaint)
            val tableHeaderY = currentY + 16f
            
            canvas.drawText("DATE", colDate, tableHeaderY, tableHeaderPaint)
            canvas.drawText("MERCHANT", colMerchant, tableHeaderY, tableHeaderPaint)
            canvas.drawText("CATEGORY", colCategory, tableHeaderY, tableHeaderPaint)
            tableHeaderPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("AMOUNT", colAmount, tableHeaderY, tableHeaderPaint)
            tableHeaderPaint.textAlign = Paint.Align.LEFT
            
            currentY += 45f
            
            // --- Table Rows ---
            chunk.forEach { tx ->
                val date = rowDateFormatter.format(Date(tx.timestamp))
                val merchant = if (tx.merchant.length > 30) tx.merchant.take(27) + "..." else tx.merchant
                val category = tx.category.uppercase()
                
                canvas.drawText(date, colDate, currentY, rowTextPaint)
                canvas.drawText(merchant, colMerchant, currentY, rowTextPaint.apply { typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD) })
                rowTextPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL) // reset
                
                labelPaint.textAlign = Paint.Align.LEFT
                canvas.drawText(category, colCategory, currentY, labelPaint)
                
                val amountStr = formatMoney(tx.amount)
                val paint = if (tx.isIncome) incomePaint else expensePaint
                canvas.drawText(amountStr, colAmount, currentY, paint)
                
                currentY += 15f
                canvas.drawLine(marginX, currentY, rightMargin, currentY, linePaint)
                currentY += 25f
            }
            
            // --- Footer ---
            labelPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("This is a computer-generated document. No signature is required.", pageInfo.pageWidth / 2f, pageInfo.pageHeight - 40f, labelPaint)
            labelPaint.textAlign = Paint.Align.LEFT
            
            document.finishPage(page)
        }
        
        document.writeTo(outputStream)
        document.close()
    }
}
