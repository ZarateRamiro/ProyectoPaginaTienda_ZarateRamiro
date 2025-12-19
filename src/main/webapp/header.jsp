<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Inicio - Tienda</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
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

            <% if (usuario != null && "admin".equalsIgnoreCase(usuario.getRol())) { %>
                <a href="SeUsuario?accion=listar" class="btn btn-warning ms-2">
                    Administrar usuarios
                </a>
            <% } %>
        </div>
    </div>
</nav>
<hr>