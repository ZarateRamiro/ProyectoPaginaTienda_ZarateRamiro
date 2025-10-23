package org.isp63.prog1.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.isp63.prog1.dao.ProductoDao;
import org.isp63.prog1.entities.Producto;

import java.io.IOException;

public class SeProducto  extends HttpServlet {

  // El formulario usa method="POST"
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    // 1. Recoger y validar los parámetros del formulario
    String nombre = request.getParameter("nombre");
    String descripcion = request.getParameter("descripcion");
    String precioStr = request.getParameter("precio");
    String imagen = request.getParameter("imagen");

    double precio = 0.0;
    try {
      // Convertir el precio de String a Double
      precio = Double.parseDouble(precioStr);
    } catch (NumberFormatException e) {
      // Manejo de error si el precio no es válido
      response.sendRedirect("productosAdmin.jsp?error=precioInvalido");
      return;
    }

    // 2. Crear el objeto Producto
    Producto nuevoProducto = new Producto();
    nuevoProducto.setNombre(nombre);
    nuevoProducto.setDescripcion(descripcion);
    nuevoProducto.setPrecio(precio);
    nuevoProducto.setImagen(imagen);

    // 3. Usar el DAO para insertar el producto
    ProductoDao dao = new ProductoDao();

    try {
      // Llamar al método insert que implementaste en el DAO
      dao.insert(nuevoProducto);

      // 4. Redirigir al administrador a la vista de productos con mensaje de éxito
      response.sendRedirect("productosAdmin.jsp?exito=creacion");

    } catch (RuntimeException e) {
      // Manejo de error si falla la inserción en la DB
      e.printStackTrace();
      response.sendRedirect("productosAdmin.jsp?error=db");
    }
  }
}
