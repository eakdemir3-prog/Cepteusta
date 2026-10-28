package com.example

import android.graphics.Bitmap
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.captureToImage
import com.example.data.model.JobStatus
import com.example.data.repository.CepteUstaRepository
import com.example.ui.components.CepteUstaBottomNavBar
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CustomerDetailScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FinancesScreen
import com.example.ui.screens.JobDetailScreen
import com.example.ui.screens.JobsScreen
import com.example.ui.screens.QuoteDetailScreen
import com.example.ui.screens.ReceivablesScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.theme.UstacaTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Play Store görselleri için gerçek uygulama ekranlarını örnek verilerle PNG olarak kaydeder.
 * Çalıştırma: gradle :app:testDebugUnitTest --tests "com.example.StoreScreenshotTest"
 * Çıktı: app/build/store-shots/
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-xxhdpi")
class StoreScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private lateinit var repo: CepteUstaRepository

    @Before
    fun seed() = runBlocking {
        repo = CepteUstaApp.instance.repository
        if (repo.allCustomers.first().isEmpty()) repo.loadSampleData()
        val profile = repo.getProfile()
        repo.saveProfile(
            profile.copy(
                businessName = "Yılmaz Elektrik & Tesisat",
                category = "Elektrik & Elektronik",
                phone = "0532 000 00 00",
                address = "Kadıköy, İstanbul",
                isFirstSetupDone = true
            )
        )
    }

    private fun save(name: String, waitForText: String? = null, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.setContent { UstacaTheme(darkTheme = dark) { content() } }
        if (waitForText != null) {
            compose.waitUntil(15_000) {
                compose.onAllNodesWithText(waitForText, substring = true).fetchSemanticsNodes().isNotEmpty()
            }
        }
        compose.mainClock.advanceTimeBy(1_500)
        compose.waitForIdle()
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val dir = File(System.getProperty("user.dir"), "build/store-shots").apply { mkdirs() }
        File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Composable
    private fun WithBottomBar(route: String, content: @Composable () -> Unit) {
        Scaffold(bottomBar = { CepteUstaBottomNavBar(currentRoute = route, onNavigate = {}) }) { pad ->
            androidx.compose.foundation.layout.Box(Modifier.padding(pad)) { content() }
        }
    }

    @Test
    fun s1_dashboard() {
        val profile = runBlocking { repo.getProfile() }
        val stats = runBlocking { repo.getDashboardStats().first() }
        val jobs = runBlocking { repo.allJobsWithCustomer.first() }
        val activity = runBlocking { repo.recentActivity.first() }
        save("1_dashboard") {
            WithBottomBar(NavRoutes.DASHBOARD) {
                DashboardScreen(profile, stats, jobs, activity, {}, {}, {}, {}, {}, {}, {})
            }
        }
    }

    @Test
    fun s2_job_detail() {
        val jobs = runBlocking { repo.allJobsWithCustomer.first() }
        val job = (jobs.firstOrNull { it.job.status == JobStatus.IN_PROGRESS } ?: jobs.first()).job
        save("2_job_detail", waitForText = job.title) {
            JobDetailScreen(job.id, repo, "₺", {}, {}, { _, _ -> }, {})
        }
    }

    @Test
    fun s3_calendar() {
        val jobs = runBlocking { repo.allJobsWithCustomer.first() }
        save("3_calendar") {
            WithBottomBar(NavRoutes.CALENDAR) { CalendarScreen(jobs, "₺", {}, {}) }
        }
    }

    @Test
    fun s4_receivables() {
        val profile = runBlocking { repo.getProfile() }
        val jobs = runBlocking { repo.allJobsWithCustomer.first() }
        val payments = runBlocking { repo.allPayments.first() }
        save("4_receivables") {
            ReceivablesScreen(jobs, payments, "₺", profile.businessName, {}, {})
        }
    }

    @Test
    fun s5_quote_detail() {
        val profile = runBlocking { repo.getProfile() }
        val quote = runBlocking { repo.allQuotes.first() }.firstOrNull() ?: return
        save("5_quote_detail", waitForText = quote.title) {
            QuoteDetailScreen(quote.id, profile, repo, "₺", {}, {}, {}, {})
        }
    }

    @Test
    fun s6_customer_detail() {
        val customer = runBlocking { repo.allCustomers.first() }.first()
        save("6_customer_detail", waitForText = customer.phone) {
            CustomerDetailScreen(customer.id, repo, "₺", {}, {}, {}, {}, {}, {})
        }
    }

    @Test
    fun s7_finances() {
        val profile = runBlocking { repo.getProfile() }
        val payments = runBlocking { repo.allPayments.first() }
        val expenses = runBlocking { repo.allExpenses.first() }
        val customers = runBlocking { repo.allCustomers.first() }
        val jobs = runBlocking { repo.allJobsWithCustomer.first() }
        save("7_finances") {
            FinancesScreen(payments, expenses, customers, "₺", {}, {}, {}, {}, {}, profile, jobs)
        }
    }

    @Test
    fun s8_reports() {
        val profile = runBlocking { repo.getProfile() }
        val jobs = runBlocking { repo.allJobsWithCustomer.first() }
        val payments = runBlocking { repo.allPayments.first() }
        val expenses = runBlocking { repo.allExpenses.first() }
        save("8_reports") { ReportsScreen(profile, jobs, payments, expenses, "₺", {}) }
    }

    @Test
    fun s9_jobs() {
        val jobs = runBlocking { repo.allJobsWithCustomer.first() }
        save("9_jobs") {
            WithBottomBar(NavRoutes.JOBS) { JobsScreen(jobs, "₺", {}, {}) }
        }
    }

    @Test
    fun s10_dashboard_dark() {
        val profile = runBlocking { repo.getProfile() }
        val stats = runBlocking { repo.getDashboardStats().first() }
        val jobs = runBlocking { repo.allJobsWithCustomer.first() }
        val activity = runBlocking { repo.recentActivity.first() }
        save("10_dashboard_dark", dark = true) {
            WithBottomBar(NavRoutes.DASHBOARD) {
                DashboardScreen(profile, stats, jobs, activity, {}, {}, {}, {}, {}, {}, {})
            }
        }
    }
}
