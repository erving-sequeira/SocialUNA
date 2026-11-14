
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

    //Recibe los servicios de usuarios y amistades junto con el repositorio de publicaciones.
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

    //Recibe el texto, lo guarda como post general en el muro del usuario y devuelve el post.
    @Override
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

    //Devuelve los posts del muro propio y de amigos, con los más nuevos primero.
    @Override
    public ArrayList<Publicacion> obtenerFeed() {

        Usuario usuario = exigirSesion();

        ArrayList<Publicacion> todas =
                repositorioPublicaciones.listar();

        ArrayList<Usuario> amigos =
                servicioAmistad.listarMisAmigos();

        ArrayList<Publicacion> feed = new ArrayList<>();

        for (Publicacion publicacion : todas) {

            if (publicacion.getAmbito() != AmbitoPublicacion.MURO) {
                continue;
            }

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

    //Devuelve el usuario que entró. Da error si se intenta usar el muro sin sesión.
    private Usuario exigirSesion() {

        Usuario usuario = servicioUsuarios.getUsuarioActual();

        if (usuario == null) {
            throw new IllegalStateException(
                    "Debe iniciar sesión para realizar esta operación.");
        }

        return usuario;
    }

    //Revisa que el texto recibido tenga contenido. Da error si llega vacío.
    private void validarTexto(String texto) {

        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(
                    "El texto de la publicación no puede estar vacío.");
        }
    }

    //Recibe una lista y devuelve una copia ordenada de más nueva a más vieja. En empates usa el ID.
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
                        .thenComparing(Publicacion::getId)
        );

        return ordenadas;
    }
}



