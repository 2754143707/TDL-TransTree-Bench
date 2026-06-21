import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.stream.Stream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class ExperimentRunner {
    private static final String DEFAULT_MODEL = "gemini";

    public static void main(String[] args) {
        String mode = null;
        String model = DEFAULT_MODEL;

        for (int i = 0; i < args.length; i++) {
            if ("--mode".equals(args[i]) && i + 1 < args.length) mode = args[i + 1];
            if ("--model".equals(args[i]) && i + 1 < args.length) model = args[i + 1];
        }

        if (mode == null) {
            System.err.println("Usage: java ExperimentRunner --mode [baseline|color|anchor|vcot_k0|proposed_k3] --model [gemini|qwen-plus|claude]");
            return;
        }

        System.out.println("ExperimentRunner Starting...");
        System.out.println("Mode: " + mode);
        System.out.println("Model: " + model);

        try {
            runExperiment(mode, model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void runExperiment(String mode, String model) throws Exception {
        LlmConfig config = LlmConfig.getInstance();
        config.overrideProperty("llm.active", model);
        config.overrideProperty("llm." + model + ".temperature", "0.0");

        String inputDirName;
        String outputDirName;
        int maxRetries = 1;

        switch (mode) {
            case "baseline":
                inputDirName = "raw_images";
                outputDirName = "1_baseline_raw";
                maxRetries = 1;
                break;
            case "color":
                inputDirName = "vcot_ablation_color";
                outputDirName = "2_ablation_color";
                maxRetries = 1;
                break;
            case "anchor":
                inputDirName = "vcot_ablation_anchor";
                outputDirName = "3_ablation_anchor";
                maxRetries = 1;
                break;
            case "vcot_k0":
                inputDirName = "vcot_full";
                outputDirName = "4_vcot_k0";
                maxRetries = 1;
                break;
            case "proposed_k3":
                inputDirName = "vcot_full";
                outputDirName = "5_proposed_k3";
                maxRetries = 4;
                break;
            default:
                throw new IllegalArgumentException("Unknown mode: " + mode);
        }

        config.overrideProperty("toolchain.maxRetries", String.valueOf(maxRetries));

        ToolchainService service = new ToolchainService();

        Path inputDir = Path.of(BenchConstants.DATASET_ROOT, inputDirName);
        Path outputDir = Path.of(BenchConstants.RESULTS_ROOT, outputDirName, model);

        Files.createDirectories(outputDir);
        service.setOutputDirectory(outputDir.toString());

        System.out.println("Processing " + inputDir.toAbsolutePath() + " -> " + outputDir.toAbsolutePath());

        if (!Files.exists(inputDir)) {
            System.err.println("Input directory not found: " + inputDir.toAbsolutePath());
            return;
        }

        try (Stream<Path> files = Files.list(inputDir)) {
            files.filter(p -> {
                    String name = p.toString().toLowerCase();
                    return name.endsWith(".png") || name.endsWith(".pdf") || name.endsWith(".jpg");
                })
                 .sorted()
                 .forEach(imagePath -> {
                     System.out.println("----------------------------------------");
                     System.out.println("Processing: " + imagePath.getFileName());
                     try {
                         byte[] bytes = Files.readAllBytes(imagePath);
                         String base64 = Base64.getEncoder().encodeToString(bytes);

                         String fileName = imagePath.getFileName().toString();
                         String baseName = fileName.substring(0, fileName.lastIndexOf('.'));

                         Path pdfPath = Path.of(BenchConstants.DATASET_ROOT, "pdf_texts", baseName + ".pdf");
                         if (!Files.exists(pdfPath) && baseName.contains("_")) {
                             String currentName = baseName;
                             while (currentName.contains("_")) {
                                 currentName = currentName.substring(0, currentName.lastIndexOf('_'));
                                 Path tryPath = Path.of(BenchConstants.DATASET_ROOT, "pdf_texts", currentName + ".pdf");
                                 if (Files.exists(tryPath)) {
                                     pdfPath = tryPath;
                                     break;
                                 }
                             }
                         }

                         String pdfTextInfo = "";
                         if (Files.exists(pdfPath)) {
                             try (PDDocument document = PDDocument.load(pdfPath.toFile())) {
                                 PDFTextStripper stripper = new PDFTextStripper();
                                 String originalText = stripper.getText(document);
                                 pdfTextInfo = "[PDF text: " + originalText.trim() + "]\n";
                                 System.out.println("  Using PDF text: " + pdfPath.getFileName());
                             } catch (Exception e) {
                                 System.err.println("  Failed to extract PDF text: " + e.getMessage());
                             }
                         }

                         String prompt = pdfTextInfo + "[Base64 data: " + base64 + "]\nGenerate the translation-tree DSDL instance from this diagram.";
                         service.generateAndValidate(prompt, BenchConstants.TYPE_TRANSLATION_TREE, imagePath.getFileName().toString());

                     } catch (Exception e) {
                         System.err.println("Failed processing " + imagePath + ": " + e.getMessage());
                         e.printStackTrace();
                     }
                 });
        }

        System.out.println("Experiment " + mode + " completed.");
    }
}
