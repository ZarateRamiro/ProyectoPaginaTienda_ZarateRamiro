package org.isp63.prog1.servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.isp63.prog1.dao.CarritoDao;
import org.isp63.prog1.dao.ItemCarritoDao;
import org.isp63.prog1.dao.ProductoDao;
import org.isp63.prog1.entities.Carrito;
import org.isp63.prog1.entities.Producto;
import org.isp63.prog1.entities.Usuario;
import org.isp63.prog1.service.CarritoService;
import org.isp63.prog1.servlets.CarritoServlet;
import org.isp63.prog1.util.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoServletTest {

  // Mocks de la API Servlet (simulan el entorno Web)
  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private HttpSession session;

  @Mock
  private RequestDispatcher dispatcher;

  // Mocks de la capa de datos / servicios
  @Mock
  private CarritoDao carritoDao;

  @Mock
  private ItemCarritoDao itemCarritoDao;

  @Mock
  private ProductoDao productoDao;

  @Mock
  private CarritoService carritoService;

  private CarritoServlet servlet;

  @BeforeEach
  void setUp() {
    // Le pasamos manualmente todos los Mocks al Servlet mediante el nuevo constructor
    servlet = new CarritoServlet(carritoDao, itemCarritoDao, productoDao, carritoService);
  }

  private Usuario simularUsuarioComun() {
    when(request.getSession(false)).thenReturn(session);
    Usuario user = new Usuario(1, "Juan", "juan@mail.com", "1234", Rol.USUARIO);
    when(session.getAttribute("usuario")).thenReturn(user);
    return user;
  }

  // --- CONTROL DE ACCESO ---

  @Test
  void deberia_RedirigirALogin_Cuando_NoHaySesion() throws Exception {
    when(request.getSession(false)).thenReturn(null);

    servlet.doGet(request, response);

    verify(response).sendRedirect("login.jsp");
    verify(carritoDao, never()).getActivoByUsuarioId(anyInt());
  }

  @Test
  void deberia_RedirigirAIndex_Cuando_UsuarioEsAdmin() throws Exception {
    when(request.getSession(false)).thenReturn(session);
    Usuario admin = new Usuario(1, "Admin", "admin@mail.com", "1234", Rol.ADMIN);
    when(session.getAttribute("usuario")).thenReturn(admin);

    servlet.doGet(request, response);

    verify(response).sendRedirect("index.jsp");
    verify(carritoDao, never()).getActivoByUsuarioId(anyInt());
  }

  // --- GET ---

  @Test
  void deberia_VerCarritoCorrectamente_Cuando_AccionEsNull() throws Exception {
    Usuario u = simularUsuarioComun();
    Carrito c = new Carrito();
    c.setId(10);

    when(request.getParameter("accion")).thenReturn(null);
    when(carritoDao.getActivoByUsuarioId(u.getId())).thenReturn(c);
    when(itemCarritoDao.getByCarritoId(10)).thenReturn(new ArrayList<>());
    when(carritoService.calcularTotal(anyList())).thenReturn(0.0);
    when(request.getRequestDispatcher("carrito.jsp")).thenReturn(dispatcher);

    servlet.doGet(request, response);

    verify(request).setAttribute("carrito", c);
    verify(request).setAttribute(eq("items"), anyList());
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_VaciarCarrito_YRedirigir() throws Exception {
    Usuario u = simularUsuarioComun();
    Carrito c = new Carrito();
    c.setId(10);

    when(request.getParameter("accion")).thenReturn("vaciar");
    when(carritoDao.getActivoByUsuarioId(u.getId())).thenReturn(c);

    servlet.doGet(request, response);

    verify(itemCarritoDao).vaciarCarrito(10);
    verify(response).sendRedirect("SeCarrito?accion=ver");
  }

  // --- POST ---

  @Test
  void deberia_AgregarProducto_Cuando_HayStock() throws Exception {
    Usuario u = simularUsuarioComun();
    Producto p = new Producto();
    p.setId(100);
    p.setStock(5);

    Carrito c = new Carrito();
    c.setId(10);

    when(request.getParameter("accion")).thenReturn("agregar");
    when(request.getParameter("productoId")).thenReturn("100");
    when(request.getParameter("cantidad")).thenReturn("2");

    when(productoDao.getById(100)).thenReturn(p);
    when(carritoDao.obtenerOCrearActivo(u)).thenReturn(c);

    servlet.doPost(request, response);

    verify(itemCarritoDao).agregarOIncrementar(c, p, 2);
    verify(response).sendRedirect("SeCarrito?accion=ver");
  }

  @Test
  void deberia_RedirigirConError_AlAgregar_SiNoHayStock() throws Exception {
    simularUsuarioComun();
    Producto p = new Producto();
    p.setId(100);
    p.setStock(0); // Sin stock

    when(request.getParameter("accion")).thenReturn("agregar");
    when(request.getParameter("productoId")).thenReturn("100");
    when(request.getParameter("cantidad")).thenReturn("1");
    when(productoDao.getById(100)).thenReturn(p);

    servlet.doPost(request, response);

    verify(response).sendRedirect("SeProducto?accion=listar&error=sinStock");
    verify(itemCarritoDao, never()).agregarOIncrementar(any(), any(), anyInt());
  }
}