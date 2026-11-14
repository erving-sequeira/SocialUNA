package cr.ac.una.est.socialuna.persistence;

import com.google.gson.reflect.TypeToken;
import cr.ac.una.est.socialuna.model.entity.Usuario;
import cr.ac.una.est.socialuna.persistence.GestorJSON;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Predicate;

public class RepositorioUsuariosJSON {
    private static final String ARCHIVO = "usuarios.json";
    private static final Type TIPO = new TypeToken<ArrayList<Usuario>>() { }.getType();
    private final GestorJSON gestor;
    private ArrayList<Usuario> datos;

    //Recibe el gestor JSON y carga los usuarios guardados.
    public RepositorioUsuariosJSON(GestorJSON gestor) {
        this.gestor = Objects.requireNonNull(gestor);
        cargar();
    }

    //Lee usuarios.json, revisa que no haya IDs ni nombres de usuario repetidos y carga los perfiles.
    public void cargar() {
        List<Usuario> leidos = gestor.leer(ARCHIVO, TIPO, new ArrayList<>());
        var usuarios = new ArrayList<Usuario>();
        var ids = new HashSet<String>();
        var nombresUsuario = new HashSet<String>();
        try {
            for (Usuario guardado : leidos) {
                Usuario usuario = copiar(guardado);
                if (!ids.add(usuario.getId()) || !nombresUsuario.add(usuario.getNombreUsuario())) {
                    throw new IllegalArgumentException("ID o nombre de usuario repetido.");
                }
                usuarios.add(usuario);
            }
        } catch (IllegalArgumentException | NullPointerException error) {
            throw new IllegalStateException("usuarios.json contiene usuarios inválidos.", error);
        }
        datos = usuarios;
    }

    //Escribe en usuarios.json la lista que está en memoria.
    public void guardar() {
        gestor.escribir(ARCHIVO, datos, TIPO);
    }

    //Recibe un usuario y lo agrega o reemplaza por ID. Actualiza la lista solo si se pudo guardar.
    public void guardar(Usuario usuario) {
        Usuario copia = copiar(usuario);
        ArrayList<Usuario> usuarios = new ArrayList<>(datos);
        int indice = -1;
        for (int i = 0; i < usuarios.size(); i++) {
            Usuario actual = usuarios.get(i);
            if (actual.getId().equals(copia.getId())) {
                indice = i;
            } else if (actual.getNombreUsuario().equals(copia.getNombreUsuario())) {
                throw new IllegalArgumentException("Ya existe ese nombre de usuario.");
            }
        }
        if (indice == -1) usuarios.add(copia);
        else usuarios.set(indice, copia);
        gestor.escribir(ARCHIVO, usuarios, TIPO);
        datos = usuarios;
    }

    //Devuelve una lista con copias de los perfiles guardados.
    public ArrayList<Usuario> listarUsuarios() {
        var usuarios = new ArrayList<Usuario>();
        for (Usuario usuario : datos) usuarios.add(copiar(usuario));
        return usuarios;
    }

    //Busca por ID y devuelve una copia del perfil, o null si no aparece.
    public Usuario buscarUsuarioPorId(String id) {
        String criterio = exigirTexto(id);
        return buscar(usuario -> usuario.getId().equals(criterio));
    }

    //Busca por nombre y devuelve una copia del primer perfil que coincide, o null si no hay ninguno.
    public Usuario buscarUsuarioPorNombre(String nombre) {
        String criterio = exigirTexto(nombre);
        return buscar(usuario -> usuario.getNombre().equals(criterio));
    }

    //Busca por nombre de usuario y devuelve una copia del perfil, o null si no aparece.
    public Usuario buscarUsuarioPorNombreUsuario(String nombreUsuario) {
        String criterio = exigirTexto(nombreUsuario);
        return buscar(usuario -> usuario.getNombreUsuario().equals(criterio));
    }

    //Recibe una condición y devuelve una copia del primer perfil que la cumple, o null si no hay ninguno.
    private Usuario buscar(Predicate<Usuario> criterio) {
        for (Usuario usuario : datos) {
            if (criterio.test(usuario)) return copiar(usuario);
        }
        return null;
    }

    //Recibe un perfil y devuelve otro objeto con los mismos datos.
    private static Usuario copiar(Usuario usuario) {
        if (usuario == null) throw new IllegalArgumentException("El usuario es obligatorio.");
        return new Usuario(usuario.getId(), usuario.getNombre(), usuario.getNombreUsuario(),
                usuario.getPasswordHash(), usuario.getCarreraId(), usuario.getSedeId());
    }

    //Recibe el texto de búsqueda y lo devuelve sin espacios en los bordes. Da error si venía vacío.
    private static String exigirTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El criterio de búsqueda es obligatorio.");
        }
        return texto.strip();
    }
}
