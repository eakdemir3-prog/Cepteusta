package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.BusinessProfile
import com.example.ui.MainViewModel
import com.example.ui.components.CepteUstaBottomNavBar
import com.example.ui.components.QuickActionBottomSheet
import com.example.ui.screens.AddEditCustomerScreen
import com.example.ui.screens.AddEditJobScreen
import com.example.ui.screens.AddEditQuoteScreen
import com.example.ui.screens.AddExpenseScreen
import com.example.ui.screens.AddPaymentScreen
import com.example.ui.screens.BackupSettingsScreen
import com.example.ui.screens.BusinessProfileScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CustomerDetailScreen
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FinancesScreen
import com.example.ui.screens.JobDetailScreen
import com.example.ui.screens.JobsScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.QuoteDetailScreen
import com.example.ui.screens.QuotesScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.UstacaTheme

object NavRoutes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val JOBS = "jobs"
    const val CUSTOMERS = "customers"
    const val CALENDAR = "calendar"
    const val MORE = "more"
    const val JOB_DETAIL = "job_detail/{jobId}"
    const val JOB_ADD_EDIT = "job_add_edit?jobId={jobId}&customerId={customerId}"
    const val CUSTOMER_DETAIL = "customer_detail/{customerId}"
    const val CUSTOMER_ADD_EDIT = "customer_add_edit?customerId={customerId}"
    const val QUOTES = "quotes"
    const val QUOTE_DETAIL = "quote_detail/{quoteId}"
    const val QUOTE_ADD_EDIT = "quote_add_edit?quoteId={quoteId}&customerId={customerId}"
    const val FINANCES = "finances"
    const val ADD_PAYMENT = "add_payment?jobId={jobId}&customerId={customerId}"
    const val ADD_EXPENSE = "add_expense"
    const val REPORTS = "reports"
    const val BUSINESS_PROFILE = "business_profile"
    const val BACKUP_SETTINGS = "backup_settings"
    const val SETTINGS = "settings"
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.factory(CepteUstaApp.instance.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val profile by viewModel.profile.collectAsStateWithLifecycle()
            val currentProfile = profile ?: BusinessProfile()

            val isDarkTheme = when (currentProfile.themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            UstacaTheme(darkTheme = isDarkTheme) {
                MainAppNavigation(viewModel = viewModel, profile = currentProfile)
            }
        }
    }
}

@Composable
fun MainAppNavigation(viewModel: MainViewModel, profile: BusinessProfile) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavRoutes.SPLASH

    val showQuickAction by viewModel.showQuickActionSheet.collectAsStateWithLifecycle()
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val jobs by viewModel.jobs.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val allCustomers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val quotes by viewModel.quotes.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val recentActivity by viewModel.recentActivity.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val isRootBottomBarScreen = currentRoute in listOf(
        NavRoutes.DASHBOARD,
        NavRoutes.JOBS,
        NavRoutes.CUSTOMERS,
        NavRoutes.CALENDAR,
        NavRoutes.MORE
    )

    fun navigateBottomTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (isRootBottomBarScreen) {
                CepteUstaBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route -> navigateBottomTab(route) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (isRootBottomBarScreen) innerPadding.calculateBottomPadding() else innerPadding.calculateBottomPadding())
        ) {
            NavHost(
                navController = navController,
                startDestination = NavRoutes.SPLASH
            ) {
                composable(NavRoutes.SPLASH) {
                    SplashScreen(
                        onSplashFinished = {
                            val next = if (!profile.isFirstSetupDone) NavRoutes.ONBOARDING else NavRoutes.DASHBOARD
                            navController.navigate(next) {
                                popUpTo(NavRoutes.SPLASH) { inclusive = true }
                            }
                        }
                    )
                }

                composable(NavRoutes.ONBOARDING) {
                    OnboardingScreen(
                        initialProfile = profile,
                        onComplete = {
                            viewModel.completeOnboarding(it)
                            navController.navigate(NavRoutes.DASHBOARD) {
                                popUpTo(NavRoutes.ONBOARDING) { inclusive = true }
                            }
                        }
                    )
                }

                // 1. Dashboard (Ana Sayfa)
                composable(NavRoutes.DASHBOARD) {
                    DashboardScreen(
                        profile = profile,
                        stats = stats,
                        jobs = jobs,
                        recentActivity = recentActivity,
                        onJobClick = { jobId -> navController.navigate("job_detail/$jobId") },
                        onNewJob = { navController.navigate("job_add_edit") },
                        onNewCustomer = { navController.navigate("customer_add_edit") },
                        onNewQuote = { navController.navigate("quote_add_edit") },
                        onAddPayment = { navController.navigate("add_payment") },
                        onSeeAllJobs = { navigateBottomTab(NavRoutes.JOBS) },
                        onProfileClick = { navController.navigate(NavRoutes.BUSINESS_PROFILE) }
                    )
                }

                // 2. Job Orders (İşler / İş Emirleri)
                composable(NavRoutes.JOBS) {
                    JobsScreen(
                        jobs = jobs,
                        currencySymbol = profile.currencySymbol,
                        onJobClick = { jobId -> navController.navigate("job_detail/$jobId") },
                        onNewJob = { navController.navigate("job_add_edit") }
                    )
                }

                // 3. Customers (Müşteriler)
                composable(NavRoutes.CUSTOMERS) {
                    CustomersScreen(
                        customers = customers,
                        onCustomerClick = { custId -> navController.navigate("customer_detail/$custId") },
                        onNewCustomer = { navController.navigate("customer_add_edit") },
                        onBack = { navController.popBackStack() }
                    )
                }

                // 4. Calendar (Takvim)
                composable(NavRoutes.CALENDAR) {
                    CalendarScreen(
                        jobs = jobs,
                        currencySymbol = profile.currencySymbol,
                        onJobClick = { jobId -> navController.navigate("job_detail/$jobId") },
                        onNewJob = { navController.navigate("job_add_edit") }
                    )
                }

                // 5. More (Daha Fazla)
                composable(NavRoutes.MORE) {
                    MoreScreen(
                        profile = profile,
                        onNavigateCustomers = { navigateBottomTab(NavRoutes.CUSTOMERS) },
                        onNavigateQuotes = { navController.navigate(NavRoutes.QUOTES) },
                        onNavigateFinances = { navController.navigate(NavRoutes.FINANCES) },
                        onNavigateReports = { navController.navigate(NavRoutes.REPORTS) },
                        onNavigateProfile = { navController.navigate(NavRoutes.BUSINESS_PROFILE) },
                        onNavigateBackup = { navController.navigate(NavRoutes.BACKUP_SETTINGS) },
                        onNavigateSettings = { navController.navigate(NavRoutes.SETTINGS) }
                    )
                }

                // Job Detail
                composable(
                    route = NavRoutes.JOB_DETAIL,
                    arguments = listOf(navArgument("jobId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val jobId = backStackEntry.arguments?.getLong("jobId") ?: 0L
                    JobDetailScreen(
                        jobId = jobId,
                        repository = CepteUstaApp.instance.repository,
                        currencySymbol = profile.currencySymbol,
                        onBack = { navController.popBackStack() },
                        onEditJob = { id -> navController.navigate("job_add_edit?jobId=$id") },
                        onAddPaymentForJob = { jId, cId ->
                            navController.navigate("add_payment?jobId=$jId&customerId=$cId")
                        },
                        onJobDeleted = { navController.popBackStack() }
                    )
                }

                // Add / Edit Job
                composable(
                    route = NavRoutes.JOB_ADD_EDIT,
                    arguments = listOf(
                        navArgument("jobId") {
                            type = NavType.LongType
                            defaultValue = 0L
                        },
                        navArgument("customerId") {
                            type = NavType.LongType
                            defaultValue = 0L
                        }
                    )
                ) { backStackEntry ->
                    val jobId = backStackEntry.arguments?.getLong("jobId")?.takeIf { it > 0 }
                    val custId = backStackEntry.arguments?.getLong("customerId")?.takeIf { it > 0 }
                    AddEditJobScreen(
                        jobId = jobId,
                        initialCustomerId = custId,
                        customers = customers,
                        repository = CepteUstaApp.instance.repository,
                        onBack = { navController.popBackStack() },
                        onJobSaved = { navController.popBackStack() },
                        onAddNewCustomer = { navController.navigate("customer_add_edit") }
                    )
                }

                // Customer Detail
                composable(
                    route = NavRoutes.CUSTOMER_DETAIL,
                    arguments = listOf(navArgument("customerId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val customerId = backStackEntry.arguments?.getLong("customerId") ?: 0L
                    CustomerDetailScreen(
                        customerId = customerId,
                        repository = CepteUstaApp.instance.repository,
                        currencySymbol = profile.currencySymbol,
                        onBack = { navController.popBackStack() },
                        onEditCustomer = { id -> navController.navigate("customer_add_edit?customerId=$id") },
                        onNewJobForCustomer = { cId -> navController.navigate("job_add_edit?customerId=$cId") },
                        onJobClick = { jId -> navController.navigate("job_detail/$jId") },
                        onQuoteClick = { qId -> navController.navigate("quote_detail/$qId") },
                        onCustomerDeleted = { navController.popBackStack() }
                    )
                }

                // Add / Edit Customer
                composable(
                    route = NavRoutes.CUSTOMER_ADD_EDIT,
                    arguments = listOf(
                        navArgument("customerId") {
                            type = NavType.LongType
                            defaultValue = 0L
                        }
                    )
                ) { backStackEntry ->
                    val customerId = backStackEntry.arguments?.getLong("customerId")?.takeIf { it > 0 }
                    AddEditCustomerScreen(
                        customerId = customerId,
                        repository = CepteUstaApp.instance.repository,
                        onBack = { navController.popBackStack() },
                        onCustomerSaved = { navController.popBackStack() }
                    )
                }

                // Quotes List
                composable(NavRoutes.QUOTES) {
                    QuotesScreen(
                        quotes = quotes,
                        customers = customers,
                        currencySymbol = profile.currencySymbol,
                        onQuoteClick = { qId -> navController.navigate("quote_detail/$qId") },
                        onNewQuote = { navController.navigate("quote_add_edit") },
                        onBack = { navController.popBackStack() }
                    )
                }

                // Quote Detail
                composable(
                    route = NavRoutes.QUOTE_DETAIL,
                    arguments = listOf(navArgument("quoteId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val quoteId = backStackEntry.arguments?.getLong("quoteId") ?: 0L
                    QuoteDetailScreen(
                        quoteId = quoteId,
                        profile = profile,
                        repository = CepteUstaApp.instance.repository,
                        currencySymbol = profile.currencySymbol,
                        onBack = { navController.popBackStack() },
                        onEditQuote = { id -> navController.navigate("quote_add_edit?quoteId=$id") },
                        onJobCreatedFromQuote = { newJobId ->
                            navController.navigate("job_detail/$newJobId") {
                                popUpTo(NavRoutes.QUOTES)
                            }
                        },
                        onQuoteDeleted = { navController.popBackStack() }
                    )
                }

                // Add / Edit Quote
                composable(
                    route = NavRoutes.QUOTE_ADD_EDIT,
                    arguments = listOf(
                        navArgument("quoteId") {
                            type = NavType.LongType
                            defaultValue = 0L
                        },
                        navArgument("customerId") {
                            type = NavType.LongType
                            defaultValue = 0L
                        }
                    )
                ) { backStackEntry ->
                    val quoteId = backStackEntry.arguments?.getLong("quoteId")?.takeIf { it > 0 }
                    val custId = backStackEntry.arguments?.getLong("customerId")?.takeIf { it > 0 }
                    AddEditQuoteScreen(
                        quoteId = quoteId,
                        initialCustomerId = custId,
                        customers = customers,
                        repository = CepteUstaApp.instance.repository,
                        currencySymbol = profile.currencySymbol,
                        onBack = { navController.popBackStack() },
                        onQuoteSaved = { navController.popBackStack() }
                    )
                }

                // Finances
                composable(NavRoutes.FINANCES) {
                    FinancesScreen(
                        payments = payments,
                        expenses = expenses,
                        customers = allCustomers,
                        currencySymbol = profile.currencySymbol,
                        onAddPayment = { navController.navigate("add_payment") },
                        onAddExpense = { navController.navigate(NavRoutes.ADD_EXPENSE) },
                        onDeletePayment = { viewModel.deletePayment(it) },
                        onDeleteExpense = { viewModel.deleteExpense(it) },
                        onBack = { navController.popBackStack() }
                    )
                }

                // Add Payment
                composable(
                    route = NavRoutes.ADD_PAYMENT,
                    arguments = listOf(
                        navArgument("jobId") {
                            type = NavType.LongType
                            defaultValue = 0L
                        },
                        navArgument("customerId") {
                            type = NavType.LongType
                            defaultValue = 0L
                        }
                    )
                ) { backStackEntry ->
                    val jobId = backStackEntry.arguments?.getLong("jobId")?.takeIf { it > 0 }
                    val custId = backStackEntry.arguments?.getLong("customerId")?.takeIf { it > 0 }
                    AddPaymentScreen(
                        prefillJobId = jobId,
                        prefillCustomerId = custId,
                        customers = allCustomers,
                        onSavePayment = {
                            viewModel.addPayment(it) {
                                navController.popBackStack()
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                // Add Expense
                composable(NavRoutes.ADD_EXPENSE) {
                    AddExpenseScreen(
                        onSaveExpense = {
                            viewModel.addExpense(it) {
                                navController.popBackStack()
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                // Reports
                composable(NavRoutes.REPORTS) {
                    ReportsScreen(
                        profile = profile,
                        jobs = jobs,
                        payments = payments,
                        expenses = expenses,
                        currencySymbol = profile.currencySymbol,
                        onBack = { navController.popBackStack() }
                    )
                }

                // Business Profile Edit
                composable(NavRoutes.BUSINESS_PROFILE) {
                    BusinessProfileScreen(
                        currentProfile = profile,
                        onSaveProfile = { viewModel.updateProfile(it) },
                        onBack = { navController.popBackStack() }
                    )
                }

                // Backup & Restore
                composable(NavRoutes.BACKUP_SETTINGS) {
                    BackupSettingsScreen(
                        onExportBackup = { callback ->
                            viewModel.exportBackup(CepteUstaApp.instance, callback)
                        },
                        onRestoreBackup = { json ->
                            viewModel.restoreBackup(json)
                        },
                        onLoadSampleData = { viewModel.loadSampleData() },
                        onResetAllData = {
                            viewModel.resetAllData()
                            navController.navigate(NavRoutes.DASHBOARD) {
                                popUpTo(0)
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                // Settings
                composable(NavRoutes.SETTINGS) {
                    SettingsScreen(
                        profile = profile,
                        onUpdateTheme = { mode ->
                            viewModel.updateProfile(profile.copy(themeMode = mode))
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }

    // Quick Action Bottom Sheet
    if (showQuickAction) {
        QuickActionBottomSheet(
            onDismiss = { viewModel.setQuickActionSheet(false) },
            onNewJob = { navController.navigate("job_add_edit") },
            onNewCustomer = { navController.navigate("customer_add_edit") },
            onNewQuote = { navController.navigate("quote_add_edit") },
            onAddPayment = { navController.navigate("add_payment") },
            onAddExpense = { navController.navigate(NavRoutes.ADD_EXPENSE) }
        )
    }
}
