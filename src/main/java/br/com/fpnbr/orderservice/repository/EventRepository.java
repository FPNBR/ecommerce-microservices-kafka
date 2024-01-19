package br.com.fpnbr.orderservice.repository;

import br.com.fpnbr.orderservice.domain.document.Event;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {
}
