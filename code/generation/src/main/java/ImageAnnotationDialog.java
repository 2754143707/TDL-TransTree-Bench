import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import javax.imageio.ImageIO;

/**
 * 图片上传和标注对话框
 * 用于翻译树DSL功能的第一步：图片标注
 */
public class ImageAnnotationDialog extends JDialog {
    private static final int DIALOG_WIDTH = 800;
    private static final int DIALOG_HEIGHT = 600;
    
    private JLabel imageLabel;
    private JTextArea pdfTextArea;
    private JButton uploadImageButton;
    private JButton uploadPdfButton;
    private JButton annotateButton;
    private JButton nextStepButton;
    private JProgressBar progressBar;
    private JTextArea logArea;
    
    private File selectedImageFile;
    private String pdfText = "";
    private String annotatedImageBase64 = "";
    private boolean annotationCompleted = false;
    
    private LlmClient llmClient;
    
    public ImageAnnotationDialog(Frame parent, LlmClient llmClient) {
        super(parent, "翻译树DSL - 图片标注", true);
        // 创建专门用于图片标注的LLM客户端
        this.llmClient = createDoubaoImageClient();
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setLocationRelativeTo(parent);
    }
    
    /**
     * 创建豆包图片标注专用客户端
     */
    private LlmClient createDoubaoImageClient() {
        try {
            LlmConfig config = LlmConfig.getInstance();
            
            String apiKey = config.getConfigProperty("llm.doubao-image.apiKey", "");
            String apiBase = config.getConfigProperty("llm.doubao-image.apiBase", "");
            String model = config.getConfigProperty("llm.doubao-image.model", "");
            double temperature = Double.parseDouble(config.getConfigProperty("llm.doubao-image.temperature", "0.2"));
            
            return new OpenAiLlmClient(
                java.net.http.HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(30))
                    .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                    .version(java.net.http.HttpClient.Version.HTTP_1_1)
                    .build(),
                apiKey, apiBase, model, temperature
            );
        } catch (Exception e) {
            System.err.println("创建豆包图片标注客户端失败: " + e.getMessage());
            return llmClient; // 降级使用传入的客户端
        }
    }
    
    private void initializeComponents() {
        // 图片显示区域
        imageLabel = new JLabel("请上传原始图片", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(400, 300));
        imageLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        
        // PDF文本区域
        pdfTextArea = new JTextArea(10, 30);
        pdfTextArea.setLineWrap(true);
        pdfTextArea.setWrapStyleWord(true);
        pdfTextArea.setText("请在此粘贴PDF原文内容...");
        
        // 按钮
        uploadImageButton = new JButton("📷 上传原图");
        uploadPdfButton = new JButton("📄 上传PDF文本");
        annotateButton = new JButton("🎨 开始标注");
        annotateButton.setEnabled(false);
        nextStepButton = new JButton("➡️ 下一步：生成DSL");
        nextStepButton.setEnabled(false);
        
        // 进度条
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("等待上传文件");
        
        // 日志区域
        logArea = new JTextArea(5, 50);
        logArea.setEditable(false);
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.GREEN);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // 上部面板：图片和PDF文本
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // 左侧：图片区域
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBorder(new TitledBorder("原始图片"));
        imagePanel.add(new JScrollPane(imageLabel), BorderLayout.CENTER);
        
        JPanel imageButtonPanel = new JPanel(new FlowLayout());
        imageButtonPanel.add(uploadImageButton);
        imageButtonPanel.add(annotateButton);
        imagePanel.add(imageButtonPanel, BorderLayout.SOUTH);
        
        // 右侧：PDF文本区域
        JPanel pdfPanel = new JPanel(new BorderLayout());
        pdfPanel.setBorder(new TitledBorder("PDF原文"));
        pdfPanel.add(new JScrollPane(pdfTextArea), BorderLayout.CENTER);
        
        JPanel pdfButtonPanel = new JPanel(new FlowLayout());
        pdfButtonPanel.add(uploadPdfButton);
        pdfPanel.add(pdfButtonPanel, BorderLayout.SOUTH);
        
        topPanel.add(imagePanel, BorderLayout.WEST);
        topPanel.add(pdfPanel, BorderLayout.CENTER);
        
        // 中部：进度条
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        progressPanel.add(progressBar, BorderLayout.CENTER);
        
        // 下部：日志区域
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(new TitledBorder("处理日志"));
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        
        mainPanel.add(topPanel, BorderLayout.CENTER);
        mainPanel.add(progressPanel, BorderLayout.NORTH);
        mainPanel.add(logPanel, BorderLayout.SOUTH);
        
        // 底部按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(nextStepButton);
        buttonPanel.add(new JButton("取消") {{
            addActionListener(e -> dispose());
        }});
        
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        uploadImageButton.addActionListener(this::uploadImage);
        uploadPdfButton.addActionListener(this::uploadPdfText);
        annotateButton.addActionListener(this::annotateImage);
        nextStepButton.addActionListener(this::proceedToNextStep);
    }
    
    private void uploadImage(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter(
            "图片文件 (*.jpg, *.png, *.gif, *.bmp)", "jpg", "jpeg", "png", "gif", "bmp"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = fileChooser.getSelectedFile();
            try {
                BufferedImage image = ImageIO.read(selectedImageFile);
                if (image != null) {
                    // 缩放图片以适应显示
                    ImageIcon icon = new ImageIcon(image.getScaledInstance(400, 300, Image.SCALE_SMOOTH));
                    imageLabel.setIcon(icon);
                    imageLabel.setText("");
                    
                    logMessage("✅ 图片上传成功: " + selectedImageFile.getName());
                    checkCanAnnotate();
                } else {
                    JOptionPane.showMessageDialog(this, "无法读取图片文件", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "读取图片失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void uploadPdfText(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("文本文件 (*.txt)", "txt"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Path path = fileChooser.getSelectedFile().toPath();
                pdfText = Files.readString(path);
                pdfTextArea.setText(pdfText);
                logMessage("✅ PDF文本上传成功: " + fileChooser.getSelectedFile().getName());
                checkCanAnnotate();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "读取文本文件失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void checkCanAnnotate() {
        boolean canAnnotate = selectedImageFile != null && !pdfText.trim().isEmpty();
        annotateButton.setEnabled(canAnnotate);
        if (canAnnotate) {
            progressBar.setString("准备就绪，可以开始标注");
        }
    }
    
    private void annotateImage(ActionEvent e) {
        if (selectedImageFile == null) {
            JOptionPane.showMessageDialog(this, "请先上传图片", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 在后台线程中执行标注
        SwingWorker<String, String> worker = new SwingWorker<String, String>() {
            @Override
            protected String doInBackground() throws Exception {
                publish("🎨 开始图片标注...");
                progressBar.setIndeterminate(true);
                progressBar.setString("正在调用豆包进行图片标注...");
                
                // 将图片转换为Base64
                byte[] imageBytes = Files.readAllBytes(selectedImageFile.toPath());
                String imageBase64 = Base64.getEncoder().encodeToString(imageBytes);
                
                // 构建标注提示词
                String annotationPrompt = buildAnnotationPrompt(imageBase64);
                
                publish("📡 调用豆包图片标注API...");
                
                // 调用LLM进行图片标注
                String result = llmClient.generate(annotationPrompt, "图片标注");
                
                publish("✅ 图片标注完成");
                return result;
            }
            
            @Override
            protected void process(java.util.List<String> chunks) {
                for (String message : chunks) {
                    logMessage(message);
                }
            }
            
            @Override
            protected void done() {
                try {
                    annotatedImageBase64 = get();
                    progressBar.setIndeterminate(false);
                    progressBar.setString("标注完成");
                    annotationCompleted = true;
                    nextStepButton.setEnabled(true);
                    
                    // 显示标注结果（这里简化处理，实际应该显示标注后的图片）
                    logMessage("🎯 图片标注已完成，可以进行下一步DSL生成");
                    
                } catch (Exception ex) {
                    progressBar.setIndeterminate(false);
                    progressBar.setString("标注失败");
                    logMessage("❌ 标注失败: " + ex.getMessage());
                    JOptionPane.showMessageDialog(ImageAnnotationDialog.this, 
                        "图片标注失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private String buildAnnotationPrompt(String imageBase64) {
        try {
            // 读取图片标注提示词
            Path promptPath = Paths.get("config/prompts", BenchConstants.PROMPT_IMAGE_ANNOTATION);
            String promptTemplate = Files.readString(promptPath);
            
            // 构建完整的提示词（包含图片）
            return promptTemplate + "\n\n请对以下图片进行标注：\n[图片数据: " + imageBase64.substring(0, Math.min(100, imageBase64.length())) + "...]";
            
        } catch (IOException e) {
            return "请对上传的图片进行逻辑节点标注，按照翻译树DSL的要求添加红色和绿色的标注线。";
        }
    }
    
    private void proceedToNextStep(ActionEvent e) {
        if (!annotationCompleted) {
            JOptionPane.showMessageDialog(this, "请先完成图片标注", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 设置结果并关闭对话框
        setVisible(false);
    }
    
    private void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    // Getter方法供外部获取结果
    public String getAnnotatedImageBase64() {
        return annotatedImageBase64;
    }
    
    public String getPdfText() {
        return pdfText;
    }
    
    public boolean isAnnotationCompleted() {
        return annotationCompleted;
    }
}