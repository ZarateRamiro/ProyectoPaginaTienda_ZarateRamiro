package org.isp63.prog1.servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.isp63.prog1.dao.UsuarioDao;
import org.isp63.prog1.entities.Usuario;
import org.isp63.prog1.servlets.UsuarioServlet;
import org.isp63.prog1.util.Rol;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServletTest {

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

  @InjectMocks
  private UsuarioServlet servlet;

  private void simularAdmin() {
    when(request.getSession()).thenReturn(session);
    Usuario admin = new Usuario(1, "Admin", "admin@mail.com", "1234", Rol.ADMIN);
    when(session.getAttribute("usuario")).thenReturn(admin);
  }

  // --- CONTROL DE ACCESO ---

  @Test
  void deberia_RedirigirAIndex_Cuando_UsuarioNoEsAdmin() throws Exception {
    when(request.getSession()).thenReturn(session);
    when(session.getAttribute("usuario")).thenReturn(null);

    servlet.doGet(request, response);

    verify(response).sendRedirect("index.jsp");
    verify(usuarioDao, never()).getAllUsuariosComunes();
  }

  // --- GET ---

  @Test
  void deberia_ListarUsuariosComunes_Cuando_AccionEsNull() throws Exception {
    simularAdmin();
    when(request.getParameter("accion")).thenReturn(null);
    when(usuarioDao.getAllUsuariosComunes()).thenReturn(new ArrayList<>());
    when(request.getRequestDispatcher("listaUsuarios.jsp")).thenReturn(dispatcher);

    servlet.doGet(request, response);

    verify(usuarioDao).getAllUsuariosComunes();
    verify(request).setAttribute(eq("usuarios"), anyList());
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_MostrarFormularioEditar_Cuando_UsuarioExiste() throws Exception {
    simularAdmin();
    Usuario u = new Usuario(2, "Juan", "juan@mail.com", "1234", Rol.USUARIO);

    when(request.getParameter("accion")).thenReturn("editar");
    when(request.getParameter("id")).thenReturn("2");
    when(usuarioDao.getById(2)).thenReturn(u);
    when(request.getRequestDispatcher("FormUsuario.jsp")).thenReturn(dispatcher);

    servlet.doGet(request, response);

    verify(request).setAttribute("usuario", u);
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_RedirigirAListar_Cuando_UsuarioAEditarNoExiste() throws Exception {
    simularAdmin();
    when(request.getParameter("accion")).thenReturn("editar");
    when(request.getParameter("id")).thenReturn("99");
    when(usuarioDao.getById(99)).thenReturn(null);

    servlet.doGet(request, response);

    verify(response).sendRedirect("SeUsuario?accion=listar");
    verify(request, never()).getRequestDispatcher(anyString());
  }

  @Test
  void deberia_EliminarUsuarioYRedirigir() throws Exception {
    simularAdmin();
    when(request.getParameter("accion")).thenReturn("eliminar");
    when(request.getParameter("id")).thenReturn("2");

    servlet.doGet(request, response);

    verify(usuarioDao).delete(2);
    verify(response).sendRedirect("SeUsuario?accion=listar");
  }

  // --- POST ---

  @Test
  void deberia_GuardarUsuarioCorrectamente() throws Exception {
    simularAdmin();
    when(request.getParameter("accion")).thenReturn("guardar");
    when(request.getParameter("nombre")).thenReturn("Carlos");
    when(request.getParameter("email")).thenReturn("carlos@mail.com");
    when(request.getParameter("password")).thenReturn("1234");

    servlet.doPost(request, response);

    verify(usuarioDao).insert(any(Usuario.class));
    verify(response).sendRedirect("SeUsuario?accion=listar");
  }

  @Test
  void deberia_ActualizarUsuarioCorrectamente() throws Exception {
    simularAdmin();
    when(request.getParameter("accion")).thenReturn("actualizar");
    when(request.getParameter("id")).thenReturn("2");
    when(request.getParameter("nombre")).thenReturn("Carlos Modificado");
    when(request.getParameter("email")).thenReturn("carlos@mail.com");
    when(request.getParameter("rol")).thenReturn(Rol.USUARIO);

    servlet.doPost(request, response);

    verify(usuarioDao).update(any(Usuario.class));
    verify(response).sendRedirect("SeUsuario?accion=listar");
  }
}