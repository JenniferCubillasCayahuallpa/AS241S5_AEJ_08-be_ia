package ap1.jennifer.cubillas.service;

import ap1.jennifer.cubillas.model.Traduccion;
import ap1.jennifer.cubillas.repository.TraduccionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class TraduccionService {

    private final WebClient webClient;
    private final TraduccionRepository repository;

    @Value("${rapidapi.key}")
    private String rapidApiKey;

    @Value("${rapidapi.deep-translate.url}")
    private String apiUrl;

    @Value("${rapidapi.deep-translate.host}")
    private String apiHost;

    public TraduccionService(WebClient.Builder builder, TraduccionRepository repository) {
        this.webClient = builder.build();
        this.repository = repository;
    }

    // CREATE — llama a la API y guarda
    public Mono<Traduccion> traducir(String texto, String origen, String destino) {
        Map<String, String> body = new HashMap<>();
        body.put("q", texto);
        body.put("source", origen);
        body.put("target", destino);

        return webClient.post()
                .uri(apiUrl)
                .header("Content-Type", "application/json")
                .header("x-rapidapi-host", apiHost)
                .header("x-rapidapi-key", rapidApiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    Map<?, ?> data = (Map<?, ?>) response.get("data");
                    Map<?, ?> translations = (Map<?, ?>) data.get("translations");
                    Object raw = translations.get("translatedText");
                    String textoTraducido = (raw instanceof java.util.List)
                            ? ((java.util.List<?>) raw).get(0).toString()
                            : raw.toString();

                    Traduccion t = new Traduccion();
                    t.setTextoOriginal(texto);
                    t.setIdiomaOrigen(origen);
                    t.setIdiomaDestino(destino);
                    t.setTextoTraducido(textoTraducido);
                    t.setFechaConsulta(LocalDateTime.now());
                    return t;
                })
                .flatMap(repository::save);
    }

    // READ — lista solo los no eliminados
    public Flux<Traduccion> listarTodas() {
        return repository.findByEliminadoFalse();
    }

    // READ por ID
    public Mono<Traduccion> obtenerPorId(String id) {
        return repository.findById(id)
                .filter(t -> !t.isEliminado());
    }

    // UPDATE — re-traduce con nuevos datos y actualiza
    public Mono<Traduccion> actualizar(String id, String texto, String origen, String destino) {
        return repository.findById(id)
                .filter(t -> !t.isEliminado())
                .flatMap(existente -> {
                    Map<String, String> body = new HashMap<>();
                    body.put("q", texto);
                    body.put("source", origen);
                    body.put("target", destino);

                    return webClient.post()
                            .uri(apiUrl)
                            .header("Content-Type", "application/json")
                            .header("x-rapidapi-host", apiHost)
                            .header("x-rapidapi-key", rapidApiKey)
                            .bodyValue(body)
                            .retrieve()
                            .bodyToMono(Map.class)
                            .map(response -> {
                                Map<?, ?> data = (Map<?, ?>) response.get("data");
                                Map<?, ?> translations = (Map<?, ?>) data.get("translations");
                                Object raw = translations.get("translatedText");
                                String textoTraducido = (raw instanceof java.util.List)
                                        ? ((java.util.List<?>) raw).get(0).toString()
                                        : raw.toString();

                                existente.setTextoOriginal(texto);
                                existente.setIdiomaOrigen(origen);
                                existente.setIdiomaDestino(destino);
                                existente.setTextoTraducido(textoTraducido);
                                existente.setFechaConsulta(LocalDateTime.now());
                                return existente;
                            })
                            .flatMap(repository::save);
                });
    }

    // DELETE lógico
    public Mono<Traduccion> eliminar(String id) {
        return repository.findById(id)
                .flatMap(t -> {
                    t.setEliminado(true);
                    return repository.save(t);
                });
    }
}
