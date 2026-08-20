# 🏛️ التقرير الشامل والمفصل لمنظومة التوظيف الذكية (HR Recruitment Microservices Platform)

---

## 📑 الفهرس العام للتقرير
1. [المقدمة والرؤية المعمارية للمنظومة (Executive Architectural Overview)](#1-المقدمة-والرؤية-المعمارية-للمنظومة)
2. [المكدس التكنولوجي والأدوات المستخدمة (Tech Stack & Tools)](#2-المكدس-التكنولوجي-والأدوات-المستخدمة)
3. [التشريح التفصيلي للخدمات المصغرة الستة (The 6 Microservices Breakdown)](#3-التشريح-التفصيلي-للخدمات-المصغرة-الستة)
   - [3.1 خدمة المصادقة والهوية (Auth Service - Port 8081)](#31-خدمة-المصادقة-والهوية-auth-service---port-8081)
   - [3.2 خدمة الوظائف (Job Service - Port 8082)](#32-خدمة-الوظائف-job-service---port-8082)
   - [3.3 خدمة المرشحين (Candidate Service - Port 8083)](#33-خدمة-المرشحين-candidate-service---port-8083)
   - [3.4 خدمة طلبات التوظيف (Application Service - Port 8084)](#34-خدمة-طلبات-التوظيف-application-service---port-8084)
   - [3.5 خدمة الذكاء الاصطناعي ومعالجة السير الذاتية (AI Service - Port 8085)](#35-خدمة-الذكاء-الاصطناعي-ومعالجة-السير-الذاتية-ai-service---port-8085)
   - [3.6 خدمة الإشعارات والبريد (Notification Service - Port 8086)](#36-خدمة-الإشعارات-والبريد-notification-service---port-8086)
4. [معمارية وهندسة قواعد البيانات (Database Architecture & Data Models)](#4-معمارية-وهندسة-قواعد-البيانات)
5. [طبقة الحماية والأمان المتقدم (Security & Identity Architecture)](#5-طبقة-الحماية-والأمان-المتقدم)
6. [خوارزميات الذكاء الاصطناعي واستخراج النصوص (AI Algorithms & OCR Engine)](#6-خوارزميات-الذكاء-الاصطناعي-واستخراج-النصوص)
7. [العمليات والنشر والإدارة (DevOps, Docker & Execution Scripts)](#7-العمليات-والنشر-والإدارة)
8. [الدليل المرجعي لكافة المسارات (Complete API Endpoints Catalog)](#8-الدليل-المرجعي-لكافة-المسارات)

---

## 1. المقدمة والرؤية المعمارية للمنظومة

تم بناء هذه المنظومة لتكون **منصة مؤسسية متكاملة لإدارة دورة التوظيف واستقطاب الكفاءات (End-to-End Enterprise Talent Acquisition System)**، بالاعتماد على معمارية الخدمات المصغرة الموزعة (**Distributed Microservices Architecture**) لضمان أقصى درجات المرونة (Scalability)، وقابلية الصيانة (Maintainability)، وفصل المسؤوليات (Separation of Concerns).

```
                             ┌───────────────────────────────────┐
                             │       Frontend / API Clients      │
                             └─────────────────┬─────────────────┘
                                               │
               ┌───────────────────────────────┼───────────────────────────────┐
               │                               │                               │
               ▼                               ▼                               ▼
    ┌──────────────────────┐        ┌──────────────────────┐        ┌──────────────────────┐
    │     Auth Service     │        │     Job Service      │        │  Candidate Service   │
    │     (Port 8081)      │        │     (Port 8082)      │        │     (Port 8083)      │
    │  • JWT / BCrypt      │        │  • Job Postings      │        │  • Profile Mgmt      │
    │  • Hybrid LDAP Auth  │        │  • Auto-Cleanup Cron │        │  • CV File Storage   │
    │  • 6-digit OTP Reset │        │  • Status Lifecycle  │        │  • Skill Search      │
    └──────────┬───────────┘        └──────────┬───────────┘        └──────────┬───────────┘
               │                               │                               │
               │                               │                               │
               ▼                               ▼                               ▼
    ┌──────────────────────────────────────────────────────────────────────────────────────┐
    │                      Oracle Database (Users, Jobs, Candidates, Apps)                 │
    └──────────────────────────────────────────────────────────────────────────────────────┘
               ▲                               ▲                               ▲
               │                               │                               │
    ┌──────────┴───────────┐        ┌──────────┴───────────┐        ┌──────────┴───────────┐
    │  Application Service │        │      AI Service      │        │ Notification Service │
    │     (Port 8084)      │        │     (Port 8085)      │        │     (Port 8086)      │
    │  • Public Tracking   │        │  • PDF / DOCX Parser │        │  • SMTP Mail Sender  │
    │  • Interview Scoring │        │  • Resume OCR Extract│        │  • HTML / Plain Text │
    │  • Advanced Analytics│        │  • Skill Match Engine│        │  • Mock / Dev Mode   │
    └──────────────────────┘        └──────────────────────┘        └──────────────────────┘
```

### أهداف المنظومة:
1. **أتمتة دورة التوظيف**: من نشر الوظيفة الشاغرة، واستقبال المرشحين، والفرز الذكي، حتى المقابلة والتوظيف النهائي.
2. **المطابقة الذكية بواسطة الذكاء الاصطناعي**: فحص ملفات السيرة الذاتية (PDF/Word)، واستخراج المهارات وسنوات الخبرة، ومقارنتها آلياً بمتطلبات الوظيفة وإعطاء نسبة توافق دقيقة.
3. **التتبع اللحظي للمرشحين (Public Tracking)**: إتاحة رابط تتبع عام للمتقدمين باستخدام رمز التتبع المشفر (`UUID Tracking ID`) أو البريد الإلكتروني.
4. **أمان مؤسسي متعدد المستويات**: مصادقة مزدوجة هجينة (LDAP + Local DB)، وتوليد رموز OTP لاستعادة الحسابات، وصلاحيات دقيقة مبنية على الأدوار (RBAC).

---

## 2. المكدس التكنولوجي والأدوات المستخدمة

| المجال | التقنية / الأداة | الإصدار | الغرض والاستخدام |
| :--- | :--- | :--- | :--- |
| **لغة البرمجة** | **Java** | **Java 21 LTS** | الاستفادة من أحدث مزايا لغة جافا، الـ Pattern Matching، والأداء العالي. |
| **إطار العمل الأساسي** | **Spring Boot** | **3.4.0** | بناء الخدمات المصغرة السحابية والـ REST APIs. |
| **إدارة البناء والحزم** | **Gradle (Kotlin DSL)** | **8.x / 9.x** | تجميع المشروع المتعدد الوحدات وإدارة الاعتمادات (`build.gradle.kts`). |
| **قواعد البيانات** | **Oracle Database** | **19c / 21c** | تخزين البيانات العلائقية والمعاملات الحساسة (`ojdbc11`). |
| **الوصول للبيانات (ORM)** | **Spring Data JPA / Hibernate** | **6.x** | إدارة الـ Entities، العلاقات، والاستعلامات التلقائية و `CLOB`. |
| **طبقة الأمان** | **Spring Security** | **6.x** | إدارة الفلاتر، حماية المسارات، وعزل الجلسات. |
| **التوكن الرقمي** | **JJWT (Java JWT)** | **0.11.5** | إصدار والتحقق من التوكنات عديمة الحالة (`HMAC-SHA256`). |
| **خدمة الدليل** | **Spring Security LDAP** | **3.4.0** | الربط مع Active Directory / OpenLDAP للمصادقة المركزية. |
| **معالجة المستندات والـ PDF** | **Apache PDFBox** | **2.0.29** | استخراج النصوص والجداول والبيانات من ملفات الـ PDF. |
| **معالجة ملفات الأوفيس** | **Apache POI & POI-OOXML**| **5.2.3** | استخراج النصوص من ملفات Word (`.doc`, `.docx`). |
| **التعرف التلقائي على الملفات** | **Apache Tika Core & Parsers** | **2.9.0** | كشف نوع المحتوى وفهرسة المستندات المتنوعة. |
| **خدمة البريد الإلكتروني** | **Spring Mail / JavaMailSender**| **Jakarta Mail** | إرسال رسائل التنبيهات والأكواد عبر بروتوكول SMTP/TLS. |
| **توليد الأكواد المساعدة** | **Project Lombok** | **1.18.x** | تقليل الكود المكرر (`@Data`, `@RequiredArgsConstructor`, `@Builder`). |
| **الحاويات والافتراضية** | **Docker & Docker Compose** | **v2+** | بناء صور خفيفة (`eclipse-temurin:21-jre-alpine`) وتشغيل الحاويات. |
| **أدوات التشغيل والأتمتة** | **PowerShell & Batch** | **Windows Shell** | سكربتات التشغيل الموحد، الفحص، والإيقاف (`run-all.bat`, `stop-all.bat`). |
| **أطر الاختبار** | **JUnit 5 / Mockito / MockMvc**| **Jupiter** | اختبارات الوحدة واختبارات التكامل لكافة الطبقات. |

---

## 3. التشريح التفصيلي للخدمات المصغرة الستة

---

### 3.1 خدمة المصادقة والهوية (`Auth Service` - المنفذ: `8081`)

تعتبر هذه الخدمة هي **بوابة الأمان والتحكم في الهويات (Identity & Access Management)** لكامل المنظومة.

#### المهام الرئيسية:
1. **تسجيل المستخدمين الجدد (`/register`)**: تشفير كلمة المرور بـ `BCrypt`، والتأكد من عدم تكرار اسم المستخدم والبريد.
2. **تسجيل الدخول الهجين (`/login`)**:
   - التحقق أولاً من خادم **LDAP** (في حال تفعيله عبر `ldap.enabled: true`).
   - في حال نجاح الدخول عبر LDAP، يتم سحب بيانات الموظف ومزامنته في قاعدة البيانات تلقائياً (`Auto-Provisioning`).
   - في حال فشل LDAP أو تعطيله، يتم الرجوع لقاعدة البيانات المحلية والتحقق بـ `BCrypt`.
   - إصدار توكن JWT موحد للمستخدم يحتوي على الصلاحيات واسم المستخدم.
3. **نظام استعادة كلمة المرور (Forgot / Reset Password Flow)**:
   - توليد رمز تأكيد رقمي آمن (6-digit OTP) عبر `SecureRandom`.
   - تحديد صلاحية للرمز مدتها **15 دقيقة**.
   - إرسال الرمز للمستخدم عبر استدعاء داخلي لـ `notification-service`.
   - التحقق من الرمز وتحديث كلمة المرور مع مسح الـ OTP لمنع تكرار استخدامه.

#### ملفات الخدمة الرئيسية:
* [`AuthService.java`](file:///c:/Users/Hossam/Downloads/project/services/services/auth/src/main/java/com/services/auth/service/AuthService.java): العقل المدبر لعمليات المصادقة والتسجيل والـ OTP.
* [`LdapAuthService.java`](file:///c:/Users/Hossam/Downloads/project/services/services/auth/src/main/java/com/services/auth/service/LdapAuthService.java): معالجة الاتصال والربط مع خوادم Active Directory / LDAP.
* [`JwtService.java`](file:///c:/Users/Hossam/Downloads/project/services/services/auth/src/main/java/com/services/auth/service/JwtService.java): بناء التوكنات وفك تشفيرها والتحقق من صلاحيتها الزمنية.
* [`NotificationClient.java`](file:///c:/Users/Hossam/Downloads/project/services/services/auth/src/main/java/com/services/auth/service/NotificationClient.java): عميل HTTP (RestClient) للتواصل مع خدمة الإشعارات مع معالجة آمنة للاستثناءات.

---

### 3.2 خدمة الوظائف (`Job Service` - المنفذ: `8082`)

المسؤولة عن إدارة الشواغر الوظيفية ودورة حياة الإعلان الوظيفي في المؤسسة.

#### المهام الرئيسية:
1. **إنشاء وتحديث الوظائف**: تحديد المسمى الوظيفي، الوصف، المتطلبات، نطاق الراتب، القسم، ونوع التوظيف (Full-time, Part-time, Remote).
2. **إدارة حالات الوظيفة (`JobStatus`)**: `OPEN` (مفتوحة لاستقبال الطلبات)، `CLOSED` (مغلقة)، `ON_HOLD` (معلقة مؤقتاً).
3. **مهمة التنظيف التلقائي المجدولة (`JobCleanupService`)**:
   - تعمل يومياً في تمام الساعة 2:00 صباحاً عبر Cron Expression: `@Scheduled(cron = "0 0 2 * * ?")`.
   - تقوم بالبحث عن جميع الوظائف المغلقة (`CLOSED`) التي مضى على إغلاقها أكثر من 30 يوماً وحذفها تلقائياً لتخفيف العبء على قاعدة البيانات.

#### ملفات الخدمة الرئيسية:
* [`Job.java`](file:///c:/Users/Hossam/Downloads/project/services/services/job/src/main/java/com/services/job/model/Job.java): نموذج بيانات الوظيفة.
* [`JobService.java`](file:///c:/Users/Hossam/Downloads/project/services/services/job/src/main/java/com/services/job/service/JobService.java): منطق الأعمال الخاص بالوظائف.
* [`JobCleanupService.java`](file:///c:/Users/Hossam/Downloads/project/services/services/job/src/main/java/com/services/job/service/JobCleanupService.java): محرك الأرشفة والحذف التلقائي.

---

### 3.3 خدمة المرشحين (`Candidate Service` - المنفذ: `8083`)

المسؤولة عن بناء قاعدة بيانات المواهب والكوادر البشرية وإدارة ملفاتهم الشخصية وسيرهم الذاتية.

#### المهام الرئيسية:
1. **تسجيل المرشحين**: دعم الاستقبال عبر `application/json` أو عبر `multipart/form-data` مع رفع ملف السيرة الذاتية (CV).
2. **تخزين وتتبع السير الذاتية**: حفظ مسارات وأسماء ملفات الـ CV.
3. **البحث المتقدم بالمهارات**: البحث السريع في حقول المهارات (`CLOB`) باستخدام `findBySkillsContainingIgnoreCase`.
4. **تتبع حالة التوظيف**: تمييز المرشحين الموظفين حالياً عن غير الموظفين (`isEmployed`).
5. **تخزين نتائج التحقق الذكي**: استقبال وتخزين نتائج تدقيق الذكاء الاصطناعي (`aiValidated`, `aiConfidenceScore`, `aiValidationNotes`).

#### ملفات الخدمة الرئيسية:
* [`Candidate.java`](file:///c:/Users/Hossam/Downloads/project/services/services/candidate/src/main/java/com/services/candidate/model/Candidate.java): نموذج المرشح الشامل.
* [`CandidateService.java`](file:///c:/Users/Hossam/Downloads/project/services/services/candidate/src/main/java/com/services/candidate/service/CandidateService.java): معالجة السجلات والبحث.

---

### 3.4 خدمة طلبات التوظيف (`Application Service` - المنفذ: `8084`)

تعتبر هذه الخدمة هي **العمود الفقري ومحرك العمليات التشغيلية (Workflow Engine)** للتوظيف.

#### دورة حياة طلب التوظيف (`ApplicationStatus`):
```
[APPLIED] / [APPLIED_TO_INTERVIEW] ──> [INTERVIEW] ──> [UNDER_REVIEW] ──> [HIRED] أو [REJECTED]
```

#### المهام الرئيسية:
1. **التقديم ومنع التكرار**: فحص عدم تقديم المرشح لنفس الوظيفة أكثر من مرة (`existsByCandidateIdAndJobId`).
2. **نظام التتبع اللحظي العام (Public Tracking)**:
   - توليد معرف تتبع فريد وعشوائي (`UUID Tracking ID`) لكل طلب يتم إرجاعه في هيدر الـ HTTP: `X-Tracking-ID`.
   - إتاحة مسار عام لا يتطلب تسجيل دخول: `/api/applications/public/status/tracking?trackingId=...`.
3. **إدارة المقابلات والتقييمات**:
   - تعيين المقابل وتاريخ المقابلة (`assignInterviewer`).
   - تسجيل درجة التقييم والملاحظات (`addEvaluation`).
   - الترقية التلقائية: إذا حصل المرشح على درجة تقييم **>= 70**، يتم نقله تلقائياً إلى حالة قيد المراجعة النهائية (`UNDER_REVIEW`).
4. **لوحة الإحصائيات والتحليلات المتقدمة (`/api/applications/stats`)**:
   - إجمالي الطلبات.
   - التوزيع الإحصائي للطلبات حسب كل حالة.
   - متوسط الدرجات التي أعطاها الذكاء الاصطناعي للمرشحين (`averageAIScore`).
   - أعلى درجة ذكاء اصطناعي مسجلة (`maxAIScore`).
   - معدل نجاح التوظيف الفعلي (`hiringRate = Hired / Total * 100`).
   - عدد الطلبات في آخر أسبوع وآخر شهر.

---

### 3.5 خدمة الذكاء الاصطناعي ومعالجة السير الذاتية (`AI Service` - المنفذ: `8085`)

خدمة مستقلة عديمة الحالة (**Stateless Service**) مخصصة لمعالجة اللغات الطبيعية وهندسة استخراج البيانات من المستندات.

#### مكونات ومحركات الخدمة:
1. **محرك استخراج النصوص (`TextExtractorService`)**:
   - يستخدم **Apache PDFBox** لقراءة ملفات الـ PDF وفك تشفير الجداول والفقرات.
   - يستخدم **Apache POI** لمعالجة مستندات Microsoft Word القديمة والحديثة (`.doc`, `.docx`).
   - يستخدم **Apache Tika** كطبقة استخراج شاملة لكافة أنواع الملفات النصية.
2. **محرك تحليل وفحص السيرة الذاتية (`CvParserService`)**:
   - استخراج الاسم الكامل عبر الـ Regex وتحليل السطور الأولى.
   - استخراج البريد الإلكتروني بدقة عبر `EMAIL_PATTERN`.
   - استخراج المهارات الفنية المكتوبة في أقسام (Skills, Core Competencies).
   - استخراج سنوات الخبرة والمؤهلات الأكاديمية (Degrees, Education).
   - **حساب درجة الثقة (Confidence Score)**: مقارنة البيانات المدخلة يدوياً من المرشح مع البيانات المستخرجة فعلياً من ملف السيرة الذاتية لتحديد ما إذا كانت البيانات صحيحة أم تم التلاعب بها.
3. **محرك المطابقة الوظيفية (`AiMatchingService`)**:
   - **المهارات (وزن 60%)**: حساب النسبة المئوية للمهارات المشتركة بين المرشح ومتطلبات الوظيفة.
   - **الخبرة (وزن 30%)**: مطابقة سنوات خبرة المرشح مع سنوات الخبرة المطلوبة في الوظيفة وفق مصفوفة مرنة.
   - **المسمى الوظيفي (وزن 10%)**: مطابقة الكلمات المفتاحية للمسمى الوظيفي مع خبرات المرشح.
   - **تصنيف التوافق**: إعطاء تقييم فوري (`Excellent` >= 80%, `Good` >= 60%, `Moderate` >= 40%, `Low` < 40%).

---

### 3.6 خدمة الإشعارات والبريد (`Notification Service` - المنفذ: `8086`)

خدمة مركزية لإرسال التنبيهات ورسائل البريد الإلكتروني عبر بروتوكول **SMTP**.

#### المزايا التشغيلية:
1. **دعم الرسائل النصية وقوالب الـ HTML**: إرسال رسائل بريد عادية أو مصممة بـ HTML.
2. **وضع المحاكاة الآمن (Mock / Dev Fallback Mode)**:
   - في حال كان إعداد الـ SMTP مضبوطاً على الحساب الافتراضي التجريبي (`dummy@gmail.com`) أو عند انقطاع خادم البريد، لا تتوقف المنظومة؛ بل تقوم الخدمة بتسجيل الإيميل في سجلات الـ Log بنجاح ومتابعة العمليات بسلاسة.

---

## 4. معمارية وهندسة قواعد البيانات

تعتمد المنظومة على **Oracle Database** كقاعدة بيانات علائقية موحدة للميكروسيرفس، مع تطبيق مبدأ استقلالية الجداول واستخدام مولدات التسلسل الخاصة بأوراكل (`Sequence Generators`).

```
  ┌─────────────────────────────────┐               ┌─────────────────────────────────┐
  │              USERS              │               │              JOBS               │
  ├─────────────────────────────────┤               ├─────────────────────────────────┤
  │ PK  ID                 (NUMBER) │               │ PK  ID                 (NUMBER) │
  │     USERNAME       (VARCHAR 50) │               │     TITLE        (VARCHAR 255)  │
  │     EMAIL         (VARCHAR 100) │               │     DESCRIPTION          (CLOB) │
  │     PASSWORD      (VARCHAR 255) │               │     REQUIREMENTS         (CLOB) │
  │     FIRST_NAME     (VARCHAR 50) │               │     STATUS        (VARCHAR 50)  │
  │     LAST_NAME      (VARCHAR 50) │               │     LOCATION     (VARCHAR 255)  │
  │     EMPLOYEE_ID    (VARCHAR 50) │               │     SALARY_RANGE (VARCHAR 100)  │
  │     DEPARTMENT    (VARCHAR 100) │               │     DEPARTMENT   (VARCHAR 100)  │
  │     ROLE           (VARCHAR 20) │               │     EMPLOYMENT_TYPE (VARCHAR 50)│
  │     STATUS         (VARCHAR 20) │               │     CREATED_AT      (TIMESTAMP) │
  │     LDAP_DN       (VARCHAR 255) │               │     UPDATED_AT      (TIMESTAMP) │
  │     RESET_TOKEN   (VARCHAR 100) │               └────────────────┬────────────────┘
  │     RESET_TOKEN_EXPIRY (TIMEST) │                                │
  │     IS_DELETED        (BOOLEAN) │                                │ (Logical Relation)
  │     CREATED_AT      (TIMESTAMP) │                                │
  │     UPDATED_AT      (TIMESTAMP) │                                ▼
  └─────────────────────────────────┘               ┌─────────────────────────────────┐
                                                    │          APPLICATIONS           │
  ┌─────────────────────────────────┐               ├─────────────────────────────────┤
  │           CANDIDATES            │               │ PK  ID                 (NUMBER) │
  ├─────────────────────────────────┤ (Logical Rel) │     TRACKING_ID   (VARCHAR 36)  │
  │ PK  ID                 (NUMBER) │──────────────>│ FK  CANDIDATE_ID       (NUMBER) │
  │     FIRST_NAME    (VARCHAR 100) │               │ FK  JOB_ID             (NUMBER) │
  │     LAST_NAME     (VARCHAR 100) │               │     JOB_TITLE    (VARCHAR 255)  │
  │     EMAIL         (VARCHAR 150) │               │     CANDIDATE_EMAIL(VARCHAR 150)│
  │     PHONE_NUMBER   (VARCHAR 20) │               │     STATUS        (VARCHAR 50)  │
  │     SKILLS               (CLOB) │               │     INTERVIEWER_ID     (NUMBER) │
  │     EXPERIENCE_YEARS   (NUMBER) │               │     INTERVIEW_DATE  (TIMESTAMP) │
  │     CURRENT_POSITION (VAR 100)  │               │     EVALUATION_SCORE   (NUMBER) │
  │     CURRENT_COMPANY  (VAR 100)  │               │     EVALUATION_NOTES     (CLOB) │
  │     CV_FILE_NAME  (VARCHAR 255) │               │     AI_ROLE              (CLOB) │
  │     CV_FILE_PATH  (VARCHAR 500) │               │     AI_MATCH_SCORE     (NUMBER) │
  │     IS_EMPLOYED       (BOOLEAN) │               │     AI_MATCH_LEVEL(VARCHAR 50)  │
  │     AI_VALIDATED      (BOOLEAN) │               │     REJECTION_REASON     (CLOB) │
  │     AI_CONFIDENCE_SCORE (NUMBER)│               │     APPLICATION_DATE(TIMESTAMP) │
  │     AI_VALIDATION_NOTES  (CLOB) │               │     STATUS_CHANGED_AT(TIMESTAMP)│
  │     CREATED_AT      (TIMESTAMP) │               │     HIRED_DATE      (TIMESTAMP) │
  │     UPDATED_AT      (TIMESTAMP) │               └─────────────────────────────────┘
  └─────────────────────────────────┘
```

### تسلسلات أوراكل (Oracle Sequences):
1. `USER_SEQ`: لتوليد المفاتيح الأساسية لجدول المستخدمين.
2. `JOB_SEQ`: لتوليد المفاتيح الأساسية لجدول الوظائف.
3. `CANDIDATE_SEQ`: لتوليد المفاتيح الأساسية للمرشحين.
4. `APPLICATION_SEQ`: لتوليد المفاتيح الأساسية لطلبات التوظيف.

---

## 5. طبقة الحماية والأمان المتقدم

### 1. معيار المصادقة الموحد (Stateless JWT Authentication):
- بعد التحقق من هوية المستخدم (سواء عبر قاعدة البيانات أو خادم LDAP)، يتم توليد توكن مشفر باستخدام خوارزمية `HS256` ومفتاح سري محمي بمتغير بيئي `JWT_SECRET`.
- يحتوي التوكن على اسم المستخدم، الأدوار (`Roles`)، تاريخ الإصدار، وتاريخ الانتهاء (افتراضياً 24 ساعة = `86,400,000 ms`).

### 2. مصفوفة الصلاحيات والأدوار (Role-Based Access Control - RBAC):
* **`ADMIN`**: إدارة شاملة للنظام، المستخدمين، الإعدادات، والوظائف.
* **`HR`**: إنشاء الوظائف، مراجعة طلبات التوظيف، تعيين المقابلين، وتعيين المرشحين.
* **`INTERVIEWER`**: الاطلاع على طلبات المقابلات المعينة له، وتسجيل درجات التقييم والملاحظات الفنية.
* **`EMPLOYEE`**: الرتبة الافتراضية للموظفين المتزامنين تلقائياً من دليل الـ LDAP.

### 3. أمان استعادة كلمة المرور (OTP Cryptographic Lifecycle):
* توليد الأرقام باستخدام `java.security.SecureRandom` غير القابل للتنبؤ.
* مدة صلاحية صارمة (15 دقيقة).
* الإلغاء الفوري والتصفير (`resetToken = null`) بمجرد نجاح عملية إعادة التعيين.

---

## 6. خوارزميات الذكاء الاصطناعي واستخراج النصوص

### نموذج تقييم مطابقة الوظيفة (AI Match Formula):

$$\text{Final Score} = (\text{Skill Match} \times 0.60) + \text{Experience Bonus} + \text{Title Match Bonus}$$

1. **نسبة المهارات ($\text{Skill Match}$)**:
   $$\text{Skill Match} = \left( \frac{\text{Matched Skills Count}}{\text{Total Required Skills}} \right) \times 100$$
2. **بونص الخبرة ($\text{Experience Bonus}$)**:
   * إذا كانت خبرة المرشح $\ge$ خبرة الوظيفة: $+30 \text{ نقطة}$.
   * إذا كانت خبرة المرشح أقل بـ سنتين فقط: $+15 \text{ نقطة}$.
   * إذا كانت أقل بـ 4 سنوات: $+7 \text{ نقاط}$.
   * خلاف ذلك: $0 \text{ نقطة}$.
3. **بونص المسمى الوظيفي ($\text{Title Match Bonus}$)**:
   * تطابق الكلمات المفتاحية بين المسمى ومهارات المرشح: $+10 \text{ نقاط}$.

---

## 7. العمليات والنشر والإدارة

### 1. التشغيل المحلي السريع (Local Scripting):
* **`run-all.bat`**: يقوم بإيقاف أي عمليات قديمة على المنافذ 8081-8086، ثم بناء ملفات الـ JAR بـ Gradle، ثم فتح 6 نوافذ طرفية مستقلة لكل خدمة، وفحص حالتها تلقائياً بعد 15 ثانية.
* **`stop-all.bat` / `scripts/stop-all.ps1`**: إنهاء العمليات على كافة المنافذ بأمان.
* **`status.bat` / `scripts/status.ps1`**: فحص صحة واستجابة المنافذ الستة.

### 2. التشغيل عبر حاويات دوكر (Docker Compose):
ملف [`docker-compose.yml`](file:///c:/Users/Hossam/Downloads/project/services/services/docker-compose.yml) جاهز لتشغيل كافة الخدمات بحاويات خفيفة تعتمد على Alpine Linux:
```bash
docker-compose up -d --build
```

---

## 8. الدليل المرجعي لكافة المسارات (API Reference)

### أ. خدمة المصادقة (Auth Service - Port 8081)
| Method | Endpoint | Description | Payload / Params |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | تسجيل مستخدم جديد | `{"username", "email", "password", "firstName", "lastName", "role", ...}` |
| `POST` | `/api/auth/login` | تسجيل الدخول (LDAP / DB) | `{"username", "password"}` |
| `POST` | `/api/auth/forgot-password`| طلب رمز استعادة كلمة المرور | `{"email": "user@example.com"}` |
| `POST` | `/api/auth/verify-reset-code`| التحقق من صحة كود الـ OTP | `{"email": "...", "code": "123456"}` |
| `POST` | `/api/auth/reset-password` | تحديث كلمة المرور بالرمز | `{"email": "...", "code": "...", "newPassword": "..."}` |

### ب. خدمة الوظائف (Job Service - Port 8082)
| Method | Endpoint | Description | Payload / Params |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/jobs` | نشر وظيفة جديدة | `{"title", "description", "requirements", "status", "location", ...}` |
| `GET` | `/api/jobs` | استرجاع كافة الوظائف | - |
| `GET` | `/api/jobs/open` | استرجاع الوظائف المفتوحة فقط | - |
| `GET` | `/api/jobs/{id}` | استرجاع تفاصيل وظيفة محددة | `id` |
| `PUT` | `/api/jobs/{id}` | تحديث بيانات وظيفة | `id`, JSON Body |
| `PUT` | `/api/jobs/{id}/status` | تحديث حالة وظيفة | `id`, `?status=OPEN/CLOSED` |
| `DELETE`| `/api/jobs/{id}` | حذف وظيفة | `id` |

### ج. خدمة المرشحين (Candidate Service - Port 8083)
| Method | Endpoint | Description | Payload / Params |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/candidates` | إنشاء مرشح (JSON / Multipart) | JSON أو Form Data مع ملف `cvFile` |
| `GET` | `/api/candidates` | استرجاع كافة المرشحين | - |
| `GET` | `/api/candidates/{id}` | استرجاع مرشح بالمعرف | `id` |
| `GET` | `/api/candidates/email/{email}`| البحث عن مرشح بالبريد | `email` |
| `PUT` | `/api/candidates/{id}` | تحديث بيانات مرشح | `id`, JSON / Query Params |
| `DELETE`| `/api/candidates/{id}` | حذف مرشح | `id` |

### د. خدمة طلبات التوظيف (Application Service - Port 8084)
| Method | Endpoint | Description | Payload / Params |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/applications` | تقديم طلب توظيف جديد | `{"candidateId", "jobId", "jobTitle", "candidateEmail"}` |
| `GET` | `/api/applications/public/status/tracking` | التتبع العام برمز الـ UUID | `?trackingId=...` |
| `GET` | `/api/applications/public/status` | التتبع بالبريد ورقم الوظيفة | `?email=...&jobId=...` |
| `PUT` | `/api/applications/{id}/status` | تحديث حالة الطلب | `id`, `{"status": "...", "reason": "..."}` |
| `PUT` | `/api/applications/{id}/assign-interviewer`| تعيين المقابل والموعد | `id`, `?interviewerId=...&interviewDate=...` |
| `PUT` | `/api/applications/{id}/evaluation` | تسجيل درجة التقييم والملاحظات| `id`, `?score=85&notes=...` |
| `PUT` | `/api/applications/{id}/hire` | قبول وتوظيف المرشح | `id` |
| `PUT` | `/api/applications/{id}/reject` | رفض الطلب مع السبب | `id`, `?reason=...` |
| `GET` | `/api/applications/stats` | لوحة الإحصائيات الشاملة | - |
| `GET` | `/api/applications/top-rated` | قائمة أعلى المرشحين تقييماً | `?minScore=70` |
| `GET` | `/api/applications` | جلب كافة الطلبات | - |

### هـ. خدمة الذكاء الاصطناعي (AI Service - Port 8085)
| Method | Endpoint | Description | Payload / Params |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/ai/parse-cv` | قراءة وفحص ملف السيرة الذاتية | `file` (Multipart), `name`, `email`, `skills`, `experienceYears` |
| `POST` | `/api/ai/match` | حساب نسبة التوافق بين المرشح والوظيفة | `{"candidateSkills", "candidateExperienceYears", "jobTitle", "jobRequirements"}` |

### و. خدمة الإشعارات (Notification Service - Port 8086)
| Method | Endpoint | Description | Payload / Params |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/notifications/send-email` | إرسال بريد إلكتروني | `{"to": "...", "subject": "...", "body": "..."}` |

---
**تم إعداد هذا التقرير الفني الشامل لتوثيق كامل جوانب وهندسة المنظومة.**
