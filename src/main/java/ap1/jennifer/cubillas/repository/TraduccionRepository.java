package ap1.jennifer.cubillas.repository;

import ap1.jennifer.cubillas.model.Traduccion;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface TraduccionRepository extends ReactiveMongoRepository<Traduccion, String> {
    Flux<Traduccion> findByEliminadoFalse();
}
