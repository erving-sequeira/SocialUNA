# SocialUNA

SocialUNA será una red social estudiantil para conectar a la comunidad de la Universidad Nacional. El proyecto se desarrolla como aplicación de escritorio con interfaz gráfica JavaFX y una estructura organizada según el patrón MVC.

## Funcionalidades previstas

- Registro de estudiantes e inicio de sesión.
- Búsqueda de usuarios y gestión de amistades.
- Creación de publicaciones y un muro con contenido propio y de amistades, ordenado de más reciente a más antiguo.
- Gestión de usuarios, amistades y publicaciones en memoria mediante estructuras de datos.
- Guardado y carga de la información desde archivos para conservarla entre ejecuciones.

La persistencia se realizará mediante archivos; el proyecto no utilizará una base de datos relacional.

## Organización del código

- `model`: entidades y estructuras de datos del dominio.
- `persistence`: lectura y escritura de datos en archivos.
- `service`: reglas de negocio.
- `controller` y `view`: coordinación de acciones y componentes de la interfaz.

## Tecnologías

- Java 21
- JavaFX 21
- Maven

## Estado actual

El proyecto cuenta con la estructura inicial MVC y una ventana JavaFX vacía titulada **SocialUNA**. Las funcionalidades descritas están planificadas y se incorporarán durante el desarrollo.

## Ejecutar

Con Java 21 instalado, desde la raíz del proyecto ejecuta:

```bash
./mvnw javafx:run
```

En Windows:

```powershell
.\mvnw.cmd javafx:run
```
