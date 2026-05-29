package com.nexttoppers.feed.ui.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.PremiumMembership
import com.nexttoppers.feed.data.model.PremiumPlan
import com.nexttoppers.feed.data.model.premiumPlans
import com.nexttoppers.feed.data.repository.AuthRepository
import com.nexttoppers.feed.data.repository.PremiumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    private val premiumRepository: PremiumRepository
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

    private val uid get() = authRepository.currentUser?.uid ?: ""

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

    fun purchase() {
        val plan = _selectedPlan.value ?: return
        if (uid.isEmpty()) return
        viewModelScope.launch {
            _purchaseState.value = PurchaseState.Processing
            // Placeholder: simulate payment gateway delay
            delay(1800L)
            premiumRepository.activatePremium(uid, plan)
                .onSuccess { _purchaseState.value = PurchaseState.Success }
                .onFailure { _purchaseState.value = PurchaseState.Error(it.message ?: "Purchase failed") }
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
