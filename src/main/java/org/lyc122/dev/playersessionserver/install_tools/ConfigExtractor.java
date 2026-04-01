package org.lyc122.dev.playersessionserver.install_tools;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ConfigExtractor {
    /**
     * 从JAR中提取配置文件到外部config目录
     */
    public static void extractConfigFromJar(String RESOURCE_PATH, String TARGET_DIR, String TARGET_FILE_NAME) throws IOException {

        // 构建目标文件路径
        File targetDir = new File("./" + TARGET_DIR);
        File targetFile = new File(targetDir, TARGET_FILE_NAME);

        // 如果目标文件已存在，询问是否覆盖
        if (targetFile.exists()) {
            System.out.println("Config already exists: " + targetFile.getAbsolutePath());
            return;
        }

        // 确保目标目录存在
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new IOException("Cannot create directory: " + targetDir.getAbsolutePath());
        }

        // 读取资源文件
        String content = readResourceFromClasspath(RESOURCE_PATH);
        if (content == null) {
            throw new FileNotFoundException("Cannot find resource in Jar file: " + RESOURCE_PATH);
        }

        // 写入目标文件
        writeToFile(targetFile, content);
        System.out.println("Config file created: " + targetFile.getAbsolutePath());
    }

    /**
     * 从类路径读取资源文件
     */
    private static String readResourceFromClasspath(String RESOURCE_PATH) throws IOException {
        // 去掉开头的斜杠（如果需要）
        String path = RESOURCE_PATH.substring(1);

        InputStream inputStream;

        // 方法1: 通过当前线程的ClassLoader
        inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(path);

        // 方法2: 如果方法1失败，尝试通过当前类的ClassLoader
        if (inputStream == null) {
            inputStream = ConfigExtractor.class.getClassLoader().getResourceAsStream(path);
        }

        // 方法3: 如果方法2失败，尝试通过当前类的资源
        if (inputStream == null) {
            inputStream = ConfigExtractor.class.getResourceAsStream(RESOURCE_PATH);
        }

        if (inputStream == null) {
            return null;
        }

        // 读取输入流
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }

            return content.toString();
        }
    }


    /**
     * 将内容写入文件
     */
    private static void writeToFile(File file, String content) throws IOException {
        // 确保父目录存在
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Cannot create directory: " + parentDir.getAbsolutePath());
            }
        }

        // 写入文件
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write(content);
        }
    }

}