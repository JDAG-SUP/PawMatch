# 🐕 PawMatch

PawMatch es una aplicación móvil nativa (Android/iOS) impulsada por **Kotlin Multiplatform Mobile (KMM)** y **Supabase** diseñada para ayudar a que los dueños de mascotas conecten a sus compañeros peludos mediante un ecosistema lúdico, dinámico y amigable inspirado en el "Tinder para Mascotas".

## 🚀 Arquitectura y Tecnologías
La base integral del proyecto se ha construido bajo la filosofía de Clean Architecture (Dominio, Capa de Datos, y Presentación compartida):
- **Core Compartido (Shared)**: Uso de _Kotlin Multiplatform_ para escribir el 100% de la lógica de negocio (ViewModels, Repositorios, APIs y Networking) de modo que tanto Android como iOS compartan el mismo cerebro.
- **Backend / BaaS**: Supabase. Maneja PostgreSQL (Roles, Row Level Security - RLS), Subidas (Storage), y Eventos de Mensajería Push y Chat asíncrono vía `Supabase Realtime SDK`.
- **Inyección de Dependencias**: Koin.
- **Interfaz Android**: Jetpack Compose Nativo.
- **Interfaz iOS** (Lógica preparada, interfaz gráfica pronta a implementarse vía SwiftUI).

## ✨ Características (MVP)
1. **Autenticación en la nube**: Inicio de sesión clásico verificado y atado internamente a los esquemas de bases de datos restringiendo visualizaciones gracias al RLS.
2. **Onboarding Activo**: Alta de dueños y mascotas detalladas (Razas, Especies, etc)
3. **Feed Dinámico (Discover)**: Mecánica fluida custom con animaciones `Jetpack Compose` que permite desechar o dar 'Me Gusta' a otros cachorros.
4. **Smart Matchmaking**: El backend empareja usuarios inteligentemente mediando SQL Triggers con verificación de 'Mutuo Acuerdo'.
5. **Chat Realtime**: Hilo de mensajería asíncrona soportando comunicación instantaénea sin *Pulling* gracias a Websockets (Sockets Postgres de Supabase).

## 🛠️ Cómo Iniciar
El repositorio utiliza el entorno moderno KMM con `Gradle Version Catalogs`.

### Android
1. Necesitas configurar tu entorno local con `Java 17` y **Android Studio**.
2. Clona el proyecto y ábrelo en Android Studio. El IDE localizará automáticamente el SDK adecuado y configurará `local.properties`.
3. Selecciona la variante `:androidApp` y compila usando `assembleDebug` o corriendo el emulador.

> *Nota: Asegúrate de tener inyectados o provistos en tu sistema las URLs del Endpoints reales de Supabase (`YOUR_SUPABASE_URL` y las llaves anónimas) dentro del `AppModule.kt` o como variables de contorno.*