package com.sample.calorease.presentation.components

/**
 * ComponentPreviews.kt
 *
 * Self-contained @Preview composables for all 8 reusable UI components.
 * No hiltViewModel() is used — all components are stateless or use local state.
 *
 * Includes multiple states per component:
 *  - Enabled / Disabled / Loading / Error / Progress levels
 */

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.sample.calorease.presentation.theme.CalorEaseTheme
import com.sample.calorease.presentation.theme.DarkTurquoise

// ──────────────────────────────────────────────
// CalorEaseButton
// ──────────────────────────────────────────────

@Preview(name = "Button - Default", showBackground = true)
@Composable
private fun CalorEaseButtonDefaultPreview() {
    CalorEaseTheme {
        Surface {
            CalorEaseButton(text = "Get Started", onClick = {})
        }
    }
}

@Preview(name = "Button - Loading", showBackground = true)
@Composable
private fun CalorEaseButtonLoadingPreview() {
    CalorEaseTheme {
        Surface {
            CalorEaseButton(text = "Get Started", onClick = {}, isLoading = true)
        }
    }
}

@Preview(name = "Button - Disabled", showBackground = true)
@Composable
private fun CalorEaseButtonDisabledPreview() {
    CalorEaseTheme {
        Surface {
            CalorEaseButton(text = "Get Started", onClick = {}, enabled = false)
        }
    }
}

// ──────────────────────────────────────────────
// CalorEaseOutlinedButton
// ──────────────────────────────────────────────

@Preview(name = "Outlined Button - Default", showBackground = true)
@Composable
private fun CalorEaseOutlinedButtonDefaultPreview() {
    CalorEaseTheme {
        Surface {
            CalorEaseOutlinedButton(text = "Sign in with Google", onClick = {})
        }
    }
}

// ──────────────────────────────────────────────
// CalorEaseTextField
// ──────────────────────────────────────────────

@Preview(name = "TextField - Empty", showBackground = true)
@Composable
private fun CalorEaseTextFieldEmptyPreview() {
    CalorEaseTheme {
        Surface {
            CalorEaseTextField(
                value = "",
                onValueChange = {},
                label = "Email",
                placeholder = "Enter your email",
                leadingIcon = Icons.Default.Email
            )
        }
    }
}

@Preview(name = "TextField - Filled", showBackground = true)
@Composable
private fun CalorEaseTextFieldFilledPreview() {
    CalorEaseTheme {
        Surface {
            CalorEaseTextField(
                value = "test@calorease.com",
                onValueChange = {},
                label = "Email",
                leadingIcon = Icons.Default.Email
            )
        }
    }
}

@Preview(name = "TextField - Error", showBackground = true)
@Composable
private fun CalorEaseTextFieldErrorPreview() {
    CalorEaseTheme {
        Surface {
            CalorEaseTextField(
                value = "notanemail",
                onValueChange = {},
                label = "Email",
                isError = true,
                errorMessage = "Invalid email format"
            )
        }
    }
}

@Preview(name = "TextField - Password", showBackground = true)
@Composable
private fun CalorEaseTextFieldPasswordPreview() {
    CalorEaseTheme {
        Surface {
            CalorEaseTextField(
                value = "mySecretPass",
                onValueChange = {},
                label = "Password",
                isPassword = true
            )
        }
    }
}

// ──────────────────────────────────────────────
// CalorEaseCard
// ──────────────────────────────────────────────

@Preview(name = "Card - Default", showBackground = true)
@Composable
private fun CalorEaseCardPreview() {
    CalorEaseTheme {
        Surface {
            CalorEaseCard {
                Text(
                    text = "Card Content",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Preview(name = "Card - Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CalorEaseCardDarkPreview() {
    CalorEaseTheme {
        Surface {
            CalorEaseCard {
                Text(
                    text = "Dark Mode Card",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

// ──────────────────────────────────────────────
// CalorEaseProgressBar
// ──────────────────────────────────────────────

@Preview(name = "Progress Bar - 0%", showBackground = true)
@Composable
private fun CalorEaseProgressBarEmptyPreview() {
    CalorEaseTheme {
        Surface {
            CalorEaseProgressBar(
                progress = 0f,
                stepText = "Step 0 of 4",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "Progress Bar - 25%", showBackground = true)
@Composable
private fun CalorEaseProgressBarQuarterPreview() {
    CalorEaseTheme {
        Surface {
            CalorEaseProgressBar(
                progress = 0.25f,
                stepText = "Step 1 of 4",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "Progress Bar - 50%", showBackground = true)
@Composable
private fun CalorEaseProgressBarHalfPreview() {
    CalorEaseTheme {
        Surface {
            CalorEaseProgressBar(
                progress = 0.5f,
                stepText = "Step 2 of 4",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "Progress Bar - 75%", showBackground = true)
@Composable
private fun CalorEaseProgressBarThreeQuarterPreview() {
    CalorEaseTheme {
        Surface {
            CalorEaseProgressBar(
                progress = 0.75f,
                stepText = "Step 3 of 4",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "Progress Bar - 100%", showBackground = true)
@Composable
private fun CalorEaseProgressBarFullPreview() {
    CalorEaseTheme {
        Surface {
            CalorEaseProgressBar(
                progress = 1.0f,
                stepText = "Step 4 of 4",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

// ──────────────────────────────────────────────
// Navigation Bars
// (Uses rememberNavController which works fine in preview)
// ──────────────────────────────────────────────

@Preview(name = "Bottom Nav - User", showBackground = true)
@Composable
private fun BottomNavigationBarPreview() {
    CalorEaseTheme {
        Surface {
            BottomNavigationBar(navController = rememberNavController())
        }
    }
}

@Preview(name = "Bottom Nav - Admin", showBackground = true)
@Composable
private fun AdminBottomNavigationBarPreview() {
    CalorEaseTheme {
        Surface {
            AdminBottomNavigationBar(navController = rememberNavController())
        }
    }
}
