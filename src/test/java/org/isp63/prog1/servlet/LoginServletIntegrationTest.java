package org.isp63.prog1.servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.isp63.prog1.dao.UsuarioDao;
import org.isp63.prog1.entities.Usuario;
import org.isp63.prog1.interfaces.AdminConexion;
import org.isp63.prog1.servlets.LoginServlet;
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

import static org.mockito.Mockito.*;

@Testcontainers
@ExtendWith(MockitoExtension.class)
class LoginServletIntegrationTest {

  @Container
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("carrito_db_test")
      .withUsername("test")
      .withPassword("test");

  private LoginServlet servlet;
  private UsuarioDao usuarioDao;

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
    ConexionPool.close();
    AdminConexion.INSTANCE.recargarPoolParaTests();

    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {

      st.execute("CREATE TABLE usuario (" +
          "id INT AUTO_INCREMENT PRIMARY KEY, " +
          "nombre VARCHAR(50), " +
          "email VARCHAR(100), " +
          "password VARCHAR(100), " +
          "rol VARCHAR(20))");
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    servlet = new LoginServlet();
    usuarioDao = new UsuarioDao();

    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {

      st.execute("TRUNCATE TABLE usuario");

      // Insertamos un usuario de prueba (Asegúrate de coincidir con cómo maneja la contraseña tu UsuarioDao)
      st.execute("INSERT INTO usuario (id, nombre, email, password, rol) " +
          "VALUES (1, 'Juan', 'juan@mail.com', '1234', 'USUARIO')");
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
  void deberia_IniciarSesionExitosamente_CuandoCredencialesSonCorrectas() throws Exception {
    // 1. Simular datos del formulario
    when(request.getParameter("nombre")).thenReturn("Juan");
    when(request.getParameter("password")).thenReturn("1234");
    when(request.getSession()).thenReturn(session);

    // 2. Ejecutar doPost
    servlet.doPost(request, response);

    // 3. Verificaciones
    verify(session).setAttribute(eq("usuario"), any(Usuario.class));
    verify(response).sendRedirect("index.jsp");
    verify(request, never()).getRequestDispatcher(anyString());
  }

  @Test
  void deberia_MostrarError_CuandoPasswordEsIncorrecta() throws Exception {
    // 1. Simular datos del formulario con clave errónea
    when(request.getParameter("nombre")).thenReturn("Juan");
    when(request.getParameter("password")).thenReturn("clave_incorrecta");
    when(request.getRequestDispatcher("login.jsp")).thenReturn(dispatcher);

    // 2. Ejecutar doPost
    servlet.doPost(request, response);

    // 3. Verificaciones
    verify(request).setAttribute("error", "Usuario o contraseña incorrectos");
    verify(dispatcher).forward(request, response);
    verify(response, never()).sendRedirect(anyString());
  }

  @Test
  void deberia_MostrarError_CuandoUsuarioNoExiste() throws Exception {
    // 1. Simular datos de un usuario inexistente
    when(request.getParameter("nombre")).thenReturn("UsuarioInexistente");
    when(request.getParameter("password")).thenReturn("1234");
    when(request.getRequestDispatcher("login.jsp")).thenReturn(dispatcher);

    // 2. Ejecutar doPost
    servlet.doPost(request, response);

    // 3. Verificaciones
    verify(request).setAttribute("error", "Usuario o contraseña incorrectos");
    verify(dispatcher).forward(request, response);
    verify(response, never()).sendRedirect(anyString());
  }
}