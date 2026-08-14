package org.isp63.prog1.servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.isp63.prog1.dao.CarritoDao;
import org.isp63.prog1.dao.ItemCarritoDao;
import org.isp63.prog1.dao.ProductoDao;
import org.isp63.prog1.entities.Carrito;
import org.isp63.prog1.entities.ItemCarrito;
import org.isp63.prog1.entities.Producto;
import org.isp63.prog1.entities.Usuario;
import org.isp63.prog1.interfaces.AdminConexion;
import org.isp63.prog1.servlets.CarritoServlet;
import org.isp63.prog1.util.ConexionPool;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.MySQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoServletIntegrationTest {

  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("carrito_db_test")
      .withUsername("test")
      .withPassword("test");

  private CarritoServlet servlet;
  private CarritoDao carritoDao;
  private ItemCarritoDao itemCarritoDao;
  private ProductoDao productoDao;

  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private HttpSession session;
  @Mock
  private RequestDispatcher dispatcher;

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
          "nombre VARCHAR(50), " +
          "email VARCHAR(100), " +
          "password VARCHAR(100), " +
          "rol VARCHAR(20))");

      st.execute("CREATE TABLE IF NOT EXISTS carrito (" +
          "id INT AUTO_INCREMENT PRIMARY KEY, " +
          "usuario_id INT, " +
          "fecha_creacion DATE, " +
          "estado VARCHAR(20), " +
          "FOREIGN KEY (usuario_id) REFERENCES usuario(id))");

      st.execute("CREATE TABLE IF NOT EXISTS producto (" +
          "id INT AUTO_INCREMENT PRIMARY KEY, " +
          "nombre VARCHAR(50), " +
          "descripcion VARCHAR(255), " +
          "precio DOUBLE, " +
          "imagen VARCHAR(100), " +
          "stock INT, " +
          "tipo VARCHAR(20))");

      st.execute("CREATE TABLE IF NOT EXISTS item_carrito (" +
          "id INT AUTO_INCREMENT PRIMARY KEY, " +
          "carrito_id INT, " +
          "producto_id INT, " +
          "cantidad INT, " +
          "precio_unitario DOUBLE, " +
          "FOREIGN KEY (carrito_id) REFERENCES carrito(id), " +
          "FOREIGN KEY (producto_id) REFERENCES producto(id))");
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    servlet = new CarritoServlet();
    carritoDao = new CarritoDao();
    itemCarritoDao = new ItemCarritoDao();
    productoDao = new ProductoDao();

    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {

      st.execute("SET FOREIGN_KEY_CHECKS = 0");
      st.execute("TRUNCATE TABLE item_carrito");
      st.execute("TRUNCATE TABLE carrito");
      st.execute("TRUNCATE TABLE producto");
      st.execute("TRUNCATE TABLE usuario");
      st.execute("SET FOREIGN_KEY_CHECKS = 1");

      st.execute("INSERT INTO usuario (id, nombre, email, password, rol) VALUES (1, 'Juan', 'juan@mail.com', '1234', 'USUARIO')");
      st.execute("INSERT INTO producto (id, nombre, descripcion, precio, imagen, stock, tipo) VALUES (100, 'Notebook', 'Notebook gamer', 2500.0, 'notebook.jpg', 10, 'BUZO')");
    }
  }

  // ===========================================================
  // LIMPIEZA DE CONEXIONES Y POOL
  // ===========================================================

  @AfterEach
  void tearDown() {
    // Cierra el pool después de cada test para liberar conexiones
    ConexionPool.close();
  }

  @AfterAll
  static void tearDownAll() {
    // Cierre final cuando termina toda la suite
    ConexionPool.close();
  }

  @Test
  void deberia_AgregarProductoAlCarrito() throws Exception {
    when(request.getParameter("accion")).thenReturn("agregar");
    when(request.getParameter("productoId")).thenReturn("100");
    when(request.getParameter("cantidad")).thenReturn("2");

    when(request.getSession(false)).thenReturn(session);
    Usuario usuario = new Usuario(1, "Juan", "juan@mail.com", "1234", "COMUN");
    when(session.getAttribute("usuario")).thenReturn(usuario);

    servlet.doPost(request, response);

    List<ItemCarrito> items = itemCarritoDao.getByCarritoId(carritoDao.getActivoByUsuarioId(1).getId());
    assertThat(items).hasSize(1);
    assertThat(items.get(0).getCantidad()).isEqualTo(2);
  }

  @Test
  void deberia_VaciarCarrito() throws Exception {
    Carrito carrito = carritoDao.obtenerOCrearActivo(new Usuario(1, "Juan", "juan@mail.com", "1234", "USUARIO"));
    Producto producto = productoDao.getById(100);
    itemCarritoDao.agregarOIncrementar(carrito, producto, 1);

    when(request.getParameter("accion")).thenReturn("vaciar");
    when(request.getSession(false)).thenReturn(session);
    Usuario usuario = new Usuario(1, "Juan", "juan@mail.com", "1234", "USUARIO");
    when(session.getAttribute("usuario")).thenReturn(usuario);

    servlet.doGet(request, response);

    List<ItemCarrito> items = itemCarritoDao.getByCarritoId(carrito.getId());
    assertThat(items).isEmpty();
  }
}