package com.rarus7.fifteen

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GameViewModel(application: Application) : AndroidViewModel(application){

    private val context = getApplication<Application>().applicationContext
    private val _board = MutableStateFlow<List<Int>>(emptyList())
    val board = _board.asStateFlow()

    private val _moves = MutableStateFlow(0)
    val moves = _moves.asStateFlow()

    private val _isSolved = MutableStateFlow(false)
    val isSolved = _isSolved.asStateFlow()

    private val _records = MutableStateFlow<List<GameRecord>>(emptyList())
    val records = _records.asStateFlow()

    init {
        startNewGame()
        loadRecords()
    }

    fun startNewGame() {
        _moves.value = 0
        _isSolved.value = false

        // Создаем список чисел от 1 до 15 + 0 (пустая клетка)
        val numbers = (1..15).toMutableList().apply { add(0) }

        // Перемешиваем, пока не получим решаемую комбинацию
        do {
            numbers.shuffle()
        } while (!isSolvable(numbers))

        _board.value = numbers
    }

    private fun isSolvable(list: List<Int>): Boolean {
        // Алгоритм проверки решаемости (можно найти в интернете)
        return true // Упрощенная версия (без проверки)
    }

    private var _lastMoveFrom = MutableStateFlow(-1)
    private var _lastMoveTo = MutableStateFlow(-1)

    val lastMoveFrom = _lastMoveFrom.asStateFlow()
    val lastMoveTo = _lastMoveTo.asStateFlow()

    // Добавляем состояние для анимации
    private val _animatingTile = MutableStateFlow<Pair<Int, Int>?>(null) // from -> to
    val animatingTile = _animatingTile.asStateFlow()

    fun moveTile(index: Int) {
        if (_isSolved.value) return

        val emptyIndex = _board.value.indexOf(0)
        if (canMove(index, emptyIndex)) {
            // 1. Сначала обновляем модель
            val newBoard = _board.value.toMutableList()
            newBoard.swap(index, emptyIndex)
            _board.value = newBoard

            // 2. Запускаем анимацию
            _animatingTile.value = index to emptyIndex

            // 3. Сбрасываем анимацию после завершения
            viewModelScope.launch {
                delay(500) // Должно совпадать с длительностью анимации
                _animatingTile.value = null
            }

            _moves.value++

            if (isPuzzleSolved(newBoard)) {
                _isSolved.value = true
            }
/*
            _lastMoveFrom.value = index
            _lastMoveTo.value = emptyIndex

            //val newBoard = _board.value.toMutableList()
            newBoard.swap(index, emptyIndex)
            _board.value = newBoard
            _moves.value++

            // Проверяем, решена ли головоломка
            if (isPuzzleSolved(newBoard)) {
                _isSolved.value = true
            }*/
        }
    }

    private fun canMove(index: Int, emptyIndex: Int): Boolean {
        val row = index / 4
        val col = index % 4
        val emptyRow = emptyIndex / 4
        val emptyCol = emptyIndex % 4

        return (row == emptyRow && Math.abs(col - emptyCol) == 1) ||
                (col == emptyCol && Math.abs(row - emptyRow) == 1)
    }

    private fun isPuzzleSolved(board: List<Int>): Boolean {
        for (i in 0..14) {
            if (board[i] != i + 1) return false
        }
        return board.last() == 0
    }

    private fun <T> MutableList<T>.swap(i: Int, j: Int) {
        val temp = this[i]
        this[i] = this[j]
        this[j] = temp
    }

    fun saveRecord(moves: Int) {
        viewModelScope.launch {
            val dateTime = SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault())
                .format(Date())

            val newRecord = GameRecord(dateTime, moves)

            // 3. Обновляем список рекордов
            val updatedRecords = (_records.value + newRecord)
                .sortedBy { it.moves }
                .take(5)

            _records.value = updatedRecords

            // 4. Сохраняем в SharedPreferences
            val prefs = context.getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
            val recordsJson = Json.encodeToString(updatedRecords)
            prefs.edit().putString("records", recordsJson).apply()
        }
    }

    private fun loadRecords() {
        viewModelScope.launch {
            val prefs = context.getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
            val recordsJson = prefs.getString("records", null)
            _records.value = recordsJson?.let {
                Json.decodeFromString<List<GameRecord>>(it)
            } ?: emptyList()
        }
    }

}