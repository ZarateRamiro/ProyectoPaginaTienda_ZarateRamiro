package org.isp63.prog1.entities;

import java.time.LocalDate;

public class Carrito {
  private Integer id;
  private Usuario usuario;
  private LocalDate fechaDeCreacion;
  private String estado;

  public Carrito() {
  }

  public Carrito(Integer id) {
    this.id = id;
  }

  public Carrito(Integer id, Usuario usuario, LocalDate fechaDeCreacion, String estado) {
    this.id = id;
    this.usuario = usuario;
    this.fechaDeCreacion = fechaDeCreacion;
    this.estado = estado;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }

  public LocalDate getFechaDeCreacion() {
    return fechaDeCreacion;
  }

  public void setFechaDeCreacion(LocalDate fechaDeCreacion) {
    this.fechaDeCreacion = fechaDeCreacion;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }



  @Override
  public String toString() {
    return "Carrito{" +
        "usuario=" + usuario +
        ", fechaDeCreacion=" + fechaDeCreacion +
        ", estado='" + estado + '\'' +
        '}';
  }

}
