package org.isp63.prog1.servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.isp63.prog1.dao.ProductoDao;
import org.isp63.prog1.entities.Producto;
import org.isp63.prog1.entities.Usuario;
import org.isp63.prog1.enums.TipoProducto;
import org.isp63.prog1.servlets.ProductoServlet;
import org.isp63.prog1.util.ConexionPool;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Testcontainers
@ExtendWith(MockitoExtension.class)
class ProductoServletIntegrationTest {

  @Container
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("carrito_db_test")
      .withUsername("test")
      .withPassword("test");

  private ProductoServlet servlet;
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
    mysql.start();
    System.setProperty("db.url", mysql.getJdbcUrl());
    System.setProperty("db.user", mysql.getUsername());
    System.setProperty("db.pass", mysql.getPassword());

    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {

      st.execute("CREATE TABLE producto (" +
          "id INT AUTO_INCREMENT PRIMARY KEY, " +
          "nombre VARCHAR(50), " +
          "descripcion VARCHAR(255), " +
          "precio DOUBLE, " +
          "imagen VARCHAR(100), " +
          "stock INT, " +
          "tipo VARCHAR(20))");
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    servlet = new ProductoServlet();
    productoDao = new ProductoDao();

    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {

      st.execute("TRUNCATE TABLE producto");

      // Insertamos productos iniciales para pruebas de lectura/edición/eliminación
      st.execute("INSERT INTO producto (id, nombre, descripcion, precio, imagen, stock, tipo) " +
          "VALUES (1, 'Buzo Hoodie', 'Buzo de algodón', 15000.0, 'buzo.jpg', 10, 'BUZO')");
      st.execute("INSERT INTO producto (id, nombre, descripcion, precio, imagen, stock, tipo) " +
          "VALUES (2, 'Remera Oversize', 'Remera 100% algodón', 8000.0, 'remera.jpg', 15, 'REMERA')");
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
  // ===========================================================
  // MÉTODOS DE APOYO PARA SEGURIDAD
  // ===========================================================

  private void simularUsuarioAdmin() {
    when(request.getSession()).thenReturn(session);
    // Ajusta el string del rol ("ADMIN" o según lo que reconozca tu clase Rol.esAdmin)
    Usuario admin = new Usuario(1, "Admin", "admin@mail.com", "1234", "ADMIN");
    when(session.getAttribute("usuario")).thenReturn(admin);
  }

  private void simularUsuarioComun() {
    when(request.getSession()).thenReturn(session);
    Usuario comun = new Usuario(2, "Juan", "juan@mail.com", "1234", "USUARIO");
    when(session.getAttribute("usuario")).thenReturn(comun);
  }

  // ===========================================================
  // TESTS DE LECTURA (doGet)
  // ===========================================================

  @Test
  void deberia_ListarTodosLosProductos() throws Exception {
    when(request.getParameter("accion")).thenReturn(null);
    when(request.getRequestDispatcher("productos.jsp")).thenReturn(dispatcher);

    servlet.doGet(request, response);

    verify(request).setAttribute(eq("productos"), anyList());
    verify(request).setAttribute(eq("tipos"), eq(TipoProducto.values()));
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_FiltrarProductosPorTipo() throws Exception {
    when(request.getParameter("accion")).thenReturn("listar");
    when(request.getParameter("tipo")).thenReturn("BUZO");
    when(request.getRequestDispatcher("productos.jsp")).thenReturn(dispatcher);

    servlet.doGet(request, response);

    verify(request).setAttribute("tipoSeleccionado", "BUZO");
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_MostrarFormularioNuevo_SiEsAdmin() throws Exception {
    simularUsuarioAdmin();
    when(request.getParameter("accion")).thenReturn("nuevo");
    when(request.getRequestDispatcher("FormProducto.jsp")).thenReturn(dispatcher);

    servlet.doGet(request, response);

    verify(request).setAttribute("tipos", TipoProducto.values());
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_RedirigirAIndex_AlIntentarNuevo_SiNoEsAdmin() throws Exception {
    simularUsuarioComun();
    when(request.getParameter("accion")).thenReturn("nuevo");

    servlet.doGet(request, response);

    verify(response).sendRedirect("index.jsp");
    verify(request, never()).getRequestDispatcher(anyString());
  }

  @Test
  void deberia_CargarProductoParaEditar_SiEsAdmin() throws Exception {
    simularUsuarioAdmin();
    when(request.getParameter("accion")).thenReturn("editar");
    when(request.getParameter("id")).thenReturn("1");
    when(request.getRequestDispatcher("FormProducto.jsp")).thenReturn(dispatcher);

    servlet.doGet(request, response);

    verify(request).setAttribute(eq("producto"), any(Producto.class));
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_EliminarProducto_SiEsAdmin() throws Exception {
    simularUsuarioAdmin();
    when(request.getParameter("accion")).thenReturn("eliminar");
    when(request.getParameter("id")).thenReturn("1");

    servlet.doGet(request, response);

    verify(response).sendRedirect("SeProducto?accion=listar");
    assertThat(productoDao.getById(1)).isNull();
  }

  // ===========================================================
  // TESTS DE ESCRITURA (doPost)
  // ===========================================================

  @Test
  void deberia_GuardarProducto_SiEsAdmin() throws Exception {
    simularUsuarioAdmin();
    when(request.getParameter("accion")).thenReturn("guardar");
    when(request.getParameter("nombre")).thenReturn("Campera de Cuero");
    when(request.getParameter("descripcion")).thenReturn("Campera negra");
    when(request.getParameter("precio")).thenReturn("25000.0");
    when(request.getParameter("imagen")).thenReturn("campera.jpg");
    when(request.getParameter("stock")).thenReturn("5");
    when(request.getParameter("tipo")).thenReturn("BUZO"); // Reemplaza por un enum válido en tu proyecto

    servlet.doPost(request, response);

    verify(response).sendRedirect("SeProducto?accion=listar");

    List<Producto> productos = productoDao.getAll();
    assertThat(productos).hasSize(3);
  }

  @Test
  void deberia_ActualizarProducto_SiEsAdmin() throws Exception {
    simularUsuarioAdmin();
    when(request.getParameter("accion")).thenReturn("actualizar");
    when(request.getParameter("id")).thenReturn("1");
    when(request.getParameter("nombre")).thenReturn("Buzo Hoodie Editado");
    when(request.getParameter("descripcion")).thenReturn("Nueva descripcion");
    when(request.getParameter("precio")).thenReturn("18000.0");
    when(request.getParameter("imagen")).thenReturn("buzo_nuevo.jpg");
    when(request.getParameter("stock")).thenReturn("8");
    when(request.getParameter("tipo")).thenReturn("BUZO");

    servlet.doPost(request, response);

    verify(response).sendRedirect("SeProducto?accion=listar");

    Producto actualizado = productoDao.getById(1);
    assertThat(actualizado.getNombre()).isEqualTo("Buzo Hoodie Editado");
    assertThat(actualizado.getPrecio()).isEqualTo(18000.0);
  }

  @Test
  void deberia_RedirigirAIndex_AlGuardar_SiNoEsAdmin() throws Exception {
    simularUsuarioComun();
    when(request.getParameter("accion")).thenReturn("guardar");

    servlet.doPost(request, response);

    verify(response).sendRedirect("index.jsp");
  }
}