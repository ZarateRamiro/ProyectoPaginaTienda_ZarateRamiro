package org.isp63.prog1.servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.isp63.prog1.dao.ProductoDao;
import org.isp63.prog1.entities.Producto;
import org.isp63.prog1.entities.Usuario;
import org.isp63.prog1.enums.TipoProducto;
import org.isp63.prog1.servlets.ProductoServlet;
import org.isp63.prog1.util.Rol;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServletTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private HttpSession session;

  @Mock
  private RequestDispatcher dispatcher;

  @Mock
  private ProductoDao productoDao;

  @InjectMocks
  private ProductoServlet servlet;

  private void simularAdmin() {
    when(request.getSession()).thenReturn(session);
    Usuario admin = new Usuario(1, "Admin", "admin@mail.com", "1234", Rol.ADMIN);
    when(session.getAttribute("usuario")).thenReturn(admin);
  }

  @Test
  void deberia_ListarTodosLosProductos_Cuando_NoHayTipo() throws Exception {
    when(request.getParameter("accion")).thenReturn(null);
    when(request.getParameter("tipo")).thenReturn(null);
    when(productoDao.getAll()).thenReturn(new ArrayList<>());
    when(request.getRequestDispatcher("productos.jsp")).thenReturn(dispatcher);

    servlet.doGet(request, response);

    verify(productoDao).getAll();
    verify(request).setAttribute(eq("productos"), anyList());
    verify(dispatcher).forward(request, response);
  }

  @Test
  void deberia_FiltrarProductos_Cuando_SeEspecificaTipo() throws Exception {
    // 1. Simular parámetros de Request
    when(request.getParameter("accion")).thenReturn("listar"); // o null
    when(request.getParameter("tipo")).thenReturn("BUZO");

    // 2. Crear datos de prueba
    Producto p1 = new Producto();
    p1.setTipo(TipoProducto.BUZO);

    Producto p2 = new Producto();
    p2.setTipo(TipoProducto.REMERA);

    List<Producto> listaCompleta = List.of(p1, p2);

    // 3. Mockear respuestas del DAO y Dispatcher
    when(productoDao.getAll()).thenReturn(listaCompleta);
    when(request.getRequestDispatcher("productos.jsp")).thenReturn(dispatcher);

    // 4. Ejecutar
    servlet.doGet(request, response);

    // 5. Verificaciones
    verify(productoDao).getAll();
    verify(request).setAttribute(eq("productos"), anyList());
    verify(request).setAttribute("tipoSeleccionado", "BUZO");
    verify(dispatcher).forward(request, response);
  }
  @Test
  void deberia_GuardarProducto_SiEsAdmin() throws Exception {
    simularAdmin();
    when(request.getParameter("accion")).thenReturn("guardar");
    when(request.getParameter("nombre")).thenReturn("Campera");
    when(request.getParameter("descripcion")).thenReturn("Campera de cuero");
    when(request.getParameter("precio")).thenReturn("15000.0");
    when(request.getParameter("imagen")).thenReturn("img.jpg");
    when(request.getParameter("stock")).thenReturn("10");
    when(request.getParameter("tipo")).thenReturn("BUZO");

    servlet.doPost(request, response);

    verify(productoDao).insert(any(Producto.class));
    verify(response).sendRedirect("SeProducto?accion=listar");
  }

  @Test
  void deberia_RedirigirAIndex_AlGuardar_SiUsuarioNoEsAdmin() throws Exception {
    when(request.getSession()).thenReturn(session);
    when(session.getAttribute("usuario")).thenReturn(null);
    when(request.getParameter("accion")).thenReturn("guardar");

    servlet.doPost(request, response);

    verify(productoDao, never()).insert(any());
    verify(response).sendRedirect("index.jsp");
  }
}