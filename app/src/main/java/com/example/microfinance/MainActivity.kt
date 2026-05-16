package com.example.microfinance
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.microfinance.ui.dashboard.DashboardScreen
import com.example.microfinance.ui.dashboard.DashboardViewModel
import com.example.microfinance.ui.loan.LoanScreen
import com.example.microfinance.ui.loan.LoanViewModel
import com.example.microfinance.ui.member.MemberScreen
import com.example.microfinance.ui.member.MemberViewModel
import com.example.microfinance.ui.savings.SavingsScreen
import com.example.microfinance.ui.savings.SavingsViewModel
import com.example.microfinance.ui.splash.SplashScreen
import com.example.microfinance.ui.theme.BackgroundLight
import com.example.microfinance.ui.theme.BrandPrimary
import com.example.microfinance.ui.theme.DividerColor
import com.example.microfinance.ui.theme.MicroFinanceTheme
import com.example.microfinance.ui.theme.PoppinsFontFamily
import com.example.microfinance.ui.theme.SurfaceCard
import com.example.microfinance.ui.theme.TextPrimary
import com.example.microfinance.ui.theme.TextSecondary
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Savings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MicroFinanceTheme {
                var showSplash by remember { mutableStateOf(true) }

                AnimatedContent(
                    targetState = showSplash,
                    transitionSpec = {
                        fadeIn(tween(400)) togetherWith fadeOut(tween(300))
                    },
                    label = "splash_transition"
                ) { isSplash ->
                    if (isSplash) {
                        SplashScreen(onSplashComplete = { showSplash = false })
                    } else {
                        MainApp()
                    }
                }
            }
        }
    }
}

@Composable
private fun MainApp() {
    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.MEMBERS) }
    val memberViewModel: MemberViewModel   = viewModel()
    val savingsViewModel: SavingsViewModel = viewModel()
    val loanViewModel: LoanViewModel       = viewModel()
    val dashboardViewModel: DashboardViewModel = viewModel()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundLight,
        bottomBar = {
            PremiumBottomNav(
                currentScreen = currentScreen,
                onScreenSelected = { currentScreen = it }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Top app bar ───────────────────────────────────────────────
            AppTopBar(currentScreen = currentScreen)

            // ── Screen content with animated transition ───────────────────
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    (fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 20 }) togetherWith
                    (fadeOut(tween(150)) + slideOutVertically(tween(150)) { -it / 20 })
                },
                label = "screen_transition",
                modifier = Modifier.fillMaxSize()
            ) { screen ->
                when (screen) {
                    AppScreen.MEMBERS   -> MemberScreen(viewModel = memberViewModel,   modifier = Modifier.fillMaxSize())
                    AppScreen.SAVINGS   -> SavingsScreen(viewModel = savingsViewModel, modifier = Modifier.fillMaxSize())
                    AppScreen.LOANS     -> LoanScreen(viewModel = loanViewModel,       modifier = Modifier.fillMaxSize())
                    AppScreen.DASHBOARD -> DashboardScreen(viewModel = dashboardViewModel, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top App Bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AppTopBar(currentScreen: AppScreen) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(
            text = "Mahila-Shakti Unnati",
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = TextPrimary,
            letterSpacing = 0.2.sp
        )
        Text(
            text = "Micro Finance",
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = BrandPrimary,
            letterSpacing = 1.5.sp
        )
    }
    HorizontalDivider(color = DividerColor, thickness = 1.dp)
}

// ─────────────────────────────────────────────────────────────────────────────
// Premium Bottom Navigation
// ─────────────────────────────────────────────────────────────────────────────

private data class NavItem(
    val screen: AppScreen,
    val label: String,
    val icon: ImageVector
)

@Composable
private fun PremiumBottomNav(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit
) {
    val items = listOf(
        NavItem(AppScreen.MEMBERS,   "Members",   Icons.Rounded.AccountCircle),
        NavItem(AppScreen.SAVINGS,   "Savings",   Icons.Rounded.Savings),
        NavItem(AppScreen.LOANS,     "Loans",     Icons.Rounded.CreditCard),
        NavItem(AppScreen.DASHBOARD, "Dashboard", Icons.Rounded.BarChart),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        HorizontalDivider(color = DividerColor, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                NavTab(
                    item = item,
                    isSelected = currentScreen == item.screen,
                    onClick = { onScreenSelected(item.screen) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NavTab(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconTint  = if (isSelected) BrandPrimary else TextSecondary
    val labelColor = if (isSelected) BrandPrimary else TextSecondary
    val bgColor   = if (isSelected) BrandPrimary.copy(alpha = 0.10f) else Color.Transparent

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true)
            ) { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = item.label,
            fontFamily = PoppinsFontFamily,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 10.sp,
            color = labelColor,
            maxLines = 1
        )
    }
}

private enum class AppScreen {
    MEMBERS, SAVINGS, LOANS, DASHBOARD
}
