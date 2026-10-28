package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.model.ActivityLog
import com.example.data.model.BusinessProfile
import com.example.data.model.Customer
import com.example.data.model.CustomerWithDetails
import com.example.data.model.DashboardStats
import com.example.data.model.Expense
import com.example.data.model.ExpenseCategorySum
import com.example.data.model.Job
import com.example.data.model.JobDetailBundle
import com.example.data.model.JobPhoto
import com.example.data.model.JobPriority
import com.example.data.model.JobStatus
import com.example.data.model.JobWithCustomer
import com.example.data.model.Payment
import com.example.data.model.PaymentMethod
import com.example.data.model.Quote
import com.example.data.model.QuoteItem
import com.example.data.model.QuoteStatus
import com.example.data.model.QuoteWithDetails
import com.example.util.Formatters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Calendar

class CepteUstaRepository(private val db: AppDatabase) {

    private val profileDao = db.businessProfileDao()
    private val customerDao = db.customerDao()
    private val jobDao = db.jobDao()
    private val quoteDao = db.quoteDao()
    private val paymentDao = db.paymentDao()
    private val expenseDao = db.expenseDao()
    private val activityDao = db.activityLogDao()

    // Business Profile
    val profileFlow: Flow<BusinessProfile?> = profileDao.getProfileFlow()

    suspend fun getProfile(): BusinessProfile {
        return profileDao.getProfile() ?: BusinessProfile()
    }

    suspend fun saveProfile(profile: BusinessProfile) = withContext(Dispatchers.IO) {
        profileDao.insertOrUpdate(profile)
    }

    // Customers
    val activeCustomers: Flow<List<Customer>> = customerDao.getAllActiveCustomers()
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()

    fun getCustomerByIdFlow(id: Long): Flow<Customer?> = customerDao.getCustomerByIdFlow(id)

    suspend fun getCustomerById(id: Long): Customer? = withContext(Dispatchers.IO) {
        customerDao.getCustomerById(id)
    }

    suspend fun findCustomerByPhone(phone: String, excludeId: Long = 0L): Customer? = withContext(Dispatchers.IO) {
        customerDao.findByPhone(phone.trim(), excludeId)
    }

    suspend fun saveCustomer(customer: Customer): Long = withContext(Dispatchers.IO) {
        val id = customerDao.insert(customer)
        val action = if (customer.id == 0L) "Yeni Müşteri Eklendi" else "Müşteri Bilgisi Güncellendi"
        logActivity(action, customer.name, "CUSTOMER", if (customer.id == 0L) id else customer.id)
        id
    }

    suspend fun deleteCustomer(id: Long) = withContext(Dispatchers.IO) {
        val cust = customerDao.getCustomerById(id)
        customerDao.deleteById(id)
        if (cust != null) {
            logActivity("Müşteri Silindi", cust.name, "CUSTOMER", id)
        }
    }

    suspend fun archiveCustomer(id: Long, archived: Boolean) = withContext(Dispatchers.IO) {
        customerDao.setArchived(id, archived)
    }

    fun getCustomerWithDetails(customerId: Long): Flow<CustomerWithDetails?> {
        return combine(
            customerDao.getCustomerByIdFlow(customerId),
            jobDao.getJobsForCustomer(customerId),
            quoteDao.getQuotesForCustomer(customerId),
            paymentDao.getPaymentsForCustomer(customerId)
        ) { cust, jobs, quotes, payments ->
            if (cust == null) null
            else {
                // İptal edilen işler borç sayılmaz (ana sayfadaki alacak hesabıyla aynı)
                val totalBilled = jobs.filter { it.status != JobStatus.CANCELLED }
                    .sumOf { if (it.finalPrice > 0) it.finalPrice else it.estimatedPrice }
                val totalPaid = payments.sumOf { it.amount }
                val balance = totalBilled - totalPaid
                CustomerWithDetails(
                    customer = cust,
                    jobs = jobs,
                    quotes = quotes,
                    payments = payments,
                    totalBilled = totalBilled,
                    totalPaid = totalPaid,
                    balance = balance
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    // Jobs
    val allJobsWithCustomer: Flow<List<JobWithCustomer>> = jobDao.getAllJobsWithCustomerFlow()

    fun getJobsInDateRange(start: Long, end: Long): Flow<List<JobWithCustomer>> =
        jobDao.getJobsInDateRange(start, end)

    fun getJobWithCustomer(jobId: Long): Flow<JobWithCustomer?> =
        jobDao.getJobWithCustomerByIdFlow(jobId)

    suspend fun getJobBundle(jobId: Long): JobDetailBundle? = withContext(Dispatchers.IO) {
        val job = jobDao.getJobById(jobId) ?: return@withContext null
        val cust = customerDao.getCustomerById(job.customerId) ?: return@withContext null
        val photos = jobDao.getPhotosForJobSync(jobId)
        val payments = paymentDao.getPaymentsForJobSync(jobId)
        val totalPaid = payments.sumOf { it.amount }
        val price = if (job.finalPrice > 0.0) job.finalPrice else job.estimatedPrice
        val remaining = (price - totalPaid).coerceAtLeast(0.0)
        JobDetailBundle(
            job = job,
            customer = cust,
            photos = photos,
            payments = payments,
            totalPaid = totalPaid,
            remainingBalance = remaining
        )
    }

    suspend fun saveJob(job: Job): Long = withContext(Dispatchers.IO) {
        val coded = if (job.jobCode.isBlank()) {
            val count = jobDao.getAllJobsWithCustomerFlow().first().size + 1
            job.copy(jobCode = "IS-${1000 + count}")
        } else job
        val finalJob = withCompletion(coded, if (job.id != 0L) jobDao.getJobById(job.id) else null)
        val id = jobDao.insert(finalJob)
        val action = if (job.id == 0L) "Yeni İş Oluşturuldu" else "İş Güncellendi"
        logActivity(action, "${finalJob.jobCode}: ${finalJob.title}", "JOB", if (job.id == 0L) id else job.id)
        id
    }

    suspend fun updateJobStatus(jobId: Long, newStatus: String, finalPrice: Double? = null) = withContext(Dispatchers.IO) {
        val job = jobDao.getJobById(jobId) ?: return@withContext
        val changed = if (finalPrice != null) {
            job.copy(status = newStatus, finalPrice = finalPrice)
        } else {
            job.copy(status = newStatus)
        }
        jobDao.update(withCompletion(changed, job))
        logActivity("İş Durumu Değişti", "${job.jobCode} durumu: ${JobStatus.getLabel(newStatus)}", "JOB", jobId)
    }

    /** Tamamlandı'ya geçince tamamlanma anını yazar, geri alınınca sıfırlar. */
    private fun withCompletion(job: Job, previous: Job?): Job = when {
        job.status != JobStatus.COMPLETED -> job.copy(completedAt = 0L)
        job.completedAt > 0L -> job
        previous != null && previous.status == JobStatus.COMPLETED && previous.completedAt > 0L ->
            job.copy(completedAt = previous.completedAt)
        else -> job.copy(completedAt = System.currentTimeMillis())
    }

    /** Geçerlilik tarihi geçen taslak/gönderilmiş teklifleri "Süresi Doldu" yapar. */
    suspend fun expireOldQuotes() = withContext(Dispatchers.IO) {
        quoteDao.expireOldQuotes(System.currentTimeMillis())
    }

    suspend fun deleteJob(jobId: Long) = withContext(Dispatchers.IO) {
        val job = jobDao.getJobById(jobId)
        jobDao.deleteById(jobId)
        if (job != null) {
            logActivity("İş Silindi", "${job.jobCode} - ${job.title}", "JOB", jobId)
        }
    }

    // Job Photos
    fun getPhotosForJob(jobId: Long): Flow<List<JobPhoto>> = jobDao.getPhotosForJob(jobId)

    suspend fun addJobPhoto(photo: JobPhoto): Long = withContext(Dispatchers.IO) {
        jobDao.insertPhoto(photo)
    }

    suspend fun deleteJobPhoto(photoId: Long) = withContext(Dispatchers.IO) {
        jobDao.deletePhotoById(photoId)
    }

    // Quotes
    val allQuotes: Flow<List<Quote>> = quoteDao.getAllQuotes()

    fun getQuoteWithDetails(quoteId: Long): Flow<QuoteWithDetails?> {
        return combine(
            quoteDao.getQuoteByIdFlow(quoteId),
            quoteDao.getItemsForQuote(quoteId),
            customerDao.getAllCustomers()
        ) { quote, items, customers ->
            if (quote == null) null
            else {
                val cust = customers.find { it.id == quote.customerId } ?: Customer(name = "Bilinmeyen Müşteri", phone = "")
                QuoteWithDetails(quote, cust, items)
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun saveQuote(quote: Quote, items: List<QuoteItem>): Long = withContext(Dispatchers.IO) {
        val finalQuote = if (quote.quoteNumber.isBlank()) {
            val count = quoteDao.getAllQuotes().first().size + 1
            quote.copy(quoteNumber = "TEK-${1000 + count}")
        } else quote

        val total = items.sumOf { it.total }
        val quoteWithTotal = finalQuote.copy(totalAmount = total)
        val id = quoteDao.saveQuoteWithItems(quoteWithTotal, items)
        logActivity("Teklif Kaydedildi", "${quoteWithTotal.quoteNumber}: ${quoteWithTotal.title}", "QUOTE", id)
        id
    }

    suspend fun updateQuoteStatus(quoteId: Long, status: String) = withContext(Dispatchers.IO) {
        quoteDao.updateStatus(quoteId, status)
        val quote = quoteDao.getQuoteById(quoteId)
        if (quote != null) {
            logActivity("Teklif Durumu Değişti", "${quote.quoteNumber}: ${QuoteStatus.getLabel(status)}", "QUOTE", quoteId)
        }
    }

    suspend fun convertQuoteToJob(quoteId: Long): Long = withContext(Dispatchers.IO) {
        val quote = quoteDao.getQuoteById(quoteId) ?: return@withContext 0L
        val items = quoteDao.getItemsForQuoteSync(quoteId)
        val jobDesc = if (items.isNotEmpty()) {
            items.joinToString("\n") { "• ${it.description} (${it.quantity} ${it.unit} x ${it.unitPrice} ₺)" }
        } else quote.notes

        val newJob = Job(
            customerId = quote.customerId,
            title = quote.title,
            description = jobDesc,
            estimatedPrice = quote.totalAmount,
            finalPrice = quote.totalAmount,
            notes = "Kabul edilen ${quote.quoteNumber} numaralı tekliften aktarıldı."
        )
        val jobId = saveJob(newJob)
        quoteDao.updateStatus(quoteId, QuoteStatus.ACCEPTED)
        logActivity("Teklif İşe Dönüştürüldü", "${quote.quoteNumber} -> ${newJob.title}", "JOB", jobId)
        jobId
    }

    suspend fun deleteQuote(quoteId: Long) = withContext(Dispatchers.IO) {
        quoteDao.deleteQuoteById(quoteId)
    }

    // Payments
    val allPayments: Flow<List<Payment>> = paymentDao.getAllPayments()

    fun getPaymentsForJob(jobId: Long): Flow<List<Payment>> = paymentDao.getPaymentsForJob(jobId)

    suspend fun addPayment(payment: Payment): Long = withContext(Dispatchers.IO) {
        val id = paymentDao.insert(payment)
        logActivity("Tahsilat Alındı", "${Formatters.formatCurrency(payment.amount)} (${PaymentMethod.getLabel(payment.paymentMethod)})", "PAYMENT", id)
        id
    }

    suspend fun deletePayment(paymentId: Long) = withContext(Dispatchers.IO) {
        paymentDao.deleteById(paymentId)
    }

    // Expenses
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()

    fun getExpensesGroupedByCategory(): Flow<List<ExpenseCategorySum>> = expenseDao.getExpensesGroupedByCategory()

    suspend fun addExpense(expense: Expense): Long = withContext(Dispatchers.IO) {
        val id = expenseDao.insert(expense)
        logActivity("Gider Eklendi", "${expense.category}: ${Formatters.formatCurrency(expense.amount)}", "EXPENSE", id)
        id
    }

    suspend fun deleteExpense(expenseId: Long) = withContext(Dispatchers.IO) {
        expenseDao.deleteById(expenseId)
    }

    // Activity Logs
    val recentActivity: Flow<List<ActivityLog>> = activityDao.getRecentLogs(25)

    private suspend fun logActivity(title: String, desc: String, type: String, relatedId: Long? = null) {
        try {
            activityDao.insert(
                ActivityLog(
                    title = title,
                    description = desc,
                    type = type,
                    relatedId = relatedId
                )
            )
        } catch (_: Exception) {}
    }

    // Dashboard Stats Reactive Flow
    fun getDashboardStats(): Flow<DashboardStats> {
        val todayStart = Formatters.getTodayStartMillis()
        val todayEnd = Formatters.getTodayEndMillis()
        val monthStart = Formatters.getMonthStartMillis()

        return combine(
            jobDao.getAllJobsWithCustomerFlow(),
            paymentDao.getAllPayments(),
            expenseDao.getAllExpenses()
        ) { jobsWithCust, payments, expenses ->
            val jobs = jobsWithCust.map { it.job }

            val todayJobs = jobs.count { it.scheduledDate in todayStart..todayEnd }
            val inProgress = jobs.count { it.status == JobStatus.IN_PROGRESS }
            val completedThisMonth = jobs.count {
                it.status == JobStatus.COMPLETED &&
                    (if (it.completedAt > 0L) it.completedAt else it.createdAt) >= monthStart
            }
            val pending = jobs.count { it.status == JobStatus.NEW || it.status == JobStatus.SCHEDULED }

            val collectedMonth = payments.filter { it.date >= monthStart }.sumOf { it.amount }
            val expensesMonth = expenses.filter { it.date >= monthStart }.sumOf { it.amount }

            // Pending receivables: sum of (finalPrice or estimatedPrice) minus total payments for that job
            val nonCancelledJobs = jobs.filter { it.status != JobStatus.CANCELLED }
            var totalPendingReceivable = 0.0
            for (j in nonCancelledJobs) {
                val price = if (j.finalPrice > 0.0) j.finalPrice else j.estimatedPrice
                val jobPayments = payments.filter { it.jobId == j.id }.sumOf { it.amount }
                val remain = (price - jobPayments).coerceAtLeast(0.0)
                totalPendingReceivable += remain
            }

            val estimatedNet = collectedMonth - expensesMonth

            DashboardStats(
                todayJobsCount = todayJobs,
                inProgressJobsCount = inProgress,
                completedThisMonthCount = completedThisMonth,
                pendingJobsCount = pending,
                totalCollectedThisMonth = collectedMonth,
                totalPendingReceivable = totalPendingReceivable,
                totalExpensesThisMonth = expensesMonth,
                estimatedNetIncomeThisMonth = estimatedNet
            )
        }.flowOn(Dispatchers.IO)
    }

    // Backup & Restore
    suspend fun exportDataToJson(context: Context): File = withContext(Dispatchers.IO) {
        val root = JSONObject()
        val profile = getProfile()
        val customers = customerDao.getAllCustomers().first()
        val jobs = jobDao.getAllJobsWithCustomerFlow().first().map { it.job }
        val quotes = quoteDao.getAllQuotes().first()
        val payments = paymentDao.getAllPayments().first()
        val expenses = expenseDao.getAllExpenses().first()

        // Profile json
        val profileObj = JSONObject().apply {
            put("businessName", profile.businessName)
            put("category", profile.category)
            put("phone", profile.phone)
            put("email", profile.email)
            put("address", profile.address)
            put("currencySymbol", profile.currencySymbol)
            put("themeMode", profile.themeMode)
        }
        root.put("profile", profileObj)

        // Customers json
        val custArray = JSONArray()
        customers.forEach { c ->
            custArray.put(JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("phone", c.phone)
                put("email", c.email)
                put("address", c.address)
                put("notes", c.notes)
                put("createdAt", c.createdAt)
            })
        }
        root.put("customers", custArray)

        // Jobs json
        val jobsArray = JSONArray()
        jobs.forEach { j ->
            jobsArray.put(JSONObject().apply {
                put("id", j.id)
                put("jobCode", j.jobCode)
                put("customerId", j.customerId)
                put("title", j.title)
                put("description", j.description)
                put("category", j.category)
                put("address", j.address)
                put("scheduledDate", j.scheduledDate)
                put("scheduledTime", j.scheduledTime)
                put("status", j.status)
                put("priority", j.priority)
                put("estimatedPrice", j.estimatedPrice)
                put("finalPrice", j.finalPrice)
                put("notes", j.notes)
                put("createdAt", j.createdAt)
                put("completedAt", j.completedAt)
            })
        }
        root.put("jobs", jobsArray)

        // Payments json
        val payArray = JSONArray()
        payments.forEach { p ->
            payArray.put(JSONObject().apply {
                put("customerId", p.customerId)
                put("jobId", p.jobId ?: 0L)
                put("amount", p.amount)
                put("date", p.date)
                put("paymentMethod", p.paymentMethod)
                put("description", p.description)
            })
        }
        root.put("payments", payArray)

        // Expenses json
        val expArray = JSONArray()
        expenses.forEach { e ->
            expArray.put(JSONObject().apply {
                put("category", e.category)
                put("amount", e.amount)
                put("date", e.date)
                put("description", e.description)
            })
        }
        root.put("expenses", expArray)

        root.put("version", 1)
        root.put("exportDate", System.currentTimeMillis())

        val backupDir = File(context.cacheDir, "backups")
        if (!backupDir.exists()) backupDir.mkdirs()
        val file = File(backupDir, "CepteUsta_Yedek_${System.currentTimeMillis()}.json")
        file.writeText(root.toString(2), Charsets.UTF_8)
        file
    }

    suspend fun restoreDataFromJson(jsonContent: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonContent)
            if (!root.has("customers") || !root.has("jobs")) return@withContext false

            // Restore Profile if present
            if (root.has("profile")) {
                val p = root.getJSONObject("profile")
                val cur = getProfile()
                val updated = cur.copy(
                    businessName = p.optString("businessName", cur.businessName),
                    category = p.optString("category", cur.category),
                    phone = p.optString("phone", cur.phone),
                    email = p.optString("email", cur.email),
                    address = p.optString("address", cur.address),
                    currencySymbol = p.optString("currencySymbol", cur.currencySymbol),
                    themeMode = p.optString("themeMode", cur.themeMode)
                )
                profileDao.insertOrUpdate(updated)
            }

            // Restore customers
            val oldToNewCustId = mutableMapOf<Long, Long>()
            val custArray = root.getJSONArray("customers")
            for (i in 0 until custArray.length()) {
                val c = custArray.getJSONObject(i)
                val oldId = c.optLong("id")
                val newCust = Customer(
                    name = c.optString("name", "Müşteri"),
                    phone = c.optString("phone", ""),
                    email = c.optString("email", ""),
                    address = c.optString("address", ""),
                    notes = c.optString("notes", ""),
                    createdAt = c.optLong("createdAt", System.currentTimeMillis())
                )
                val newId = customerDao.insert(newCust)
                oldToNewCustId[oldId] = newId
            }

            // Restore jobs
            val oldToNewJobId = mutableMapOf<Long, Long>()
            val jobsArray = root.getJSONArray("jobs")
            for (i in 0 until jobsArray.length()) {
                val j = jobsArray.getJSONObject(i)
                val oldJobId = j.optLong("id")
                val oldCustId = j.optLong("customerId")
                val newCustId = oldToNewCustId[oldCustId] ?: continue

                val newJob = Job(
                    jobCode = j.optString("jobCode", ""),
                    customerId = newCustId,
                    title = j.optString("title", ""),
                    description = j.optString("description", ""),
                    category = j.optString("category", "Genel"),
                    address = j.optString("address", ""),
                    scheduledDate = j.optLong("scheduledDate", 0L),
                    scheduledTime = j.optString("scheduledTime", ""),
                    status = j.optString("status", JobStatus.NEW),
                    priority = j.optString("priority", JobPriority.NORMAL),
                    estimatedPrice = j.optDouble("estimatedPrice", 0.0),
                    finalPrice = j.optDouble("finalPrice", 0.0),
                    notes = j.optString("notes", ""),
                    createdAt = j.optLong("createdAt", System.currentTimeMillis()),
                    completedAt = j.optLong("completedAt", 0L)
                )
                val newJobId = jobDao.insert(newJob)
                oldToNewJobId[oldJobId] = newJobId
            }

            // Restore payments
            if (root.has("payments")) {
                val payArray = root.getJSONArray("payments")
                for (i in 0 until payArray.length()) {
                    val p = payArray.getJSONObject(i)
                    val oldCustId = p.optLong("customerId")
                    val oldJobId = p.optLong("jobId")
                    val newCustId = oldToNewCustId[oldCustId] ?: continue
                    val newJobId = if (oldJobId > 0) oldToNewJobId[oldJobId] else null
                    val pay = Payment(
                        customerId = newCustId,
                        jobId = newJobId,
                        amount = p.optDouble("amount", 0.0),
                        date = p.optLong("date", System.currentTimeMillis()),
                        paymentMethod = p.optString("paymentMethod", PaymentMethod.CASH),
                        description = p.optString("description", "")
                    )
                    paymentDao.insert(pay)
                }
            }

            // Restore expenses
            if (root.has("expenses")) {
                val expArray = root.getJSONArray("expenses")
                for (i in 0 until expArray.length()) {
                    val e = expArray.getJSONObject(i)
                    val exp = Expense(
                        category = e.optString("category", "Diğer"),
                        amount = e.optDouble("amount", 0.0),
                        date = e.optLong("date", System.currentTimeMillis()),
                        description = e.optString("description", "")
                    )
                    expenseDao.insert(exp)
                }
            }

            logActivity("Yedekten Geri Yüklendi", "Veriler başarıyla içe aktarıldı", "BACKUP")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Sample Data Loader (Opt-in)
    suspend fun loadSampleData() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        // 1. Customers
        val c1Id = customerDao.insert(Customer(name = "Ahmet Yılmaz", phone = "0532 555 12 34", address = "Kadıköy Moda Cd. No:14/2", notes = "Klima ve elektrik panosu için aradı."))
        val c2Id = customerDao.insert(Customer(name = "Zeynep Kaya", phone = "0544 444 88 99", address = "Beşiktaş Barbaros Blv. No:88", notes = "Banyo tesisatı ve batarya değişimi"))
        val c3Id = customerDao.insert(Customer(name = "Murat Demir", phone = "0505 111 22 33", address = "Üsküdar Çamlıca Mah. Menekşe Sk. No:5", notes = "Daire boya & badana işi"))
        val c4Id = customerDao.insert(Customer(name = "Sevgi Çelik", phone = "0553 999 77 66", address = "Ataşehir Batı Mah. Ihlamur Sk.", notes = "Kombi bakımı ve petek temizliği"))

        // 2. Jobs
        cal.timeInMillis = now
        val todayDate = cal.timeInMillis

        cal.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrowDate = cal.timeInMillis

        cal.add(Calendar.DAY_OF_YEAR, -3)
        val pastDate = cal.timeInMillis

        // Job 1: In progress today
        val j1Id = jobDao.insert(Job(
            jobCode = "IS-1001",
            customerId = c1Id,
            title = "Ana Sigorta Kutusu Değişimi & Kaçak Akım",
            description = "Eski sigorta panosu sökülüp Viko kaçak akım rölesi ve 8 adet sigorta takılacak.",
            category = "Elektrik & Elektronik",
            address = "Kadıköy Moda Cd. No:14/2",
            scheduledDate = todayDate,
            scheduledTime = "14:30",
            status = JobStatus.IN_PROGRESS,
            priority = JobPriority.HIGH,
            estimatedPrice = 3800.0,
            finalPrice = 3800.0,
            notes = "Dairenin ana şalteri bina girişinden indirilecek."
        ))

        // Job 2: Scheduled tomorrow
        val j2Id = jobDao.insert(Job(
            jobCode = "IS-1002",
            customerId = c2Id,
            title = "Mutfak Bataryası & Lavabo Gider Onarımı",
            description = "Su sızdıran mutfak bataryası yenisiyle değiştirilecek ve fleks hortumlar yenilenecek.",
            category = "Sıhhi Tesisat",
            address = "Beşiktaş Barbaros Blv. No:88",
            scheduledDate = tomorrowDate,
            scheduledTime = "10:00",
            status = JobStatus.SCHEDULED,
            priority = JobPriority.NORMAL,
            estimatedPrice = 1750.0,
            notes = "ECA batarya müşteri tarafından hazırlandı."
        ))

        // Job 3: Completed 3 days ago
        val j3Id = jobDao.insert(Job(
            jobCode = "IS-1003",
            customerId = c4Id,
            title = "Kombi Yıllık Bakımı ve Tesisat Temizliği",
            description = "E.C.A kombi fan ve yanma odası temizliği, genleşme tankı havası basıldı, filtre temizlendi.",
            category = "Klima & Isıtma",
            address = "Ataşehir Batı Mah. Ihlamur Sk.",
            scheduledDate = pastDate,
            scheduledTime = "16:00",
            status = JobStatus.COMPLETED,
            priority = JobPriority.NORMAL,
            estimatedPrice = 2200.0,
            finalPrice = 2200.0,
            notes = "İş sorunsuz tamamlandı, garanti 1 yıl verildi."
        ))

        // Job 4: Urgent New Job
        jobDao.insert(Job(
            jobCode = "IS-1004",
            customerId = c3Id,
            title = "Salon Tavanı Su Hasarı Alçı & Boya",
            description = "Üst kattan akan su tavanı kabartmış. Kuruma sonrası fileli alçı ve Dyo tavan boyası yapılacak.",
            category = "Boya & Badana",
            address = "Üsküdar Çamlıca Mah. Menekşe Sk. No:5",
            scheduledDate = todayDate,
            scheduledTime = "17:00",
            status = JobStatus.NEW,
            priority = JobPriority.URGENT,
            estimatedPrice = 4500.0,
            notes = "Müşteri acil bakılmasını istedi."
        ))

        // 3. Payments
        // Partial payment for Job 1
        paymentDao.insert(Payment(
            customerId = c1Id,
            jobId = j1Id,
            amount = 1500.0,
            date = now - (2 * 3600 * 1000),
            paymentMethod = PaymentMethod.CASH,
            description = "Malzeme avansı nakit alındı."
        ))
        // Full payment for Job 3
        paymentDao.insert(Payment(
            customerId = c4Id,
            jobId = j3Id,
            amount = 2200.0,
            date = pastDate + (4 * 3600 * 1000),
            paymentMethod = PaymentMethod.TRANSFER,
            description = "Kombi bakım bedeli EFT ile tahsil edildi."
        ))

        // 4. Expenses
        expenseDao.insert(Expense(
            category = "Malzeme",
            amount = 1250.0,
            date = now - (24 * 3600 * 1000),
            description = "Sigorta kutusu, kaçak akım rölesi ve W otomatlar"
        ))
        expenseDao.insert(Expense(
            category = "Yakıt",
            amount = 850.0,
            date = now - (48 * 3600 * 1000),
            description = "Servis aracı mazot dolumu"
        ))
        expenseDao.insert(Expense(
            category = "Ekipman",
            amount = 450.0,
            date = now - (72 * 3600 * 1000),
            description = "Dijital multimetre prob takımı ve klemensler"
        ))

        // 5. Quote
        val quoteId = quoteDao.insertQuote(Quote(
            quoteNumber = "TEK-1001",
            customerId = c3Id,
            title = "3+1 Daire Komple Boya ve Tadilat Teklifi",
            date = now,
            validUntilDate = now + (14L * 24 * 60 * 60 * 1000),
            status = QuoteStatus.SENT,
            notes = "Boya malzemeleri Marshall Silikonlu İpek Mat olarak fiyata dahildir. Süre 4 iş günüdür.",
            totalAmount = 24500.0
        ))
        quoteDao.insertItems(listOf(
            QuoteItem(quoteId = quoteId, description = "Tüm tavanlar fileli tamirat ve tavan boyası", quantity = 110.0, unit = "m²", unitPrice = 70.0, total = 7700.0),
            QuoteItem(quoteId = quoteId, description = "İç cephe duvarlar 2 kat Marshall boya uygulaması", quantity = 280.0, unit = "m²", unitPrice = 50.0, total = 14000.0),
            QuoteItem(quoteId = quoteId, description = "Zemin koruma, kapı bantlama ve son temizlik", quantity = 1.0, unit = "Paket", unitPrice = 2800.0, total = 2800.0)
        ))

        logActivity("Örnek Veriler Yüklendi", "Tanıtım ve test amaçlı kayıtlar eklendi.", "SYSTEM")
    }

    suspend fun resetAllData() = withContext(Dispatchers.IO) {
        db.clearAllTables()
        profileDao.insertOrUpdate(BusinessProfile(id = 1, isFirstSetupDone = true))
        logActivity("Veriler Sıfırlandı", "Tüm sistem kayıtları temizlendi.", "SYSTEM")
    }
}
