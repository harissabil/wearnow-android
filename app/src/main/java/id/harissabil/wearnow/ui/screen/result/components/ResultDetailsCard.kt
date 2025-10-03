package id.harissabil.wearnow.ui.screen.result.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amplifyframework.datastore.generated.model.TryOnHistory
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ResultDetailsCard(
    tryOnHistory: TryOnHistory,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Try-On Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status
            StatusRow(
                status = tryOnHistory.status.name
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Metadata if available
            tryOnHistory.metadata?.let { metadata ->
                // Parse metadata JSON to extract garment class and merge style
                // For now, we'll show placeholder values
                MetadataRow(
                    garmentClass = "Upper Body", // Extract from metadata
                    mergeStyle = "Balanced"      // Extract from metadata
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Processing time
            ProcessingTimeRow(
                createdAt = tryOnHistory.createdAt,
                completedAt = tryOnHistory.completedAt
            )

            // Error message if failed
            if (tryOnHistory.status.name == "FAILED" && !tryOnHistory.errorMessage.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                ErrorMessageCard(errorMessage = tryOnHistory.errorMessage)
            }
        }
    }
}

@Composable
private fun StatusRow(
    status: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (status) {
                "COMPLETED" -> Icons.Default.CheckCircle
                "FAILED" -> Icons.Default.Error
                else -> Icons.Default.AccessTime
            },
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = when (status) {
                "COMPLETED" -> Color(0xFF4CAF50)
                "FAILED" -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.primary
            }
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = "Status",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = when (status) {
                    "COMPLETED" -> "Completed Successfully"
                    "FAILED" -> "Processing Failed"
                    "PROCESSING" -> "Still Processing"
                    else -> "Unknown Status"
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MetadataRow(
    garmentClass: String,
    mergeStyle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Garment Type
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Category,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = "Type",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = garmentClass,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Merge Style
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = "Style",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = mergeStyle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ProcessingTimeRow(
    createdAt: com.amplifyframework.core.model.temporal.Temporal.DateTime?,
    completedAt: com.amplifyframework.core.model.temporal.Temporal.DateTime?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AccessTime,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.tertiary
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = "Processing Time",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val timeText = if (createdAt != null && completedAt != null) {
                try {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    val startTime = dateFormat.parse(createdAt.format())
                    val endTime = dateFormat.parse(completedAt.format())

                    if (startTime != null && endTime != null) {
                        val durationSeconds = (endTime.time - startTime.time) / 1000
                        when {
                            durationSeconds < 60 -> "${durationSeconds}s"
                            durationSeconds < 3600 -> "${durationSeconds / 60}m ${durationSeconds % 60}s"
                            else -> "${durationSeconds / 3600}h ${(durationSeconds % 3600) / 60}m"
                        }
                    } else {
                        "Processing duration unknown"
                    }
                } catch (_: Exception) {
                    "Processing duration unknown"
                }
            } else if (createdAt != null) {
                try {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    val startTime = dateFormat.parse(createdAt.format())
                    if (startTime != null) {
                        val displayFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
                        "Started ${displayFormat.format(startTime)}"
                    } else {
                        "Processing time unknown"
                    }
                } catch (_: Exception) {
                    "Processing time unknown"
                }
            } else {
                "Processing time unknown"
            }

            Text(
                text = timeText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ErrorMessageCard(
    errorMessage: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Error Details",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            )
        }
    }
}
