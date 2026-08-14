package org.isp63.prog1.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.isp63.prog1.servlets.LogoutServlet;
import org.isp63.prog1.util.ConexionPool;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutServletIntegrationTest {

  private LogoutServlet servlet;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private HttpSession session;

  @BeforeEach
  void setUp() {
    servlet = new LogoutServlet();
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
  void deberia_InvalidarSesionYRedirigir_CuandoExisteSesionActiva() throws Exception {
    // 1. Simular que existe una sesión activa
    when(request.getSession(false)).thenReturn(session);

    // 2. Ejecutar doGet
    servlet.doGet(request, response);

    // 3. Verificaciones
    verify(session).invalidate();
    verify(response).sendRedirect("index.jsp");
  }

  @Test
  void deberia_RedirigirAIndex_CuandoNoExisteSesion() throws Exception {
    // 1. Simular que no hay sesión activa (retorna null)
    when(request.getSession(false)).thenReturn(null);

    // 2. Ejecutar doGet
    servlet.doGet(request, response);

    // 3. Verificaciones: no debe intentar invalidar nada, pero sí redirigir
    verify(session, never()).invalidate();
    verify(response).sendRedirect("index.jsp");
  }
}