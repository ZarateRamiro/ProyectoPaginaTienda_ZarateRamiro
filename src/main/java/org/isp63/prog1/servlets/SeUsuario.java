package org.isp63.prog1.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import org.isp63.prog1.dao.UsuarioDao;
import org.isp63.prog1.entities.Usuario;
import org.isp63.prog1.interfaces.AdmConnexion;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

@WebServlet("/SeUsuario")
public class SeUsuario extends HttpServlet implements AdmConnexion {

  private UsuarioDao usuarioDao;
  private Connection conn;

  @Override
  public void init() throws ServletException {
    try {
      conn = obtenerConexion(); // ✅ ACA se crea
      usuarioDao = new UsuarioDao(conn);
    } catch (Exception e) {
      throw new ServletException("Error al inicializar SeUsuario", e);
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    Usuario admin = (Usuario) request.getSession().getAttribute("usuario");
    if (admin == null || !"admin".equalsIgnoreCase(admin.getRol())) {
      response.sendRedirect("index.jsp");
      return;
    }

    String accion = request.getParameter("accion");
    if (accion == null) accion = "listar";

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
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    String accion = request.getParameter("accion");

    if ("guardar".equals(accion)) {
      guardarUsuario(request, response);
    } else if ("actualizar".equals(accion)) {
      actualizarUsuario(request, response);
    } else {
      response.sendRedirect("SeUsuario?accion=listar");
    }
  }

  // ---------------- MÉTODOS ----------------

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
    u.setRol("usuario");

    usuarioDao.insert(u);
    response.sendRedirect("SeUsuario?accion=listar");
  }

  private void actualizarUsuario(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    Usuario u = new Usuario();
    u.setId(Integer.parseInt(request.getParameter("id")));
    u.setNombre(request.getParameter("nombre"));
    u.setEmail(request.getParameter("email"));
    u.setRol(request.getParameter("rol"));

    usuarioDao.update(u);
    response.sendRedirect("SeUsuario?accion=listar");
  }

  private void eliminarUsuario(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    int id = Integer.parseInt(request.getParameter("id"));
    usuarioDao.delete(id);
    response.sendRedirect("SeUsuario?accion=listar");
  }

  @Override
  public void destroy() {
    try {
      if (conn != null) conn.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

