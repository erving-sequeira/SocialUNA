# SocialUNA

Red social estudiantil de escritorio: registro, amistades, feed y comunidades
por carrera y sede para estudiantes de la UNA.
Desarrollado para el curso EIF207 - Estructuras de Datos (UNA).

## Integrantes

- Jose Alvarado
- Isabella Cubillo
- Erving Sequeira
- Arleth Varela

## Cómo ejecutar

1. Abrir el proyecto en IntelliJ (requiere JDK 21 o superior con `JAVA_HOME`).
2. Ejecutar la clase `Launcher` (`cr.ac.una.est.socialuna.app.Launcher`), o
   `.\mvnw.cmd javafx:run` desde la terminal.
3. Al arrancar se abre Login. Usa **Crear cuenta** para abrir Registro y seleccionar una carrera y una sede activas.
4. Una vez creada la cuenta, vuelve a Login e ingresa. La ventana provisional de Inicio permite cerrar sesión mientras se integra el feed.

Los archivos de acceso se leen desde el directorio `datos/`. Al faltar un archivo de catálogo, la aplicación guarda un catálogo inicial de ejemplo. Puedes ampliar los arreglos de `carreras.json` y `sedes.json` según las opciones del proyecto.

## Funcionalidades

- Registro con hash SHA-256, login y sesión activa.
- Amistades con grafo propio de usuarios (agregar, eliminar, listar).
- Feed principal: publicaciones del usuario y sus amigos, orden descendente.
- Comunidades por carrera y sede, con publicaciones y comentarios de un nivel.
- Perfil con datos académicos editables (carrera y sede).

## Arquitectura

- **MVC**: JavaFX con FXML (vistas separadas de controladores), modelo en `model`.
- **Contratos**: interfaces de servicio (`service/contract`) que dividen el trabajo por responsables.
- **Grafo propio**: `GrafoAmigos` con vertices y adyacencias; `ArrayList` solo como soporte.
- **Persistencia**: usuarios, carreras, sedes, amistades y publicaciones en archivos JSON separados. El repositorio de amistades admite el arreglo y el objeto anterior con `relaciones`.

## Estructura del proyecto

```text
src/
├── main/
│   ├── java/
│   │   ├── module-info.java
│   │   └── cr/ac/una/est/socialuna/
│   │       ├── app/
│   │       │   ├── AppSocialUNA.java        # Punto de entrada JavaFX
│   │       │   └── Launcher.java            # Lanza la aplicación sin módulo JavaFX
│   │       │
│   │       ├── model/
│   │       │   ├── entity/                  # Usuario, RelacionAmistad, Carrera, SedeCampus
│   │       │   └── graph/
│   │       │       ├── VerticeAmigos.java    # ID del usuario y sus IDs de vecinos
│   │       │       └── GrafoAmigos.java      # Amistades por listas de adyacencia
│   │       │
│   │       ├── service/
│   │       │   └── contract/                # IServicioUsuarios, IServicioAmistad
│   │       │
│   │       ├── persistence/
│   │       │   ├── GestorJSON.java           # Lectura/escritura común con Gson
│   │       │   └── RepositorioAmistadesJSON.java  # Relaciones en amistades.json
│   │       │
│   │       └── ui/
│   │           ├── controller/             # Login y Registro
│   │           ├── component/
│   │           └── NavegadorVistas.java
│   │
│   └── resources/cr/ac/una/est/socialuna/ui/
│       ├── fxml/                           # Login.fxml y Registro.fxml
│       └── css/
│
└── test/
    └── java/cr/ac/una/est/socialuna/
        ├── app/AppSocialUNATest.java        # Arranque gráfico
        ├── model/entity/                    # UsuarioTest, RelacionAmistadTest, CatalogosTest
        ├── model/graph/GrafoAmigosTest.java # Conexión recíproca, duplicados y borrado
        └── persistence/
            └── RepositorioAmistadesJSONTest.java  # Guardar, reabrir y errores de archivo
```
