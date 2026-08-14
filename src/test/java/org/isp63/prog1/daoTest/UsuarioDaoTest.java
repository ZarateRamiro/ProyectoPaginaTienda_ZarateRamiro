package org.isp63.prog1.daoTest;

import org.isp63.prog1.dao.UsuarioDao;
import org.isp63.prog1.entities.Usuario;
import org.isp63.prog1.interfaces.AdminConexion;
import org.isp63.prog1.util.ConexionPool;
import org.isp63.prog1.util.Rol;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioDaoTest {

  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("tienda")
      .withUsername("test")
      .withPassword("test");

  private UsuarioDao usuarioDao;

  @BeforeAll
  static void setupDatabase() throws Exception {
    if (!mysql.isRunning()) {
      mysql.start();
    }

    System.setProperty("db.url", mysql.getJdbcUrl());
    System.setProperty("db.user", mysql.getUsername());
    System.setProperty("db.pass", mysql.getPassword());
    System.setProperty("db.password", mysql.getPassword());

    ConexionPool.close();
    AdminConexion.INSTANCE.recargarPoolParaTests();

    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {

      st.execute("CREATE TABLE IF NOT EXISTS usuario (" +
          "id INT AUTO_INCREMENT PRIMARY KEY, " +
          "nombre VARCHAR(100) NOT NULL, " +
          "email VARCHAR(100) NOT NULL UNIQUE, " +
          "password VARCHAR(100) NOT NULL, " +
          "rol ENUM('ADMIN','USUARIO') NOT NULL)");
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    usuarioDao = new UsuarioDao();

    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {

      st.execute("SET FOREIGN_KEY_CHECKS = 0");
      st.execute("TRUNCATE TABLE usuario");
      st.execute("SET FOREIGN_KEY_CHECKS = 1");
    }
  }

  // =================================================================================
  // TESTS PARA INSERT / GET BY ID
  // =================================================================================

  @Test
  void deberia_InsertarYObtenerUsuario_Cuando_DatosSonValidos() {
    // --- 1. ARRANGE ---
    Usuario nuevoUsuario = new Usuario(0, "Carlos Sainz", "carlos@mail.com", "hash123", Rol.USUARIO);

    // --- 2. ACT ---
    usuarioDao.insert(nuevoUsuario);
    Usuario recuperado = usuarioDao.getById(nuevoUsuario.getId());

    // --- 3. ASSERT ---
    assertThat(nuevoUsuario.getId()).isGreaterThan(0);
    assertThat(recuperado).isNotNull();
    assertThat(recuperado.getNombre()).isEqualTo("Carlos Sainz");
    assertThat(recuperado.getEmail()).isEqualTo("carlos@mail.com");
    assertThat(recuperado.getRol()).isEqualTo(Rol.USUARIO);
  }

  @Test
  void getById_DeberiaRetornarNull_CuandoNoExiste() {
    // --- ACT & ASSERT ---
    assertThat(usuarioDao.getById(999)).isNull();
  }

  // =================================================================================
  // TESTS PARA VALIDAR LOGIN
  // =================================================================================

  @Test
  void validarLogin_DeberiaRetornarUsuario_CuandoCredencialesSonCorrectas() {
    // --- 1. ARRANGE ---
    usuarioDao.insert(new Usuario(0, "admin", "admin@tienda.com", "admin123", "ADMIN"));

    // --- 2. ACT ---
    Usuario resultado = usuarioDao.validarLogin("admin", "admin123");

    // --- 3. ASSERT ---
    assertThat(resultado).isNotNull();
    assertThat(resultado.getNombre()).isEqualTo("admin");
    assertThat(resultado.getRol()).isEqualTo("ADMIN");
  }

  @Test
  void validarLogin_DeberiaRetornarNull_CuandoPasswordEsIncorrecta() {
    // --- 1. ARRANGE ---
    usuarioDao.insert(new Usuario(0, "juan", "juan@mail.com", "correcta", Rol.USUARIO));

    // --- 2. ACT ---
    Usuario resultado = usuarioDao.validarLogin("juan", "incorrecta");

    // --- 3. ASSERT ---
    assertThat(resultado).isNull();
  }

  @Test
  void validarLogin_DeberiaRetornarNull_CuandoUsuarioNoExiste() {
    // --- ACT & ASSERT ---
    assertThat(usuarioDao.validarLogin("fantasma", "123")).isNull();
  }

  // =================================================================================
  // TESTS PARA UPDATE
  // =================================================================================

  @Test
  void update_DeberiaActualizarNombreEmailYRol_PeroNoLaPassword() {
    // --- 1. ARRANGE ---
    Usuario original = new Usuario(0, "Juan Perez", "juan@mail.com", "claveOriginal", Rol.USUARIO);
    usuarioDao.insert(original);

    Usuario aEditar = usuarioDao.getById(original.getId());
    aEditar.setNombre("Juan Editado");
    aEditar.setEmail("juan.editado@mail.com");
    aEditar.setRol("ADMIN");

    // --- 2. ACT ---
    usuarioDao.update(aEditar);

    // --- 3. ASSERT ---
    Usuario modificado = usuarioDao.getById(original.getId());
    assertThat(modificado.getNombre()).isEqualTo("Juan Editado");
    assertThat(modificado.getEmail()).isEqualTo("juan.editado@mail.com");
    assertThat(modificado.getRol()).isEqualTo("ADMIN");
    // La contraseña no se modifica porque UsuarioDao.update() no la incluye en el UPDATE
    assertThat(modificado.getPassword()).isEqualTo("claveOriginal");
  }

  // =================================================================================
  // TESTS PARA DELETE / EXISTS
  // =================================================================================

  @Test
  void deberia_EliminarUsuario_Cuando_SeProporcionaIdValido() {
    // --- 1. ARRANGE ---
    Usuario usuario = new Usuario(0, "Borrar Yo", "borrar@mail.com", "123", Rol.USUARIO);
    usuarioDao.insert(usuario);
    int id = usuario.getId();

    assertThat(usuarioDao.existsById(id)).isTrue();

    // --- 2. ACT ---
    usuarioDao.delete(id);

    // --- 3. ASSERT ---
    assertThat(usuarioDao.existsById(id)).isFalse();
    assertThat(usuarioDao.getById(id)).isNull();
  }

  @Test
  void existsById_DeberiaRetornarFalse_CuandoNoExiste() {
    assertThat(usuarioDao.existsById(999)).isFalse();
  }

  // =================================================================================
  // TESTS PARA GET ALL / GET BY ROL
  // =================================================================================

  @Test
  void getAll_DeberiaRetornarTodosLosUsuarios_OrdenadosPorNombre() {
    // --- 1. ARRANGE ---
    usuarioDao.insert(new Usuario(0, "Zoe", "zoe@mail.com", "123", Rol.USUARIO));
    usuarioDao.insert(new Usuario(0, "Ana", "ana@mail.com", "123", "ADMIN"));

    // --- 2. ACT ---
    List<Usuario> todos = usuarioDao.getAll();

    // --- 3. ASSERT ---
    assertThat(todos).hasSize(2);
    assertThat(todos)
        .extracting(Usuario::getNombre)
        .containsExactly("Ana", "Zoe"); // orden alfabético por SQL_GETALL
  }

  @Test
  void getByRol_DeberiaFiltrarSoloPorElRolIndicado() {
    // --- 1. ARRANGE ---
    usuarioDao.insert(new Usuario(0, "Admin Uno", "admin1@mail.com", "123", "ADMIN"));
    usuarioDao.insert(new Usuario(0, "Usuario Uno", "usuario1@mail.com", "123", Rol.USUARIO));
    usuarioDao.insert(new Usuario(0, "Usuario Dos", "usuario2@mail.com", "123", Rol.USUARIO));

    // --- 2. ACT ---
    List<Usuario> admins = usuarioDao.getByRol("ADMIN");
    List<Usuario> usuariosComunes = usuarioDao.getAllUsuariosComunes();

    // --- 3. ASSERT ---
    assertThat(admins).hasSize(1);
    assertThat(admins.get(0).getNombre()).isEqualTo("Admin Uno");

    assertThat(usuariosComunes).hasSize(2);
    assertThat(usuariosComunes)
        .extracting(Usuario::getNombre)
        .containsExactlyInAnyOrder("Usuario Uno", "Usuario Dos");
  }
}