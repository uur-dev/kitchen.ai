package com.br3akPoint.ai_service.service;

import com.br3akPoint.ai_service.client.StorageClient;
import com.br3akPoint.ai_service.data.RecipeAIResponse;
import com.br3akPoint.ai_service.util.GeneratedMultiPartFile;
import com.google.genai.Client;
import com.google.genai.types.GenerateImagesConfig;
import com.google.genai.types.GenerateImagesResponse;
import com.google.genai.types.GeneratedImage;
import com.google.genai.types.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
public class RecipeAIService {

    private static final Logger log = LoggerFactory.getLogger(RecipeAIService.class);
    private static final Resource PROMPT = new ClassPathResource("prompts/recipe.st");

    private final ChatClient chatClient;
    private final Client genImageAIClient;
    private final StorageClient storageClient;
    private final BeanOutputConverter<RecipeAIResponse> converter;

    @Value("${gen.ai.imageModel}")
    private String imageModelType;

    public RecipeAIService(ChatClient.Builder builder, Client genImageAIClient, StorageClient storageClient) {
        this.chatClient = builder.defaultSystem(load()).build();
        this.genImageAIClient = genImageAIClient;
        this.storageClient = storageClient;
        this.converter = new BeanOutputConverter<>(RecipeAIResponse.class);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Generate a recipe from a free-text description.
     *
     * @param userText e.g. "a quick vegan pasta with spinach and garlic for 2"
     * @param cuisine  optional cuisine filter; pass null or blank to omit
     */
    public RecipeAIResponse getRecipeByText(String userText, String cuisine) {
        log.debug("Generating recipe from text: {}", userText);
        try {
            String rawJson = chatClient
                    .prompt()
                    .user(buildPrompt(userText, cuisine))
                    .call()
                    .content();
            return parseAndEnrich(rawJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate recipe from text", e);
        }
    }

    /**
     * Generate a recipe by analysing an image or audio file.
     * <p>
     * Supported images : image/jpeg, image/png, image/webp, image/gif
     * Supported audio  : audio/mpeg, audio/wav, audio/ogg, audio/mp4
     *
     * @param file    the uploaded MultipartFile
     * @param cuisine optional cuisine filter
     */
    public RecipeAIResponse getRecipeByImageOrAudio(MultipartFile file, String cuisine) {
        log.debug("Generating recipe from media: {} ({})", file.getOriginalFilename(), file.getContentType());
        try {
            String prompt = buildPrompt("Please consider the media file for best results", cuisine);
            String rawJson = chatClient
                    .prompt()
                    .user(spec -> spec
                            .text(prompt)
                            .media(MimeType.valueOf(requireContentType(file)), file.getResource()))
                    .call()
                    .content();
            return parseAndEnrich(rawJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate recipe from text", e);
        }
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    /** Build the prompt string, resolving all placeholders. */
    private String buildPrompt(String userInput, String cuisine) {
        return load()
                .replace("{{USER_INPUT}}", userInput)
                .replace("{{CUISINE}}", (cuisine != null && !cuisine.isBlank()) ? cuisine : "null")
                .replace("{{SCHEMA}}", converter.getFormat());
    }

    /** Parse the raw JSON response and optionally enrich it with a generated image. */
    private RecipeAIResponse parseAndEnrich(String rawJson) {
        log.info("Raw JSON from model: {}", rawJson);
        RecipeAIResponse response = converter.convert(sanitize(rawJson));
        if (Boolean.TRUE.equals(response.getStatus())) {
            String title = response.getTitle();
            String cuisine = response.getCuisine();
            List<String> ingredientsList = response.getIngredients().stream().map(RecipeAIResponse.Ingredient::getName).toList();
            String keyIngredients = String.join(", ", ingredientsList);
            generateImage(title, cuisine, keyIngredients).ifPresent(response::setImage);
        }
        return response;
    }

    /**
     * Generate a food photo for the given recipe title.
     *
     * @return Base64-encoded image string wrapped in Optional, or empty if generation fails.
     */
    private Optional<String> generateImage(String recipeTitle, String cuisine, String keyIngredients) {
        log.debug("Generating image for recipe: {}", recipeTitle);
        try {
            String imageGenPrompt = "Professional food photography of {title}, a {cuisine} dish with {key_ingredients}, overhead studio shot, soft diffused lighting, clean white surface, sharp focus";
            GenerateImagesResponse response = genImageAIClient.models.generateImages(
                    imageModelType,
                    imageGenPrompt
                            .replace("{title}", recipeTitle)
                            .replace("{cuisine}", cuisine)
                            .replace("{key_ingredients}", keyIngredients),
                    GenerateImagesConfig.builder()
                            .aspectRatio("3:4")
                            .numberOfImages(1).build()
            );

            return response.generatedImages()
                    .filter(images -> !images.isEmpty())
                    .map(images -> images.get(0))
                    .flatMap(GeneratedImage::image)
                    .flatMap(Image::imageBytes)
                    .flatMap(bytes -> {
                        log.debug("Image generated for: {}", recipeTitle);
                        return uploadAndGetUrl(recipeTitle, bytes);
                    });

        } catch (Exception e) {
            log.error("Image generation failed for '{}': {}", recipeTitle, e.getMessage());
            return Optional.empty();
        }
    }

    /** Read a classpath resource as a UTF-8 string. Fails fast at startup if missing. */
    private static String load() {
        try {
            return RecipeAIService.PROMPT.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load prompt file: " + RecipeAIService.PROMPT.getFilename(), e);
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
        if (ct == null || ct.isBlank())
            throw new IllegalArgumentException("Missing content-type for uploaded file.");
        return ct;
    }

    private Optional<String> uploadAndGetUrl(String recipeTitle, byte[] fileBytes) {
        try {
            var fileName = recipeTitle.trim().toLowerCase().replace(" ", "-") + ".png";
            var multiPartFile = new GeneratedMultiPartFile(fileBytes, fileName, "image/png");
            var response = storageClient.upload(multiPartFile);

            return response.getStatus() && response.hasData()
                    ? Optional.ofNullable(response.getData().get("url"))
                    : Optional.empty();

        } catch (Exception e) {
            log.error("Image Uploading failed for '{}': {}", recipeTitle, e.getMessage());
            return Optional.empty();
        }
    }
}