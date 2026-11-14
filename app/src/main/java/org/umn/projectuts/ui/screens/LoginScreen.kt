package org.umn.projectuts.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.umn.projectuts.ui.components.PrimaryActionButton
import org.umn.projectuts.ui.components.SafeUmnWordmark
import org.umn.projectuts.ui.theme.UmnBlue
import org.umn.projectuts.ui.theme.UmnNavy
import org.umn.projectuts.viewmodel.LoginViewModel

@Composable
fun LoginScreen(vm: LoginViewModel, onLoggedIn: (String) -> Unit) {
    val focus = LocalFocusManager.current
    val context = LocalContext.current
    val heroRes = remember {
        context.resources.getIdentifier("login_bg", "drawable", context.packageName)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(Brush.verticalGradient(listOf(UmnNavy, UmnBlue)))
        ) {
            if (heroRes != 0) {
                Image(
                    painter = painterResource(heroRes),
                    contentDescription = "Gedung Universitas Multimedia Nusantara",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                UmnNavy.copy(alpha = 0.30f),
                                UmnNavy.copy(alpha = 0.50f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SafeUmnWordmark(color = Color.White, fontSize = 44.sp)
                Text(
                    "Sign in to your account",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(
            modifier = Modifier
                .offset(y = (-24).dp)
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.background,
                    RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            if (vm.authError != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        vm.authError.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            OutlinedTextField(
                value = vm.email,
                onValueChange = { vm.email = it },
                label = { Text("Email") },
                placeholder = { Text("example@student.umn.ac.id") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                isError = vm.emailError != null,
                supportingText = { vm.emailError?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focus.moveFocus(FocusDirection.Down) }
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(4.dp))

            OutlinedTextField(
                value = vm.password,
                onValueChange = { vm.password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { vm.passwordVisible = !vm.passwordVisible }) {
                        Icon(
                            if (vm.passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (vm.passwordVisible) "Sembunyikan password" else "Tampilkan password"
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (vm.passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                isError = vm.passwordError != null,
                supportingText = { vm.passwordError?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focus.clearFocus()
                        vm.login(onLoggedIn)
                    }
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = vm.rememberMe,
                    onCheckedChange = { vm.rememberMe = it }
                )
                Text("Remember me", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(8.dp))

            PrimaryActionButton(
                text = "Log in",
                onClick = {
                    focus.clearFocus()
                    vm.login(onLoggedIn)
                },
                loading = vm.isLoading
            )

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(Modifier.weight(1f))
                Text(
                    "  Or login with  ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(Modifier.weight(1f))
            }

            Spacer(Modifier.height(20.dp))

            OutlinedButton(
                onClick = { vm.loginWithSso(onLoggedIn) },
                enabled = !vm.isLoading,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Single Sign On (SSO)", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Demo: gunakan email @student.umn.ac.id dan password minimal 6 karakter.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}
