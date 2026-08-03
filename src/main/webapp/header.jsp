<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Tienda</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<%
    org.isp63.prog1.entities.Usuario usuario =
        (org.isp63.prog1.entities.Usuario) session.getAttribute("usuario");
    boolean esAdminHeader = usuario != null && "ADMIN".equalsIgnoreCase(usuario.getRol());
    boolean esUsuarioComunHeader = usuario != null && !esAdminHeader;
%>

<nav class="navbar navbar-expand-lg navbar-dark bg-primary">
    <div class="container-fluid">
        <a class="navbar-brand" href="index.jsp">Mi Tienda</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarTienda" aria-controls="navbarTienda" aria-expanded="false" aria-label="Menú">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="navbarTienda">
            <div class="navbar-nav me-auto">
                <a class="nav-link" href="SeProducto?accion=listar">Productos</a>
                <% if (esUsuarioComunHeader) { %>
                    <a class="nav-link" href="SeCarrito?accion=ver">Carrito</a>
                <% } %>
                <% if (esAdminHeader) { %>
                    <a class="nav-link" href="SeUsuario?accion=listar">Usuarios</a>
                <% } %>
            </div>

            <div class="d-flex align-items-center gap-2 text-white">
                <% if (usuario != null) { %>
                    <span>Hola, <strong><%= usuario.getNombre() %></strong></span>
                    <a href="logout" class="btn btn-light btn-sm">Cerrar sesión</a>
                <% } else { %>
                    <a href="login.jsp" class="btn btn-light btn-sm">Iniciar sesión</a>
                <% } %>
            </div>
        </div>
    </div>
</nav>

