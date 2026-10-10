package com.maas.pedidos_conectado.repository;

import com.maas.pedidos_conectado.domain.ItemPedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, String> {
    List<ItemPedido> findByListaId(String listaId);
    void deleteByListaId(String listaId);
}
