package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.*
import com.example.viewmodel.ActiveGame
import com.example.viewmodel.Direction
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.GridPoint
import com.example.viewmodel.MemoryCard
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("arcade_scaffold"),
                    containerColor = SophisticatedBackground
                ) { innerPadding ->
                    ArcadeApp(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ArcadeApp(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel()
) {
    val activeGame by viewModel.activeGame.collectAsState()

    Box(
        modifier = modifier
            .background(SophisticatedBackground)
            .drawBehind {
                // Subtle scanlines in the background for retro-arcade TV look
                val step = 10f
                for (y in 0..size.height.toInt() step step.toInt()) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.01f),
                        start = Offset(0f, y.toFloat()),
                        end = Offset(size.width, y.toFloat()),
                        strokeWidth = 1f
                    )
                }
            }
    ) {
        when (activeGame) {
            ActiveGame.NONE -> MainMenuScreen(viewModel = viewModel)
            ActiveGame.SNAKE -> SnakeScreen(viewModel = viewModel)
            ActiveGame.TIC_TAC_TOE -> TicTacToeScreen(viewModel = viewModel)
            ActiveGame.MEMORY -> MemoryScreen(viewModel = viewModel)
        }
    }
}

// --- MAIN MENU SCREEN ---
@Composable
fun MainMenuScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val allScores by viewModel.allScores.collectAsState()
    val snakeBest by viewModel.snakeHighScore.collectAsState()
    val tttBest by viewModel.ticTacToeHighScore.collectAsState()
    val memoryBest by viewModel.memoryHighScore.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- TOP APP BAR ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SophisticatedSurface)
                            .clickable { /* Subtle decorative tap */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Gamepad,
                            contentDescription = "Menu",
                            tint = SophisticatedPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = "Offline Hub",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.SansSerif,
                        color = SophisticatedText
                    )
                }
                
                // Avatar badge "OA" (Offline Arcade)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SophisticatedPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "OA",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SophisticatedOnPrimary,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        }

        // --- FEATURED HERO GAME CARD ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(192.dp)
                    .testTag("featured_hero_card")
                    .clickable { viewModel.selectGame(ActiveGame.SNAKE) }
                    .border(1.dp, SophisticatedOutline.copy(alpha = 0.2f), RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(SophisticatedAccent, Color(0xFF381E72))
                            )
                        )
                        .padding(24.dp)
                ) {
                    // Badge Top Right
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "INSTALLED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SophisticatedSecondary,
                            letterSpacing = 1.sp
                        )
                    }

                    // Bottom Column Content
                    Column(
                        modifier = Modifier.align(Alignment.BottomStart)
                    ) {
                        Text(
                            text = "Neon Snake",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Classic pixel runner. No Wi-Fi needed.",
                            fontSize = 14.sp,
                            color = SophisticatedSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.selectGame(ActiveGame.SNAKE) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SophisticatedPrimary,
                                contentColor = SophisticatedOnPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = "PLAY NOW",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // --- QUICK START TITLE ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Jump back in",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = SophisticatedText
                )
                Text(
                    text = "3 offline games",
                    fontSize = 12.sp,
                    color = SophisticatedPrimary
                )
            }
        }

        // --- QUICK START GAMES LIST ---
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Game Row 1: Neon Snake & Cyber Tic-Tac-Toe
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SophisticatedGameTile(
                        title = "Neon Snake",
                        tagline = "Slither & Grow",
                        emoji = "🐍",
                        iconBackground = Color(0xFFE1BEE7),
                        tag = "snake_game_card",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.selectGame(ActiveGame.SNAKE) }
                    )
                    SophisticatedGameTile(
                        title = "Cyber TTT",
                        tagline = "VS AI or Friend",
                        emoji = "♟️",
                        iconBackground = Color(0xFFBBDEFB),
                        tag = "ttt_game_card",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.selectGame(ActiveGame.TIC_TAC_TOE) }
                    )
                }

                // Game Row 2: Cyber Match & Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SophisticatedGameTile(
                        title = "Cyber Match",
                        tagline = "Pairs & Logic",
                        emoji = "🧩",
                        iconBackground = Color(0xFFFFCDD2),
                        tag = "memory_game_card",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.selectGame(ActiveGame.MEMORY) }
                    )
                    
                    // Display best score indicator quick badge
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SophisticatedSurface)
                            .border(1.dp, SophisticatedOutline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFF9C4)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🏆", fontSize = 18.sp)
                            }
                            Column {
                                Text(
                                    text = "Top Record",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SophisticatedText
                                )
                                Text(
                                    text = "Snake: $snakeBest pts",
                                    fontSize = 11.sp,
                                    color = SophisticatedSubtext
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- LEADERBOARD / STATS ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SophisticatedOutline.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .background(SophisticatedSurface, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT MATCHES",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SophisticatedSecondary,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    if (allScores.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.clearDatabaseScores() },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("clear_scores_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteSweep,
                                contentDescription = "Clear scores",
                                tint = CyberPink
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                if (allScores.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.BarChart,
                                contentDescription = "No stats yet",
                                tint = SophisticatedSubtext,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No local matches played yet. Go offline and break records!",
                                fontSize = 12.sp,
                                color = SophisticatedSubtext,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    allScores.take(3).forEach { score ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val (gameName, emoji, tint) = when (score.gameType) {
                                    "SNAKE" -> Triple("Neon Snake", "🐍", CyberGreen)
                                    "TIC_TAC_TOE" -> Triple("Cyber TTT", "♟️", CyberCyan)
                                    else -> Triple("Cyber Match", "🧩", CyberPink)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(SophisticatedOutline.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(emoji, fontSize = 14.sp)
                                }
                                Text(
                                    text = gameName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SophisticatedText
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${score.score} pts",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SophisticatedPrimary
                                )
                                val dateStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(score.timestamp))
                                Text(
                                    text = dateStr,
                                    fontSize = 10.sp,
                                    color = SophisticatedSubtext
                                )
                            }
                        }
                        if (score != allScores.take(3).last()) {
                            HorizontalDivider(color = SophisticatedOutline.copy(alpha = 0.3f), thickness = 1.dp)
                        }
                    }
                }
            }
        }

        // --- STORAGE STATUS TIP (BOTTOM) ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SophisticatedOutline.copy(alpha = 0.15f))
                    .border(1.dp, SophisticatedOutline, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SophisticatedSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💾", fontSize = 20.sp)
                }
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "You have 3 offline games fully installed on device. Play anytime without Wi-Fi, cellular, or data.",
                        fontSize = 12.sp,
                        color = SophisticatedSubtext,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SophisticatedGameTile(
    title: String,
    tagline: String,
    emoji: String,
    iconBackground: Color,
    tag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SophisticatedSurface)
            .border(1.dp, SophisticatedOutline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(tag)
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 18.sp)
            }
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SophisticatedText
                )
                Text(
                    text = tagline,
                    fontSize = 11.sp,
                    color = SophisticatedSubtext,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


// --- RETRO SNAKE SCREEN ---
@Composable
fun SnakeScreen(viewModel: GameViewModel) {
    val snakeBody = viewModel.snakeBody
    val food = viewModel.snakeFood
    val isGameOver = viewModel.isSnakeGameOver
    val isPaused = viewModel.isSnakePaused
    val score = viewModel.snakeScore
    val bestScore by viewModel.snakeHighScore.collectAsState()
    val haptic = LocalHapticFeedback.current

    // Observe food changes to trigger a quick tactile tick
    LaunchedEffect(food) {
        if (score > 0) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("snake_screen_root")
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top HUD
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.selectGame(ActiveGame.NONE) },
                modifier = Modifier.testTag("snake_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = SophisticatedText
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "NEON SNAKE",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    color = SophisticatedPrimary
                )
                Text(
                    text = "BEST: $bestScore",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = SophisticatedSecondary
                )
            }
            IconButton(
                onClick = { viewModel.toggleSnakePause() },
                modifier = Modifier.testTag("snake_pause_button")
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                    contentDescription = if (isPaused) "Resume" else "Pause",
                    tint = SophisticatedText
                )
            }
        }

        // Gameplay Arena Card
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .border(1.dp, SophisticatedOutline, RoundedCornerShape(16.dp))
                .background(SophisticatedSurface, RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    // Swipe gesture controls
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val (x, y) = dragAmount
                        if (abs(x) > abs(y)) {
                            if (x > 0) viewModel.changeSnakeDirection(Direction.RIGHT)
                            else viewModel.changeSnakeDirection(Direction.LEFT)
                        } else {
                            if (y > 0) viewModel.changeSnakeDirection(Direction.DOWN)
                            else viewModel.changeSnakeDirection(Direction.UP)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Main Canvas Arena
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellWidth = size.width / viewModel.snakeGridSize
                val cellHeight = size.height / viewModel.snakeGridSize

                // Draw Grid Dots (Subtle grid)
                for (i in 0 until viewModel.snakeGridSize) {
                    for (j in 0 until viewModel.snakeGridSize) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.03f),
                            radius = 1.5f,
                            center = Offset(i * cellWidth + cellWidth / 2, j * cellHeight + cellHeight / 2)
                        )
                    }
                }

                // Draw Food
                drawRect(
                    color = SophisticatedPrimary,
                    topLeft = Offset(food.x * cellWidth + 2f, food.y * cellHeight + 2f),
                    size = Size(cellWidth - 4f, cellHeight - 4f)
                )

                // Draw Snake Body
                snakeBody.forEachIndexed { index, point ->
                    val isHead = index == 0
                    drawRoundRect(
                        color = if (isHead) SophisticatedSecondary else SophisticatedPrimary.copy(alpha = 0.65f),
                        topLeft = Offset(point.x * cellWidth + 1.5f, point.y * cellHeight + 1.5f),
                        size = Size(cellWidth - 3f, cellHeight - 3f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
            }

            // Paused Overlay
            if (isPaused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SophisticatedBackground.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "GAME PAUSED",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        color = SophisticatedText
                    )
                }
            }

            // Game Over Overlay
            if (isGameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SophisticatedBackground.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "GAME OVER",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.SansSerif,
                            color = SophisticatedPrimary
                        )
                        Text(
                            text = "SCORE: $score",
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            color = SophisticatedText
                        )
                        Button(
                            onClick = { viewModel.resetSnakeGame() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SophisticatedPrimary,
                                contentColor = SophisticatedOnPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("snake_replay_button")
                        ) {
                            Text(
                                "PLAY AGAIN",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Live Score HUD
        Text(
            text = "SCORE: $score",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = SophisticatedPrimary
        )

        // On-Screen Tactile D-Pad Controller
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.changeSnakeDirection(Direction.UP)
                },
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(SophisticatedSurface)
                    .border(1.dp, SophisticatedOutline, CircleShape)
                    .testTag("dpad_up")
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowUpward,
                    contentDescription = "Up",
                    tint = SophisticatedPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.changeSnakeDirection(Direction.LEFT)
                    },
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(SophisticatedSurface)
                        .border(1.dp, SophisticatedOutline, CircleShape)
                        .testTag("dpad_left")
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Left",
                        tint = SophisticatedPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(48.dp))

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.changeSnakeDirection(Direction.RIGHT)
                    },
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(SophisticatedSurface)
                        .border(1.dp, SophisticatedOutline, CircleShape)
                        .testTag("dpad_right")
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = "Right",
                        tint = SophisticatedPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.changeSnakeDirection(Direction.DOWN)
                },
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(SophisticatedSurface)
                    .border(1.dp, SophisticatedOutline, CircleShape)
                    .testTag("dpad_down")
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowDownward,
                    contentDescription = "Down",
                    tint = SophisticatedPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}


// --- TIC-TAC-TOE SCREEN ---
@Composable
fun TicTacToeScreen(viewModel: GameViewModel) {
    val board = viewModel.tttBoard
    val currentPlayer = viewModel.tttCurrentPlayer
    val winner = viewModel.tttWinner
    val winningLine = viewModel.tttWinningLine
    val isSinglePlayer = viewModel.tttIsSinglePlayer
    val xWins = viewModel.tttXWins
    val oWins = viewModel.tttOWins
    val draws = viewModel.tttDraws
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ttt_screen_root")
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TTT Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.selectGame(ActiveGame.NONE) },
                modifier = Modifier.testTag("ttt_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = SophisticatedText
                )
            }
            Text(
                text = "CYBER TTT",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = SophisticatedPrimary
            )
            IconButton(
                onClick = { viewModel.resetTicTacToeGame() },
                modifier = Modifier.testTag("ttt_reset_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Reset Game",
                    tint = SophisticatedText
                )
            }
        }

        // Game Mode Selector (Toggle Buttons)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, SophisticatedOutline, RoundedCornerShape(12.dp))
                .background(SophisticatedSurface, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val singleWeight = if (isSinglePlayer) 1.2f else 1f
            val multiWeight = if (!isSinglePlayer) 1.2f else 1f

            Button(
                onClick = { viewModel.changeTttGameMode(true) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSinglePlayer) SophisticatedPrimary else Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(singleWeight)
                    .testTag("ttt_mode_single")
            ) {
                Text(
                    text = "VS AI",
                    color = if (isSinglePlayer) SophisticatedOnPrimary else SophisticatedText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }
            Button(
                onClick = { viewModel.changeTttGameMode(false) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isSinglePlayer) SophisticatedPrimary else Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(multiWeight)
                    .testTag("ttt_mode_multi")
            ) {
                Text(
                    text = "PASS & PLAY",
                    color = if (!isSinglePlayer) SophisticatedOnPrimary else SophisticatedText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }

        // Score Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("PLAYER X", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = SophisticatedPrimary)
                Text("$xWins", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = SophisticatedText)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("DRAWS", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = SophisticatedSubtext)
                Text("$draws", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = SophisticatedText)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val label = if (isSinglePlayer) "AI (O)" else "PLAYER O"
                Text(label, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = SophisticatedSecondary)
                Text("$oWins", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = SophisticatedText)
            }
        }

        // Game Turn Status
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val statusText = when {
                winner == "DRAW" -> "IT'S A DRAW!"
                winner != null -> "VICTORY FOR PLAYER $winner!"
                isSinglePlayer && currentPlayer == "O" -> "AI THINKING..."
                else -> "PLAYER $currentPlayer'S TURN"
            }
            val statusColor = when (winner) {
                "X" -> SophisticatedPrimary
                "O" -> SophisticatedSecondary
                "DRAW" -> SophisticatedSecondary
                else -> if (currentPlayer == "X") SophisticatedPrimary else SophisticatedSecondary
            }
            Text(
                text = statusText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = statusColor
            )
        }

        // 3x3 Grid
        Column(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (row in 0 until 3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (col in 0 until 3) {
                        val index = row * 3 + col
                        val cellValue = board[index]
                        val isWinningCell = winningLine?.contains(index) == true

                        val borderTint by animateColorAsState(
                            targetValue = when {
                                isWinningCell -> SophisticatedSecondary
                                cellValue == "X" -> SophisticatedPrimary.copy(alpha = 0.5f)
                                cellValue == "O" -> SophisticatedSecondary.copy(alpha = 0.5f)
                                else -> SophisticatedOutline
                            }
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SophisticatedSurface)
                                .border(
                                    width = if (isWinningCell) 2.dp else 1.dp,
                                    color = borderTint,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    if (winner == null && !(isSinglePlayer && currentPlayer == "O")) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.playTttMove(index)
                                    }
                                }
                                .testTag("ttt_cell_$index"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (cellValue != "") {
                                if (cellValue == "X") {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "X",
                                        tint = SophisticatedPrimary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                } else if (cellValue == "O") {
                                    Icon(
                                        imageVector = Icons.Filled.RadioButtonUnchecked,
                                        contentDescription = "O",
                                        tint = SophisticatedSecondary,
                                        modifier = Modifier.size(42.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Play Again Button when game is finished
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (winner != null) {
                Button(
                    onClick = { viewModel.resetTicTacToeGame() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SophisticatedPrimary,
                        contentColor = SophisticatedOnPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("ttt_replay_button")
                ) {
                    Text(
                        text = "PLAY NEXT ROUND",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


// --- CYBER MEMORY MATCH SCREEN ---
@Composable
fun MemoryScreen(viewModel: GameViewModel) {
    val cards = viewModel.memoryCards
    val moves = viewModel.memoryMoves
    val isGameOver = viewModel.isMemoryGameOver
    val bestScore by viewModel.memoryHighScore.collectAsState()
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("memory_screen_root")
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header HUD
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.selectGame(ActiveGame.NONE) },
                modifier = Modifier.testTag("memory_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = SophisticatedText
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "CYBER MATCH",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    color = SophisticatedPrimary
                )
                Text(
                    text = "RECORD MOVES: ${if (bestScore > 0) bestScore else "---"}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = SophisticatedSecondary
                )
            }
            IconButton(
                onClick = { viewModel.resetMemoryGame() },
                modifier = Modifier.testTag("memory_reset_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Reset",
                    tint = SophisticatedText
                )
            }
        }

        // Live stats row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MOVES: $moves",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = SophisticatedText
            )
            val matches = viewModel.memoryMatchedPairs
            Text(
                text = "MATCHED: $matches/8",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = SophisticatedPrimary
            )
        }

        // 4x4 Grid
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (cards.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (row in 0 until 4) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (col in 0 until 4) {
                                val index = row * 4 + col
                                val card = cards[index]

                                MemoryCardView(
                                    card = card,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.flipCard(index)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .testTag("memory_card_$index")
                                )
                            }
                        }
                    }
                }
            }

            // Game Won Overlay
            if (isGameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SophisticatedBackground.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = "Winner",
                            tint = SophisticatedPrimary,
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = "GRID CLEAR!",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            color = SophisticatedPrimary
                        )
                        Text(
                            text = "TOTAL MOVES: $moves",
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            color = SophisticatedText
                        )
                        Button(
                            onClick = { viewModel.resetMemoryGame() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SophisticatedPrimary,
                                contentColor = SophisticatedOnPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("memory_replay_button")
                        ) {
                            Text(
                                "PLAY AGAIN",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Padding spacer at bottom
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MemoryCardView(
    card: MemoryCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 3D card flipping rotation animation
    val rotation by animateFloatAsState(
        targetValue = if (card.isFlipped || card.isMatched) 180f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
    )

    val listIcons = listOf(
        Icons.Filled.Favorite,
        Icons.Filled.Star,
        Icons.Filled.Face,
        Icons.Filled.ThumbUp,
        Icons.Filled.Build,
        Icons.Filled.ShoppingCart,
        Icons.Filled.Lock,
        Icons.Filled.Info
    )

    val colors = listOf(
        SophisticatedPrimary,
        SophisticatedSecondary,
        SophisticatedAccent,
        Color(0xFFE1BEE7),
        Color(0xFFBBDEFB),
        Color(0xFFFFCDD2),
        Color(0xFFFFF9C4),
        Color(0xFFC8E6C9)
    )

    val cardIcon = listIcons.getOrElse(card.iconIndex) { Icons.Filled.QuestionMark }
    val iconColor = colors.getOrElse(card.iconIndex) { SophisticatedPrimary }

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
            }
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = !card.isFlipped && !card.isMatched, onClick = onClick)
            .border(
                width = 1.dp,
                color = if (card.isMatched) SophisticatedSecondary.copy(alpha = 0.5f) else SophisticatedOutline,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        if (rotation <= 90f) {
            // BACK OF CARD (Classic Elegant Ornament)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SophisticatedSurface),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .border(1.dp, SophisticatedPrimary.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .background(SophisticatedPrimary.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Fingerprint,
                        contentDescription = null,
                        tint = SophisticatedPrimary.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            // FRONT OF CARD (Clean Theme Icon Symbol)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f } // Counteract mirror effect
                    .background(SophisticatedSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = cardIcon,
                    contentDescription = "Memory symbol",
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
