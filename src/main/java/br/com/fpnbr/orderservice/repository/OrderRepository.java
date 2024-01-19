package br.com.fpnbr.orderservice.repository;

import br.com.fpnbr.orderservice.domain.document.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
}
