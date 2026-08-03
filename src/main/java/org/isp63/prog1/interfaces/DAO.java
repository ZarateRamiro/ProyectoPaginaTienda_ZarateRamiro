package org.isp63.prog1.interfaces;

import java.util.List;

public interface DAO<O, K> {
  List<O> getAll();
  void insert(O objeto);
  void update(O objeto);
  void delete(K id);
  O getById(K id);
  boolean existsById(K id);
}
