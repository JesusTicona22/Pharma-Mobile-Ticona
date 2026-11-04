package pe.edu.upeu.pharmamobil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.koin.android.ext.android.inject
import pe.edu.upeu.pharmamobil.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobil.permiso.GestorPermisosAndroid
import pe.edu.upeu.pharmamobil.permiso.PuentePermisos

class MainActivity : ComponentActivity() {

    // Inyectamos el gestor creado por Koin
    private val gestorPermisos: GestorPermisos by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Conectamos el puente a la Activity para que pueda lanzar el diálogo
        (gestorPermisos as? GestorPermisosAndroid)?.puente = PuentePermisos(this)

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}