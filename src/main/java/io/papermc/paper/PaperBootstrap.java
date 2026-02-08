package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🦞 [OpenClaw] 查找 OpenAI 初始化代码...");
        try {
            String baseDir = "/home/container";
            
            // 查找 OpenClaw 主代码中的 OpenAI 初始化
            System.out.println("\n📋 搜索 dist 目录...");
            ProcessBuilder grep1 = new ProcessBuilder("grep", "-rn", "new OpenAI", baseDir + "/node_modules/openclaw/dist/");
            grep1.inheritIO();
            grep1.start().waitFor();

            System.out.println("\n📋 搜索 baseURL 配置...");
            ProcessBuilder grep2 = new ProcessBuilder("grep", "-rn", "baseURL", baseDir + "/node_modules/openclaw/dist/");
            grep2.inheritIO();
            grep2.start().waitFor();

            System.out.println("\n📋 搜索 providers 相关代码...");
            ProcessBuilder grep3 = new ProcessBuilder("grep", "-rn", "provider", baseDir + "/node_modules/openclaw/dist/providers/");
            grep3.inheritIO();
            grep3.start().waitFor();

            System.out.println("\n📋 列出 providers 目录...");
            ProcessBuilder ls = new ProcessBuilder("ls", "-la", baseDir + "/node_modules/openclaw/dist/providers/");
            ls.inheritIO();
            ls.start().waitFor();

            System.out.println("\n📋 查看 openai provider 文件...");
            ProcessBuilder cat = new ProcessBuilder("cat", baseDir + "/node_modules/openclaw/dist/providers/openai.js");
            cat.inheritIO();
            cat.start().waitFor();

        } catch (Exception e) { e.printStackTrace(); }
    }
}
