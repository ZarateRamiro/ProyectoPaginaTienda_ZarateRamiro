package org.isp63.prog1.servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.isp63.prog1.dao.UsuarioDao;
import org.isp63.prog1.entities.Usuario;
import org.isp63.prog1.servlets.LoginServlet;
import org.isp63.prog1.util.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServletTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private HttpSession session;

  @Mock
  private RequestDispatcher dispatcher;

  @Mock
  private UsuarioDao usuarioDao;

  private LoginServlet servlet;

  @BeforeEach
  void setUp() {
    servlet = new LoginServlet(usuarioDao);
  }

  @Test
  void deberia_IniciarSesionYRedirigirAIndex_Cuando_CredencialesSonValidas() throws Exception {
    Usuario usuarioValido = new Usuario(1, "Juan", "juan@mail.com", "1234", Rol.USUARIO);

    when(request.getParameter("nombre")).thenReturn("Juan");
    when(request.getParameter("password")).thenReturn("1234");
    when(usuarioDao.validarLogin("Juan", "1234")).thenReturn(usuarioValido);
    when(request.getSession()).thenReturn(session);

    servlet.doPost(request, response);

    verify(session).setAttribute("usuario", usuarioValido);
    verify(response).sendRedirect("index.jsp");
  }

  @Test
  void deberia_MostrarErrorYForwardALogin_Cuando_CredencialesSonInvalidas() throws Exception {
    when(request.getParameter("nombre")).thenReturn("usuarioInvalido");
    when(request.getParameter("password")).thenReturn("claveErronea");
    when(usuarioDao.validarLogin("usuarioInvalido", "claveErronea")).thenReturn(null);
    when(request.getRequestDispatcher("login.jsp")).thenReturn(dispatcher);

    servlet.doPost(request, response);

    verify(request).setAttribute("error", "Usuario o contraseña incorrectos");
    verify(dispatcher).forward(request, response);
    verify(response, never()).sendRedirect(anyString());
  }
}