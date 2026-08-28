# BuscadorAulas — ESCOM IPN

Aplicación Android de navegación indoor para el campus de la Escuela Superior de Cómputo (ESCOM) del Instituto Politécnico Nacional. El proyecto permite identificar un salón mediante OCR, seleccionar un destino, calcular una ruta dentro del campus y mostrar indicaciones paso a paso con apoyo de realidad aumentada.

**Desarrollado por Marco Antonio Anaya.**

## Funcionalidades principales

- Detección de letreros de salones mediante la cámara y reconocimiento de texto.
- Confirmación de la ubicación actual a partir del salón detectado.
- Selección y búsqueda de salones y puntos de interés del campus.
- Generación de rutas entre edificios, pisos, pasillos y escaleras.
- Indicaciones compactas y contextuales para avanzar, girar, subir o bajar pisos.
- Navegación con realidad aumentada mediante una flecha 3D anclada al entorno.
- Mapa 2D del campus por niveles, con ubicación, destino y ruta.
- Historial local de recorridos.
- Temas visuales Guinda/Azul y modos claro, oscuro o sistema.

## Tecnologías

- Kotlin
- Jetpack Compose + Material 3
- CameraX
- Google ML Kit Text Recognition
- ARCore / SceneView
- Room
- DataStore Preferences
- Gson
- Gradle Version Catalog

## Estructura del proyecto

```text
app/src/main/java/mx/ipn/escom/buscadoraulas/
├── ar/            # geometría y apoyo para elementos AR
├── data/          # modelos, Room y repositorios
├── ml/            # reconocimiento de texto
├── routing/       # grafo y generación de rutas
└── ui/            # navegación, pantallas, temas y ViewModels

app/src/main/assets/
├── locations.json
├── routes.json
└── models/arrow.glb
```

## Requisitos

- Android Studio compatible con Android Gradle Plugin 9.x.
- JDK 17 recomendado.
- Android SDK 35.
- Dispositivo Android con API 24 o superior y cámara.
- Para la navegación AR, un dispositivo compatible con ARCore ofrece la experiencia completa.

## Compilación

1. Clona el repositorio.
2. Ábrelo en Android Studio.
3. Configura el SDK local; Android Studio generará `local.properties`.
4. Sincroniza Gradle.
5. Ejecuta la aplicación en un dispositivo físico.

También puede generarse el APK de depuración con:

```bash
./gradlew assembleDebug
```

En Windows:

```powershell
.\gradlew.bat assembleDebug
```

## Flujo de uso

1. Escanear el letrero de un salón.
2. Confirmar la ubicación detectada.
3. Seleccionar un salón o punto de interés como destino.
4. Revisar el resumen de la ruta.
5. Iniciar la navegación AR y seguir las indicaciones.

## Datos del campus

La representación de ubicaciones, salones y rutas usada por la aplicación está incluida en los archivos de `assets` y en el grafo de navegación del proyecto. El mapa y las indicaciones fueron construidos específicamente para el prototipo académico de ESCOM.

## Autor

Marco Antonio Anaya  
ESCOM — Instituto Politécnico Nacional
