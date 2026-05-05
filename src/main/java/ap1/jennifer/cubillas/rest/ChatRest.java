package ap1.jennifer.cubillas.rest;

import ap1.jennifer.cubillas.model.ChatRespuesta;
import ap1.jennifer.cubillas.service.ChatService;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatRest {

    private final ChatService service;

    public ChatRest(ChatService service) {
        this.service = service;
    }

    // CREATE
    @PostMapping
    public Mono<ChatRespuesta> preguntar(@RequestBody ChatRequest request) {
        return service.preguntar(request.pregunta());
    }

    // READ ALL
    @GetMapping
    public Flux<ChatRespuesta> listar() {
        return service.listarTodas();
    }

    // READ BY ID
    @GetMapping("/{id}")
    public Mono<ChatRespuesta> obtener(@PathVariable String id) {
        return service.obtenerPorId(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public Mono<ChatRespuesta> actualizar(@PathVariable String id, @RequestBody ChatRequest request) {
        return service.actualizar(id, request.pregunta());
    }

    // DELETE lógico
    @DeleteMapping("/{id}")
    public Mono<ChatRespuesta> eliminar(@PathVariable String id) {
        return service.eliminar(id);
    }

    public record ChatRequest(@JsonProperty("pregunta") String pregunta) {}
}
