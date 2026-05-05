package constant;

public class RecipeMessageBrokerKeys {

    // ─── Main Exchange & Queue ────────────────────────────────
    public static final String RECIPE_EXCHANGE_NAME = "recipe.exchange";
    public static final String RECIPE_QUEUE_NAME = "recipe.ai.queue";
    public static final String RECIPE_ROUTING_KEY = "recipe.request.created";

    // ─── Dead Letter Exchange & Queue ─────────────────────────
    public static final String DLX_EXCHANGE = "recipe.dlx.exchange";
    public static final String DLX_QUEUE = "recipe.dlx.queue";
    public static final String DLX_ROUTING_KEY = "recipe.failed";
}