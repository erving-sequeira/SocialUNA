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

    public ServicioUsuarios(RepositorioUsuariosJSON repositorioUsuarios,
                            RepositorioSedesJSON repositorioSedes,
                            RepositorioCarrerasJSON repositorioCarreras) {
        this.repositorioUsuarios = Objects.requireNonNull(repositorioUsuarios);
        this.repositorioSedes = Objects.requireNonNull(repositorioSedes);
        this.repositorioCarreras = Objects.requireNonNull(repositorioCarreras);
    }

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

    @Override
    public void cerrarSesion() {
        usuarioActual = null;
    }

    @Override
    public Usuario getUsuarioActual() {
        return usuarioActual == null ? null : copiar(usuarioActual);
    }

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

    @Override
    public Usuario obtenerPerfil(String id) {
        return repositorioUsuarios.buscarUsuarioPorId(id);
    }

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

    @Override
    public ArrayList<Carrera> listarCarreras() {
        ArrayList<Carrera> carreras = repositorioCarreras.listarCarreras();
        carreras.removeIf(carrera -> !carrera.isActiva());
        return carreras;
    }

    @Override
    public ArrayList<SedeCampus> listarSedes() {
        ArrayList<SedeCampus> sedes = repositorioSedes.listarSedes();
        sedes.removeIf(sede -> !sede.isActiva());
        return sedes;
    }

    private Usuario exigirSesion() {
        if (usuarioActual == null) {
            throw new IllegalStateException("Debes iniciar sesión para actualizar tus datos.");
        }
        return usuarioActual;
    }

    private Usuario buscarParaInicioSesion(String alias) {
        // Conserva el acceso con la grafía original si ya existían cuentas con mayúsculas.
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

    private static String normalizarNombreUsuario(String nombre) {
        return nombre.strip().toLowerCase(Locale.ROOT);
    }

    private static String calcularHash(String contrasena) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(contrasena.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 no está disponible.", error);
        }
    }

    private void validarRegistro(String nombre, String alias, String contrasena) {
        exigirTexto(nombre, "El nombre");
        exigirTexto(contrasena, "La contraseña");
        for (Usuario existente : repositorioUsuarios.listarUsuarios()) {
            if (normalizarNombreUsuario(existente.getNombreUsuario()).equals(alias)) {
                throw new IllegalArgumentException("El nombre de usuario ya existe.");
            }
        }
    }

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

    private static String exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " es obligatorio.");
        }
        return valor.strip();
    }

    private static Usuario copiar(Usuario usuario) {
        return new Usuario(usuario.getId(), usuario.getNombre(), usuario.getNombreUsuario(),
                usuario.getPasswordHash(), usuario.getCarreraId(), usuario.getSedeId());
    }
}
