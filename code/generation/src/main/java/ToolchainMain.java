import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ToolchainMain {
    private static final Path NL_SPEC_DIR = Path.of("nl-specs");
    private static final String DEFAULT_TYPE = BenchConstants.TYPE_TRANSLATION_TREE;

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║         TDL-TransTree-Bench DSL Toolchain                      ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println();

        // 打印LLM配置
        LlmConfig.getInstance().printConfig();

        System.out.println("🔧 初始化服务...");

        ToolchainService service = new ToolchainService();
        ensureNlDir();

        System.out.println("📂 扫描自然语言需求文件...");
        List<Path> nlFiles = loadNlFiles();

        if (nlFiles.isEmpty()) {
            System.err.println("❌ nl-specs 目录为空，请放入自然语言需求文本 (.txt)");
            return;
        }

        System.out.println("✅ 找到 " + nlFiles.size() + " 个需求文件");
        for (int i = 0; i < nlFiles.size(); i++) {
            System.out.println("   " + (i + 1) + ". " + nlFiles.get(i).getFileName());
        }
        System.out.println();

        for (int i = 0; i < nlFiles.size(); i++) {
            Path nlFile = nlFiles.get(i);
            System.out.println("╔════════════════════════════════════════════════════════════════╗");
            System.out.println("║ 处理文件 [" + (i + 1) + "/" + nlFiles.size() + "]: " + nlFile.getFileName());
            System.out.println("╚════════════════════════════════════════════════════════════════╝");
            System.out.println();

            String nlSpec = readTrimmed(nlFile);
            if (nlSpec.isBlank()) {
                System.err.println("   ⚠️  跳过，文件内容为空: " + nlFile.getFileName());
                System.out.println();
                continue;
            }

            System.out.println("📄 需求内容预览:");
            System.out.println("---");
            String preview = nlSpec.length() > 200 ? nlSpec.substring(0, 200) + "..." : nlSpec;
            System.out.println(preview);
            System.out.println("---");
            System.out.println("📊 完整需求长度: " + nlSpec.length() + " 字符");
            System.out.println();

            // 提取文件名（不含扩展名）
            String sourceFileName = nlFile.getFileName().toString();

            ToolchainResult result = service.generateAndValidate(nlSpec, DEFAULT_TYPE, sourceFileName);

            System.out.println("📋 执行结果汇总:");
            System.out.println("   成功: " + result.success);
            System.out.println("   日志: " + result.logPath);
            System.out.println("   SVG: " + result.svgPath);
            if (!result.success) {
                System.err.println("   失败原因: " + result.failureReason);
            }
            System.out.println();
        }

        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║ 所有文件处理完成！                                              ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
    }

    private static void ensureNlDir() {
        try {
            Files.createDirectories(NL_SPEC_DIR);
        } catch (IOException e) {
            throw new IllegalStateException("无法创建 nl-specs 目录", e);
        }
    }

    private static List<Path> loadNlFiles() {
        try {
            return Files.list(NL_SPEC_DIR)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".txt"))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException("读取 nl-specs 目录失败", e);
        }
    }

    private static String readTrimmed(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            throw new IllegalStateException("读取文件失败: " + file.toAbsolutePath(), e);
        }
    }
}
