package org.isp63.prog1.daoTest;

import org.isp63.prog1.dao.ItemCarritoDao;
import org.isp63.prog1.entities.Carrito;
import org.isp63.prog1.entities.ItemCarrito;
import org.isp63.prog1.entities.Producto;
import org.isp63.prog1.enums.TipoProducto;
import org.isp63.prog1.interfaces.AdminConexion;
import org.isp63.prog1.util.ConexionPool;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemCarritoDaoTest {

  // Arreglado: Removidas anotaciones para controlar el inicio del contenedor manualmente en @BeforeAll
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("tienda")
      .withUsername("test")
      .withPassword("test");

  private ItemCarritoDao itemCarritoDao;
  private Carrito carritoBase;
  private Producto productoBase;
  private Producto productoSecundario;

  @BeforeAll
  static void setupDatabase() throws Exception {
    if (!mysql.isRunning()) {
      mysql.start();
    }

    System.setProperty("db.url", mysql.getJdbcUrl());
    System.setProperty("db.user", mysql.getUsername());
    System.setProperty("db.pass", mysql.getPassword());
    System.setProperty("db.password", mysql.getPassword());

    ConexionPool.close();
    AdminConexion.INSTANCE.recargarPoolParaTests();

    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {

      st.execute("CREATE TABLE IF NOT EXISTS usuario (" +
          "id INT AUTO_INCREMENT PRIMARY KEY, " +
          "nombre VARCHAR(100) NOT NULL, " +
          "email VARCHAR(100) NOT NULL UNIQUE, " +
          "password VARCHAR(100) NOT NULL, " +
          "rol ENUM('ADMIN','USUARIO') NOT NULL)");

      st.execute("CREATE TABLE IF NOT EXISTS producto (" +
          "id INT AUTO_INCREMENT PRIMARY KEY, " +
          "nombre VARCHAR(100) NOT NULL, " +
          "descripcion TEXT, " +
          "precio DECIMAL(10,2) NOT NULL, " +
          "imagen VARCHAR(500), " +
          "stock INT NOT NULL DEFAULT 0, " +
          "tipo ENUM('REMERA','CAMPERA','BUZO','PANTALON','JEAN','SHORT'," +
          "'ZAPATILLAS','ZAPATOS','BOTAS','SANDALIAS','GORRA','ACCESORIOS') NOT NULL)");

      st.execute("CREATE TABLE IF NOT EXISTS carrito (" +
          "id INT AUTO_INCREMENT PRIMARY KEY, " +
          "usuario_id INT NOT NULL, " +
          "fecha_creacion DATE NOT NULL, " +
          "estado VARCHAR(20) NOT NULL, " +
          "CONSTRAINT fk_carrito_usuario FOREIGN KEY(usuario_id) " +
          "REFERENCES usuario(id) ON DELETE CASCADE ON UPDATE CASCADE)");

      st.execute("CREATE TABLE IF NOT EXISTS item_carrito (" +
          "id INT AUTO_INCREMENT PRIMARY KEY, " +
          "carrito_id INT NOT NULL, " +
          "producto_id INT NOT NULL, " +
          "cantidad INT NOT NULL, " +
          "precio_unitario DECIMAL(10,2) NOT NULL, " +
          "CONSTRAINT fk_item_carrito FOREIGN KEY(carrito_id) " +
          "REFERENCES carrito(id) ON DELETE CASCADE ON UPDATE CASCADE, " +
          "CONSTRAINT fk_item_producto FOREIGN KEY(producto_id) " +
          "REFERENCES producto(id) ON DELETE RESTRICT ON UPDATE CASCADE)");
    }
  }

  @AfterAll
  static void tearDownAll() {
    ConexionPool.close();
    AdminConexion.INSTANCE.cerrarPool();
    System.clearProperty("db.url");
    System.clearProperty("db.user");
    System.clearProperty("db.pass");
    System.clearProperty("db.password");
  }

  @BeforeEach
  void setUp() throws Exception {
    itemCarritoDao = new ItemCarritoDao();

    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {

      st.execute("SET FOREIGN_KEY_CHECKS = 0");
      st.execute("TRUNCATE TABLE item_carrito");
      st.execute("TRUNCATE TABLE carrito");
      st.execute("TRUNCATE TABLE producto");
      st.execute("TRUNCATE TABLE usuario");
      st.execute("SET FOREIGN_KEY_CHECKS = 1");

      st.execute("INSERT INTO usuario (id, nombre, email, password, rol) " +
          "VALUES (1, 'Juan Perez', 'juan@mail.com', '123456', 'USUARIO')");

      st.execute("INSERT INTO producto (id, nombre, descripcion, precio, stock, tipo) " +
          "VALUES (1, 'Remera Classic', 'Remera 100% algodon', 1500.00, 10, 'REMERA')");

      st.execute("INSERT INTO producto (id, nombre, descripcion, precio, stock, tipo) " +
          "VALUES (2, 'Jean Azul', 'Jean talle M', 4500.00, 5, 'JEAN')");

      st.execute("INSERT INTO carrito (id, usuario_id, fecha_creacion, estado) " +
          "VALUES (1, 1, '2026-03-01', 'ACTIVO')");
    }

    carritoBase = new Carrito(1);
    productoBase = crearProducto(1, "Remera Classic", TipoProducto.REMERA, 1500.00, 10);
    productoSecundario = crearProducto(2, "Jean Azul", TipoProducto.JEAN, 4500.00, 5);
  }

  // =================================================================================
  // TESTS PARA INSERT / GET BY ID
  // =================================================================================

  @Test
  void deberia_InsertarYObtenerItemCarrito_Cuando_DatosSonValidos() {
    ItemCarrito nuevoItem = new ItemCarrito(null, carritoBase, productoBase, 2, 1500.00);

    itemCarritoDao.insert(nuevoItem);
    ItemCarrito recuperado = itemCarritoDao.getById(nuevoItem.getId());

    assertThat(nuevoItem.getId()).isGreaterThan(0);
    assertThat(recuperado).isNotNull();
    assertThat(recuperado.getCantidad()).isEqualTo(2);
    assertThat(recuperado.getPrecioUnitario()).isEqualTo(1500.00);
    assertThat(recuperado.getSubtotal()).isEqualTo(3000.00);
    assertThat(recuperado.getCarrito().getId()).isEqualTo(1);
    assertThat(recuperado.getProducto().getId()).isEqualTo(1);
    assertThat(recuperado.getProducto().getNombre()).isEqualTo("Remera Classic");
  }

  @Test
  void getById_DeberiaRetornarNull_CuandoNoExiste() {
    assertThat(itemCarritoDao.getById(999)).isNull();
  }

  // =================================================================================
  // TESTS PARA UPDATE
  // =================================================================================

  @Test
  void update_DeberiaActualizarCantidadYPrecioUnitario() {
    ItemCarrito item = new ItemCarrito(null, carritoBase, productoBase, 1, 1500.00);
    itemCarritoDao.insert(item);

    ItemCarrito aModificar = itemCarritoDao.getById(item.getId());
    aModificar.setCantidad(5);
    aModificar.setPrecioUnitario(1400.00);

    itemCarritoDao.update(aModificar);

    ItemCarrito modificado = itemCarritoDao.getById(item.getId());
    assertThat(modificado.getCantidad()).isEqualTo(5);
    assertThat(modificado.getPrecioUnitario()).isEqualTo(1400.00);
    assertThat(modificado.getSubtotal()).isEqualTo(7000.00);
  }

  // =================================================================================
  // TESTS PARA DELETE / EXISTS
  // =================================================================================

  @Test
  void deberia_EliminarItemCarrito_Cuando_SeProporcionaIdValido() {
    ItemCarrito item = new ItemCarrito(null, carritoBase, productoBase, 1, 1500.00);
    itemCarritoDao.insert(item);
    int id = item.getId();

    assertThat(itemCarritoDao.existsById(id)).isTrue();

    itemCarritoDao.delete(id);

    assertThat(itemCarritoDao.existsById(id)).isFalse();
    assertThat(itemCarritoDao.getById(id)).isNull();
  }

  @Test
  void existsById_DeberiaRetornarFalse_CuandoNoExiste() {
    assertThat(itemCarritoDao.existsById(999)).isFalse();
  }

  // =================================================================================
  // TESTS PARA GET ALL / GET BY CARRITO
  // =================================================================================

  @Test
  void getAll_DeberiaRetornarTodosLosItems() {
    itemCarritoDao.insert(new ItemCarrito(null, carritoBase, productoBase, 1, 1500.00));
    itemCarritoDao.insert(new ItemCarrito(null, carritoBase, productoSecundario, 3, 4500.00));

    List<ItemCarrito> todos = itemCarritoDao.getAll();

    assertThat(todos).hasSize(2);
  }

  @Test
  void getByCarritoId_DeberiaRetornarSoloLosItemsDeEseCarrito() {
    itemCarritoDao.insert(new ItemCarrito(null, carritoBase, productoBase, 2, 1500.00));
    itemCarritoDao.insert(new ItemCarrito(null, carritoBase, productoSecundario, 1, 4500.00));

    List<ItemCarrito> items = itemCarritoDao.getByCarritoId(1);

    assertThat(items).hasSize(2);
    assertThat(items).extracting(ItemCarrito::getCantidad).containsExactlyInAnyOrder(2, 1);
  }

  @Test
  void getByCarritoYProducto_DeberiaEncontrarElItemCorrespondiente() {
    itemCarritoDao.insert(new ItemCarrito(null, carritoBase, productoBase, 2, 1500.00));

    ItemCarrito resultado = itemCarritoDao.getByCarritoYProducto(1, 1);

    assertThat(resultado).isNotNull();
    assertThat(resultado.getCantidad()).isEqualTo(2);
  }

  @Test
  void getByCarritoYProducto_DeberiaRetornarNull_CuandoNoHayCoincidencia() {
    assertThat(itemCarritoDao.getByCarritoYProducto(1, 2)).isNull();
  }

  // =================================================================================
  // TESTS PARA MÉTODOS DE NEGOCIO
  // =================================================================================

  @Test
  void agregarOIncrementar_DeberiaCrearNuevoItem_CuandoNoExisteEnElCarrito() {
    itemCarritoDao.agregarOIncrementar(carritoBase, productoBase, 3);

    ItemCarrito item = itemCarritoDao.getByCarritoYProducto(1, 1);
    assertThat(item).isNotNull();
    assertThat(item.getCantidad()).isEqualTo(3);
  }

  @Test
  void agregarOIncrementar_DeberiaSumarCantidad_CuandoYaExisteEnElCarrito() {
    itemCarritoDao.insert(new ItemCarrito(null, carritoBase, productoBase, 2, 1500.00));

    itemCarritoDao.agregarOIncrementar(carritoBase, productoBase, 3);

    ItemCarrito item = itemCarritoDao.getByCarritoYProducto(1, 1);
    assertThat(item.getCantidad()).isEqualTo(5);
  }

  @Test
  void agregarOIncrementar_DeberiaLimitarAlStockDisponible() {
    itemCarritoDao.agregarOIncrementar(carritoBase, productoBase, 999);

    ItemCarrito item = itemCarritoDao.getByCarritoYProducto(1, 1);
    assertThat(item.getCantidad()).isEqualTo(10);
  }

  @Test
  void agregarOIncrementar_NoDeberiaHacerNada_CuandoProductoSinStock() {
    Producto sinStock = crearProducto(1, "Remera Classic", TipoProducto.REMERA, 1500.00, 0);

    itemCarritoDao.agregarOIncrementar(carritoBase, sinStock, 1);

    assertThat(itemCarritoDao.getByCarritoYProducto(1, 1)).isNull();
  }

  @Test
  void actualizarCantidad_ConStockDisponible_DeberiaLimitarLaCantidad() {
    ItemCarrito item = new ItemCarrito(null, carritoBase, productoBase, 2, 1500.00);
    itemCarritoDao.insert(item);

    itemCarritoDao.actualizarCantidad(item.getId(), 999, 10);

    ItemCarrito actualizado = itemCarritoDao.getById(item.getId());
    assertThat(actualizado.getCantidad()).isEqualTo(10);
  }

  @Test
  void actualizarCantidad_ConCantidadCero_DeberiaEliminarElItem() {
    ItemCarrito item = new ItemCarrito(null, carritoBase, productoBase, 2, 1500.00);
    itemCarritoDao.insert(item);

    itemCarritoDao.actualizarCantidad(item.getId(), 0);

    assertThat(itemCarritoDao.existsById(item.getId())).isFalse();
  }

  @Test
  void actualizarCantidad_SinLimiteDeStock_DeberiaActualizarDirecto() {
    ItemCarrito item = new ItemCarrito(null, carritoBase, productoBase, 1, 1500.00);
    itemCarritoDao.insert(item);

    itemCarritoDao.actualizarCantidad(item.getId(), 7);

    assertThat(itemCarritoDao.getById(item.getId()).getCantidad()).isEqualTo(7);
  }

  @Test
  void perteneceAlCarrito_DeberiaRetornarTrue_CuandoElItemEsDeEseCarrito() {
    ItemCarrito item = new ItemCarrito(null, carritoBase, productoBase, 1, 1500.00);
    itemCarritoDao.insert(item);

    assertThat(itemCarritoDao.perteneceAlCarrito(item.getId(), 1)).isTrue();
  }

  @Test
  void perteneceAlCarrito_DeberiaRetornarFalse_CuandoElItemEsDeOtroCarrito() {
    ItemCarrito item = new ItemCarrito(null, carritoBase, productoBase, 1, 1500.00);
    itemCarritoDao.insert(item);

    assertThat(itemCarritoDao.perteneceAlCarrito(item.getId(), 999)).isFalse();
  }

  @Test
  void vaciarCarrito_DeberiaEliminarTodosLosItemsDeEseCarrito() {
    itemCarritoDao.insert(new ItemCarrito(null, carritoBase, productoBase, 1, 1500.00));
    itemCarritoDao.insert(new ItemCarrito(null, carritoBase, productoSecundario, 2, 4500.00));

    itemCarritoDao.vaciarCarrito(1);

    assertThat(itemCarritoDao.getByCarritoId(1)).isEmpty();
  }

  // --- MÉTODO AUXILIAR ---
  private Producto crearProducto(int id, String nombre, TipoProducto tipo, double precio, int stock) {
    Producto p = new Producto();
    p.setId(id);
    p.setNombre(nombre);
    p.setDescripcion("Descripcion de " + nombre);
    p.setPrecio(precio);
    p.setImagen("img/test.jpg");
    p.setStock(stock);
    p.setTipo(tipo);
    return p;
  }
}