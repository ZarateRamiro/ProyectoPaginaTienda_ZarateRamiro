package org.isp63.prog1.dao;

import org.isp63.prog1.entities.Carrito;
import org.isp63.prog1.entities.Usuario;
import org.isp63.prog1.interfaces.DAO;
import org.isp63.prog1.util.ConexionPool;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CarritoDao implements DAO<Carrito, Integer> {

  private static final String ESTADO_ACTIVO = "ACTIVO";

  private static final String SELECT_BASE =
      "SELECT c.id, c.fecha_creacion, c.estado, " +
          "u.id AS usuario_id, u.nombre AS usuario_nombre, u.email AS usuario_email, " +
          "u.password AS usuario_password, u.rol AS usuario_rol " +
          "FROM carrito c INNER JOIN usuario u ON c.usuario_id = u.id ";

  private static final String SQL_GETALL = SELECT_BASE + "ORDER BY c.fecha_creacion DESC";
  private static final String SQL_GETBYID = SELECT_BASE + "WHERE c.id = ?";
  private static final String SQL_GET_ACTIVO_BY_USUARIO =
      SELECT_BASE + "WHERE c.usuario_id = ? AND c.estado = 'ACTIVO' ORDER BY c.id DESC LIMIT 1";
  private static final String SQL_INSERT =
      "INSERT INTO carrito (usuario_id, fecha_creacion, estado) VALUES (?, ?, ?)";
  private static final String SQL_UPDATE =
      "UPDATE carrito SET usuario_id = ?, fecha_creacion = ?, estado = ? WHERE id = ?";
  private static final String SQL_DELETE = "DELETE FROM carrito WHERE id = ?";

  @Override
  public List<Carrito> getAll() {
    List<Carrito> carritos = new ArrayList<>();

    try (Connection conn = ConexionPool.getConnection();
         PreparedStatement pst = conn.prepareStatement(SQL_GETALL);
         ResultSet rs = pst.executeQuery()) {

      while (rs.next()) {
        carritos.add(mapearCarrito(rs));
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error al obtener carritos", e);
    }

    return carritos;
  }

  @Override
  public void insert(Carrito carrito) {
    try (Connection conn = ConexionPool.getConnection();
         PreparedStatement pst = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

      pst.setInt(1, carrito.getUsuario().getId());
      pst.setDate(2, Date.valueOf(carrito.getFechaDeCreacion()));
      pst.setString(3, carrito.getEstado());
      pst.executeUpdate();

      try (ResultSet rs = pst.getGeneratedKeys()) {
        if (rs.next()) {
          carrito.setId(rs.getInt(1));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error al insertar carrito", e);
    }
  }

  @Override
  public void update(Carrito carrito) {
    try (Connection conn = ConexionPool.getConnection();
         PreparedStatement pst = conn.prepareStatement(SQL_UPDATE)) {

      pst.setInt(1, carrito.getUsuario().getId());
      pst.setDate(2, Date.valueOf(carrito.getFechaDeCreacion()));
      pst.setString(3, carrito.getEstado());
      pst.setInt(4, carrito.getId());
      pst.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Error al actualizar carrito", e);
    }
  }

  @Override
  public void delete(Integer id) {
    try (Connection conn = ConexionPool.getConnection();
         PreparedStatement pst = conn.prepareStatement(SQL_DELETE)) {

      pst.setInt(1, id);
      pst.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Error al eliminar carrito", e);
    }
  }

  @Override
  public Carrito getById(Integer id) {
    try (Connection conn = ConexionPool.getConnection();
         PreparedStatement pst = conn.prepareStatement(SQL_GETBYID)) {

      pst.setInt(1, id);

      try (ResultSet rs = pst.executeQuery()) {
        if (rs.next()) {
          return mapearCarrito(rs);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error al obtener carrito por id", e);
    }

    return null;
  }

  public Carrito getActivoByUsuarioId(Integer usuarioId) {
    try (Connection conn = ConexionPool.getConnection();
         PreparedStatement pst = conn.prepareStatement(SQL_GET_ACTIVO_BY_USUARIO)) {

      pst.setInt(1, usuarioId);

      try (ResultSet rs = pst.executeQuery()) {
        if (rs.next()) {
          return mapearCarrito(rs);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error al obtener carrito activo", e);
    }

    return null;
  }

  public Carrito obtenerOCrearActivo(Usuario usuario) {
    Carrito carrito = getActivoByUsuarioId(usuario.getId());
    if (carrito != null) {
      return carrito;
    }

    carrito = new Carrito(null, usuario, LocalDate.now(), ESTADO_ACTIVO);
    insert(carrito);
    return carrito;
  }

  public void marcarFinalizado(Connection conn, int carritoId) throws SQLException {
    String sql = "UPDATE carrito SET estado = 'FINALIZADO' WHERE id = ? AND estado = 'ACTIVO'";
    try (PreparedStatement pst = conn.prepareStatement(sql)) {
      pst.setInt(1, carritoId);
      int filas = pst.executeUpdate();
      if (filas == 0) {
        throw new SQLException("No se pudo finalizar el carrito id " + carritoId);
      }
    }
  }

  @Override
  public boolean existsById(Integer id) {
    return getById(id) != null;
  }

  private Carrito mapearCarrito(ResultSet rs) throws SQLException {
    Usuario usuario = new Usuario(
        rs.getInt("usuario_id"),
        rs.getString("usuario_nombre"),
        rs.getString("usuario_email"),
        rs.getString("usuario_password"),
        rs.getString("usuario_rol")
    );

    return new Carrito(
        rs.getInt("id"),
        usuario,
        rs.getDate("fecha_creacion").toLocalDate(),
        rs.getString("estado")
    );
  }
}