package org.isp63.prog1.dao;

import org.isp63.prog1.entities.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDao {

  private Connection conn;

  public UsuarioDao(Connection conn) {
    this.conn = conn;
  }

  public Usuario validarLogin(String nombre, String password) {
    Usuario u = null;
    String sql = "SELECT * FROM usuario WHERE nombre = ? AND password = ?";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, nombre);
      ps.setString(2, password);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          u = new Usuario(
              rs.getInt("id"),
              rs.getString("nombre"),
              rs.getString("email"),
              rs.getString("password"),
              rs.getString("rol")
          );
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return u;
  }

  public List<Usuario> getAllUsuariosComunes() {
    List<Usuario> lista = new ArrayList<>();
    String sql = "SELECT * FROM usuario WHERE rol = 'usuario' ORDER BY nombre";

    try (PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        Usuario u = new Usuario(
            rs.getInt("id"),
            rs.getString("nombre"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("rol")
        );
        lista.add(u);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return lista;
  }

  public Usuario getById(int id) {
    Usuario u = null;
    String sql = "SELECT * FROM usuario WHERE id = ?";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, id);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          u = new Usuario(
              rs.getInt("id"),
              rs.getString("nombre"),
              rs.getString("email"),
              rs.getString("password"),
              rs.getString("rol")
          );
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return u;
  }

  public void insert(Usuario u) {
    String sql = "INSERT INTO usuario (nombre, email, password, rol) VALUES (?, ?, ?, ?)";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, u.getNombre());
      ps.setString(2, u.getEmail());
      ps.setString(3, u.getPassword());
      ps.setString(4, u.getRol());
      ps.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void update(Usuario u) {
    String sql = "UPDATE usuario SET nombre = ?, email = ?, rol = ? WHERE id = ?";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, u.getNombre());
      ps.setString(2, u.getEmail());
      ps.setString(3, u.getRol());
      ps.setInt(4, u.getId());
      ps.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void delete(int id) {
    String sql = "DELETE FROM usuario WHERE id = ?";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, id);
      ps.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
