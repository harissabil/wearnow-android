package id.harissabil.wearnow.ui.screen.result

import android.net.Uri
import com.amplifyframework.datastore.generated.model.TryOnHistory

data class ResultUiState(
    val isLoading: Boolean = true,
    val tryOnHistory: TryOnHistory? = null,
    val userPhotoUri: Uri? = null,
    val garmentPhotoUri: Uri? = null,
    val resultPhotoUri: Uri? = null,
    val errorMessage: String? = null,
    val isDownloading: Boolean = false,
    val downloadProgress: String = "",
    val showShareDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
)