<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Agregar Nuevo Producto</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>

<body class="bg-light">

    <div class="container my-5">
        <h2 class="text-center mb-4">Agregar Nuevo Producto</h2>

        <form action="SeProducto" method="POST" class="p-4 bg-white shadow-sm rounded">

            <div class="mb-3">
                <label for="nombre" class="form-label">Nombre</label>
                <input type="text" class="form-control" id="nombre" name="nombre" required>
            </div>

            <div class="mb-3">
                <label for="descripcion" class="form-label">Descripción</label>
                <textarea class="form-control" id="descripcion" name="descripcion" rows="3" required></textarea>
            </div>

            <div class="mb-3">
                <label for="precio" class="form-label">Precio</label>
                <input type="number" step="0.01" class="form-control" id="precio" name="precio" required>
            </div>

            <div class="mb-3">
                <label for="imagen" class="form-label">URL de Imagen</label>
                <input type="text" class="form-control" id="imagen" name="imagen" required>
            </div>

            <div class="d-grid gap-2 d-md-flex justify-content-md-end">
                <a href="productosAdmin.jsp" class="btn btn-secondary me-md-2">Cancelar</a>
                <button type="submit" class="btn btn-success">Guardar Producto</button>
            </div>
        </form>
    </div>

</body>
</html>