package org.isp63.prog1.service;

import org.isp63.prog1.dao.CarritoDao;
import org.isp63.prog1.dao.ProductoDao;
import org.isp63.prog1.entities.Carrito;
import org.isp63.prog1.entities.ItemCarrito;
import org.isp63.prog1.exception.SinStockException;
import org.isp63.prog1.util.ConexionPool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class CarritoService {

  private final ProductoDao productoDao = new ProductoDao();
  private final CarritoDao carritoDao = new CarritoDao();

  public double calcularTotal(List<ItemCarrito> items) {
    return items.stream()
        .mapToDouble(ItemCarrito::getSubtotal)
        .sum();
  }

  public void validarStockDisponible(List<ItemCarrito> items) {
    List<String> faltantes = items.stream()
        .filter(item -> item.getCantidad() > item.getProducto().getStock())
        .map(item -> item.getProducto().getNombre())
        .collect(Collectors.toList());

    if (!faltantes.isEmpty()) {
      throw new SinStockException("Stock insuficiente para: " + String.join(", ", faltantes));
    }
  }

  public void finalizarCompra(Carrito carrito, List<ItemCarrito> items) {
    if (carrito == null || items == null || items.isEmpty()) {
      throw new IllegalArgumentException("No hay productos para finalizar la compra");
    }

    validarStockDisponible(items);

    try (Connection conn = ConexionPool.getConnection()) {
      conn.setAutoCommit(false);
      try {
        for (ItemCarrito item : items) {
          productoDao.descontarStock(conn, item.getProducto().getId(), item.getCantidad());
        }
        carritoDao.marcarFinalizado(conn, carrito.getId());
        conn.commit();
      } catch (SQLException e) {
        conn.rollback();
        throw new RuntimeException("Error al finalizar la compra", e);
      } finally {
        conn.setAutoCommit(true);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error de conexion al finalizar la compra", e);
    }
  }
}
