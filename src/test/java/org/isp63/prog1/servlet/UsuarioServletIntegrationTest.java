package org.isp63.prog1.servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.isp63.prog1.dao.UsuarioDao;
import org.isp63.prog1.entities.Usuario;
import org.isp63.prog1.servlets.UsuarioServlet;
import org.isp63.prog1.util.ConexionPool;
import org.isp63.prog1.util.Rol;
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
class UsuarioServletIntegrationTest {

  @Container
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("carrito_db_test")
      .withUsername("test")
      .withPassword("test");

  private UsuarioServlet servlet;
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
    servlet = new UsuarioServlet();
    usuarioDao = new UsuarioDao();

    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {

      st.execute("TRUNCATE TABLE usuario");

      // Insertamos un admin y un par de usuarios de prueba
      st.execute("INSERT INTO usuario (id, nombre, email, password, rol) VALUES (1, 'Admin', 'admin@mail.com', '1234', '" + Rol.ADMIN + "')");
      st.execute("INSERT INTO usuario (id, nombre, email, password, rol) VALUES (2, 'Pedro', 'pedro@mail.com', '1234', '" + Rol.USUARIO + "')");
      st.execute("INSERT INTO usuario (id, nombre, email, password, rol) VALUES (3, 'Maria', 'maria@mail.com', '1234', '" + Rol.USUARIO + "')");
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
    Usuario admin = new Usuario(1, "Admin", "admin@mail.com", "1234", Rol.ADMIN);
    when(session.getAttribute("usuario")).thenReturn(admin);
  }

  private void simularUsuarioComun() {
    when(request.getSession()).thenReturn(session);
    Usuario comun = new Usuario(2, "Pedro", "pedro@mail.com", "1234", Rol.USUARIO);
    when(session.getAttribute("usuario")).thenReturn(comun);
  }

  // ===========================================================
  // TESTS DE SEGURIDAD
  // ===========================================================

  @Test
  void deberia_RedirigirAIndex_CuandoUsuarioNoEstaAutenticado() throws Exception {
    when(request.getSession()).thenReturn(session);
    when(session.getAttribute("usuario")).thenReturn(null);

    servlet.doGet(request, response);

    verify(response).sendRedirect("index.jsp");
    verify(request, never()).getRequestDispatcher(anyString());
  }

  @Test
  void deberia_RedirigirAIndex_CuandoUsuarioNoEsAdmin() throws Exception {
    simularUsuarioComun();

    servlet.doGet(request, response);

    verify(response).sendRedirect("index.jsp");
    verify(request, never()).getRequestDispatcher(anyString());
  }

  // ===========================================================
  // TESTS DE LECTURA (doGet)
  // ===========================================================

  @Test
  void deberia_ListarUsuariosComunes_SiEsAdmin() throws Exception {
    simularUsuarioAdmin();
    when(request.getParameter("accion")).thenReturn("listar");
    when(request.getRequestDispatcher("listaUsuarios.jsp")).thenReturn(dispatcher);

    servlet.doGet(request, response);

    verify(request).setAttribute(eq("usuarios"), anyList());
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_MostrarFormularioNuevo_SiEsAdmin() throws Exception {
    simularUsuarioAdmin();
    when(request.getParameter("accion")).thenReturn("nuevo");
    when(request.getRequestDispatcher("FormUsuario.jsp")).thenReturn(dispatcher);

    servlet.doGet(request, response);

    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_CargarUsuarioParaEditar_SiEsAdmin() throws Exception {
    simularUsuarioAdmin();
    when(request.getParameter("accion")).thenReturn("editar");
    when(request.getParameter("id")).thenReturn("2");
    when(request.getRequestDispatcher("FormUsuario.jsp")).thenReturn(dispatcher);

    servlet.doGet(request, response);

    verify(request).setAttribute(eq("usuario"), any(Usuario.class));
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_RedirigirAListar_AlEditar_SiUsuarioNoExiste() throws Exception {
    simularUsuarioAdmin();
    when(request.getParameter("accion")).thenReturn("editar");
    when(request.getParameter("id")).thenReturn("999"); // ID inexistente

    servlet.doGet(request, response);

    verify(response).sendRedirect("SeUsuario?accion=listar");
    verify(request, never()).getRequestDispatcher(anyString());
  }

  @Test
  void deberia_EliminarUsuario_SiEsAdmin() throws Exception {
    simularUsuarioAdmin();
    when(request.getParameter("accion")).thenReturn("eliminar");
    when(request.getParameter("id")).thenReturn("2");

    servlet.doGet(request, response);

    verify(response).sendRedirect("SeUsuario?accion=listar");
    assertThat(usuarioDao.getById(2)).isNull();
  }

  // ===========================================================
  // TESTS DE ESCRITURA (doPost)
  // ===========================================================

  @Test
  void deberia_GuardarNuevoUsuario_SiEsAdmin() throws Exception {
    simularUsuarioAdmin();
    when(request.getParameter("accion")).thenReturn("guardar");
    when(request.getParameter("nombre")).thenReturn("Carlos");
    when(request.getParameter("email")).thenReturn("carlos@mail.com");
    when(request.getParameter("password")).thenReturn("12345");

    servlet.doPost(request, response);

    verify(response).sendRedirect("SeUsuario?accion=listar");

    List<Usuario> usuarios = usuarioDao.getAllUsuariosComunes();
    // Como teníamos 2 usuarios comunes en el setUp + 1 nuevo = 3 usuarios comunes
    assertThat(usuarios).hasSize(3);
  }

  @Test
  void deberia_ActualizarUsuario_SiEsAdmin() throws Exception {
    simularUsuarioAdmin();
    when(request.getParameter("accion")).thenReturn("actualizar");
    when(request.getParameter("id")).thenReturn("2");
    when(request.getParameter("nombre")).thenReturn("Pedro Actualizado");
    when(request.getParameter("email")).thenReturn("pedro_nuevo@mail.com");
    when(request.getParameter("rol")).thenReturn(Rol.USUARIO);

    servlet.doPost(request, response);

    verify(response).sendRedirect("SeUsuario?accion=listar");

    Usuario actualizado = usuarioDao.getById(2);
    assertThat(actualizado.getNombre()).isEqualTo("Pedro Actualizado");
    assertThat(actualizado.getEmail()).isEqualTo("pedro_nuevo@mail.com");
  }

  @Test
  void deberia_RedirigirAListar_CuandoAccionDesconocidaEnPost() throws Exception {
    simularUsuarioAdmin();
    when(request.getParameter("accion")).thenReturn("desconocida");

    servlet.doPost(request, response);

    verify(response).sendRedirect("SeUsuario?accion=listar");
  }
}