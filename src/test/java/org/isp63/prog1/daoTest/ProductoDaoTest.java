package org.isp63.prog1.daoTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.isp63.prog1.dao.ProductoDao;
import org.isp63.prog1.entities.Producto;
import org.isp63.prog1.enums.TipoProducto;
import org.isp63.prog1.interfaces.AdminConexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class ProductoDaoTest {

  @Container
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("tienda")
      .withUsername("test")
      .withPassword("test");

  private ProductoDao productoDao;

  @BeforeAll
  static void setupDatabase() throws Exception {
    System.setProperty("db.url", mysql.getJdbcUrl());
    System.setProperty("db.user", mysql.getUsername());
    System.setProperty("db.password", mysql.getPassword());

    AdminConexion.INSTANCE.recargarPoolParaTests();

    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {

      st.execute("CREATE TABLE producto (" +
          "id INT AUTO_INCREMENT PRIMARY KEY, " +
          "nombre VARCHAR(100) NOT NULL, " +
          "descripcion TEXT, " +
          "precio DECIMAL(10,2) NOT NULL, " +
          "imagen VARCHAR(500), " +
          "stock INT NOT NULL DEFAULT 0, " +
          "tipo ENUM('REMERA','CAMPERA','BUZO','PANTALON','JEAN','SHORT','ZAPATILLAS','ZAPATOS','BOTAS','SANDALIAS','GORRA','ACCESORIOS') NOT NULL)");
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    productoDao = new ProductoDao();

    try (Connection conn = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
         Statement st = conn.createStatement()) {

      st.execute("SET FOREIGN_KEY_CHECKS = 0");
      st.execute("TRUNCATE TABLE producto");
      st.execute("SET FOREIGN_KEY_CHECKS = 1");
    }
  }

  @Test
  void deberia_InsertarYObtenerProducto_Cuando_DatosSonValidos() {
    // --- 1. ARRANGE ---
    Producto nuevo = crearProducto("Remera Oversize", TipoProducto.REMERA, 12000.00, 15);

    // --- 2. ACT ---
    productoDao.insert(nuevo);
    Integer idGenerado = nuevo.getId();

    Producto recuperado = productoDao.getById(idGenerado);

    // --- 3. ASSERT ---
    assertThat(idGenerado).isGreaterThan(0);
    assertThat(productoDao.existsById(idGenerado)).isTrue();
    assertThat(recuperado).isNotNull();
    assertThat(recuperado.getNombre()).isEqualTo("Remera Oversize");
    assertThat(recuperado.getTipo()).isEqualTo(TipoProducto.REMERA);
  }

  @Test
  void deberia_FiltrarPorTipo_UsandoStreams() {
    // --- 1. ARRANGE ---
    productoDao.insert(crearProducto("Remera Negra", TipoProducto.REMERA, 10000.00, 5));
    productoDao.insert(crearProducto("Remera Blanca", TipoProducto.REMERA, 10000.00, 3));
    productoDao.insert(crearProducto("Jean Slim", TipoProducto.JEAN, 25000.00, 8));

    // --- 2. ACT ---
    List<Producto> remeras = productoDao.getByTipo(TipoProducto.REMERA);

    // --- 3. ASSERT ---
    assertThat(remeras).hasSize(2);
    assertThat(remeras)
        .extracting(Producto::getNombre)
        .containsExactlyInAnyOrder("Remera Negra", "Remera Blanca");
  }

  @Test
  void deberia_BuscarPorNombre_UsandoStreams() {
    // --- 1. ARRANGE ---
    productoDao.insert(crearProducto("Campera de Cuero", TipoProducto.CAMPERA, 50000.00, 2));
    productoDao.insert(crearProducto("Buzo Cuello Redondo", TipoProducto.BUZO, 20000.00, 4));

    // --- 2. ACT ---
    List<Producto> resultados = productoDao.buscarPorNombre("cuero");

    // --- 3. ASSERT ---
    assertThat(resultados).hasSize(1);
    assertThat(resultados.get(0).getNombre()).isEqualTo("Campera de Cuero");
  }

  @Test
  void deberia_ObtenerProductoMasCaro_UsandoStreams() {
    // --- 1. ARRANGE ---
    productoDao.insert(crearProducto("Gorra", TipoProducto.GORRA, 5000.00, 10));
    productoDao.insert(crearProducto("Zapatillas Running", TipoProducto.ZAPATILLAS, 45000.00, 5));
    productoDao.insert(crearProducto("Pantalon Cargo", TipoProducto.PANTALON, 18000.00, 7));

    // --- 2. ACT ---
    Optional<Producto> masCaro = productoDao.getProductoMasCaro();

    // --- 3. ASSERT ---
    assertThat(masCaro).isPresent();
    assertThat(masCaro.get().getNombre()).isEqualTo("Zapatillas Running");
    assertThat(masCaro.get().getPrecio()).isEqualTo(45000.00);
  }

  @Test
  void deberia_ObtenerProductosBajoStock_UsandoStreams() {
    // --- 1. ARRANGE ---
    productoDao.insert(crearProducto("Producto A", TipoProducto.ACCESORIOS, 2000.00, 1));
    productoDao.insert(crearProducto("Producto B", TipoProducto.ACCESORIOS, 2000.00, 10));

    // --- 2. ACT ---
    List<Producto> bajoStock = productoDao.getProductosBajoStock(3);

    // --- 3. ASSERT ---
    assertThat(bajoStock).hasSize(1);
    assertThat(bajoStock.get(0).getNombre()).isEqualTo("Producto A");
  }

  // --- MÉTODO AUXILIAR ---
  private Producto crearProducto(String nombre, TipoProducto tipo, double precio, int stock) {
    Producto p = new Producto();
    p.setNombre(nombre);
    p.setDescripcion("Descripcion de " + nombre);
    p.setPrecio(precio);
    p.setImagen("img/test.jpg");
    p.setStock(stock);
    p.setTipo(tipo);
    return p;
  }
}