# Wisdom Hub | 智慧枢纽

> **English:** A minimalist yet powerful personal space designed for knowledge sedimentation and daily inspiration. It features a private "Garden" for your own growth and a public "Plaza" for community interaction.
>
> **日本語:** 知識の蓄積と日常のインスピレーションのために設計された、ミニマルでパワフルな個人用スペース。
>
> **中文：** 一个简约而不失强大的个人空间，专为知识沉淀与灵感记录而设计。包含用于自我成长的私密“花园”和用于交流互动的公共“广场”。

---

## 🌟 Features / 主要功能

- **Dual-Post System (双轨发布)**: 支持结构化的长篇专栏（Markdown）和轻量级的“碎碎念”九宫格动态。
- **Explore & Search (探索与检索)**: 具备广场时间线推流与全局关键字智能高亮搜索功能，搭配无感加载的丝滑分页。
- **Community Interaction (社区互动)**: 完善的帖子状态流转，支持点赞、个人收藏等互动操作，打造充满温度的沉淀空间。
- **Privacy Control (隐私管控)**: 精细的可见性设置，支持“个人花园”私密记录与“广场”公开分享的一键切换。
- **AI Integrated (智能驱动)**: 预留 DeepL 与 Ollama 接口，致力于支持智能内容创作、多语言翻译与学习辅助。
- **Modern Stack (现代架构)**: 基于 Spring Boot 3 与 Vue 3 的高性能全栈架构，结合柔和的“奶油绿”治愈系 UI 设计。

## 🛠️ Tech Stack / 技术栈

- **Backend**: Spring Boot 3.5.x, Java 17, MySQL 8.0, MyBatis, Redis
- **Frontend**: Vue 3, Vite, TypeScript, Element Plus, Marked.js
- **Cloud & Tools**: Aliyun OSS (Object Storage), JWT (Auth), Axios, QQ Mail Service (SMTP)

---

## 🚀 Quick Start / 快速启动

### 1. 后端配置 (Backend)
本项目采用 `dev` 环境隔离敏感信息，启动前请：
1. 进入 `wisdom-hub-server/src/main/resources` 目录。
2. 将 `application-dev.yml.example` 复制一份并重命名为 `application-dev.yml`。
3. **根据注释填写你的本地参数**：MySQL 地址、Redis 密码、QQ 邮箱授权码以及阿里云 OSS 凭证。
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

# 阿里云 OSS 配置 (图片上传必需)
aliyun:
  oss:
    endpoint: oss-cn-beijing.aliyuncs.com
    access-key-id: YOUR_ACCESS_KEY_ID
    access-key-secret: YOUR_ACCESS_KEY_SECRET
    bucket-name: YOUR_BUCKET_NAME

# 应用安全配置
app:
  security:
    # 临时邮箱黑名单（支持动态扩展）
    disposable-email-domains: >
      chacuo.net, mailinator.com, guerrillamail.com, tempmail.com
    verify-code:
      expire-minutes: 5  # 验证码有效期（分钟）
    rate-limit:
      email-interval-seconds: 60  # 同一邮箱发送间隔（秒）

# 预留给 AI 接口的配置
api:
  deepl:
    key: "YOUR_DEEPL_API_KEY"
  ollama:
    base-url: "http://localhost:11434"

