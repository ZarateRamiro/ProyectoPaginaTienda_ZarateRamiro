<%@ page import="java.util.List" %>
<%@ page import="org.isp63.prog1.entities.Producto" %>
<%@ page import="org.isp63.prog1.entities.Usuario" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ include file="header.jsp" %>

<%
    List<Producto> productos = (List<Producto>) request.getAttribute("productos");
    String errorParam = request.getParameter("error");
    boolean esAdmin = usuario != null && "ADMIN".equalsIgnoreCase(usuario.getRol());
    boolean esUsuarioComun = usuario != null && !esAdmin;
%>

<style>
    .catalog-toolbar {
        background: #ffffff;
        border: 1px solid #e6e9ef;
        border-radius: 8px;
        padding: 1rem;
    }

    .product-card {
        border: 1px solid #e8ecf2;
        border-radius: 8px;
        overflow: hidden;
        transition: transform .15s ease, box-shadow .15s ease;
    }

    .product-card:hover {
        transform: translateY(-2px);
        box-shadow: 0 .75rem 1.5rem rgba(20, 33, 61, .08);
    }

    .product-image {
        height: 210px;
        object-fit: cover;
        background: #f2f4f7;
    }

    .descripcion-clamp {
        display: -webkit-box;
        -webkit-line-clamp: 3;
        -webkit-box-orient: vertical;
        overflow: hidden;
    }
</style>

<main class="container my-4">

    <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-3">
        <div>
            <h2 class="mb-1">Productos</h2>
            <p class="text-muted mb-0">Explorá nuestro catálogo.</p>
        </div>

        <% if (esAdmin) { %>
            <a href="SeProducto?accion=nuevo" class="btn btn-success">
                Agregar producto
            </a>
        <% } %>
    </div>

    <% if ("sinStock".equals(errorParam)) { %>
        <div class="alert alert-warning">
            El producto no tiene stock disponible.
        </div>
    <% } %>

    <div class="row row-cols-1 row-cols-sm-2 row-cols-lg-3 row-cols-xl-4 g-4">

        <% if (productos != null && !productos.isEmpty()) { %>

            <% for (Producto p : productos) { %>

                <%
                    boolean tieneStock = p.getStock() > 0;
                %>

                <div class="col d-flex">

                    <article class="card product-card flex-fill">

                        <img
                                src="<%= p.getImagen() %>"
                                class="card-img-top product-image"
                                alt="<%= p.getNombre() %>">

                        <div class="card-body d-flex flex-column">

                            <div class="d-flex justify-content-between align-items-start gap-2 mb-2">

                                <h5 class="card-title mb-0">
                                    <%= p.getNombre() %>
                                </h5>

                                <span class="badge text-bg-light border">
                                    <%= p.getTipo().getDescripcion() %>
                                </span>

                            </div>

                            <p class="card-text text-muted descripcion-clamp flex-grow-1">
                                <%= p.getDescripcion() %>
                            </p>

                            <div class="d-flex justify-content-between align-items-center mb-3">

                                <strong>
                                    $<%= String.format("%.2f", p.getPrecio()) %>
                                </strong>

                                <span class="<%= tieneStock ? "text-success" : "text-danger" %> small">
                                    <%= tieneStock ? "Stock: " + p.getStock() : "Sin stock" %>
                                </span>

                            </div>

                            <% if (esUsuarioComun) { %>

                                <form method="post" action="SeCarrito" class="d-flex gap-2 mt-auto">

                                    <input type="hidden" name="accion" value="agregar">

                                    <input type="hidden"
                                           name="productoId"
                                           value="<%= p.getId() %>">

                                    <input type="number"
                                           name="cantidad"
                                           class="form-control form-control-sm"
                                           value="1"
                                           min="1"
                                           max="<%= p.getStock() %>"
                                           <%= tieneStock ? "" : "disabled" %>>

                                    <button type="submit"
                                            class="btn btn-primary btn-sm"
                                            <%= tieneStock ? "" : "disabled" %>>

                                        Agregar

                                    </button>

                                </form>

                            <% } else if (esAdmin) { %>

                                <div class="d-flex gap-2 mt-auto">

                                    <a href="SeProducto?accion=editar&id=<%= p.getId() %>"
                                       class="btn btn-warning btn-sm">
                                        Editar
                                    </a>

                                    <a href="SeProducto?accion=eliminar&id=<%= p.getId() %>"
                                       class="btn btn-danger btn-sm"
                                       onclick="return confirm('¿Seguro que deseas eliminar este producto?');">

                                        Eliminar

                                    </a>

                                </div>

                            <% } else { %>

                                <a href="login.jsp"
                                   class="btn btn-outline-primary btn-sm mt-auto">

                                    Iniciar sesión para comprar

                                </a>

                            <% } %>

                        </div>

                    </article>

                </div>

            <% } %>

        <% } else { %>

            <div class="col-12 text-center text-muted py-5">
                <p>No hay productos disponibles por el momento.</p>
            </div>

        <% } %>

    </div>

</main>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

</body>
</html>