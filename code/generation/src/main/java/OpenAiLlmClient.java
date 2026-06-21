import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class OpenAiLlmClient implements LlmClient {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final HttpClient httpClient;
    private final String apiKey;
    private final String apiBase;
    private final String model;
    private final double temperature;
    private final String authHeaderName;
    private final String authScheme;

    /**
     * 从配置文件构造（推荐方式）
     */
    public OpenAiLlmClient() {
        this(LlmConfig.getInstance());
    }

    /**
     * 从LlmConfig构造
     */
    public OpenAiLlmClient(LlmConfig config) {
        this(HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(30))
                .build(),
                config.getApiKey(),
                config.getApiBase(),
                config.getModel(),
                config.getTemperature());
    }

    /**
     * 完全自定义构造（用于测试）
     */
    public OpenAiLlmClient(HttpClient httpClient, String apiKey, String apiBase, String model, double temperature) {
        this(httpClient, apiKey, apiBase, model, temperature, "Authorization", "Bearer");
    }

    /**
     * 更灵活的构造，支持自定义认证头（仅设置头名与方案）
     */
    public OpenAiLlmClient(HttpClient httpClient, String apiKey, String apiBase, String model, double temperature,
                           String authHeaderName, String authScheme) {
        this.httpClient = httpClient;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.apiBase = apiBase == null ? "" : apiBase.trim();
        this.model = model == null ? "" : model.trim();
        this.temperature = temperature;
        this.authHeaderName = authHeaderName == null ? "Authorization" : authHeaderName.trim();
        this.authScheme = authScheme == null ? "Bearer" : authScheme.trim();
    }

    /**
     * 从响应中提取base64数据
     */
    private String extractBase64FromResponse(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        String s = data.trim();

        // 1) 如果是data URL格式，提取base64部分
        if (s.startsWith("data:image/")) {
            int commaIndex = s.indexOf(",");
            if (commaIndex > 0 && commaIndex + 1 < s.length()) {
                String base64 = s.substring(commaIndex + 1).trim();
                System.out.println("   ✅ 已从data URL提取base64数据，长度: " + base64.length() + " 字符");
                return base64.replaceAll("\\s+", "");
            }
        }

        // 2) 如果是一个JSON字符串且包含 data:image/...;base64, 直接定位并提取
        java.util.regex.Pattern dataUrlPattern = java.util.regex.Pattern.compile("data:image/[^;]+;base64,([A-Za-z0-9+/=\\r\\n]+)", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher m = dataUrlPattern.matcher(s);
        if (m.find()) {
            String base64 = m.group(1).replaceAll("\\s+", "");
            System.out.println("   ✅ 从嵌入的data URL中提取到base64，长度: " + base64.length() + " 字符");
            return base64;
        }

        // 3) 如果字符串本身就是一串base64（长度较长且只包含base64字符），则直接返回
        if (s.matches("^[A-Za-z0-9+/=\\r\\n]+$") && s.length() > 200) {
            String cleaned = s.replaceAll("\\s+", "");
            System.out.println("   ✅ 响应看似纯base64串，长度: " + cleaned.length() + " 字符");
            return cleaned;
        }

        // 4) 查找文本中可能的长base64片段（百分比阈值以避免误捕获）
        java.util.regex.Pattern longB64Pattern = java.util.regex.Pattern.compile("([A-Za-z0-9+/=]{200,})");
        java.util.regex.Matcher m2 = longB64Pattern.matcher(s);
        if (m2.find()) {
            String candidate = m2.group(1);
            String cleaned = candidate.replaceAll("\\s+", "");
            System.out.println("   ✅ 从响应中提取到长 base64 片段，长度: " + cleaned.length() + " 字符");
            return cleaned;
        }

        // 5) 如果包含 'b64_json' 风格（有时response是字符串形式），尝试解析为 JSON 并提取
        try {
            JsonNode root = MAPPER.readTree(s);
            // 常见路径：data[0].b64_json 或 image 或 b64_json
            JsonNode b64 = root.at("/data/0/b64_json");
            if (!b64.isMissingNode() && !b64.asText().isBlank()) {
                String cleaned = b64.asText().replaceAll("\\s+", "");
                System.out.println("   ✅ 从 JSON.data[0].b64_json 提取到 base64，长度: " + cleaned.length() + " 字符");
                return cleaned;
            }
            JsonNode image = root.at("/image");
            if (!image.isMissingNode() && !image.asText().isBlank()) {
                String text = image.asText();
                // 递归尝试从该字段中提取
                return extractBase64FromResponse(text);
            }
        } catch (Exception ignored) {
            // 解析失败则忽略，继续后续策略
        }

        // 6) 兜底：若未能识别，返回原始响应（上层会尝试解码并保存调试信息）
        System.out.println("   ⚠️ 未能从响应中明确提取 base64，返回原始内容供上层处理（长度: " + s.length() + "）");
        return s;
    }

    /**
     * 从prompt中提取base64图片数据
     */
    private String extractBase64FromPrompt(String prompt) {
        if (prompt == null || !prompt.contains("[Base64数据:")) {
            return null;
        }
        
        try {
            int startIdx = prompt.indexOf("[Base64数据: ") + "[Base64数据: ".length();
            int endIdx = prompt.indexOf("]", startIdx);
            
            if (startIdx > 0 && endIdx > startIdx) {
                String base64Data = prompt.substring(startIdx, endIdx).trim();
                System.out.println("   📊 提取到Base64数据，长度: " + base64Data.length() + " 字符");
                return base64Data;
            }
        } catch (Exception e) {
            System.err.println("   ⚠️  提取Base64数据失败: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * 从prompt中提取文本部分（去除base64数据）
     */
    private String extractTextFromPrompt(String prompt) {
        if (prompt == null) {
            return "";
        }
        
        // 移除[图片文件: xxx]和[Base64数据: xxx]部分
        String text = prompt.replaceAll("\\[图片文件:.*?\\]", "")
                           .replaceAll("\\[Base64数据:.*?\\]", "")
                           .trim();
        
        return text.isEmpty() ? "请分析并标注这张图片。" : text;
    }

    private boolean isNullLike(String s) {
        if (s == null) return true;
        String t = s.trim();
        return t.equalsIgnoreCase("null") || t.equalsIgnoreCase("none") || t.equalsIgnoreCase("nil") || t.isEmpty();
    }

    /**
     * 保存完整HTTP响应到 output/annotated-images/ 目录，便于排查
     */
    private void saveResponseForDebug(String respBody, String hint) {
        try {
            java.nio.file.Path dir = java.nio.file.Path.of("output", "annotated-images");
            if (!java.nio.file.Files.exists(dir)) java.nio.file.Files.createDirectories(dir);
            String safeHint = hint == null ? "" : hint.replaceAll("[^A-Za-z0-9_-]", "_");
            String fileName = "llm_response_" + System.currentTimeMillis() + (safeHint.isEmpty() ? "" : "_" + safeHint) + ".txt";
            java.nio.file.Path filePath = dir.resolve(fileName);
            java.nio.file.Files.writeString(filePath, respBody == null ? "" : respBody, StandardCharsets.UTF_8);
            System.out.println("   🔁 已保存完整响应到: " + filePath.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("   ⚠️ 保存LLM完整响应失败: " + e.getMessage());
        }
    }

    @Override
    public String generate(String prompt, String type) {
        System.out.println("   🌐 准备HTTP请求...");
        
        // 检查是否为图片生成API（豆包使用专门的端点，Gemini通过Cherry Studio也支持）
        boolean isDoubaoImageGeneration = apiBase.contains("/images/generations");
        
        // 检查prompt中是否包含图片数据（用于Gemini Vision分析）
        boolean hasImageData = prompt.contains("[Base64数据:");
        
        // 判断是否为图片标注任务（包含图片数据 + 标注提示词）
        boolean isImageAnnotationTask = hasImageData && (prompt.contains("逻辑节点图") || prompt.contains("标注"));
        
        ObjectNode payload = MAPPER.createObjectNode();
        payload.put("model", model);

        if (isDoubaoImageGeneration) {
            // 豆包图片生成API格式
            payload.put("prompt", prompt);
            payload.put("n", 1);
            payload.put("size", "2048x2048");
            payload.put("response_format", "b64_json");
            
            System.out.println("   🎨 豆包图片生成模式");
            System.out.println("   📡 API地址: " + apiBase);
            System.out.println("   🤖 模型: " + model);
            System.out.println("   📝 提示词长度: " + prompt.length() + " 字符");
        } else if (isImageAnnotationTask) {
            // Gemini 图片标注模式（通过Cherry Studio）
            // 使用聊天格式，但期望返回图片
            ArrayNode messages = payload.putArray("messages");
            
            // System message
            ObjectNode system = messages.addObject();
            system.put("role", "system");
            system.put("content", "你是一个图片标注助手。请在原图上绘制标注，并直接返回标注后的图片。");
            
            // User message with image
            ObjectNode user = messages.addObject();
            user.put("role", "user");
            
            // 提取base64图片数据和文本提示
            String base64Data = extractBase64FromPrompt(prompt);
            String textPrompt = extractTextFromPrompt(prompt);
            
            if (base64Data != null && !base64Data.isEmpty()) {
                // 使用content数组格式（文本+图片）
                ArrayNode contentArray = user.putArray("content");
                
                // 添加文本部分
                ObjectNode textPart = contentArray.addObject();
                textPart.put("type", "text");
                textPart.put("text", textPrompt);
                
                // 添加图片部分
                ObjectNode imagePart = contentArray.addObject();
                imagePart.put("type", "image_url");
                ObjectNode imageUrl = imagePart.putObject("image_url");
                imageUrl.put("url", "data:image/png;base64," + base64Data);
                
                System.out.println("   🖼️  Gemini图片标注模式（通过Cherry Studio）");
                System.out.println("   📡 API地址: " + apiBase);
                System.out.println("   🤖 模型: " + model);
                System.out.println("   📝 文本提示词长度: " + textPrompt.length() + " 字符");
                System.out.println("   🎨 图片Base64长度: " + base64Data.length() + " 字符");
            } else {
                user.put("content", prompt);
                System.out.println("   ⚠️  未找到有效图片数据，使用纯文本模式");
            }
            
            payload.put("temperature", temperature);
        } else {
            // 普通聊天API格式
            ArrayNode messages = payload.putArray("messages");
            ObjectNode system = messages.addObject();
            system.put("role", "system");
            system.put("content", "你是一个 DSL 生成助手，只输出 DSL 内容。");

            ObjectNode user = messages.addObject();
            user.put("role", "user");
            user.put("content", prompt);

            payload.put("temperature", temperature);
            
            System.out.println("   💬 聊天模式");
            System.out.println("   📡 API地址: " + apiBase);
            System.out.println("   🤖 模型: " + model);
            System.out.println("   🌡️  温度: " + temperature);
        }
        
        System.out.println("   📋 请求payload长度: " + payload.toString().length() + " 字符");

        try {
            // 使用配置的认证头和方案（默认 Authorization: Bearer）
            String headerValue = (authScheme == null || authScheme.isBlank()) ? apiKey : (authScheme + " " + apiKey);

            // 兼容：若用户在 llm-config.properties 里只填到 /compatible-mode/v1 或 /v1，则自动补全 OpenAI 兼容路径
            String requestUrl = apiBase;
            if (requestUrl != null) {
                String trimmed = requestUrl.trim();
                // DashScope OpenAI Compatible Mode
                if (trimmed.endsWith("/compatible-mode/v1")) {
                    trimmed = trimmed + "/chat/completions";
                }
                // OpenAI 风格 base
                if (trimmed.endsWith("/v1")) {
                    trimmed = trimmed + "/chat/completions";
                }
                requestUrl = trimmed;
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(requestUrl))
                    .header(authHeaderName, headerValue)
                    .header("Content-Type", "application/json")
                    .timeout(java.time.Duration.ofMinutes(5))
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8))
                    .build();

            System.out.println("   ⏳ 发送请求中...");
            System.out.println("   🔗 请求URL: " + requestUrl);
            System.out.println("   🔑 API Key前缀: " + (apiKey.length() > 8 ? apiKey.substring(0, 8) + "****" : "****"));

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            System.out.println("   ✅ 收到响应，状态码: " + response.statusCode());

            if (response.statusCode() >= 400) {
                System.err.println("   ❌ API请求失败: " + response.statusCode());
                System.err.println("   响应内容: " + response.body());
                throw new IllegalStateException("LLM API 请求失败: " + response.statusCode() + " -> " + response.body());
            }

            JsonNode root = MAPPER.readTree(response.body());
            
            // 对于图片标注任务，打印完整响应以便调试
            if (isImageAnnotationTask) {
                System.out.println("   🔍 完整响应内容（前1000字符）:");
                String responsePreview = response.body().substring(0, Math.min(1000, response.body().length()));
                System.out.println(responsePreview);
            }
            
            // 根据API类型处理不同的响应格式
            if (isDoubaoImageGeneration) {
                // 豆包图片生成API响应格式
                JsonNode data = root.at("/data");
                if (data.isMissingNode() || !data.isArray() || data.size() == 0) {
                    System.err.println("   ❌ 图片生成API返回格式错误");
                    System.err.println("   完整响应: " + response.body());
                    throw new IllegalStateException("图片生成API返回格式错误，请检查响应。");
                }

                JsonNode firstImage = data.get(0);

                // 处理base64格式的图片数据
                JsonNode b64Json = firstImage.get("b64_json");
                if (b64Json != null && !b64Json.asText().isBlank()) {
                    String result = b64Json.asText().trim();
                    System.out.println("   🖼️  收到base64图片数据，长度: " + result.length() + " 字符");
                    if (isNullLike(result)) {
                        System.out.println("   ⚠️  收到字面 'null'，视为无图片返回");
                        saveResponseForDebug(response.body(), "doubao_b64_json_null");
                        return null;
                    }
                    return result;
                }

                // 回退：尝试从完整响应体中提取 base64（有些服务会将图片嵌入不同字段或被转义）
                String fallback = extractBase64FromResponse(response.body());
                if (fallback != null && !fallback.isBlank() && !isNullLike(fallback) && fallback.length() > 200) {
                    System.out.println("   🔁 回退：从完整响应中提取到base64，长度: " + fallback.length());
                    return fallback;
                }

                // 备选方案：处理URL格式（注释掉，但保留代码）
                /*
                JsonNode imageUrl = firstImage.get("url");
                if (imageUrl != null && !imageUrl.asText().isBlank()) {
                    String result = imageUrl.asText().trim();
                    System.out.println("   🖼️  图片URL: " + result);
                    return result;
                }
                */

                System.err.println("   ❌ 图片数据为空");
                System.err.println("   完整响应: " + response.body());
                // 保存完整响应便于排查
                saveResponseForDebug(response.body(), "doubao_no_image");
                throw new IllegalStateException("图片生成失败，未返回图片数据。详情已保存到 output/annotated-images/ 下的调试文件。");
            } else {
                // 聊天API响应格式（包括Gemini图片标注）
                JsonNode content = root.at("/choices/0/message/content");

                // 对于图片标注任务，检查多个可能的字段
                if (isImageAnnotationTask) {
                    System.out.println("   🔍 检查响应结构...");

                    // 检查是否有 image 或 image_url 字段
                    JsonNode imageField = root.at("/choices/0/message/image");
                    JsonNode imageUrlField = root.at("/choices/0/message/image_url");
                    JsonNode dataField = root.at("/data");

                    if (!imageField.isMissingNode()) {
                        System.out.println("   ✅ 找到 image 字段");
                        String imageData = imageField.asText().trim();
                        if (!imageData.isEmpty()) {
                            String extracted = extractBase64FromResponse(imageData);
                            if (isNullLike(extracted)) {
                                System.out.println("   ⚠️ image 字段提取到 'null'，视为无图片返回");
                                saveResponseForDebug(response.body(), "image_field_null");
                                return null;
                            }
                            return extracted;
                        }
                    }

                    if (!imageUrlField.isMissingNode()) {
                        System.out.println("   ✅ 找到 image_url 字段");
                        String imageUrl = imageUrlField.asText().trim();
                        if (!imageUrl.isEmpty()) {
                            String extracted = extractBase64FromResponse(imageUrl);
                            if (isNullLike(extracted)) {
                                System.out.println("   ⚠️ image_url 字段提取到 'null'，视为无图片返回");
                                saveResponseForDebug(response.body(), "image_url_null");
                                return null;
                            }
                            return extracted;
                        }
                    }

                    if (!dataField.isMissingNode() && dataField.isArray() && dataField.size() > 0) {
                        System.out.println("   ✅ 找到 data 数组字段");
                        JsonNode firstData = dataField.get(0);
                        JsonNode b64 = firstData.get("b64_json");
                        if (b64 != null && !b64.asText().isBlank()) {
                            String val = b64.asText().trim();
                            if (isNullLike(val)) {
                                System.out.println("   ⚠️ data[0].b64_json 为 'null'，视为无图片返回");
                                saveResponseForDebug(response.body(), "data_b64_null");
                                return null;
                            }
                            return val;
                        }
                        JsonNode url = firstData.get("url");
                        if (url != null && !url.asText().isBlank()) {
                            String extracted = extractBase64FromResponse(url.asText().trim());
                            if (isNullLike(extracted)) {
                                System.out.println("   ⚠️ data[0].url 提取到 'null'，视为无图片返回");
                                saveResponseForDebug(response.body(), "data_url_null");
                                return null;
                            }
                            return extracted;
                        }
                    }

                    // 回退：尝试从完整响应体中提取 base64（有时嵌套在其他字段或被转义）
                    String fallback2 = extractBase64FromResponse(response.body());
                    if (fallback2 != null && !fallback2.isBlank() && !isNullLike(fallback2) && fallback2.length() > 200) {
                        System.out.println("   🔁 回退：从完整响应中提取到base64，长度: " + fallback2.length());
                        return fallback2;
                    }

                }

                if (content.isMissingNode() || content.asText().isBlank()) {
                    System.err.println("   ❌ API返回内容为空");
                    System.err.println("   完整响应: " + response.body());
                    saveResponseForDebug(response.body(), "content_empty");
                    throw new IllegalStateException("LLM API 返回为空，请检查模型或配额。完整响应已保存以便排查。");
                }

                String result = content.asText().trim();

                // 检查是否为图片标注任务的响应
                if (isImageAnnotationTask) {
                    System.out.println("   🖼️  Gemini图片标注响应，内容长度: " + result.length() + " 字符");

                    // 检查响应中是否包含base64图片数据
                    if (result.contains("base64,") || (result.matches("^[A-Za-z0-9+/]*={0,2}$") && result.length() > 1000)) {
                        System.out.println("   ✅ 检测到base64图片数据");
                        String extracted = extractBase64FromResponse(result);
                        if (isNullLike(extracted)) {
                            System.out.println("   ⚠️ 提取后为 'null'，视为无图片返回");
                            saveResponseForDebug(response.body(), "content_extracted_null");
                            return null;
                        }
                        return extracted;
                    } else {
                        System.out.println("   ⚠️  响应不是图片数据，可能是文本描述");
                        System.out.println("   响应前200字符: " + result.substring(0, Math.min(200, result.length())));
                    }
                }

                System.out.println("   📝 LLM返回内容长度: " + result.length() + " 字符");
                if (isNullLike(result)) {
                    System.out.println("   ⚠️ 返回内容为 'null'，视为无图片");
                    saveResponseForDebug(response.body(), "content_null");
                    return null;
                }
                return result;
            }
        } catch (java.net.ConnectException e) {
            System.err.println("   ❌ 连接失败: " + e.getMessage());
            System.err.println("   💡 请检查网络连接和API地址是否正确");

            throw new IllegalStateException("无法连接到API服务器: " + e.getMessage(), e);
        } catch (java.net.http.HttpTimeoutException e) {
            System.err.println("   ❌ 请求超时: " + e.getMessage());
            throw new IllegalStateException("API请求超时: " + e.getMessage(), e);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("   ❌ 网络请求异常: " + e.getMessage());
            System.err.println("   异常类型: " + e.getClass().getSimpleName());

            throw new IllegalStateException("调用 LLM API 失败: " + e.getMessage(), e);
        }
    }
}

