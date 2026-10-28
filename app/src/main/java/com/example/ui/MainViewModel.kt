package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ActivityLog
import com.example.data.model.BusinessProfile
import com.example.data.model.Customer
import com.example.data.model.DashboardStats
import com.example.data.model.Expense
import com.example.data.model.Job
import com.example.data.model.JobPhoto
import com.example.data.model.JobWithCustomer
import com.example.data.model.Payment
import com.example.data.model.Quote
import com.example.data.model.QuoteItem
import com.example.data.repository.CepteUstaRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

sealed class Screen {
    object Splash : Screen()
    object Onboarding : Screen()
    object Dashboard : Screen()
    object Jobs : Screen()
    data class JobDetail(val jobId: Long) : Screen()
    data class AddEditJob(val jobId: Long? = null, val initialCustomerId: Long? = null) : Screen()
    object Customers : Screen()
    data class CustomerDetail(val customerId: Long) : Screen()
    data class AddEditCustomer(val customerId: Long? = null) : Screen()
    object CalendarView : Screen()
    object Quotes : Screen()
    data class QuoteDetail(val quoteId: Long) : Screen()
    data class AddEditQuote(val quoteId: Long? = null, val initialCustomerId: Long? = null) : Screen()
    object Finances : Screen()
    data class AddPayment(val prefillJobId: Long? = null, val prefillCustomerId: Long? = null) : Screen()
    object AddExpense : Screen()
    object Reports : Screen()
    object More : Screen()
    object BusinessProfileEdit : Screen()
    object BackupSettings : Screen()
    object Settings : Screen()
}

class MainViewModel(private val repository: CepteUstaRepository) : ViewModel() {

    // Screen Stack for proper back navigation
    private val _screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.Splash))
    val currentScreen: StateFlow<Screen> = MutableStateFlow<Screen>(Screen.Splash).apply {
        viewModelScope.launch {
            _screenStack.collect { stack ->
                value = stack.lastOrNull() ?: Screen.Dashboard
            }
        }
    }

    // Navigation Tab (0: Home, 1: Jobs, 2: New QuickAction, 3: Calendar, 4: More)
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Profile Flow
    val profile: StateFlow<BusinessProfile?> = repository.profileFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Dashboard Stats
    val dashboardStats: StateFlow<DashboardStats> = repository.getDashboardStats().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardStats()
    )

    // Data lists
    val jobs: StateFlow<List<JobWithCustomer>> = repository.allJobsWithCustomer.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val customers: StateFlow<List<Customer>> = repository.activeCustomers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allCustomers: StateFlow<List<Customer>> = repository.allCustomers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val quotes: StateFlow<List<Quote>> = repository.allQuotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val payments: StateFlow<List<Payment>> = repository.allPayments.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val expenses: StateFlow<List<Expense>> = repository.allExpenses.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recentActivity: StateFlow<List<ActivityLog>> = repository.recentActivity.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // UI Feedback events
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Quick action sheet state
    private val _showQuickActionSheet = MutableStateFlow(false)
    val showQuickActionSheet: StateFlow<Boolean> = _showQuickActionSheet.asStateFlow()

    fun setQuickActionSheet(show: Boolean) {
        _showQuickActionSheet.value = show
    }

    fun navigateTo(screen: Screen) {
        val current = _screenStack.value
        // If navigating to a root screen, adjust selectedTab
        when (screen) {
            Screen.Dashboard -> _selectedTab.value = 0
            Screen.Jobs -> _selectedTab.value = 1
            Screen.CalendarView -> _selectedTab.value = 3
            Screen.More -> _selectedTab.value = 4
            else -> {}
        }
        _screenStack.value = current + screen
    }

    fun navigateBack(): Boolean {
        val current = _screenStack.value
        return if (current.size > 1) {
            val newStack = current.dropLast(1)
            _screenStack.value = newStack
            when (newStack.last()) {
                Screen.Dashboard -> _selectedTab.value = 0
                Screen.Jobs -> _selectedTab.value = 1
                Screen.CalendarView -> _selectedTab.value = 3
                Screen.More -> _selectedTab.value = 4
                else -> {}
            }
            true
        } else {
            false
        }
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
        when (index) {
            0 -> _screenStack.value = listOf(Screen.Dashboard)
            1 -> _screenStack.value = listOf(Screen.Jobs)
            2 -> setQuickActionSheet(true)
            3 -> _screenStack.value = listOf(Screen.CalendarView)
            4 -> _screenStack.value = listOf(Screen.More)
        }
    }

    fun onSplashFinished() {
        viewModelScope.launch {
            val prof = repository.getProfile()
            if (!prof.isFirstSetupDone) {
                _screenStack.value = listOf(Screen.Onboarding)
            } else {
                _screenStack.value = listOf(Screen.Dashboard)
            }
        }
    }

    fun completeOnboarding(profile: BusinessProfile) {
        viewModelScope.launch {
            repository.saveProfile(profile.copy(isFirstSetupDone = true))
            _screenStack.value = listOf(Screen.Dashboard)
            _userMessage.emit("Ustaca'ya hoş geldiniz!")
        }
    }

    fun updateProfile(profile: BusinessProfile) {
        viewModelScope.launch {
            repository.saveProfile(profile)
            _userMessage.emit("İşletme profili güncellendi")
        }
    }

    // Customer Actions
    fun saveCustomer(customer: Customer, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveCustomer(customer)
            _userMessage.emit("Müşteri kaydedildi")
            onDone()
        }
    }

    fun deleteCustomer(id: Long, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteCustomer(id)
            _userMessage.emit("Müşteri silindi")
            onDone()
        }
    }

    fun archiveCustomer(id: Long, archived: Boolean) {
        viewModelScope.launch {
            repository.archiveCustomer(id, archived)
            _userMessage.emit(if (archived) "Müşteri arşivlendi" else "Müşteri arşivden çıkarıldı")
        }
    }

    // Job Actions
    fun saveJob(job: Job, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.saveJob(job)
            _userMessage.emit("İş kaydedildi: ${job.title}")
            onDone(id)
        }
    }

    fun updateJobStatus(jobId: Long, status: String, finalPrice: Double? = null) {
        viewModelScope.launch {
            repository.updateJobStatus(jobId, status, finalPrice)
            _userMessage.emit("İş durumu güncellendi")
        }
    }

    fun deleteJob(jobId: Long, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteJob(jobId)
            _userMessage.emit("İş silindi")
            onDone()
        }
    }

    fun addJobPhoto(photo: JobPhoto) {
        viewModelScope.launch {
            repository.addJobPhoto(photo)
            _userMessage.emit("Fotoğraf eklendi")
        }
    }

    fun deleteJobPhoto(photoId: Long) {
        viewModelScope.launch {
            repository.deleteJobPhoto(photoId)
            _userMessage.emit("Fotoğraf silindi")
        }
    }

    // Quote Actions
    fun saveQuote(quote: Quote, items: List<QuoteItem>, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.saveQuote(quote, items)
            _userMessage.emit("Teklif kaydedildi")
            onDone(id)
        }
    }

    fun updateQuoteStatus(quoteId: Long, status: String) {
        viewModelScope.launch {
            repository.updateQuoteStatus(quoteId, status)
            _userMessage.emit("Teklif durumu güncellendi")
        }
    }

    fun convertQuoteToJob(quoteId: Long, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val jobId = repository.convertQuoteToJob(quoteId)
            _userMessage.emit("Teklif başarıyla işe dönüştürüldü!")
            onDone(jobId)
        }
    }

    fun deleteQuote(quoteId: Long, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteQuote(quoteId)
            _userMessage.emit("Teklif silindi")
            onDone()
        }
    }

    // Payments
    fun addPayment(payment: Payment, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.addPayment(payment)
            _userMessage.emit("Tahsilat kaydedildi")
            onDone()
        }
    }

    fun deletePayment(paymentId: Long) {
        viewModelScope.launch {
            repository.deletePayment(paymentId)
            _userMessage.emit("Tahsilat silindi")
        }
    }

    // Expenses
    fun addExpense(expense: Expense, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.addExpense(expense)
            _userMessage.emit("Gider kaydedildi")
            onDone()
        }
    }

    fun deleteExpense(expenseId: Long) {
        viewModelScope.launch {
            repository.deleteExpense(expenseId)
            _userMessage.emit("Gider kaydı silindi")
        }
    }

    // Sample Data & Reset
    fun loadSampleData() {
        viewModelScope.launch {
            repository.loadSampleData()
            _userMessage.emit("Örnek veriler başarıyla yüklendi!")
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
            _userMessage.emit("Tüm veriler sıfırlandı.")
            _screenStack.value = listOf(Screen.Dashboard)
        }
    }

    // JSON Backup / Restore
    fun exportBackup(context: Context, onFileReady: (File) -> Unit) {
        viewModelScope.launch {
            val file = repository.exportDataToJson(context)
            onFileReady(file)
            _userMessage.emit("Yedek dosyası hazırlandı")
        }
    }

    fun restoreBackup(jsonContent: String) {
        viewModelScope.launch {
            val ok = repository.restoreDataFromJson(jsonContent)
            if (ok) {
                _userMessage.emit("Yedek başarıyla geri yüklendi!")
            } else {
                _userMessage.emit("Yedek dosyası formatı geçersiz!")
            }
        }
    }

    companion object {
        fun factory(repository: CepteUstaRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(repository) as T
                }
            }
    }
}
