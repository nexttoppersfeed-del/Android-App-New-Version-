package com.nexttoppers.feed.ui.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.PremiumMembership
import com.nexttoppers.feed.data.model.PremiumPlan
import com.nexttoppers.feed.data.model.premiumPlans
import com.nexttoppers.feed.data.repository.AuthRepository
import com.nexttoppers.feed.data.repository.PremiumRepository
import com.nexttoppers.feed.data.repository.PremiumRequestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PurchaseState {
    object Idle       : PurchaseState()
    object Processing : PurchaseState()
    object Success    : PurchaseState()
    data class Error(val message: String) : PurchaseState()
}

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val premiumRepository: PremiumRepository,
    private val requestRepository: PremiumRequestRepository
) : ViewModel() {

    private val _membership = MutableStateFlow(PremiumMembership())
    val membership: StateFlow<PremiumMembership> = _membership

    private val _selectedPlan = MutableStateFlow<PremiumPlan?>(
        premiumPlans.firstOrNull { it.isRecommended }
    )
    val selectedPlan: StateFlow<PremiumPlan?> = _selectedPlan

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState

    private val _showUpgradeDialog = MutableStateFlow(false)
    val showUpgradeDialog: StateFlow<Boolean> = _showUpgradeDialog

    // UPI payment sheet state
    private val _showUpiSheet = MutableStateFlow(false)
    val showUpiSheet: StateFlow<Boolean> = _showUpiSheet

    private val uid get() = authRepository.currentUser?.uid ?: ""
    private val userName get() = authRepository.currentUser?.displayName ?: "User"
    private val userEmail get() = authRepository.currentUser?.email ?: ""

    init {
        observeMembership()
        checkExpiry()
    }

    private fun observeMembership() {
        if (uid.isEmpty()) return
        viewModelScope.launch {
            premiumRepository.observePremiumStatus(uid).collect { result ->
                result.onSuccess { _membership.value = it }
            }
        }
    }

    private fun checkExpiry() {
        if (uid.isEmpty()) return
        viewModelScope.launch { premiumRepository.checkAndRefreshExpiry(uid) }
    }

    fun selectPlan(plan: PremiumPlan) {
        _selectedPlan.value = plan
    }

    fun showUpgradePrompt() {
        _showUpgradeDialog.value = true
    }

    fun dismissUpgradeDialog() {
        _showUpgradeDialog.value = false
    }

    // Opens the UPI payment bottom sheet instead of immediately processing
    fun purchase() {
        if (_selectedPlan.value == null) return
        if (uid.isEmpty()) return
        _showUpiSheet.value = true
    }

    fun dismissUpiSheet() {
        _showUpiSheet.value = false
    }

    // Called when user submits the UTR/transaction ID from the UPI sheet
    fun submitPaymentRequest(utrId: String) {
        val plan = _selectedPlan.value ?: return
        if (uid.isEmpty()) return
        if (utrId.isBlank()) {
            _purchaseState.value = PurchaseState.Error("Please enter a valid UTR / transaction ID")
            return
        }
        _showUpiSheet.value = false
        viewModelScope.launch {
            _purchaseState.value = PurchaseState.Processing
            requestRepository.submitRequest(
                userId   = uid,
                username = userName,
                userEmail = userEmail,
                plan     = plan.type.name,
                amount   = plan.price,
                utrId    = utrId
            )
                .onSuccess { _purchaseState.value = PurchaseState.Success }
                .onFailure { _purchaseState.value = PurchaseState.Error(it.message ?: "Submission failed") }
        }
    }

    fun resetPurchaseState() {
        _purchaseState.value = PurchaseState.Idle
    }

    fun restorePurchase() {
        if (uid.isEmpty()) return
        viewModelScope.launch {
            _purchaseState.value = PurchaseState.Processing
            premiumRepository.restorePurchase(uid)
                .onSuccess { m ->
                    _membership.value = m
                    _purchaseState.value = PurchaseState.Idle
                }
                .onFailure { _purchaseState.value = PurchaseState.Error(it.message ?: "Restore failed") }
        }
    }
}
