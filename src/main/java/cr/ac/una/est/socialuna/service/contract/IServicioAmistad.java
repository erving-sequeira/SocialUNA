package cr.ac.una.est.socialuna.service.contract;

import cr.ac.una.est.socialuna.model.entity.Usuario;
import java.util.ArrayList;

/** Operaciones de amistad del usuario en sesión. */
public interface IServicioAmistad {

    void agregarAmigo(String amigoId);

    void eliminarAmigo(String amigoId);

    ArrayList<Usuario> listarAmigos();

    boolean sonAmigos(String usuarioId, String otroId);
}
