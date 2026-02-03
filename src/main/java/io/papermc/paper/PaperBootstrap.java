package io.papermc.paper;
import java.io.*;
import java.nio.file.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🔎 [OpenClaw] 正在读取核心 Schema 源码，直接看它是怎么校验 Telegram 的...");
        try {
            String openclawDir = "/home/container/openclaw";
            // 这是定义所有 Key 和数据类型的地方
            File schemaFile = new File(openclawDir, "dist/config/schema.js");
            
            if (schemaFile.exists()) {
                String content = new String(Files.readAllBytes(schemaFile.toPath()));
                // 我们直接搜 telegram 和 model 相关的代码块
                System.out.println("\n--- 源码片段开始 ---");
                System.out.println(content);
                System.out.println("\n--- 源码片段结束 ---");
            } else {
                System.out.println("❌ 错误：找不到 schema.js 文件，请确认路径。");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
