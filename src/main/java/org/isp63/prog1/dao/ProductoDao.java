package org.isp63.prog1.dao;

import org.isp63.prog1.entities.Producto;
import org.isp63.prog1.interfaces.AdmConnexion;
import org.isp63.prog1.interfaces.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Necesario para obtener ID generado
import java.util.ArrayList;
import java.util.List;

public class ProductoDao implements AdmConnexion, DAO<Producto, Integer> {

  // NOTA 1: Se recomienda eliminar 'private Connection conn = null;'
  // cuando se usa try-with-resources, ya que la conexión se gestiona localmente.
  // private Connection conn = null;

  private static final String SQL_INSERT =
      "INSERT INTO producto (nombre, descripcion, precio, imagen) " +
          "VALUES (?, ?, ?, ?)";

  private static final String SQL_UPDATE =
      "UPDATE producto SET " +
          "nombre = ?, descripcion = ?, precio = ?, imagen = ? " +
          "WHERE id = ?";

  private static final String SQL_DELETE =
      "DELETE FROM producto WHERE id = ?";

  private static final String SQL_GETALL =
      "SELECT * FROM producto ORDER BY nombre";

  private static final String SQL_GETBYID =
      "SELECT * FROM producto WHERE id = ?";

  // -------------------------------------------------------------------
  // NOTA 2: Optimización de getAll con try-with-resources
  // -------------------------------------------------------------------
  @Override
  public List<Producto> getAll() {
    List<Producto> listaProductos = new ArrayList<>();

    // Uso de try-with-resources: conn, pst, y rs se cierran automáticamente.
    try (Connection conn = obtenerConexion();
         PreparedStatement pst = conn.prepareStatement(SQL_GETALL);
         ResultSet rs = pst.executeQuery()) {

      while (rs.next()) {
        Producto producto = new Producto();
        producto.setId(rs.getInt("id"));
        producto.setNombre(rs.getString("nombre"));
        producto.setDescripcion(rs.getString("descripcion"));
        // NOTA: Usar rs.getDouble() si 'precio' es DECIMAL/DOUBLE en la DB.
        producto.setPrecio(rs.getDouble("precio"));
        producto.setImagen(rs.getString("imagen"));
        listaProductos.add(producto);
      }

    } catch (SQLException e) {
      System.err.println("Error al obtener todos los productos.");
      throw new RuntimeException(e);
    }
    return listaProductos;
  }

  // -------------------------------------------------------------------
  // 3. INSERT (Crear Producto)
  // -------------------------------------------------------------------
  @Override
  public void insert(Producto objeto) {
    Producto producto = objeto;

    try (Connection conn = obtenerConexion();
         // Se usa Statement.RETURN_GENERATED_KEYS para obtener el ID asignado.
         PreparedStatement pst = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

      pst.setString(1, producto.getNombre());
      pst.setString(2, producto.getDescripcion());
      pst.setDouble(3, producto.getPrecio());
      pst.setString(4, producto.getImagen());

      pst.executeUpdate();

      // Obtener el ID que la base de datos acaba de asignar
      try (ResultSet rs = pst.getGeneratedKeys()) {
        if (rs.next()) {
          producto.setId(rs.getInt(1));
          System.out.println("Producto insertado con ID: " + producto.getId());
        }
      }

    } catch (SQLException e) {
      System.err.println("Error al insertar el producto.");
      throw new RuntimeException(e);
    }
  }

  // -------------------------------------------------------------------
  // 4. UPDATE (Editar Producto)
  // -------------------------------------------------------------------
  @Override
  public void update(Producto objeto) {
    Producto producto = objeto;

    try (Connection conn = obtenerConexion();
         PreparedStatement pst = conn.prepareStatement(SQL_UPDATE)) {

      // Parámetros de actualización
      pst.setString(1, producto.getNombre());
      pst.setString(2, producto.getDescripcion());
      pst.setDouble(3, producto.getPrecio());
      pst.setString(4, producto.getImagen());

      // Parámetro de la condición WHERE
      pst.setInt(5, producto.getId());

      int resultado = pst.executeUpdate();
      if (resultado == 0) {
        System.out.println("Advertencia: No se encontró producto con ID " + producto.getId() + " para actualizar.");
      }

    } catch (SQLException e) {
      System.err.println("Error al actualizar el producto con ID: " + producto.getId());
      throw new RuntimeException(e);
    }
  }

  // -------------------------------------------------------------------
  // 5. DELETE (Eliminar Producto)
  // -------------------------------------------------------------------
  @Override
  public void delete(Integer id) {
    try (Connection conn = obtenerConexion();
         PreparedStatement pst = conn.prepareStatement(SQL_DELETE)) {

      pst.setInt(1, id);

      int resultado = pst.executeUpdate();
      if (resultado == 1) {
        System.out.println("Producto eliminado correctamente: " + id);
      } else {
        System.out.println("Advertencia: No se encontró producto con ID " + id + " para eliminar.");
      }

    } catch (SQLException e) {
      System.err.println("Error al eliminar el producto con ID: " + id);
      throw new RuntimeException(e);
    }
  }

  // -------------------------------------------------------------------
  // 6. GET BY ID (Obtener Producto Específico)
  // -------------------------------------------------------------------
  @Override
  public Producto getById(Integer id) {
    Producto producto = null;

    try (Connection conn = obtenerConexion();
         PreparedStatement pst = conn.prepareStatement(SQL_GETBYID)) {

      pst.setInt(1, id);

      try (ResultSet rs = pst.executeQuery()) {
        if (rs.next()) {
          producto = new Producto();
          producto.setId(rs.getInt("id"));
          producto.setNombre(rs.getString("nombre"));
          producto.setDescripcion(rs.getString("descripcion"));
          producto.setPrecio(rs.getDouble("precio"));
          producto.setImagen(rs.getString("imagen"));
        }
      }

    } catch (SQLException e) {
      System.err.println("Error al obtener producto por ID: " + id);
      throw new RuntimeException(e);
    }

    return producto;
  }

  // -------------------------------------------------------------------
  // 7. EXISTS BY ID (Verificar existencia)
  // -------------------------------------------------------------------
  @Override
  public boolean existsById(Integer id) {
    boolean existe = false;

    try (Connection conn = obtenerConexion();
         PreparedStatement pst = conn.prepareStatement(SQL_GETBYID)) {

      pst.setInt(1, id);

      try (ResultSet rs = pst.executeQuery()) {
        // Si hay una fila, el producto existe
        if (rs.next()) {
          existe = true;
        }
      }

    } catch (SQLException e) {
      System.err.println("Error al verificar existencia de producto: " + id);
      throw new RuntimeException(e);
    }

    return existe;
  }
}