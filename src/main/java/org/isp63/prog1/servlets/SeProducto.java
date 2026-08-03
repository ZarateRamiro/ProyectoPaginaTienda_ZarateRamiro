package org.isp63.prog1.servlets;

import org.isp63.prog1.enums.TipoProducto;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.isp63.prog1.dao.ProductoDao;
import org.isp63.prog1.entities.Producto;
import org.isp63.prog1.entities.Usuario;
import org.isp63.prog1.util.Rol;

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

    if (requiereAdmin(accion) && !esAdmin(request)) {
      response.sendRedirect("index.jsp");
      return;
    }

    switch (accion) {

      case "nuevo":
        request.setAttribute("tipos", TipoProducto.values());
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

    if (requiereAdmin(accion) && !esAdmin(request)) {
      response.sendRedirect("index.jsp");
      return;
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

  // ===========================================================
  // CRUD
  // ===========================================================

  private void listarProductos(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    List<Producto> productos = productoDao.getAll();
    String tipoSeleccionado = request.getParameter("tipo");

    if (tipoSeleccionado != null && !tipoSeleccionado.isBlank()) {
      TipoProducto tipo = TipoProducto.valueOf(tipoSeleccionado);
      productos = productos.stream()
          .filter(producto -> tipo.equals(producto.getTipo()))
          .toList();
    }

    request.setAttribute("productos", productos);
    request.setAttribute("tipos", TipoProducto.values());
    request.setAttribute("tipoSeleccionado", tipoSeleccionado);

    request.getRequestDispatcher("productos.jsp").forward(request, response);
  }

  private void guardarProducto(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    Producto producto = new Producto();

    producto.setNombre(request.getParameter("nombre"));
    producto.setDescripcion(request.getParameter("descripcion"));
    producto.setPrecio(Double.parseDouble(request.getParameter("precio")));
    producto.setImagen(request.getParameter("imagen"));
    producto.setStock(Integer.parseInt(request.getParameter("stock")));

    producto.setTipo(
        TipoProducto.valueOf(request.getParameter("tipo"))
    );

    productoDao.insert(producto);

    response.sendRedirect("SeProducto?accion=listar");
  }

  private void editarProducto(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    int id = Integer.parseInt(request.getParameter("id"));

    Producto producto = productoDao.getById(id);

    if (producto == null) {
      response.sendRedirect("SeProducto?accion=listar");
      return;
    }

    request.setAttribute("producto", producto);
    request.setAttribute("tipos", TipoProducto.values());

    request.getRequestDispatcher("FormProducto.jsp").forward(request, response);
  }

  private void actualizarProducto(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    Producto producto = new Producto();

    producto.setId(Integer.parseInt(request.getParameter("id")));
    producto.setNombre(request.getParameter("nombre"));
    producto.setDescripcion(request.getParameter("descripcion"));
    producto.setPrecio(Double.parseDouble(request.getParameter("precio")));
    producto.setImagen(request.getParameter("imagen"));
    producto.setStock(Integer.parseInt(request.getParameter("stock")));

    producto.setTipo(
        TipoProducto.valueOf(request.getParameter("tipo"))
    );

    productoDao.update(producto);

    response.sendRedirect("SeProducto?accion=listar");
  }

  private void eliminarProducto(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    int id = Integer.parseInt(request.getParameter("id"));

    productoDao.delete(id);

    response.sendRedirect("SeProducto?accion=listar");
  }

  // ===========================================================
  // SEGURIDAD
  // ===========================================================

  private boolean requiereAdmin(String accion) {

    return "nuevo".equals(accion)
        || "editar".equals(accion)
        || "eliminar".equals(accion)
        || "guardar".equals(accion)
        || "actualizar".equals(accion);
  }

  private boolean esAdmin(HttpServletRequest request) {

    Usuario usuario = (Usuario) request.getSession().getAttribute("usuario");

    return usuario != null && Rol.esAdmin(usuario.getRol());
  }
}
