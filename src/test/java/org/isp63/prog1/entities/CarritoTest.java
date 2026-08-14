package org.isp63.prog1.entities;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CarritoTest {

  @Test
  void deberia_CrearCarritoConConstructorVacio() {
    Carrito carrito = new Carrito();

    assertThat(carrito.getId()).isNull();
    assertThat(carrito.getUsuario()).isNull();
    assertThat(carrito.getFechaDeCreacion()).isNull();
    assertThat(carrito.getEstado()).isNull();
  }

  @Test
  void deberia_CrearCarritoConSoloId() {
    Carrito carrito = new Carrito(1);

    assertThat(carrito.getId()).isEqualTo(1);
    assertThat(carrito.getUsuario()).isNull();
    assertThat(carrito.getFechaDeCreacion()).isNull();
    assertThat(carrito.getEstado()).isNull();
  }

  @Test
  void deberia_CrearCarritoConTodosLosCampos() {
    Usuario usuario = new Usuario(30,"Leon","leon@gmail.com","1234","Usuario");
    LocalDate fecha = LocalDate.of(2024, 5, 10);

    Carrito carrito = new Carrito(10, usuario, fecha, "ABIERTO");

    assertThat(carrito.getId()).isEqualTo(10);
    assertThat(carrito.getUsuario()).isEqualTo(usuario);
    assertThat(carrito.getFechaDeCreacion()).isEqualTo(fecha);
    assertThat(carrito.getEstado()).isEqualTo("ABIERTO");
  }

  @Test
  void deberia_PermitirModificarCamposConSetters() {
    Carrito carrito = new Carrito();
    Usuario usuario = new Usuario(47,"Ana","Ana@gmail.com","1234","Usuario");
    LocalDate fecha = LocalDate.of(2025, 1, 1);

    carrito.setId(99);
    carrito.setUsuario(usuario);
    carrito.setFechaDeCreacion(fecha);
    carrito.setEstado("CERRADO");

    assertThat(carrito.getId()).isEqualTo(99);
    assertThat(carrito.getUsuario()).isEqualTo(usuario);
    assertThat(carrito.getFechaDeCreacion()).isEqualTo(fecha);
    assertThat(carrito.getEstado()).isEqualTo("CERRADO");
  }

  @Test
  void deberia_TenerToStringConCamposEsperados() {
    Usuario usuario = new Usuario(12,"Jose","joselo@gmail.com","1234","Usuario");
    LocalDate fecha = LocalDate.of(2025, 1, 1);
    Carrito carrito = new Carrito(5, usuario, fecha, "EN_PROCESO");

    String resultado = carrito.toString();

    assertThat(resultado)
        .contains("usuario=" + usuario.toString())
        .contains("fechaDeCreacion=" + fecha.toString())
        .contains("estado='EN_PROCESO'");
  }
}
