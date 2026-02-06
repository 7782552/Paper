package io.papermc.paper;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("═".repeat(60));
        System.out.println("🔍 OpenClaw 诊断工具");
        System.out.println("═".repeat(60));
        
        String baseDir = "/home/container";
        String nodeBin = baseDir + "/node-v22/bin/node";
        String ocBin = baseDir + "/node_modules/.bin/openclaw";
        
        Map<String, String> env = new HashMap<>();
        env.put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
        env.put("HOME", baseDir);
        
        try {
            // 1. 检查 Node 版本
            System.out.println("\n📌 [1] Node.js 版本:");
            System.out.println("-".repeat(40));
            runAndCapture(env, baseDir, nodeBin, "--version");
            
            // 2. 检查 OpenClaw 版本
            System.out.println("\n📌 [2] OpenClaw 版本:");
            System.out.println("-".repeat(40));
            runAndCapture(env, baseDir, nodeBin, ocBin, "--version");
            
            // 3. 检查 openclaw 帮助
            System.out.println("\n📌 [3] OpenClaw 可用命令:");
            System.out.println("-".repeat(40));
            runAndCapture(env, baseDir, nodeBin, ocBin, "--help");
            
            // 4. 检查 config 帮助
            System.out.println("\n📌 [4] OpenClaw config 命令帮助:");
            System.out.println("-".repeat(40));
            runAndCapture(env, baseDir, nodeBin, ocBin, "config", "--help");
            
            // 5. 检查 pairing 帮助
            System.out.println("\n📌 [5] OpenClaw pairing 命令帮助:");
            System.out.println("-".repeat(40));
            runAndCapture(env, baseDir, nodeBin, ocBin, "pairing", "--help");
            
            // 6. 检查 onboard 帮助
            System.out.println("\n📌 [6] OpenClaw onboard 命令帮助:");
            System.out.println("-".repeat(40));
            runAndCapture(env, baseDir, nodeBin, ocBin, "onboard", "--help");
            
            // 7. 列出 .openclaw 目录内容
            System.out.println("\n📌 [7] .openclaw 目录内容:");
            System.out.println("-".repeat(40));
            File openclawDir = new File(baseDir + "/.openclaw");
            if (openclawDir.exists()) {
                listDirectory(openclawDir, "");
            } else {
                System.out.println("   ❌ 目录不存在: " + openclawDir.getAbsolutePath());
            }
            
            // 8. 读取配置文件
            System.out.println("\n📌 [8] openclaw.json 配置文件内容:");
            System.out.println("-".repeat(40));
            File configFile = new File(baseDir + "/.openclaw/openclaw.json");
            if (configFile.exists()) {
                String content = new String(Files.readAllBytes(configFile.toPath()));
                System.out.println(content);
            } else {
                System.out.println("   ❌ 配置文件不存在");
            }
            
            // 9. 读取其他可能的配置文件
            System.out.println("\n📌 [9] 其他配置文件:");
            System.out.println("-".repeat(40));
            String[] possibleConfigs = {
                "/.openclaw/config.json",
                "/.openclaw/settings.json",
                "/.openclaw/channels.json",
                "/.openclaw/auth.json"
            };
            for (String cfg : possibleConfigs) {
                File f = new File(baseDir + cfg);
                if (f.exists()) {
                    System.out.println("\n   📄 " + cfg + ":");
                    String content = new String(Files.readAllBytes(f.toPath()));
                    System.out.println(content);
                }
            }
            
            // 10. 检查 npm 包信息
            System.out.println("\n📌 [10] OpenClaw 包信息:");
            System.out.println("-".repeat(40));
            File packageJson = new File(baseDir + "/node_modules/openclaw/package.json");
            if (packageJson.exists()) {
                String content = new String(Files.readAllBytes(packageJson.toPath()));
                // 只提取关键信息
                System.out.println(content);
            } else {
                // 尝试其他路径
                packageJson = new File(baseDir + "/node_modules/@anthropic-ai/claw/package.json");
                if (packageJson.exists()) {
                    String content = new String(Files.readAllBytes(packageJson.toPath()));
                    System.out.println(content);
                } else {
                    System.out.println("   找不到 package.json");
                }
            }
            
            // 11. 列出 node_modules/.bin 目录
            System.out.println("\n📌 [11] node_modules/.bin 可用命令:");
            System.out.println("-".repeat(40));
            File binDir = new File(baseDir + "/node_modules/.bin");
            if (binDir.exists()) {
                String[] bins = binDir.list();
                if (bins != null) {
                    Arrays.sort(bins);
                    for (String bin : bins) {
                        System.out.println("   - " + bin);
                    }
                }
            }
            
            // 12. 运行 openclaw config list
            System.out.println("\n📌 [12] OpenClaw 当前配置 (config list):");
            System.out.println("-".repeat(40));
            runAndCapture(env, baseDir, nodeBin, ocBin, "config", "list");
            
            // 13. 运行 openclaw doctor
            System.out.println("\n📌 [13] OpenClaw Doctor 诊断:");
            System.out.println("-".repeat(40));
            runAndCapture(env, baseDir, nodeBin, ocBin, "doctor");
            
            // 14. 检查 pairing list
            System.out.println("\n📌 [14] OpenClaw Pairing 列表:");
            System.out.println("-".repeat(40));
            runAndCapture(env, baseDir, nodeBin, ocBin, "pairing", "list");
            
            // 15. 环境变量
            System.out.println("\n📌 [15] 相关环境变量:");
            System.out.println("-".repeat(40));
            String[] envVars = {"HOME", "PATH", "GEMINI_API_KEY", "NODE_ENV"};
            for (String var : envVars) {
                String val = System.getenv(var);
                if (var.contains("KEY") || var.contains("TOKEN")) {
                    val = val != null ? val.substring(0, Math.min(10, val.length())) + "..." : "null";
                }
                System.out.println("   " + var + " = " + val);
            }
            
            System.out.println("\n" + "═".repeat(60));
            System.out.println("✅ 诊断完成！请将以上所有输出发给我");
            System.out.println("═".repeat(60));
            
            // 保持程序运行一会儿以便查看输出
            Thread.sleep(300000); // 5分钟
            
        } catch (Exception e) {
            System.err.println("❌ 诊断出错: " + e.getMessage());
            e.printStackTrace();
            try {
                Thread.sleep(300000);
            } catch (InterruptedException ie) {}
        }
    }
    
    static void runAndCapture(Map<String, String> env, String workDir, String... cmd) {
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.environment().putAll(env);
            pb.directory(new File(workDir));
            pb.redirectErrorStream(true);
            
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("   " + line);
            }
            p.waitFor();
        } catch (Exception e) {
            System.out.println("   ❌ 执行失败: " + e.getMessage());
        }
    }
    
    static void listDirectory(File dir, String indent) {
        File[] files = dir.listFiles();
        if (files != null) {
            Arrays.sort(files);
            for (File f : files) {
                if (f.isDirectory()) {
                    System.out.println(indent + "📁 " + f.getName() + "/");
                    listDirectory(f, indent + "   ");
                } else {
                    long size = f.length();
                    System.out.println(indent + "📄 " + f.getName() + " (" + size + " bytes)");
                }
            }
        }
    }
}
