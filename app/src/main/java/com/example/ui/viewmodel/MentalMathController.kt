package com.example.ui.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Owns the state and game logic of the Mental Math (Soroban) challenge.
 * Extracted from [OrbitViewModel] to keep the ViewModel focused on coordination.
 */
class MentalMathController {

    private val _difficulty = MutableStateFlow(MentalMathDifficulty.BEGINNER)
    val difficulty: StateFlow<MentalMathDifficulty> = _difficulty.asStateFlow()

    private val _gameState = MutableStateFlow(MentalMathGameState.IDLE)
    val gameState: StateFlow<MentalMathGameState> = _gameState.asStateFlow()

    private val _sequence = MutableStateFlow<List<Int>>(emptyList())
    val sequence: StateFlow<List<Int>> = _sequence.asStateFlow()

    private val _currentNumber = MutableStateFlow<Int?>(null)
    val currentNumber: StateFlow<Int?> = _currentNumber.asStateFlow()

    private val _userAnswer = MutableStateFlow("")
    val userAnswer: StateFlow<String> = _userAnswer.asStateFlow()

    private val _isCorrect = MutableStateFlow<Boolean?>(null)
    val isCorrect: StateFlow<Boolean?> = _isCorrect.asStateFlow()

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()

    private val _xp = MutableStateFlow(120)
    val xp: StateFlow<Int> = _xp.asStateFlow()

    fun setDifficulty(newDifficulty: MentalMathDifficulty) {
        _difficulty.value = newDifficulty
        _gameState.value = MentalMathGameState.IDLE
    }

    fun startChallenge(scope: CoroutineScope) {
        val diff = _difficulty.value
        val seq = buildSequence(diff)
        _sequence.value = seq
        _userAnswer.value = ""
        _isCorrect.value = null
        _gameState.value = MentalMathGameState.PLAYING

        scope.launch {
            for (num in seq) {
                _currentNumber.value = num
                delay(diff.delayMs)
                _currentNumber.value = null
                delay(FLASH_GAP_MS)
            }
            _gameState.value = MentalMathGameState.ANSWERING
        }
    }

    /**
     * Generates a sequence of numbers to add/subtract mentally. The first number is
     * always positive; later numbers may be negated when it keeps the running sum
     * non-negative so the result stays child-friendly.
     */
    private fun buildSequence(diff: MentalMathDifficulty): List<Int> {
        val seq = mutableListOf<Int>()
        var runningSum = 0
        for (i in 0 until diff.count) {
            var num = Random.nextInt(diff.min, diff.max + 1)
            if (i > 0 && Random.nextBoolean() && runningSum - num >= 0) {
                num = -num
            }
            runningSum += num
            seq.add(num)
        }
        return seq
    }

    fun submitAnswer(answerStr: String) {
        val expected = _sequence.value.sum()
        val correct = answerStr.toIntOrNull() == expected
        _isCorrect.value = correct
        _gameState.value = MentalMathGameState.RESULT
        if (correct) {
            _streak.value += 1
            _xp.value += XP_PER_CORRECT * _difficulty.value.stars
        } else {
            _streak.value = 0
        }
    }

    /**
     * Called by the ViewModel when a round finishes, so the earned XP and best
     * streak are accumulated into the durable per-user achievement store.
     */
    fun recordRoundOutcome(scope: CoroutineScope, userId: String, persist: suspend (Int, Int, Int) -> Unit) {
        val earnedXp = if (_isCorrect.value == true) XP_PER_CORRECT * _difficulty.value.stars else 0
        val correct = if (_isCorrect.value == true) 1 else 0
        val streak = _streak.value
        scope.launch { persist(earnedXp, streak, correct) }
    }

    private companion object {
        const val FLASH_GAP_MS = 200L
        const val XP_PER_CORRECT = 20
    }
}
