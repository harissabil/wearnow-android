package id.harissabil.wearnow.ui.screen.home.data

// Data classes for Virtual Try-On Response matching your Lambda interface exactly
data class VirtualTryOnResponse(
    val virtualTryOn: VirtualTryOnResult
)

data class VirtualTryOnResult(
    val success: Boolean,
    val historyId: String,
    val resultUrl: String? = null,
    val errorMessage: String? = null,
    val processingTime: Long? = null // Your Lambda returns number, which maps to Long
)

// Input data class for GraphQL that matches your TryOnRequest interface
data class VirtualTryOnInput(
    val userId: String,
    val userPhotoId: String,
    val userPhotoUrl: String,
    val garmentPhotoUrl: String,
    val historyId: String,
    val options: VirtualTryOnOptions? = null
)

data class VirtualTryOnOptions(
    val garmentClass: String? = null,
    val maskType: String? = null,
    val mergeStyle: String? = null
)
