package com.nexttoppers.feed.ui.resources

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.BreadcrumbItem
import com.nexttoppers.feed.data.model.FolderDisplayItem
import com.nexttoppers.feed.data.model.Resource
import com.nexttoppers.feed.data.model.ResourceFolder
import com.nexttoppers.feed.data.model.ResourceType
import com.nexttoppers.feed.data.repository.FolderRepository
import com.nexttoppers.feed.data.repository.ResourcesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOrder(val label: String) {
    LATEST("Latest"),
    OLDEST("Oldest"),
    MOST_VIEWED("Most Viewed")
}

sealed class SubjectResourcesUiState {
    object Loading : SubjectResourcesUiState()
    object Empty : SubjectResourcesUiState()
    data class Success(val items: List<Resource>) : SubjectResourcesUiState()
    data class Error(val message: String) : SubjectResourcesUiState()
}

@HiltViewModel
class SubjectResourcesViewModel @Inject constructor(
    private val repository: ResourcesRepository,
    private val folderRepository: FolderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val subject: String = savedStateHandle.get<String>("subject") ?: ""

    private val _uiState = MutableStateFlow<SubjectResourcesUiState>(SubjectResourcesUiState.Loading)
    val uiState: StateFlow<SubjectResourcesUiState> = _uiState

    private val _selectedType = MutableStateFlow<ResourceType?>(null)
    val selectedType: StateFlow<ResourceType?> = _selectedType

    private val _premiumFilter = MutableStateFlow<Boolean?>(null)
    val premiumFilter: StateFlow<Boolean?> = _premiumFilter

    private val _sortOrder = MutableStateFlow(SortOrder.LATEST)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView

    // ── Folder display state ───────────────────────────────────────────────────

    private val _displayFolders = MutableStateFlow<List<FolderDisplayItem>>(emptyList())
    val displayFolders: StateFlow<List<FolderDisplayItem>> = _displayFolders

    /** True when we are showing a resource list inside a folder (not the folder grid) */
    private val _isInFolder = MutableStateFlow(false)
    val isInFolder: StateFlow<Boolean> = _isInFolder

    private val _breadcrumbs = MutableStateFlow<List<BreadcrumbItem>>(emptyList())
    val breadcrumbs: StateFlow<List<BreadcrumbItem>> = _breadcrumbs

    private val _currentFolderName = MutableStateFlow<String?>(null)
    val currentFolderName: StateFlow<String?> = _currentFolderName

    // ── Legacy compat for FilterBottomSheet ────────────────────────────────────

    private val _selectedFolder = MutableStateFlow<ResourceType?>(null)
    val selectedFolder: StateFlow<ResourceType?> = _selectedFolder

    private val _availableFolders = MutableStateFlow<List<Pair<ResourceType, Int>>>(emptyList())
    val availableFolders: StateFlow<List<Pair<ResourceType, Int>>> = _availableFolders

    // ── Internal data ──────────────────────────────────────────────────────────

    /** All resources for this subject (realtime from Firestore) */
    private var allItems: List<Resource> = emptyList()

    /** Firestore folders for this subject */
    private var allFolders: List<ResourceFolder> = emptyList()

    /** Whether the subject has real Firestore folders */
    private var useRealFolders = false

    // ── Navigation scope — exactly ONE of these is set when isInFolder = true ─

    /** When set: show items where folderId == this value */
    private var currentRealFolderId: String? = null

    /** When set: show items of this type */
    private var currentTypeFolder: ResourceType? = null

    /** When true AND currentTypeFolder is set: show only items where folderId.isBlank() */
    private var showOnlyUnfiled = false

    val hasActiveFilters: Boolean
        get() = _selectedType.value != null ||
                _premiumFilter.value != null ||
                _sortOrder.value != SortOrder.LATEST

    init { loadAll() }

    // ── Loading ────────────────────────────────────────────────────────────────

    private fun loadAll() {
        _uiState.value = SubjectResourcesUiState.Loading
        viewModelScope.launch {
            allFolders = folderRepository.getFoldersForSubject(subject)
            useRealFolders = allFolders.isNotEmpty()

            repository.observeBySubject(subject).collect { result ->
                result
                    .onSuccess { items ->
                        allItems = items
                        if (_isInFolder.value) {
                            // Refresh resource list in the current scope
                            applyFilters()
                        } else {
                            // Rebuild folder grid counts
                            if (useRealFolders) buildRealFolderGrid(currentRealFolderId)
                            else updateTypeFolders()
                        }
                    }
                    .onFailure { err ->
                        _uiState.value = SubjectResourcesUiState.Error(err.message ?: "Failed")
                    }
            }
        }
    }

    // ── Scope accessor ─────────────────────────────────────────────────────────

    /**
     * Returns the items relevant to the current navigation scope:
     * - In a real folder    → items where folderId == currentRealFolderId
     * - In an unfiled group → items where folderId.isBlank() && type matches
     * - In a type folder    → items where type matches (all, not just unfiled)
     * - At root             → all items (only used for folder grid counts, not shown as list)
     */
    private fun scopedItems(): List<Resource> = when {
        currentRealFolderId != null -> allItems.filter { it.folderId == currentRealFolderId }

        currentTypeFolder != null -> {
            val byType = allItems.filter {
                it.type.equals(currentTypeFolder!!.name, ignoreCase = true)
            }
            if (showOnlyUnfiled) byType.filter { it.folderId.isBlank() } else byType
        }

        else -> allItems
    }

    // ── Real folder grid ───────────────────────────────────────────────────────

    private fun buildRealFolderGrid(parentFolderId: String?) {
        val childFolders = if (parentFolderId == null)
            allFolders.filter { it.parentId.isBlank() }
        else
            allFolders.filter { it.parentId == parentFolderId }

        val folderItems = childFolders.sortedBy { it.order }.map { folder ->
            val resources     = allItems.filter { it.folderId == folder.id }
            val subFolderCount = allFolders.count { it.parentId == folder.id }
            FolderDisplayItem(
                id               = folder.id,
                name             = folder.name,
                emoji            = folder.iconEmoji,
                fileCount        = resources.count { !it.isLecture() },
                lectureCount     = resources.count { it.isLecture() },
                childFolderCount = subFolderCount,
                isRealFolder     = true
            )
        }

        // If at root, also show type-grouped virtual folders for unfiled resources
        val virtualItems: List<FolderDisplayItem> = if (parentFolderId == null) {
            allItems
                .filter { it.folderId.isBlank() }
                .groupBy { it.type.uppercase() }
                .mapNotNull { (typeName, typeItems) ->
                    val type = ResourceType.values()
                        .firstOrNull { it.name.equals(typeName, ignoreCase = true) }
                        ?: return@mapNotNull null
                    FolderDisplayItem(
                        id           = "UNFILED_${type.name}",
                        name         = "${type.displayName} (Unfiled)",
                        emoji        = type.emoji,
                        fileCount    = typeItems.count { !it.isLecture() },
                        lectureCount = typeItems.count { it.isLecture() },
                        isRealFolder = false,
                        typeFolder   = type
                    )
                }
                .sortedBy { it.name }
        } else emptyList()

        _displayFolders.value = folderItems + virtualItems

        // Update _uiState so the screen exits Loading when we are in folder-grid mode.
        // Without this, an empty displayFolders list would show skeleton cards forever.
        if (_uiState.value is SubjectResourcesUiState.Loading) {
            _uiState.value = if ((folderItems + virtualItems).isEmpty())
                SubjectResourcesUiState.Empty
            else
                SubjectResourcesUiState.Success(allItems)
        }
    }

    // ── Type-based fallback ────────────────────────────────────────────────────

    private fun updateTypeFolders() {
        val grouped = allItems
            .groupBy { it.type.uppercase() }
            .mapNotNull { (typeName, items) ->
                val type = ResourceType.values()
                    .firstOrNull { it.name.equals(typeName, ignoreCase = true) }
                type?.to(items.size)
            }
            .sortedBy { (type, _) ->
                listOf(
                    ResourceType.LECTURE, ResourceType.DPP, ResourceType.MODULE,
                    ResourceType.NOTES, ResourceType.ACP, ResourceType.PDF,
                    ResourceType.PRACTICE
                ).indexOf(type).let { if (it < 0) Int.MAX_VALUE else it }
            }
        _availableFolders.value = grouped
        _displayFolders.value = grouped.map { (type, count) ->
            FolderDisplayItem(
                id           = "TYPE_${type.name}",
                name         = type.displayName,
                emoji        = type.emoji,
                fileCount    = count,
                lectureCount = 0,
                isRealFolder = false,
                typeFolder   = type
            )
        }

        // Update _uiState so the screen exits Loading when we are in folder-grid mode.
        if (_uiState.value is SubjectResourcesUiState.Loading) {
            _uiState.value = if (grouped.isEmpty())
                SubjectResourcesUiState.Empty
            else
                SubjectResourcesUiState.Success(allItems)
        }
    }

    // ── Navigation ─────────────────────────────────────────────────────────────

    fun openFolder(item: FolderDisplayItem) {
        when {
            item.isRealFolder        -> openRealFolder(item.id, item.name)
            item.id.startsWith("UNFILED_") && item.typeFolder != null
                                     -> openUnfiledTypeGroup(item.typeFolder!!)
            item.typeFolder != null  -> openTypeFolder(item.typeFolder!!)
        }
    }

    /** Real Firestore folder — may have subfolders or leaf resources */
    private fun openRealFolder(folderId: String, folderName: String) {
        val hasSubfolders = allFolders.any { it.parentId == folderId }

        if (hasSubfolders) {
            // Show subfolder grid — not resource list
            currentRealFolderId = folderId
            _isInFolder.value   = false
            updateBreadcrumb(folderId, folderName)
            buildRealFolderGrid(folderId)
        } else {
            // Leaf folder — show resource list scoped to this folder
            currentRealFolderId  = folderId
            currentTypeFolder    = null
            showOnlyUnfiled      = false
            _isInFolder.value    = true
            _currentFolderName.value = folderName
            _selectedFolder.value    = null
            _selectedType.value      = null
            updateBreadcrumb(folderId, folderName)
            applyFilters()
        }
    }

    /** Unfiled virtual group — resources of this type with folderId.isBlank() */
    private fun openUnfiledTypeGroup(type: ResourceType) {
        currentRealFolderId      = null
        currentTypeFolder        = type
        showOnlyUnfiled          = true
        _isInFolder.value        = true
        _selectedFolder.value    = type
        _selectedType.value      = null      // additional filters start clear
        _currentFolderName.value = "${type.displayName} (Unfiled)"
        val subjectName = subject.replaceFirstChar { it.uppercaseChar() }
        _breadcrumbs.value = listOf(
            BreadcrumbItem(null, subjectName),
            BreadcrumbItem("UNFILED_${type.name}", "${type.displayName} (Unfiled)")
        )
        applyFilters()
    }

    /** Type-based folder (fallback when no real folders exist) */
    fun openTypeFolder(type: ResourceType) {
        currentRealFolderId      = null
        currentTypeFolder        = type
        showOnlyUnfiled          = false
        _isInFolder.value        = true
        _selectedFolder.value    = type
        _selectedType.value      = null
        _currentFolderName.value = type.displayName
        val subjectName = subject.replaceFirstChar { it.uppercaseChar() }
        _breadcrumbs.value = listOf(
            BreadcrumbItem(null, subjectName),
            BreadcrumbItem("TYPE_${type.name}", type.displayName)
        )
        applyFilters()
    }

    private fun updateBreadcrumb(folderId: String, folderName: String) {
        val crumbs = mutableListOf<BreadcrumbItem>()
        crumbs.add(BreadcrumbItem(null, subject.replaceFirstChar { it.uppercaseChar() }))
        // Walk the folder tree upward
        var current: String? = folderId
        val path = mutableListOf<BreadcrumbItem>()
        while (current != null) {
            val folder = allFolders.firstOrNull { it.id == current }
            if (folder != null) {
                path.add(0, BreadcrumbItem(folder.id, folder.name))
                current = folder.parentId.takeIf { it.isNotBlank() }
            } else break
        }
        _breadcrumbs.value = crumbs + path
    }

    /** Navigate to a breadcrumb item */
    fun navigateToBreadcrumb(item: BreadcrumbItem) {
        when {
            item.id == null -> closeFolder()   // back to subject root

            item.id.startsWith("UNFILED_") -> {
                val type = ResourceType.values()
                    .firstOrNull { "UNFILED_${it.name}" == item.id } ?: return
                openUnfiledTypeGroup(type)
            }

            item.id.startsWith("TYPE_") -> {
                val type = ResourceType.values()
                    .firstOrNull { "TYPE_${it.name}" == item.id } ?: return
                openTypeFolder(type)
            }

            else -> {
                val folder = allFolders.firstOrNull { it.id == item.id } ?: return
                openRealFolder(folder.id, folder.name)
            }
        }
    }

    /** Go back — close folder and return to parent or subject root */
    fun closeFolder() {
        val crumbs = _breadcrumbs.value
        if (crumbs.size > 2) {
            navigateToBreadcrumb(crumbs[crumbs.size - 2])
            return
        }
        // Return to subject root
        currentRealFolderId      = null
        currentTypeFolder        = null
        showOnlyUnfiled          = false
        _isInFolder.value        = false
        _currentFolderName.value = null
        _selectedFolder.value    = null
        _selectedType.value      = null
        _premiumFilter.value     = null
        _sortOrder.value         = SortOrder.LATEST
        _breadcrumbs.value       = emptyList()
        if (useRealFolders) buildRealFolderGrid(null) else updateTypeFolders()
    }

    // ── Filters ────────────────────────────────────────────────────────────────

    fun selectType(type: ResourceType?) {
        _selectedType.value = type
        applyFilters()
    }

    fun setPremiumFilter(premium: Boolean?) {
        _premiumFilter.value = premium
        applyFilters()
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
        applyFilters()
    }

    fun clearAllFilters() {
        _selectedType.value  = null
        _premiumFilter.value = null
        _sortOrder.value     = SortOrder.LATEST
        applyFilters()
    }

    fun toggleViewMode() { _isGridView.value = !_isGridView.value }

    fun refresh() {
        currentRealFolderId  = null
        currentTypeFolder    = null
        showOnlyUnfiled      = false
        allFolders           = emptyList()
        allItems             = emptyList()
        _isInFolder.value    = false
        _breadcrumbs.value   = emptyList()
        _currentFolderName.value = null
        loadAll()
    }

    /** Apply secondary filters (type, premium, sort) to the current navigation scope */
    private fun applyFilters() {
        val base = scopedItems()

        // If we're in a type folder (including unfiled), skip the extra type filter
        // since the scope already constrains by type.
        var filtered = base
        if (currentRealFolderId != null) {
            // In a real folder — let user apply type sub-filter
            _selectedType.value?.let { type ->
                filtered = filtered.filter {
                    it.type.equals(type.name, ignoreCase = true)
                }
            }
        }
        _premiumFilter.value?.let { isPremium ->
            filtered = filtered.filter { it.premium == isPremium }
        }
        filtered = when (_sortOrder.value) {
            SortOrder.LATEST      -> filtered.sortedByDescending { it.createdAt.seconds }
            SortOrder.OLDEST      -> filtered.sortedBy { it.createdAt.seconds }
            SortOrder.MOST_VIEWED -> filtered.sortedByDescending { it.views }
        }
        _uiState.value = if (filtered.isEmpty()) SubjectResourcesUiState.Empty
        else SubjectResourcesUiState.Success(filtered)
    }

    // ── Legacy — used by FilterBottomSheet ─────────────────────────────────────

    @Deprecated("Use openFolder(FolderDisplayItem) instead")
    fun openFolder(type: ResourceType) { openTypeFolder(type) }
}
