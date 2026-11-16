package cr.ac.una.est.socialuna.service.contract;
import cr.ac.una.est.socialuna.model.entity.Comentario;
import cr.ac.una.est.socialuna.model.entity.Publicacion;
import cr.ac.una.est.socialuna.model.entity.TipoPublicacion;
import java.util.ArrayList;

public interface IServicioPublicacion {

    //Recibe el texto, lo publica en el muro del usuario en sesión y devuelve la publicación.
    Publicacion publicar(String texto);

    //Recibe el texto y el tipo. Los guarda en el muro personal y devuelve la publicación.
    Publicacion publicar(String texto, TipoPublicacion tipo);

    //Devuelve las publicaciones del muro propio y de amigos, con las más nuevas primero.
    ArrayList<Publicacion> obtenerFeed();

    //Recibe el ID de una publicación visible y el texto. Guarda y devuelve el comentario.
    Comentario comentar(String publicacionId, String texto);

    //Recibe el ID del post, el comentario padre y el texto. Guarda y devuelve la respuesta.
    Comentario responder(String publicacionId, String comentarioPadreId, String texto);

    //Recibe el ID de una publicación visible y devuelve sus comentarios.
    ArrayList<Comentario> obtenerComentarios(String publicacionId);

    //Recibe el ID del post y del comentario propio. Borra el comentario y sus respuestas.
    void eliminarComentario(String publicacionId, String comentarioId);

    //Recibe el ID de una publicación visible y alterna el me gusta del usuario en sesión.
    Publicacion alternarReaccion(String publicacionId);

    //Recibe el ID de un usuario y devuelve sus publicaciones de muro visibles para quien mira.
    ArrayList<Publicacion> obtenerPublicacionesDe(String usuarioId);

    //Recibe el ID de una publicación del muro propia y la elimina con sus comentarios.
    void eliminarPublicacion(String publicacionId);

}

