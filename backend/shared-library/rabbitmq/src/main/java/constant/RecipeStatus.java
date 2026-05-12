package constant;

public enum RecipeStatus {
    failed,
    completed;

    public String getNotificationTitle() {
        if(this.equals(failed)) {
            return "Recipe Failed";
        } else {
            return "Recipe Ready";
        }
    }
}