package cr.ac.una.est.socialuna.service.contract;

import cr.ac.una.est.socialuna.model.entity.Carrera;
import cr.ac.una.est.socialuna.model.entity.SedeCampus;
import cr.ac.una.est.socialuna.model.entity.Usuario;
import java.util.ArrayList;

public interface IServicioUsuarios {

    //Recibe nombre, usuario, contraseña e IDs de carrera y sede. Guarda y devuelve la cuenta nueva.
    Usuario registrar(String nombre, String nombreUsuario, String contrasena, String carreraId, String sedeId);

    //Verifica el nombre de usuario y la contraseña, abre la sesión y devuelve el perfil.
    Usuario iniciarSesion(String nombreUsuario, String contrasena);

    //Cierra la sesión del usuario actual.
    void cerrarSesion();

    //Devuelve el usuario en sesión, o null si nadie ha entrado.
    Usuario getUsuarioActual();

    //Recibe un texto y devuelve los perfiles que coinciden por nombre o usuario.
    ArrayList<Usuario> buscarUsuarios(String texto);

    //Recibe el ID de un usuario y devuelve su perfil, o null si no existe.
    Usuario obtenerPerfil(String usuarioId);

    //Recibe los IDs de carrera y sede, guarda los cambios y devuelve el perfil actualizado.
    Usuario actualizarDatosAcademicos(String carreraId, String sedeId);

    //Devuelve las carreras activas del catálogo.
    ArrayList<Carrera> listarCarreras();

    //Devuelve las sedes activas del catálogo.
    ArrayList<SedeCampus> listarSedes();
}
