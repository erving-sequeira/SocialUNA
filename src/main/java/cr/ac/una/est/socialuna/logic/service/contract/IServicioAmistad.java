package cr.ac.una.est.socialuna.logic.service.contract;

import cr.ac.una.est.socialuna.logic.model.entity.Usuario;
import java.util.ArrayList;

/** Operaciones de amistad del usuario en sesión. */
public interface IServicioAmistad {

    void agregarAmigo(String amigoId);

    void eliminarAmigo(String amigoId);

    /** Lista de amistades del usuario en sesión. */
    ArrayList<Usuario> listarMisAmigos();

    /** Lista de amistades del perfil indicado, consultada por un usuario en sesión. */
    ArrayList<Usuario> listarAmigosDe(String perfilId);

    /** Total de amistades del perfil indicado en el grafo. */
    int contarAmigosDe(String perfilId);

    boolean sonAmigos(String usuarioId, String otroId);
}
