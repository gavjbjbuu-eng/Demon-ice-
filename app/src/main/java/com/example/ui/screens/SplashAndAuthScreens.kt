package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.auth.AuthHelper
import com.example.ui.components.CustomerSupportRow
import com.example.ui.components.openWhatsAppSupport
import com.example.ui.theme.AlertBlockedRed
import com.example.ui.theme.CardLavenderBorder
import com.example.ui.theme.DarkInk
import com.example.ui.theme.DeepPurple
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.EsportsYellow
import com.example.ui.theme.MutedPurple
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.SplashDarkPurple
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    val scale = remember { Animatable(0.82f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = tween(550))
        alpha.animateTo(1f, animationSpec = tween(350))
        delay(350L)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashDarkPurple)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_splash_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            SplashDarkPurple.copy(alpha = 0.45f),
                            DeepPurple.copy(alpha = 0.65f),
                            SplashDarkPurple.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.scale(scale.value)
            ) {
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .clip(CircleShape)
                        .border(4.dp, EsportsYellow, CircleShape)
                        .background(DeepPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_mascot_cat),
                        contentDescription = "ICE ESPORTS crowned gaming cat mascot",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "ICE",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 46.sp,
                        fontStyle = FontStyle.Italic,
                        letterSpacing = 2.sp
                    ),
                    color = PureWhite
                )
                Text(
                    text = "ESPORTS",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 44.sp,
                        fontStyle = FontStyle.Italic,
                        letterSpacing = 2.sp
                    ),
                    color = EsportsYellow
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "PLAY  •  COMPETE  •  WIN",
                    style = MaterialTheme.typography.titleSmall.copy(letterSpacing = 2.sp),
                    color = PureWhite.copy(alpha = 0.92f)
                )

                Spacer(modifier = Modifier.height(22.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = RoyalPurple,
                    border = BorderStroke(1.5.dp, EsportsYellow)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RUN BY PUSHKAR",
                            style = MaterialTheme.typography.titleSmall,
                            color = PureWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = EsportsYellow,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(alpha.value)
            ) {
                Text(
                    text = "Powered by Firebase",
                    style = MaterialTheme.typography.labelMedium,
                    color = PureWhite.copy(alpha = 0.78f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "CS • BR • Night Mode • Survival",
                    style = MaterialTheme.typography.bodySmall,
                    color = EsportsYellow.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        AuthHelper.attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = {},
            scope = scope
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite)
    ) {
        // Slanted deep purple top wave matching reference Screenshot 2
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
        ) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height * 0.58f)
                lineTo(0f, size.height * 0.92f)
                close()
            }
            drawPath(
                path = path,
                brush = Brush.verticalGradient(listOf(SplashDarkPurple, DeepPurple, RoyalPurple))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .widthIn(max = 460.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(134.dp)
                            .clip(CircleShape)
                            .border(3.dp, EsportsYellow, CircleShape)
                            .background(DeepPurple)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_mascot_cat),
                            contentDescription = "ICE ESPORTS mascot",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ICE ESPORTS",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontStyle = FontStyle.Italic,
                            letterSpacing = 1.sp
                        ),
                        color = EsportsYellow
                    )
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = DeepPurple
                    ) {
                        Text(
                            text = "PLAY • COMPETE • WIN",
                            style = MaterialTheme.typography.labelSmall,
                            color = PureWhite,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(34.dp))

            Column(
                modifier = Modifier.widthIn(max = 460.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome Back!",
                    style = MaterialTheme.typography.headlineLarge,
                    color = DarkInk
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Sign in with your Gmail account to enter tournaments",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MutedPurple,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftLavender),
                    border = BorderStroke(1.dp, CardLavenderBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = RoyalPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "One-Tap Gmail Cloud Sign-In",
                                style = MaterialTheme.typography.titleSmall,
                                color = DarkInk
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "All your player stats, Free Fire UID, matches, credits, and alerts sync instantly on Firebase Cloud Firestore.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedPurple,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                errorMessage = null
                                isLoading = true
                                AuthHelper.onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = {
                                        isLoading = false
                                        onAuthSuccess()
                                    },
                                    onAuthError = { msg ->
                                        isLoading = false
                                        errorMessage = msg
                                    },
                                    scope = scope,
                                    onAuthCancelled = { isLoading = false }
                                )
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RoyalPurple,
                                contentColor = PureWhite
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("google_sign_in_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = EsportsYellow,
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Signing in...", style = MaterialTheme.typography.labelLarge)
                            } else {
                                Text(
                                    text = "G",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = EsportsYellow,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Continue with Google",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }

                        if (!errorMessage.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AlertBlockedRed.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, AlertBlockedRed.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = errorMessage!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AlertBlockedRed,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MutedPurple,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Role-verified access • No public admin/host signup",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedPurple
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Need Help? Customer Support & Channel",
                    style = MaterialTheme.typography.labelMedium,
                    color = DarkInk
                )
                Spacer(modifier = Modifier.height(10.dp))
                CustomerSupportRow(darkText = true)
            }
        }
    }
}

@Composable
fun BlockedScreen(
    reason: String,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashDarkPurple)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_mascot_cat),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.14f),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .systemBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(AlertBlockedRed.copy(alpha = 0.16f))
                    .border(3.dp, AlertBlockedRed, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = "Account blocked",
                    tint = AlertBlockedRed,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "You are Blocked!",
                style = MaterialTheme.typography.headlineLarge,
                color = AlertBlockedRed,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DeepPurple.copy(alpha = 0.9f)),
                border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.16f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Reason:",
                        style = MaterialTheme.typography.labelMedium,
                        color = PureWhite.copy(alpha = 0.72f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = reason.ifBlank { "Suspicious Activity" },
                        style = MaterialTheme.typography.titleMedium,
                        color = PureWhite,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Contact Support:",
                style = MaterialTheme.typography.bodyMedium,
                color = PureWhite.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(28.dp))
                    .background(DeepPurple.copy(alpha = 0.9f))
                    .border(1.dp, EsportsYellow.copy(alpha = 0.6f), RoundedCornerShape(28.dp))
                    .clickable { openWhatsAppSupport(context) }
                    .padding(horizontal = 18.dp, vertical = 10.dp)
                    .testTag("blocked_whatsapp_support")
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF25D366)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SupportAgent,
                        contentDescription = "WhatsApp",
                        tint = PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "9525052870",
                    style = MaterialTheme.typography.titleMedium,
                    color = PureWhite,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = onSignOut,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = ElectricPurple,
                    contentColor = PureWhite
                ),
                border = BorderStroke(1.dp, EsportsYellow),
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("blocked_sign_out_button")
            ) {
                Text(
                    text = "Go Back / Sign Out",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
