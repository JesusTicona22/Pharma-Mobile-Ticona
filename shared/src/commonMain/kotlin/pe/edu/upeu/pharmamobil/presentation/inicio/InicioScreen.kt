package pe.edu.upeu.pharmamobil.presentation.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import pe.edu.upeu.pharmamobil.domain.model.Sesion
import pe.edu.upeu.pharmamobil.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobil.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobil.domain.permiso.Permiso
import pe.edu.upeu.pharmamobil.navigation.Screen

/** Cada acceso rapido de la portada lleva a uno de los modulos de la app. */
private data class Opcion(
    val screen: Screen,
    val icono: ImageVector,
    val titulo: String,
    val descripcion: String,
    val permisoRequerido: String? = null
)

private val OPCIONES = listOf(
    Opcion(
        screen = Screen.Productos,
        icono = Icons.Default.Medication,
        titulo = "Catálogo de productos",
        descripcion = "Consulta medicamentos y gestiona su precio y stock.",
        permisoRequerido = "CATALOGO_LEER"
    ),
    Opcion(
        screen = Screen.Clientes,
        icono = Icons.Default.Person,
        titulo = "Registrar clientes",
        descripcion = "Guarda los datos de contacto para la boleta.",
        permisoRequerido = "CLIENTES_LEER"
    ),
    Opcion(
        screen = Screen.Pedidos,
        icono = Icons.Default.ShoppingCart,
        titulo = "Revisar pedidos",
        descripcion = "Disponible en una próxima sesión del curso.",
        permisoRequerido = "VENTAS_LEER"
    )
)

@Composable
fun InicioScreen(
    onNavegar: (Screen) -> Unit,
    sesion: Sesion? = null,
    modifier: Modifier = Modifier
) {
    val opcionesPermitidas = OPCIONES.filter { opcion ->
        opcion.permisoRequerido == null || sesion?.puede(opcion.permisoRequerido) != false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Portada()

        Text(
            text = "Qué puedes hacer",
            style = MaterialTheme.typography.titleMedium
        )

        opcionesPermitidas.forEach { opcion ->

            AccesoRapido(
                icono = opcion.icono,
                titulo = opcion.titulo,
                descripcion = opcion.descripcion,
                onClick = {
                    onNavegar(opcion.screen)
                }
            )
        }

        TarjetaPermisoNotificaciones()
    }
}


@Composable
private fun Portada() {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Icon(
                imageVector = Icons.Default.LocalPharmacy,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )

            Text(
                text = "PharmaMobil",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Sistema de gestión farmacéutica para tu cadena de boticas.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


@Composable
private fun AccesoRapido(
    icono: ImageVector,
    titulo: String,
    descripcion: String,
    onClick: () -> Unit
) {

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {

                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {

                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TarjetaPermisoNotificaciones(
    gestorPermisos: GestorPermisos = koinInject()
) {
    var estadoPermiso by remember { mutableStateOf<EstadoPermiso>(EstadoPermiso.Denegado) }
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(gestorPermisos) {
        estadoPermiso = gestorPermisos.estado(Permiso.NOTIFICACIONES)
    }

    DisposableEffect(lifecycleOwner, gestorPermisos) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    estadoPermiso = gestorPermisos.estado(Permiso.NOTIFICACIONES)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = when (estadoPermiso) {
                        EstadoPermiso.Concedido -> Icons.Default.CheckCircle
                        EstadoPermiso.Denegado -> Icons.Default.Notifications
                        EstadoPermiso.DenegadoParaSiempre -> Icons.Default.NotificationsOff
                    },
                    contentDescription = null,
                    tint = when (estadoPermiso) {
                        EstadoPermiso.Concedido -> MaterialTheme.colorScheme.primary
                        EstadoPermiso.Denegado -> MaterialTheme.colorScheme.secondary
                        EstadoPermiso.DenegadoParaSiempre -> MaterialTheme.colorScheme.error
                    }
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Notificaciones de sincronización",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = when (estadoPermiso) {
                            EstadoPermiso.Concedido -> "Estado: Concedido"
                            EstadoPermiso.Denegado -> "Estado: Denegado (se puede solicitar)"
                            EstadoPermiso.DenegadoParaSiempre -> "Estado: Denegado permanentemente"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = when (estadoPermiso) {
                    EstadoPermiso.Concedido ->
                        "Las alertas están activas para avisarte cuando se complete la sincronización o falte stock."
                    EstadoPermiso.Denegado ->
                        "Recibe alertas cuando tus cambios pendientes terminen de sincronizarse con PharmaSoft o cuando un medicamento requiera reposición."
                    EstadoPermiso.DenegadoParaSiempre ->
                        "El permiso fue bloqueado permanentemente. Para activar las alertas de sincronización debes habilitarlo manualmente desde Ajustes."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            when (estadoPermiso) {
                EstadoPermiso.Concedido -> Unit
                EstadoPermiso.Denegado -> {
                    Button(
                        onClick = {
                            scope.launch {
                                estadoPermiso = gestorPermisos.solicitar(Permiso.NOTIFICACIONES)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Activar notificaciones")
                    }
                }
                EstadoPermiso.DenegadoParaSiempre -> {
                    FilledTonalButton(
                        onClick = { gestorPermisos.abrirAjustes() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ir a Ajustes")
                    }
                }
            }
        }
    }
}