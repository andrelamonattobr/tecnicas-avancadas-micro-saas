package com.maas.pedidos_conectado.repository;

import com.maas.pedidos_conectado.domain.ListaPedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ListaPedidoRepository extends JpaRepository<ListaPedido, String> {
    List<ListaPedido> findByEmpresaId(String empresaId);
}
