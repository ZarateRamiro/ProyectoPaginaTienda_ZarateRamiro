package org.isp63.prog1.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.isp63.prog1.dao.ProductoDao;
import org.isp63.prog1.entities.Producto;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "SeProducto", urlPatterns = {"/SeProducto"})
public class SeProducto extends HttpServlet {

  private final ProductoDao productoDao = new ProductoDao();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String accion = request.getParameter("accion");
    if (accion == null) {
      accion = "listar";
    }

    switch (accion) {
      case "nuevo":
        request.getRequestDispatcher("FormProducto.jsp").forward(request, response);
        break;

      case "editar":
        editarProducto(request, response);
        break;

      case "eliminar":
        eliminarProducto(request, response);
        break;

      default:
        listarProductos(request, response);
        break;
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String accion = request.getParameter("accion");
    if (accion == null) {
      accion = "guardar";
    }

    switch (accion) {
      case "guardar":
        guardarProducto(request, response);
        break;
      case "actualizar":
        actualizarProducto(request, response);
        break;
      default:
        listarProductos(request, response);
        break;
    }
  }

  // -------------------------------------------------------------------------
  // MÉTODOS AUXILIARES CRUD
  // -------------------------------------------------------------------------

  private void listarProductos(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    List<Producto> lista = productoDao.getAll();
    request.setAttribute("productos", lista);
    request.getRequestDispatcher("productos.jsp").forward(request, response);
  }

  private void guardarProducto(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String nombre = request.getParameter("nombre");
    String descripcion = request.getParameter("descripcion");
    double precio = Double.parseDouble(request.getParameter("precio"));
    String imagen = request.getParameter("imagen");

    Producto nuevo = new Producto();
    nuevo.setNombre(nombre);
    nuevo.setDescripcion(descripcion);
    nuevo.setPrecio(precio);
    nuevo.setImagen(imagen);

    productoDao.insert(nuevo);
    response.sendRedirect("SeProducto?accion=listar");
  }

  private void editarProducto(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    int id = Integer.parseInt(request.getParameter("id"));
    Producto producto = productoDao.getById(id);

    if (producto != null) {
      request.setAttribute("producto", producto);
      request.getRequestDispatcher("FormProducto.jsp").forward(request, response);
    } else {
      response.sendRedirect("SeProducto?accion=listar");
    }
  }

  private void actualizarProducto(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    int id = Integer.parseInt(request.getParameter("id"));
    String nombre = request.getParameter("nombre");
    String descripcion = request.getParameter("descripcion");
    double precio = Double.parseDouble(request.getParameter("precio"));
    String imagen = request.getParameter("imagen");

    Producto p = new Producto(id, nombre, descripcion, precio, imagen);
    productoDao.update(p);
    response.sendRedirect("productosAdmin.jsp");
  }

  private void eliminarProducto(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    int id = Integer.parseInt(request.getParameter("id"));
    productoDao.delete(id);
    response.sendRedirect("SeProducto?accion=listar");
  }
}


