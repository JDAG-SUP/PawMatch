# Documento de Progreso y Estado de Desarrollo (PawMatch)

Este documento detalla rigurosamente el estatus del Programa (basado en el PRD Original "PawMatch"), definiendo lo que se completó de manera íntegra, las secciones parcialmente logradas, elementos pospuestos o pendientes y el mapa a futuro.

---

## 🟢 Etapas Completadas o Parcialmente Completadas

### Fase F01: Autenticación y Seguridad
- **Estado**: Completada.
- **Implementado**: 
  - SDK de Supabase Auth funcional en la capa KMM para Email y Password.
  - Gestión de tokens/JWT y estado intermitente (Guardado de Sesión local) administrados en el `AuthViewModel`.
  - Pantallas de Login/Registro nativas en Compose con ruteo de seguridad (`AppNavigation.kt`); impidiendo ir al Dashboard sin cuenta activa.
- **Faltante/Para Futuro**: El PRD mencionaba acceso rápido por Redes Sociales (Google / Apple OAuth2). Se acordó mutuamente posponerlo para favorecer el MVP Core usando sólo credenciales directas.

### Fase F02: Gestión de Perfiles y Mascotas (Onboarding)
- **Estado**: Parcialmente Completado.
- **Implementado**:
  - Modelo de dominio relacional (`UserProfile` y `Pet`) con mapeos DTO.
  - Pantallas dinámicas animadas para crear la Biografía (Ajustes de Perfil) y asignar dueños.
  - Botón de listado local (Mi Perfil) con capacidad de destruir (Delete) los historiales e inventarios de mascotas in-situ.
- **Parcial / Pospuesto**: Las fotos de las mascotas. Aunque el repositorio compartido de Kotlin y los Scripts en DB de Supabase ya traen los Buckets de `Storage` activados y listos (`uploadPetPhoto`), pospusimos la incrustación gráfica del Selector de Galerías nativo (*Image Picker*) porque excede la inmediatez de un MVP y requiere permisos de Intents/FileManagers en Android y iOS independientemente.

### Fase F03: Descubrimiento y Feed
- **Estado**: Completada.
- **Implementado**:
  - Mecanismo en memoria de UI (Jetpack Compose Custom) para efectuar el *Swipe* hacia izquierda y derecha rotando cartas animadamente, todo bajo enfoques de Cero-Dependencias externas (solo Corrutinas).
  - Listas exclusivas: El `SupabasePetDataSource` filtra tus propias interacciones y esconde a tus perros para evitar el 'AutoSwipe'.
- **Para Futuro**: Mejoramiento del Query en BD usando las directivas espaciales (PostGIS) instaladas para filtrar no solo por novedad, sino por una coordenada de distancia concéntrica al dispositivo.

### Fase F04: Matcheo (Mutuo Acuerdo)
- **Estado**: Completada.
- **Implementado**:
  - SQL Triggers de alta seguridad directamente en el Postgrest. Funciona en Background comparando el historial si Firulais le dio like a Otto, registrándolo en la tabla `Matches`.
  - Escucha de notificaciones push a través del Suscriptor Websocket para desplegar matches locales al instante de recibirlos.
  - Tab nativa inferior (Bottom Bar) de listado dinámico de Matches en Android.

### Fase F05: Mensajería del Chat Room
- **Estado**: 🟡 Parcialmente Completado.
- **Implementado**:
  - Sala Virtual de Chat en tiempo real. 
  - Reglas RLS en SQL para blindar por seguridad quién lee el chat (solo los humanos partícipes pueden leer o ver esas filas).
  - Un ViewModel robusto extrayendo simultáneamente el historial pasivo de Cloud y escuchando activamente del Socket.
- **Parcial / Pospuesto**: El diseño actual UI solo permite enviar "Strings" (Texto). Posibilidad de escalar para incrustar localizaciones, GIFS o fotos nativas debido a que la Tabla `messages` aguanta Enum Type = `Image`. Marca de fecha (*Timestamps* y "Leído") en las burbujas ignorada visualmente por rápidez en Compose.

---

## 🔴 Etapas Faltantes y de Futuro (Backlog Activo)

Estas áreas están completamente vírgenes y deben orillarse como nuevos "Sprints" en los tableros del proyecto.

1. **Cliente iOS Independiente (SwiftUI / Darwin)**: Toda la capa KMM proveé `ViewModels` listos. Un desarrollador de Swift nativo únicamente necesita escribir la capa de Vista gráfica y observar los Estados emitidos por nuestra gran capa "shared".
2. **Subida Real Nativa de Storage Files**: Diseñar el módulo que interactúe con los permisos de Camara API o Fotos en ambos sistemas operativos para enviar convertidos en ByteArrays hacia las carpetas de Perros creadas.
3. **Módulo de Encuentros (F06)**: Las tablas SQL ya aguardan su uso (`meetups`, `reviews`). Hay que montar los `ViewModels` para crear las invitaciones y las ventanas de calendario de agenda para concretar citas presenciales entre las mascotas y los likes.
4. **Cache & Persistencia Offline**: Incorporar una base de datos local sólida como SQLDelight (usada habitualmente junto a KMM) para almacenar los mensajes instantáneos en la memoria física del celular, previniendo cargas extra hacia Supabase y permitiendo Chat "Offline-first".
