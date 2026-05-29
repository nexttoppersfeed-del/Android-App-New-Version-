package com.nexttoppers.feed.ui.premium

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexttoppers.feed.data.model.MembershipType
import com.nexttoppers.feed.data.model.PremiumMembership
import com.nexttoppers.feed.data.model.PremiumPlan
import com.nexttoppers.feed.data.model.premiumPlans
import com.nexttoppers.feed.ui.theme.AccentCyan
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.PremiumViolet
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceDark
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary
import kotlinx.coroutines.launch

private const val UPI_ID = "nexttoppersfeed@upi"

// ── Premium screen ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onBack: () -> Unit,
    onUpgradeSuccess: () -> Unit,
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val membership    by viewModel.membership.collectAsState()
    val selectedPlan  by viewModel.selectedPlan.collectAsState()
    val purchaseState by viewModel.purchaseState.collectAsState()
    val showDialog    by viewModel.showUpgradeDialog.collectAsState()
    val showUpiSheet  by viewModel.showUpiSheet.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope      = rememberCoroutineScope()

    // Navigate on success — now means "request submitted" not "activated"
    LaunchedEffect(purchaseState) {
        if (purchaseState is PurchaseState.Success) {
            // Don't pop screen; show success inline. If needed:
            // onUpgradeSuccess()
            // viewModel.resetPurchaseState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {
        // Ambient background glow
        val transition = rememberInfiniteTransition(label = "premiumBg")
        val glowAlpha by transition.animateFloat(
            initialValue  = 0.04f,
            targetValue   = 0.10f,
            animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Reverse),
            label         = "premiumBgGlow"
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(PremiumGold.copy(glowAlpha), Color.Transparent),
                        center = Offset(Float.POSITIVE_INFINITY / 2, 0f),
                        radius = 600f
                    )
                )
        )

        LazyColumn(
            modifier            = Modifier.fillMaxSize(),
            contentPadding      = PaddingValues(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── Top bar ─────────────────────────────────────────────────────────
            item {
                Row(
                    modifier          = Modifier.fillMaxWidth().padding(top = 52.dp, start = 8.dp, end = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, null, tint = TextSecondary)
                    }
                    Text(
                        "Premium",
                        style = TextStyle(
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            brush      = Brush.linearGradient(listOf(PremiumGold, PremiumViolet))
                        )
                    )
                }
            }

            // ── Hero section ────────────────────────────────────────────────────
            item {
                PremiumHeroSection(
                    membership = membership,
                    modifier   = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // ── Current membership (if active) ──────────────────────────────────
            if (membership.isActive) {
                item {
                    CurrentMembershipCard(
                        membership    = membership,
                        onManageClick = {},
                        modifier      = Modifier.padding(horizontal = 20.dp).padding(bottom = 16.dp)
                    )
                }
            }

            // ── Success state — pending review card ──────────────────────────────
            if (purchaseState is PurchaseState.Success) {
                item {
                    PendingReviewCard(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        onDismiss = { viewModel.resetPurchaseState() }
                    )
                }
            }

            // ── Benefits section ────────────────────────────────────────────────
            item {
                BenefitsSection(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // ── Plans heading ───────────────────────────────────────────────────
            if (!(purchaseState is PurchaseState.Success)) {
                item {
                    Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                        Text(
                            "Choose Your Plan",
                            color      = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 20.sp
                        )
                        Text("Pay once · Admin verifies · Instant unlock", color = TextMuted, fontSize = 12.sp)
                    }
                }

                // ── Plan cards ──────────────────────────────────────────────────
                items(premiumPlans) { plan ->
                    MembershipPlanCard(
                        plan       = plan,
                        isSelected = selectedPlan?.type == plan.type,
                        onClick    = { viewModel.selectPlan(plan) },
                        modifier   = Modifier
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 12.dp)
                    )
                }

                // ── Purchase button ─────────────────────────────────────────────
                item {
                    PurchaseButton(
                        plan          = selectedPlan,
                        membership    = membership,
                        purchaseState = purchaseState,
                        onPurchase    = { viewModel.purchase() },
                        modifier      = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }

                // ── Restore purchase ────────────────────────────────────────────
                item {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.restorePurchase() }
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Restore, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        Text(
                            "  Restore Purchase",
                            color    = TextMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            // ── FAQ ─────────────────────────────────────────────────────────────
            item {
                FaqSection(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
            }
        }

        // ── Upgrade Dialog ───────────────────────────────────────────────────────
        if (showDialog) {
            UpgradeDialog(
                onDismiss = { viewModel.dismissUpgradeDialog() },
                onUpgrade = { viewModel.dismissUpgradeDialog(); viewModel.purchase() }
            )
        }
    }

    // ── UPI payment bottom sheet ─────────────────────────────────────────────────
    if (showUpiSheet) {
        UpiPaymentSheet(
            plan       = selectedPlan,
            sheetState = sheetState,
            onSubmit   = { utrId -> viewModel.submitPaymentRequest(utrId) },
            onDismiss  = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    viewModel.dismissUpiSheet()
                }
            }
        )
    }
}

// ── UPI payment bottom sheet ───────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpiPaymentSheet(
    plan: PremiumPlan?,
    sheetState: SheetState,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var utrInput by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = SurfaceDark,
        tonalElevation   = 0.dp,
        shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                "Complete Payment",
                color      = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize   = 18.sp
            )
            if (plan != null) {
                Text(
                    "${plan.type.displayName} · ${plan.price}",
                    color    = PremiumGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // UPI ID box
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("UPI ID", color = TextMuted, fontSize = 12.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, PremiumGold.copy(0.35f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(UPI_ID, color = PremiumGold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick  = { clipboard.setText(AnnotatedString(UPI_ID)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Rounded.ContentCopy, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }
                Text(
                    "Open your UPI app (GPay, PhonePe, Paytm), pay the amount to the above UPI ID, then enter the UTR / Transaction ID below.",
                    color      = TextMuted,
                    fontSize   = 12.sp,
                    lineHeight = 18.sp
                )
            }

            // UTR input
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("UTR / Transaction ID", color = TextMuted, fontSize = 12.sp)
                OutlinedTextField(
                    value         = utrInput,
                    onValueChange = { utrInput = it.trim() },
                    modifier      = Modifier.fillMaxWidth(),
                    placeholder   = { Text("e.g. 425812345678", color = TextMuted, fontSize = 13.sp) },
                    singleLine    = true,
                    shape         = RoundedCornerShape(14.dp),
                    textStyle     = TextStyle(color = TextPrimary, fontSize = 14.sp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = AccentCyan,
                        unfocusedBorderColor = SurfaceElevated,
                        cursorColor          = AccentCyan,
                        focusedContainerColor   = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction      = ImeAction.Done
                    )
                )
            }

            // Submit button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (utrInput.length >= 6)
                            Brush.horizontalGradient(listOf(PremiumGold, PremiumViolet))
                        else
                            Brush.horizontalGradient(listOf(SurfaceElevated, SurfaceElevated))
                    )
                    .then(
                        if (utrInput.length >= 6)
                            Modifier.clickable { onSubmit(utrInput) }
                        else Modifier
                    )
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Submit Payment Request",
                    color      = if (utrInput.length >= 6) BackgroundBlack else TextMuted,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 16.sp
                )
            }

            Text(
                "Our team typically approves within a few hours. You'll be notified once activated.",
                color     = TextMuted,
                fontSize  = 11.sp,
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth()
            )
        }
    }
}

// ── Pending review card ────────────────────────────────────────────────────────
@Composable
private fun PendingReviewCard(modifier: Modifier = Modifier, onDismiss: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .border(1.dp, AccentCyan.copy(0.4f), RoundedCornerShape(20.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Rounded.CheckCircle, null, tint = AccentCyan, modifier = Modifier.size(40.dp))
        Text(
            "Request Submitted!",
            color      = TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            fontSize   = 18.sp
        )
        Text(
            "Pending admin review",
            color    = AccentCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            "Your payment request has been received. Our team will verify and activate your plan within a few hours.",
            color     = TextSecondary,
            fontSize  = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceElevated)
                .clickable(onClick = onDismiss)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text("Dismiss", color = TextSecondary, fontSize = 13.sp)
        }
    }
}

// ── Hero section ───────────────────────────────────────────────────────────────
@Composable
private fun PremiumHeroSection(membership: PremiumMembership, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "hero")
    val crownScale by transition.animateFloat(
        initialValue  = 0.92f,
        targetValue   = 1.05f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "heroScale"
    )

    Column(
        modifier            = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    Brush.radialGradient(colors = listOf(PremiumGold.copy(0.15f), Color.Transparent)),
                    CircleShape
                )
                .border(2.dp, Brush.linearGradient(listOf(PremiumGold.copy(0.6f), PremiumViolet.copy(0.4f))), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("👑", fontSize = 52.sp, modifier = Modifier.padding(top = 4.dp))
        }

        if (!membership.isActive) {
            Text(
                "Unlock Everything",
                style = TextStyle(
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    brush      = Brush.horizontalGradient(listOf(PremiumGold, PremiumViolet))
                ),
                textAlign = TextAlign.Center
            )
            Text(
                "Join thousands of toppers who learn faster with premium",
                color     = TextSecondary,
                fontSize  = 14.sp,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                "You're Premium 🎉",
                style = TextStyle(
                    fontSize   = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    brush      = Brush.horizontalGradient(listOf(PremiumGold, PremiumViolet))
                ),
                textAlign = TextAlign.Center
            )
            Text(
                "All features unlocked — keep topping!",
                color     = TextSecondary,
                fontSize  = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Benefits section ───────────────────────────────────────────────────────────
@Composable
private fun BenefitsSection(modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .border(1.dp, PremiumGold.copy(0.2f), RoundedCornerShape(20.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Premium Benefits", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
        val benefits = listOf(
            Triple("📚", "Unlimited Notes", "Access all premium study notes instantly"),
            Triple("🎯", "Exclusive Quizzes", "Practise with chapter-wise advanced tests"),
            Triple("📥", "Priority Downloads", "Faster PDF downloads with no limits"),
            Triple("🏆", "Leaderboard Badge", "Stand out with a gold VIP rank badge"),
            Triple("🚫", "Ad-Free Experience", "Zero ads — zero distractions"),
            Triple("⚡", "Early Access", "Get new resources before everyone else")
        )
        benefits.forEach { (emoji, title, subtitle) ->
            PremiumBenefitItem(emoji = emoji, title = title, subtitle = subtitle)
        }
    }
}

// ── Purchase button ────────────────────────────────────────────────────────────
@Composable
private fun PurchaseButton(
    plan: PremiumPlan?,
    membership: PremiumMembership,
    purchaseState: PurchaseState,
    onPurchase: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isProcessing    = purchaseState is PurchaseState.Processing
    val isAlreadyActive = membership.isActive && membership.type == plan?.type

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    if (!isProcessing && !isAlreadyActive)
                        Brush.horizontalGradient(listOf(PremiumGold, PremiumViolet))
                    else
                        Brush.horizontalGradient(listOf(SurfaceElevated, SurfaceElevated))
                )
                .then(if (!isProcessing && !isAlreadyActive) Modifier.clickable(onClick = onPurchase) else Modifier)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                isProcessing -> CircularProgressIndicator(
                    modifier    = Modifier.size(24.dp),
                    color       = PremiumGold,
                    strokeWidth = 2.dp
                )
                isAlreadyActive -> Text(
                    "Current Plan ✓",
                    color      = TextSecondary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 17.sp
                )
                plan != null -> Text(
                    "Pay via UPI · ${plan.price}",
                    color      = BackgroundBlack,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 17.sp
                )
                else -> Text(
                    "Select a Plan Above",
                    color      = TextMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 17.sp
                )
            }
        }

        if (purchaseState is PurchaseState.Error) {
            Text(
                (purchaseState as PurchaseState.Error).message,
                color     = NeonCyan.copy(0.8f),
                fontSize  = 12.sp,
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth()
            )
        }

        Text(
            "🔒 Secure UPI payment · Admin verification · Instant unlock after approval",
            color     = TextMuted,
            fontSize  = 11.sp,
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth()
        )
    }
}

// ── FAQ section ────────────────────────────────────────────────────────────────
@Composable
private fun FaqSection(modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .border(1.dp, SurfaceElevated, RoundedCornerShape(20.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("FAQs", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
        val faqs = listOf(
            "How do I pay?" to "Tap 'Pay via UPI', send the amount to our UPI ID using any app (GPay, PhonePe, Paytm), then enter your UTR/Transaction ID.",
            "How long does activation take?" to "Our team typically approves within a few hours of receiving your request.",
            "Can I cancel anytime?" to "Yes — your plan will simply expire at the end of the period. Contact support for early refunds.",
            "How do I restore my purchase?" to "Tap 'Restore Purchase' above. Your subscription will be synced immediately.",
            "What if my UTR is wrong?" to "Contact support with the correct UTR and your payment screenshot for manual review."
        )
        faqs.forEach { (q, a) ->
            FaqItem(question = q, answer = a)
        }
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Q: $question", color = PremiumGold, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Text("A: $answer", color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
    }
}
