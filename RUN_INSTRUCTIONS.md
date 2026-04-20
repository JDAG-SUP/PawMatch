# Guía Rápida para Compilar y Ejecutar PawMatch 🚀

Este documento contiene las instrucciones precisas para correr el proyecto **PawMatch** en tu computadora. Dado el stack de tecnologías nativas y en la nube (Android Nativo, Kotlin, Jetpack Compose y Firebase), es necesario comprobar ciertos parámetros en Android Studio.

---

## 💻 1. Requisitos Previos Generales

Antes de comenzar, asegúrate de cumplir con los siguientes requisitos en tu máquina:
- **Android Studio** instalado (preferiblemente la versión más reciente "Jellyfish" o superior).
- **Emulador de Android** configurado (Pixel 6 u 8 con Google Play services activados) **o** un dispositivo físico conectado vía depuración USB.
- **Conexión a Internet** activa para que Gradle pueda resolver y descargar todas las librerías (`libs.versions.toml`).

---

## ⚙️ 2. Importación y Sincronización

1. Abre **Android Studio**.
2. Selecciona **"Open"** y navega hasta la carpeta raíz del proyecto (la carpeta principal que contiene el archivo `settings.gradle.kts` y la subcarpeta `app/`).
3. Al abrirlo, Android Studio mostrará un mensaje en la parte inferior o derecha diciendo **"Gradle project sync in progress..."**. 
    - Deja que termine. Esto descargará Firebase, Coil, Jetpack Compose y todo el SDK.
    - Si te aparece alguna recomendación de "Migrate to Gradle Daemon toolchain", asegúrate de aceptarla para evitar problemas de compatibilidad con Java.

---

## 🔥 3. Comprobación Crítica de Firebase

Para que la aplicación compile correctamente y no arroje errores instantáneos (Crashes) al abrir por falta de llaves de Google, **DEBES** confirmar lo siguiente:

1. **El archivo `google-services.json`:**
   Navega a la estructura de archivos en la barra lateral izquierda seleccionando la vista "Project". Despliega tu carpeta principal y abre la capeta `app/`. Dentro de esta debe estar el archivo llamado exactamente `google-services.json`. *(Si no está ahí, debes entrar a tu Consola de Firebase en la web, ir a la configuración de la App Android que creamos y descargarlo hacia ese destino).*
2. **Métodos de Autenticación Activos:**
   Dado que la aplicación arranca en la pantalla `AuthScreen` y permite a un usuario registrarse con correo, debes asegurarte que en la nube esto sea válido. Entra a tu [Consola de Firebase](https://console.firebase.google.com/), entra al proyecto "PawMatch" -> **Authentication** -> **Sign-in method**, y enciende la palanca de "Correo y contraseña".
3. **Reglas de Base de Datos Open (Modo de prueba):**
   Para poder leer información en tu fase inicial MVP de la colección `users`, `pets` y `matches`, asegúrate que las reglas de `Firestore Database` permitan la lectura y escritura. Idealmente de esta forma en Firebase:
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /{document=**} {
         allow read, write: if true; // Cambiar a 'if request.auth != null' al lanzar a producción.
       }
     }
   }
   ```

---

## ▶️ 4. Ejecución del Proyecto

1. Una vez el elefante de Gradle en la esquina superior derecha termine de sincronizar y no tengas errores rojos en la consola de abajo (Build).
2. Selecciona tu emulador o dispositivo físico en la lista superior de dispositivos central.
3. Haz clic en el botón verde con el icono de Play **(Run 'app')** o presiona `Shift + F10`.
4. El proceso de *Build Running...* tardará un poco la primera vez. Una vez listo, debe lanzarse automáticamente la pantalla inicial con tonos crema e integrando el título "PawMatch 🐾" en tu dispositivo.
