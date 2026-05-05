package ap1.jennifer.cubillas.repository;

import ap1.jennifer.cubillas.model.ChatRespuesta;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ChatRespuestaRepository extends ReactiveMongoRepository<ChatRespuesta, String> {
    Flux<ChatRespuesta> findByEliminadoFalse();
}
