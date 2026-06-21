/**
 * 图片标注结果
 */
public class AnnotationResult {
    public final boolean success;
    public final String imagePath;
    public final String imageData;
    
    public AnnotationResult(boolean success, String imagePath, String imageData) {
        this.success = success;
        this.imagePath = imagePath;
        this.imageData = imageData;
    }
    
    @Override
    public String toString() {
        return String.format("AnnotationResult{success=%s, imagePath='%s', dataLength=%d}", 
                success, imagePath, imageData != null ? imageData.length() : 0);
    }
}