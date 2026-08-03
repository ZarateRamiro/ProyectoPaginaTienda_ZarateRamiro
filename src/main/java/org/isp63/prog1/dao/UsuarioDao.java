package org.isp63.prog1.dao;

import org.isp63.prog1.entities.Usuario;
import org.isp63.prog1.interfaces.AdmConnexion;
import org.isp63.prog1.interfaces.DAO;
import org.isp63.prog1.util.Rol;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDao implements AdmConnexion, DAO<Usuario, Integer> {

  private static final String SQL_GETALL = "SELECT id, nombre, email, password, rol FROM usuario ORDER BY nombre";
  private static final String SQL_GETALL_USUARIOS =
      "SELECT id, nombre, email, password, rol FROM usuario WHERE rol = ? ORDER BY nombre";
  private static final String SQL_GETBYID = "SELECT id, nombre, email, password, rol FROM usuario WHERE id = ?";
  private static final String SQL_LOGIN =
      "SELECT id, nombre, email, password, rol FROM usuario WHERE nombre = ? AND password = ?";
  private static final String SQL_INSERT =
      "INSERT INTO usuario (nombre, email, password, rol) VALUES (?, ?, ?, ?)";
  private static final String SQL_UPDATE =
      "UPDATE usuario SET nombre = ?, email = ?, rol = ? WHERE id = ?";
  private static final String SQL_DELETE = "DELETE FROM usuario WHERE id = ?";

  public Usuario validarLogin(String nombre, String password) {
    try (Connection conn = obtenerConexion();
         PreparedStatement pst = conn.prepareStatement(SQL_LOGIN)) {

      pst.setString(1, nombre);
      pst.setString(2, password);

      try (ResultSet rs = pst.executeQuery()) {
        if (rs.next()) {
          return mapearUsuario(rs);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error al validar login", e);
    }

    return null;
  }

  public List<Usuario> getAllUsuariosComunes() {
    return getByRol(Rol.USUARIO);
  }

  public List<Usuario> getByRol(String rol) {
    List<Usuario> lista = new ArrayList<>();

    try (Connection conn = obtenerConexion();
         PreparedStatement pst = conn.prepareStatement(SQL_GETALL_USUARIOS)) {

      pst.setString(1, rol);

      try (ResultSet rs = pst.executeQuery()) {
        while (rs.next()) {
          lista.add(mapearUsuario(rs));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error al obtener usuarios por rol", e);
    }

    return lista;
  }

  @Override
  public List<Usuario> getAll() {
    List<Usuario> lista = new ArrayList<>();

    try (Connection conn = obtenerConexion();
         PreparedStatement pst = conn.prepareStatement(SQL_GETALL);
         ResultSet rs = pst.executeQuery()) {

      while (rs.next()) {
        lista.add(mapearUsuario(rs));
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error al obtener usuarios", e);
    }

    return lista;
  }

  @Override
  public void insert(Usuario usuario) {
    try (Connection conn = obtenerConexion();
         PreparedStatement pst = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

      pst.setString(1, usuario.getNombre());
      pst.setString(2, usuario.getEmail());
      pst.setString(3, usuario.getPassword());
      pst.setString(4, usuario.getRol());
      pst.executeUpdate();

      try (ResultSet rs = pst.getGeneratedKeys()) {
        if (rs.next()) {
          usuario.setId(rs.getInt(1));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error al insertar usuario", e);
    }
  }

  @Override
  public void update(Usuario usuario) {
    try (Connection conn = obtenerConexion();
         PreparedStatement pst = conn.prepareStatement(SQL_UPDATE)) {

      pst.setString(1, usuario.getNombre());
      pst.setString(2, usuario.getEmail());
      pst.setString(3, usuario.getRol());
      pst.setInt(4, usuario.getId());
      pst.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Error al actualizar usuario", e);
    }
  }

  @Override
  public void delete(Integer id) {
    try (Connection conn = obtenerConexion();
         PreparedStatement pst = conn.prepareStatement(SQL_DELETE)) {

      pst.setInt(1, id);
      pst.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Error al eliminar usuario", e);
    }
  }

  @Override
  public Usuario getById(Integer id) {
    try (Connection conn = obtenerConexion();
         PreparedStatement pst = conn.prepareStatement(SQL_GETBYID)) {

      pst.setInt(1, id);

      try (ResultSet rs = pst.executeQuery()) {
        if (rs.next()) {
          return mapearUsuario(rs);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error al obtener usuario por id", e);
    }

    return null;
  }

  @Override
  public boolean existsById(Integer id) {
    return getById(id) != null;
  }

  private Usuario mapearUsuario(ResultSet rs) throws SQLException {
    return new Usuario(
        rs.getInt("id"),
        rs.getString("nombre"),
        rs.getString("email"),
        rs.getString("password"),
        rs.getString("rol")
    );
  }
}
