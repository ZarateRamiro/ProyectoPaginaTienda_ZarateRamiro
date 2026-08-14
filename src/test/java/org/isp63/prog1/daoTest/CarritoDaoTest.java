package org.isp63.prog1.daoTest;

import org.isp63.prog1.dao.CarritoDao;
import org.isp63.prog1.entities.Carrito;
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
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CarritoDaoTest {

  // Inicialización manual para asegurar el orden estricto antes de tocar ConexionPool
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("tienda")
      .withUsername("test")
      .withPassword("test");

  private CarritoDao carritoDao;
  private Usuario usuarioBase;

  @BeforeAll
  static void setupDatabase() throws Exception {
    // 1. Iniciar el contenedor manualmente si no está corriendo
    if (!mysql.isRunning()) {
      mysql.start();
    }

    // 2. Setear las propiedades del sistema inmediatamente después de arrancar
    System.setProperty("db.url", mysql.getJdbcUrl());
    System.setProperty("db.user", mysql.getUsername());
    System.setProperty("db.pass", mysql.getPassword());
    System.setProperty("db.password", mysql.getPassword()); // Seteamos ambas por compatibilidad

    // 3. Forzar el reinicio/recarga del pool de conexiones con la URL de Testcontainers
    ConexionPool.close();
    AdminConexion.INSTANCE.recargarPoolParaTests();

    // 4. Crear la estructura inicial del schema
    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {

      st.execute("CREATE TABLE IF NOT EXISTS usuario (" +
          "id INT AUTO_INCREMENT PRIMARY KEY, " +
          "nombre VARCHAR(100) NOT NULL, " +
          "email VARCHAR(100) NOT NULL UNIQUE, " +
          "password VARCHAR(100) NOT NULL, " +
          "rol ENUM('ADMIN','USUARIO') NOT NULL)");

      st.execute("CREATE TABLE IF NOT EXISTS carrito (" +
          "id INT AUTO_INCREMENT PRIMARY KEY, " +
          "usuario_id INT NOT NULL, " +
          "fecha_creacion DATE NOT NULL, " +
          "estado VARCHAR(20) NOT NULL, " +
          "CONSTRAINT fk_carrito_usuario FOREIGN KEY(usuario_id) " +
          "REFERENCES usuario(id) ON DELETE CASCADE ON UPDATE CASCADE)");
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    carritoDao = new CarritoDao();

    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {

      // Desactivamos FK momentáneamente para truncar ambas tablas sin orden estricto
      st.execute("SET FOREIGN_KEY_CHECKS = 0");
      st.execute("TRUNCATE TABLE carrito");
      st.execute("TRUNCATE TABLE usuario");
      st.execute("SET FOREIGN_KEY_CHECKS = 1");

      // Insertamos un usuario base para poder crear carritos en cada test
      st.execute("INSERT INTO usuario (id, nombre, email, password, rol) " +
          "VALUES (1, 'Juan Perez', 'juan@mail.com', '123456', 'USUARIO')");
    }

    usuarioBase = new Usuario(1, "Juan Perez", "juan@mail.com", "123456", Rol.USUARIO);
  }

  // =================================================================================
  // TESTS PARA INSERT / GET BY ID
  // =================================================================================

  @Test
  void deberia_InsertarYObtenerCarrito_Cuando_DatosSonValidos() {
    // --- 1. ARRANGE ---
    Carrito nuevoCarrito = new Carrito(null, usuarioBase, LocalDate.of(2026, 8, 3), "ACTIVO");

    // --- 2. ACT ---
    carritoDao.insert(nuevoCarrito);
    Carrito recuperado = carritoDao.getById(nuevoCarrito.getId());

    // --- 3. ASSERT ---
    assertThat(nuevoCarrito.getId()).isGreaterThan(0);
    assertThat(recuperado).isNotNull();
    assertThat(recuperado.getEstado()).isEqualTo("ACTIVO");
    assertThat(recuperado.getFechaDeCreacion()).isEqualTo(LocalDate.of(2026, 8, 3));
    assertThat(recuperado.getUsuario().getId()).isEqualTo(1);
    assertThat(recuperado.getUsuario().getNombre()).isEqualTo("Juan Perez");
  }

  @Test
  void getById_DeberiaRetornarNull_CuandoNoExiste() {
    assertThat(carritoDao.getById(999)).isNull();
  }

  // =================================================================================
  // TESTS PARA UPDATE
  // =================================================================================

  @Test
  void update_DeberiaActualizarFechaYEstado() {
    // --- 1. ARRANGE ---
    Carrito original = new Carrito(null, usuarioBase, LocalDate.of(2026, 1, 1), "ACTIVO");
    carritoDao.insert(original);

    Carrito aEditar = carritoDao.getById(original.getId());
    aEditar.setEstado("FINALIZADO");
    aEditar.setFechaDeCreacion(LocalDate.of(2026, 2, 15));

    // --- 2. ACT ---
    carritoDao.update(aEditar);

    // --- 3. ASSERT ---
    Carrito modificado = carritoDao.getById(original.getId());
    assertThat(modificado.getEstado()).isEqualTo("FINALIZADO");
    assertThat(modificado.getFechaDeCreacion()).isEqualTo(LocalDate.of(2026, 2, 15));
  }

  // =================================================================================
  // TESTS PARA DELETE / EXISTS
  // =================================================================================

  @Test
  void deberia_EliminarCarrito_Cuando_SeProporcionaIdValido() {
    // --- 1. ARRANGE ---
    Carrito carrito = new Carrito(null, usuarioBase, LocalDate.now(), "ACTIVO");
    carritoDao.insert(carrito);
    int id = carrito.getId();

    assertThat(carritoDao.existsById(id)).isTrue();

    // --- 2. ACT ---
    carritoDao.delete(id);

    // --- 3. ASSERT ---
    assertThat(carritoDao.existsById(id)).isFalse();
    assertThat(carritoDao.getById(id)).isNull();
  }

  @Test
  void existsById_DeberiaRetornarFalse_CuandoNoExiste() {
    assertThat(carritoDao.existsById(999)).isFalse();
  }

  // =================================================================================
  // TESTS PARA GET ALL
  // =================================================================================

  @Test
  void getAll_DeberiaRetornarTodosLosCarritos() {
    // --- 1. ARRANGE ---
    carritoDao.insert(new Carrito(null, usuarioBase, LocalDate.of(2026, 1, 1), "FINALIZADO"));
    carritoDao.insert(new Carrito(null, usuarioBase, LocalDate.of(2026, 3, 1), "ACTIVO"));

    // --- 2. ACT ---
    List<Carrito> todos = carritoDao.getAll();

    // --- 3. ASSERT ---
    assertThat(todos).hasSize(2);
    // SQL_GETALL ordena por fecha_creacion DESC
    assertThat(todos.get(0).getFechaDeCreacion()).isEqualTo(LocalDate.of(2026, 3, 1));
  }

  // =================================================================================
  // TESTS PARA MÉTODOS ESPECÍFICOS DE CARRITO
  // =================================================================================

  @Test
  void getActivoByUsuarioId_DeberiaRetornarElCarritoActivo_CuandoExiste() {
    // --- 1. ARRANGE ---
    carritoDao.insert(new Carrito(null, usuarioBase, LocalDate.of(2026, 1, 1), "FINALIZADO"));
    Carrito activo = new Carrito(null, usuarioBase, LocalDate.of(2026, 2, 1), "ACTIVO");
    carritoDao.insert(activo);

    // --- 2. ACT ---
    Carrito resultado = carritoDao.getActivoByUsuarioId(usuarioBase.getId());

    // --- 3. ASSERT ---
    assertThat(resultado).isNotNull();
    assertThat(resultado.getId()).isEqualTo(activo.getId());
    assertThat(resultado.getEstado()).isEqualTo("ACTIVO");
  }

  @Test
  void getActivoByUsuarioId_DeberiaRetornarNull_CuandoNoHayCarritoActivo() {
    // --- 1. ARRANGE ---
    carritoDao.insert(new Carrito(null, usuarioBase, LocalDate.of(2026, 1, 1), "FINALIZADO"));

    // --- 2. ACT & ASSERT ---
    assertThat(carritoDao.getActivoByUsuarioId(usuarioBase.getId())).isNull();
  }

  @Test
  void obtenerOCrearActivo_DeberiaRetornarElExistente_SiYaHayUnoActivo() {
    // --- 1. ARRANGE ---
    Carrito existente = new Carrito(null, usuarioBase, LocalDate.now(), "ACTIVO");
    carritoDao.insert(existente);

    // --- 2. ACT ---
    Carrito resultado = carritoDao.obtenerOCrearActivo(usuarioBase);

    // --- 3. ASSERT ---
    assertThat(resultado.getId()).isEqualTo(existente.getId());
    // No debería haberse creado un segundo carrito
    assertThat(carritoDao.getAll()).hasSize(1);
  }

  @Test
  void obtenerOCrearActivo_DeberiaCrearUnoNuevo_SiNoHayActivo() {
    // --- ACT ---
    Carrito resultado = carritoDao.obtenerOCrearActivo(usuarioBase);

    // --- ASSERT ---
    assertThat(resultado).isNotNull();
    assertThat(resultado.getId()).isGreaterThan(0);
    assertThat(resultado.getEstado()).isEqualTo("ACTIVO");
  }

  @Test
  void marcarFinalizado_DeberiaActualizarEstado_CuandoElCarritoEstaActivo() throws SQLException {
    // --- 1. ARRANGE ---
    Carrito carrito = new Carrito(null, usuarioBase, LocalDate.now(), "ACTIVO");
    carritoDao.insert(carrito);

    // --- 2. ACT ---
    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword())) {
      carritoDao.marcarFinalizado(conn, carrito.getId());
    }

    // --- 3. ASSERT ---
    Carrito actualizado = carritoDao.getById(carrito.getId());
    assertThat(actualizado.getEstado()).isEqualTo("FINALIZADO");
  }

  @Test
  void marcarFinalizado_DeberiaLanzarSQLException_CuandoNoEncuentraCarritoActivo() throws SQLException {
    // --- ACT & ASSERT ---
    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword())) {
      SQLException excepcion = assertThrows(SQLException.class,
          () -> carritoDao.marcarFinalizado(conn, 999));
      assertThat(excepcion.getMessage()).contains("No se pudo finalizar el carrito id 999");
    }
  }
}