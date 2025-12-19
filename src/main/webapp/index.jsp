<%@ include file="header.jsp" %>


<div class="d-flex flex-column justify-content-center align-items-center vh-100">
    <h1 class="mb-4 text-primary">Bienvenido a Mi Tienda</h1>
    <p class="mb-4 text-secondary">Explora nuestros productos disponibles.</p>

    <%
        // **LÓGICA DE REDIRECCIÓN:**
        String urlDestino = "productos.jsp"; // URL por defecto (para usuarios no logueados o normales)

        if (usuario != null && "admin".equalsIgnoreCase(usuario.getRol())) {
            // Si es un administrador, lo enviamos a la vista de administración
            urlDestino = "productosAdmin.jsp";
        }
    %>

    <a href="<%= urlDestino %>" class="btn btn-success btn-lg">Ver productos</a>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>