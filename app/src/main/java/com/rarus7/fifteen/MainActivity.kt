package com.rarus7.fifteen

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times

import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // Основной экран игры
                FifteenGameScreen()
            }
        }
    }
}

@Composable
@Preview
fun FifteenGameScreen(viewModel: GameViewModel = viewModel(factory = GameViewModelFactory(LocalContext.current.applicationContext as Application))) {
    // Собираем состояния из ViewModel
    val board by viewModel.board.collectAsState()
    val moves by viewModel.moves.collectAsState()
    val isSolved by viewModel.isSolved.collectAsState()
    val animatingTile by viewModel.animatingTile.collectAsState()
    val records by viewModel.records.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Заголовок с количеством ходов
        Text(
            text = "Moves: $moves",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        // 2. Сообщение о победе
        if (isSolved) {
            Text(
                text = "You Win! 🎉",
                color = Color.Green,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 3. Игровое поле 4x4
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            itemsIndexed(board) { index, number ->
                TileItem(
                    index = index,
                    number = number,
                    animatingTile = animatingTile,
                    onClick = { viewModel.moveTile(index) }
                )
            }
        }

        // 4. Кнопка новой игры
        Button(onClick = {
            viewModel.startNewGame()
            if (isSolved) viewModel.saveRecord(moves)
        }) {
            Text("New Game")
        }

        // Таблица рекордов
        RecordsTable(records)
    }
}

@Composable
fun TileItem(
    index: Int,
    number: Int,
    animatingTile: Pair<Int, Int>?,
    onClick: () -> Unit
) {
    // Вычисляем анимацию перемещения
    val (offsetX, offsetY) = if (animatingTile?.first == index) {
        val (from, to) = animatingTile
        val fromRow = from / 4
        val fromCol = from % 4
        val toRow = to / 4
        val toCol = to % 4

        val xAnim by animateDpAsState(
            targetValue = (toCol - fromCol) * 100.dp,
            animationSpec = tween(300),
            label = "X Animation"
        )

        val yAnim by animateDpAsState(
            targetValue = (toRow - fromRow) * 100.dp,
            animationSpec = tween(300),
            label = "Y Animation"
        )

        xAnim to yAnim
    } else {
        0.dp to 0.dp
    }

    // Отображаем плитку (кроме пустой)
    if (number != 0) {
        Box(
            modifier = Modifier
                .padding(4.dp)
                .aspectRatio(1f)
                .offset(offsetX, offsetY) // Применяем анимацию
                .background(
                    color = Color(0xFF6200EE),
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        // Пустая клетка (невидимая)
        Box(
            modifier = Modifier
                .padding(4.dp)
                .aspectRatio(1f)
        )
    }
}

@Composable
fun RecordsTable(records: List<GameRecord>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Заголовок таблицы
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Date/Time",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Text(
                "Moves",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        // Список рекордов
        LazyColumn {
            items(records) { record ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        record.dateTime,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        record.moves.toString(),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
                Divider()
            }
        }
    }
}