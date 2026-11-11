package cr.ac.una.est.socialuna.logic.service.contract;

import cr.ac.una.est.socialuna.logic.model.entity.Usuario;
import java.util.ArrayList;

/** Operaciones de amistad del usuario en sesión. */
public interface IServicioAmistad {

    void agregarAmigo(String amigoId);

    void eliminarAmigo(String amigoId);

    ArrayList<Usuario> listarAmigos();

    boolean sonAmigos(String usuarioId, String otroId);
}
