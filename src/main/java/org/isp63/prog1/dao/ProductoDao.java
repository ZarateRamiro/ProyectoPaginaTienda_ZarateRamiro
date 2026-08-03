package org.isp63.prog1.dao;

import org.isp63.prog1.enums.TipoProducto;
import org.isp63.prog1.entities.Producto;
import org.isp63.prog1.exception.SinStockException;
import org.isp63.prog1.interfaces.AdmConnexion;
import org.isp63.prog1.interfaces.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductoDao implements AdmConnexion, DAO<Producto, Integer> {

  private static final String SELECT_BASE =
      "SELECT id, nombre, descripcion, precio, imagen, stock, tipo FROM producto ";

  private static final String SQL_INSERT =
      "INSERT INTO producto (nombre, descripcion, precio, imagen, stock, tipo) VALUES (?, ?, ?, ?, ?, ?)";

  private static final String SQL_UPDATE =
      "UPDATE producto SET nombre = ?, descripcion = ?, precio = ?, imagen = ?, stock = ?, tipo = ? WHERE id = ?";

  private static final String SQL_DELETE =
      "DELETE FROM producto WHERE id = ?";

  private static final String SQL_GETALL =
      SELECT_BASE + "ORDER BY nombre";

  private static final String SQL_GETBYID =
      SELECT_BASE + "WHERE id = ?";

  @Override
  public List<Producto> getAll() {

    List<Producto> productos = new ArrayList<>();

    try (Connection conn = obtenerConexion();
         PreparedStatement pst = conn.prepareStatement(SQL_GETALL);
         ResultSet rs = pst.executeQuery()) {

      while (rs.next()) {
        productos.add(mapearProducto(rs));
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error al obtener productos", e);
    }

    return productos;
  }

  @Override
  public void insert(Producto producto) {

    try (Connection conn = obtenerConexion();
         PreparedStatement pst = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

      cargarParametrosProducto(pst, producto);

      pst.executeUpdate();

      try (ResultSet rs = pst.getGeneratedKeys()) {
        if (rs.next()) {
          producto.setId(rs.getInt(1));
        }
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error al insertar producto", e);
    }
  }

  @Override
  public void update(Producto producto) {

    try (Connection conn = obtenerConexion();
         PreparedStatement pst = conn.prepareStatement(SQL_UPDATE)) {

      cargarParametrosProducto(pst, producto);

      pst.setInt(7, producto.getId());

      pst.executeUpdate();

    } catch (SQLException e) {
      throw new RuntimeException("Error al actualizar producto", e);
    }
  }

  @Override
  public void delete(Integer id) {

    try (Connection conn = obtenerConexion();
         PreparedStatement pst = conn.prepareStatement(SQL_DELETE)) {

      pst.setInt(1, id);

      pst.executeUpdate();

    } catch (SQLException e) {
      throw new RuntimeException("Error al eliminar producto", e);
    }
  }

  @Override
  public Producto getById(Integer id) {

    try (Connection conn = obtenerConexion();
         PreparedStatement pst = conn.prepareStatement(SQL_GETBYID)) {

      pst.setInt(1, id);

      try (ResultSet rs = pst.executeQuery()) {

        if (rs.next()) {
          return mapearProducto(rs);
        }
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error al obtener producto", e);
    }

    return null;
  }

  @Override
  public boolean existsById(Integer id) {
    return getById(id) != null;
  }

  public void descontarStock(Connection conn, int productoId, int cantidad) throws SQLException {

    String sql = "UPDATE producto SET stock = stock - ? WHERE id = ? AND stock >= ?";

    try (PreparedStatement pst = conn.prepareStatement(sql)) {

      pst.setInt(1, cantidad);
      pst.setInt(2, productoId);
      pst.setInt(3, cantidad);

      int filas = pst.executeUpdate();

      if (filas == 0) {
        throw new SinStockException(
            "No hay stock suficiente para el producto id " + productoId
        );
      }
    }
  }

  private void cargarParametrosProducto(PreparedStatement pst, Producto producto)
      throws SQLException {

    pst.setString(1, producto.getNombre());
    pst.setString(2, producto.getDescripcion());
    pst.setDouble(3, producto.getPrecio());
    pst.setString(4, producto.getImagen());
    pst.setInt(5, producto.getStock());
    pst.setString(6, producto.getTipo().name());
  }

  private Producto mapearProducto(ResultSet rs)
      throws SQLException {

    Producto producto = new Producto();

    producto.setId(rs.getInt("id"));
    producto.setNombre(rs.getString("nombre"));
    producto.setDescripcion(rs.getString("descripcion"));
    producto.setPrecio(rs.getDouble("precio"));
    producto.setImagen(rs.getString("imagen"));
    producto.setStock(rs.getInt("stock"));
    producto.setTipo(
        TipoProducto.valueOf(rs.getString("tipo"))
    );

    return producto;
  }

}