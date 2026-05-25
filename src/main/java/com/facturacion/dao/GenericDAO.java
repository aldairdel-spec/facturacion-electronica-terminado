package com.facturacion.dao;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz genérica DAO con operaciones CRUD básicas.
 * @param <T>  Tipo de entidad
 * @param <ID> Tipo del identificador
 */
public interface GenericDAO<T, ID> {

    /** Guarda una entidad nueva y retorna el ID generado */
    int guardar(T entidad);

    /** Actualiza una entidad existente */
    boolean actualizar(T entidad);

    /** Elimina lógicamente o físicamente una entidad */
    boolean eliminar(ID id);

    /** Busca por ID */
    Optional<T> buscarPorId(ID id);

    /** Retorna todos los registros */
    List<T> listarTodos();
}
