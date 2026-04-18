package com.example.myapplication.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.myapplication.data.airbuddy.model.AvatarStage
import com.example.myapplication.data.airbuddy.stageDisplayName
import com.example.myapplication.data.profile.UserProfile
import java.io.File

private val screenBg = Color(0xFFFAFAF7)
private val primaryText = Color(0xFF2E4A32)
private val mutedText = Color(0xFF8B9590)
private val cardBg = Color(0xFFFFFFFF)
private val cardBorder = Color(0xFFEEEEEE)
private val accentGreen = Color(0xFF2E7D32)
private val accentGreenLight = Color(0xFF43A047)
private val lightGreenBg = Color(0xFFE8F5E9)
private val logoutRed = Color(0xFFC62828)

private data class TreeAvatarOption(
    val id: Int,
    val emoji: String,
    val label: String,
    val bgColor: Color
)

private val treeAvatarOptions = listOf(
    TreeAvatarOption(0, "\uD83C\uDF33", "Oak Tree", Color(0xFFE8F5E9)),
    TreeAvatarOption(1, "\uD83C\uDF32", "Pine Tree", Color(0xFFE0F2F1)),
    TreeAvatarOption(2, "\uD83C\uDF38", "Cherry Blossom", Color(0xFFFCE4EC)),
    TreeAvatarOption(3, "\uD83C\uDF34", "Palm Tree", Color(0xFFFFF8E1)),
    TreeAvatarOption(4, "\uD83C\uDF8B", "Bonsai", Color(0xFFF1F8E9)),
    TreeAvatarOption(5, "\uD83C\uDF31", "Sapling", Color(0xFFE8F5E9))
)

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val selectedAvatarId by viewModel.selectedAvatarId.collectAsState()
    val customAvatarPath by viewModel.customAvatarPath.collectAsState()
    val userName by viewModel.userName.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setCustomAvatar(uri)
        }
    }

    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize().background(screenBg), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = accentGreen)
            }
        }
        state.error != null -> {
            ErrorContent(message = state.error!!)
        }
        state.profile != null -> {
            ProfileContent(
                state = state,
                profile = state.profile!!,
                selectedAvatarId = selectedAvatarId,
                customAvatarPath = customAvatarPath,
                userName = userName,
                onAvatarSelected = { viewModel.selectAvatar(it) },
                onUploadImage = { imagePickerLauncher.launch("image/*") },
                onUsernameChanged = { viewModel.updateUsername(it) },
                onLogout = onLogout
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    state: ProfileUiState,
    profile: UserProfile,
    selectedAvatarId: Int,
    customAvatarPath: String?,
    userName: String,
    onAvatarSelected: (Int) -> Unit,
    onUploadImage: () -> Unit,
    onUsernameChanged: (String) -> Unit,
    onLogout: () -> Unit
) {
    var showAvatarPicker by remember { mutableStateOf(false) }
    var showUsernameDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // ── B) Hero avatar ──
        ProfileAvatar(
            selectedAvatarId = selectedAvatarId,
            customAvatarPath = customAvatarPath,
            onChangeAvatar = { showAvatarPicker = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── C) Username + email ──
        Text(
            text = userName.ifEmpty { "User" },
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = primaryText
        )
        Spacer(modifier = Modifier.height(2.dp))
        if (profile.email.isNotEmpty()) {
            Text(
                text = profile.email,
                fontSize = 13.sp,
                color = mutedText
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── D) YOUR PROGRESS card ──
        SectionLabel("YOUR PROGRESS")
        ProgressCard(
            xp = state.xp,
            stage = state.avatarStage,
            loginStreak = state.loginStreak,
            quizzesCompleted = state.quizzesCompleted,
            parksVisited = state.parksVisited
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── E) ACCOUNT section ──
        SectionLabel("ACCOUNT")

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(cardBg)
                .border(0.5.dp, cardBorder, RoundedCornerShape(14.dp))
        ) {
            Column {
                // Username row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showUsernameDialog = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Username",
                        fontSize = 14.sp,
                        color = primaryText
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = userName.ifEmpty { "Not set" },
                            fontSize = 14.sp,
                            color = mutedText
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Edit",
                            modifier = Modifier.size(20.dp),
                            tint = mutedText
                        )
                    }
                }

                HorizontalDivider(thickness = 0.5.dp, color = cardBorder, modifier = Modifier.padding(horizontal = 16.dp))

                // Email row (not editable)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Email",
                        fontSize = 14.sp,
                        color = primaryText
                    )
                    Text(
                        text = profile.email.ifEmpty { "—" },
                        fontSize = 14.sp,
                        color = mutedText
                    )
                }
            }
        }

        // ── F) Log out — small text button ──
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Log out",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = logoutRed,
            modifier = Modifier
                .clickable(onClick = onLogout)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
    }

    // Avatar picker dialog
    if (showAvatarPicker) {
        AvatarPickerSheet(
            selectedAvatarId = selectedAvatarId,
            customAvatarPath = customAvatarPath,
            onAvatarSelected = onAvatarSelected,
            onUploadImage = onUploadImage,
            onDismiss = { showAvatarPicker = false }
        )
    }

    // Username edit dialog
    if (showUsernameDialog) {
        UsernameEditDialog(
            currentUsername = userName,
            onSave = onUsernameChanged,
            onDismiss = { showUsernameDialog = false }
        )
    }
}

@Composable
private fun ProgressCard(
    xp: Int,
    stage: AvatarStage,
    loginStreak: Int,
    quizzesCompleted: Int,
    parksVisited: Int
) {
    val stageName = stageDisplayName(stage)
    val (currentFloor, nextFloor) = stageBounds(xp)
    val isHealthy = stage == AvatarStage.HEALTHY_TREE
    val progress = if (isHealthy) 1f else {
        val span = (nextFloor - currentFloor).coerceAtLeast(1)
        ((xp - currentFloor).toFloat() / span).coerceIn(0f, 1f)
    }
    val nextStageLabel = nextStageName(stage)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(0.5.dp, cardBorder, RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Row 1: XP value + stage pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$xp",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Light,
                        color = accentGreen
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "XP",
                        fontSize = 14.sp,
                        color = mutedText,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }
                // Stage pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(lightGreenBg)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = stageName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = accentGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Row 2: Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = accentGreenLight,
                trackColor = accentGreen.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Round,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Row 3: Stage labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stageName, fontSize = 11.sp, color = mutedText)
                Text(
                    text = if (isHealthy) "Keep it up!" else nextStageLabel,
                    fontSize = 11.sp,
                    color = mutedText
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 4: Inline stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "\u2753 $quizzesCompleted Quizzes",
                    fontSize = 12.sp,
                    color = mutedText
                )
                Text(
                    text = "\uD83C\uDF33 $parksVisited Parks",
                    fontSize = 12.sp,
                    color = mutedText
                )
                Text(
                    text = "\uD83D\uDD25 $loginStreak Day streak",
                    fontSize = 12.sp,
                    color = mutedText
                )
            }
        }
    }
}

private fun stageBounds(xp: Int): Pair<Int, Int> = when {
    xp <= 15 -> 0 to 16
    xp <= 40 -> 16 to 41
    xp <= 65 -> 41 to 66
    xp <= 85 -> 66 to 86
    else -> 86 to 86
}

private fun nextStageName(stage: AvatarStage): String = when (stage) {
    AvatarStage.WILTED -> "Seed"
    AvatarStage.SEED -> "Sprout"
    AvatarStage.SPROUT -> "Young Tree"
    AvatarStage.YOUNG_TREE -> "Healthy Tree"
    AvatarStage.HEALTHY_TREE -> "Healthy Tree"
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun ProfileAvatar(
    selectedAvatarId: Int,
    customAvatarPath: String?,
    onChangeAvatar: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            if (selectedAvatarId == -1 && customAvatarPath != null) {
                GlideImage(
                    model = File(customAvatarPath),
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, accentGreen, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                val avatar = treeAvatarOptions.getOrNull(selectedAvatarId) ?: treeAvatarOptions[0]
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(avatar.bgColor)
                        .border(2.dp, accentGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = avatar.emoji, fontSize = 56.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(lightGreenBg)
                .clickable { onChangeAvatar() }
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = accentGreen
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Change avatar",
                    fontSize = 11.sp,
                    color = accentGreen,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvatarPickerSheet(
    selectedAvatarId: Int,
    customAvatarPath: String?,
    onAvatarSelected: (Int) -> Unit,
    onUploadImage: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Choose your avatar",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = primaryText,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(200.dp)
            ) {
                items(treeAvatarOptions) { option ->
                    val isSelected = selectedAvatarId == option.id && customAvatarPath == null
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            onAvatarSelected(option.id)
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(option.bgColor)
                                .then(
                                    if (isSelected) Modifier.border(2.dp, accentGreen, CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = option.emoji, fontSize = 36.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = option.label,
                            fontSize = 10.sp,
                            color = if (isSelected) accentGreen else mutedText,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(lightGreenBg)
                    .clickable { onUploadImage() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Upload your own",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = accentGreen
                )
            }
        }
    }
}

@Composable
private fun UsernameEditDialog(
    currentUsername: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var usernameInput by remember { mutableStateOf(currentUsername) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit username",
                fontWeight = FontWeight.SemiBold,
                color = primaryText
            )
        },
        text = {
            OutlinedTextField(
                value = usernameInput,
                onValueChange = { usernameInput = it },
                label = { Text("Your display name in the app") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (usernameInput.isNotBlank()) {
                        onSave(usernameInput.trim())
                        onDismiss()
                    }
                }
            ) {
                Text("Save", color = accentGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = mutedText,
        letterSpacing = 1.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    )
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun ErrorContent(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            color = Color(0xFFC62828),
            textAlign = TextAlign.Center
        )
    }
}
