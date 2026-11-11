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
3. Al arrancar se abre la ventana SocialUNA (800×600).

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
- **Persistencia**: tres archivos JSON serializados con Gson, cargados una vez al iniciar.

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
│   │       │   ├── DocumentoAmistades.java   # Raíz de amistades.json
│   │       │   └── RepositorioAmistadesJSON.java  # Relaciones en amistades.json
│   │       │
│   │       └── ui/
│   │           ├── controller/
│   │           ├── component/
│   │           └── navigation/
│   │
│   └── resources/cr/ac/una/est/socialuna/ui/
│       ├── fxml/
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
