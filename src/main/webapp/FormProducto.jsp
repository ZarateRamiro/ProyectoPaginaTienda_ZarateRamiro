<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.isp63.prog1.entities.Producto" %>

<%
    // Verificamos si se envió un producto desde el servlet (modo edición)
    Producto producto = (Producto) request.getAttribute("producto");
    boolean esEdicion = (producto != null);
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title><%= esEdicion ? "Editar Producto" : "Agregar Nuevo Producto" %></title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>

<body class="bg-light">

    <div class="container my-5">
        <h2 class="text-center mb-4">
            <%= esEdicion ? "Editar Producto" : "Agregar Nuevo Producto" %>
        </h2>

        <!-- 👇 Aquí va el formulario que sirve tanto para crear como editar -->
        <form action="SeProducto" method="POST"

            <!-- Campo oculto para el id (solo si es edición) -->
            <% if (esEdicion) { %>
                <input type="hidden" name="id" value="<%= producto.getId() %>">
            <% } %>

            <!-- Campo oculto para definir la acción -->
            <input type="hidden" name="accion" value="<%= esEdicion ? "actualizar" : "guardar" %>">

            <div class="mb-3">
                <label for="nombre" class="form-label">Nombre</label>
                <input type="text" class="form-control" id="nombre" name="nombre"
                       value="<%= esEdicion ? producto.getNombre() : "" %>" required>
            </div>

            <div class="mb-3">
                <label for="descripcion" class="form-label">Descripción</label>
                <textarea class="form-control" id="descripcion" name="descripcion" rows="3" required><%= esEdicion ? producto.getDescripcion() : "" %></textarea>
            </div>

            <div class="mb-3">
                <label for="precio" class="form-label">Precio</label>
                <input type="number" step="0.01" class="form-control" id="precio" name="precio"
                       value="<%= esEdicion ? producto.getPrecio() : "" %>" required>
            </div>

            <div class="mb-3">
                <label for="imagen" class="form-label">URL de Imagen</label>
                <input type="text" class="form-control" id="imagen" name="imagen"
                       value="<%= esEdicion ? producto.getImagen() : "" %>" required>
            </div>

            <div class="d-grid gap-2 d-md-flex justify-content-md-end">
                <a href="productosAdmin.jsp" class="btn btn-secondary me-md-2">Cancelar</a>
                <button type="submit" class="btn btn-success">
                    <%= esEdicion ? "Actualizar" : "Guardar" %> Producto
                </button>
            </div>
        </form>
    </div>

</body>
</html>
