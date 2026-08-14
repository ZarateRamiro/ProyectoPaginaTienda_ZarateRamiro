package org.isp63.prog1.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.isp63.prog1.servlets.LogoutServlet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutServletTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private HttpSession session;

  private LogoutServlet servlet;

  @BeforeEach
  void setUp() {
    servlet = new LogoutServlet();
  }

  @Test
  void deberia_InvalidarSesionYRedirigirAIndex_Cuando_ExisteSesion() throws Exception {
    when(request.getSession(false)).thenReturn(session);

    servlet.doGet(request, response);

    verify(session).invalidate();
    verify(response).sendRedirect("index.jsp");
  }

  @Test
  void deberia_RedirigirAIndexSinExcepcion_Cuando_NoExisteSesion() throws Exception {
    when(request.getSession(false)).thenReturn(null);

    servlet.doGet(request, response);

    verify(session, never()).invalidate();
    verify(response).sendRedirect("index.jsp");
  }
}