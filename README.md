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

## Prompts del Equipo

### Erving Sequeira
### 1. Estructura inicial del proyecto

**Prompt:** "Voy a empezar un proyecto de escritorio en JavaFX para una red social universitaria. ¿Qué estructura de paquetes en capas me recomiendas? ¿Y cómo dejo el `pom` y el `module-info` para que corra con `mvn javafx:run`?"

**Respuesta:** Capas `model`, `persistence`, `service`, `view`; `Launcher` separado de `Application`; `pom` con JavaFX 21 + `javafx-maven-plugin`; `module-info` con `requires javafx.controls` y `javafx.fxml`, y `exports` del paquete de arranque.

**Uso:** Se armó el esqueleto (commit inicial). El paquete `view` terminó llamándose `ui` (`controller`, `component`, `fxml`, `css`) y se agregó `app`. JUnit se sumó al `pom` después, cuando empezaron los tests.

### 2. Capa de servicio (interfaces)

**Prompt:** "Ahora la capa de servicio. ¿Me propones las interfaces con sus métodos para usuarios y amistades? Todavía no quiero implementarlas."

**Respuesta:** `IServicioUsuarios` e `IServicioAmistad` en `service.contract`, métodos en español (`registrar`, `iniciarSesion`, `buscarUsuarios`, `enviarSolicitud`, `aceptarSolicitud`, `listarAmigos`), y la disyuntiva `Optional<T>` vs. excepciones.

**Uso:** Se usó tal cual la separación `service`/`service.contract` y los nombres. Se descartó `Optional`: la interfaz devuelve la entidad o `null`, y los datos inválidos lanzan `IllegalArgumentException`. Tampoco hubo DTOs: se devuelven las entidades del modelo.

### 3. Modelado del grafo de amistades

**Prompt:** "Para las amistades pensamos un grafo con lista de adyacencia. Al inicio dijimos dirigido, pero creo que no tiene sentido. ¿Cómo lo modelamos y qué operaciones necesita?"

**Respuesta:** Grafo **no dirigido** (la amistad es simétrica), `GrafoAmigos` con vértices y `Set` de adyacentes, `conectar`/`desconectar`/`sonAmigos`/`obtenerAmigos`, y de propina `amigosEnComun` y `distancia` con BFS.

**Uso:** Se implementó el grafo no dirigido con sus tests. **BFS, `distancia` y `amigosEnComun` no se usaron**: ninguna pantalla los necesitaba. El grafo quedó como índice en memoria dentro de `ServicioAmistad`; la fuente de verdad son los JSON.

### 4. Persistencia en JSON

**Prompt:** "Sigo con persistencia: quiero guardar todo en JSON. Necesito que el `module-info` se lleve bien con Gson y que el proyecto compile antes de escribir los repositorios."

**Respuesta:** Dependencia Gson 2.11, `opens` de los paquetes con clases reflejadas (`persistence`, `model.entity`) a `com.google.gson`, y un `GestorJSON` genérico con `Files.createTempFile` + `ATOMIC_MOVE` y fallback si el sistema de archivos no lo soporta.

**Uso:** Se agregó Gson al `pom` y los `opens` al `module-info`; el gestor se usó tal cual (los archivos quedan en la carpeta `datos/`). El valor real del agente fue cerrar el ciclo compilar → arreglar `module-info`, que a mano cuesta.

### 5. Recuperación de trabajo perdido por merge

**Prompt:** "Perdimos trabajo: un merge de una rama desactualizada contra `main` resolvió conflictos mal y se borró lo más reciente. Revisa el historial, encuentra los commits y recupera los cambios. Verifica que compile."

**Respuesta:** Revisó `git log` y `git reflog`, ubicó los commits de los compañeros, recuperó los archivos y lo dejó todo en un commit.

**Uso:** Se recuperó el backend en un commit unificado (`d161edc`).

### 6. Registro de fábricas de controladores

**Prompt:** "Cada vez que agregamos una pantalla hay que tocar un `switch` para crear el controlador y todos chocamos en el mismo archivo. Mi compañero propuso registrar fábricas y usarlas con `setControllerFactory`. ¿Cómo funciona y cómo lo aplico?"

**Respuesta:** `Map<Class<?>, Supplier<?>>` con `registrarFabrica(...)` y un `crearControlador` que falla con mensaje claro si falta la fábrica; el `FXMLLoader` recibe `setControllerFactory(this::crearControlador)`.

**Uso:** `NavegadorVistas` con navegación entre FXML y las fábricas registradas en `AppSocialUNA`. El `switch` compartido desapareció.

### 7. Estilos e iconos (AtlantaFX + Ikonli)

**Prompt:** "Quiero que la app no se vea a JavaFX pelado, pero sin escribir todo el CSS. ¿Sirve AtlantaFX? ¿Y para los iconos de la barra inferior?"

**Respuesta:** `atlantafx-base` con `PrimerLight`/`PrimerDark` vía `Application.setUserAgentStylesheet(...)`, Ikonli + Octicons (`new FontIcon("oct-home-16")`) y un toggle por propiedad de sistema (`-Dsocialuna.tema=light`) para no montar un sistema de preferencias.

**Uso:** `Tema.java` con claro/oscuro, iconos en el menú y `acceso.css` propio encima del tema.
