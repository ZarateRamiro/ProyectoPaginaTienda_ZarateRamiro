<%@ page import="org.isp63.prog1.enums.TipoProducto" %>
<%@ page import="org.isp63.prog1.entities.Producto" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ include file="header.jsp" %>

<%
    Producto producto = (Producto) request.getAttribute("producto");
    boolean esEdicion = producto != null;
%>

<main class="container my-4">
    <div class="row justify-content-center">
        <div class="col-12 col-lg-8">

            <h2 class="mb-3">
                <%= esEdicion ? "Editar producto" : "Agregar producto" %>
            </h2>

            <form action="SeProducto" method="post" class="bg-white border rounded-2 p-4">

                <% if (esEdicion) { %>
                    <input type="hidden" name="id" value="<%= producto.getId() %>">
                <% } %>

                <input type="hidden"
                       name="accion"
                       value="<%= esEdicion ? "actualizar" : "guardar" %>">

                <div class="row g-3">

                    <div class="col-12 col-md-6">
                        <label class="form-label" for="nombre">Nombre</label>

                        <input
                                type="text"
                                class="form-control"
                                id="nombre"
                                name="nombre"
                                value="<%= esEdicion ? producto.getNombre() : "" %>"
                                required>
                    </div>

                    <div class="col-12 col-md-6">

                        <label class="form-label" for="tipo">
                            Tipo de producto
                        </label>

                        <select class="form-select"
                                id="tipo"
                                name="tipo"
                                required>

                            <option value="">
                                Seleccionar tipo
                            </option>

                            <% for (TipoProducto tipo : TipoProducto.values()) { %>

                                <option
                                        value="<%= tipo.name() %>"
                                        <%= esEdicion && producto.getTipo() == tipo ? "selected" : "" %>>

                                    <%= tipo.getDescripcion() %>

                                </option>

                            <% } %>

                        </select>

                    </div>

                    <div class="col-12">

                        <label class="form-label"
                               for="descripcion">

                            Descripción

                        </label>

                        <textarea
                                class="form-control"
                                id="descripcion"
                                name="descripcion"
                                rows="3"
                                required><%= esEdicion ? producto.getDescripcion() : "" %></textarea>

                    </div>

                    <div class="col-12 col-md-4">

                        <label class="form-label"
                               for="precio">

                            Precio

                        </label>

                        <input
                                type="number"
                                step="0.01"
                                min="0"
                                class="form-control"
                                id="precio"
                                name="precio"
                                value="<%= esEdicion ? producto.getPrecio() : "" %>"
                                required>

                    </div>

                    <div class="col-12 col-md-4">

                        <label class="form-label"
                               for="stock">

                            Stock

                        </label>

                        <input
                                type="number"
                                min="0"
                                class="form-control"
                                id="stock"
                                name="stock"
                                value="<%= esEdicion ? producto.getStock() : "" %>"
                                required>

                    </div>

                    <div class="col-12">

                        <label class="form-label"
                               for="imagen">

                            URL de la imagen

                        </label>

                        <input
                                type="url"
                                class="form-control"
                                id="imagen"
                                name="imagen"
                                value="<%= esEdicion ? producto.getImagen() : "" %>"
                                required>

                    </div>

                </div>

                <div class="d-flex justify-content-end gap-2 mt-4">

                    <a href="SeProducto?accion=listar"
                       class="btn btn-outline-secondary">

                        Cancelar

                    </a>

                    <button type="submit"
                            class="btn btn-success">

                        <%= esEdicion ? "Actualizar" : "Guardar" %>

                    </button>

                </div>

            </form>

        </div>
    </div>
</main>

</body>
</html>