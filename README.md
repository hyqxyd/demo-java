# SpringBoot-Vue  (Java后端)

## 项目说明

本项目是一个基于SpringBoot和Vue.js的全栈示例项目的后端部分，实现了一个在线教育平台的核心功能。项目采用前后端分离架构，后端使用SpringBoot框架，前端使用Vue.js框架（前端代码在单独的仓库中）。

## 项目结构

```
src/main/java/cn/fighter3/
├── DemoJavaApplication.java        # 应用程序入口
├── config/                         # 配置类
├── controller/                     # 控制器
├── dto/                            # 数据传输对象
├── entity/                         # 实体类
├── exception/                      # 异常处理
├── mapper/                         # MyBatis Mapper接口
├── result/                         # 结果封装
├── service/                        # 服务接口
│   └── impl/                       # 服务实现类
└── vo/                             # 视图对象
```

## 技术栈

- Spring Boot 2.x
- MyBatis-Plus
- MySQL
- Lombok
- Maven

## 功能模块

- 用户管理：注册、登录、个人信息管理
- 课程管理：创建、更新、删除课程，学生加入/退出课程
- 主题管理：创建、更新、删除主题，调整主题顺序
- 问题管理：创建、更新、删除问题，调整问题顺序，分配问题给学生
- 学生答案管理：提交答案、批阅答案
- 学习记录管理：记录学习时间和进度

## 新版本项目

在 `newdemo`目录中包含了一个基于Spring AI框架的新版本项目，整合了原项目的功能并添加了AI功能支持。新版本支持多种AI模型，包括OpenAI、文心一言、通义千问和科大讯飞等。

## 使用方法

### 前提条件

- JDK 8或更高版本
- Maven 3.6或更高版本
- MySQL 5.7或更高版本

### 配置数据库

1. 创建MySQL数据库
2. 修改 `application.properties`中的数据库连接信息

### 构建和运行

```bash
# 构建项目
mvn clean package

# 运行项目
java -jar target/demo-java-0.0.1-SNAPSHOT.jar
```

或者使用Maven直接运行：

```bash
mvn spring-boot:run
```

## API文档

启动应用后，可以通过以下地址访问API文档：

```
http://localhost:8080/swagger-ui.html
```

## 注意事项

- 本项目仅供学习和参考使用
- 生产环境中请妥善保管数据库密码和API密钥
- 建议在使用前先阅读代码了解项目结构和功能

## 许可证

[MIT License](LICENSE)
