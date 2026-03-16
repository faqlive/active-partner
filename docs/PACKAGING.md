# Active Partner Packaging Guide

This guide summarizes the recommended packaging workflows for this project.

## Requirements

- JDK 11+ (JDK 17 recommended for `jpackage`).
- Project Maven Wrapper (`mvnw` / `mvnw.cmd`).
- Windows x64 to build Windows artifacts.

Base commands:

```powershell
.\mvnw clean test
.\mvnw clean package
```

## 1) Executable JAR (quick)

Build a `.jar` runnable on machines with Java installed.

```powershell
.\mvnw clean package
```

Expected output:

- `target\active-partner-1.0-SNAPSHOT.jar`

Run:

```powershell
java -jar .\target\active-partner-1.0-SNAPSHOT.jar
```

## 2) Portable app with bundled runtime (no installer)

Recommended when WiX is not available.

```powershell
.\mvnw clean javafx:jlink
Compress-Archive -Path .\target\app\* -DestinationPath .\target\ActivePartner-portable.zip -Force
```

Expected output:

- `target\app\` (portable runtime image)
- `target\ActivePartner-portable.zip`

On another machine:

1. Unzip.
2. Run `app\bin\app.bat`.

## 3) jpackage app-image (no installer)

Build a portable app image with `jpackage`.

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

Expected output:

- `target\dist\ActivePartner\`

## 4) jpackage EXE installer (requires WiX)

For `--type exe`, `jpackage` requires WiX Toolset.

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

Expected output:

- `target\installer\ActivePartner-*.exe`

## Icons

Project icon files:

- `src/main/resources/org/example/activepartner/assets/app-icon.png`
- `src/main/resources/org/example/activepartner/assets/app-icon.ico`

Usage:

- Window, taskbar, tray: loaded from app resources (`.png`).
- Windows installer/executable: pass `.ico` through `--icon`.

## Common issues

### Error: `Missing .jpackage.xml file in app-image dir`

Do not use `--app-image target\app` if that folder comes from `javafx:jlink`.

Use the `--input + --main-jar + --main-class` flow (sections 3 and 4).

### Error: `Can not find WiX tools` / `Invalid or unsupported type: [exe]`

WiX is not available in the current environment. Use:

- `--type app-image` (section 3), or
- `javafx:jlink` portable flow (section 2).

### Recursive output (`dist` inside `input`)

Never set `--dest` inside `--input`.

Correct:

- `--input .\target\jpackage-input`
- `--dest .\target\dist`

## Recommended workflow for this project

1. Internal sharing: `javafx:jlink` + portable ZIP.
2. Final distribution: `jpackage --type exe` (when WiX is available).
