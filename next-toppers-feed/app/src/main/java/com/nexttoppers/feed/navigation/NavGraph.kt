package com.nexttoppers.feed.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nexttoppers.feed.ui.activity.ActivityFeedScreen
import com.nexttoppers.feed.ui.admin.AdminAnnouncementsScreen
import com.nexttoppers.feed.ui.admin.AdminDashboardScreen
import com.nexttoppers.feed.ui.admin.AdminQuizManagementScreen
import com.nexttoppers.feed.ui.admin.AnalyticsScreen
import com.nexttoppers.feed.ui.admin.ModerationScreen
import com.nexttoppers.feed.ui.admin.PremiumRequestsScreen
import com.nexttoppers.feed.ui.admin.ResourceManagementScreen
import com.nexttoppers.feed.ui.announcements.AnnouncementDetailScreen
import com.nexttoppers.feed.ui.auth.LoginScreen
import com.nexttoppers.feed.ui.chats.ChatListViewModel
import com.nexttoppers.feed.ui.chats.ChatScreen
import com.nexttoppers.feed.ui.chats.ChatsScreen
import com.nexttoppers.feed.ui.downloads.DownloadsScreen
import com.nexttoppers.feed.ui.home.HomeScreen
import com.nexttoppers.feed.ui.leaderboard.LeaderboardScreen
import com.nexttoppers.feed.ui.lecture.LecturePlayerScreen
import com.nexttoppers.feed.ui.legal.AboutScreen
import com.nexttoppers.feed.ui.legal.AppFeedbackScreen
import com.nexttoppers.feed.ui.legal.LegalScreen
import com.nexttoppers.feed.ui.legal.LegalType
import com.nexttoppers.feed.ui.legal.ReportIssueScreen
import com.nexttoppers.feed.ui.notifications.NotificationCenterScreen
import com.nexttoppers.feed.ui.pdf.PdfViewerScreen
import com.nexttoppers.feed.ui.premium.PremiumScreen
import com.nexttoppers.feed.ui.profile.ProfileScreen
import com.nexttoppers.feed.ui.quiz.QuizHomeScreen
import com.nexttoppers.feed.ui.quiz.QuizPlayerScreen
import com.nexttoppers.feed.ui.quiz.QuizPlayerViewModel
import com.nexttoppers.feed.ui.quiz.QuizResultScreen
import com.nexttoppers.feed.ui.resources.ResourceDetailScreen
import com.nexttoppers.feed.ui.resources.ResourcesScreen
import com.nexttoppers.feed.ui.resources.SubjectResourcesScreen
import com.nexttoppers.feed.ui.settings.SettingsScreen
import com.nexttoppers.feed.ui.splash.SplashScreen
import com.nexttoppers.feed.ui.tests.TestsScreen
import com.nexttoppers.feed.util.AppLogger
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.net.URLEncoder

object Routes {
    const val SPLASH = "splash"
    const val LOGIN  = "login"

    const val HOME        = "home"
    const val RESOURCES   = "resources"
    const val LEADERBOARD = "leaderboard"
    const val CHATS       = "chats"
    const val PROFILE     = "profile"

    const val SETTINGS           = "settings"
    const val DOWNLOADS          = "downloads"
    const val NOTIFICATIONS      = "notifications"
    const val PREMIUM            = "premium"
    const val ADMIN              = "admin"
    const val ADMIN_PREMIUM_REQUESTS = "admin/premium_requests"
    const val ADMIN_RESOURCES    = "admin/resources"
    const val ADMIN_ANNOUNCEMENTS = "admin/announcements"
    const val ADMIN_MODERATION   = "admin/moderation"
    const val ADMIN_ANALYTICS    = "admin/analytics"
    const val ADMIN_QUIZ         = "admin/quiz"
    const val ABOUT              = "about"
    const val LEGAL_PRIVACY      = "legal/privacy"
    const val LEGAL_TERMS        = "legal/terms"
    const val REPORT_ISSUE       = "report_issue"
    const val FEEDBACK           = "feedback"
    const val ACTIVITY_FEED      = "activity_feed"

    const val SUBJECT_RESOURCES = "resources/subject/{subject}"
    const val RESOURCE_DETAIL   = "resources/detail/{resourceId}"

    const val PDF_VIEWER =
        "pdf_viewer?resourceId={resourceId}&title={title}&localPath={localPath}&fileUrl={fileUrl}"

    const val QUIZ_HOME   = "quiz_home"
    const val QUIZ_PLAYER = "quiz_player/{quizId}"
    const val QUIZ_RESULT = "quiz_result"

    const val TESTS = "tests"

    const val CHAT = "chat/{chatId}"

    const val LECTURE_PLAYER = "lecture_player?url={url}&title={title}"

    const val ANNOUNCEMENT_DETAIL = "announcement/{announcementId}"

    fun chat(chatId: String) = "chat/$chatId"
    fun quizPlayer(quizId: String) = "quiz_player/$quizId"
    fun announcementDetail(id: String) = "announcement/$id"

    fun lecturePlayer(url: String, title: String): String {
        val encodedUrl   = URLEncoder.encode(url,   "UTF-8")
        val encodedTitle = URLEncoder.encode(title, "UTF-8")
        return "lecture_player?url=$encodedUrl&title=$encodedTitle"
    }

    fun subjectResources(subject: String) = "resources/subject/$subject"
    fun resourceDetail(id: String) = "resources/detail/$id"

    fun pdfViewer(
        resourceId: String,
        title: String,
        localPath: String,
        fileUrl: String = ""
    ): String {
        val encodedTitle   = URLEncoder.encode(title,     "UTF-8")
        val encodedPath    = URLEncoder.encode(localPath, "UTF-8")
        val encodedFileUrl = URLEncoder.encode(fileUrl,   "UTF-8")
        return "pdf_viewer?resourceId=$resourceId&title=$encodedTitle&localPath=$encodedPath&fileUrl=$encodedFileUrl"
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME,        "Home",      Icons.Rounded.Home),
    BottomNavItem(Routes.RESOURCES,   "Resources", Icons.Rounded.LibraryBooks),
    BottomNavItem(Routes.LEADERBOARD, "Ranks",     Icons.Rounded.EmojiEvents),
    BottomNavItem(Routes.CHATS,       "Connect",   Icons.Rounded.Chat),
    BottomNavItem(Routes.PROFILE,     "Profile",   Icons.Rounded.Person)
)

private val bottomNavRoutes = bottomNavItems.map { it.route }.toSet()

fun NavController.navigateSafe(route: String) {
    runCatching {
        if (currentDestination?.route != route) navigate(route)
    }.onFailure {
        AppLogger.error("NavSafe", "Failed to navigate to $route: ${it.message}")
    }
}

@Composable
fun NtfNavGraph() {
    val rootNavController = rememberNavController()

    NavHost(
        navController    = rootNavController,
        startDestination = Routes.SPLASH,
        enterTransition  = { fadeIn(tween(300)) },
        exitTransition   = { fadeOut(tween(200)) }
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToHome = {
                    rootNavController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    rootNavController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    rootNavController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.HOME,
            enterTransition = { fadeIn(tween(420)) },
            exitTransition  = { fadeOut(tween(280)) }
        ) {
            MainAppShell(
                onSignedOut = {
                    rootNavController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
private fun MainAppShell(onSignedOut: () -> Unit) {
    val navController = rememberNavController()
    val navBackStack  by navController.currentBackStackEntryAsState()
    val currentRoute  = navBackStack?.destination?.route
    val showBottomBar = bottomNavRoutes.contains(currentRoute)

    val chatListVm: ChatListViewModel = hiltViewModel()
    val totalUnread  by chatListVm.totalUnread.collectAsState()

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope       = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState     = drawerState,
        gesturesEnabled = drawerState.isOpen || showBottomBar,
        drawerContent   = {
            NtfNavDrawer(
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    navController.navigateSafe(route)
                },
                onSignOut = {
                    scope.launch { drawerState.close() }
                    onSignedOut()
                }
            )
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 3.dp
                    ) {
                        val currentDest = navBackStack?.destination
                        bottomNavItems.forEach { item ->
                            val selected = currentDest?.hierarchy?.any { it.route == item.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState    = true
                                    }
                                },
                                icon = {
                                    if (item.route == Routes.CHATS && totalUnread > 0) {
                                        BadgedBox(badge = {
                                            Badge { Text(if (totalUnread > 9) "9+" else totalUnread.toString()) }
                                        }) {
                                            Icon(item.icon, item.label)
                                        }
                                    } else {
                                        Icon(item.icon, item.label)
                                    }
                                },
                                label = { Text(item.label, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController    = navController,
                startDestination = Routes.HOME,
                modifier         = Modifier.padding(innerPadding),
                enterTransition  = { fadeIn(tween(280)) + slideInHorizontally(tween(280)) { it / 6 } },
                exitTransition   = { fadeOut(tween(200)) },
                popEnterTransition  = { fadeIn(tween(280)) },
                popExitTransition   = { fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { it / 6 } }
            ) {

                // ── Home ────────────────────────────────────────────────────────
                composable(Routes.HOME) {
                    HomeScreen(
                        onNavigateToNotes              = { navController.navigateSafe(Routes.RESOURCES) },
                        onNavigateToLectures           = { navController.navigateSafe(Routes.RESOURCES) },
                        onNavigateToTests              = { navController.navigateSafe(Routes.TESTS) },
                        onNavigateToChats              = { navController.navigateSafe(Routes.CHATS) },
                        onNavigateToLeaderboard        = { navController.navigateSafe(Routes.LEADERBOARD) },
                        onNavigateToPremium            = { navController.navigateSafe(Routes.PREMIUM) },
                        onNavigateToSettings           = { navController.navigateSafe(Routes.SETTINGS) },
                        onNavigateToNotifications      = { navController.navigateSafe(Routes.NOTIFICATIONS) },
                        onNavigateToAnnouncementDetail = { id -> navController.navigateSafe(Routes.announcementDetail(id)) },
                        onNavigateToActivityFeed       = { navController.navigateSafe(Routes.ACTIVITY_FEED) },
                        onNavigateToSubject            = { subject -> navController.navigateSafe(Routes.subjectResources(subject)) },
                        onOpenDrawer                   = { scope.launch { drawerState.open() } }
                    )
                }

                // ── Resources ───────────────────────────────────────────────────
                composable(Routes.RESOURCES) {
                    ResourcesScreen(
                        onNavigateToSubject = { subject -> navController.navigateSafe(Routes.subjectResources(subject)) },
                        onNavigateToDetail  = { id -> navController.navigateSafe(Routes.resourceDetail(id)) }
                    )
                }

                // ── Subject resources ───────────────────────────────────────────
                composable(
                    route = Routes.SUBJECT_RESOURCES,
                    arguments = listOf(navArgument("subject") { type = NavType.StringType; defaultValue = "" })
                ) {
                    SubjectResourcesScreen(
                        onBack            = { navController.popBackStack() },
                        onNavigateToDetail = { id -> navController.navigateSafe(Routes.resourceDetail(id)) }
                    )
                }

                // ── Resource detail ─────────────────────────────────────────────
                composable(
                    route = Routes.RESOURCE_DETAIL,
                    arguments = listOf(navArgument("resourceId") { type = NavType.StringType; defaultValue = "" })
                ) {
                    ResourceDetailScreen(
                        onBack        = { navController.popBackStack() },
                        onOpenPdf     = { localPath, resourceId, title, fileUrl ->
                            navController.navigateSafe(Routes.pdfViewer(resourceId, title, localPath, fileUrl))
                        },
                        onPlayLecture = { url, title ->
                            navController.navigateSafe(Routes.lecturePlayer(url, title))
                        }
                    )
                }

                // ── Leaderboard ─────────────────────────────────────────────────
                composable(Routes.LEADERBOARD) { LeaderboardScreen() }

                // ── Chats ───────────────────────────────────────────────────────
                composable(Routes.CHATS) {
                    ChatsScreen(onNavigateToChat = { chatId -> navController.navigate(Routes.chat(chatId)) })
                }

                // ── Single chat ─────────────────────────────────────────────────
                composable(
                    route = Routes.CHAT,
                    arguments = listOf(navArgument("chatId") { type = NavType.StringType; defaultValue = "" })
                ) {
                    ChatScreen(onBack = { navController.popBackStack() })
                }

                // ── Profile ─────────────────────────────────────────────────────
                composable(Routes.PROFILE) {
                    ProfileScreen(
                        onSignedOut          = onSignedOut,
                        onNavigateToSettings = { navController.navigateSafe(Routes.SETTINGS) },
                        onNavigateToAdmin    = { navController.navigateSafe(Routes.ADMIN) }
                    )
                }

                // ── Settings ────────────────────────────────────────────────────
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        onBack                    = { navController.popBackStack() },
                        onSignOut                 = { navController.popBackStack(); onSignedOut() },
                        onNavigateToPrivacyPolicy = { navController.navigateSafe(Routes.LEGAL_PRIVACY) },
                        onNavigateToTerms         = { navController.navigateSafe(Routes.LEGAL_TERMS) },
                        onNavigateToAbout         = { navController.navigateSafe(Routes.ABOUT) },
                        onNavigateToReportIssue   = { navController.navigateSafe(Routes.REPORT_ISSUE) },
                        onNavigateToFeedback      = { navController.navigateSafe(Routes.FEEDBACK) },
                        onNavigateToDownloads     = { navController.navigateSafe(Routes.DOWNLOADS) }
                    )
                }

                // ── Downloads ───────────────────────────────────────────────────
                composable(Routes.DOWNLOADS) {
                    DownloadsScreen(
                        onOpenPdf = { localPath, resourceId, title ->
                            navController.navigateSafe(Routes.pdfViewer(resourceId, title, localPath))
                        },
                        onNavigateToDetail = { id -> navController.navigateSafe(Routes.resourceDetail(id)) }
                    )
                }

                // ── Notifications ───────────────────────────────────────────────
                composable(Routes.NOTIFICATIONS) {
                    NotificationCenterScreen(
                        onBack       = { navController.popBackStack() },
                        onNavigateTo = { route -> navController.navigateSafe(route) }
                    )
                }

                // ── Premium ─────────────────────────────────────────────────────
                composable(Routes.PREMIUM) {
                    PremiumScreen(
                        onBack           = { navController.popBackStack() },
                        onUpgradeSuccess = { navController.popBackStack() }
                    )
                }

                // ── Admin Dashboard ──────────────────────────────────────────────
                composable(Routes.ADMIN) {
                    AdminDashboardScreen(
                        onNavigateToPremiumRequests = { navController.navigateSafe(Routes.ADMIN_PREMIUM_REQUESTS) },
                        onNavigateToResources       = { navController.navigateSafe(Routes.ADMIN_RESOURCES) },
                        onNavigateToAnnouncements   = { navController.navigateSafe(Routes.ADMIN_ANNOUNCEMENTS) },
                        onNavigateToModeration      = { navController.navigateSafe(Routes.ADMIN_MODERATION) },
                        onNavigateToAnalytics       = { navController.navigateSafe(Routes.ADMIN_ANALYTICS) },
                        onNavigateToQuizManagement  = { navController.navigateSafe(Routes.ADMIN_QUIZ) }
                    )
                }

                // ── Admin sub-screens ────────────────────────────────────────────
                composable(Routes.ADMIN_PREMIUM_REQUESTS) {
                    PremiumRequestsScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.ADMIN_RESOURCES) {
                    ResourceManagementScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.ADMIN_ANNOUNCEMENTS) {
                    AdminAnnouncementsScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.ADMIN_MODERATION) {
                    ModerationScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.ADMIN_ANALYTICS) {
                    AnalyticsScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.ADMIN_QUIZ) {
                    AdminQuizManagementScreen(onBack = { navController.popBackStack() })
                }

                // ── Legal ───────────────────────────────────────────────────────
                composable(Routes.ABOUT) { AboutScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.LEGAL_PRIVACY) {
                    LegalScreen(type = LegalType.PRIVACY_POLICY, onBack = { navController.popBackStack() })
                }
                composable(Routes.LEGAL_TERMS) {
                    LegalScreen(type = LegalType.TERMS_OF_SERVICE, onBack = { navController.popBackStack() })
                }
                composable(Routes.REPORT_ISSUE) {
                    ReportIssueScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.FEEDBACK) {
                    AppFeedbackScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.ACTIVITY_FEED) {
                    ActivityFeedScreen(onBack = { navController.popBackStack() })
                }

                // ── PDF Viewer ──────────────────────────────────────────────────
                composable(
                    route = Routes.PDF_VIEWER,
                    arguments = listOf(
                        navArgument("resourceId") { type = NavType.StringType; defaultValue = "" },
                        navArgument("title")      { type = NavType.StringType; defaultValue = "" },
                        navArgument("localPath")  { type = NavType.StringType; defaultValue = "" },
                        navArgument("fileUrl")    { type = NavType.StringType; defaultValue = "" }
                    )
                ) { backStack ->
                    val title     = URLDecoder.decode(backStack.arguments?.getString("title")     ?: "", "UTF-8")
                    val localPath = URLDecoder.decode(backStack.arguments?.getString("localPath") ?: "", "UTF-8")
                    val fileUrl   = URLDecoder.decode(backStack.arguments?.getString("fileUrl")   ?: "", "UTF-8")
                    val resourceId = backStack.arguments?.getString("resourceId") ?: ""
                    PdfViewerScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                // ── Quiz ────────────────────────────────────────────────────────
                composable(Routes.QUIZ_HOME) {
                    QuizHomeScreen(
                        onNavigateToPlayer = { quizId -> navController.navigate(Routes.quizPlayer(quizId)) }
                    )
                }
                composable(
                    route = Routes.QUIZ_PLAYER,
                    arguments = listOf(navArgument("quizId") { type = NavType.StringType })
                ) {
                    QuizPlayerScreen(
                        onBack             = { navController.popBackStack() },
                        onNavigateToResult = { _, _, _, _, _, _ ->
                            navController.navigate(Routes.QUIZ_RESULT) { launchSingleTop = true }
                        }
                    )
                }
                composable(Routes.QUIZ_RESULT) {
                    val playerEntry = remember(navController) {
                        navController.getBackStackEntry(Routes.QUIZ_PLAYER)
                    }
                    val playerVm: QuizPlayerViewModel = hiltViewModel(playerEntry)
                    QuizResultScreen(
                        onBack          = { navController.popBackStack(Routes.QUIZ_HOME, inclusive = false) },
                        onRetakeQuiz    = { quizId ->
                            navController.navigate(Routes.quizPlayer(quizId)) {
                                popUpTo(Routes.QUIZ_HOME) { inclusive = false }
                            }
                        },
                        playerViewModel = playerVm
                    )
                }

                // ── Tests ────────────────────────────────────────────────────────
                composable(Routes.TESTS) {
                    TestsScreen(onBack = { navController.popBackStack() })
                }

                // ── Lecture Player ───────────────────────────────────────────────
                composable(
                    route = Routes.LECTURE_PLAYER,
                    arguments = listOf(
                        navArgument("url")   { type = NavType.StringType; defaultValue = "" },
                        navArgument("title") { type = NavType.StringType; defaultValue = "" }
                    )
                ) { backStack ->
                    val url   = URLDecoder.decode(backStack.arguments?.getString("url")   ?: "", "UTF-8")
                    val title = URLDecoder.decode(backStack.arguments?.getString("title") ?: "", "UTF-8")
                    LecturePlayerScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                // ── Announcement detail ──────────────────────────────────────────
                composable(
                    route = Routes.ANNOUNCEMENT_DETAIL,
                    arguments = listOf(navArgument("announcementId") { type = NavType.StringType; defaultValue = "" })
                ) {
                    AnnouncementDetailScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}

// ── Navigation Drawer ──────────────────────────────────────────────────────────
@Composable
private fun NtfNavDrawer(
    onNavigate: (String) -> Unit,
    onSignOut: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp)
        ) {
            // Brand header
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("NT", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Next Toppers Feed", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("Study Smarter", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(8.dp))

            drawerItem("Home",          Icons.Rounded.Home,              Routes.HOME,          onNavigate)
            drawerItem("Resources",     Icons.Rounded.LibraryBooks,      Routes.RESOURCES,     onNavigate)
            drawerItem("Leaderboard",   Icons.Rounded.EmojiEvents,       Routes.LEADERBOARD,   onNavigate)
            drawerItem("Notifications", Icons.Rounded.Notifications,     Routes.NOTIFICATIONS, onNavigate)
            drawerItem("Downloads",     Icons.Rounded.Download,          Routes.DOWNLOADS,     onNavigate)
            drawerItem("Premium",       Icons.Rounded.WorkspacePremium,  Routes.PREMIUM,       onNavigate)
            drawerItem("Quiz",          Icons.Rounded.Quiz,              Routes.QUIZ_HOME,     onNavigate)

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(8.dp))

            drawerItem("Settings",    Icons.Rounded.Settings,  Routes.SETTINGS,    onNavigate)
            drawerItem("Feedback",    Icons.Rounded.Feedback,  Routes.FEEDBACK,    onNavigate)
            drawerItem("About",       Icons.Rounded.Info,      Routes.ABOUT,       onNavigate)

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(8.dp))

            // Sign out
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSignOut)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Logout, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(14.dp))
                Text("Sign Out", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun drawerItem(label: String, icon: ImageVector, route: String, onNavigate: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigate(route) }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}
