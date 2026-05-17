package com.br3akPoint.ai_service.service;

import com.br3akPoint.ai_service.data.RecipeAIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Generates structured {@link RecipeAIResponse} POJOs from text, image, or audio input.
 *
 * Prompts are loaded from classpath:prompts/*.st — edit them without touching Java code.
 */
@Service
public class RecipeAIService {

    private static final Logger log = LoggerFactory.getLogger(RecipeAIService.class);

    // Prompt files live under src/main/resources/prompts/
    private static final Resource PROMPT   = new ClassPathResource("prompts/recipe.st");

    private final ChatClient chatClient;
    private final BeanOutputConverter<RecipeAIResponse> converter;

    public RecipeAIService(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem(load(PROMPT))
                .build();
        this.converter = new BeanOutputConverter<>(RecipeAIResponse.class);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Generate a recipe from a free-text description.
     *
     * @param userText  e.g. "a quick vegan pasta with spinach and garlic for 2"
     */
    public RecipeAIResponse getRecipeByText(String userText, String cuisine) {
        try {
            log.debug("Generating recipe from text: {}", userText);

            String prompt = load(PROMPT)
                    .replace("{{USER_INPUT}}", userText)
                    .replace("{{CUISINE}}", (cuisine != null && !cuisine.isEmpty()) ? cuisine : "null")
                    .replace("{{SCHEMA}}",   converter.getFormat());

            String rawJson = chatClient.prompt().user(prompt).call().content();

            log.info("Generated Recipe Raw JSON {}", rawJson);

            return converter.convert(sanitize(rawJson));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generate a recipe by analysing an image (photo of a dish or ingredients)
     * or an audio file (voice description of what to cook).
     *
     * Supported images : image/jpeg, image/png, image/webp, image/gif
     * Supported audio  : audio/mpeg, audio/wav, audio/ogg, audio/mp4
     *
     * @param file  the uploaded MultipartFile
     */
    public RecipeAIResponse getRecipeByImageOrAudio(MultipartFile file, String cuisine) throws IOException {
        log.debug("Generating recipe from media: {} ({})", file.getOriginalFilename(), file.getContentType());

        String prompt = load(PROMPT)
                .replace("{{USER_INPUT}}", "Please Consider Media file for best result")
                .replace("{{CUISINE}}", (cuisine != null && !cuisine.isEmpty()) ? cuisine : "null")
                .replace("{{SCHEMA}}",   converter.getFormat());

        String rawJson = chatClient
                .prompt()
                .user(spec -> spec
                        .text(prompt)
                        .media(MimeType.valueOf(requireContentType(file)), file.getResource()))
                .call()
                .content();

        log.info("Generated Recipe Raw JSON {}", rawJson);

        return converter.convert(sanitize(rawJson));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Read a classpath resource as a UTF-8 string. Fails fast at startup if missing. */
    private static String load(Resource resource) {
        try {
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load prompt file: " + resource.getFilename(), e);
        }
    }

    /** Strip accidental ```json … ``` fences that models sometimes add. */
    private static String sanitize(String raw) {
        if (raw == null) return "{}";
        String s = raw.strip();
        if (s.startsWith("```")) {
            s = s.replaceFirst("^```[a-zA-Z]*\\n?", "");
            if (s.endsWith("```")) s = s.substring(0, s.lastIndexOf("```")).stripTrailing();
        }
        return s;
    }

    /** Validate and return the content-type; throws if null/blank. */
    private static String requireContentType(MultipartFile file) {
        String ct = file.getContentType();
        if (ct == null || ct.isBlank()) throw new IllegalArgumentException("Missing content-type for uploaded file.");
        return ct;
    }
}