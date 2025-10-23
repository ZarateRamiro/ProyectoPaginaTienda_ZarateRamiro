package org.isp63.prog1.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.isp63.prog1.dao.UsuarioDao;
import org.isp63.prog1.entities.Usuario;
import org.isp63.prog1.interfaces.AdmConnexion;

import java.io.IOException;
import java.sql.Connection;

@WebServlet("/login")

public class SeLogin extends HttpServlet implements AdmConnexion {
  private Connection conn = null;

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String nombre = request.getParameter("nombre");
    String password = request.getParameter("password");

    conn = obtenerConexion();
    UsuarioDao dao = new UsuarioDao(conn);
    Usuario u = dao.validarLogin(nombre, password);

    if (u != null) {
      HttpSession session = request.getSession();
      session.setAttribute("usuario", u);

      // 🔹 Si es ADMIN → redirige a /admin/productosAdmin.jsp
      // 🔹 Si es USER → redirige a /productosAdmin.jsp
      if ("ADMIN".equals(u.getRol())) {
        response.sendRedirect("index.jsp");
      } else {
        response.sendRedirect("index.jsp");
      }
    } else {
      request.setAttribute("error", "Usuario o contraseña incorrectos");
      request.getRequestDispatcher("login.jsp").forward(request, response);
    }
  }
}



