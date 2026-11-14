package cr.ac.una.est.socialuna.service.contract;

import cr.ac.una.est.socialuna.model.entity.Usuario;
import java.util.ArrayList;

public interface IServicioAmistad {

    //Recibe el ID del otro perfil y agrega la amistad al usuario que inició sesión.
    void agregarAmigo(String amigoId);

    //Recibe el ID del otro perfil y quita la amistad del usuario que inició sesión.
    void eliminarAmigo(String amigoId);

    //Devuelve la lista de amigos de la sesión actual.
    ArrayList<Usuario> listarMisAmigos();

    //Recibe el ID de un perfil y devuelve su lista de amigos.
    ArrayList<Usuario> listarAmigosDe(String perfilId);

    //Recibe el ID de un perfil y devuelve cuántos amigos tiene.
    int contarAmigosDe(String perfilId);

    //Recibe dos IDs y devuelve true si esos usuarios son amigos.
    boolean sonAmigos(String usuarioId, String otroId);
}
