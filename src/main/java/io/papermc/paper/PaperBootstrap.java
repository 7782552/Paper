// 3. 写入“极简主义”JSON —— 仅保留它认可的骨架
            System.out.println("📝 阶段 3：正在写入极简引导配置 (剔除争议字段)...");
            String configJson = "{"
                + "\"meta\":{\"lastTouchedVersion\":\"2026.2.1\"},"
                + "\"gateway\":{"
                    + "\"port\":" + internalPort + ","
                    + "\"mode\":\"local\","
                    + "\"bind\":\"loopback\","
                    + "\"auth\":{\"mode\":\"token\",\"token\":\"" + gatewayToken + "\"}"
                + "},"
                + "\"plugins\":{"
                    + "\"enabled\":[\"telegram\"]" // 2026 版可能改成了这种数组模式，或者只需声明启用
                + "}"
            + "}";
            Files.write(Paths.get(baseDir + "/.openclaw/openclaw.json"), configJson.getBytes());

            // 4. (隧道部分保持不变...)

            // 5. 启动 OpenClaw：通过环境变量强行灌入 Token
            System.out.println("🚀 阶段 4：正在通过环境变量强灌 Token...");
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node",
                "dist/index.js", "gateway", 
                "--port", String.valueOf(internalPort), 
                "--force"
            );
            
            pb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            
            // --- 强灌开始 ---
            // 2026 版为了防止字段冲突，通常会优先读取环境变量
            env.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            env.put("OPENCLAW_PLUGINS_TELEGRAM_BOT_TOKEN", botToken); // 备选变量名
            env.put("TELEGRAM_BOT_TOKEN", botToken); // 最原始的变量名
            
            env.put("OPENCLAW_GATEWAY_TOKEN", gatewayToken);
            env.put("NODE_ENV", "production");

            pb.inheritIO();
            pb.start().waitFor();
