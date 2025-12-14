# Hospital Routing App - Kotlin

Aplicación Android para la búsqueda y gestión de centros médicos con integración de mapas y rutas en tiempo real.

## 📱 Arquitectura de Activities

### 1. **MainActivity**
**Propósito**: Pantalla principal de la aplicación que permite buscar hospitales por especialidad.

**Funcionalidad**:
- Presenta un `Spinner` con especialidades médicas (Cardiología, Dermatología, Pediatría, Neurología, Traumatología)
- Ofrece tres opciones principales mediante botones:
  - **Buscar**: Navega a `MapActivity` pasando la especialidad seleccionada
  - **Hospitales**: Navega a `HospitalActivity` para ver la lista de centros médicos
  - **Especialidades**: Navega a `EspecialidadActivity` para gestionar especialidades

**Conexiones**:
- → `MapActivity` (búsqueda por especialidad)
- → `HospitalActivity` (gestión de hospitales)
- → `EspecialidadActivity` (gestión de especialidades)

---

### 2. **HospitalActivity**
**Propósito**: Administración de centros médicos registrados en la base de datos.

**Funcionalidad**:
- Muestra una lista de hospitales mediante `ListView` con `CentroMedicoAdapter`
- Carga datos de Firebase Realtime Database en tiempo real
- Permite agregar nuevos hospitales mediante botón
- Actualiza automáticamente la lista cuando hay cambios en Firebase

**Conexiones**:
- ← `MainActivity` (navegación desde menú principal)
- → `AddHospitalActivity` (agregar nuevo hospital)
- ↔ Firebase Realtime Database (tabla "CentrosMedicos")

---

### 3. **AddHospitalActivity**
**Propósito**: Formulario completo para registrar un nuevo centro médico.

**Funcionalidad**:
- Captura información del hospital:
  - Nombre del centro médico
  - Especialidades médicas (mediante `ChipGroup` con chips dinámicos)
  - Ubicación geográfica (latitud y longitud)
  - Horarios de atención
- Utiliza `ActivityResultContracts` para recibir datos de sub-activities
- Guarda el estado de la actividad para preservar datos en rotación de pantalla
- Valida que todos los campos estén completos antes de guardar
- Genera un UUID único para cada hospital
- Persiste los datos en Firebase

**Conexiones**:
- ← `HospitalActivity` (navegación para agregar hospital)
- → `SeleccionarUbicacionActivity` (selección de coordenadas en mapa)
- → `HorarioDeAtencionActivity` (definición de horarios)
- ↔ Firebase Realtime Database (guardado de datos)

**Flujo de datos**:
1. Usuario ingresa nombre y selecciona especialidades
2. Navega a `SeleccionarUbicacionActivity` → recibe latitud/longitud
3. Navega a `HorarioDeAtencionActivity` → recibe horarios
4. Guarda todo en Firebase y cierra la actividad

---

### 4. **SeleccionarUbicacionActivity**
**Propósito**: Interfaz interactiva para seleccionar la ubicación geográfica del hospital en un mapa.

**Funcionalidad**:
- Implementa Google Maps (`OnMapReadyCallback`)
- Obtiene la ubicación actual del dispositivo usando `FusedLocationProviderClient`
- Permite seleccionar ubicación tocando cualquier punto del mapa
- Muestra un marcador en la posición seleccionada
- Devuelve las coordenadas (latitud/longitud) a `AddHospitalActivity`

**Conexiones**:
- ← `AddHospitalActivity` (lanzada con `ActivityResultLauncher`)
- → Devuelve `Intent` con extras: "latitud" y "longitud"
- Requiere permisos de ubicación (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`)

---

### 5. **HorarioDeAtencionActivity**
**Propósito**: Formulario para definir los horarios de atención del centro médico.

**Funcionalidad**:
- Captura horarios para tres grupos de días:
  - **Lunes a Viernes**: Hora de apertura y cierre
  - **Sábado**: Hora de apertura y cierre
  - **Domingo**: Hora de apertura y cierre
- Crea objetos `HorarioAtencion` para cada día de la semana (0-6)
- Valida que todos los campos estén completos
- Devuelve un `ArrayList<HorarioAtencion>` serializable

**Conexiones**:
- ← `AddHospitalActivity` (lanzada con `ActivityResultLauncher`)
- → Devuelve `Intent` con extra: "horarioAtencion" (ArrayList serializable)

**Modelo de datos**:
```kotlin
HorarioAtencion(dia: Int, horaInicio: Int, horaFin: Int)
// dia: 0-6 (Lunes=0, Domingo=6)
```

---

### 6. **MapActivity**
**Propósito**: Visualización de rutas en tiempo real entre dos usuarios usando Google Maps.

**Funcionalidad**:
- Muestra mapa interactivo con Google Maps API
- Rastrea la ubicación del usuario en tiempo real con `FusedLocationProviderClient`
- Actualiza la posición cada 5-8 segundos
- Sube ubicación a Firebase Realtime Database
- Escucha la ubicación de otro usuario en tiempo real
- Dibuja rutas entre dos usuarios usando Google Directions API
- Muestra marcadores diferenciados:
  - **Azul**: Usuario actual ("Yo")
  - **Rojo**: Otro usuario ("Amigo")
- Calcula y muestra distancia y tiempo estimado de la ruta

**Conexiones**:
- ← `MainActivity` (recibe especialidad seleccionada vía Intent)
- ↔ Firebase Realtime Database (tabla "users" con ubicaciones en tiempo real)
- ↔ Google Directions API (cálculo de rutas)
- Utiliza Retrofit + Coroutines para llamadas asíncronas

**Flujo en tiempo real**:
1. Usuario A actualiza su ubicación → Firebase
2. Firebase notifica a Usuario B → actualiza marcador
3. Se recalcula la ruta entre ambos usuarios
4. Se dibuja polyline en el mapa

---

### 7. **EspecialidadActivity**
**Propósito**: Gestión de especialidades médicas (en desarrollo).

**Funcionalidad actual**:
- Activity básica con botón para añadir especialidades
- Layout definido pero lógica no implementada completamente

**Conexiones**:
- ← `MainActivity` (navegación desde menú principal)

---

## 🔗 Diagrama de Flujo de Navegación

```
MainActivity (Inicio)
    ├─→ MapActivity (Búsqueda con especialidad + rutas en tiempo real)
    │       └─→ Firebase (ubicaciones de usuarios)
    │
    ├─→ HospitalActivity (Lista de hospitales)
    │       ├─→ Firebase (lectura de CentrosMedicos)
    │       └─→ AddHospitalActivity (Agregar hospital)
    │               ├─→ SeleccionarUbicacionActivity (Mapa de selección)
    │               │       └─→ Retorna: latitud, longitud
    │               ├─→ HorarioDeAtencionActivity (Horarios)
    │               │       └─→ Retorna: ArrayList<HorarioAtencion>
    │               └─→ Firebase (guardar CentroMedico)
    │
    └─→ EspecialidadActivity (Gestión de especialidades)
```

---

## 🗄️ Modelo de Datos

### CentroMedico
```kotlin
CentroMedico(
    id: String,                           // UUID único
    nombre: String,                       // Nombre del hospital
    especialidades: ArrayList<String>,    // Lista de especialidades
    horarios: ArrayList<HorarioAtencion>, // Horarios por día
    latitud: Double,                      // Coordenada geográfica
    longitud: Double                      // Coordenada geográfica
)
```

### HorarioAtencion
```kotlin
HorarioAtencion(
    dia: Int,         // 0=Lunes, 1=Martes, ..., 6=Domingo
    horaInicio: Int,  // Hora de apertura (formato 24h)
    horaFin: Int      // Hora de cierre (formato 24h)
)
```

---

## 🔧 Tecnologías Utilizadas

- **Lenguaje**: Kotlin
- **Base de datos**: Firebase Realtime Database
- **Mapas**: Google Maps SDK para Android
- **Rutas**: Google Directions API
- **Ubicación**: Google Location Services (FusedLocationProviderClient)
- **Networking**: Retrofit + Gson
- **Asincronía**: Coroutines (Dispatchers.IO)
- **UI Components**: Material Design (ChipGroup, MaterialButton)

---

## 📋 Características Principales

1. **Búsqueda por especialidad**: Filtrado de hospitales según especialidades médicas
2. **Gestión CRUD**: Creación y lectura de centros médicos
3. **Selección geográfica**: Interfaz intuitiva para ubicar hospitales en mapa
4. **Horarios flexibles**: Sistema completo de gestión de horarios de atención
5. **Rutas en tiempo real**: Cálculo dinámico de rutas entre usuarios
6. **Sincronización en vivo**: Actualización automática de datos con Firebase
7. **Persistencia de estado**: Manejo de cambios de configuración (rotación)

---

## 🚀 Flujo de Usuario Principal

1. Usuario abre `MainActivity`
2. Selecciona una especialidad médica
3. Presiona "Buscar" → abre `MapActivity` con rutas
4. O presiona "Hospitales" → ve lista en `HospitalActivity`
5. Desde `HospitalActivity` puede agregar nuevo hospital:
   - Llena datos en `AddHospitalActivity`
   - Selecciona ubicación en mapa interactivo
   - Define horarios de atención
   - Guarda en Firebase
6. Los datos se sincronizan automáticamente en todos los dispositivos

---

## 📝 Notas de Desarrollo

- Los IDs de usuario en `MapActivity` están hardcodeados ("user_1", "user_2") - se recomienda usar Firebase Auth
- Las especialidades están definidas estáticamente - considerar cargarlas desde Firebase
- `EspecialidadActivity` está en desarrollo y requiere implementación completa
- Se requiere configurar `google-services.json` para Firebase
- Necesario API Key de Google Maps en `strings.xml`

---

## 🔐 Permisos Requeridos

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
```

---

## 🎯 Próximas Mejoras Sugeridas

- Implementar autenticación de usuarios con Firebase Auth
- Agregar búsqueda por proximidad geográfica
- Implementar sistema de calificaciones para hospitales
- Añadir filtros avanzados (horarios disponibles, servicios, etc.)
- Completar funcionalidad de `EspecialidadActivity`
- Implementar caché local para funcionamiento offline
- Agregar notificaciones push para actualizaciones importantes
