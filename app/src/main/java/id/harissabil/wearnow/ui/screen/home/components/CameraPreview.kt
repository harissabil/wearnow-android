package id.harissabil.wearnow.ui.screen.home.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CameraPreview(
    capturedGarmentUri: Uri?,
    selectedGarmentUri: Uri?,
    onImageCaptured: (Uri) -> Unit,
    onGalleryImageSelected: (Uri) -> Unit,
    onCameraReady: (Boolean) -> Unit,
    onClearCapturedImage: () -> Unit = {},
    onClearSelectedImage: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var preview: Preview? by remember { mutableStateOf(null) }
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }
    var isFrontCamera by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onGalleryImageSelected(it) }
    }

    // Init camera provider once
    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProvider = cameraProviderFuture.get()
    }

    // Rebind camera when switching front/back
    LaunchedEffect(isFrontCamera, cameraProvider) {
        val provider = cameraProvider ?: return@LaunchedEffect

        preview = Preview.Builder().build()
        val capture = ImageCapture.Builder().build()
        imageCapture = capture

        val cameraSelector = if (isFrontCamera) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        try {
            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                capture
            )
            onCameraReady(true)
        } catch (exc: Exception) {
            onCameraReady(false)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Capture Garment",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = { isFrontCamera = !isFrontCamera }
                ) {
                    Icon(
                        imageVector = Icons.Default.FlipCameraAndroid,
                        contentDescription = "Flip camera",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Camera preview or captured image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                when {
                    capturedGarmentUri != null || selectedGarmentUri != null -> {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(capturedGarmentUri ?: selectedGarmentUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Captured garment",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f))
                                .clickable {
                                    if (capturedGarmentUri != null) onClearCapturedImage()
                                    if (selectedGarmentUri != null) onClearSelectedImage()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tap to retake",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    else -> {
                        AndroidView(
                            factory = { ctx ->
                                PreviewView(ctx).apply {
                                    scaleType = PreviewView.ScaleType.FILL_CENTER
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                            update = { previewView ->
                                preview?.setSurfaceProvider(previewView.surfaceProvider)
                            }
                        )
                    }
                }
            }

            // Camera controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery button
                Card(
                    modifier = Modifier
                        .size(56.dp)
                        .clickable { galleryLauncher.launch("image/*") },
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Gallery",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Capture button
                Card(
                    modifier = Modifier
                        .size(72.dp)
                        .clickable {
                            if (capturedGarmentUri != null || selectedGarmentUri != null) {
                                if (capturedGarmentUri != null) onClearCapturedImage()
                                if (selectedGarmentUri != null) onClearSelectedImage()
                                return@clickable
                            }

                            imageCapture?.let { capture ->
                                val photoFile = File(
                                    context.cacheDir,
                                    "garment_${
                                        SimpleDateFormat(
                                            "yyyyMMdd_HHmmss",
                                            Locale.getDefault()
                                        ).format(Date())
                                    }.jpg"
                                )

                                val outputFileOptions =
                                    ImageCapture.OutputFileOptions.Builder(photoFile).build()

                                capture.takePicture(
                                    outputFileOptions,
                                    ContextCompat.getMainExecutor(context),
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                            onImageCaptured(Uri.fromFile(photoFile))
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            // Handle error
                                        }
                                    }
                                )
                            }
                        },
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = if (capturedGarmentUri != null || selectedGarmentUri != null) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    border = BorderStroke(2.dp, Color.White)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (capturedGarmentUri != null || selectedGarmentUri != null) {
                                Icons.Default.Refresh
                            } else {
                                Icons.Default.CameraAlt
                            },
                            contentDescription = if (capturedGarmentUri != null || selectedGarmentUri != null) {
                                "Retake"
                            } else {
                                "Capture"
                            },
                            tint = if (capturedGarmentUri != null || selectedGarmentUri != null) {
                                MaterialTheme.colorScheme.onErrorContainer
                            } else {
                                Color.White
                            },
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(56.dp))
            }
        }
    }
}

