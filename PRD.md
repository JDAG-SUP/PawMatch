# PawMatch - Documento de Requisitos y Arquitectura

PawMatch es una aplicación móvil que conecta mascotas domésticas para citas de socialización, reproducción controlada o simple convivencia amigable. Los dueños crean perfiles detallados de sus animales, exploran matches basados en compatibilidad de especie, raza, edad y ubicación, y coordinan encuentros de forma segura desde la app.

## 1.1 Propuesta de Valor
•	Algoritmo de compatibilidad personalizado por especie, raza, temperamento y vacunación.
•	Chat en tiempo real entre dueños con verificación de identidad.
•	Geolocalización inteligente para sugerir matches cercanos.
•	Historial médico y de vacunación opcional en cada perfil de mascota.
•	Modo "Encuentro Seguro": guía paso a paso para coordinar citas presenciales.

## 1.2 Usuarios Objetivo
| Segmento | Motivación principal |
|---|---|
| Dueños que buscan compañía para su mascota | Socialización y bienestar animal |
| Criadores responsables | Reproducción controlada con historial verificable |
| Dueños de primera vez | Aprendizaje y comunidad de apoyo |
| Rescatistas y adoptantes | Red de contactos y recursos |

## 2. Stack Tecnológico

### 2.1 Frontend — Kotlin Multiplatform Mobile (KMM)
| Capa | Tecnología / Librería |
|---|---|
| Lenguaje compartido | Kotlin Multiplatform (KMM) |
| UI Android | Jetpack Compose |
| UI iOS | SwiftUI + módulo KMM compilado como framework |
| Navegación | Compose Navigation (Android) / NavigationStack (iOS) |
| Inyección de dependencias | Koin (shared module) |
| Networking | Ktor Client (multiplataforma) |
| Serialización | kotlinx.serialization |
| Coroutines / async | Kotlin Coroutines + Flow |
| Imágenes | Coil 3 (Compose Multiplatform) |
| Mapas | Google Maps SDK (Android) / MapKit (iOS) |
| Almacenamiento local | SQLDelight (shared) |
| Push Notifications | Firebase Cloud Messaging + APNs |

### 2.2 Backend — Supabase
| Servicio Supabase | Uso en PawMatch |
|---|---|
| PostgreSQL | Base de datos principal (usuarios, mascotas, matches, mensajes) |
| Auth | Autenticación: email/password, OAuth (Google, Apple) |
| Storage | Fotos de mascotas, avatars, documentos veterinarios |
| Realtime | Chat en tiempo real, notificaciones de match |
| Edge Functions (Deno) | Lógica de matching, validaciones complejas, webhooks |
| Row Level Security (RLS) | Control de acceso por usuario autenticado |
| PostGIS extension | Búsqueda geoespacial de mascotas cercanas |
| pg_cron | Limpieza de matches expirados, recordatorios |

## 3. Arquitectura del Sistema

### 3.1 Arquitectura General
Patrón principal: Clean Architecture + MVVM en capas compartidas KMM
Comunicación cliente-servidor: REST (Supabase PostgREST) + WebSockets (Realtime)
Seguridad: JWT tokens via Supabase Auth + RLS en base de datos
CDN: Supabase Storage con URLs firmadas para imágenes privadas

### 3.2 Capas de la Arquitectura KMM
**Capa de Presentación (Platform-specific)**
•	Android: Jetpack Compose + ViewModel (AndroidX)
•	iOS: SwiftUI + ObservableObject wrapeando ViewModel KMM
•	Cada pantalla tiene su propio ViewModel en el módulo shared

**Capa de Dominio (100% Kotlin compartido)**
•	Use Cases: un archivo por caso de uso (GetMatchesUseCase, SendMessageUseCase...)
•	Repository Interfaces: contratos definidos en dominio, implementados en data
•	Entities: modelos de negocio puros sin dependencias de framework
•	Mappers: transformación entre entidades de dominio y DTOs

**Capa de Datos (Kotlin compartido)**
•	Repositories: implementaciones concretas que deciden cache vs. red
•	Remote Data Sources: llamadas a Supabase via Ktor + Supabase Kotlin SDK
•	Local Data Sources: SQLDelight para cache offline
•	DTOs: clases de serialización para JSON de la API

### 3.3 Estructura de Módulos del Proyecto
```
pawmatch/
├── androidApp/            → App Android (Compose, Activities)
├── iosApp/                → App iOS (SwiftUI + XCFramework)
└── shared/
    ├── commonMain/
    │   ├── domain/
    │   │   ├── entities/
    │   │   ├── usecases/
    │   │   └── repositories/
    │   ├── data/
    │   │   ├── remote/    → Supabase API clients
    │   │   ├── local/     → SQLDelight
    │   │   └── repositories/
    │   ├── presentation/
    │   │   └── viewmodels/
    │   └── di/            → Koin modules
    ├── androidMain/       → Platform specifics Android
    └── iosMain/           → Platform specifics iOS
```

## 4. Esquema de Base de Datos (Supabase / PostgreSQL)

### 4.1 Tablas Principales
**users_profiles**
```sql
id              UUID PRIMARY KEY (FK → auth.users)
display_name    TEXT NOT NULL
bio             TEXT
avatar_url      TEXT
phone           TEXT
verified        BOOLEAN DEFAULT false
location        GEOGRAPHY(POINT)   -- PostGIS
created_at      TIMESTAMPTZ DEFAULT now()
updated_at      TIMESTAMPTZ DEFAULT now()
```

**pets**
```sql
id              UUID PRIMARY KEY DEFAULT gen_random_uuid()
owner_id        UUID REFERENCES users_profiles(id) ON DELETE CASCADE
name            TEXT NOT NULL
species         TEXT NOT NULL        -- dog, cat, rabbit, bird...
breed           TEXT
gender          TEXT                 -- male, female, unknown
birth_date      DATE
size            TEXT                 -- small, medium, large, giant
temperament     TEXT[]               -- playful, calm, energetic...
photos          TEXT[]               -- Storage URLs
bio             TEXT
vaccinated      BOOLEAN DEFAULT false
neutered        BOOLEAN DEFAULT false
health_notes    TEXT
looking_for     TEXT[]               -- play, breeding, adoption
is_active       BOOLEAN DEFAULT true
created_at      TIMESTAMPTZ DEFAULT now()
```

**swipes**
```sql
id              UUID PRIMARY KEY DEFAULT gen_random_uuid()
swiper_pet_id   UUID REFERENCES pets(id)
swiped_pet_id   UUID REFERENCES pets(id)
direction       TEXT NOT NULL    -- like | pass
created_at      TIMESTAMPTZ DEFAULT now()
UNIQUE(swiper_pet_id, swiped_pet_id)
```

**matches**
```sql
id              UUID PRIMARY KEY DEFAULT gen_random_uuid()
pet_a_id        UUID REFERENCES pets(id)
pet_b_id        UUID REFERENCES pets(id)
status          TEXT DEFAULT 'active'  -- active | archived | blocked
created_at      TIMESTAMPTZ DEFAULT now()
UNIQUE(pet_a_id, pet_b_id)
```

**messages**
```sql
id              UUID PRIMARY KEY DEFAULT gen_random_uuid()
match_id        UUID REFERENCES matches(id) ON DELETE CASCADE
sender_id       UUID REFERENCES users_profiles(id)
content         TEXT
media_url       TEXT
type            TEXT DEFAULT 'text'  -- text | image | location | event
read_at         TIMESTAMPTZ
created_at      TIMESTAMPTZ DEFAULT now()
```

**meetups**
```sql
id              UUID PRIMARY KEY DEFAULT gen_random_uuid()
match_id        UUID REFERENCES matches(id)
proposed_by     UUID REFERENCES users_profiles(id)
title           TEXT
scheduled_at    TIMESTAMPTZ
location_name   TEXT
location_coords GEOGRAPHY(POINT)
status          TEXT DEFAULT 'pending'  -- pending | confirmed | cancelled | completed
notes           TEXT
created_at      TIMESTAMPTZ DEFAULT now()
```

**reviews**
```sql
id              UUID PRIMARY KEY DEFAULT gen_random_uuid()
meetup_id       UUID REFERENCES meetups(id)
reviewer_id     UUID REFERENCES users_profiles(id)
reviewed_pet_id UUID REFERENCES pets(id)
rating          SMALLINT CHECK (rating BETWEEN 1 AND 5)
comment         TEXT
created_at      TIMESTAMPTZ DEFAULT now()
```

### 4.2 Vistas y Funciones SQL Clave
•	Vista: `nearby_pets` — combina pets + users_profiles + PostGIS ST_DWithin.
•	Función: `process_swipe(swiper, swiped, direction)` — inserta en swipes, detecta match mutuo y crea registro en matches.
•	Función: `get_feed_for_pet(pet_id, radius_km, limit)` — retorna candidatos excluyendo ya vistos, priorizando por compatibilidad.
•	Trigger: `on_new_match` — dispara notificación Realtime y registra en tabla notifications.

*(Consulta el prompt original para detalles completos de características y algoritmos)*
