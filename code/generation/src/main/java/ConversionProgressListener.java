/**
 * 转换进度监听器接口
 * 用于实时报告转换过程中的各个步骤
 */
public interface ConversionProgressListener {

    /**
     * 报告日志信息
     */
    void onLog(String message);

    /**
     * 报告开始新的尝试
     */
    void onAttemptStart(int attemptNumber, int maxAttempts, boolean isRetry);

    /**
     * 报告 LLM 调用开始
     */
    void onLlmCallStart(String llmName);

    /**
     * 报告 LLM 调用完成
     */
    void onLlmCallComplete(String rawDsl, long durationMs);

    /**
     * 报告 DSL 清理完成
     */
    void onDslCleaned(String cleanedDsl);

    /**
     * 报告解析开始
     */
    void onParseStart();

    /**
     * 报告解析完成
     */
    void onParseComplete(ParseResult result);

    /**
     * 报告最终 DSL 内容
     */
    void onDslGenerated(String dsl, int attemptNumber);

    /**
     * 报告错误信息
     */
    void onError(String error);

    /**
     * 报告成功
     */
    void onSuccess(int attemptNumber, String dslPath, String svgPath);

    /**
     * 报告失败
     */
    void onFailure(int totalAttempts, String reason);
}

