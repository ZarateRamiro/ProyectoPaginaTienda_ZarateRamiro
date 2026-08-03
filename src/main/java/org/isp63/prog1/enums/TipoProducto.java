package org.isp63.prog1.enums;

public enum TipoProducto {

  REMERA("Remera"),
  CAMPERA("Campera"),
  BUZO("Buzo"),
  PANTALON("Pantalón"),
  JEAN("Jean"),
  SHORT("Short"),
  ZAPATILLAS("Zapatillas"),
  ZAPATOS("Zapatos"),
  BOTAS("Botas"),
  SANDALIAS("Sandalias"),
  GORRA("Gorra"),
  ACCESORIOS("Accesorios");

  private final String descripcion;

  TipoProducto(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getDescripcion() {
    return descripcion;
  }
}