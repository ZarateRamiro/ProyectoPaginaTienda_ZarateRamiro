package org.isp63.prog1.entities;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class UsuarioTest {

  @Test
  void deberia_CrearUsuarioConConstructorVacio() {
    Usuario usuario = new Usuario();

    assertThat(usuario.getId()).isEqualTo(0); // int por defecto
    assertThat(usuario.getNombre()).isNull();
    assertThat(usuario.getEmail()).isNull();
    assertThat(usuario.getPassword()).isNull();
    assertThat(usuario.getRol()).isNull();
  }

  @Test
  void deberia_CrearUsuarioConTodosLosCampos() {
    Usuario usuario = new Usuario(
        1,
        "Juan Perez",
        "juan@mail.com",
        "1234",
        "ADMIN"
    );

    assertThat(usuario.getId()).isEqualTo(1);
    assertThat(usuario.getNombre()).isEqualTo("Juan Perez");
    assertThat(usuario.getEmail()).isEqualTo("juan@mail.com");
    assertThat(usuario.getPassword()).isEqualTo("1234");
    assertThat(usuario.getRol()).isEqualTo("ADMIN");
  }

  @Test
  void deberia_PermitirModificarCamposConSetters() {
    Usuario usuario = new Usuario();
    usuario.setId(2);
    usuario.setNombre("Ana Gomez");
    usuario.setEmail("ana@mail.com");
    usuario.setPassword("abcd");
    usuario.setRol("TECNICO");

    assertThat(usuario.getId()).isEqualTo(2);
    assertThat(usuario.getNombre()).isEqualTo("Ana Gomez");
    assertThat(usuario.getEmail()).isEqualTo("ana@mail.com");
    assertThat(usuario.getPassword()).isEqualTo("abcd");
    assertThat(usuario.getRol()).isEqualTo("TECNICO");
  }
}