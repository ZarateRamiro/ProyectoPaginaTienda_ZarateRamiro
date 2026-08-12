package org.isp63.prog1.daoTest;

import org.isp63.prog1.dao.ItemCarritoDao;
import org.isp63.prog1.interfaces.AdminConexion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.isp63.prog1.interfaces.AdmConnexion;
import org.isp63.prog1.entities.Carrito;
import org.isp63.prog1.entities.ItemCarrito;
import org.isp63.prog1.entities.Producto;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class ItemCarritoDAOTest {

  @Container
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("tienda")
      .withUsername("test")
      .withPassword("test");

  private ItemCarritoDao itemCarritoDAO;

  @BeforeAll
  static void setupDatabase() throws Exception {
    System.setProperty("db.url", mysql.getJdbcUrl());
    System.setProperty("db.user", mysql.getUsername());
    System.setProperty("db.password", mysql.getPassword());

    AdminConexion.INSTANCE.recargarPoolParaTests();

    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {

      // 1. Tabla usuario
      st.execute("CREATE TABLE usuario (" +
          "id INT AUTO_INCREMENT PRIMARY KEY, " +
          "nombre VARCHAR(100) NOT NULL, " +
          "email VARCHAR(100) NOT NULL UNIQUE, " +
          "password VARCHAR(100) NOT NULL, " +
          "rol ENUM('ADMINISTRADOR','USUARIO') NOT NULL)");

      // 2. Tabla producto
      st.execute("CREATE TABLE producto (" +
          "id INT AUTO_INCREMENT PRIMARY KEY, " +
          "nombre VARCHAR(100) NOT NULL, " +
          "descripcion TEXT, " +
          "precio DECIMAL(10,2) NOT NULL, " +
          "imagen VARCHAR(500), " +
          "stock INT NOT NULL DEFAULT 0, " +
          "tipo ENUM('REMERA','CAMPERA','BUZO','PANTALON','JEAN','SHORT','ZAPATILLAS','ZAPATOS','BOTAS','SANDALIAS','GORRA','ACCESORIOS') NOT NULL)");

      // 3. Tabla carrito
      st.execute("CREATE TABLE carrito (" +
          "id INT AUTO_INCREMENT PRIMARY KEY, " +
          "usuario_id INT NOT NULL, " +
          "fecha_creacion DATE NOT NULL, " +
          "estado VARCHAR(20) NOT NULL, " +
          "CONSTRAINT fk_carrito_usuario FOREIGN KEY(usuario_id) REFERENCES usuario(id) ON DELETE CASCADE ON UPDATE CASCADE)");

      // 4. Tabla item_carrito
      st.execute("CREATE TABLE item_carrito (" +
          "id INT AUTO_INCREMENT PRIMARY KEY, " +
          "carrito_id INT NOT NULL, " +
          "producto_id INT NOT NULL, " +
          "cantidad INT NOT NULL, " +
          "precio_unitario DECIMAL(10,2) NOT NULL, " +
          "CONSTRAINT fk_item_carrito FOREIGN KEY(carrito_id) REFERENCES carrito(id) ON DELETE CASCADE ON UPDATE CASCADE, " +
          "CONSTRAINT fk_item_producto FOREIGN KEY(producto_id) REFERENCES producto(id) ON DELETE RESTRICT ON UPDATE CASCADE)");
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    itemCarritoDAO = new ItemCarritoDao();

    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {

      // Desactivamos temporalmente las FK para truncar de forma limpia
      st.execute("SET FOREIGN_KEY_CHECKS = 0");
      st.execute("TRUNCATE TABLE item_carrito");
      st.execute("TRUNCATE TABLE carrito");
      st.execute("TRUNCATE TABLE producto");
      st.execute("TRUNCATE TABLE usuario");
      st.execute("SET FOREIGN_KEY_CHECKS = 1");

      // Insertamos datos semilla (ID = 1 para cada tabla padre)
      st.execute("INSERT INTO usuario (id, nombre, email, password, rol) " +
          "VALUES (1, 'Juan Perez', 'juan@mail.com', '123456', 'USUARIO')");

      st.execute("INSERT INTO producto (id, nombre, descripcion, precio, stock, tipo) " +
          "VALUES (1, 'Remera Classic', 'Remera 100% algodon', 1500.00, 10, 'REMERA')");

      st.execute("INSERT INTO producto (id, nombre, descripcion, precio, stock, tipo) " +
          "VALUES (2, 'Jean Azul', 'Jean talle M', 4500.00, 5, 'JEAN')");

      st.execute("INSERT INTO carrito (id, usuario_id, fecha_creacion, estado) " +
          "VALUES (1, 1, '2026-03-01', 'ACTIVO')");
    }
  }

  @Test
  void deberia_InsertarYObtenerItemCarrito_Cuando_DatosSonValidos() {
    // --- 1. ARRANGE ---
    ItemCarrito nuevoItem = crearItemBasico(1, 1, 2, 1500.00);

    // --- 2. ACT ---
    itemCarritoDAO.insert(nuevoItem);
    Integer idGenerado = nuevoItem.getId();

    ItemCarrito itemRecuperado = itemCarritoDAO.getById(idGenerado);

    // --- 3. ASSERT ---
    assertThat(idGenerado).isGreaterThan(0);
    assertThat(itemCarritoDAO.existsById(idGenerado)).isTrue();

    assertThat(itemRecuperado).isNotNull();
    assertThat(itemRecuperado.getCantidad()).isEqualTo(2);
    assertThat(itemRecuperado.getPrecioUnitario()).isEqualTo(1500.00);
    assertThat(itemRecuperado.getSubtotal()).isEqualTo(3000.00);
    assertThat(itemRecuperado.getCarrito().getId()).isEqualTo(1);
    assertThat(itemRecuperado.getProducto().getId()).isEqualTo(1);
  }

  @Test
  void deberia_ActualizarCantidadYPrecioUnitario_Cuando_SeModificaElItem() {
    // --- 1. ARRANGE ---
    ItemCarrito item = crearItemBasico(1, 1, 1, 1500.00);
    itemCarritoDAO.insert(item);

    ItemCarrito aModificar = itemCarritoDAO.getById(item.getId());
    aModificar.setCantidad(5);
    aModificar.setPrecioUnitario(1400.00);

    // --- 2. ACT ---
    itemCarritoDAO.update(aModificar);

    // --- 3. ASSERT ---
    ItemCarrito modificado = itemCarritoDAO.getById(item.getId());
    assertThat(modificado.getCantidad()).isEqualTo(5);
    assertThat(modificado.getPrecioUnitario()).isEqualTo(1400.00);
    assertThat(modificado.getSubtotal()).isEqualTo(7000.00);
  }

  @Test
  void deberia_EliminarItemCarrito_Cuando_SeProporcionaUnIdValido() {
    // --- 1. ARRANGE ---
    ItemCarrito item = crearItemBasico(1, 1, 1, 1500.00);
    itemCarritoDAO.insert(item);
    Integer id = item.getId();

    assertThat(itemCarritoDAO.existsById(id)).isTrue();

    // --- 2. ACT ---
    itemCarritoDAO.delete(id);

    // --- 3. ASSERT ---
    assertThat(itemCarritoDAO.existsById(id)).isFalse();
    assertThat(itemCarritoDAO.getById(id)).isNull();
  }

  @Test
  void deberia_ObtenerItemsPorCarritoId() {
    // --- 1. ARRANGE ---
    // Agregamos dos ítems distintos (uno con producto 1 y otro con producto 2) al carrito 1
    itemCarritoDAO.insert(crearItemBasico(1, 1, 2, 1500.00));
    itemCarritoDAO.insert(crearItemBasico(1, 2, 1, 4500.00));

    // --- 2. ACT ---
    List<ItemCarrito> itemsDelCarrito = itemCarritoDAO.getByCarritoId(1);

    // --- 3. ASSERT ---
    assertThat(itemsDelCarrito).hasSize(2);
    assertThat(itemsDelCarrito).extracting(ItemCarrito::getCantidad).containsExactlyInAnyOrder(2, 1);
  }

  @Test
  void deberia_ObtenerTodosLosItems() {
    // --- 1. ARRANGE ---
    itemCarritoDAO.insert(crearItemBasico(1, 1, 1, 1500.00));
    itemCarritoDAO.insert(crearItemBasico(1, 2, 3, 4500.00));

    // --- 2. ACT ---
    List<ItemCarrito> todos = itemCarritoDAO.getAll();

    // --- 3. ASSERT ---
    assertThat(todos).hasSize(2);
  }

  // --- MÉTODOS AUXILIARES ---
  private ItemCarrito crearItemBasico(Integer carritoId, Integer productoId, int cantidad, double precioUnitario) {
    Carrito c = new Carrito();
    c.setId(carritoId);

    Producto p = new Producto();
    p.setId(productoId);

    ItemCarrito item = new ItemCarrito();
    item.setCarrito(c);
    item.setProducto(p);
    item.setCantidad(cantidad);
    item.setPrecioUnitario(precioUnitario);

    return item;
  }
}