package org.isp63.prog1.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.isp63.prog1.dao.UsuarioDao;
import org.isp63.prog1.entities.Usuario;

import java.io.IOException;

@WebServlet("/login")
public class SeLogin extends HttpServlet {

  private final UsuarioDao usuarioDao = new UsuarioDao();

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String nombre = request.getParameter("nombre");
    String password = request.getParameter("password");

    Usuario u = usuarioDao.validarLogin(nombre, password);

    if (u != null) {
      HttpSession session = request.getSession();
      session.setAttribute("usuario", u);
      response.sendRedirect("index.jsp");
    } else {
      request.setAttribute("error", "Usuario o contraseña incorrectos");
      request.getRequestDispatcher("login.jsp").forward(request, response);
    }
  }
}
