package com.nexttoppers.feed.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nexttoppers.feed.ui.auth.LoginScreen
import com.nexttoppers.feed.ui.chats.ChatsScreen
import com.nexttoppers.feed.ui.tests.TestsScreen
import com.nexttoppers.feed.ui.downloads.DownloadsScreen
import com.nexttoppers.feed.ui.home.HomeScreen
import com.nexttoppers.feed.ui.leaderboard.LeaderboardScreen
import com.nexttoppers.feed.ui.pdf.PdfViewerScreen
import com.nexttoppers.feed.ui.profile.ProfileScreen
import com.nexttoppers.feed.ui.quiz.QuizHomeScreen
import com.nexttoppers.feed.ui.resources.ResourceDetailScreen
import com.nexttoppers.feed.ui.resources.ResourcesScreen
import com.nexttoppers.feed.ui.resources.SubjectResourcesScreen
import com.nexttoppers.feed.ui.settings.SettingsScreen
import com.nexttoppers.feed.ui.splash.SplashScreen
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.SurfaceDark
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.util.AppLogger
import java.net.URLEncoder

object Routes {

    const val SPLASH = "splash"
    const val LOGIN = "login"

    const val HOME = "home"
    const val RESOURCES = "resources"
    const val LEADERBOARD = "leaderboard"
    const val CHATS = "chats"
    const val PROFILE = "profile"

    const val SETTINGS = "settings"
    const val DOWNLOADS = "downloads"

    const val SUBJECT_RESOURCES =
        "resources/subject/{subject}"

    const val RESOURCE_DETAIL =
        "resources/detail/{resourceId}"

    const val PDF_VIEWER =
        "pdf_viewer?resourceId={resourceId}&title={title}&localPath={localPath}"

    const val QUIZ_HOME =
        "quiz_home"

    const val TESTS = "tests"

    fun subjectResources(
        subject: String
    ) = "resources/subject/$subject"

    fun resourceDetail(
        id: String
    ) = "resources/detail/$id"

    fun pdfViewer(
        resourceId: String,
        title: String,
        localPath: String
    ): String {

        val encodedTitle =
            URLEncoder.encode(title, "UTF-8")

        val encodedPath =
            URLEncoder.encode(localPath, "UTF-8")

        return "pdf_viewer?resourceId=$resourceId&title=$encodedTitle&localPath=$encodedPath"
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(

    BottomNavItem(
        route = Routes.HOME,
        label = "Home",
        icon = Icons.Rounded.Home
    ),

    BottomNavItem(
        route = Routes.RESOURCES,
        label = "Resources",
        icon = Icons.Rounded.LibraryBooks
    ),

    BottomNavItem(
        route = Routes.LEADERBOARD,
        label = "Ranks",
        icon = Icons.Rounded.EmojiEvents
    ),

    BottomNavItem(
        route = Routes.CHATS,
        label = "Connect",
        icon = Icons.Rounded.Chat
    ),

    BottomNavItem(
        route = Routes.PROFILE,
        label = "Profile",
        icon = Icons.Rounded.Person
    )
)

private val bottomNavRoutes =
    bottomNavItems.map { it.route }.toSet()

fun NavController.navigateSafe(
    route: String
) {

    runCatching {

        if (currentDestination?.route != route) {
            navigate(route)
        }

    }.onFailure {

        AppLogger.error(
            "NavSafe",
            "Failed to navigate to $route: ${it.message}"
        )
    }
}

@Composable
fun NtfNavGraph() {

    val rootNavController =
        rememberNavController()

    NavHost(
        navController = rootNavController,
        startDestination = Routes.SPLASH,

        enterTransition = {
            fadeIn(tween(300))
        },

        exitTransition = {
            fadeOut(tween(200))
        }
    ) {

        composable(Routes.SPLASH) {

            SplashScreen(

                onNavigateToHome = {

                    rootNavController.navigate(
                        Routes.HOME
                    ) {
                        popUpTo(Routes.SPLASH) {
                            inclusive = true
                        }
                    }
                },

                onNavigateToLogin = {

                    rootNavController.navigate(
                        Routes.LOGIN
                    ) {
                        popUpTo(Routes.SPLASH) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {

            LoginScreen(

                onLoginSuccess = {

                    rootNavController.navigate(
                        Routes.HOME
                    ) {
                        popUpTo(Routes.LOGIN) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(
            route = Routes.HOME,

            enterTransition = {
                fadeIn(tween(420))
            },

            exitTransition = {
                fadeOut(tween(280))
            }
        ) {

            MainAppShell(

                onSignedOut = {

                    rootNavController.navigate(
                        Routes.LOGIN
                    ) {
                        popUpTo(Routes.HOME) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun MainAppShell(
    onSignedOut: () -> Unit
) {

    val navController =
        rememberNavController()

    val navBackStack by
    navController.currentBackStackEntryAsState()

    val currentRoute =
        navBackStack?.destination?.route

    val showBottomBar =
        bottomNavRoutes.contains(currentRoute)

    Scaffold(

        containerColor = BackgroundBlack,

        bottomBar = {

            if (showBottomBar) {

                NavigationBar(
                    containerColor = SurfaceDark,
                    tonalElevation = 0.dp
                ) {

                    val currentDest =
                        navBackStack?.destination

                    bottomNavItems.forEach { item ->

                        val selected =
                            currentDest?.hierarchy?.any {
                                it.route == item.route
                            } == true

                        NavigationBarItem(

                            selected = selected,

                            onClick = {

                                navController.navigate(item.route) {

                                    popUpTo(
                                        navController.graph.findStartDestination().id
                                    ) {
                                        saveState = true
                                    }

                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },

                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                            },

                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 10.sp
                                )
                            },

                            colors =
                                NavigationBarItemDefaults.colors(

                                    selectedIconColor = NeonGreen,
                                    selectedTextColor = NeonGreen,

                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted,

                                    indicatorColor =
                                        NeonGreen.copy(alpha = 0.13f)
                                )
                        )
                    }
                }
            }
        }

    ) { innerPadding ->

        NavHost(

            navController = navController,
            startDestination = Routes.HOME,

            modifier = Modifier.padding(innerPadding),

            enterTransition = {
                fadeIn(tween(260))
            },

            exitTransition = {
                fadeOut(tween(200))
            }
        ) {

            composable(Routes.HOME) {

                HomeScreen(
                    onNavigateToNotes = {},
                    onNavigateToLectures = {},
                    onNavigateToTests = {
                        navController.navigateSafe(Routes.TESTS)
                    },
                    onNavigateToChats = {
                        navController.navigateSafe(Routes.CHATS)
                    },
                    onNavigateToLeaderboard = {
                        navController.navigateSafe(Routes.LEADERBOARD)
                    },
                    onNavigateToPremium = {},
                    onNavigateToSettings = {
                        navController.navigateSafe(Routes.SETTINGS)
                    },
                    onNavigateToNotifications = {},
                    onNavigateToAnnouncementDetail = {},
                    onNavigateToActivityFeed = {}
                )
            }

            composable(Routes.RESOURCES) {

                ResourcesScreen(

                    onNavigateToSubject = { subject ->

                        navController.navigateSafe(
                            Routes.subjectResources(subject)
                        )
                    },

                    onNavigateToDetail = { id ->

                        navController.navigateSafe(
                            Routes.resourceDetail(id)
                        )
                    }
                )
            }

            composable(Routes.LEADERBOARD) {
                LeaderboardScreen()
            }

            composable(Routes.CHATS) {
                ChatsScreen()
            }

            composable(Routes.PROFILE) {

                ProfileScreen(

                    onSignedOut = onSignedOut,

                    onNavigateToSettings = {
                        navController.navigateSafe(
                            Routes.SETTINGS
                        )
                    },

                    onNavigateToAdmin = {}
                )
            }

            composable(
                route = Routes.SETTINGS
            ) {

                SettingsScreen(

                    onBack = {
                        navController.popBackStack()
                    },

                    onSignOut = {

                        navController.popBackStack()
                        onSignedOut()
                    },

                    onNavigateToPrivacyPolicy = {},
                    onNavigateToTerms = {},
                    onNavigateToAbout = {},
                    onNavigateToReportIssue = {},
                    onNavigateToFeedback = {},

                    onNavigateToDownloads = {
                        navController.navigateSafe(
                            Routes.DOWNLOADS
                        )
                    }
                )
            }

            composable(
                route = Routes.DOWNLOADS
            ) {

                DownloadsScreen(

                    onOpenPdf = { localPath, resourceId, title ->

                        navController.navigateSafe(
                            Routes.pdfViewer(
                                resourceId,
                                title,
                                localPath
                            )
                        )
                    },

                    onNavigateToDetail = { id ->

                        navController.navigateSafe(
                            Routes.resourceDetail(id)
                        )
                    }
                )
            }

            composable(

                route = Routes.SUBJECT_RESOURCES,

                arguments = listOf(

                    navArgument("subject") {
                        type = NavType.StringType
                    }
                )
            ) {

                SubjectResourcesScreen(

                    onBack = {
                        navController.popBackStack()
                    },

                    onNavigateToDetail = { id ->

                        navController.navigateSafe(
                            Routes.resourceDetail(id)
                        )
                    }
                )
            }

            composable(

                route = Routes.RESOURCE_DETAIL,

                arguments = listOf(

                    navArgument("resourceId") {
                        type = NavType.StringType
                    }
                )
            ) {

                ResourceDetailScreen(

                    onBack = {
                        navController.popBackStack()
                    },

                    onOpenPdf = { localPath, resourceId, title ->

                        navController.navigateSafe(
                            Routes.pdfViewer(
                                resourceId,
                                title,
                                localPath
                            )
                        )
                    }
                )
            }

            composable(

                route = Routes.PDF_VIEWER,

                arguments = listOf(

                    navArgument("resourceId") {
                        type = NavType.StringType
                        defaultValue = ""
                    },

                    navArgument("title") {
                        type = NavType.StringType
                        defaultValue = ""
                    },

                    navArgument("localPath") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) {

                PdfViewerScreen(

                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Routes.QUIZ_HOME
            ) {

                QuizHomeScreen(
                    onNavigateToPlayer = {}
                )
            }

            composable(
                route = Routes.TESTS
            ) {

                TestsScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
