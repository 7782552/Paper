package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🦞 [OpenClaw] 正在配置...");
        try {
            String baseDir = "/home/container";
            String nodeBin = baseDir + "/node-v22/bin/node";
            String ocBin = baseDir + "/node_modules/.bin/openclaw";
            
            String geminiApiKey = "AIzaSyCpolv3ZpSbdc9cTHlCqbURbdDhppxQ_90";
            String telegramToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
            String telegramUserId = "660059245";  // 你的 Telegram 用户 ID

            Map<String, String> env = new HashMap<>();
            env.put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
            env.put("HOME", baseDir);
            env.put("GEMINI_API_KEY", geminiApiKey);

            // 0. 删除 Telegram Webhook
            System.out.println("🗑️ 删除 Telegram Webhook...");
            URL url = new URL("https://api.telegram.org/bot" + telegramToken + "/deleteWebhook");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            System.out.println("   响应: " + conn.getResponseCode());

            // 1. 检查配置文件是否存在
            File configFile = new File(baseDir + "/.openclaw/openclaw.json");
            File openclawDir = new File(baseDir + "/.openclaw");
            
            if (!openclawDir.exists()) {
                openclawDir.mkdirs();
            }

            // 2. 如果配置不存在，先运行 onboard 创建基础结构
            if (!configFile.exists()) {
                System.out.println("📝 首次运行 onboard...");
                ProcessBuilder onboardPb = new ProcessBuilder(
                    nodeBin, ocBin, "onboard",
                    "--non-interactive",
                    "--accept-risk",
                    "--mode", "local",
                    "--auth-choice", "gemini-api-key",
                    "--gemini-api-key", geminiApiKey,
                    "--gateway-port", "18789",
                    "--gateway-bind", "lan",
                    "--gateway-auth", "token",
                    "--gateway-token", "admin123",
                    "--skip-daemon",
                    "--skip-channels",
                    "--skip-skills",
                    "--skip-health",
                    "--skip-ui"
                );
                onboardPb.environment().putAll(env);
                onboardPb.directory(new File(baseDir));
                onboardPb.inheritIO();
                onboardPb.start().waitFor();
                Thread.sleep(2000);
            }

            // 3. 【关键修复】直接写入正确的配置文件
            System.out.println("📝 写入正确的配置...");
            String correctConfig = createCorrectConfig(geminiApiKey, telegramToken, telegramUserId);
            Files.write(configFile.toPath(), correctConfig.getBytes());
            System.out.println("✅ 配置文件已更新");

            // 4. 验证配置
            System.out.println("\n📋 当前配置:");
            System.out.println(new String(Files.readAllBytes(configFile.toPath())));

            // 5. 创建/更新 telegram-pairing.json 添加已批准用户
            System.out.println("\n📝 设置 Telegram 用户预授权...");
            File credentialsDir = new File(baseDir + "/.openclaw/credentials");
            if (!credentialsDir.exists()) {
                credentialsDir.mkdirs();
            }
            
            File pairingFile = new File(credentialsDir, "telegram-pairing.json");
            String pairingJson = "{\n" +
                "  \"approved\": {\n" +
                "    \"" + telegramUserId + "\": {\n" +
                "      \"userId\": " + telegramUserId + ",\n" +
                "      \"approvedAt\": \"" + java.time.Instant.now().toString() + "\",\n" +
                "      \"source\": \"bootstrap\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"pending\": {}\n" +
                "}";
            Files.write(pairingFile.toPath(), pairingJson.getBytes());
            System.out.println("✅ 用户 " + telegramUserId + " 已预授权");

            // 6. 运行 doctor 检查
            System.out.println("\n🔧 运行 doctor...");
            runCommand(env, baseDir, nodeBin, ocBin, "doctor");

            // 7. 启动 n8n
            System.out.println("\n🚀 启动 n8n (端口 30196)...");
            File n8nDir = new File(baseDir + "/.n8n");
            if (!n8nDir.exists()) {
                n8nDir.mkdirs();
            }

            ProcessBuilder n8nPb = new ProcessBuilder(
                nodeBin,
                "--max-old-space-size=2048",
                baseDir + "/node_modules/.bin/n8n",
                "start"
            );
            n8nPb.environment().putAll(env);
            n8nPb.environment().put("N8N_PORT", "30196");
            n8nPb.environment().put("N8N_HOST", "0.0.0.0");
            n8nPb.environment().put("N8N_SECURE_COOKIE", "false");
            n8nPb.environment().put("N8N_USER_FOLDER", baseDir + "/.n8n");
            n8nPb.environment().put("N8N_DIAGNOSTICS_ENABLED", "false");
            n8nPb.environment().put("N8N_VERSION_NOTIFICATIONS_ENABLED", "false");
            n8nPb.environment().put("N8N_HIRING_BANNER_ENABLED", "false");
            n8nPb.environment().put("N8N_PERSONALIZATION_ENABLED", "false");
            n8nPb.environment().put("N8N_TEMPLATES_ENABLED", "false");
            n8nPb.environment().put("N8N_LICENSE_AUTO_RENEW_ENABLED", "false");
            n8nPb.environment().put("N8N_PAYLOAD_SIZE_MAX", "64");
            n8nPb.environment().put("EXECUTIONS_DATA_SAVE_ON_ERROR", "none");
            n8nPb.environment().put("EXECUTIONS_DATA_SAVE_ON_SUCCESS", "none");
            n8nPb.directory(new File(baseDir));
            n8nPb.inheritIO();
            n8nPb.start();

            System.out.println("⏳ 等待 n8n 启动...");
            Thread.sleep(8000);

            // 8. 启动 Gateway
            System.out.println("\n🚀 启动 OpenClaw Gateway + Telegram...");
            System.out.println("═".repeat(50));
            System.out.println("📱 Telegram 用户 " + telegramUserId + " 已预授权");
            System.out.println("🤖 模型: google/gemini-2.0-flash");
            System.out.println("🌐 Gateway: ws://0.0.0.0:18789");
            System.out.println("═".repeat(50));
            
            ProcessBuilder gatewayPb = new ProcessBuilder(
                nodeBin, ocBin, "gateway",
                "--port", "18789",
                "--bind", "lan",
                "--token", "admin123",
                "--verbose"
            );
            gatewayPb.environment().putAll(env);
            gatewayPb.directory(new File(baseDir));
            gatewayPb.inheritIO();
            gatewayPb.start().waitFor();

        } catch (Exception e) {
            System.err.println("❌ 错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 创建正确的配置文件
     */
    static String createCorrectConfig(String geminiApiKey, String telegramToken, String telegramUserId) {
        return "{\n" +
            "  \"meta\": {\n" +
            "    \"lastTouchedVersion\": \"2026.2.3-1\",\n" +
            "    \"lastTouchedAt\": \"" + java.time.Instant.now().toString() + "\"\n" +
            "  },\n" +
            "  \"wizard\": {\n" +
            "    \"lastRunAt\": \"" + java.time.Instant.now().toString() + "\",\n" +
            "    \"lastRunVersion\": \"2026.2.3-1\",\n" +
            "    \"lastRunCommand\": \"bootstrap\",\n" +
            "    \"lastRunMode\": \"local\"\n" +
            "  },\n" +
            "  \"auth\": {\n" +
            "    \"profiles\": {\n" +
            "      \"google:default\": {\n" +
            "        \"provider\": \"google\",\n" +
            "        \"mode\": \"api_key\",\n" +
            "        \"apiKey\": \"" + geminiApiKey + "\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"agents\": {\n" +
            "    \"defaults\": {\n" +
            "      \"model\": {\n" +
            "        \"primary\": \"google/gemini-2.0-flash\"\n" +
            "      },\n" +
            "      \"workspace\": \"/home/container/.openclaw/workspace\",\n" +
            "      \"compaction\": {\n" +
            "        \"mode\": \"safeguard\"\n" +
            "      },\n" +
            "      \"maxConcurrent\": 4,\n" +
            "      \"subagents\": {\n" +
            "        \"maxConcurrent\": 8\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"messages\": {\n" +
            "    \"ackReactionScope\": \"group-mentions\"\n" +
            "  },\n" +
            "  \"commands\": {\n" +
            "    \"native\": \"auto\",\n" +
            "    \"nativeSkills\": \"auto\"\n" +
            "  },\n" +
            "  \"channels\": {\n" +
            "    \"telegram\": {\n" +
            "      \"dmPolicy\": \"allowlist\",\n" +
            "      \"botToken\": \"" + telegramToken + "\",\n" +
            "      \"groupPolicy\": \"allowlist\",\n" +
            "      \"streamMode\": \"partial\",\n" +
            "      \"allowlist\": [" + telegramUserId + "]\n" +
            "    }\n" +
            "  },\n" +
            "  \"gateway\": {\n" +
            "    \"port\": 18789,\n" +
            "    \"mode\": \"local\",\n" +
            "    \"bind\": \"lan\",\n" +
            "    \"auth\": {\n" +
            "      \"mode\": \"token\",\n" +
            "      \"token\": \"admin123\"\n" +
            "    },\n" +
            "    \"tailscale\": {\n" +
            "      \"mode\": \"off\",\n" +
            "      \"resetOnExit\": false\n" +
            "    }\n" +
            "  },\n" +
            "  \"plugins\": {\n" +
            "    \"entries\": {\n" +
            "      \"telegram\": {\n" +
            "        \"enabled\": true\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
    }

    static void runCommand(Map<String, String> env, String workDir, String... cmd) throws Exception {
        System.out.println("   执行: " + String.join(" ", Arrays.copyOfRange(cmd, 0, Math.min(cmd.length, 4))) + "...");
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.environment().putAll(env);
        pb.directory(new File(workDir));
        pb.inheritIO();
        pb.start().waitFor();
    }
}
