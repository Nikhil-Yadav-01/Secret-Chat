package com.rudraksha.secretchat.ui.screens.authentication

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rudraksha.secretchat.data.model.User
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    register: (String, String, String, String, String) -> Unit,
    observeRegisterState: State<String>,
    observeRegisteredUser: State<User?>,
    onNavigateToLogin: () -> Unit = {},
    onRegisterSuccess: (User) -> Unit = {}
) {
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("@") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val registerState by observeRegisterState
    val registeredUser by observeRegisteredUser
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Ensure username starts with "@"
    LaunchedEffect(username) {
        if (username.isNotEmpty() && !username.startsWith("@")) {
            username = "@$username"
        }
    }

    // Show toast for registration errors/success
    LaunchedEffect(registerState) {
        if (registerState.isNotEmpty()) {
            Toast.makeText(context, registerState, Toast.LENGTH_SHORT).show()
        }
    }

    // Handle successful registration
    LaunchedEffect(registeredUser) {
        registeredUser?.let {
            onRegisterSuccess(it)
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        TextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = username,
            onValueChange = { newUsername ->
                if (!newUsername.contains(",") && !newUsername.contains("$") && !newUsername.contains("^")) {
                    username = if (newUsername.startsWith("@")) newUsername else "@$newUsername"
                }
            },
            label = { Text("Username") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                    )
                }
            },
            keyboardActions = KeyboardActions(
                onDone = {
                    scope.launch { register(fullName, username, email, password, confirmPassword) }
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { scope.launch { register(fullName, username, email, password, confirmPassword) } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }

        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Login")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterPreview() {
    val mockRegisterState = remember { mutableStateOf("") }
    val mockRegisteredUser = remember { mutableStateOf<User?>(null) }

    RegisterScreen(
        register = { _, _, _, _, _ ->
            mockRegisterState.value = "Mock registration successful"
        },
        observeRegisterState = mockRegisterState,
        observeRegisteredUser = mockRegisteredUser as State<User?>,
        onNavigateToLogin = { /* Mock Navigation */ },
        onRegisterSuccess = { mockRegisterState.value = "Navigating to Home..." }
    )
}
