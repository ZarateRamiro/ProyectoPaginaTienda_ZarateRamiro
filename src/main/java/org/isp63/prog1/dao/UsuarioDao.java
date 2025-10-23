package org.isp63.prog1.dao;

import org.isp63.prog1.entities.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDao {
  private Connection conn;

  public UsuarioDao(Connection conn) {
    this.conn = conn;
  }

  public Usuario validarLogin(String nombre, String password) {
    Usuario u = null;
    try {
      String sql = "SELECT * FROM usuario WHERE nombre = ? AND password = ?";
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, nombre);
      ps.setString(2, password);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        u = new Usuario(
            rs.getInt("id"),
            rs.getString("nombre"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("rol")
        );
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return u;
  }
}
