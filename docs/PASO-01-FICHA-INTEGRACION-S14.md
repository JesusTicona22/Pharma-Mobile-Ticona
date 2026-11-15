# Paso 1 — Cierre de ficha de integración y congelación de alcance (Sesión 14)

**Proyecto:** PharmaMobil (KMP) sobre PharmaSoft  
**Rama:** `feature/builds-14`  
**Fecha de cierre:** 15 de noviembre de 2026

## 1. Cierre de la tabla de huecos de la Sesión 13

| N.º | Momento del recorrido | Hueco detectado en Sesión 13 | Decisión final (Sesión 14) | Justificación / Qué se hizo o qué faltaría |
| --- | --- | --- | --- | --- |
| 1 | Primera apertura, sin sesión | Faltaba tarjeta de permisos con explicación antes de solicitar notificaciones. | **Cerrado** | Implementada `TarjetaPermisoNotificaciones` en `InicioScreen` usando `GestorPermisos` y botón `Ir a Ajustes` (Sesión 13). |
| 2 | Inicio de sesión con credenciales incorrectas | Distinción entre credenciales inválidas y falta de conexión. | **Descartado** | Ya estaba cubierto desde la Sesión 10 en `AuthRepositorioRest` y `SesionViewModel`. |
| 3 | Listado con la base local vacía | Faltaba mostrar antigüedad de los datos o estado pendiente inicial. | **Cerrado** | `BarraEstadoSincronizacion` muestra `Actualizado hace...` o `Pendiente de primera sincronización`. |
| 4 | Creación con el modo avión activado | Cola sin intentos ni último error; riesgo de reintento infinito. | **Cerrado** | Agregadas columnas `intentos` y `ultimoError` en `Producto.sq` con límite `MAX_INTENTOS = 5` y espera exponencial. |
| 5 | Sincronización al recuperar la señal | Necesidad de tolerar fallos parciales en bajada y actualizar marca de tiempo. | **Cerrado** | `ProductoRepositorioOffline.sincronizar()` sube antes de bajar y actualiza `ultimaSync` en `SincronizacionUiState`. |
| 6 | Error de validación del servidor | Rechazos permanentes (400/403/409/422) quedaban como pendiente genérico. | **Cerrado** | Filas marcadas con badge `Falló envío`, texto `ultimoError` visible y botón `Reintentar ahora`. |
| 7 | Token vencido a mitad de un formulario | Redirección automática global ante HTTP 401 desde cualquier pantalla manteniendo borrador en memoria. | **Se declara como limitación** | Al vencer la sesión durante sincronización, la fila se conserva en SQLite marcada con `"Sesión vencida"` (sin pérdida de datos). Para cerrar la redirección global automática haría falta un bus de eventos de sesión conectado al interceptor Ktor y guardado de borradores de formulario. No se modifica arquitectura tras congelar alcance. |
| 8 | Cierre de sesión | Limpieza selectiva de registros sincronizados en SQLite al cambiar de usuario en el mismo dispositivo. | **Se declara como limitación** | `CerrarSesionUseCase` borra el token en `AlmacenSeguro`, limpia la caché del `BearerAuthProvider` y aísla los `ViewModel` con `key = sesionActiva.usuario`. Para soportar múltiples cuentas en un mismo equipo sin compartir catálogo local haría falta limpiar filas con `pendiente = 0` al cerrar sesión o añadir columna `usuario`. |
| 9 | Versionado y trazabilidad de entrega | `versionName` en Android (`1.0`) e iOS (`1.0`) no coincidían con el formato semántico `1.0.0` de entrega. | **Cerrado hoy** | Actualizado `versionName = "1.0.0"` (`versionCode = 1`) en `androidApp/build.gradle.kts` y `MARKETING_VERSION = 1.0.0` en `iosApp/Configuration/Config.xcconfig`. |

## 2. Declaración de alcance congelado

A partir de este paso queda **congelado el alcance** de **PharmaMobil v1.0.0 (`versionCode = 1`)**:
- No se agregan nuevas pantallas ni funcionalidades fuera del alcance (Pedidos permanece como módulo en construcción).
- No se realizan cambios de arquitectura, librerías ni migraciones del esquema SQLDelight.
- Cualquier cambio posterior invalidaría las pruebas de entrega sobre el APK/AAB firmado.
