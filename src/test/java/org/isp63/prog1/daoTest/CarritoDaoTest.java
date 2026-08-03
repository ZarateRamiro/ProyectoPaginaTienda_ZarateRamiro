package org.isp63.prog1.daoTest;

import org.isp63.prog1.dao.CarritoDao;
import org.isp63.prog1.entities.Carrito;
import org.isp63.prog1.entities.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoDaoTest {

  @Mock private Connection mockConnection;
  @Mock private PreparedStatement mockPreparedStatement;
  @Mock private ResultSet mockResultSet;

  private CarritoDao carritoDao;

  @BeforeEach
  void setUp() throws SQLException {
    // Usamos spy e interceptamos la conexión para que no se conecte a la BD real
    carritoDao = spy(new CarritoDao());
    lenient().doReturn(mockConnection).when(carritoDao).obtenerConexion();
  }

  // =================================================================================
  // TESTS PARA INSERT
  // =================================================================================

  @Test
  void insert_DeberiaInsertarCarritoYSetearIdGenerado() throws SQLException {
    // ARRANGE
    when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
        .thenReturn(mockPreparedStatement);
    when(mockPreparedStatement.executeUpdate()).thenReturn(1);
    when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(true);
    when(mockResultSet.getInt(1)).thenReturn(42);

    Usuario usuario = new Usuario();
    usuario.setId(10);

    Carrito nuevoCarrito = new Carrito(null, usuario, LocalDate.of(2026, 8, 3), "ACTIVO");

    // ACT
    carritoDao.insert(nuevoCarrito);

    // ASSERT
    verify(mockPreparedStatement).setInt(1, 10);
    verify(mockPreparedStatement).setDate(2, Date.valueOf(LocalDate.of(2026, 8, 3)));
    verify(mockPreparedStatement).setString(3, "ACTIVO");

    assertThat(nuevoCarrito.getId()).isEqualTo(42);
  }

  @Test
  void insert_DeberiaLanzarRuntimeException_SiFallaSQL() throws SQLException {
    // ARRANGE
    when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
        .thenThrow(new SQLException("Conexión perdida"));

    Usuario u = new Usuario();
    u.setId(1);
    Carrito carrito = new Carrito(null, u, LocalDate.now(), "ACTIVO");

    // ACT & ASSERT
    RuntimeException excepcion = assertThrows(RuntimeException.class, () -> carritoDao.insert(carrito));
    assertThat(excepcion.getMessage()).contains("Error al insertar carrito");
  }

  // =================================================================================
  // TESTS PARA GET BY ID
  // =================================================================================

  @Test
  void getById_DeberiaRetornarCarritoMapeado_CuandoExiste() throws SQLException {
    // ARRANGE
    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

    when(mockResultSet.next()).thenReturn(true);
    when(mockResultSet.getInt("id")).thenReturn(5);
    when(mockResultSet.getDate("fecha_creacion")).thenReturn(Date.valueOf(LocalDate.of(2026, 8, 1)));
    when(mockResultSet.getString("estado")).thenReturn("ACTIVO");

    when(mockResultSet.getInt("usuario_id")).thenReturn(10);
    when(mockResultSet.getString("usuario_nombre")).thenReturn("Juan Perez");
    when(mockResultSet.getString("usuario_email")).thenReturn("juan@test.com");
    when(mockResultSet.getString("usuario_password")).thenReturn("123456");
    when(mockResultSet.getString("usuario_rol")).thenReturn("CLIENTE");

    // ACT
    Carrito resultado = carritoDao.getById(5);

    // ASSERT
    assertThat(resultado).isNotNull();
    assertThat(resultado.getId()).isEqualTo(5);
    assertThat(resultado.getEstado()).isEqualTo("ACTIVO");
    assertThat(resultado.getUsuario().getId()).isEqualTo(10);
    assertThat(resultado.getUsuario().getNombre()).isEqualTo("Juan Perez");
  }

  @Test
  void getById_DeberiaRetornarNull_CuandoNoExiste() throws SQLException {
    // ARRANGE
    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(false);

    // ACT
    Carrito resultado = carritoDao.getById(99);

    // ASSERT
    assertThat(resultado).isNull();
  }

  // =================================================================================
  // TESTS PARA UPDATE
  // =================================================================================

  @Test
  void update_DeberiaActualizarCorrectamente() throws SQLException {
    // ARRANGE
    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    when(mockPreparedStatement.executeUpdate()).thenReturn(1);

    Usuario u = new Usuario();
    u.setId(10);
    Carrito carrito = new Carrito(1, u, LocalDate.of(2026, 8, 3), "FINALIZADO");

    // ACT
    carritoDao.update(carrito);

    // ASSERT
    verify(mockPreparedStatement).setInt(1, 10);
    verify(mockPreparedStatement).setDate(2, Date.valueOf(LocalDate.of(2026, 8, 3)));
    verify(mockPreparedStatement).setString(3, "FINALIZADO");
    verify(mockPreparedStatement).setInt(4, 1);
    verify(mockPreparedStatement).executeUpdate();
  }

  @Test
  void update_DeberiaLanzarRuntimeException_SiFallaSQL() throws SQLException {
    when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Error en la BD"));

    Usuario u = new Usuario();
    u.setId(1);
    Carrito carrito = new Carrito(1, u, LocalDate.now(), "ACTIVO");

    RuntimeException excepcion = assertThrows(RuntimeException.class, () -> carritoDao.update(carrito));
    assertThat(excepcion.getMessage()).contains("Error al actualizar carrito");
  }

  // =================================================================================
  // TESTS PARA DELETE
  // =================================================================================

  @Test
  void delete_DeberiaEliminarCorrectamente() throws SQLException {
    // ARRANGE
    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    when(mockPreparedStatement.executeUpdate()).thenReturn(1);

    // ACT
    carritoDao.delete(1);

    // ASSERT
    verify(mockPreparedStatement).setInt(1, 1);
    verify(mockPreparedStatement).executeUpdate();
  }

  @Test
  void delete_DeberiaLanzarRuntimeException_SiFallaSQL() throws SQLException {
    when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Error al borrar"));

    RuntimeException excepcion = assertThrows(RuntimeException.class, () -> carritoDao.delete(99));
    assertThat(excepcion.getMessage()).contains("Error al eliminar carrito");
  }

  // =================================================================================
  // TESTS PARA GET ALL
  // =================================================================================

  @Test
  void getAll_DeberiaRetornarListaDeCarritos() throws SQLException {
    // ARRANGE
    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

    when(mockResultSet.next()).thenReturn(true, false);
    when(mockResultSet.getInt("id")).thenReturn(1);
    when(mockResultSet.getDate("fecha_creacion")).thenReturn(Date.valueOf(LocalDate.now()));
    when(mockResultSet.getString("estado")).thenReturn("ACTIVO");
    when(mockResultSet.getInt("usuario_id")).thenReturn(2);
    when(mockResultSet.getString("usuario_nombre")).thenReturn("Maria");

    // ACT
    List<Carrito> resultado = carritoDao.getAll();

    // ASSERT
    assertThat(resultado).hasSize(1);
    assertThat(resultado.get(0).getId()).isEqualTo(1);
  }

  @Test
  void getAll_DeberiaLanzarRuntimeException_AnteFallaDeBD() throws SQLException {
    when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("BD offline"));

    assertThrows(RuntimeException.class, () -> carritoDao.getAll());
  }

  // =================================================================================
  // TESTS PARA MÉTODOS ESPECÍFICOS DE CARRITO
  // =================================================================================

  @Test
  void getActivoByUsuarioId_DeberiaRetornarCarritoActivo_CuandoExiste() throws SQLException {
    // ARRANGE
    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

    when(mockResultSet.next()).thenReturn(true);
    when(mockResultSet.getInt("id")).thenReturn(7);
    when(mockResultSet.getDate("fecha_creacion")).thenReturn(Date.valueOf(LocalDate.now()));
    when(mockResultSet.getString("estado")).thenReturn("ACTIVO");
    when(mockResultSet.getInt("usuario_id")).thenReturn(15);

    // ACT
    Carrito resultado = carritoDao.getActivoByUsuarioId(15);

    // ASSERT
    assertThat(resultado).isNotNull();
    assertThat(resultado.getId()).isEqualTo(7);
    assertThat(resultado.getEstado()).isEqualTo("ACTIVO");
    verify(mockPreparedStatement).setInt(1, 15);
  }

  @Test
  void obtenerOCrearActivo_DeberiaRetornarExistente_SiYaExisteUnoActivo() throws SQLException {
    // ARRANGE
    Usuario u = new Usuario();
    u.setId(10);

    Carrito carritoExistente = new Carrito(100, u, LocalDate.now(), "ACTIVO");

    // Mockeamos la llamada interna a getActivoByUsuarioId
    doReturn(carritoExistente).when(carritoDao).getActivoByUsuarioId(10);

    // ACT
    Carrito resultado = carritoDao.obtenerOCrearActivo(u);

    // ASSERT
    assertThat(resultado).isEqualTo(carritoExistente);
    verify(carritoDao, never()).insert(any(Carrito.class));
  }

  @Test
  void obtenerOCrearActivo_DeberiaInsertarYNuevo_SiNoExisteActivo() throws SQLException {
    // ARRANGE
    Usuario u = new Usuario();
    u.setId(10);

    // Simulamos que no existe ningún carrito activo
    doReturn(null).when(carritoDao).getActivoByUsuarioId(10);
    // Evitamos ejecuciones reales de insert
    doNothing().when(carritoDao).insert(any(Carrito.class));

    // ACT
    Carrito resultado = carritoDao.obtenerOCrearActivo(u);

    // ASSERT
    assertThat(resultado).isNotNull();
    assertThat(resultado.getEstado()).isEqualTo("ACTIVO");
    assertThat(resultado.getUsuario().getId()).isEqualTo(10);
    verify(carritoDao).insert(any(Carrito.class));
  }

  @Test
  void marcarFinalizado_DeberiaActualizarCorrectamente_CuandoEncuentraFilas() throws SQLException {
    // ARRANGE
    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    when(mockPreparedStatement.executeUpdate()).thenReturn(1); // 1 fila afectada

    // ACT & ASSERT (no lanza excepción)
    carritoDao.marcarFinalizado(mockConnection, 5);

    verify(mockPreparedStatement).setInt(1, 5);
    verify(mockPreparedStatement).executeUpdate();
  }

  @Test
  void marcarFinalizado_DeberiaLanzarSQLException_CuandoNoAfectaFilas() throws SQLException {
    // ARRANGE
    when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    when(mockPreparedStatement.executeUpdate()).thenReturn(0); // 0 filas (no existe o no estaba ACTIVO)

    // ACT & ASSERT
    SQLException excepcion = assertThrows(SQLException.class, () -> carritoDao.marcarFinalizado(mockConnection, 999));
    assertThat(excepcion.getMessage()).contains("No se pudo finalizar el carrito id 999");
  }

  @Test
  void existsById_DeberiaRetornarTrue_SiExiste() throws SQLException {
    // ARRANGE
    Carrito c = new Carrito();
    c.setId(10);
    doReturn(c).when(carritoDao).getById(10);

    // ACT
    boolean existe = carritoDao.existsById(10);

    // ASSERT
    assertThat(existe).isTrue();
  }

  @Test
  void existsById_DeberiaRetornarFalse_SiNoExiste() throws SQLException {
    // ARRANGE
    doReturn(null).when(carritoDao).getById(99);

    // ACT
    boolean existe = carritoDao.existsById(99);

    // ASSERT
    assertThat(existe).isFalse();
  }
}