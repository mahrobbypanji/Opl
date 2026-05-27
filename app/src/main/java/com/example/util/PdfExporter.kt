package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.OplEntry
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

object PdfExporter {

    private val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    private fun formatMonthYear(yyyyMM: String): String {
        return try {
            val parts = yyyyMM.split("-")
            val year = parts[0]
            val monthIdx = parts[1].toInt() - 1
            val months = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
            "${months[monthIdx]} $year"
        } catch (e: Exception) {
            yyyyMM
        }
    }

    fun exportGrafikPdf(
        context: Context,
        month: String,
        jobStats: Map<String, Int>,
        trendStats: List<Pair<String, Int>>
    ) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        // Titles
        canvas.drawText("Laporan Distribusi OPL & Tren Produktivitas", pageInfo.pageWidth / 2f, 50f, paint)
        
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(formatMonthYear(month), pageInfo.pageWidth / 2f, 75f, paint)
        
        paint.textAlign = Paint.Align.LEFT
        var currentY = 110f
        
        // --- Distribusi Pekerjaan OPL (Bar Chart) ---
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Distribusi Pekerjaan OPL", 50f, currentY, paint)
        currentY += 20f
        
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val maxJobCount = jobStats.values.maxOrNull() ?: 1
        val barWidthMax = 300f
        
        jobStats.forEach { (job, count) ->
            canvas.drawText(job, 50f, currentY + 12f, paint)
            
            val barPaint = Paint().apply { color = Color.parseColor("#3B82F6") }
            val width = (count.toFloat() / maxJobCount) * barWidthMax
            canvas.drawRoundRect(RectF(180f, currentY, 180f + width, currentY + 15f), 4f, 4f, barPaint)
            
            canvas.drawText("$count unit", 185f + width + 5f, currentY + 12f, paint)
            currentY += 30f
        }
        
        currentY += 40f
        
        // --- Tren Produktivitas Harian ---
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Tren Produktivitas Harian", 50f, currentY, paint)
        currentY += 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        
        val maxTrendVal = trendStats.maxOfOrNull { it.second } ?: 1
        val trendHeight = 120f
        val trendStartX = 60f
        val trendBarWidth = 15f
        val trendSpacing = 25f
        
        // Draw L axes
        val axisPaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        val chartOriginX = 50f
        val chartOriginY = currentY + trendHeight + 5f
        
        // Vertical line (Y axis) & Horizontal line (X axis)
        canvas.drawLine(chartOriginX, currentY - 10f, chartOriginX, chartOriginY, axisPaint)
        canvas.drawLine(chartOriginX, chartOriginY, 550f, chartOriginY, axisPaint)

        // Draw Y axis labels
        paint.textSize = 8f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(maxTrendVal.toString(), chartOriginX - 5f, currentY, paint)
        canvas.drawText((maxTrendVal/2).toString(), chartOriginX - 5f, currentY + trendHeight/2, paint)
        canvas.drawText("0", chartOriginX - 5f, chartOriginY, paint)
        paint.textAlign = Paint.Align.LEFT
        
        var currentX = trendStartX
        trendStats.forEach { (dateStr, count) ->
            if(currentX > 530f) {
                // simple wrap if many days
                currentY += 180f
                currentX = trendStartX
                val newChartOriginY = currentY + trendHeight + 5f
                canvas.drawLine(chartOriginX, currentY - 10f, chartOriginX, newChartOriginY, axisPaint)
                canvas.drawLine(chartOriginX, newChartOriginY, 550f, newChartOriginY, axisPaint)
            }
            val h = (count.toFloat() / maxTrendVal) * trendHeight
            val barPaint = Paint().apply { color = Color.parseColor("#10B981") }
            canvas.drawRoundRect(RectF(currentX, currentY + trendHeight - h, currentX + trendBarWidth, currentY + trendHeight), 2f, 2f, barPaint)
            
            paint.textSize = 8f
            canvas.drawText(dateStr.takeLast(2), currentX + 2f, currentY + trendHeight + 15f, paint)
            canvas.drawText(count.toString(), currentX + 3f, currentY + trendHeight - h - 5f, paint)
            
            currentX += trendSpacing
        }

        document.finishPage(page)
        
        saveAndOpenFile(context, document, "Laporan_Grafik_${month.replace(" ", "_").replace("/", "-")}.pdf")
    }

    fun exportPendapatanPdf(
        context: Context,
        month: String,
        entries: List<OplEntry>,
        totalRevenue: Long,
        totalJobsCount: Int,
        incentivePercent: Int,
        targetJobPrices: Map<String, String>,
        selectedJobs: Set<String>
    ) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        
        var paint = Paint().apply { color = Color.BLACK; textSize = 18f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textAlign = Paint.Align.CENTER }

        // Title
        canvas.drawText("Laporan Pendapatan & Insentif", pageInfo.pageWidth / 2f, 50f, paint)
        
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(formatMonthYear(month), pageInfo.pageWidth / 2f, 75f, paint)
        
        paint.textAlign = Paint.Align.LEFT
        var currentY = 110f
        
        // Card Background (Pendapatan & Insentif)
        var cardPaint = Paint().apply { color = Color.parseColor("#F8FAFC"); style = Paint.Style.FILL }
        val cardRect = RectF(40f, currentY, 555f, currentY + 120f)
        canvas.drawRoundRect(cardRect, 12f, 12f, cardPaint)
        
        // draw border for card
        val borderPaint = Paint().apply { color = Color.parseColor("#E2E8F0"); style = Paint.Style.STROKE; strokeWidth = 2f }
        canvas.drawRoundRect(cardRect, 12f, 12f, borderPaint)
        
        paint.textSize = 12f
        paint.color = Color.GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Total Pendapatan", 60f, currentY + 30f, paint)
        
        paint.textSize = 18f
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(formatRp.format(totalRevenue), 60f, currentY + 55f, paint)
        
        paint.textSize = 12f
        paint.color = Color.GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Total Keseluruhan Pekerjaan OPL", 60f, currentY + 85f, paint)
        
        paint.textSize = 14f
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("$totalJobsCount Unit", 60f, currentY + 105f, paint)
        
        // Incentive part
        val incText = "Persentase Insentif: $incentivePercent%"
        paint.textSize = 12f
        paint.color = Color.parseColor("#9333EA")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(incText, 350f, currentY + 30f, paint)
        
        paint.textSize = 12f
        paint.color = Color.GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Perkiraan Nominal", 350f, currentY + 85f, paint)
        
        val nominal = totalRevenue * incentivePercent / 100
        paint.textSize = 18f
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(formatRp.format(nominal), 350f, currentY + 105f, paint)
        
        currentY += 150f
        
        canvas.drawLine(40f, currentY, 555f, currentY, Paint().apply { color = Color.LTGRAY; strokeWidth = 1f })
        currentY += 30f
        
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 14f
        paint.color = Color.BLACK
        canvas.drawText("Rincian per Unit", 50f, currentY, paint)
        currentY += 25f
        
        // Draw Table Header
        fun drawRincianTableHeader(canvasCanvas: Canvas, y: Float) {
            val headerPaint = Paint().apply { color = Color.parseColor("#E2E8F0"); style = Paint.Style.FILL }
            canvasCanvas.drawRect(50f, y, 545f, y + 25f, headerPaint)
            val textPaint = Paint().apply { color = Color.BLACK; textSize = 12f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
            canvasCanvas.drawText("No.", 60f, y + 17f, textPaint)
            canvasCanvas.drawText("Kendaraan & Tanggal", 100f, y + 17f, textPaint)
            canvasCanvas.drawText("Pekerjaan", 260f, y + 17f, textPaint)
            val alignRightPaint = Paint(textPaint).apply { textAlign = Paint.Align.RIGHT }
            canvasCanvas.drawText("Harga", 535f, y + 17f, alignRightPaint)
            
            val borderPaint = Paint().apply { color = Color.BLACK; strokeWidth = 1f; style = Paint.Style.STROKE }
            canvasCanvas.drawRect(50f, y, 545f, y + 25f, borderPaint)
        }
        
        drawRincianTableHeader(canvas, currentY)
        currentY += 25f
        
        var index = 1
        val tBorderPaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f; style = Paint.Style.STROKE }
        val tTextPaint = Paint().apply { color = Color.BLACK; textSize = 11f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL) }
        val tBoldPaint = Paint().apply { color = Color.BLACK; textSize = 11f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        
        for (entry in entries) {
            val jobs = entry.tipePekerjaan.split(",").map { it.trim() }.filter { selectedJobs.contains(it) }
            if (jobs.isEmpty()) continue
            val entryHeight = 15f + (jobs.size * 20f) + 10f // padding top + jobs + padding bottom
            
            if (currentY + entryHeight > 780f) {
                // start new page
                document.finishPage(page)
                page = document.startPage(pageInfo)
                canvas = page.canvas
                currentY = 50f
                drawRincianTableHeader(canvas, currentY)
                currentY += 25f
            }
            
            // Draw entry (Plat nomor -> Tanggal)
            canvas.drawText("${index++}", 60f, currentY + 15f, tTextPaint)
            canvas.drawText("${entry.platNomor} > ${entry.tanggalString}", 100f, currentY + 15f, tBoldPaint)
            
            var jobY = currentY + 15f
            for (job in jobs) {
                val priceStr = targetJobPrices[job] ?: "120000"
                val price = priceStr.toLongOrNull() ?: 120000L
                canvas.drawText("- $job", 260f, jobY, tTextPaint)
                
                val pricePaint = Paint(tTextPaint).apply { textAlign = Paint.Align.RIGHT }
                canvas.drawText(formatRp.format(price), 535f, jobY, pricePaint)
                jobY += 20f
            }
            
            currentY = jobY + 5f
            // Draw horizontal row separator
            canvas.drawLine(50f, currentY, 545f, currentY, tBorderPaint)
        }

        document.finishPage(page)
        
        saveAndOpenFile(context, document, "Laporan_Pendapatan_${month.replace(" ", "_").replace("/", "-")}.pdf")
    }

    private fun saveAndOpenFile(context: Context, document: PdfDocument, filename: String) {
        val file = File(context.cacheDir, filename)
        val fos = FileOutputStream(file)
        document.writeTo(fos)
        document.close()
        fos.close()

        val uri = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(Intent.createChooser(intent, "Buka PDF dengan"))
    }
}
