package br.com.fpnbr.orderservice.repository;

import br.com.fpnbr.orderservice.domain.document.Event;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {
    List<Event> findAllByOrderByCreatedAtDesc();

    Optional<Event> findFirstByOrderIdOrderByCreatedAtDesc(String orderId);

    Optional<Event> findFirstByTransactionIdOrderByCreatedAtDesc(String transactionId);
}
