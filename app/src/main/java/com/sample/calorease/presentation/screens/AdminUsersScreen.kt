package com.sample.calorease.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sample.calorease.presentation.components.CalorEaseButton
import com.sample.calorease.presentation.components.AdminBottomNavigationBar
import com.sample.calorease.presentation.components.CalorEaseCard
import com.sample.calorease.presentation.navigation.Screen
import com.sample.calorease.presentation.theme.DarkTurquoise
import com.sample.calorease.presentation.theme.Poppins
import com.sample.calorease.presentation.viewmodel.AdminUsersViewModel
import com.sample.calorease.presentation.viewmodel.UserWithStats

/**
 * Admin Users Page - Users Management with CRUD operations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(
    navController: NavController,
    viewModel: AdminUsersViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        bottomBar = { AdminBottomNavigationBar(navController = navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                // Removed verticalScroll - LazyColumn handles scrolling
        ) {
            // FIX-1c: Single header with minimal spacing
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Manage Users",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                color = DarkTurquoise
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Search by name, nickname, or email...",
                        fontFamily = Poppins
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // User Count
            Text(
                text = "${state.filteredUsers.size} user(s) found",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = Poppins,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Users Table
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DarkTurquoise)
                }
            } else if (state.filteredUsers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No users found",
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = Poppins,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.filteredUsers) { user ->
                        UserTableRow(
                            user = user,
                            onEditClick = { viewModel.showEditUserDialog(user) },
                            onStatusClick = { viewModel.showStatusConfirmDialog(user) }
                        )
                    }
                }
            }
        }
    }
    
    // Edit User Dialog
    if (state.showEditDialog) {
        EditUserDialog(
            state = state,
            viewModel = viewModel, // ✅ Phase C: Pass viewModel for admin access toggle
            onDismiss = viewModel::hideEditDialog,
            onConfirm = viewModel::saveUserEdits,
            onFieldChange = viewModel::updateEditField
        )
    }
    
    // Status Confirm Dialog
    if (state.showStatusConfirmDialog && state.selectedUser != null) {
        val user = state.selectedUser!!
        val newStatus = if (user.accountStatus == "active") "deactivated" else "active"
        
        AlertDialog(
            onDismissRequest = viewModel::hideStatusConfirmDialog,
            title = {
                Text(
                    text = "⚠️ Change Account Status",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Change status from \"${user.accountStatus}\" to \"$newStatus\" for ${user.displayName}?",
                    fontFamily = Poppins
                )
            },
            confirmButton = {
                Button(
                    onClick = viewModel::toggleUserStatus,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (newStatus == "deactivated") MaterialTheme.colorScheme.error else DarkTurquoise
                    )
                ) {
                    Text(
                        text = "Confirm",
                        fontFamily = Poppins
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideStatusConfirmDialog) {
                    Text(
                        text = "Cancel",
                        fontFamily = Poppins
                    )
                }
            }
        )
    }
    
    // ✅ Phase C: Admin Access Confirm Dialog
    if (state.showAdminAccessConfirmDialog && state.selectedUser != null) {
        val user = state.selectedUser!!
        val willGrant = state.editAdminAccess && !user.userEntity.adminAccess
        val willRevoke = !state.editAdminAccess && user.userEntity.adminAccess
        
        AlertDialog(
            onDismissRequest = viewModel::hideAdminAccessConfirmDialog,
            title = {
                Text(
                    text = if (willGrant) "👑 Grant Admin Access" else "⚠️ Revoke Admin Access",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (willGrant) {
                        "Grant admin privileges to ${user.displayName}? They will be able to manage users and view statistics."
                    } else {
                        "Remove admin access from ${user.displayName}? They will no longer have admin privileges."
                    },
                    fontFamily = Poppins
                )
            },
            confirmButton = {
                Button(
                    onClick = viewModel::applyAdminAccessToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (willGrant) DarkTurquoise else MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = "Confirm",
                        fontFamily = Poppins
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    // Reset toggle and close
                    viewModel.toggleEditAdminAccess(user.userEntity.adminAccess)
                    viewModel.hideAdminAccessConfirmDialog()
                }) {
                    Text(
                        text = "Cancel",
                        fontFamily = Poppins
                    )
                }
            }
        )
    }
}

@Composable
fun UserTableRow(
    user: UserWithStats,
    onEditClick: () -> Unit,
    onStatusClick: () -> Unit
) {
    CalorEaseCard {
        Row(
            modifier = Modifier

.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // User Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ID: ${user.userEntity.userId}",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = Poppins,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Nickname: ${user.nickname}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = Poppins
                )
                
                Row {
                    Text(
                        text = "Age: ${user.age} • ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = Poppins
                    )
                    Text(
                        text = "Height: ${user.height.toInt()}cm • ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = Poppins
                    )
                    Text(
                        text = "Weight: ${String.format("%.1f", user.weight)}kg",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = Poppins
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Badge
                    val statusColor = if (user.accountStatus == "active") 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.error
                    
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = statusColor.copy(alpha = 0.1f),
                        modifier = Modifier.clickable(onClick = onStatusClick)
                    ) {
                        Text(
                            text = user.accountStatus.uppercase(),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    
                    Text(
                        text = "Created: ${user.accountCreatedDate}",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = Poppins,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Edit Button
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit User",
                    tint = DarkTurquoise
                )
            }
        }
    }
}

@Composable
fun EditUserDialog(
    state: com.sample.calorease.presentation.viewmodel.AdminUsersState,
    viewModel: AdminUsersViewModel, // ✅ Phase C: Need viewModel for admin access toggle
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onFieldChange: (String, String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "✏️ Edit User",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.editFirstName,
                    onValueChange = { onFieldChange("firstName", it) },
                    label = { Text("First Name", fontFamily = Poppins) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = state.editLastName,
                    onValueChange = { onFieldChange("lastName", it) },
                    label = { Text("Last Name", fontFamily = Poppins) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = state.editNickname,
                    onValueChange = { onFieldChange("nickname", it) },
                    label = { Text("Nickname (optional)", fontFamily = Poppins) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = state.editAge,
                    onValueChange = { onFieldChange("age", it) },
                    label = { Text("Age", fontFamily = Poppins) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = state.editHeight,
                    onValueChange = { onFieldChange("height", it) },
                    label = { Text("Height (cm)", fontFamily = Poppins) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = state.editWeight,
                    onValueChange = { onFieldChange("weight", it) },
                    label = { Text("Weight (kg)", fontFamily = Poppins) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // ✅ Phase C: Admin Access Toggle (Super Admin Only)
                if (state.isSuperAdmin && state.selectedUser?.userEntity?.isSuperAdmin == false) {
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Divider()
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "👑 Admin Access",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Grant admin privileges",
                                fontFamily = Poppins,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Switch(
                            checked = state.editAdminAccess,
                            onCheckedChange = { newValue ->
                                viewModel.toggleEditAdminAccess(newValue)
                                if (newValue != state.selectedUser?.userEntity?.adminAccess) {
                                    viewModel.showAdminAccessConfirmDialog()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DarkTurquoise,
                                checkedTrackColor = DarkTurquoise.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = DarkTurquoise)
            ) {
                Text("Save", fontFamily = Poppins)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = Poppins)
            }
        }
    )
}
