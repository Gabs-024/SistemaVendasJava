package org.gabsferreira.service.implementacao;

import lombok.RequiredArgsConstructor;
import org.gabsferreira.domain.entity.*;
import org.gabsferreira.domain.repository.Clientes;
import org.gabsferreira.domain.repository.ItensPedidos;
import org.gabsferreira.domain.repository.Pedidos;
import org.gabsferreira.domain.repository.Produtos;
import org.gabsferreira.exception.PedidoNaoEncontradoException;
import org.gabsferreira.exception.RegraNegocioException;
import org.gabsferreira.rest.DTO.ItemPedidoDTO;
import org.gabsferreira.rest.DTO.PedidoDTO;
import org.gabsferreira.service.PedidoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final Pedidos pedidos;
    private final Clientes clientes;
    private final Produtos produtos;
    private final ItensPedidos itensPedidosRepo;


    @Override
    @Transactional
    public Pedido salvar(PedidoDTO dto) {

        Integer idCliente = dto.getCliente();
        Cliente cliente = clientes.findById(idCliente)
                .orElseThrow(() -> new RegraNegocioException("Código de cliente inválido!"));

        Pedido pedido = new Pedido();
        pedido.setTotal(dto.getTotal());
        pedido.setDataPedido(LocalDate.now());
        pedido.setCliente(cliente);
        pedido.setStatus(StatusPedido.REALIZADO);

        List<ItemPedido> itensPedidos = converterItens(pedido, dto.getItens());
        pedidos.save(pedido);
        itensPedidosRepo.saveAll(itensPedidos);
        pedido.setItens(itensPedidos);
        return pedido;
    }

    @Override
    public Optional<Pedido> obterPedidoCompleto(Integer id) {
        return pedidos.findByIdFetchItens(id);
    }

    @Override
    @Transactional
    public void atualizaStatus(Integer id, StatusPedido statusPedido) {
        pedidos.findById(id).map(pedido -> {
            pedido.setStatus(statusPedido);
            return pedidos.save(pedido);
        }).orElseThrow(() -> new PedidoNaoEncontradoException());
    }

    private List<ItemPedido> converterItens (Pedido pedido, List<ItemPedidoDTO> itens) {
        if(itens.isEmpty()) {
            throw new RegraNegocioException("Seu carrinho está vazio!");
        }

        return itens.stream().map(dto -> {
            Integer idProduto = dto.getProduto();
            Produto produto = produtos.findById(idProduto).orElseThrow(
                    () -> new RegraNegocioException(
                            "Código de produto inválido: " + idProduto
                    ));

            ItemPedido itemPedido = new ItemPedido();
            itemPedido.setQuantidade(dto.getQuantidade());
            itemPedido.setPedido(pedido);
            itemPedido.setProduto(produto);
            return itemPedido;
        }).collect(Collectors.toList());
    }

}
