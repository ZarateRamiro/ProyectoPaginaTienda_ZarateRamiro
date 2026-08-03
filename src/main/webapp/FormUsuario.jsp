<%@ page import="org.isp63.prog1.entities.Usuario" %>

<jsp:include page="header.jsp" />

<div class="container mt-4">
    <%
        Usuario usuario = (Usuario) request.getAttribute("usuario");
        boolean esEdicion = (usuario != null);
    %>

    <h2 class="mb-3 text-primary">
        <%= esEdicion ? "Editar usuario" : "Nuevo usuario" %>
    </h2>

    <form method="post" action="SeUsuario">
        <!-- Acción -->
        <input type="hidden" name="accion" value="<%= esEdicion ? "actualizar" : "guardar" %>">

        <!-- ID solo en edición -->
        <% if (esEdicion) { %>
            <input type="hidden" name="id" value="<%= usuario.getId() %>">
        <% } %>

        <!-- Nombre -->
        <div class="mb-3">
            <label class="form-label">Nombre</label>
            <input type="text"
                   name="nombre"
                   class="form-control"
                   required
                   value="<%= esEdicion ? usuario.getNombre() : "" %>">
        </div>

        <!-- Email -->
        <div class="mb-3">
            <label class="form-label">Email</label>
            <input type="email"
                   name="email"
                   class="form-control"
                   required
                   value="<%= esEdicion ? usuario.getEmail() : "" %>">
        </div>

        <!-- Password solo al crear -->
        <% if (!esEdicion) { %>
        <div class="mb-3">
            <label class="form-label">Contrasenia</label>
            <input type="password"
                   name="password"
                   class="form-control"
                   required>
        </div>
        <% } %>

        <!-- Rol -->
        <div class="mb-3">
            <label class="form-label">Rol</label>
            <select name="rol" class="form-select">
                <option value="USUARIO"
                    <%= esEdicion && "USUARIO".equalsIgnoreCase(usuario.getRol()) ? "selected" : "" %>>
                    Usuario
                </option>
                <option value="ADMIN"
                    <%= esEdicion && "ADMIN".equalsIgnoreCase(usuario.getRol()) ? "selected" : "" %>>
                    Administrador
                </option>
            </select>
        </div>

        <!-- Botones -->
        <button type="submit" class="btn btn-primary">
            <%= esEdicion ? "Actualizar" : "Guardar" %>
        </button>

        <a href="SeUsuario?accion=listar" class="btn btn-secondary ms-2">
            Cancelar
        </a>
    </form>
</div>

</body>
</html>
