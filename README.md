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

## Funcionalidades previstas

- Registro con hash SHA-256, login y sesión activa.
- Amistades con grafo propio de usuarios (agregar, eliminar, listar).
- Feed principal: publicaciones del usuario y sus amigos, orden descendente.
- Comunidades por carrera y sede, con publicaciones y comentarios de un nivel.
- Perfil con datos académicos editables (carrera y sede).

## Arquitectura

- **MVC**: JavaFX con FXML (vistas separadas de controladores), modelo en `logic/model`.
- **Contratos**: interfaces de servicio (`logic/service/contract`) que dividen el trabajo por responsables.
- **Grafo propio**: `GrafoAmigos` con vertices y adyacencias; `ArrayList` solo como soporte.
- **Persistencia**. Los repositorios usan Gson y conservan los datos cargados en memoria.

## Persistencia actual

| Archivo | Contenido | Repositorio |
|---|---|---|
| `usuarios.json` | Arreglo de objetos `Usuario`. Cada perfil guarda `carreraId` y `sedeId` opcionales. | `RepositorioUsuariosJSON` |
| `carreras.json` | Arreglo de objetos `Carrera`. | `RepositorioCarrerasJSON` |
| `sedes.json` | Arreglo de objetos `SedeCampus`. | `RepositorioSedesJSON` |
| `amistades.json` | Arreglo de objetos `RelacionAmistad`. | `RepositorioAmistadesJSON` |

`GestorJSON` recibe el directorio de datos. Los repositorios de usuarios y catálogos cargan al construirse y permiten recargar explícitamente. Para amistades se llama a `cargar()` antes de usar el repositorio. Los archivos ausentes equivalen a listas vacías y se crean al guardar. Cada catálogo tiene su propio repositorio. Se actualizan con `guardarCarreras` y `guardarSedes`, y un archivo inválido solo afecta la carga de su repositorio. Si cambia el nombre de una carrera o sede, conservar su ID mantiene la referencia de los perfiles.

`ServicioUsuarios` requiere una carrera y una sede activas para registrar una cuenta o actualizar sus datos académicos. El nombre de usuario se guarda en minúsculas y sin espacios exteriores. Los perfiles existentes pueden cargar referencias académicas vacías. La ventana inicial de JavaFX aún no carga los repositorios.

## Estructura del proyecto

```text
src/main/java/cr/ac/una/est/socialuna/
  app/                           Arranque JavaFX y Launcher
  logic/
    model/entity/                Usuario, RelacionAmistad, Carrera y SedeCampus
    model/graph/                 GrafoAmigos y VerticeAmigos
    repository/                  Repositorios de usuarios, catálogos y amistades
    service/contract/            IServicioUsuarios e IServicioAmistad
  persistence/                   GestorJSON
  ui/                            Carpetas reservadas para la interfaz
src/main/resources/              Carpetas de FXML y CSS
src/test/java/                    Pruebas de modelos, grafo, JSON y arranque
```

## Pruebas

En Windows, desde la raíz del proyecto:

```powershell
.\mvnw.cmd test
.\mvnw.cmd "-Dpruebas.javafx=true" test
```

La segunda orden incluye la prueba de la ventana JavaFX y requiere un escritorio. Las pruebas de persistencia escriben en carpetas temporales.
