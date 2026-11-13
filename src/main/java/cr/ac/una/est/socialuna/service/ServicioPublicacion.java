
package cr.ac.una.est.socialuna.service;

import cr.ac.una.est.socialuna.model.entity.AmbitoPublicacion;
import cr.ac.una.est.socialuna.model.entity.Publicacion;
import cr.ac.una.est.socialuna.model.entity.TipoPublicacion;
import cr.ac.una.est.socialuna.model.entity.Usuario;
import cr.ac.una.est.socialuna.persistence.RepositorioPublicacionesJSON;
import cr.ac.una.est.socialuna.service.contract.IServicioAmistad;
import cr.ac.una.est.socialuna.service.contract.IServicioUsuarios;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;
import cr.ac.una.est.socialuna.service.contract.IServicioPublicacion;

public class ServicioPublicacion implements IServicioPublicacion {

    private IServicioUsuarios servicioUsuarios;
    private IServicioAmistad servicioAmistad;
    private RepositorioPublicacionesJSON repositorioPublicaciones;

    // Constructor
    public ServicioPublicacion(
            IServicioUsuarios servicioUsuarios,
            IServicioAmistad servicioAmistad,
            RepositorioPublicacionesJSON repositorioPublicaciones) {

        if (servicioUsuarios == null || servicioAmistad == null
                || repositorioPublicaciones == null) {
            throw new IllegalArgumentException(
                    "Los servicios y el repositorio son obligatorios.");
        }

        this.servicioUsuarios = servicioUsuarios;
        this.servicioAmistad = servicioAmistad;
        this.repositorioPublicaciones = repositorioPublicaciones;
    }

    @Override
    // Crear una nueva publicación
    public Publicacion publicar(String texto) {

        Usuario usuario = exigirSesion();
        validarTexto(texto);

        Publicacion publicacion = new Publicacion(
                UUID.randomUUID().toString(),
                usuario.getId(),
                texto.strip(),
                System.currentTimeMillis(),
                AmbitoPublicacion.MURO,
                TipoPublicacion.GENERAL,
                null,
                null
        );

        repositorioPublicaciones.guardar(publicacion);

        return publicacion;
    }

    // Obtener las publicaciones del usuario y sus amigos
    @Override
    public ArrayList<Publicacion> obtenerFeed() {

        Usuario usuario = exigirSesion();

        ArrayList<Publicacion> todas =
                repositorioPublicaciones.listar();

        ArrayList<Usuario> amigos =
                servicioAmistad.listarAmigos();

        ArrayList<Publicacion> feed = new ArrayList<>();

        for (Publicacion publicacion : todas) {

            boolean esPropia =
                    publicacion.getAutorId().equals(usuario.getId());

            boolean esDeUnAmigo = false;

            for (Usuario amigo : amigos) {
                if (publicacion.getAutorId().equals(amigo.getId())) {
                    esDeUnAmigo = true;
                    break;
                }
            }

            if (esPropia || esDeUnAmigo) {
                feed.add(publicacion);
            }
        }

        return ordenarRecientes(feed);
    }

    // Verificar que exista una sesión activa
    private Usuario exigirSesion() {

        Usuario usuario = servicioUsuarios.getUsuarioActual();

        if (usuario == null) {
            throw new IllegalStateException(
                    "Debe iniciar sesión para realizar esta operación.");
        }

        return usuario;
    }

    // Validar el texto de una publicación
    private void validarTexto(String texto) {

        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(
                    "El texto de la publicación no puede estar vacío.");
        }
    }

    // Ordenar publicaciones de la más reciente a la más antigua
    private ArrayList<Publicacion> ordenarRecientes(
            ArrayList<Publicacion> publicaciones) {

        if (publicaciones == null) {
            throw new IllegalArgumentException(
                    "La lista de publicaciones es obligatoria.");
        }
        ArrayList<Publicacion> ordenadas =
                new ArrayList<>(publicaciones);

        ordenadas.sort(
                Comparator.comparingLong(Publicacion::getCreadoEn)
                        .reversed()
        );

        return ordenadas;
    }
}



