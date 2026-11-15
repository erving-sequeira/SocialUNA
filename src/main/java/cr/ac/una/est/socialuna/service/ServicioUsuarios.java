package cr.ac.una.est.socialuna.service;

import cr.ac.una.est.socialuna.model.entity.Carrera;
import cr.ac.una.est.socialuna.model.entity.SedeCampus;
import cr.ac.una.est.socialuna.model.entity.Usuario;
import cr.ac.una.est.socialuna.persistence.RepositorioCarrerasJSON;
import cr.ac.una.est.socialuna.persistence.RepositorioSedesJSON;
import cr.ac.una.est.socialuna.persistence.RepositorioUsuariosJSON;
import cr.ac.una.est.socialuna.service.contract.IServicioUsuarios;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class ServicioUsuarios implements IServicioUsuarios {
    private final RepositorioUsuariosJSON repositorioUsuarios;
    private final RepositorioSedesJSON repositorioSedes;
    private final RepositorioCarrerasJSON repositorioCarreras;
    private Usuario usuarioActual;

    //Recibe los repositorios de usuarios, sedes y carreras para manejar cuentas y datos académicos.
    public ServicioUsuarios(RepositorioUsuariosJSON repositorioUsuarios,
                            RepositorioSedesJSON repositorioSedes,
                            RepositorioCarrerasJSON repositorioCarreras) {
        this.repositorioUsuarios = Objects.requireNonNull(repositorioUsuarios);
        this.repositorioSedes = Objects.requireNonNull(repositorioSedes);
        this.repositorioCarreras = Objects.requireNonNull(repositorioCarreras);
    }

    //Recibe nombre, usuario, contraseña e IDs de carrera y sede. Valida los datos, guarda la cuenta y la devuelve.
    @Override
    public Usuario registrar(String nombre, String nombreUsuario, String contrasena, String carreraId, String sedeId) {
        String alias = normalizarNombreUsuario(exigirTexto(nombreUsuario, "El nombre de usuario"));
        validarRegistro(nombre, alias, contrasena);
        String carrera = exigirTexto(carreraId, "La carrera");
        String sede = exigirTexto(sedeId, "La sede");
        validarDatosAcademicos(carrera, sede);

        Usuario nuevo = new Usuario(UUID.randomUUID().toString(), nombre, alias, calcularHash(contrasena), carrera, sede);
        repositorioUsuarios.guardar(nuevo);
        return nuevo;
    }

    //Recibe el usuario y la contraseña. Si coinciden, abre la sesión y devuelve una copia del perfil.
    @Override
    public Usuario iniciarSesion(String nombreUsuario, String contrasena) {
        if (usuarioActual != null) {
            throw new IllegalStateException("Cierra la sesión actual antes de iniciar otra.");
        }
        String alias = exigirTexto(nombreUsuario, "El nombre de usuario");
        exigirTexto(contrasena, "La contraseña");
        Usuario encontrado = buscarParaInicioSesion(alias);
        if (encontrado == null || !MessageDigest.isEqual(
                encontrado.getPasswordHash().getBytes(StandardCharsets.UTF_8),
                calcularHash(contrasena).getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("El nombre de usuario o la contraseña son incorrectos.");
        }
        usuarioActual = encontrado;
        return getUsuarioActual();
    }

    //Quita el perfil de la sesión activa.
    @Override
    public void cerrarSesion() {
        usuarioActual = null;
    }

    //Devuelve una copia del perfil en sesión, o null si nadie ha entrado.
    @Override
    public Usuario getUsuarioActual() {
        return usuarioActual == null ? null : copiar(usuarioActual);
    }

    //Recibe texto y devuelve los perfiles cuyo nombre o usuario lo contiene, sin importar mayúsculas.
    @Override
    public ArrayList<Usuario> buscarUsuarios(String texto) {
        var resultados = new ArrayList<Usuario>();
        if (texto == null || texto.isBlank()) return resultados;
        String consulta = normalizarNombreUsuario(texto);
        for (Usuario candidato : repositorioUsuarios.listarUsuarios()) {
            if (normalizarNombreUsuario(candidato.getNombre()).contains(consulta)
                    || normalizarNombreUsuario(candidato.getNombreUsuario()).contains(consulta)) {
                resultados.add(candidato);
            }
        }
        return resultados;
    }

    //Busca por ID y devuelve el perfil, o null si no existe.
    @Override
    public Usuario obtenerPerfil(String id) {
        return repositorioUsuarios.buscarUsuarioPorId(id);
    }

    //Recibe los IDs de carrera y sede, guarda el cambio y devuelve una copia del perfil actualizado.
    @Override
    public Usuario actualizarDatosAcademicos(String carreraId, String sedeId) {
        Usuario actual = exigirSesion();
        String carrera = exigirTexto(carreraId, "La carrera");
        String sede = exigirTexto(sedeId, "La sede");
        validarDatosAcademicos(carrera, sede);

        Usuario actualizado = copiar(actual);
        actualizado.setCarreraId(carrera);
        actualizado.setSedeId(sede);
        repositorioUsuarios.guardar(actualizado);
        usuarioActual = actualizado;
        return getUsuarioActual();
    }

    //Devuelve las carreras activas para que se puedan seleccionar.
    @Override
    public ArrayList<Carrera> listarCarreras() {
        ArrayList<Carrera> carreras = repositorioCarreras.listarCarreras();
        carreras.removeIf(carrera -> !carrera.isActiva());
        return carreras;
    }

    //Devuelve las sedes activas para que se puedan seleccionar.
    @Override
    public ArrayList<SedeCampus> listarSedes() {
        ArrayList<SedeCampus> sedes = repositorioSedes.listarSedes();
        sedes.removeIf(sede -> !sede.isActiva());
        return sedes;
    }

    //Devuelve el perfil en sesión o da error si nadie ha entrado.
    private Usuario exigirSesion() {
        if (usuarioActual == null) {
            throw new IllegalStateException("Debes iniciar sesión para actualizar tus datos.");
        }
        return usuarioActual;
    }

    //Busca el usuario tal como llegó. Si no hay coincidencia exacta, devuelve la única que coincide sin importar mayúsculas, o null.
    private Usuario buscarParaInicioSesion(String alias) {
        Usuario exacto = repositorioUsuarios.buscarUsuarioPorNombreUsuario(alias);
        if (exacto != null) return exacto;

        String normalizado = normalizarNombreUsuario(alias);
        Usuario coincidencia = null;
        for (Usuario candidato : repositorioUsuarios.listarUsuarios()) {
            if (normalizarNombreUsuario(candidato.getNombreUsuario()).equals(normalizado)) {
                if (coincidencia != null) return null;
                coincidencia = candidato;
            }
        }
        return coincidencia;
    }

    //Recibe texto y lo devuelve en minúsculas y sin espacios en los bordes.
    private static String normalizarNombreUsuario(String nombre) {
        return nombre.strip().toLowerCase(Locale.ROOT);
    }

    //Recibe la contraseña y devuelve su hash SHA-256 en hexadecimal.
    private static String calcularHash(String contrasena) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(contrasena.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 no está disponible.", error);
        }
    }

    //Revisa que el nombre y la contraseña tengan texto y que el nombre de usuario no esté ocupado.
    private void validarRegistro(String nombre, String alias, String contrasena) {
        exigirTexto(nombre, "El nombre");
        exigirTexto(contrasena, "La contraseña");
        for (Usuario existente : repositorioUsuarios.listarUsuarios()) {
            if (normalizarNombreUsuario(existente.getNombreUsuario()).equals(alias)) {
                throw new IllegalArgumentException("El nombre de usuario ya existe.");
            }
        }
    }

    //Revisa que los IDs recibidos sean de una carrera y una sede activas.
    private void validarDatosAcademicos(String carreraId, String sedeId) {
        Carrera carrera = repositorioCarreras.buscarCarreraPorId(carreraId);
        if (carrera == null || !carrera.isActiva()) {
            throw new IllegalArgumentException("Selecciona una carrera activa del catálogo.");
        }
        SedeCampus sede = repositorioSedes.buscarSedePorId(sedeId);
        if (sede == null || !sede.isActiva()) {
            throw new IllegalArgumentException("Selecciona una sede activa del catálogo.");
        }
    }

    //Recibe un valor y el nombre del campo. Devuelve el texto sin espacios en los bordes o avisa si falta.
    private static String exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " es obligatorio.");
        }
        return valor.strip();
    }

    //Recibe un perfil y devuelve una copia con los mismos datos.
    private static Usuario copiar(Usuario usuario) {
        return new Usuario(usuario.getId(), usuario.getNombre(), usuario.getNombreUsuario(),
                usuario.getPasswordHash(), usuario.getCarreraId(), usuario.getSedeId());
    }
}
