public final class BenchConstants {
    public static final String TYPE_TRANSLATION_TREE = "translation_tree";

    public static final String PROMPT_GENERATION = "system_prompt_generation.txt";
    public static final String PROMPT_IMAGE_ANNOTATION = "system_prompt_image_annotation.txt";

    public static final String DATASET_ROOT = "../../dataset";
    public static final String RESULTS_ROOT = "../../results";

    private BenchConstants() {
    }

    public static boolean isTranslationTree(String type) {
        return TYPE_TRANSLATION_TREE.equals(type) || "消息转发".equals(type);
    }
}
