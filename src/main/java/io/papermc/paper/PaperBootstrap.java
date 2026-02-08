package io.papermc.paper;

import java.io.*;
import java.util.*;
import java.nio.file.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🦞 [OpenClaw] 查找实际使用的 OpenAI SDK...");
        try {
            String baseDir = "/home/container";
            
            // ★★★ 在 openclaw 包内搜索所有 openai 目录 ★★★
            System.out.println("📝 在 openclaw 内搜索 openai...");
            ProcessBuilder find1 = new ProcessBuilder("sh", "-c",
                "find " + baseDir + "/node_modules/openclaw -type d -name 'openai' 2>/dev/null"
            );
            find1.inheritIO();
            find1.start().waitFor();

            // ★★★ 搜索 openclaw 中所有包含 api.openai.com 的文件 ★★★
            System.out.println("\n📝 在 openclaw 内搜索 api.openai.com...");
            ProcessBuilder grep1 = new ProcessBuilder("sh", "-c",
                "grep -rl 'api.openai.com' " + baseDir + "/node_modules/openclaw/ 2>/dev/null"
            );
            grep1.inheritIO();
            grep1.start().waitFor();

            // ★★★ 查看 openclaw 的 node_modules 目录 ★★★
            System.out.println("\n📋 openclaw 的 node_modules 目录:");
            ProcessBuilder ls1 = new ProcessBuilder("sh", "-c",
                "ls -la " + baseDir + "/node_modules/openclaw/node_modules/ 2>/dev/null | head -30"
            );
            ls1.inheritIO();
            ls1.start().waitFor();

            // ★★★ 查看 @mariozechner/pi-ai 的依赖 ★★★
            System.out.println("\n📋 pi-ai 的 node_modules 目录:");
            ProcessBuilder ls2 = new ProcessBuilder("sh", "-c",
                "ls -la " + baseDir + "/node_modules/@mariozechner/pi-ai/node_modules/ 2>/dev/null | head -20"
            );
            ls2.inheritIO();
            ls2.start().waitFor();

            // ★★★ 查找所有 openai 的 index.js ★★★
            System.out.println("\n📝 查找所有 openai/index.js:");
            ProcessBuilder find2 = new ProcessBuilder("sh", "-c",
                "find " + baseDir + "/node_modules -path '*/openai/index.js' 2>/dev/null"
            );
            find2.inheritIO();
            find2.start().waitFor();

            // ★★★ 查找所有 openai/client.js ★★★
            System.out.println("\n📝 查找所有 openai/client.js:");
            ProcessBuilder find3 = new ProcessBuilder("sh", "-c",
                "find " + baseDir + "/node_modules -path '*/openai/client.js' 2>/dev/null"
            );
            find3.inheritIO();
            find3.start().waitFor();

            System.out.println("\n✅ 搜索完成");
            Thread.sleep(3000);

        } catch (Exception e) { e.printStackTrace(); }
    }
}
