package org.isp63.prog1.entities;

public class ItemCarrito {
  private Integer id;
  private Carrito carrito;
  private Producto producto;
  private int cantidad;
  private double precioUnitario;

  public ItemCarrito() {
  }

  public ItemCarrito(Integer id) {
    this.id = id;
  }

  public ItemCarrito(Integer id, Carrito carrito, Producto producto, int cantidad, double precioUnitario) {
    this.id = id;
    this.carrito = carrito;
    this.producto = producto;
    this.cantidad = cantidad;
    this.precioUnitario = precioUnitario;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Carrito getCarrito() {
    return carrito;
  }

  public void setCarrito(Carrito carrito) {
    this.carrito = carrito;
  }

  public Producto getProducto() {
    return producto;
  }

  public void setProducto(Producto producto) {
    this.producto = producto;
  }

  public int getCantidad() {
    return cantidad;
  }

  public void setCantidad(int cantidad) {
    this.cantidad = cantidad;
  }

  public double getPrecioUnitario() {
    return precioUnitario;
  }

  public void setPrecioUnitario(double precioUnitario) {
    this.precioUnitario = precioUnitario;
  }

  public double getSubtotal() {
    return cantidad * precioUnitario;
  }

  @Override
  public String toString() {
    return "ItemCarrito{" +
        "carrito=" + carrito +
        ", producto=" + producto +
        ", cantidad=" + cantidad +
        ", precioUnitario=" + precioUnitario +
        '}';
  }
}
