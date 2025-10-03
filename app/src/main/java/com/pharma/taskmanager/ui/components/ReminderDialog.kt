package com.pharma.taskmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pharma.taskmanager.data.database.TaskConstants
import com.pharma.taskmanager.data.database.TaskEntity
import com.pharma.taskmanager.ui.theme.*

@Composable
fun ReminderDialog(
    task: TaskEntity,
    onDismiss: () -> Unit,
    onComplete: () -> Unit,
    onSnooze: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                when (task.priority) {
                                    TaskConstants.PRIORITY_HIGH -> HighPriorityColor.copy(alpha = 0.1f)
                                    TaskConstants.PRIORITY_MEDIUM -> MediumPriorityColor.copy(alpha = 0.1f)
                                    else -> LowPriorityColor.copy(alpha = 0.1f)
                                },
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Priority Icon and Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Reminder",
                            modifier = Modifier.size(32.dp),
                            tint = when (task.priority) {
                                TaskConstants.PRIORITY_HIGH -> HighPriorityColor
                                TaskConstants.PRIORITY_MEDIUM -> MediumPriorityColor
                                else -> LowPriorityColor
                            }
                        )
                        
                        Column {
                            Text(
                                text = "📋 Task Reminder",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Text(
                                text = when (task.priority) {
                                    TaskConstants.PRIORITY_HIGH -> "🔴 High Priority"
                                    TaskConstants.PRIORITY_MEDIUM -> "🟡 Medium Priority"
                                    else -> "🟢 Low Priority"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = when (task.priority) {
                                    TaskConstants.PRIORITY_HIGH -> HighPriorityColor
                                    TaskConstants.PRIORITY_MEDIUM -> MediumPriorityColor
                                    else -> LowPriorityColor
                                }
                            )
                        }
                    }
                    
                    Divider(
                        color = when (task.priority) {
                            TaskConstants.PRIORITY_HIGH -> HighPriorityColor.copy(alpha = 0.3f)
                            TaskConstants.PRIORITY_MEDIUM -> MediumPriorityColor.copy(alpha = 0.3f)
                            else -> LowPriorityColor.copy(alpha = 0.3f)
                        },
                        thickness = 2.dp
                    )
                    
                    // Task Details
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            if (!task.description.isNullOrBlank()) {
                                Text(
                                    text = task.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Due Date",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Due: ${if (task.dueDateTime != null) "Today" else "No due date"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSnooze,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF00D4FF)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "⏰ Snooze",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Button(
                            onClick = onComplete,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (task.priority) {
                                    TaskConstants.PRIORITY_HIGH -> HighPriorityColor
                                    TaskConstants.PRIORITY_MEDIUM -> MediumPriorityColor
                                    else -> LowPriorityColor
                                }
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "✅ Complete",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Maybe Later",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}