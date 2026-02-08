package io.papermc.paper;

import java.io.*;
import java.util.*;
import java.nio.file.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🦞 [OpenClaw] 查找代码结构...");
        try {
            String baseDir = "/home/container";
            
            System.out.println("\n📋 列出 openclaw dist 目录结构...");
            ProcessBuilder ls1 = new ProcessBuilder("find", baseDir + "/node_modules/openclaw/dist", "-type", "f", "-name", "*.js", "-path", "*openai*");
            ls1.inheritIO();
            ls1.start().waitFor();

            System.out.println("\n📋 列出 openclaw 主目录...");
            ProcessBuilder ls2 = new ProcessBuilder("ls", "-la", baseDir + "/node_modules/openclaw/dist/");
            ls2.inheritIO();
            ls2.start().waitFor();

            System.out.println("\n📋 搜索 OpenAI client 创建...");
            ProcessBuilder grep1 = new ProcessBuilder("sh", "-c", 
                "grep -rn 'new OpenAI' " + baseDir + "/node_modules/openclaw/dist/ 2>/dev/null | head -20");
            grep1.inheritIO();
            grep1.start().waitFor();

            System.out.println("\n📋 搜索 baseURL 或 base_url...");
            ProcessBuilder grep2 = new ProcessBuilder("sh", "-c",
                "grep -rn -i 'baseurl\\|base_url' " + baseDir + "/node_modules/openclaw/dist/ 2>/dev/null | head -20");
            grep2.inheritIO();
            grep2.start().waitFor();

            System.out.println("\n📋 搜索 llm 或 model provider...");
            ProcessBuilder grep3 = new ProcessBuilder("sh", "-c",
                "grep -rn 'createClient\\|getClient\\|llmClient' " + baseDir + "/node_modules/openclaw/dist/ 2>/dev/null | head -20");
            grep3.inheritIO();
            grep3.start().waitFor();

            // 保持进程运行
            System.out.println("\n✅ 搜索完成，请查看上面的输出");
            Thread.sleep(5000);
            
        } catch (Exception e) { e.printStackTrace(); }
    }
}
