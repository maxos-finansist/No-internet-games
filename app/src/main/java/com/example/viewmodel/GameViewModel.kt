package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GameDatabase
import com.example.data.GameRepository
import com.example.data.GameScore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

// --- Game Navigation States ---
enum class ActiveGame {
    NONE, SNAKE, TIC_TAC_TOE, MEMORY
}

// --- Snake Game Components ---
data class GridPoint(val x: Int, val y: Int)

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

// --- Memory Game Components ---
data class MemoryCard(
    val id: Int,
    val iconIndex: Int,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository
    
    // Database scores and high scores
    val allScores: StateFlow<List<GameScore>>
    val snakeHighScore: StateFlow<Int>
    val ticTacToeHighScore: StateFlow<Int>
    val memoryHighScore: StateFlow<Int>

    init {
        val database = GameDatabase.getDatabase(application)
        repository = GameRepository(database.gameScoreDao())
        
        allScores = repository.getAllScores().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
        snakeHighScore = repository.getHighScore("SNAKE").stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
        
        ticTacToeHighScore = repository.getHighScore("TIC_TAC_TOE").stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
        
        memoryHighScore = repository.getHighScore("MEMORY").stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
    }

    // --- Active Screen/Game State ---
    private val _activeGame = MutableStateFlow(ActiveGame.NONE)
    val activeGame: StateFlow<ActiveGame> = _activeGame.asStateFlow()

    fun selectGame(game: ActiveGame) {
        _activeGame.value = game
        when (game) {
            ActiveGame.SNAKE -> resetSnakeGame()
            ActiveGame.TIC_TAC_TOE -> resetTicTacToeGame()
            ActiveGame.MEMORY -> resetMemoryGame()
            ActiveGame.NONE -> {
                stopSnakeLoop()
            }
        }
    }

    // --- SNAKE GAME LOGIC ---
    private var snakeJob: Job? = null
    
    // Grid settings
    val snakeGridSize = 20

    // Snake states
    var snakeBody by mutableStateOf(listOf(GridPoint(10, 10), GridPoint(10, 11), GridPoint(10, 12)))
        private set
    var snakeFood by mutableStateOf(GridPoint(5, 5))
        private set
    var snakeDirection by mutableStateOf(Direction.UP)
        private set
    var isSnakeGameOver by mutableStateOf(false)
        private set
    var isSnakePaused by mutableStateOf(false)
        private set
    var snakeScore by mutableStateOf(0)
        private set

    fun changeSnakeDirection(newDirection: Direction) {
        // Prevent 180-degree immediate turns
        if (isSnakePaused || isSnakeGameOver) return
        val isValid = when (newDirection) {
            Direction.UP -> snakeDirection != Direction.DOWN
            Direction.DOWN -> snakeDirection != Direction.UP
            Direction.LEFT -> snakeDirection != Direction.RIGHT
            Direction.RIGHT -> snakeDirection != Direction.LEFT
        }
        if (isValid) {
            snakeDirection = newDirection
        }
    }

    fun toggleSnakePause() {
        if (isSnakeGameOver) return
        isSnakePaused = !isSnakePaused
        if (!isSnakePaused) {
            startSnakeLoop()
        } else {
            stopSnakeLoop()
        }
    }

    fun resetSnakeGame() {
        stopSnakeLoop()
        snakeBody = listOf(GridPoint(10, 10), GridPoint(10, 11), GridPoint(10, 12))
        snakeDirection = Direction.UP
        snakeScore = 0
        isSnakeGameOver = false
        isSnakePaused = false
        spawnSnakeFood()
        startSnakeLoop()
    }

    private fun spawnSnakeFood() {
        var newFood: GridPoint
        do {
            newFood = GridPoint(
                Random.nextInt(snakeGridSize),
                Random.nextInt(snakeGridSize)
            )
        } while (snakeBody.contains(newFood))
        snakeFood = newFood
    }

    private fun startSnakeLoop() {
        stopSnakeLoop()
        snakeJob = viewModelScope.launch {
            while (!isSnakeGameOver && !isSnakePaused) {
                // Tick interval speed scales up slightly with higher scores
                val delayTime = (150 - (snakeScore / 5) * 5).coerceAtLeast(80).toLong()
                delay(delayTime)
                moveSnake()
            }
        }
    }

    private fun stopSnakeLoop() {
        snakeJob?.cancel()
        snakeJob = null
    }

    private fun moveSnake() {
        if (isSnakeGameOver || isSnakePaused) return

        val head = snakeBody.first()
        val newHead = when (snakeDirection) {
            Direction.UP -> GridPoint(head.x, head.y - 1)
            Direction.DOWN -> GridPoint(head.x, head.y + 1)
            Direction.LEFT -> GridPoint(head.x - 1, head.y)
            Direction.RIGHT -> GridPoint(head.x + 1, head.y)
        }

        // Check Wall Collision (Solid wall)
        if (newHead.x < 0 || newHead.x >= snakeGridSize || newHead.y < 0 || newHead.y >= snakeGridSize) {
            onSnakeGameOver()
            return
        }

        // Check Self Collision
        if (snakeBody.contains(newHead)) {
            onSnakeGameOver()
            return
        }

        // Move head
        val newBody = mutableListOf(newHead)
        newBody.addAll(snakeBody)

        // Check Food Consumption
        if (newHead == snakeFood) {
            snakeScore += 10
            spawnSnakeFood()
        } else {
            newBody.removeAt(newBody.lastIndex)
        }

        snakeBody = newBody
    }

    private fun onSnakeGameOver() {
        isSnakeGameOver = true
        stopSnakeLoop()
        if (snakeScore > 0) {
            viewModelScope.launch {
                repository.insertScore("SNAKE", snakeScore)
            }
        }
    }


    // --- TIC-TAC-TOE LOGIC ---
    var tttBoard by mutableStateOf(List(9) { "" })
        private set
    var tttCurrentPlayer by mutableStateOf("X") // "X" always goes first
        private set
    var tttIsSinglePlayer by mutableStateOf(true) // vs smart AI by default
        private set
    var tttWinner by mutableStateOf<String?>(null) // "X", "O", "DRAW", or null
        private set
    var tttWinningLine by mutableStateOf<List<Int>?>(null)
        private set
    var tttXWins by mutableStateOf(0)
        private set
    var tttOWins by mutableStateOf(0)
        private set
    var tttDraws by mutableStateOf(0)
        private set

    fun changeTttGameMode(singlePlayer: Boolean) {
        tttIsSinglePlayer = singlePlayer
        resetTicTacToeGame()
        tttXWins = 0
        tttOWins = 0
        tttDraws = 0
    }

    fun resetTicTacToeGame() {
        tttBoard = List(9) { "" }
        tttCurrentPlayer = "X"
        tttWinner = null
        tttWinningLine = null
    }

    fun playTttMove(index: Int) {
        if (tttBoard[index] != "" || tttWinner != null) return

        // Player Move
        val newBoard = tttBoard.toMutableList()
        newBoard[index] = tttCurrentPlayer
        tttBoard = newBoard

        if (checkTttWinner()) return

        // Switch Turn
        tttCurrentPlayer = if (tttCurrentPlayer == "X") "O" else "X"

        // Trigger AI if Single Player and AI Turn ("O" is AI)
        if (tttIsSinglePlayer && tttCurrentPlayer == "O" && tttWinner == null) {
            viewModelScope.launch {
                delay(400) // Realistic delay for AI "thinking"
                playTttAiMove()
            }
        }
    }

    private fun playTttAiMove() {
        val bestMove = getTttBestMove()
        if (bestMove != -1) {
            val newBoard = tttBoard.toMutableList()
            newBoard[bestMove] = "O"
            tttBoard = newBoard
            
            if (checkTttWinner()) return
            tttCurrentPlayer = "X"
        }
    }

    // Minimax / Smart AI decision
    private fun getTttBestMove(): Int {
        // 1. Win if possible
        for (i in 0..8) {
            if (tttBoard[i] == "") {
                val testBoard = tttBoard.toMutableList()
                testBoard[i] = "O"
                if (checkWinCondition(testBoard) != null) return i
            }
        }

        // 2. Block player win
        for (i in 0..8) {
            if (tttBoard[i] == "") {
                val testBoard = tttBoard.toMutableList()
                testBoard[i] = "X"
                if (checkWinCondition(testBoard) != null) return i
            }
        }

        // 3. Play center if empty
        if (tttBoard[4] == "") return 4

        // 4. Play opposite corner or random empty corner
        val corners = listOf(0, 2, 6, 8)
        val emptyCorners = corners.filter { tttBoard[it] == "" }
        if (emptyCorners.isNotEmpty()) {
            return emptyCorners.random()
        }

        // 5. Play random side
        val sides = listOf(1, 3, 5, 7)
        val emptySides = sides.filter { tttBoard[it] == "" }
        if (emptySides.isNotEmpty()) {
            return emptySides.random()
        }

        return -1
    }

    private fun checkTttWinner(): Boolean {
        val result = checkWinCondition(tttBoard)
        if (result != null) {
            tttWinner = result.first
            tttWinningLine = result.second
            if (tttWinner == "X") {
                tttXWins++
                // Insert score (10 points for player win vs AI)
                if (tttIsSinglePlayer) {
                    viewModelScope.launch {
                        repository.insertScore("TIC_TAC_TOE", 10)
                    }
                }
            } else if (tttWinner == "O") {
                tttOWins++
            }
            return true
        }

        // Check for Draw
        if (tttBoard.none { it == "" }) {
            tttWinner = "DRAW"
            tttDraws++
            return true
        }

        return false
    }

    private fun checkWinCondition(board: List<String>): Pair<String, List<Int>>? {
        val winPatterns = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Columns
            listOf(0, 4, 8), listOf(2, 4, 6)                  // Diagonals
        )

        for (pattern in winPatterns) {
            if (board[pattern[0]] != "" &&
                board[pattern[0]] == board[pattern[1]] &&
                board[pattern[0]] == board[pattern[2]]
            ) {
                return Pair(board[pattern[0]], pattern)
            }
        }
        return null
    }


    // --- CYBER MEMORY MATCH LOGIC ---
    var memoryCards by mutableStateOf<List<MemoryCard>>(emptyList())
        private set
    var memoryMoves by mutableStateOf(0)
        private set
    var isMemoryGameOver by mutableStateOf(false)
        private set
    var memoryMatchedPairs by mutableStateOf(0)
        private set

    // Control matching flow
    private var firstFlippedIndex: Int? = null
    private var isEvaluatingMatch = false

    fun resetMemoryGame() {
        // Pairs of indices (0 to 7) representing 8 unique icons
        val iconIndices = (0..7).flatMap { listOf(it, it) }.shuffled()
        memoryCards = iconIndices.mapIndexed { index, iconId ->
            MemoryCard(id = index, iconIndex = iconId)
        }
        memoryMoves = 0
        memoryMatchedPairs = 0
        isMemoryGameOver = false
        firstFlippedIndex = null
        isEvaluatingMatch = false
    }

    fun flipCard(index: Int) {
        if (isEvaluatingMatch) return
        val card = memoryCards[index]
        if (card.isFlipped || card.isMatched) return

        // Flip selected card
        val newCards = memoryCards.toMutableList()
        newCards[index] = card.copy(isFlipped = true)
        memoryCards = newCards

        val firstIndex = firstFlippedIndex
        if (firstIndex == null) {
            // First card of pair
            firstFlippedIndex = index
        } else {
            // Second card of pair
            memoryMoves++
            firstFlippedIndex = null
            isEvaluatingMatch = true

            val firstCard = memoryCards[firstIndex]
            val secondCard = memoryCards[index]

            if (firstCard.iconIndex == secondCard.iconIndex) {
                // Match Found!
                viewModelScope.launch {
                    delay(400)
                    val matchedCards = memoryCards.toMutableList()
                    matchedCards[firstIndex] = firstCard.copy(isMatched = true)
                    matchedCards[index] = secondCard.copy(isMatched = true)
                    memoryCards = matchedCards
                    memoryMatchedPairs++
                    isEvaluatingMatch = false
                    
                    if (memoryMatchedPairs == 8) {
                        onMemoryGameWon()
                    }
                }
            } else {
                // No Match - Flip back after delay
                viewModelScope.launch {
                    delay(800)
                    val resetCards = memoryCards.toMutableList()
                    resetCards[firstIndex] = firstCard.copy(isFlipped = false)
                    resetCards[index] = secondCard.copy(isFlipped = false)
                    memoryCards = resetCards
                    isEvaluatingMatch = false
                }
            }
        }
    }

    private fun onMemoryGameWon() {
        isMemoryGameOver = true
        // Calculate dynamic retro score: 100 base score, minus 2 for each extra move, floor at 10
        val finalScore = (100 - (memoryMoves - 8) * 4).coerceIn(10, 100)
        viewModelScope.launch {
            repository.insertScore("MEMORY", finalScore)
        }
    }

    // --- GENERAL LEADERBOARD MANAGEMENT ---
    fun clearDatabaseScores() {
        viewModelScope.launch {
            repository.clearAllScores()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopSnakeLoop()
    }
}
