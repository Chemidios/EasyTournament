# EasyTournament
Proyecto Final de Desarrollo de Aplicaciones Multiplataforma.

## Funcionalidades
- **Gestión de Torneos:** Creación, edición y filtrado de eventos por categoría (Videojuegos, Deportes, etc.).
- **Sistema de Inscripciones:** Control automático de plazas con cierre de inscripciones al alcanzar el máximo de participantes.
- **Perfiles de Usuario:** Sincronización automática de perfiles con metadatos (Steam ID, Riot ID) y sistema de reputación.
- **Valoraciones y Reseñas:** Sistema de feedback post-torneo con cálculo de media aritmética automatizado.
- **Cumplimiento Legal (RGPD):** Proceso de registro con aceptación de términos y políticas de privacidad integradas.

## Tecnologías
- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Backend (BaaS):** Supabase (PostgreSQL, Auth, Postgrest).
- **Red y Serialización:** Ktor Client y KotlinX Serialization.
- **Arquitectura:** MVVM (Model-View-ViewModel)

## Instalación
1. Descarga el archivo `EasyTournament 1.0.apk`.
2. Habilita "Orígenes desconocidos" en tu móvil.
3. Instala y disfruta.

## Seguridad Avanzada: 
- **Row Level Security (RLS):** Implementación de políticas de seguridad en base de datos que garantizan que solo los creadores o administradores puedan modificar la información sensible.
- **Triggers PL/pgSQL:** Automatización completa en el servidor para:•Crear perfiles de usuario automáticamente tras el registro.•Actualizar contadores de participantes en tiempo real.•Recalcular el rating de los torneos tras cada nueva reseña.

## 📄 Documentación
Puedes consultar el manual técnico y de usuario completo aquí: [Descargar PDF](https://github.com/Chemidios/EasyTournament/blob/main/Documentacion.pdf)    
