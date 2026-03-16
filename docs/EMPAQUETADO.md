# Empaquetado de Active Partner

Esta guia resume los flujos de empaquetado recomendados para este proyecto.

## Requisitos

- JDK 11+ (recomendado JDK 17 para `jpackage`).
- Maven Wrapper del proyecto (`mvnw` / `mvnw.cmd`).
- Windows x64 para generar artefactos Windows.

Comandos base:

```powershell
.\mvnw clean test
.\mvnw clean package
```

## 1) JAR ejecutable (rapido)

Genera un `.jar` listo para ejecutar en un equipo con Java instalado.

```powershell
.\mvnw clean package
```

Salida esperada:

- `target\active-partner-1.0-SNAPSHOT.jar`

Ejecucion:

```powershell
java -jar .\target\active-partner-1.0-SNAPSHOT.jar
```

## 2) Portable con runtime incluido (sin instalador)

Este es el flujo recomendado cuando no se puede usar WiX.

```powershell
.\mvnw clean javafx:jlink
Compress-Archive -Path .\target\app\* -DestinationPath .\target\ActivePartner-portable.zip -Force
```

Salida esperada:

- `target\app\` (imagen portable)
- `target\ActivePartner-portable.zip`

En otro equipo:

1. Descomprimir ZIP.
2. Ejecutar `app\bin\app.bat`.

## 3) app-image con jpackage (sin instalador)

Genera una app portable usando `jpackage`.

```powershell
Remove-Item -Recurse -Force .\target\dist, .\target\jpackage-input -ErrorAction SilentlyContinue
New-Item -ItemType Directory .\target\jpackage-input | Out-Null
Copy-Item .\target\active-partner-1.0-SNAPSHOT.jar .\target\jpackage-input\

jpackage --type app-image `
  --name ActivePartner `
  --input .\target\jpackage-input `
  --main-jar active-partner-1.0-SNAPSHOT.jar `
  --main-class org.example.activepartner.Main `
  --dest .\target\dist `
  --icon .\src\main\resources\org\example\activepartner\assets\app-icon.ico
```

Salida esperada:

- `target\dist\ActivePartner\`

## 4) Instalador EXE con jpackage (requiere WiX)

Para `--type exe`, `jpackage` necesita WiX Toolset.

```powershell
jpackage --type exe `
  --name ActivePartner `
  --input .\target\jpackage-input `
  --main-jar active-partner-1.0-SNAPSHOT.jar `
  --main-class org.example.activepartner.Main `
  --dest .\target\installer `
  --win-shortcut --win-menu `
  --icon .\src\main\resources\org\example\activepartner\assets\app-icon.ico
```

Salida esperada:

- `target\installer\ActivePartner-*.exe`

## Iconos

Archivos de icono del proyecto:

- `src/main/resources/org/example/activepartner/assets/app-icon.png`
- `src/main/resources/org/example/activepartner/assets/app-icon.ico`

Usos:

- Ventana, taskbar y tray: cargados desde recursos de la app (`.png`).
- Instalador/ejecutable Windows: `--icon ...app-icon.ico`.

## Problemas comunes

### Error: `Missing .jpackage.xml file in app-image dir`

No uses `--app-image target\app` si esa carpeta viene de `javafx:jlink`.

Usa flujo `--input + --main-jar + --main-class` (secciones 3 y 4).

### Error: `Can not find WiX tools` / `Invalid or unsupported type: [exe]`

WiX no esta disponible en el entorno actual. Usa:

- `--type app-image` (seccion 3), o
- portable con `javafx:jlink` (seccion 2).

### Salida recursiva (`dist` dentro de `input`)

Nunca pongas `--dest` dentro de `--input`.

Correcto:

- `--input .\target\jpackage-input`
- `--dest .\target\dist`

## Flujo recomendado para este proyecto

1. Desarrollo interno: `javafx:jlink` + ZIP portable.
2. Distribucion final: `jpackage --type exe` (si WiX esta disponible).
