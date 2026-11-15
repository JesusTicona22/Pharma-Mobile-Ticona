# Paso 7 — Matriz de Pruebas en Dispositivos y Registro de Defectos (Guía Práctica N.º 14)

**Proyecto:** PharmaMobil (KMP)  
**Versión probada:** `1.0.0` (`versionCode = 1`, `applicationId = pe.edu.upeu.pharmamobil`)  
**Artefacto instalable:** `androidApp/build/outputs/apk/release/androidApp-release.apk`

---

## 1. Requisitos previos para probar en teléfonos reales con PharmaSoft local

Antes de iniciar el recorrido en dispositivos físicos, ten en cuenta **la trampa de la dirección del servicio**:

- La configuración actual en `shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobil/data/remote/HttpClientFactory.kt` apunta a:
  ```
  http://10.0.2.2:8080/api/v1/
  ```
- `10.0.2.2` **solo funciona dentro del emulador de Android** (mapea al `localhost` de la PC anfitriona).
- Para ejecutar pruebas en **teléfonos Android físicos conectados a la misma red Wi-Fi**, elige una de estas dos opciones:
  1. **Por cable USB con `adb reverse` (recomendado si la app usara `127.0.0.1`/`localhost`)**: requiere que el teléfono apunte a `localhost:8080`, o bien
  2. **Usar la IP LAN de la PC** (ej. `http://192.168.1.X:8080/api/v1/`) verificando que el backend `pharmaSoft` (`docker compose up -d`) esté escuchando en el puerto `8080` y el Firewall de Windows permita conexiones entrantes al puerto `8080` (`android:usesCleartextTraffic="true"` ya está habilitado en `AndroidManifest.xml`).

---

## 2. Verificación ejecutada en emulador (Smoke Check Release / R8)

| Ítem | Detalle observado | Estado |
|---|---|---|
| Entorno | `emulator-5554` (`sdk_gphone16k_x86_64`) | Ejecutado |
| Artefacto instalado | `androidApp-release.apk` firmado con keystore `pharmamobil` (`isMinifyEnabled = true` con R8) | Instalado (`Success`) |
| Arranque de la app | `am start -n pe.edu.upeu.pharmamobil/.MainActivity` | Proceso activo (`PID 27883`), sin cierre inesperado |

---

## 3. Matriz de recorrido en dispositivos reales (2 en clase + 1 autónomo)

> Completar cada columna tras instalar `androidApp-release.apk` en los teléfonos físicos **con el entorno de desarrollo desconectado** y el backend `pharmaSoft` accesible en red local.

| N.º | Paso del recorrido | Qué se observa | Dispositivo 1 (Marca / Modelo / Android) | Dispositivo 2 (Marca / Modelo / Android) | Dispositivo 3 autónomo (Marca / Modelo / Android) |
|---|---|---|---|---|---|
| 1 | Instalación desde el archivo firmado | Instala sin IDE conectado; nombre (`PharmaMobil`) e icono son los definitivos | [ ] Pendiente | [ ] Pendiente | [ ] Pendiente |
| 2 | Inicio de sesión correcto e incorrecto | El mensaje distingue credencial inválida de falta de conexión | [ ] Pendiente | [ ] Pendiente | [ ] Pendiente |
| 3 | CRUD completo, con rechazo de validación | El mensaje del servidor llega al campo que lo originó | [ ] Pendiente | [ ] Pendiente | [ ] Pendiente |
| 4 | Operación sin el privilegio necesario | Se informa el rechazo y la sesión no se cierra | [ ] Pendiente | [ ] Pendiente | [ ] Pendiente |
| 5 | Modo avión: abrir, leer y crear un registro | La pantalla sigue mostrando datos locales y lo creado queda marcado como pendiente | [ ] Pendiente | [ ] Pendiente | [ ] Pendiente |
| 6 | Recuperar la conexión y sincronizar | Lo pendiente sube, nada se duplica y la pantalla se actualiza sola | [ ] Pendiente | [ ] Pendiente | [ ] Pendiente |
| 7 | Permiso: conceder, denegar y denegar para siempre | Los tres estados se resuelven y el tercero ofrece ir a Ajustes | [ ] Pendiente | [ ] Pendiente | [ ] Pendiente |
| 8 | Cerrar sesión y reabrir la aplicación | El token se borró y el equipo queda limpio para otro usuario | [ ] Pendiente | [ ] Pendiente | [ ] Pendiente |

---

## 4. Registro de defectos encontrados

Registrar cada hallazgo siguiendo la estructura estándar de la Guía Práctica N.º 14:

| Identificador | Dispositivo y versión | Pasos para reproducir | Resultado esperado | Resultado obtenido | Severidad (`Alta`/`Media`/`Baja`) | Estado (`Corregido`/`Pendiente`/`Descartado`) |
|---|---|---|---|---|---|---|
| D-01 *(ejemplo)* | *(Marca, modelo, Android XX)* | *(1. Abrir app... 2. ...)* | *(Comportamiento esperado)* | *(Error observado / captura)* | *(Alta / Media / Baja)* | *(Pendiente)* |
