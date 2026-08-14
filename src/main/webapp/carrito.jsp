<%@ page import="java.util.List" %>
<%@ page import="org.isp63.prog1.entities.ItemCarrito" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ include file="header.jsp" %>

<%
    List<ItemCarrito> items = (List<ItemCarrito>) request.getAttribute("items");
    Double total = (Double) request.getAttribute("total");
    String mensajeExito = (String) request.getAttribute("mensajeExito");
    String mensajeError = (String) request.getAttribute("mensajeError");
%>

<main class="container my-4">
    <% if (mensajeExito != null) { %>
        <div class="alert alert-success"><%= mensajeExito %></div>
    <% } %>
    <% if (mensajeError != null) { %>
        <div class="alert alert-danger"><%= mensajeError %></div>
    <% } %>
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
        <div>
            <h2 class="mb-1">Mi carrito</h2>
            <p class="text-muted mb-0">Revisá tus productos antes de continuar.</p>
        </div>
        <a href="SeProducto?accion=listar" class="btn btn-outline-primary">Seguir comprando</a>
    </div>

    <% if (items != null && !items.isEmpty()) { %>
        <div class="table-responsive bg-white border rounded-2">
            <table class="table align-middle mb-0">
                <thead class="table-light">
                    <tr>
                        <th>Producto</th>
                        <th>Tipo</th>
                        <th>Precio</th>
                        <th style="width: 170px">Cantidad</th>
                        <th>Subtotal</th>
                        <th class="text-end">Acciones</th>
                    </tr>
                </thead>
                <tbody>
                <% for (ItemCarrito item : items) { %>
                    <tr>
                        <td>
                            <div class="d-flex align-items-center gap-3">
                                <img src="<%= item.getProducto().getImagen() %>" alt="<%= item.getProducto().getNombre() %>" style="width: 72px; height: 72px; object-fit: cover; border-radius: 8px;">
                                <div>
                                    <strong><%= item.getProducto().getNombre() %></strong>
                                    <div class="text-muted small"><%= item.getProducto().getDescripcion() %></div>
                                </div>
                            </div>
                        </td>
                    <td>
                        <%= item.getProducto().getTipo().getDescripcion() %>
                    </td>
                        <td>$<%= String.format("%.2f", item.getPrecioUnitario()) %></td>
                        <td>
                            <form method="post" action="SeCarrito" class="d-flex gap-2">
                                <input type="hidden" name="accion" value="actualizar">
                                <input type="hidden" name="itemId" value="<%= item.getId() %>">
                                <input type="number" class="form-control form-control-sm" name="cantidad" min="1" max="<%= item.getProducto().getStock() %>" value="<%= item.getCantidad() %>">
                                <button class="btn btn-outline-primary btn-sm" type="submit">OK</button>
                            </form>
                        </td>
                        <td><strong>$<%= String.format("%.2f", item.getSubtotal()) %></strong></td>
                        <td class="text-end">
                            <a href="SeCarrito?accion=quitar&itemId=<%= item.getId() %>" class="btn btn-outline-danger btn-sm">Quitar</a>
                        </td>
                    </tr>
                <% } %>
                </tbody>
                <tfoot>
                    <tr>
                        <td colspan="4" class="text-end"><strong>Total</strong></td>
                        <td colspan="2"><strong>$<%= String.format("%.2f", total != null ? total : 0) %></strong></td>
                    </tr>
                </tfoot>
            </table>
        </div>

        <div class="d-flex justify-content-end gap-2 mt-3">
            <a href="SeCarrito?accion=vaciar" class="btn btn-outline-danger" onclick="return confirm('¿Vaciar todo el carrito?');">Vaciar carrito</a>
            <form method="post" action="SeCarrito" class="d-inline">
                <input type="hidden" name="accion" value="finalizar">
                <button type="submit" class="btn btn-success" onclick="return confirm('¿Confirmar compra?');">Finalizar compra</button>
            </form>
        </div>
    <% } else { %>
        <div class="text-center bg-white border rounded-2 py-5">
            <h4>Tu carrito está vacío</h4>
            <p class="text-muted">Agregá productos desde el catálogo para verlos acá.</p>
            <a href="SeProducto?accion=listar" class="btn btn-primary">Ver productos</a>
        </div>
    <% } %>
</main>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
