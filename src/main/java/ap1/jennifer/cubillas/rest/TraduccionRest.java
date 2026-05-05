package ap1.jennifer.cubillas.rest;

import ap1.jennifer.cubillas.model.Traduccion;
import ap1.jennifer.cubillas.service.TraduccionService;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/traduccion")
@CrossOrigin(origins = "*")
public class TraduccionRest {

    private final TraduccionService service;

    public TraduccionRest(TraduccionService service) {
        this.service = service;
    }

    // CREATE
    @PostMapping
    public Mono<Traduccion> traducir(@RequestBody TraduccionRequest request) {
        return service.traducir(request.texto(), request.origen(), request.destino());
    }

    // READ ALL
    @GetMapping
    public Flux<Traduccion> listar() {
        return service.listarTodas();
    }

    // READ BY ID
    @GetMapping("/{id}")
    public Mono<Traduccion> obtener(@PathVariable String id) {
        return service.obtenerPorId(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public Mono<Traduccion> actualizar(@PathVariable String id, @RequestBody TraduccionRequest request) {
        return service.actualizar(id, request.texto(), request.origen(), request.destino());
    }

    // DELETE lógico
    @DeleteMapping("/{id}")
    public Mono<Traduccion> eliminar(@PathVariable String id) {
        return service.eliminar(id);
    }

    public record TraduccionRequest(
            @JsonProperty("texto") String texto,
            @JsonProperty("origen") String origen,
            @JsonProperty("destino") String destino) {}
}
