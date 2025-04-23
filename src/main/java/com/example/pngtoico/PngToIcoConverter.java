package com.example.pngtoico;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Apache Commons Imaging Imports (需要将 commons-imaging JAR 添加到 classpath)
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.common.ImageBuilder;
import org.apache.commons.imaging.formats.ico.IcoImageParser;

public class PngToIcoConverter extends JFrame {

    // 支持的文件扩展名 (基于 Commons Imaging 可能支持的)
    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(".png", ".gif", ".bmp", ".jpg", ".jpeg", ".tiff");
    // ICO 分辨率
    private static final List<Dimension> ICO_RESOLUTIONS = Arrays.asList(
            new Dimension(256, 256), new Dimension(128, 128), new Dimension(96, 96),
            new Dimension(64, 64), new Dimension(48, 48), new Dimension(32, 32),
            new Dimension(24, 24), new Dimension(16, 16)
    );

    private JTextField inputPathField;
    private JTextField outputPathField;
    private JTextArea statusArea;
    private JButton convertButton;
    private File[] selectedInputFiles = null;
    private File selectedOutputDirectory = null;

    public PngToIcoConverter() {
        super("PNG/Image to ICO Converter (Java Swing)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null); // Center window

        // --- UI Components ---
        inputPathField = new JTextField(30);
        inputPathField.setEditable(false);
        JButton selectInputButton = new JButton("选择图片/文件夹...");

        outputPathField = new JTextField(30);
        outputPathField.setEditable(false);
        JButton selectOutputButton = new JButton("选择输出目录...");

        statusArea = new JTextArea(10, 50);
        statusArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(statusArea);

        convertButton = new JButton("开始转换");
        convertButton = new JButton("开始转换");
        convertButton.setEnabled(false); // Initially disabled

        // --- Layout ---
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("输入:"));
        inputPanel.add(inputPathField);
        inputPanel.add(selectInputButton);

        JPanel outputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        outputPanel.add(new JLabel("输出目录:"));
        outputPanel.add(outputPathField);
        outputPanel.add(selectOutputButton);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        controlPanel.add(convertButton);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(inputPanel);
        topPanel.add(outputPanel);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 5));
        contentPane.add(topPanel, BorderLayout.NORTH);
        contentPane.add(scrollPane, BorderLayout.CENTER);
        contentPane.add(controlPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        selectInputButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectInput();
            }
        });

        selectOutputButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectOutput();
            }
        });

        convertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startConversion();
            }
        });
    }

    private void selectInput() {
        JFileChooser fileChooser = new JFileChooser();
        // 添加文件过滤器
        javax.swing.filechooser.FileNameExtensionFilter filter = new javax.swing.filechooser.FileNameExtensionFilter(
                "支持的图片 (" + String.join(", ", SUPPORTED_EXTENSIONS) + ")",
                SUPPORTED_EXTENSIONS.stream().map(ext -> ext.substring(1)).toArray(String[]::new) // "png", "gif", etc.
        );
        fileChooser.addChoosableFileFilter(filter);
        fileChooser.setFileFilter(filter); // Set as default filter

        // Allow selecting files and directories
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(true); // Allow multiple file selection

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedInputFiles = fileChooser.getSelectedFiles(); // Can be files or a single directory
            if (selectedInputFiles != null && selectedInputFiles.length > 0) {
                 // Display selected path(s) - simplified for now
                 if (selectedInputFiles.length == 1) {
                     inputPathField.setText(selectedInputFiles[0].getAbsolutePath());
                 } else {
                     inputPathField.setText(selectedInputFiles.length + " 个文件已选择");
                 }
                 checkConversionReady();
            }
        }
    }

    private void selectOutput() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showSaveDialog(this); // Use showSaveDialog for selecting output dir conceptually
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedOutputDirectory = fileChooser.getSelectedFile();
            outputPathField.setText(selectedOutputDirectory.getAbsolutePath());
            checkConversionReady();
        }
    }

    // Returns true if ready, false otherwise. Also updates button state.
    private boolean checkConversionReady() {
        boolean ready = selectedInputFiles != null && selectedInputFiles.length > 0 && selectedOutputDirectory != null;
        convertButton.setEnabled(ready);
        return ready;
    }

    private void startConversion() {
        // Check if ready before proceeding
        if (!checkConversionReady()) {
             logStatus("错误：请先选择输入图片/文件夹和输出目录。");
             return;
        }

        statusArea.setText(""); // Clear previous status
        logStatus("开始转换...");
        convertButton.setEnabled(false); // Disable during conversion

        convertButton.setEnabled(false); // Disable during conversion

        // 使用 SwingWorker 在后台执行转换
        ConversionWorker worker = new ConversionWorker(selectedInputFiles, selectedOutputDirectory);
        worker.execute();
    }

    // SwingWorker for background processing
    private class ConversionWorker extends SwingWorker<Integer, String> {
        private final File[] inputFilesOrDirs;
        private final File outputDir;
        private int successCount = 0;
        private int failCount = 0;

        public ConversionWorker(File[] inputFilesOrDirs, File outputDir) {
            this.inputFilesOrDirs = inputFilesOrDirs;
            this.outputDir = outputDir;
        }

        @Override
        protected Integer doInBackground() throws Exception {
            List<File> filesToProcess = new ArrayList<>();

            // Collect all files to process (handle directory input)
            for (File input : inputFilesOrDirs) {
                if (isCancelled()) break;
                if (input.isDirectory()) {
                    publish("扫描文件夹: " + input.getName() + "...");
                    File[] dirFiles = input.listFiles();
                    if (dirFiles != null) {
                        for (File file : dirFiles) {
                             if (isCancelled()) break;
                            if (file.isFile() && isSupportedImageFile(file)) {
                                filesToProcess.add(file);
                            }
                        }
                    }
                } else if (input.isFile() && isSupportedImageFile(input)) {
                    filesToProcess.add(input);
                } else {
                     publish("跳过不支持的文件或类型: " + input.getName());
                }
            }

             if (filesToProcess.isEmpty()) {
                 publish("未找到支持的图片文件进行转换。");
                 return 0; // Indicate no files processed
             }

            publish("找到 " + filesToProcess.size() + " 个图片文件，开始转换...");

            // Process each file
            for (File inputFile : filesToProcess) {
                 if (isCancelled()) break;
                String outputFileName = getOutputFileName(inputFile.getName());
                File outputFile = new File(outputDir, outputFileName);
                publish("转换: " + inputFile.getName() + " -> " + outputFile.getName());
                try {
                    convertImage(inputFile, outputFile);
                    successCount++;
                } catch (Exception e) {
                    publish("  失败: " + inputFile.getName() + " - " + e.getMessage());
                    e.printStackTrace(); // Log detailed error to console
                    failCount++;
                }
            }
            return successCount;
        }

        @Override
        protected void process(List<String> chunks) {
            // Update status area on EDT
            for (String message : chunks) {
                logStatus(message);
            }
        }

        @Override
        protected void done() {
            try {
                if (isCancelled()) {
                    logStatus("转换已取消。");
                } else {
                    int processed = get(); // Get result from doInBackground
                    logStatus("--------------------");
                    logStatus("转换完成。成功: " + successCount + ", 失败: " + failCount);
                }
            } catch (InterruptedException | ExecutionException e) {
                logStatus("转换过程中发生错误: " + e.getMessage());
                e.printStackTrace();
            } finally {
                convertButton.setEnabled(true); // Re-enable button
            }
        }
    }

    // --- Actual Conversion Logic ---
    private void convertImage(File inputFile, File outputFile) throws ImagingException, IOException {
         // 1. 读取源图片
         BufferedImage sourceImage = Imaging.getBufferedImage(inputFile);
         if (sourceImage == null) {
             throw new IOException("无法读取图片文件: " + inputFile.getName());
         }

         // 2. 创建所需尺寸的 BufferedImage 列表 (Commons Imaging 需要这个)
         //    注意：直接缩放可能会损失质量，更高级的方法会使用多种缩放算法
         List<BufferedImage> images = new ArrayList<>();
         for (Dimension size : ICO_RESOLUTIONS) {
             // 创建目标尺寸的图像，保持透明度
             ImageBuilder imageBuilder = new ImageBuilder(size.width, size.height, true);
             Graphics2D g2d = imageBuilder.getGraphics2D();

             // 高质量缩放
             g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
             g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
             g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

             // 绘制缩放后的图像
             g2d.drawImage(sourceImage, 0, 0, size.width, size.height, null);
             g2d.dispose(); // 释放图形上下文资源

             images.add(imageBuilder.getBufferedImage());
         }

         // 3. 写入 ICO 文件
         try (OutputStream os = new FileOutputStream(outputFile)) {
             // 使用 IcoImageParser 来写入 ICO
             new IcoImageParser().writeImage(os, images, Collections.emptyMap());
         }
    }

    // Helper to check if a file is a supported image type by extension
    private boolean isSupportedImageFile(File file) {
        String name = file.getName().toLowerCase();
        for (String ext : SUPPORTED_EXTENSIONS) {
            if (name.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

     // Helper to generate .ico filename
     private String getOutputFileName(String inputFileName) {
        int dotIndex = inputFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return inputFileName.substring(0, dotIndex) + ".ico";
        } else {
            return inputFileName + ".ico"; // Should not happen with proper filtering
        }
    }

    private void logStatus(String message) {
        // Append message to the status area, ensuring it runs on the EDT
        SwingUtilities.invokeLater(() -> statusArea.append(message + "\n"));
    }


    public static void main(String[] args) {
        // Set Look and Feel to system default for better appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("无法设置系统外观: " + e.getMessage());
        }

        // Run the GUI on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new PngToIcoConverter().setVisible(true);
            }
        });
    }
}
