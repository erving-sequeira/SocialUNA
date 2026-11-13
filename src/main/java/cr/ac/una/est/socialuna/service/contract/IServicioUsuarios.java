package cr.ac.una.est.socialuna.service.contract;

import cr.ac.una.est.socialuna.model.entity.Carrera;
import cr.ac.una.est.socialuna.model.entity.SedeCampus;
import cr.ac.una.est.socialuna.model.entity.Usuario;
import java.util.ArrayList;

/** Operaciones de registro, sesión y datos académicos de los usuarios. */
public interface IServicioUsuarios {

    Usuario registrar(String nombre, String nombreUsuario, String contrasena, String carreraId, String sedeId);

    Usuario iniciarSesion(String nombreUsuario, String contrasena);

    void cerrarSesion();

    /** Usuario de la sesión activa; null sin sesión. */
    Usuario getUsuarioActual();

    ArrayList<Usuario> buscarUsuarios(String texto);

    Usuario obtenerPerfil(String usuarioId);

    Usuario actualizarDatosAcademicos(String carreraId, String sedeId);

    ArrayList<Carrera> listarCarreras();

    ArrayList<SedeCampus> listarSedes();
}
