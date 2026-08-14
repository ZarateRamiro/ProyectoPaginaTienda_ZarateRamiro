package org.isp63.prog1.entities;

import org.isp63.prog1.enums.TipoProducto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ProductoTest {

  @Test
  void deberia_CrearProductoConConstructorVacio() {
    Producto producto = new Producto();

    assertThat(producto.getId()).isEqualTo(0); // int por defecto
    assertThat(producto.getNombre()).isNull();
    assertThat(producto.getDescripcion()).isNull();
    assertThat(producto.getPrecio()).isEqualTo(0.0);
    assertThat(producto.getImagen()).isNull();
    assertThat(producto.getStock()).isEqualTo(0);
    assertThat(producto.getTipo()).isNull();
  }

  @Test
  void deberia_CrearProductoConSoloId() {
    Producto producto = new Producto(10);

    assertThat(producto.getId()).isEqualTo(10);
    assertThat(producto.getNombre()).isNull();
    assertThat(producto.getDescripcion()).isNull();
    assertThat(producto.getPrecio()).isEqualTo(0.0);
    assertThat(producto.getImagen()).isNull();
    assertThat(producto.getStock()).isEqualTo(0);
    assertThat(producto.getTipo()).isNull();
  }

  @Test
  void deberia_CrearProductoConTodosLosCampos() {
    Producto producto = new Producto(
        1,
        "Buzo NB",
        "Buzo new balance talle xl",
        2500.0,
        "imagen.jpg",
        5,
        TipoProducto.BUZO
    );

    assertThat(producto.getId()).isEqualTo(1);
    assertThat(producto.getNombre()).isEqualTo("Buzo NB");
    assertThat(producto.getDescripcion()).isEqualTo("Buzo new balance talle xl");
    assertThat(producto.getPrecio()).isEqualTo(2500.0);
    assertThat(producto.getImagen()).isEqualTo("imagen.jpg");
    assertThat(producto.getStock()).isEqualTo(5);
    assertThat(producto.getTipo()).isEqualTo(TipoProducto.BUZO);
  }

  @Test
  void deberia_PermitirModificarCamposConSetters() {
    Producto producto = new Producto();
    producto.setId(2);
    producto.setNombre("Mouse");
    producto.setDescripcion("Mouse inalámbrico");
    producto.setPrecio(50.0);
    producto.setImagen("mouse.png");
    producto.setStock(20);
    producto.setTipo(TipoProducto.CAMPERA);

    assertThat(producto.getId()).isEqualTo(2);
    assertThat(producto.getNombre()).isEqualTo("Mouse");
    assertThat(producto.getDescripcion()).isEqualTo("Mouse inalámbrico");
    assertThat(producto.getPrecio()).isEqualTo(50.0);
    assertThat(producto.getImagen()).isEqualTo("mouse.png");
    assertThat(producto.getStock()).isEqualTo(20);
    assertThat(producto.getTipo()).isEqualTo(TipoProducto.CAMPERA);
  }
}