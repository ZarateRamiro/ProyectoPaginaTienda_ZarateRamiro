package org.isp63.prog1.entities;

import org.isp63.prog1.enums.TipoProducto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ItemCarritoTest {

  @Test
  void deberia_CrearItemCarritoConConstructorVacio() {
    ItemCarrito item = new ItemCarrito();

    assertThat(item.getId()).isNull();
    assertThat(item.getCarrito()).isNull();
    assertThat(item.getProducto()).isNull();
    assertThat(item.getCantidad()).isEqualTo(0);
    assertThat(item.getPrecioUnitario()).isEqualTo(0.0);
  }

  @Test
  void deberia_CrearItemCarritoConSoloId() {
    ItemCarrito item = new ItemCarrito(1);

    assertThat(item.getId()).isEqualTo(1);
    assertThat(item.getCarrito()).isNull();
    assertThat(item.getProducto()).isNull();
    assertThat(item.getCantidad()).isEqualTo(0);
    assertThat(item.getPrecioUnitario()).isEqualTo(0.0);
  }

  private int id;
  private String nombre;
  private String descripcion;
  private double precio;
  private String imagen;
  private int stock;
  private TipoProducto tipo;
  @Test
  void deberia_CrearItemCarritoConTodosLosCampos() {
    Carrito carrito = new Carrito(10);
    Producto producto = new Producto(20, "Remera","Remera nike talle m",1500.0,"https://redsport.vtexassets.com/arquivos/ids/998202-800-auto?v=637106314593600000&width=800&height=auto&aspect=true",5,TipoProducto.REMERA);
    ItemCarrito item = new ItemCarrito(5, carrito, producto, 2, 1500.0);

    assertThat(item.getId()).isEqualTo(5);
    assertThat(item.getCarrito()).isEqualTo(carrito);
    assertThat(item.getProducto()).isEqualTo(producto);
    assertThat(item.getCantidad()).isEqualTo(2);
    assertThat(item.getPrecioUnitario()).isEqualTo(1500.0);
  }

  @Test
  void deberia_PermitirModificarCamposConSetters() {
    ItemCarrito item = new ItemCarrito();
    Carrito carrito = new Carrito(99);
    Producto producto = new Producto(20, "Remera","Remera nike talle m",1500.0,"https://redsport.vtexassets.com/arquivos/ids/998202-800-auto?v=637106314593600000&width=800&height=auto&aspect=true",5,TipoProducto.REMERA);

    item.setId(100);
    item.setCarrito(carrito);
    item.setProducto(producto);
    item.setCantidad(3);
    item.setPrecioUnitario(25.0);

    assertThat(item.getId()).isEqualTo(100);
    assertThat(item.getCarrito()).isEqualTo(carrito);
    assertThat(item.getProducto()).isEqualTo(producto);
    assertThat(item.getCantidad()).isEqualTo(3);
    assertThat(item.getPrecioUnitario()).isEqualTo(25.0);
  }

  @Test
  void deberia_CalcularSubtotalCorrectamente() {
    Carrito carrito = new Carrito(1);
    Producto producto = new Producto(20, "Remera","Remera nike talle m",1500.0,"https://redsport.vtexassets.com/arquivos/ids/998202-800-auto?v=637106314593600000&width=800&height=auto&aspect=true",5,TipoProducto.REMERA);
    ItemCarrito item = new ItemCarrito(1, carrito, producto, 4, 100.0);

    double subtotal = item.getSubtotal();

    assertThat(subtotal).isEqualTo(400.0);
  }

  @Test
  void deberia_TenerToStringConCamposEsperados() {
    Carrito carrito = new Carrito(1);
    Producto producto = new Producto(20, "Remera","Remera nike talle m",1500.0,"https://redsport.vtexassets.com/arquivos/ids/998202-800-auto?v=637106314593600000&width=800&height=auto&aspect=true",5,TipoProducto.REMERA);
    ItemCarrito item = new ItemCarrito(1, carrito, producto, 2, 300.0);

    String resultado = item.toString();

    assertThat(resultado)
        .contains("carrito=" + carrito.toString())
        .contains("producto=" + producto.toString())
        .contains("cantidad=2")
        .contains("precioUnitario=300.0");
  }
}