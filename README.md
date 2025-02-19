## 票务系统

## 项目结构
```
├── checkstyle  || -- # checkstyle代码格式检查组件
│   ├── ticket_service_checkstyle.xml  || -- # 检查规则配置
│   └── ticket_service_checkstyle_suppression.xml  || -- # 忽略规则配置
├── console-vue  || -- #  前端代码
├── dependencies  || -- #  全局依赖版本控制
│   └── pom.xml
├── format  || -- #  spotless代码格式化组件
│   ├── ticket_service_licenseHeader.txt  || -- #  开源协议头
│   └── ticket_service_spotless_formatter.xml  || -- #  格式化规则配置
├── frameworks  || -- #  基础架构组件库
│   ├── base  || -- #  顶层抽象基础组件 : 全局常量 初始化事件 jackson安全模式 单例对象池 自定义容器...
│   ├── cache  || -- #  缓存组件 : 缓存抽象 redis实例增强 配置布隆过滤器...
│   ├── common  || -- #  公共组件 : 枚举类 线程池快速消费 扩展拒绝策略 一些工具类...
│   ├── convention  || -- #  项目规约组件 : 异常码 异常体系 封装分页 响应体...
│   ├── database  || -- #  持久层组件 : 持久层基本属性 自定义元数据填充 分页转换工具 自定义分片算法 自定义id生成器...
│   ├── designpattern  || -- #  设计模式组件 : 封装Builder模式 责任链模式 策略模式...
│   ├── distributedid  || -- #  分布式id组件 : 雪花算法及其工具类 workid的获取...
│   ├── idempotent  || -- #  幂等组件 : ...
│   ├── log  || -- #  日志组件 : 日志注解 日志aop 日志信息实体...
│   ├── user  || -- #  用户基础组件 : JWT工具类 用户上下文 用户信息传输过滤器...
│   ├── web  || -- #  web组件 : 全局异常管理 响应对象构造器 DispatcherServlet预热...
│   └── pom.xml
├── resources  || -- # 数据库初始化资源
│   ├── data  || -- # 数据库数据初始化
│   └── db  || -- # 数据库初始化
├── web-resources  || -- # 前端资源
│   ├── nginx  || -- # 前端nginx压缩包
│   └── starter.txt  || -- # 前端控制台启动步骤
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
├── README.md
```