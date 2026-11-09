# SocialUNA

Red social estudiantil de escritorio con Java 21, JavaFX 21 y Maven. El diseño y el reparto por colores están en [docs](../docs/README.md). Verde corresponde a Dev 2.

## Estado implementado

- Arranque JavaFX con una ventana vacía titulada SocialUNA.
- Entidades `Usuario`, `RelacionAmistad`, `Carrera` y `SedeCampus`.
- Pruebas de validaciones, relaciones bidireccionales, catálogos y arranque gráfico.

Registro/login, servicios, grafo, repositorios JSON, muro y comunidades siguen pendientes. Las carpetas reservadas no representan funcionalidades ya implementadas.

## Estructura

Todos los paquetes Java parten de `cr.ac.una.est.socialuna`:

```text
src/
  main/
    java/
      module-info.java
      cr/ac/una/est/socialuna/
        app/                AppSocialUNA y Launcher; arranque e integración
        model/
          entity/           Usuario, RelacionAmistad, Carrera, SedeCampus
          graph/            reservado para GrafoAmigos y VerticeAmigos
        service/
          contract/         reservado para las interfaces de servicio
        persistence/        reservado para documentos, repositorios y GestorJSON
        ui/
          controller/       reservado para controladores JavaFX
          component/        reservado para PanelPublicaciones
          navigation/       reservado para NavegadorVistas
    resources/cr/ac/una/est/socialuna/ui/
      fxml/                 vistas XML (archivos separados de los controladores)
      css/                  estilos
  test/java/cr/ac/una/est/socialuna/
    model/entity/           pruebas del dominio
    app/                    prueba opcional del arranque JavaFX
```

`target/` es salida generada de Maven y no se versiona. `.mvn/` y los scripts `mvnw` son herramientas de construcción. Las preferencias locales de `.idea/` permanecen ignoradas por Git. Los `.gitkeep` solo conservan carpetas pendientes que Git no guardaría vacías.

Correspondencia con las etiquetas del UML: `modelo` → `model/entity`; `estructuras` → `model/graph`; `contratos.servicio` → `service/contract`; `servicio` → `service`; `persistencia` → `persistence`; `ui` → sus tres subpaquetes; `app` → `app`. Se conserva el idioma de los paquetes ya iniciado en el repositorio; los nombres y firmas de las entidades siguen el UML.

## Contratos de las entidades

`Usuario` recibe ID, nombre, nombre de usuario, hash de contraseña y referencias académicas. Solo ofrece setters de carrera y sede. Los datos académicos pueden ser null; texto vacío elimina la selección. Los campos obligatorios rechazan null o blancos y se limpian los espacios exteriores. La entidad recibe el hash ya calculado: no cifra contraseñas ni verifica unicidad o existencia de catálogos; eso corresponde al futuro ServicioUsuarios.

`RelacionAmistad` recibe su ID, ambos IDs de usuario y fecha en milisegundos. Rechaza autoamistad y fechas negativas, acepta el inicio de época. `involucra` consulta cualquiera de los extremos y devuelve false para null o un ID ajeno; `obtenerOtroUsuario` devuelve el extremo opuesto o lanza IllegalArgumentException si el usuario no pertenece a la relación. No tiene setters. La deduplicación del par A–B/B–A corresponde al repositorio/servicio de amistad, todavía pendiente.

Los cuatro modelos tienen constructor completo y constructor vacío protegido, conforme al UML. Los IDs se suministran desde fuera para conservarlos al reconstruir datos; los servicios futuros generan los UUID al crear registros nuevos. Los constructores vacíos están reservados para reconstrucción: no producen por sí solos entidades de dominio completas. No hay dependencia de JavaFX ni de Gson en el modelo.

`Carrera` y `SedeCampus` conservan sus getters y rechazan IDs/nombres ausentes. Alias y tipo mantienen el comportamiento del código inicial, sin añadir catálogos o enumeraciones no acordados.

## Ejecutar y probar

Requiere un JDK 21 o superior y `JAVA_HOME` apuntando al JDK. Desde `codigo`, en Windows:

```powershell
.\mvnw.cmd test
.\mvnw.cmd "-Dpruebas.javafx=true" test
.\mvnw.cmd javafx:run
```

En Linux/macOS, sustituir `.\mvnw.cmd` por `./mvnw`. La primera ejecución requiere acceso al repositorio de Maven para descargar dependencias. `test` ejecuta 16 pruebas de dominio y omite la gráfica; la segunda orden ejecuta también la prueba real de ventana (17 en total) y requiere un escritorio disponible. Surefire 3.5.4 garantiza la ejecución de JUnit 5. Los resultados están en `target/surefire-reports/`.

La revisión del 8 de noviembre de 2026 compiló para Java 21 y ejecutó las 17 pruebas con JDK 25. No se certifica aún persistencia JSON ni los flujos funcionales pendientes.
