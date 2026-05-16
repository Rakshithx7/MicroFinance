package com.example.microfinance.ui.member

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.microfinance.data.entity.MemberEntity
import com.example.microfinance.ui.components.EmptyState
import com.example.microfinance.ui.components.MemberAvatar
import com.example.microfinance.ui.components.PremiumCard
import com.example.microfinance.ui.components.PremiumTextField
import com.example.microfinance.ui.components.PrimaryButton
import com.example.microfinance.ui.components.SectionHeader
import com.example.microfinance.ui.theme.BackgroundLight
import com.example.microfinance.ui.theme.BrandPrimary
import com.example.microfinance.ui.theme.DividerColor
import com.example.microfinance.ui.theme.PoppinsFontFamily
import com.example.microfinance.ui.theme.StatusPending
import com.example.microfinance.ui.theme.SurfaceElevated
import com.example.microfinance.ui.theme.TextOnPrimary
import com.example.microfinance.ui.theme.TextPrimary
import com.example.microfinance.ui.theme.TextSecondary

@Composable
fun MemberScreen(
    viewModel: MemberViewModel,
    modifier: Modifier = Modifier
) {
    val members       by viewModel.members.collectAsState()
    val detailId      by viewModel.detailMemberId.collectAsState()
    val deleteMessage by viewModel.deleteMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Show delete result as snackbar
    LaunchedEffect(deleteMessage) {
        deleteMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearDeleteMessage()
        }
    }

    // Navigate to detail screen when a member is selected
    AnimatedContent(
        targetState = detailId != null,
        transitionSpec = {
            (fadeIn(tween(250)) + slideInVertically(tween(250)) { it / 10 }) togetherWith
            fadeOut(tween(200))
        },
        label = "member_detail_nav"
    ) { showDetail ->
        if (showDetail) {
            MemberDetailScreen(
                viewModel = viewModel,
                onBack    = { viewModel.closeDetail() }
            )
        } else {
            MemberListScreen(
                viewModel         = viewModel,
                members           = members,
                snackbarHostState = snackbarHostState,
                modifier          = modifier
            )
        }
    }
}

@Composable
private fun MemberListScreen(
    viewModel: MemberViewModel,
    members: List<MemberEntity>,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    var name           by remember { mutableStateOf("") }
    var phone          by remember { mutableStateOf("") }
    var showNameError  by remember { mutableStateOf(false) }
    var showForm       by remember { mutableStateOf(false) }
    var memberToDelete by remember { mutableStateOf<MemberEntity?>(null) }

    Scaffold(
        modifier          = modifier,
        containerColor    = BackgroundLight,
        snackbarHost      = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { showForm = !showForm },
                containerColor = BrandPrimary,
                contentColor   = TextOnPrimary,
                elevation      = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Icon(Icons.Rounded.Add, "Add Member")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // ── Add member form ───────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = showForm,
                    enter   = fadeIn(tween(250)) + slideInVertically(tween(250)) { -it / 2 }
                ) {
                    PremiumCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text("Add New Member", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextPrimary)

                            PremiumTextField(
                                value         = name,
                                onValueChange = { name = it; if (showNameError) showNameError = it.trim().isEmpty() },
                                label         = "Member Name *",
                                isError       = showNameError,
                                errorMessage  = "Name is required",
                                leadingIcon   = {
                                    Icon(Icons.Rounded.Person, null,
                                        tint = if (showNameError) MaterialTheme.colorScheme.error else TextSecondary,
                                        modifier = Modifier.size(20.dp))
                                }
                            )

                            PremiumTextField(
                                value         = phone,
                                onValueChange = { phone = it },
                                label         = "Phone Number",
                                keyboardType  = KeyboardType.Phone,
                                leadingIcon   = {
                                    Icon(Icons.Rounded.Phone, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                                }
                            )

                            PrimaryButton(
                                text  = "Add Member",
                                onClick = {
                                    val isValid = name.trim().isNotEmpty()
                                    showNameError = !isValid
                                    if (isValid) {
                                        viewModel.addMember(name, phone, null)
                                        name      = ""
                                        phone     = ""
                                        showForm  = false
                                    }
                                },
                                modifier    = Modifier.fillMaxWidth(),
                                leadingIcon = Icons.Rounded.Add
                            )
                        }
                    }
                }
            }

            // ── Member count header ───────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(title = "Members")
                    if (members.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(SurfaceElevated)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("${members.size}", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = BrandPrimary)
                        }
                    }
                }
            }

            if (members.isEmpty()) {
                item {
                    EmptyState(icon = Icons.Rounded.Group, message = "No members yet.\nTap + to add your first member.")
                }
            }

            // ── Member cards ──────────────────────────────────────────────
            itemsIndexed(members) { index, member ->
                AnimatedVisibility(
                    visible = true,
                    enter   = fadeIn(tween(200, delayMillis = index * 40)) +
                              slideInVertically(tween(200, delayMillis = index * 40)) { it / 3 }
                ) {
                    MemberCard(
                        member   = member,
                        onClick  = { viewModel.openDetail(member.id) },
                        onDelete = { memberToDelete = member }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // ── Delete confirmation dialog ────────────────────────────────────────
    memberToDelete?.let { member ->
        AlertDialog(
            onDismissRequest = { memberToDelete = null },
            title = {
                Text(
                    text = "Delete Member?",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${member.name}\"?\n\nThis will also remove all their savings entries. This action cannot be undone.",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMember(member)
                        memberToDelete = null
                    }
                ) {
                    Text(
                        text = "Delete",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = StatusPending
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToDelete = null }) {
                    Text("Cancel", fontFamily = PoppinsFontFamily, color = BrandPrimary)
                }
            }
        )
    }
}

@Composable
private fun MemberCard(
    member: MemberEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    PremiumCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true)
                ) { onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MemberAvatar(name = member.name, size = 46.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(member.name, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
                if (member.phone.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(member.phone, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, color = TextSecondary)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text("Tap to view profile →", fontFamily = PoppinsFontFamily, fontSize = 10.sp, color = BrandPrimary.copy(alpha = 0.7f))
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Delete member",
                    tint = StatusPending.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
