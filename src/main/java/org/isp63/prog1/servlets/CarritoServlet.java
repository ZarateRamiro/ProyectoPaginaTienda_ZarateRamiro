package org.isp63.prog1.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.isp63.prog1.dao.CarritoDao;
import org.isp63.prog1.dao.ItemCarritoDao;
import org.isp63.prog1.dao.ProductoDao;
import org.isp63.prog1.entities.Carrito;
import org.isp63.prog1.entities.ItemCarrito;
import org.isp63.prog1.entities.Producto;
import org.isp63.prog1.entities.Usuario;
import org.isp63.prog1.exception.SinStockException;
import org.isp63.prog1.service.CarritoService;
import org.isp63.prog1.util.Rol;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "SeCarrito", urlPatterns = {"/SeCarrito"})
public class CarritoServlet extends HttpServlet {
  private final CarritoDao carritoDao;
  private final ItemCarritoDao itemCarritoDao;
  private final ProductoDao productoDao;
  private final CarritoService carritoService;

  // Constructor por defecto (usado por Tomcat en producción)
  public CarritoServlet() {
    this(new CarritoDao(), new ItemCarritoDao(), new ProductoDao(), new CarritoService());
  }

  // Constructor para los tests unitarios
  public CarritoServlet(CarritoDao carritoDao, ItemCarritoDao itemCarritoDao,
                        ProductoDao productoDao, CarritoService carritoService) {
    this.carritoDao = carritoDao;
    this.itemCarritoDao = itemCarritoDao;
    this.productoDao = productoDao;
    this.carritoService = carritoService;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    Usuario usuario = obtenerUsuarioComun(request, response);
    if (usuario == null) {
      return;
    }

    String accion = request.getParameter("accion");
    if (accion == null) {
      accion = "ver";
    }

    switch (accion) {
      case "quitar":
        quitarItem(usuario, request, response);
        break;
      case "vaciar":
        vaciarCarrito(usuario, response);
        break;
      default:
        verCarrito(usuario, request, response);
        break;
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    Usuario usuario = obtenerUsuarioComun(request, response);
    if (usuario == null) {
      return;
    }

    String accion = request.getParameter("accion");
    switch (accion) {
      case "agregar":
        agregarProducto(usuario, request, response);
        break;
      case "actualizar":
        actualizarCantidad(usuario, request, response);
        break;
      case "finalizar":
        finalizarCompra(usuario, request, response);
        break;
      default:
        response.sendRedirect("SeCarrito?accion=ver");
    }
  }

  private void agregarProducto(Usuario usuario, HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    int productoId = Integer.parseInt(request.getParameter("productoId"));
    int cantidad = parseCantidad(request.getParameter("cantidad"));
    Producto producto = productoDao.getById(productoId);

    if (producto == null || producto.getStock() <= 0) {
      response.sendRedirect("SeProducto?accion=listar&error=sinStock");
      return;
    }

    Carrito carrito = carritoDao.obtenerOCrearActivo(usuario);
    itemCarritoDao.agregarOIncrementar(carrito, producto, cantidad);
    response.sendRedirect("SeCarrito?accion=ver");
  }

  private void actualizarCantidad(Usuario usuario, HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    Carrito carrito = carritoDao.obtenerOCrearActivo(usuario);
    int itemId = Integer.parseInt(request.getParameter("itemId"));
    int cantidad = parseCantidad(request.getParameter("cantidad"));

    if (!itemCarritoDao.perteneceAlCarrito(itemId, carrito.getId())) {
      response.sendRedirect("SeCarrito?accion=ver&error=acceso");
      return;
    }

    ItemCarrito item = itemCarritoDao.getById(itemId);
    if (item == null || item.getProducto() == null) {
      response.sendRedirect("SeCarrito?accion=ver");
      return;
    }

    Producto producto = productoDao.getById(item.getProducto().getId());
    if (producto == null) {
      response.sendRedirect("SeCarrito?accion=ver");
      return;
    }

    itemCarritoDao.actualizarCantidad(itemId, cantidad, producto.getStock());
    response.sendRedirect("SeCarrito?accion=ver");
  }

  private void quitarItem(Usuario usuario, HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    Carrito carrito = carritoDao.obtenerOCrearActivo(usuario);
    int itemId = Integer.parseInt(request.getParameter("itemId"));

    if (itemCarritoDao.perteneceAlCarrito(itemId, carrito.getId())) {
      itemCarritoDao.delete(itemId);
    }

    response.sendRedirect("SeCarrito?accion=ver");
  }

  private void vaciarCarrito(Usuario usuario, HttpServletResponse response) throws IOException {
    Carrito carrito = carritoDao.getActivoByUsuarioId(usuario.getId());
    if (carrito != null) {
      itemCarritoDao.vaciarCarrito(carrito.getId());
    }
    response.sendRedirect("SeCarrito?accion=ver");
  }

  private void finalizarCompra(Usuario usuario, HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    Carrito carrito = carritoDao.getActivoByUsuarioId(usuario.getId());
    if (carrito == null) {
      response.sendRedirect("SeCarrito?accion=ver");
      return;
    }

    List<ItemCarrito> items = itemCarritoDao.getByCarritoId(carrito.getId());
    if (items.isEmpty()) {
      response.sendRedirect("SeCarrito?accion=ver&error=vacio");
      return;
    }

    try {
      carritoService.finalizarCompra(carrito, items);
      response.sendRedirect("SeCarrito?accion=ver&exito=compra");
    } catch (SinStockException e) {
      response.sendRedirect("SeCarrito?accion=ver&error=sinStock");
    }
  }

  private void verCarrito(Usuario usuario, HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    Carrito carrito = carritoDao.getActivoByUsuarioId(usuario.getId());
    List<ItemCarrito> items = carrito == null ? List.of() : itemCarritoDao.getByCarritoId(carrito.getId());
    double total = carritoService.calcularTotal(items);

    request.setAttribute("carrito", carrito);
    request.setAttribute("items", items);
    request.setAttribute("total", total);
    request.setAttribute("mensajeExito", "compra".equals(request.getParameter("exito")) ? "Compra finalizada correctamente." : null);
    request.setAttribute("mensajeError", obtenerMensajeError(request.getParameter("error")));
    request.getRequestDispatcher("carrito.jsp").forward(request, response);
  }

  private String obtenerMensajeError(String error) {
    if (error == null) {
      return null;
    }
    return switch (error) {
      case "sinStock" -> "Hay productos sin stock suficiente para completar la compra.";
      case "vacio" -> "No podés finalizar un carrito vacío.";
      case "acceso" -> "No tenés permiso para modificar ese ítem.";
      default -> "Ocurrió un error al procesar el carrito.";
    };
  }

  private Usuario obtenerUsuarioComun(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    HttpSession session = request.getSession(false);
    Usuario usuario = session == null ? null : (Usuario) session.getAttribute("usuario");

    if (usuario == null) {
      response.sendRedirect("login.jsp");
      return null;
    }

    if (Rol.esAdmin(usuario.getRol())) {
      response.sendRedirect("index.jsp");
      return null;
    }

    return usuario;
  }

  private int parseCantidad(String valor) {
    try {
      int cantidad = Integer.parseInt(valor);
      return Math.max(cantidad, 1);
    } catch (NumberFormatException e) {
      return 1;
    }
  }
}
