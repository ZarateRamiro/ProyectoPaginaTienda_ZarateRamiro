<%@ page import="java.util.List" %>
<%@ page import="org.isp63.prog1.entities.Usuario" %>

<jsp:include page="header.jsp" />

<div class="container mt-4">
    <h2 class="mb-3 text-primary">Administrar usuarios</h2>

    <table class="table table-bordered table-hover align-middle">
        <thead class="table-dark">
            <tr>
                <th>ID</th>
                <th>Nombre</th>
                <th>Email</th>
                <th>Rol</th>
                <th style="width: 180px">Acciones</th>
            </tr>
        </thead>
        <tbody>
        <%
            List<Usuario> usuarios = (List<Usuario>) request.getAttribute("usuarios");
            if (usuarios != null && !usuarios.isEmpty()) {
                for (Usuario u : usuarios) {
        %>
            <tr>
                <td><%= u.getId() %></td>
                <td><%= u.getNombre() %></td>
                <td><%= u.getEmail() %></td>
                <td><%= u.getRol() %></td>
                <td>
                    <a href="SeUsuario?accion=editar&id=<%= u.getId() %>"
                       class="btn btn-warning btn-sm">
                        Editar
                    </a>

                    <a href="SeUsuario?accion=eliminar&id=<%= u.getId() %>"
                       class="btn btn-danger btn-sm"
                       onclick="return confirm('¿Seguro que querés eliminar este usuario?');">
                        Eliminar
                    </a>
                </td>
            </tr>
        <%
                }
            } else {
        %>
            <tr>
                <td colspan="5" class="text-center text-muted">
                    No hay usuarios registrados
                </td>
            </tr>
        <%
            }
        %>
        </tbody>
    </table>

    <a href="SeUsuario?accion=nuevo" class="btn btn-success">
        + Nuevo usuario
    </a>
</div>

</body>
</html>
