package pe.edu.upeu.pharmamobil

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.pharmamobil.domain.model.Sesion
import pe.edu.upeu.pharmamobil.navigation.Screen
import pe.edu.upeu.pharmamobil.presentation.cliente.ClienteScreen
import pe.edu.upeu.pharmamobil.presentation.components.EstadoVacio
import pe.edu.upeu.pharmamobil.presentation.inicio.InicioScreen
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoScreen
import pe.edu.upeu.pharmamobil.presentation.sesion.LoginScreen
import pe.edu.upeu.pharmamobil.presentation.sesion.SesionViewModel
import pe.edu.upeu.pharmamobil.theme.PharmaMobilTheme
// Agrega este import arriba junto a los de Koin en App.kt:
import org.koin.compose.koinInject
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro

/** Una sola fuente para el menu lateral, el titulo de la barra superior y el permiso requerido. */
private data class Destino(
    val screen: Screen,
    val titulo: String,
    val icono: ImageVector,
    val permisoRequerido: String? = null
)

private val DESTINOS = listOf(
    Destino(Screen.Inicio, "Inicio", Icons.Default.Home, null),
    Destino(Screen.Productos, "Productos", Icons.Default.Medication, "CATALOGO_LEER"),
    Destino(Screen.Clientes, "Clientes", Icons.Default.Person, "CLIENTES_LEER"),
    Destino(Screen.Pedidos, "Pedidos", Icons.Default.ShoppingCart, "VENTAS_LEER")
)

private val ScreenSaver = Saver<Screen, Int>(
    save = { pantalla ->
        DESTINOS.indexOfFirst { it.screen == pantalla }.coerceAtLeast(0)
    },
    restore = { indice ->
        DESTINOS.getOrElse(indice) { DESTINOS.first() }.screen
    }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() = KoinContext {

    val almacenSeguro: AlmacenSeguro = koinInject()

    var pantallaActual by rememberSaveable(stateSaver = ScreenSaver) {
        mutableStateOf<Screen>(Screen.Inicio)
    }

    // Lee el tema guardado al abrir la app
    var darkTheme by rememberSaveable {
        mutableStateOf(almacenSeguro.leerModoOscuro())
    }

    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val scope = rememberCoroutineScope()

    val sesionViewModel: SesionViewModel = koinViewModel()
    val sesionUiState by sesionViewModel.uiState.collectAsStateWithLifecycle()

    PharmaMobilTheme(
        darkTheme = darkTheme
    ) {
        // Surface global: asegura que Login y todas las pantallas tengan el fondo del tema (claro u oscuro)
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val sesionActiva = sesionUiState.sesion

            when {
                sesionUiState.verificandoSesionInicial -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                sesionActiva == null -> {
                    LoginScreen(
                        uiState = sesionUiState,
                        onUsuarioChange = sesionViewModel::onUsuarioChange,
                        onClaveChange = sesionViewModel::onClaveChange,
                        onIniciarSesion = {
                            pantallaActual = Screen.Inicio
                            sesionViewModel.iniciarSesion()
                        }
                    )
                }

                else -> {
                    val destinosPermitidos = DESTINOS.filter { destino ->
                        destino.permisoRequerido == null || sesionActiva.puede(destino.permisoRequerido)
                    }

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet {
                                DrawerHeader(sesion = sesionActiva)

                                HorizontalDivider()

                                Spacer(modifier = Modifier.height(12.dp))

                                destinosPermitidos.forEach { destino ->
                                    NavigationDrawerItem(
                                        label = { Text(destino.titulo) },
                                        selected = pantallaActual == destino.screen,
                                        onClick = {
                                            pantallaActual = destino.screen
                                            scope.launch { drawerState.close() }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = destino.icono,
                                                contentDescription = null
                                            )
                                        },
                                        modifier = Modifier.padding(
                                            NavigationDrawerItemDefaults.ItemPadding
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                HorizontalDivider()

                                ModoOscuro(
                                    activo = darkTheme,
                                    onCambiar = { nuevoValor ->
                                        darkTheme = nuevoValor
                                        almacenSeguro.guardarModoOscuro(nuevoValor)
                                    }
                                )

                                NavigationDrawerItem(
                                    label = { Text("Cerrar sesión") },
                                    selected = false,
                                    onClick = {
                                        scope.launch {
                                            drawerState.close()
                                            pantallaActual = Screen.Inicio
                                            sesionViewModel.cerrarSesion()
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Logout,
                                            contentDescription = "Cerrar sesión"
                                        )
                                    },
                                    modifier = Modifier.padding(
                                        NavigationDrawerItemDefaults.ItemPadding
                                    )
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    ) {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Text(text = tituloDe(pantallaActual))
                                    },
                                    navigationIcon = {
                                        IconButton(
                                            onClick = {
                                                scope.launch { drawerState.open() }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Menu,
                                                contentDescription = "Abrir menú"
                                            )
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        ) { paddingValues ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                            ) {
                                when (pantallaActual) {
                                    Screen.Inicio ->
                                        InicioScreen(
                                            sesion = sesionActiva,
                                            onNavegar = { destino ->
                                                pantallaActual = destino
                                            }
                                        )

                                    Screen.Productos ->
                                        ProductoScreen(
                                            viewModel = koinViewModel(key = sesionActiva.usuario),
                                            puedeEscribir = sesionActiva.puede("CATALOGO_ESCRIBIR")
                                        )

                                    Screen.Clientes ->
                                        ClienteScreen(
                                            viewModel = koinViewModel(key = sesionActiva.usuario)
                                        )

                                    Screen.Pedidos ->
                                        EstadoVacio(
                                            icono = Icons.Default.ShoppingCart,
                                            titulo = "Pedidos en construcción",
                                            descripcion = "Este módulo llega en una próxima sesión del curso.",
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerHeader(sesion: Sesion) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.LocalPharmacy,
                contentDescription = null,
                modifier = Modifier
                    .padding(10.dp)
                    .size(28.dp)
            )
        }

        Column {
            Text(
                text = "PharmaMobil",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "${sesion.nombreCompleto} (${sesion.usuario})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ModoOscuro(
    activo: Boolean,
    onCambiar: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 28.dp,
                vertical = 16.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.DarkMode,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Modo oscuro",
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = activo,
            onCheckedChange = onCambiar
        )
    }
}

private fun tituloDe(
    screen: Screen
): String {
    return DESTINOS.first { it.screen == screen }.titulo
}