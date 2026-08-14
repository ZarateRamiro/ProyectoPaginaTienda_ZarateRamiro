package org.isp63.prog1.service;

import org.isp63.prog1.entities.Carrito;
import org.isp63.prog1.entities.ItemCarrito;
import org.isp63.prog1.entities.Producto;
import org.isp63.prog1.exception.SinStockException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CarritoServiceTest {

  private final CarritoService carritoService = new CarritoService();

  @Test
  void calcularTotal_sumaSubtotalesConStream() {
    Producto campera = new Producto();
    campera.setNombre("Campera");
    campera.setStock(10);

    Producto medias = new Producto();
    medias.setNombre("Medias");
    medias.setStock(5);

    List<ItemCarrito> items = List.of(
        new ItemCarrito(1, new Carrito(1), campera, 2, 38000),
        new ItemCarrito(2, new Carrito(1), medias, 1, 12000)
    );

    assertEquals(88000, carritoService.calcularTotal(items), 0.001);
  }

  @Test
  void validarStockDisponible_lanzaExcepcionCuandoNoAlcanza() {
    Producto producto = new Producto();
    producto.setNombre("Zapatilla");
    producto.setStock(1);

    ItemCarrito item = new ItemCarrito(1, new Carrito(1), producto, 3, 1000);

    assertThrows(SinStockException.class, () -> carritoService.validarStockDisponible(List.of(item)));
  }
}
