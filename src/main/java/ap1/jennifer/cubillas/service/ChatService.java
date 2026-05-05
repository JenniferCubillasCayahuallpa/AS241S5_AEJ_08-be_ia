package ap1.jennifer.cubillas.service;

import ap1.jennifer.cubillas.model.ChatRespuesta;
import ap1.jennifer.cubillas.repository.ChatRespuestaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private final WebClient webClient;
    private final ChatRespuestaRepository repository;

    @Value("${rapidapi.key}")
    private String rapidApiKey;

    @Value("${rapidapi.openai21.url}")
    private String apiUrl;

    @Value("${rapidapi.openai21.host}")
    private String apiHost;

    public ChatService(WebClient.Builder builder, ChatRespuestaRepository repository) {
        this.webClient = builder.build();
        this.repository = repository;
    }

    // CREATE — llama a la API y guarda
    public Mono<ChatRespuesta> preguntar(String pregunta) {
        List<Map<String, String>> body = List.of(
                Map.of("role", "system", "content", "I'm an AI assistant bot based on ChatGPT 3."),
                Map.of("role", "user", "content", pregunta)
        );

        return webClient.post()
                .uri(apiUrl)
                .header("Content-Type", "application/json")
                .header("x-rapidapi-host", apiHost)
                .header("x-rapidapi-key", rapidApiKey)
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .map(errorBody -> new RuntimeException(
                                        "Error de API [" + clientResponse.statusCode() + "]: " + errorBody)))
                .bodyToMono(Map.class)
                .map(response -> {
                    Object choicesRaw = response.get("choices");
                    String respuestaTexto;
                    if (choicesRaw instanceof List<?> choices && !choices.isEmpty()) {
                        Map<?, ?> first = (Map<?, ?>) choices.get(0);
                        Map<?, ?> message = (Map<?, ?>) first.get("message");
                        respuestaTexto = message != null ? message.get("content").toString() : "Sin respuesta";
                    } else {
                        respuestaTexto = response.toString();
                    }

                    ChatRespuesta chat = new ChatRespuesta();
                    chat.setPregunta(pregunta);
                    chat.setRespuesta(respuestaTexto);
                    chat.setFechaConsulta(LocalDateTime.now());
                    return chat;
                })
                .flatMap(repository::save);
    }

    // READ — lista solo los no eliminados
    public Flux<ChatRespuesta> listarTodas() {
        return repository.findByEliminadoFalse();
    }

    // READ por ID
    public Mono<ChatRespuesta> obtenerPorId(String id) {
        return repository.findById(id)
                .filter(c -> !c.isEliminado());
    }

    // UPDATE — re-pregunta a la API con nueva pregunta y actualiza
    public Mono<ChatRespuesta> actualizar(String id, String nuevaPregunta) {
        return repository.findById(id)
                .filter(c -> !c.isEliminado())
                .flatMap(existente -> {
                    List<Map<String, String>> body = List.of(
                            Map.of("role", "system", "content", "I'm an AI assistant bot based on ChatGPT 3."),
                            Map.of("role", "user", "content", nuevaPregunta)
                    );

                    return webClient.post()
                            .uri(apiUrl)
                            .header("Content-Type", "application/json")
                            .header("x-rapidapi-host", apiHost)
                            .header("x-rapidapi-key", rapidApiKey)
                            .bodyValue(body)
                            .retrieve()
                            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                                    clientResponse -> clientResponse.bodyToMono(String.class)
                                            .map(errorBody -> new RuntimeException(
                                                    "Error de API [" + clientResponse.statusCode() + "]: " + errorBody)))
                            .bodyToMono(Map.class)
                            .map(response -> {
                                Object choicesRaw = response.get("choices");
                                String respuestaTexto;
                                if (choicesRaw instanceof List<?> choices && !choices.isEmpty()) {
                                    Map<?, ?> first = (Map<?, ?>) choices.get(0);
                                    Map<?, ?> message = (Map<?, ?>) first.get("message");
                                    respuestaTexto = message != null ? message.get("content").toString() : "Sin respuesta";
                                } else {
                                    respuestaTexto = response.toString();
                                }

                                existente.setPregunta(nuevaPregunta);
                                existente.setRespuesta(respuestaTexto);
                                existente.setFechaConsulta(LocalDateTime.now());
                                return existente;
                            })
                            .flatMap(repository::save);
                });
    }

    // DELETE lógico
    public Mono<ChatRespuesta> eliminar(String id) {
        return repository.findById(id)
                .flatMap(c -> {
                    c.setEliminado(true);
                    return repository.save(c);
                });
    }
}
