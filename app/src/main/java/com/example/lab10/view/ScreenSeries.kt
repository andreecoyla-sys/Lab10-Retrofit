package com.example.lab10.view

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.lab10.data.SerieApiService
import com.example.lab10.data.SerieModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ContenidoSeriesListado(navController: NavHostController, servicio: SerieApiService) {
    val listaSeries = remember { mutableStateListOf<SerieModel>() }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val listado = servicio.selectSeries()
                withContext(Dispatchers.Main) {
                    listaSeries.clear()
                    listaSeries.addAll(listado)
                }
                Log.d("API-SUCCESS", "Series cargadas exitosamente: ${listado.size}")
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    Log.e("API-ERROR", "Error real al cargar series: ${e.message}")
                }
            }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "ID", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.15f))
                Text(text = "SERIE", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.55f))
                Text(text = "Acción", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.3f))
            }
        }
        items(listaSeries) { item ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${item.id ?: ""}", fontSize = 16.sp, modifier = Modifier.weight(0.15f))
                Text(text = item.name, fontSize = 16.sp, modifier = Modifier.weight(0.55f))
                Row(modifier = Modifier.weight(0.3f)) {
                    IconButton(onClick = { navController.navigate("serieVer/${item.id}") }) {
                        Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = { navController.navigate("serieDel/${item.id}") }) {
                        Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Eliminar")
                    }
                }
            }
        }
    }
}

@Composable
fun ContenidoSerieEditar(navController: NavHostController, servicio: SerieApiService, pid: Int = 0) {
    var id by remember { mutableStateOf(pid) }
    var name by remember { mutableStateOf("") }
    var release_date by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    if (id != 0) {
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                try {
                    val objSerie = servicio.selectSerie(id.toString())
                    if (objSerie.isSuccessful) {
                        objSerie.body()?.let {
                            withContext(Dispatchers.Main) {
                                name = it.name
                                release_date = it.release_date ?: ""
                                rating = (it.rating ?: 0).toString()
                                category = it.category ?: ""
                            }
                        }
                    } else {
                        Log.e("API-ERROR", "Error al obtener serie: ${objSerie.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    Log.e("API-ERROR", "Error al obtener serie: ${e.message}")
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = if (id == 0) "Nuevo" else id.toString(),
            onValueChange = { },
            label = { Text("ID") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        TextField(
            value = release_date,
            onValueChange = { release_date = it },
            label = { Text("Release Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        TextField(
            value = rating,
            onValueChange = { rating = it },
            label = { Text("Rating") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        TextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    try {
                        val ratingInt = rating.toIntOrNull() ?: 0
                        val res = if (id == 0) {
                            val nuevaSerie = SerieModel(
                                id = null,
                                name = name,
                                release_date = release_date.ifBlank { null },
                                rating = ratingInt,
                                category = category.ifBlank { null }
                            )
                            servicio.insertSerie(nuevaSerie)
                        } else {
                            val serieEditada = SerieModel(
                                id = id,
                                name = name,
                                release_date = release_date.ifBlank { null },
                                rating = ratingInt,
                                category = category.ifBlank { null }
                            )
                            servicio.updateSerie(id.toString(), serieEditada)
                        }

                        if (res.isSuccessful) {
                            Log.d("API-SUCCESS", "Serie grabada exitosamente")
                            withContext(Dispatchers.Main) {
                                navController.popBackStack()
                                navController.navigate("series")
                            }
                        } else {
                            val errorBody = res.errorBody()?.string()
                            Log.e("API-ERROR", "Error al grabar serie (${res.code()}): $errorBody")
                        }
                    } catch (e: Exception) {
                        Log.e("API-ERROR", "Error en Grabar: ${e.message}", e)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Grabar", fontSize = 16.sp)
        }
    }
}

@Composable
fun ContenidoSerieEliminar(navController: NavHostController, servicio: SerieApiService, id: Int) {
    var showDialog by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Confirmar Eliminación") },
            text = { Text("¿Está seguro de eliminar la Serie?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        scope.launch(Dispatchers.IO) {
                            try {
                                val res = servicio.deleteSerie(id.toString())
                                if (res.isSuccessful) {
                                    withContext(Dispatchers.Main) {
                                        navController.popBackStack()
                                        navController.navigate("series")
                                    }
                                } else {
                                    Log.e("API-ERROR", "Error al eliminar: ${res.errorBody()?.string()}")
                                }
                            } catch (e: Exception) {
                                Log.e("API-ERROR", "Error al eliminar: ${e.message}")
                            }
                        }
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDialog = false
                        navController.navigate("series")
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}