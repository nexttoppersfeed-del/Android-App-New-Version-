package com.nexttoppers.feed.ui.tests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.NtfTest
import com.nexttoppers.feed.data.model.TestAttempt
import com.nexttoppers.feed.data.repository.AuthRepository
import com.nexttoppers.feed.data.repository.TestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TestsUiState {
    object Loading : TestsUiState()
    object Empty : TestsUiState()
    data class Success(val tests: List<NtfTest>) : TestsUiState()
    data class Error(val message: String) : TestsUiState()
}

@HiltViewModel
class TestsViewModel @Inject constructor(
    private val testRepository: TestRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TestsUiState>(TestsUiState.Loading)
    val uiState: StateFlow<TestsUiState> = _uiState

    private val _subjectFilter = MutableStateFlow<String?>(null)
    val subjectFilter: StateFlow<String?> = _subjectFilter

    private val _attempts = MutableStateFlow<List<TestAttempt>>(emptyList())
    val attempts: StateFlow<List<TestAttempt>> = _attempts

    val currentUid: String get() = authRepository.currentUser?.uid ?: ""

    private var allTests: List<NtfTest> = emptyList()

    init {
        observeTests()
        observeAttempts()
    }

    private fun observeTests() {
        viewModelScope.launch {
            testRepository.observeAllTests().collect { result ->
                result
                    .onSuccess { tests ->
                        allTests = tests
                        applyFilter()
                    }
                    .onFailure { err ->
                        _uiState.value = TestsUiState.Error(err.message ?: "Failed to load tests")
                    }
            }
        }
    }

    private fun observeAttempts() {
        val uid = currentUid
        if (uid.isEmpty()) return
        viewModelScope.launch {
            testRepository.observeUserAttempts(uid).collect { result ->
                result.onSuccess { _attempts.value = it }
            }
        }
    }

    fun setSubjectFilter(subject: String?) {
        _subjectFilter.value = subject
        applyFilter()
    }

    private fun applyFilter() {
        val filtered = _subjectFilter.value
            ?.let { s -> allTests.filter { it.subject.equals(s, ignoreCase = true) } }
            ?: allTests
        _uiState.value = if (filtered.isEmpty()) TestsUiState.Empty
        else TestsUiState.Success(filtered)
    }

    fun getAttemptForTest(testId: String): TestAttempt? =
        _attempts.value.firstOrNull { it.testId == testId }

    fun refresh() {
        _uiState.value = TestsUiState.Loading
        observeTests()
    }
}
