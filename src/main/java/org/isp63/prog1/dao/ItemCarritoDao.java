package org.isp63.prog1.dao;

import org.isp63.prog1.entities.Carrito;
import org.isp63.prog1.entities.ItemCarrito;
import org.isp63.prog1.entities.Producto;
import org.isp63.prog1.enums.TipoProducto;
import org.isp63.prog1.interfaces.AdmConnexion;
import org.isp63.prog1.interfaces.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ItemCarritoDao implements AdmConnexion, DAO<ItemCarrito, Integer> {

  private static final String SELECT_BASE =
      "SELECT\n" +
          "    ic.id,\n" +
          "    ic.carrito_id,\n" +
          "    ic.producto_id,\n" +
          "    ic.cantidad,\n" +
          "    ic.precio_unitario,\n" +
          "\n" +
          "    p.nombre AS producto_nombre,\n" +
          "    p.descripcion AS producto_descripcion,\n" +
          "    p.precio AS producto_precio,\n" +
          "    p.imagen AS producto_imagen,\n" +
          "    p.stock AS producto_stock,\n" +
          "    p.tipo AS producto_tipo\n" +
          "\n" +
          "FROM item_carrito ic\n" +
          "JOIN producto p ON ic.producto_id = p.id\n";

  private static final String SQL_GETALL = SELECT_BASE + " ORDER BY ic.id";
  private static final String SQL_GETBYID = SELECT_BASE + " WHERE ic.id = ?";
  private static final String SQL_GET_BY_CARRITO = SELECT_BASE + " WHERE ic.carrito_id = ? ORDER BY ic.id";
  private static final String SQL_GET_BY_CARRITO_AND_PRODUCTO =
      SELECT_BASE + " WHERE ic.carrito_id = ? AND ic.producto_id = ?";
  private static final String SQL_INSERT =
      "INSERT INTO item_carrito (carrito_id, producto_id, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
  private static final String SQL_UPDATE =
      "UPDATE item_carrito SET carrito_id = ?, producto_id = ?, cantidad = ?, precio_unitario = ? WHERE id = ?";
  private static final String SQL_UPDATE_CANTIDAD = "UPDATE item_carrito SET cantidad = ? WHERE id = ?";
  private static final String SQL_DELETE = "DELETE FROM item_carrito WHERE id = ?";
  private static final String SQL_DELETE_BY_CARRITO = "DELETE FROM item_carrito WHERE carrito_id = ?";

  @Override
  public List<ItemCarrito> getAll() {
    List<ItemCarrito> items = new ArrayList<>();

    try (Connection conn = obtenerConexion();
         PreparedStatement pst = conn.prepareStatement(SQL_GETALL);
         ResultSet rs = pst.executeQuery()) {

      while (rs.next()) {
        items.add(mapearItem(rs));
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error al obtener items de carrito", e);
    }

    return items;
  }

  public List<ItemCarrito> getByCarritoId(Integer carritoId) {
    List<ItemCarrito> items = new ArrayList<>();

    try (Connection conn = obtenerConexion();
         PreparedStatement pst = conn.prepareStatement(SQL_GET_BY_CARRITO)) {

      pst.setInt(1, carritoId);

      try (ResultSet rs = pst.executeQuery()) {
        while (rs.next()) {
          items.add(mapearItem(rs));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error al obtener items del carrito", e);
    }

    return items;
  }

  public ItemCarrito getByCarritoYProducto(Integer carritoId, Integer productoId) {
    try (Connection conn = obtenerConexion();
         PreparedStatement pst = conn.prepareStatement(SQL_GET_BY_CARRITO_AND_PRODUCTO)) {

      pst.setInt(1, carritoId);
      pst.setInt(2, productoId);

      try (ResultSet rs = pst.executeQuery()) {
        if (rs.next()) {
          return mapearItem(rs);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error al buscar item del carrito", e);
    }

    return null;
  }

  public void agregarOIncrementar(Carrito carrito, Producto producto, int cantidad) {
    if (producto == null || producto.getStock() <= 0 || cantidad <= 0) {
      return;
    }

    ItemCarrito existente = getByCarritoYProducto(carrito.getId(), producto.getId());
    int cantidadFinal = existente == null
        ? Math.min(cantidad, producto.getStock())
        : Math.min(existente.getCantidad() + cantidad, producto.getStock());

    if (cantidadFinal <= 0) {
      return;
    }

    if (existente != null) {
      existente.setCantidad(cantidadFinal);
      update(existente);
      return;
    }

    ItemCarrito nuevo = new ItemCarrito(null, carrito, producto, cantidadFinal, producto.getPrecio());
    insert(nuevo);
  }

  public void actualizarCantidad(Integer itemId, int cantidad, int stockDisponible) {
    if (cantidad <= 0) {
      delete(itemId);
      return;
    }

    int cantidadFinal = Math.min(cantidad, stockDisponible);
    if (cantidadFinal <= 0) {
      delete(itemId);
      return;
    }

    try (Connection conn = obtenerConexion();
         PreparedStatement pst = conn.prepareStatement(SQL_UPDATE_CANTIDAD)) {

      pst.setInt(1, cantidadFinal);
      pst.setInt(2, itemId);
      pst.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Error al actualizar cantidad del item", e);
    }
  }

  public boolean perteneceAlCarrito(Integer itemId, Integer carritoId) {
    ItemCarrito item = getById(itemId);
    return item != null
        && item.getCarrito() != null
        && item.getCarrito().getId() != null
        && item.getCarrito().getId().equals(carritoId);
  }

  public void actualizarCantidad(Integer itemId, int cantidad) {
    if (cantidad <= 0) {
      delete(itemId);
      return;
    }

    try (Connection conn = obtenerConexion();
         PreparedStatement pst = conn.prepareStatement(SQL_UPDATE_CANTIDAD)) {

      pst.setInt(1, cantidad);
      pst.setInt(2, itemId);
      pst.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Error al actualizar cantidad del item", e);
    }
  }

  public void vaciarCarrito(Integer carritoId) {
    try (Connection conn = obtenerConexion();
         PreparedStatement pst = conn.prepareStatement(SQL_DELETE_BY_CARRITO)) {

      pst.setInt(1, carritoId);
      pst.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Error al vaciar carrito", e);
    }
  }

  @Override
  public void insert(ItemCarrito item) {
    try (Connection conn = obtenerConexion();
         PreparedStatement pst = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

      pst.setInt(1, item.getCarrito().getId());
      pst.setInt(2, item.getProducto().getId());
      pst.setInt(3, item.getCantidad());
      pst.setDouble(4, item.getPrecioUnitario());
      pst.executeUpdate();

      try (ResultSet rs = pst.getGeneratedKeys()) {
        if (rs.next()) {
          item.setId(rs.getInt(1));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error al insertar item de carrito", e);
    }
  }

  @Override
  public void update(ItemCarrito item) {
    try (Connection conn = obtenerConexion();
         PreparedStatement pst = conn.prepareStatement(SQL_UPDATE)) {

      pst.setInt(1, item.getCarrito().getId());
      pst.setInt(2, item.getProducto().getId());
      pst.setInt(3, item.getCantidad());
      pst.setDouble(4, item.getPrecioUnitario());
      pst.setInt(5, item.getId());
      pst.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Error al actualizar item de carrito", e);
    }
  }

  @Override
  public void delete(Integer id) {
    try (Connection conn = obtenerConexion();
         PreparedStatement pst = conn.prepareStatement(SQL_DELETE)) {

      pst.setInt(1, id);
      pst.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Error al eliminar item de carrito", e);
    }
  }

  @Override
  public ItemCarrito getById(Integer id) {
    try (Connection conn = obtenerConexion();
         PreparedStatement pst = conn.prepareStatement(SQL_GETBYID)) {

      pst.setInt(1, id);

      try (ResultSet rs = pst.executeQuery()) {
        if (rs.next()) {
          return mapearItem(rs);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error al obtener item de carrito por id", e);
    }

    return null;
  }

  @Override
  public boolean existsById(Integer id) {
    return getById(id) != null;
  }

  private ItemCarrito mapearItem(ResultSet rs) throws SQLException {

    Producto producto = new Producto();

    producto.setId(rs.getInt("producto_id"));
    producto.setNombre(rs.getString("producto_nombre"));
    producto.setDescripcion(rs.getString("producto_descripcion"));
    producto.setPrecio(rs.getDouble("producto_precio"));
    producto.setImagen(rs.getString("producto_imagen"));
    producto.setStock(rs.getInt("producto_stock"));
    producto.setTipo(
        TipoProducto.valueOf(rs.getString("producto_tipo"))
    );

    Carrito carrito = new Carrito(rs.getInt("carrito_id"));

    return new ItemCarrito(
        rs.getInt("id"),
        carrito,
        producto,
        rs.getInt("cantidad"),
        rs.getDouble("precio_unitario")
    );
  }}