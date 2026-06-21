import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Stream;
import java.util.OptionalInt;
import java.util.Comparator;
import java.util.stream.Collectors;

public class ToolchainService {
    private String dslDir = "dsl";
    private String outputDir = "output";
    private String annotatedImagesDir = "output/annotated-images";

    // 外部配置目录（优先级高）
    private static final Path EXTERNAL_CONFIG_DIR = Path.of("config");
    private static final Path EXTERNAL_PROMPTS_DIR = EXTERNAL_CONFIG_DIR.resolve("prompts");

    // 资源路径（classpath，打包后使用）
    private static final String CLASSPATH_PROMPTS_PREFIX = "/prompts/";

    private LlmClient llmClient;
    private LlmConfig config;
    private ConversionProgressListener progressListener;
    private ConversionHistory conversionHistory;
    private OrganizedStorageService storageService;

    public ToolchainService(LlmClient llmClient) {
        this.llmClient = llmClient;
        this.config = LlmConfig.getInstance();
        this.conversionHistory = new ConversionHistory();
        this.storageService = new OrganizedStorageService();
    }

    /**
     * 设置输出目录（用于实验运行器）
     */
    public void setOutputDirectory(String dir) {
        this.outputDir = dir;
        this.dslDir = dir;
        this.annotatedImagesDir = Path.of(dir).resolve("annotated-images").toString();
        // 更新 organized storage service 的 output base
        this.storageService.setOutputBase(dir);
    }

    public ToolchainService() {
        this(new OpenAiLlmClient());
    }

    /**
     * 获取转换历史管理器
     */
    public ConversionHistory getConversionHistory() {
        return conversionHistory;
    }

    /**
     * 获取组织化存储服务
     */
    public OrganizedStorageService getStorageService() {
        return storageService;
    }

    /**
     * 重新加载配置和 LLM 客户端
     * 用于配置更改后刷新服务
     */
    public void reloadConfig() {
        this.config = LlmConfig.getInstance();
        this.llmClient = new OpenAiLlmClient();
        System.out.println("🔄 ToolchainService 已重新加载配置");
    }

    /**
     * 切换到豆包图片标注模型
     */
    private void switchToDoubaoImageModel() {
        try {
            // 强制重新加载配置，确保获取最新的配置
            LlmConfig.resetInstance();
            this.config = LlmConfig.getInstance();

            // 临时切换到 doubao-image 配置
            this.llmClient = createDoubaoImageClient();
            notifyLog("🔄 已切换到豆包图片标注模型 (Doubao-Seedream-4.5)");
        } catch (Exception e) {
            notifyLog("⚠️  切换模型失败，使用默认配置: " + e.getMessage());
        }
    }

    /**
     * 切换到豆包DSL生成模型
     */
    private void switchToDoubaoDslModel() {
        try {
            // 临时切换到 doubao-dsl 配置
            this.llmClient = createDoubaoDslClient();
            notifyLog("🔄 已切换到豆包DSL生成模型 (Doubao-Seed-1.6-flash)");
        } catch (Exception e) {
            notifyLog("⚠️  切换模型失败，使用默认配置: " + e.getMessage());
        }
    }

    /**
     * 切换到Gemini图片标注模型
     */
    private void switchToGeminiImageModel() {
        try {
            // 强制重新加载配置，确保获取最新的配置
            LlmConfig.resetInstance();
            this.config = LlmConfig.getInstance();

            // 临时切换到 gemini-image 配置
            this.llmClient = createGeminiImageClient();
            notifyLog("🔄 已切换到Gemini图片标注模型 (Gemini 3 Pro Image Preview)");
        } catch (Exception e) {
            notifyLog("⚠️  切换模型失败，使用默认配置: " + e.getMessage());
        }
    }

    /**
     * 创建豆包图片标注客户端
     */
    private LlmClient createDoubaoImageClient() {
        String apiKey = config.getConfigProperty("llm.doubao-image.apiKey", "");
        String apiBase = config.getConfigProperty("llm.doubao-image.apiBase", "");
        String model = config.getConfigProperty("llm.doubao-image.model", "");
        double temperature = Double.parseDouble(config.getConfigProperty("llm.doubao-image.temperature", "0.2"));

        // 添加调试信息
        notifyLog("🔍 豆包图片标注客户端配置:");
        notifyLog("   API Key: " + (apiKey.length() > 8 ? apiKey.substring(0, 8) + "****" : "****"));
        notifyLog("   API Base: " + apiBase);
        notifyLog("   Model: " + model);
        notifyLog("   Temperature: " + temperature);

        if (model.isEmpty()) {
            notifyLog("⚠️  警告: 模型配置为空，请检查配置文件");
        }

        return new OpenAiLlmClient(
            java.net.http.HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(30))
                .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                .version(java.net.http.HttpClient.Version.HTTP_1_1)
                .build(),
            apiKey, apiBase, model, temperature
        );
    }

    /**
     * 创建豆包DSL生成客户端
     */
    private LlmClient createDoubaoDslClient() {
        String apiKey = config.getConfigProperty("llm.doubao-dsl.apiKey", "");
        String apiBase = config.getConfigProperty("llm.doubao-dsl.apiBase", "");
        String model = config.getConfigProperty("llm.doubao-dsl.model", "");
        double temperature = Double.parseDouble(config.getConfigProperty("llm.doubao-dsl.temperature", "0.2"));

        return new OpenAiLlmClient(
            java.net.http.HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(30))
                .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                .version(java.net.http.HttpClient.Version.HTTP_1_1)
                .build(),
            apiKey, apiBase, model, temperature
        );
    }

    /**
     * 创建Gemini图片标注客户端
     */
    private LlmClient createGeminiImageClient() {
        String apiKey = config.getConfigProperty("llm.gemini-image.apiKey", "");
        String apiBase = config.getConfigProperty("llm.gemini-image.apiBase", "");
        String model = config.getConfigProperty("llm.gemini-image.model", "");
        double temperature = Double.parseDouble(config.getConfigProperty("llm.gemini-image.temperature", "0.2"));

        // 添加调试信息
        notifyLog("🔍 Gemini图片标注客户端配置:");
        notifyLog("   API Key: " + (apiKey.length() > 8 ? apiKey.substring(0, 8) + "****" : "****"));
        notifyLog("   API Base: " + apiBase);
        notifyLog("   Model: " + model);
        notifyLog("   Temperature: " + temperature);

        if (model.isEmpty()) {
            notifyLog("⚠️  警告: 模型配置为空，请检查配置文件");
        }

        return new OpenAiLlmClient(
            java.net.http.HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(30))
                .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                .version(java.net.http.HttpClient.Version.HTTP_1_1)
                .build(),
            apiKey, apiBase, model, temperature
        );
    }

    /**
     * 获取LLM客户端
     */
    public LlmClient getLlmClient() {
        return this.llmClient;
    }

    /**
     * 设置进度监听器
     */
    public void setProgressListener(ConversionProgressListener listener) {
        this.progressListener = listener;
    }

    /**
     * 通知进度监听器
     */
    private void notifyLog(String message) {
        System.out.println(message);
        if (progressListener != null) {
            progressListener.onLog(message);
        }
    }

    /**
     * 翻译树图片标注功能
     * 直接返回标注后的图片，不进行DSL解析
     */
    public AnnotationResult annotateImage(String imageContent, String sourceFileName) {
        notifyLog("========================================");
        notifyLog("🎨 翻译树图片标注启动");
        notifyLog("========================================");
        notifyLog("📂 源文件: " + (sourceFileName != null ? sourceFileName : "未指定"));
        
        try {
            ensureDirectories();
            
            String activeLlm = config.getActiveLlm();
            String modelName = "";
            
            if ("doubao-image".equals(activeLlm)) {
                switchToDoubaoImageModel();
                modelName = "豆包图片标注 (Doubao-Seedream-4.5)";
            } else if ("gemini-image".equals(activeLlm)) {
                switchToGeminiImageModel();
                modelName = "Gemini图片标注 (Gemini 3 Pro Image Preview)";
            } else {
                String error = "当前激活的LLM不支持图片标注，请在配置中选择 doubao-image 或 gemini-image，当前为: " + activeLlm;
                notifyLog(error);
                if (progressListener != null) {
                    progressListener.onError(error);
                }
                return new AnnotationResult(false, "", error);
            }
            
            notifyLog("🔧 正在构建图片标注提示词...");
            String prompt = buildImageAnnotationPrompt(imageContent);
            notifyLog("✅ 提示词构建完成，长度: " + prompt.length() + " 字符");
            
            notifyLog("🤖 正在调用 " + modelName + "...");
            if (progressListener != null) {
                progressListener.onLlmCallStart(modelName);
            }
            
            long startTime = System.currentTimeMillis();
            String annotatedImageData;
            
            try {
                annotatedImageData = llmClient.generate(prompt, "图片标注");
            } catch (Exception e) {
                String error = "❌ 图片标注API调用失败: " + e.getMessage();
                notifyLog(error);
                if (progressListener != null) {
                    progressListener.onError(error);
                }
                return new AnnotationResult(false, "", "API调用失败: " + e.getMessage());
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            notifyLog("✅ 图片标注API调用完成，耗时: " + duration + " ms");
            
            if (annotatedImageData == null || annotatedImageData.isBlank()) {
                String error = "❌ 错误: 图片标注返回空内容";
                notifyLog(error);
                if (progressListener != null) {
                    progressListener.onError(error);
                }
                // 尝试查找最近的 llm_response 文件以便排查
                try {
                    java.nio.file.Path debug = findLatestLlmResponseFile();
                    if (debug != null) {
                        String dbgMsg = "（LLM 响应已保存: " + debug.toAbsolutePath() + "）";
                        notifyLog("   调试文件: " + debug.toAbsolutePath());
                        error = error + " " + dbgMsg;
                    }
                } catch (Exception ignore) {}
                return new AnnotationResult(false, "", "返回空内容");
            }
            
            // 保存标注后的图片
            String fileName = buildAnnotatedImageFileName(sourceFileName);
            Path annotatedImagePath = saveAnnotatedImage(annotatedImageData, fileName);
            
            notifyLog("========================================");
            notifyLog("🎉 图片标注完成！");
            notifyLog("========================================");
            notifyLog("📁 标注图片保存位置: " + annotatedImagePath.toAbsolutePath());
            notifyLog("💡 请从文件夹中选择最佳标注图片用于DSL生成");
            notifyLog("");
            
            if (progressListener != null) {
                progressListener.onSuccess(1, annotatedImagePath.toString(), "");
            }
            
            return new AnnotationResult(true, annotatedImagePath.toString(), annotatedImageData);
            
        } catch (Exception e) {
            String error = "❌ 图片标注处理失败: " + e.getMessage();
            notifyLog(error);
            if (progressListener != null) {
                progressListener.onError(error);
            }
            return new AnnotationResult(false, "", "处理失败: " + e.getMessage());
        }
    }

    /**
     * 构建图片标注提示词
     */
    private String buildImageAnnotationPrompt(String imageContent) {
        String basePrompt = readPromptFile(BenchConstants.PROMPT_IMAGE_ANNOTATION).trim();
        
        return String.format(
                "%s\n" +
                "\n" +
                "[输入图片和PDF内容]\n" +
                "%s\n" +
                "\n" +
                "[重要提醒]\n" +
                "请按照提示词要求对图片进行标注，返回标注后的图片数据。\n",
                basePrompt, imageContent == null ? "" : imageContent);
    }

    /**
     * 构建标注图片文件名
     */
    private String buildAnnotatedImageFileName(String sourceFileName) {
        String baseName;
        if (sourceFileName != null && !sourceFileName.isBlank()) {
            // 去掉原始扩展名，添加标注标识
            baseName = sourceFileName.replaceAll("\\.(png|jpg|jpeg|gif|bmp)$", "");
            baseName = baseName + "_annotated";
        } else {
            // 使用时间戳作为默认名称
            baseName = "annotated_" + System.currentTimeMillis();
        }
        
        // 清理文件名
        baseName = baseName.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}_-]+", "_");
        return baseName;
    }

    /**
     * 保存标注后的图片
     * 同时保存原始数据（txt）和PNG图片，方便调试
     */
    private Path saveAnnotatedImage(String imageData, String fileName) {
        try {
            Path annotatedDir = Path.of(annotatedImagesDir);
            if (!Files.exists(annotatedDir)) {
                Files.createDirectories(annotatedDir);
            }

            notifyLog("🔍 开始保存图片数据...");
            notifyLog("   数据长度: " + (imageData != null ? imageData.length() : 0) + " 字符");

            if (imageData == null || imageData.trim().isEmpty()) {
                notifyLog("   ⚠️  图片数据为空");
                String debugFileName = fileName + "_empty.txt";
                Path debugPath = annotatedDir.resolve(debugFileName);
                Files.writeString(debugPath, "数据为空", StandardCharsets.UTF_8);
                return debugPath;
            }

            String trimmed = imageData.trim();
            notifyLog("   数据前50字符: " + trimmed.substring(0, Math.min(50, trimmed.length())));

            // 1) 如果是 data URL，直接使用现有实现
            if (trimmed.startsWith("data:image/")) {
                return saveFromDataUrl(trimmed, fileName);
            }

            // 2) 如果是远程 URL，尝试下载并保存为 PNG
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                String actualFileName = fileName + ".png";
                Path imagePath = annotatedDir.resolve(actualFileName);
                try {
                    notifyLog("   🌐 发现远程图片URL，开始下载: " + trimmed);
                    java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                            .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                            .connectTimeout(java.time.Duration.ofSeconds(30))
                            .build();
                    java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                            .uri(java.net.URI.create(trimmed))
                            .header("User-Agent", "Link16-DSL-Converter/1.0")
                            .timeout(java.time.Duration.ofSeconds(60))
                            .GET()
                            .build();
                    java.net.http.HttpResponse<byte[]> resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofByteArray());
                    if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                        byte[] bytes = resp.body();
                        Files.write(imagePath, bytes);
                        notifyLog("   ✅ 远程图片已下载并保存: " + imagePath.getFileName());
                        notifyLog("   📊 图片大小: " + (bytes.length / 1024) + " KB");
                        return imagePath;
                    } else {
                        notifyLog("   ❌ 下载远程图片失败，HTTP状态: " + resp.statusCode());
                        // 继续后续尝试（如解析为base64）
                    }
                } catch (Exception e) {
                    notifyLog("   ❌ 下载远程图片异常: " + e.getMessage());
                    // 继续尝试其它方式
                }
            }

            // 3) 尝试直接作为 base64 解码（移除空白、可能的引号）
            String cleaned = trimmed;
            // 去掉可能的 JSON 两端引号
            if ((cleaned.startsWith("\"") && cleaned.endsWith("\"")) || (cleaned.startsWith("'") && cleaned.endsWith("'"))) {
                cleaned = cleaned.substring(1, cleaned.length() - 1);
            }
            // 去掉 data URL 前缀如果存在
            int idx = cleaned.indexOf("base64,");
            if (idx >= 0) {
                cleaned = cleaned.substring(idx + "base64,".length());
            }
            // 移除所有空白字符
            cleaned = cleaned.replaceAll("\\s+", "");

            String pngFileName = fileName + ".png";
            Path pngPath = annotatedDir.resolve(pngFileName);
            try {
                byte[] imageBytes = java.util.Base64.getDecoder().decode(cleaned);
                Files.write(pngPath, imageBytes);
                notifyLog("   ✅ PNG图片已保存: " + pngPath.getFileName());
                notifyLog("   📊 图片大小: " + (imageBytes.length / 1024) + " KB");
                notifyLog("   💡 如果PNG打不开，请检查 " + fileName + "_raw.txt 查看原始数据");
                return pngPath;
            } catch (IllegalArgumentException e) {
                notifyLog("   ⚠️ 直接Base64解码失败: " + e.getMessage());
                // 尝试从文本中提取长base64片段
                java.util.regex.Pattern p = java.util.regex.Pattern.compile("([A-Za-z0-9+/=]{200,})");
                java.util.regex.Matcher mm = p.matcher(trimmed);
                if (mm.find()) {
                    String candidate = mm.group(1).replaceAll("\\s+", "");
                    try {
                        byte[] imageBytes2 = java.util.Base64.getDecoder().decode(candidate);
                        Files.write(pngPath, imageBytes2);
                        notifyLog("   ✅ 从响应中提取长base64片段并保存为PNG: " + pngPath.getFileName());
                        notifyLog("   📊 图片大小: " + (imageBytes2.length / 1024) + " KB");
                        return pngPath;
                    } catch (IllegalArgumentException ex) {
                        notifyLog("   ❌ 提取的base64片段解码失败: " + ex.getMessage());
                    }
                }

                // 4) 尝试解析为 JSON 并从常见字段中取出 base64
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(trimmed);
                    com.fasterxml.jackson.databind.JsonNode b64 = root.at("/data/0/b64_json");
                     if (!b64.isMissingNode() && !b64.asText().isBlank()) {
                         String cleaned2 = b64.asText().replaceAll("\\s+", "");
                         byte[] imageBytes3 = java.util.Base64.getDecoder().decode(cleaned2);
                         Files.write(pngPath, imageBytes3);
                         notifyLog("   ✅ 从JSON字段提取并保存PNG: " + pngPath.getFileName());
                         return pngPath;
                     }
                 } catch (Exception ex) {
                     // ignore
                 }

                notifyLog("   ❌ Base64 解码尝试全部失败，保存原始数据供调试");
                // 保存原始数据以便人工检查
                String txtFileName = fileName + "_raw.txt";
                Path txtPath = annotatedDir.resolve(txtFileName);
                Files.writeString(txtPath, trimmed, StandardCharsets.UTF_8);
                notifyLog("   ✅ 原始数据已保存: " + txtPath.getFileName());
                return txtPath;
            }

        } catch (IOException e) {
            throw new IllegalStateException("保存标注图片失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 从 data URL 格式保存图片
     */
    private Path saveFromDataUrl(String dataUrl, String fileName) throws IOException {
        String actualFileName = fileName + ".png";
        Path imagePath = Path.of(annotatedImagesDir).resolve(actualFileName);
        
        int commaIndex = dataUrl.indexOf(",");
        if (commaIndex < 0) {
            notifyLog("   ❌ data URL 格式错误：找不到逗号分隔符");
            return saveDebugFile(dataUrl, fileName);
        }
        
        String base64Data = dataUrl.substring(commaIndex + 1).trim().replaceAll("\\s+", "");
        try {
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
            Files.write(imagePath, imageBytes);
            notifyLog("   ✅ 标注图片已保存为PNG: " + imagePath.getFileName());
            notifyLog("   📊 图片大小: " + (imageBytes.length / 1024) + " KB");
            return imagePath;
        } catch (IllegalArgumentException e) {
            notifyLog("   ❌ Base64解码失败: " + e.getMessage());
            return saveDebugFile(dataUrl, fileName);
        }
    }
    
    /**
     * 从纯 base64 格式保存图片
     */
    private Path saveFromBase64(String base64Data, String fileName) throws IOException {
        String actualFileName = fileName + ".png";
        Path imagePath = Path.of(annotatedImagesDir).resolve(actualFileName);
        
        try {
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
            Files.write(imagePath, imageBytes);
            notifyLog("   ✅ 标注图片已保存为PNG: " + imagePath.getFileName());
            notifyLog("   📊 图片大小: " + (imageBytes.length / 1024) + " KB");
            return imagePath;
        } catch (IllegalArgumentException e) {
            notifyLog("   ❌ Base64解码失败: " + e.getMessage());
            return saveDebugFile(base64Data, fileName);
        }
    }
    
    /**
     * 保存调试文件
     */
    private Path saveDebugFile(String data, String fileName) throws IOException {
        String actualFileName = fileName + "_debug.txt";
        Path textPath = Path.of(annotatedImagesDir).resolve(actualFileName);
        
        String debugInfo = "图片数据类型: 未知\n" +
                          "数据长度: " + (data != null ? data.length() : 0) + "\n" +
                          "数据前500字符:\n" + 
                          (data != null && data.length() > 500 ? 
                              data.substring(0, 500) + "..." : data) + "\n";
        Files.writeString(textPath, debugInfo, StandardCharsets.UTF_8);
        notifyLog("   ⚠️  未识别的图片数据格式，已保存调试信息: " + textPath.getFileName());
        return textPath;
    }

    public ToolchainResult generateAndValidate(String nlSpec, String type) {
        return generateAndValidate(nlSpec, type, null);
    }

    public ToolchainResult generateAndValidate(String nlSpec, String type, String sourceFileName) {
        notifyLog("========================================");
        notifyLog("🚀 工具链启动");
        notifyLog("========================================");
        notifyLog("📋 类型: " + type);
        notifyLog("📝 需求长度: " + (nlSpec == null ? 0 : nlSpec.length()) + " 字符");
        if (sourceFileName != null) {
            notifyLog("📂 源文件: " + sourceFileName);
        }

        // 暂时允许 Vision 模型用于 DSL 生成（Run 1 baseline实验需Vision模型）
        // if ("doubao-image".equals(activeLlm) || "gemini-image".equals(activeLlm)) {
        // ... exception ...
        // }

        // 从配置读取最大重试次数
        int maxRetries = config.getMaxRetries();
        boolean enablePreCheck = config.isEnableSyntaxPreCheck();

        notifyLog("🔄 最大重试次数: " + maxRetries);
        notifyLog("🔍 语法预检查: " + (enablePreCheck ? "启用" : "禁用"));
        notifyLog("");

        ensureDirectories();
        String feedback = "";
        String previousDsl = "";  // 保存上一版DSL
        List<GenerationAttempt> attempts = new java.util.ArrayList<>();
        String failureReason = "";
        String baseName = buildDslFileName(type, sourceFileName);

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            notifyLog("----------------------------------------");
            notifyLog("🔄 第 " + attempt + "/" + maxRetries + " 次尝试");
            notifyLog("----------------------------------------");

            // 第一次是生成，后续是修改
            boolean isRetry = attempt > 1;

            // 通知监听器开始新的尝试
            if (progressListener != null) {
                progressListener.onAttemptStart(attempt, maxRetries, isRetry);
            }

            if (isRetry) {
                notifyLog("📝 模式: 修改模式（基于上一版DSL）");
                notifyLog("📊 上一版DSL长度: " + previousDsl.length() + " 字符");
                notifyLog("❌ 错误反馈长度: " + feedback.length() + " 字符");
            } else {
                notifyLog("📝 模式: 生成模式（从零生成）");
            }

            notifyLog("🔧 正在构建提示词...");
            String prompt = buildPrompt(nlSpec, type, feedback, previousDsl, isRetry);
            notifyLog("✅ 提示词构建完成，长度: " + prompt.length() + " 字符");

            notifyLog("🤖 正在调用 LLM (" + config.getActiveLlmName() + ")...");
            if (progressListener != null) {
                progressListener.onLlmCallStart(config.getActiveLlmName());
            }

            long startTime = System.currentTimeMillis();
            String dsl = llmClient.generate(prompt, type);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            notifyLog("✅ LLM 返回完成，耗时: " + duration + " ms");
            if (progressListener != null) {
                progressListener.onLlmCallComplete(dsl, duration);
            }

            if (dsl == null || dsl.isBlank()) {
                String error = "❌ 错误: LLM 返回空 DSL 内容";
                notifyLog(error);
                if (progressListener != null) {
                    progressListener.onError(error);
                }
                throw new IllegalStateException("LLM 返回空 DSL 内容。");
            }
            notifyLog("📄 原始输出长度: " + dsl.length() + " 字符");

            // 🆕 清理 LLM 输出中的说明文字和 Markdown 标记
            notifyLog("🧹 正在清理输出中的说明文字...");
            String originalDsl = dsl;
            dsl = DslOutputCleaner.clean(dsl);

            if (!DslOutputCleaner.isValidDslOutput(dsl)) {
                notifyLog("⚠️  警告: 清理后的输出不包含有效的 DSL 代码，使用原始输出");
                dsl = originalDsl;
            } else if (!dsl.equals(originalDsl)) {
                notifyLog("   ✅ 已移除说明文字，清理后长度: " + dsl.length() + " 字符");
            } else {
                notifyLog("   ✅ 输出已是纯 DSL 代码，无需清理");
            }

            // 通知监听器 DSL 已清理
            if (progressListener != null) {
                progressListener.onDslCleaned(dsl);
                // 注意：这里先不回调 onDslGenerated，因为后续还可能进行语法预检查/翻译树二次清理，
                // 否则 GUI 预览会展示中间态（可能仍包含字面 \n 或未缩进）。
            }

            // 🆕 语法预检查
            if (enablePreCheck) {
                notifyLog("🔍 正在进行语法预检查...");
                DslSyntaxPreChecker.PreCheckResult preCheckResult = DslSyntaxPreChecker.check(dsl);
                preCheckResult.printReport();

                if (preCheckResult.hasChanges) {
                    notifyLog("   ✅ 已自动修复部分格式问题");
                    dsl = preCheckResult.fixedDsl;
                }
            }

            // 🔒 双保险：翻译树 DSL 在落盘前再清理一次，确保换行/缩进已应用
            if (BenchConstants.isTranslationTree(type)) {
                dsl = DslOutputCleaner.clean(dsl);
                notifyLog("   ✅ 翻译树DSL落盘前已再次清理(contains\\n=" + dsl.contains("\\n") + ")");
            }

            // ✅ 在所有格式化/预检查之后，回调最终 DSL 给 GUI 预览
            if (progressListener != null) {
                progressListener.onDslGenerated(dsl, attempt);
            }

            String fileName = baseName + "-attempt-" + attempt;
            Path dslFile = Path.of(dslDir).resolve(fileName + ".dsl");
            Path attemptCopy = Path.of(outputDir).resolve(fileName + ".dsl");

            notifyLog("💾 正在保存DSL文件...");
            writeDslFile(dslFile, dsl);
            writeDslFile(attemptCopy, dsl);
            notifyLog("✅ DSL已保存到: " + dslFile);
            notifyLog("✅ 备份已保存到: " + attemptCopy);

            notifyLog("🔍 正在进行语法解析...");
            if (progressListener != null) {
                progressListener.onParseStart();
            }

            notifyLog("   使用翻译树 DSL 解析器");
            ParseResult parseResult = TranslationTreeParserRunner.parseFile(dslFile.toFile());
            notifyLog("✅ 解析完成");

            if (progressListener != null) {
                progressListener.onParseComplete(parseResult);
            }

            notifyLog("📝 正在写入错误摘要...");
            writeAttemptSummary(fileName, parseResult);
            attempts.add(new GenerationAttempt(attempt, dsl, parseResult.errors, attemptCopy.toString(), parseResult.logPath));

            if (parseResult.success) {
                notifyLog("");
                notifyLog("========================================");
                notifyLog("🎉 成功！DSL 语法解析通过！");
                notifyLog("========================================");
                notifyLog("✅ 成功尝试次数: " + attempt);
                notifyLog("📊 解析日志: " + parseResult.logPath);
                notifyLog("🖼️  可视化图: " + parseResult.svgPath);
                notifyLog("");

                // 保存成功的转换历史
                recordConversionHistory(sourceFileName, type, dsl, parseResult, attempt, true, "");

                // 自动保存到组织化目录
                saveToOrganizedStorage(sourceFileName, type, dsl, parseResult);

                return new ToolchainResult(true, dsl, parseResult.logPath, parseResult.svgPath, parseResult.errors, attempts, "");
            }

            notifyLog("⚠️  解析失败，发现 " + parseResult.errorCount + " 个错误");

            // 保存当前DSL和错误信息，用于下一次重试
            previousDsl = dsl;
            feedback = buildSyntaxFeedback(parseResult.syntaxErrors);
            if (feedback.isBlank()) {
                failureReason = String.join(System.lineSeparator(), parseResult.errors);
            } else {
                failureReason = feedback;
            }

            if (attempt < maxRetries) {
                notifyLog("🔄 准备进行第 " + (attempt + 1) + " 次重试...");
                notifyLog("");
            }
        }

        notifyLog("");
        notifyLog("========================================");
        notifyLog("❌ 失败：已达到最大重试次数");
        notifyLog("========================================");
        notifyLog("🔢 总尝试次数: " + maxRetries);
        notifyLog("📋 最终失败原因: " + failureReason);
        notifyLog("");

        if (progressListener != null) {
            progressListener.onFailure(maxRetries, failureReason);
        }

        // 记录失败的转换历史
        recordConversionHistory(sourceFileName, type, previousDsl, null, maxRetries, false, failureReason);

        return new ToolchainResult(false, "", "", "", List.of(failureReason), attempts, failureReason);
    }

    private String buildPrompt(String nlSpec, String type, String feedback, String previousDsl, boolean isRetry) {
        return buildTranslationTreePrompt(nlSpec, feedback, previousDsl, isRetry);
    }

    /**
     * 构建翻译树DSL提示词
     */
    private String buildTranslationTreePrompt(String nlSpec, String feedback, String previousDsl, boolean isRetry) {
        // 获取当前用户选择的LLM配置
        String activeLlm = config.getActiveLlm();
        notifyLog("📋 翻译树DSL使用用户选择的LLM: " + activeLlm);
        
        // 强制为 False，因为 generateAndValidate 是用于生成 DSL 的。
        // 原有逻辑假设 "gemini-image" 只能做标注，这在 Vision-to-DSL 实验中是不成立的。
        boolean isImageAnnotation = false; 
        
        if (isImageAnnotation) {
            notifyLog("🎨 执行图片标注任务 (用户选择: " + activeLlm + ")");
        } else {
            notifyLog("🔧 执行DSL生成任务 (用户选择: " + activeLlm + ")");
        }
        
        String promptFile = isImageAnnotation ?
            BenchConstants.PROMPT_IMAGE_ANNOTATION :
            BenchConstants.PROMPT_GENERATION;
            
        String basePrompt = readPromptFile(promptFile).trim();

        if (isRetry) {
            String modificationPrompt = String.format(
                    "[Task Change]\n" +
                    "Your task is to repair the previous DSDL instance to fix syntax errors reported by the parser.\n" +
                    "\n" +
                    "[Previous DSDL Instance (with syntax errors)]\n" +
                    "```\n" +
                    "%s\n" +
                    "```\n" +
                    "\n" +
                    "[Parser-reported syntax errors]\n" +
                    "%s\n" +
                    "\n" +
                    "[Repair Requirements]\n" +
                    "1. Read the syntax errors carefully.\n" +
                    "2. Locate the offending positions (line, token).\n" +
                    "3. Fix errors according to BNF_Strict constraints.\n" +
                    "4. Output the complete repaired DSDL instance only.\n" +
                    "5. Do not add explanations or Markdown fences.\n" +
                    "6. Output must start with Message and end with the closing brace.\n" +
                    "\n" +
                    "[Original multimodal input (for reference)]\n" +
                    "%s\n",
                    previousDsl, feedback == null ? "No detailed errors" : feedback, nlSpec == null ? "" : nlSpec);

            return basePrompt + "\n\n" + modificationPrompt;
        } else {
            if (isImageAnnotation) {
                return String.format(
                        "%s\n" +
                        "\n" +
                        "[Input Image and PDF Content]\n" +
                        "%s\n" +
                        "\n" +
                        "[Reminder]\n" +
                        "Annotate the image according to the prompt and return the annotated image.\n",
                        basePrompt, nlSpec == null ? "" : nlSpec);
            } else {
                return String.format(
                        "%s\n" +
                        "\n" +
                        "[Input Content]\n" +
                        "%s\n" +
                        "\n" +
                        "[Reminder]\n" +
                        "Follow Interaction Rules: perform Phases 1-3 internally and return only the Phase 4 DSDL instance.\n" +
                        "Output must start with Message and end with the closing brace.\n" +
                        "Do not add explanations, headings, or Markdown fences.\n" +
                        "Inputs may include PDF text, annotated diagram(s), or both.\n",
                        basePrompt, nlSpec == null ? "" : nlSpec);
            }
        }
    }

    /**
     * 读取提示词文件（支持外部配置、源码目录、classpath 三级降级）
     *
     * @param fileName 提示词文件名
     * @return 文件内容
     */
    private String readPromptFile(String fileName) {
        // 1. 优先读取外部配置目录（运行时可修改）
        Path externalPath = EXTERNAL_PROMPTS_DIR.resolve(fileName);
        if (Files.exists(externalPath)) {
            try {
                String content = Files.readString(externalPath, StandardCharsets.UTF_8);
                System.out.println("✅ 从外部配置读取提示词: " + externalPath);
                return content;
            } catch (IOException e) {
                System.err.println("⚠️  读取外部配置失败，尝试其他路径: " + e.getMessage());
            }
        }

        // 2. 尝试读取仓库级 prompts 目录（开发环境）
        Path repoPromptPath = Path.of("..", "..", "prompts",
                fileName.equals(BenchConstants.PROMPT_GENERATION) ? "dsdl_generation" : "image_annotation",
                fileName.equals(BenchConstants.PROMPT_GENERATION) ? "system_prompt.txt" : "vcot_full.txt");
        if (Files.exists(repoPromptPath)) {
            try {
                String content = Files.readString(repoPromptPath, StandardCharsets.UTF_8);
                System.out.println("✅ 从仓库 prompts 目录读取提示词: " + repoPromptPath);
                return content;
            } catch (IOException e) {
                System.err.println("⚠️  读取仓库 prompts 目录失败: " + e.getMessage());
            }
        }

        // 3. 降级到 classpath（打包后的资源）
        String resourcePath = CLASSPATH_PROMPTS_PREFIX + fileName;
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is != null) {
                String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                System.out.println("✅ 从 classpath 读取提示词: " + resourcePath);
                return content;
            }
        } catch (IOException e) {
            System.err.println("⚠️  从 classpath 读取失败: " + e.getMessage());
        }

        throw new IllegalStateException("无法读取提示词文件: " + fileName +
                "\n已尝试路径:\n" +
                "  1. " + externalPath.toAbsolutePath() + "\n" +
                "  2. " + repoPromptPath.toAbsolutePath() + "\n" +
                "  3. classpath:" + resourcePath);
    }

    private String readResource(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("无法读取文件: " + path.toAbsolutePath(), e);
        }
    }

    private String buildDslFileName(String type, String sourceFileName) {
        String base;

        if (sourceFileName != null && !sourceFileName.isBlank()) {
            // 使用源文件名（去掉扩展名）
            base = sourceFileName.replaceAll("\\.[a-zA-Z0-9]+$", "");
        } else if (type != null && !type.isBlank()) {
            // 使用类型名
            base = type;
        } else {
            // 默认名称
            base = "generated";
        }

        // 清理文件名，只保留字母、数字、下划线和中文
        base = base.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}_-]+", "_");
        return base;
    }

    /**
     * 为给定 baseName 和后缀（如 "single" 或 "repair"）在 dsl 目录中查找已存在的文件并返回下一个可用序号。
     * 如果目录不存在或出错，返回 1 作为默认序号。
     */
    private int getNextSequenceIndex(String baseName, String suffix) {
        try {
            Path dir = Path.of(dslDir);
            if (!Files.exists(dir)) return 1;

            // 正确构造正则：匹配形如 baseName-suffix-<number>.dsl 的文件名
            String regex = Pattern.quote(baseName) + "-" + Pattern.quote(suffix) + "-(\\d+)\\.dsl";
            Pattern p = Pattern.compile(regex);

            try (Stream<Path> stream = Files.list(dir)) {
                OptionalInt max = stream
                        .map(Path::getFileName)
                        .map(Object::toString)
                        .map(p::matcher)
                        .filter(Matcher::matches)
                        .mapToInt(m -> Integer.parseInt(m.group(1)))
                        .max();

                return max.isPresent() ? (max.getAsInt() + 1) : 1;
            }
        } catch (Exception e) {
            // 出错时返回默认序号
            return 1;
        }
    }

    private void ensureDirectories() {
        try {
            Files.createDirectories(Path.of(dslDir));
            Files.createDirectories(Path.of(outputDir));
            Files.createDirectories(Path.of(annotatedImagesDir));
        } catch (IOException e) {
            throw new IllegalStateException("无法创建输出目录。", e);
        }
    }

    private void writeDslFile(Path target, String dsl) {
        try {
            Files.writeString(target, dsl.trim() + System.lineSeparator(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("无法写入 DSL 文件: " + target.toAbsolutePath(), e);
        }
    }

    private void writeAttemptSummary(String fileName, ParseResult parseResult) {
        Path summaryPath = Path.of(outputDir).resolve(fileName + "-errors.txt");
        StringBuilder summary = new StringBuilder();
        summary.append("解析结果: ").append(parseResult.success ? "成功" : "失败").append(System.lineSeparator());
        summary.append("错误数量: ").append(parseResult.errorCount).append(System.lineSeparator());
        summary.append("错误列表:").append(System.lineSeparator());
        for (String error : parseResult.errors) {
            summary.append("- ").append(error).append(System.lineSeparator());
        }
        try {
            Files.writeString(summaryPath, summary.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("无法写入错误摘要文件: " + summaryPath.toAbsolutePath(), e);
        }
    }

    private String buildSyntaxFeedback(List<SyntaxErrorDetail> syntaxErrors) {
        if (syntaxErrors == null || syntaxErrors.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("请修正以下语法错误后重新生成：\n");
        for (SyntaxErrorDetail detail : syntaxErrors) {
            sb.append("- ").append(detail.toPromptLine()).append("\n");
        }
        return sb.toString().trim();
    }

    /**
     * 记录转换历史到JSON
     */
    private void recordConversionHistory(String sourceFile, String dslType, String dsl,
                                         ParseResult parseResult, int attempts,
                                         boolean success, String failureReason) {
        try {
            ConversionRecord record = new ConversionRecord();
            record.setId(java.util.UUID.randomUUID().toString());
            record.setTimestamp(java.time.LocalDateTime.now());
            record.setSourceFile(sourceFile != null ? sourceFile : "unknown");
            record.setDslType(dslType);
            record.setLlmModel(config.getActiveLlmName());
            record.setStatus(success ? "success" : "failed");
            record.setAttempts(attempts);
            record.setTotalErrors(parseResult != null ? parseResult.errorCount : 0);

            // 设置文件路径
            if (parseResult != null) {
                ConversionRecord.FilePaths filePaths = new ConversionRecord.FilePaths();
                filePaths.setLog(parseResult.logPath);
                filePaths.setSvg(parseResult.svgPath);
                filePaths.setDot(parseResult.dotPath);
                record.setFiles(filePaths);
            }

            // 设置统计信息
            ConversionRecord.ConversionStats stats = new ConversionRecord.ConversionStats();
            if (dsl != null) {
                stats.setDslLines((int) dsl.lines().count());
            }
            record.setStats(stats);

            if (!success) {
                record.setFailureReason(failureReason);
            }

            conversionHistory.addRecord(record);
            notifyLog("📝 转换历史已记录 (ID: " + record.getId().substring(0, 8) + "...)");

        } catch (Exception e) {
            System.err.println("⚠️ 记录转换历史失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 保存到组织化目录结构
     */
    private void saveToOrganizedStorage(String sourceFile, String dslType, String dsl, ParseResult parseResult) {
        try {
            String logContent = parseResult.logPath != null ?
                Files.readString(Path.of(parseResult.logPath), StandardCharsets.UTF_8) : "";
            String errors = parseResult.errors != null ?
                String.join("\n", parseResult.errors) : "";

            storageService.saveConversionResult(
                sourceFile,
                dslType,
                dsl,
                logContent,
                errors,
                parseResult.svgPath,
                parseResult.dotPath
            );

        } catch (Exception e) {
            System.err.println("⚠️ 保存到组织化目录失败: " + e.getMessage());
        }
    }

    /**
     * 单次生成一个 DSL 实例（不自动重试），并返回解析结果与生成内容。
     * nlSpec: 自然语言需求（可以为空），type: DSL类型（例如 功能模型/消息转发）
     */
    public synchronized ToolchainResult generateSingleDslInstance(String nlSpec, String type, String sourceFileName) {
        notifyLog("🔧 单次DSL实例生成：开始（type=" + type + ")");
        try {
            ensureDirectories();

            // 构建提示词（第一次生成，isRetry=false）
            String prompt = buildPrompt(nlSpec == null ? "" : nlSpec, type, "", "", false);
            notifyLog("🔍 生成提示词长度: " + prompt.length() + " 字符");

            String modelName = config.getActiveLlmName();
            notifyLog("🤖 调用 LLM: " + modelName + "（单次）");

            long start = System.currentTimeMillis();
            String dsl = llmClient.generate(prompt, type);
            long duration = System.currentTimeMillis() - start;
            notifyLog("✅ LLM 返回（单次），耗时: " + duration + " ms");

            if (dsl == null) dsl = "";

            // 清理输出
            String originalDsl = dsl;
            dsl = DslOutputCleaner.clean(dsl);
            if (!DslOutputCleaner.isValidDslOutput(dsl)) {
                notifyLog("⚠️ 清理后未检测到有效DSL，保留原始输出以便查看");
                dsl = originalDsl;
            }

            // 保存 DSL 与备份
            String baseName = buildDslFileName(type, sourceFileName);
            int seq = getNextSequenceIndex(baseName, "single");
            String fileName = baseName + "-single-" + seq;
            Path dslFile = Path.of(dslDir).resolve(fileName + ".dsl");
            Path attemptCopy = Path.of(outputDir).resolve(fileName + ".dsl");
            writeDslFile(dslFile, dsl);
            writeDslFile(attemptCopy, dsl);
            notifyLog("💾 单次DSL已保存: " + dslFile);

            // 解析
            notifyLog("🔍 开始语法解析（单次）...");
            ParseResult parseResult = TranslationTreeParserRunner.parseFile(dslFile.toFile());
            notifyLog("✅ 解析完成（单次），错误数: " + parseResult.errorCount);

            // 写错误摘要
            writeAttemptSummary(fileName, parseResult);

            List<GenerationAttempt> attempts = new java.util.ArrayList<>();
            attempts.add(new GenerationAttempt(1, dsl, parseResult.errors, dslFile.toString(), parseResult.logPath));

            if (parseResult.success) {
                notifyLog("🎉 单次生成并解析成功");
                return new ToolchainResult(true, dsl, parseResult.logPath, parseResult.svgPath, parseResult.errors, attempts, "");
            } else {
                String failureReason = String.join(System.lineSeparator(), parseResult.errors);
                notifyLog("⚠️ 单次生成解析发现错误: " + parseResult.errorCount);
                return new ToolchainResult(false, dsl, parseResult.logPath, parseResult.svgPath, parseResult.errors, attempts, failureReason);
            }
        } catch (Exception e) {
            notifyLog("❌ 单次生成失败: " + e.getMessage());
            List<GenerationAttempt> attempts = new java.util.ArrayList<>();
            return new ToolchainResult(false, "", "", "", List.of(e.getMessage()), attempts, e.getMessage());
        }
    }

    /**
     * 基于上一版DSL和用户修改后的错误/反馈进行修复并重新生成（一次性），返回新生成的 DSL 与解析结果。
     * previousDsl: 上一次 LLM 生成的 DSL 完整文本
     * userFeedback: 用户在 GUI 中编辑后的错误信息或补充要求
     */
    public synchronized ToolchainResult repairAndRegenerate(String previousDsl, String userFeedback, String type, String sourceFileName) {
        if (previousDsl == null || previousDsl.isBlank()) {
            String err = "缺少上一版 DSL，无法进行修复重生成。请先执行单次生成。";
            notifyLog("⚠️ " + err);
            List<GenerationAttempt> attempts = new java.util.ArrayList<>();
            return new ToolchainResult(false, "", "", "", List.of(err), attempts, err);
        }

        notifyLog("🔧 修复并重新生成：开始（基于上一版DSL）");
        try {
            ensureDirectories();

            // 构建修改类提示词（isRetry = true，使用 previousDsl 和 userFeedback）
            String prompt = buildPrompt("", type, userFeedback == null ? "" : userFeedback, previousDsl, true);
            notifyLog("🔍 修复提示词长度: " + prompt.length() + " 字符");

            String modelName = config.getActiveLlmName();
            notifyLog("🤖 调用 LLM: " + modelName + "（修复重生成）");

            long start = System.currentTimeMillis();
            String dsl = llmClient.generate(prompt, type);
            long duration = System.currentTimeMillis() - start;
            notifyLog("✅ LLM 返回（修复重生成），耗时: " + duration + " ms");

            if (dsl == null) dsl = "";

            // 清理输出
            String originalDsl = dsl;
            dsl = DslOutputCleaner.clean(dsl);
            if (!DslOutputCleaner.isValidDslOutput(dsl)) {
                notifyLog("⚠️ 修复后清理未检测到有效DSL，保留原始输出以便查看");
                dsl = originalDsl;
            }

            // 保存 DSL
            String baseName = buildDslFileName(type, sourceFileName);
            int seq = getNextSequenceIndex(baseName, "repair");
            String fileName = baseName + "-repair-" + seq;
            Path dslFile = Path.of(dslDir).resolve(fileName + ".dsl");
            Path attemptCopy = Path.of(outputDir).resolve(fileName + ".dsl");
            writeDslFile(dslFile, dsl);
            writeDslFile(attemptCopy, dsl);
            notifyLog("💾 修复后DSL已保存: " + dslFile);

            // 解析
            notifyLog("🔍 开始语法解析（修复版）...");
            ParseResult parseResult = TranslationTreeParserRunner.parseFile(dslFile.toFile());
            notifyLog("✅ 解析完成（修复版），错误数: " + parseResult.errorCount);

            // 写错误摘要
            writeAttemptSummary(fileName, parseResult);

            List<GenerationAttempt> attempts = new java.util.ArrayList<>();
            attempts.add(new GenerationAttempt(1, dsl, parseResult.errors, dslFile.toString(), parseResult.logPath));

            if (parseResult.success) {
                notifyLog("🎉 修复并重新生成成功");
                return new ToolchainResult(true, dsl, parseResult.logPath, parseResult.svgPath, parseResult.errors, attempts, "");
            } else {
                String failureReason = String.join(System.lineSeparator(), parseResult.errors);
                notifyLog("⚠️ 修复后仍存在错误: " + parseResult.errorCount);
                return new ToolchainResult(false, dsl, parseResult.logPath, parseResult.svgPath, parseResult.errors, attempts, failureReason);
            }
        } catch (Exception e) {
            notifyLog("❌ 修复并重新生成失败: " + e.getMessage());
            List<GenerationAttempt> attempts = new java.util.ArrayList<>();
            return new ToolchainResult(false, "", "", "", List.of(e.getMessage()), attempts, e.getMessage());
        }
    }

    /**
     * 查找 output/annotated-images 下最新的 llm_response_ 文件（用于调试）
     */
    private Path findLatestLlmResponseFile() {
        try {
            Path dir = Path.of(annotatedImagesDir);
            if (!Files.exists(dir)) return null;
            return Files.list(dir)
                    .filter(p -> p.getFileName().toString().startsWith("llm_response_"))
                    .max((a, b) -> Long.compare(a.toFile().lastModified(), b.toFile().lastModified()))
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
