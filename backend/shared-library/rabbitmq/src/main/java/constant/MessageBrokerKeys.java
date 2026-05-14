package constant;

public class MessageBrokerKeys {

    // ─── Main Exchange & Queue ────────────────────────────────
    public static final String RECIPE_EXCHANGE_NAME = "recipe.exchange";
    public static final String RECIPE_QUEUE_NAME = "recipe.ai.queue";
    public static final String RECIPE_ROUTING_KEY = "recipe.request.created";

    // ─── Dead Letter Exchange & Queue ─────────────────────────
    public static final String RECIPE_DLX_EXCHANGE = "recipe.dlx.exchange";
    public static final String RECIPE_DLX_QUEUE = "recipe.dlx.queue";
    public static final String RECIPE_DLX_ROUTING_KEY = "recipe.failed";

    // ─── Notification Exchange & Queue ────────────────────────────────
    public static final String NOTIFICATION_EXCHANGE_NAME = "notification.exchange";
    public static final String NOTIFICATION_QUEUE_NAME = "notification.queue";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.recipe.processed";

    // ─── Notification Dead Letter Exchange & Queue ─────────────────────────
    public static final String NOTIFICATION_DLX_EXCHANGE = "notification.dlx.exchange";
    public static final String NOTIFICATION_DLX_QUEUE = "notification.dlx.queue";
    public static final String NOTIFICATION_DLX_ROUTING_KEY = "notification.process.failed";
}