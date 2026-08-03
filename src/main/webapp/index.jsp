<%@ include file="header.jsp" %>

<main class="container py-5">
    <section class="row align-items-center g-4 py-4">
        <div class="col-12 col-lg-6">
            <p class="text-uppercase text-primary fw-semibold mb-2">Tienda deportiva</p>
            <h1 class="display-5 fw-bold mb-3">Productos, categorías y carrito en un solo lugar</h1>
            <p class="lead text-muted mb-4">Explorá el catálogo, filtrá por categoría y armá tu carrito si ya iniciaste sesión.</p>
            <div class="d-flex gap-2 flex-wrap">
                <a href="SeProducto?accion=listar" class="btn btn-success btn-lg">Ver productos</a>
                <% if (usuario == null) { %>
                    <a href="login.jsp" class="btn btn-outline-primary btn-lg">Iniciar sesión</a>
                <% } else if (!"ADMIN".equalsIgnoreCase(usuario.getRol())) { %>
                    <a href="SeCarrito?accion=ver" class="btn btn-outline-primary btn-lg">Mi carrito</a>
                <% } %>
            </div>
        </div>
        <div class="col-12 col-lg-6">
            <div class="bg-white border rounded-2 p-4 shadow-sm">
                <div class="row g-3">
                    <div class="col-6">
                        <div class="border rounded-2 p-3 h-100">
                            <strong>Filtros</strong>
                            <p class="text-muted mb-0 small">Catálogo por categoría.</p>
                        </div>
                    </div>
                    <div class="col-6">
                        <div class="border rounded-2 p-3 h-100">
                            <strong>Carrito</strong>
                            <p class="text-muted mb-0 small">Items, cantidades y total.</p>
                        </div>
                    </div>
                    <div class="col-12">
                        <div class="border rounded-2 p-3">
                            <strong>Administración</strong>
                            <p class="text-muted mb-0 small">Alta, baja y modificación de productos y usuarios.</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>
</main>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
