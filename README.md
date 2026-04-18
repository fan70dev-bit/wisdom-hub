# Wisdom Hub | 智慧枢纽

> **English:** A minimalist yet powerful personal space designed for knowledge sedimentation and daily inspiration. It features a private "Garden" for your own growth and a public "Plaza" for community interaction.
>
> **日本語:** 知識の蓄積と日常のインスピレーションのために設計された、ミニマルでパワフルな個人用スペース。
>
> **中文：** 一个简约而不失强大的个人空间，专为知识沉淀与灵感记录而设计。包含用于自我成长的私密“花园”和用于交流互动的公共“广场”。

---

## 🌟 Features / 主要功能

- **Dual-Post System**: 支持结构化的长篇博客和轻量级的“碎碎念”（动态）。
- **Privacy Control**: 精细的可见性设置，支持“个人花园”私密记录与“广场”公开分享。
- **AI Integrated**: 预留 DeepL 与 Ollama 接口，支持智能内容创作与多语言翻译。
- **Modern Stack**: 基于 Spring Boot 3 与 Vue 3 的高性能全栈架构。
- **Secure Auth**: 严谨的 QQ 邮箱验证码注册/登录流程，集成 JWT 身份验证。

## 🛠️ Tech Stack / 技术栈

- **Backend**: Spring Boot 3.5.x, Java 17, MySQL 8.0, MyBatis, Redis
- **Frontend**: Vue 3, Vite, TypeScript, Element Plus
- **Tools**: JWT (Auth), Axios, QQ Mail Service (SMTP)

---

## 🚀 Quick Start / 快速启动

### 1. 后端配置 (Backend)
本项目采用 `dev` 环境隔离敏感信息，启动前请：
1. 进入 `wisdom-hub-server/src/main/resources` 目录。
2. 将 `application-dev.yml.example` 复制一份并重命名为 `application-dev.yml`。
3. **根据注释填写你的本地参数**：MySQL 地址、Redis 地址及 QQ 邮箱授权码。
4. 运行 `WisdomHubApplication.java`。

### 2. 前端配置 (Frontend)
1. 进入 `wisdom-hub-web` 目录。
2. 安装依赖：`npm install`
3. 启动开发服务器：`npm run dev`

---

## ⚙️ Configuration Template / 配置模板示例

`application-dev.yml` 的核心结构参考如下：

```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/wisdom_hub?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true
    username: root
    password: YOUR_DATABASE_PASSWORD_HERE
    driver-class-name: com.mysql.cj.jdbc.Driver


  # Redis配置
  data:
    redis:
      host: localhost
      port: 6379
      password: YOUR_REDIS_PASSWORD_HERE
      database: 0
      timeout: 5000
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
          max-wait: 3000

  # 邮箱配置（QQ邮箱SMTP）
  mail:
    host: smtp.qq.com
    port: 587
    username: YOUR_EMAIL@qq.com  # 你的QQ邮箱
    password: YOUR_SMTP_AUTH_CODE  # QQ邮箱授权码（非QQ密码）
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
          ssl:
            enable: false
    default-encoding: UTF-8

# 应用安全配置
app:
  security:
    # 临时邮箱黑名单（支持动态扩展）
    disposable-email-domains: >
      chacuo.net,
      bccto.me,
      mailinator.com,
      guerrillamail.com,
      10minutemail.com,
      tempmail.com,
      throwaway.email,
      yopmail.com,
      maildrop.cc,
      sharklasers.com,
      getnada.com,
      temp-mail.org

    # 验证码配置
    verify-code:
      expire-minutes: 5  # 验证码有效期（分钟）

    # 频率限制配置
    rate-limit:
      email-interval-seconds: 60  # 同一邮箱发送间隔（秒）
      ip-daily-limit: 10  # 同一IP每日限制次数

# 预留给 AI 接口的配置
api:
  deepl:
    key: "你的DeepL_API_Key"
  ollama:
    base-url: "http://localhost:11434"

