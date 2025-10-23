<%@ page import="java.util.List" %>
<%@ page import="org.isp63.prog1.entities.Producto" %>
<%@ page import="org.isp63.prog1.dao.ProductoDao" %>
<%@ page import="org.isp63.prog1.entities.Usuario" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Administración de Productos</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        /* Estilo para truncar la descripción a un máximo de 3 líneas con puntos suspensivos */
        .descripcion-clamp {
            display: -webkit-box;
            -webkit-line-clamp: 3;
            -webkit-box-orient: vertical;
            overflow: hidden;
            text-overflow: ellipsis;
        }
    </style>
</head>
<body class="bg-light">
<nav class="navbar navbar-expand-lg navbar-dark bg-primary">
    <div class="container-fluid">
        <a class="navbar-brand" href="index.jsp">Mi Tienda</a>
        <div class="d-flex">
            <%
                // Recuperamos el objeto Usuario de la sesión (solo una vez)
                org.isp63.prog1.entities.Usuario usuario =
                    (org.isp63.prog1.entities.Usuario) session.getAttribute("usuario");
            %>

            <% if (usuario != null) { %>
                <div class="usuario-logueado">
                    <span>Hola, <strong><%= usuario.getNombre() %></strong>!</span>
                    <a href="logout" style="margin-left:10px;" class="btn btn-light">Cerrar sesión</a>
                </div>
            <% } else { %>
                <a href="login.jsp" class="btn btn-light">Iniciar sesión</a>
            <% } %>

        </div>
    </div>
</nav>

    <div class="container my-5">
        <h2 class="text-center mb-4">Administración de Productos</h2>

        <div class="d-flex justify-content-end mb-4">
            <a href="FormProducto.jsp" class="btn btn-success btn-lg">
                Agregar Producto
            </a>
        </div>
        <div class="row row-cols-1 row-cols-sm-2 row-cols-md-3 row-cols-lg-4 g-4">
            <%
                ProductoDao dao = new ProductoDao();
                List<Producto> productos = dao.getAll();

                if (productos != null && !productos.isEmpty()) {
                    for (Producto p : productos) {
            %>
                       <div class="col d-flex">
                           <div class="card shadow-sm flex-fill">
                               <img src="<%= p.getImagen() %>" class="card-img-top" alt="<%= p.getNombre() %>"
                                    style="height: 200px; object-fit: cover;">

                               <div class="card-body d-flex flex-column text-center">
                                   <h5 class="card-title"><%= p.getNombre() %></h5>

                                   <p class="card-text text-muted descripcion-clamp flex-grow-1 mb-2">
                                       <%= p.getDescripcion() %>
                                   </p>

                                   <p class="fw-bold mb-3 mt-auto">$<%= p.getPrecio() %></p>

                                   <div class="d-flex justify-content-center flex-wrap">
                                       <a href="#" class="btn btn-outline-primary btn-sm me-2 mb-1">Ver más</a>

                                       <a href="FormProducto.jsp?id=<%= p.getId() %>" class="btn btn-warning btn-sm me-2 mb-1">Editar</a>
                                       <a href="seProducto?operacion=eliminar&id=<%=p.getId()%>" class="btn btn-danger btn-sm mb-1">Eliminar</a>
                                   </div>
                               </div>
                           </div>
                       </div> <%
                    }
                } else {
            %>
                    <div class="col-12 text-center text-muted">
                        <p>No hay productos disponibles por el momento.</p>
                    </div>
            <%
                }
            %>
        </div>
    </div>
</body>
</html>