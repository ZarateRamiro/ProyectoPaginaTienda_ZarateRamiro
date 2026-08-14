package org.isp63.prog1.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.isp63.prog1.dao.UsuarioDao;
import org.isp63.prog1.entities.Usuario;
import org.isp63.prog1.util.Rol;

import java.io.IOException;
import java.util.List;

@WebServlet("/SeUsuario")
public class UsuarioServlet extends HttpServlet {private final UsuarioDao usuarioDao;

  // Constructor para Tomcat / Producción
  public UsuarioServlet() {
    this(new UsuarioDao());
  }

  // Constructor para Tests Unitarios
  public UsuarioServlet(UsuarioDao usuarioDao) {
    this.usuarioDao = usuarioDao;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    if (!esAdmin(request, response)) {
      return;
    }

    String accion = request.getParameter("accion");
    if (accion == null) {
      accion = "listar";
    }

    switch (accion) {
      case "nuevo":
        request.getRequestDispatcher("FormUsuario.jsp").forward(request, response);
        break;
      case "editar":
        editarUsuario(request, response);
        break;
      case "eliminar":
        eliminarUsuario(request, response);
        break;
      default:
        listarUsuarios(request, response);
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    if (!esAdmin(request, response)) {
      return;
    }

    String accion = request.getParameter("accion");

    if ("guardar".equals(accion)) {
      guardarUsuario(request, response);
    } else if ("actualizar".equals(accion)) {
      actualizarUsuario(request, response);
    } else {
      response.sendRedirect("SeUsuario?accion=listar");
    }
  }

  private void listarUsuarios(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    List<Usuario> lista = usuarioDao.getAllUsuariosComunes();
    request.setAttribute("usuarios", lista);
    request.getRequestDispatcher("listaUsuarios.jsp").forward(request, response);
  }

  private void editarUsuario(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    int id = Integer.parseInt(request.getParameter("id"));
    Usuario u = usuarioDao.getById(id);

    if (u != null) {
      request.setAttribute("usuario", u);
      request.getRequestDispatcher("FormUsuario.jsp").forward(request, response);
    } else {
      response.sendRedirect("SeUsuario?accion=listar");
    }
  }

  private void guardarUsuario(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    Usuario u = new Usuario();
    u.setNombre(request.getParameter("nombre"));
    u.setEmail(request.getParameter("email"));
    u.setPassword(request.getParameter("password"));
    u.setRol(Rol.USUARIO);

    usuarioDao.insert(u);
    response.sendRedirect("SeUsuario?accion=listar");
  }

  private void actualizarUsuario(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    Usuario u = new Usuario();
    u.setId(Integer.parseInt(request.getParameter("id")));
    u.setNombre(request.getParameter("nombre"));
    u.setEmail(request.getParameter("email"));
    u.setRol(normalizarRol(request.getParameter("rol")));

    usuarioDao.update(u);
    response.sendRedirect("SeUsuario?accion=listar");
  }

  private void eliminarUsuario(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    int id = Integer.parseInt(request.getParameter("id"));
    usuarioDao.delete(id);
    response.sendRedirect("SeUsuario?accion=listar");
  }

  private boolean esAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Usuario admin = (Usuario) request.getSession().getAttribute("usuario");
    if (admin == null || !Rol.esAdmin(admin.getRol())) {
      response.sendRedirect("index.jsp");
      return false;
    }
    return true;
  }

  private String normalizarRol(String rol) {
    return Rol.esAdmin(rol) ? Rol.ADMIN : Rol.USUARIO;
  }
}
