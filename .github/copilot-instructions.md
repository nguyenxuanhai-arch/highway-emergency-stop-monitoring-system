# Copilot Instructions for Highway Emergency Stop Monitoring System

## Project Overview
Spring Boot 4.0.2 application (Java 21) for highway incident detection and emergency stop management with photographic evidence. Multi-layer architecture: controllers → services → repositories with JWT-based security and MySQL database.

## UC-01: Xử lý xe dừng khẩn cấp trên cao tốc (có hình ảnh minh chứng)

**Mô tả:** Hệ thống ITS hỗ trợ Trung tâm điều hành phát hiện, xác nhận và xử lý tình huống xe dừng khẩn cấp trên cao tốc, sử dụng hình ảnh từ camera giao thông để xác minh sự cố trước khi ra quyết định xử lý.

**Actor chính:** Nhân viên Trung tâm điều hành cao tốc  
**Actor phụ:** Hệ thống camera giao thông (mô phỏng bằng ảnh upload)

**Điều kiện tiên quyết:**
- Nhân viên đã đăng nhập hệ thống
- Hệ thống camera hoặc nhân viên cung cấp ít nhất một ảnh sự cố
- Hệ thống bản đồ hoạt động bình thường

**Luồng chính:**
1. Camera hoặc nhân viên phát hiện xe dừng khẩn cấp → chụp ảnh hiện trường
2. POST `/incidents` với ảnh → hệ thống tạo sự cố (status=DETECTED, detection_time=now(), vị trí, mô tả)
3. Sự cố xuất hiện trên Dashboard và bản đồ cao tốc
4. Nhân viên GET `/incidents/{id}` để xem chi tiết + ảnh minh chứng
5. Nhân viên xác nhận sự cố dựa trên ảnh → PUT `/incidents/{id}/confirm` (status=CONFIRMED)
6. Sau xử lý xong → PUT `/incidents/{id}/resolve` (status=RESOLVED, resolution_time=now())
7. Hệ thống lưu thời điểm kết thúc + ảnh minh chứng phục vụ thống kê

**Luồng thay thế:**
- A1: Nhân viên upload thêm ảnh trong quá trình xử lý → gắn vào cùng sự cố
- A2: Nếu ảnh không rõ, sự cố giữ trạng thái DETECTED (chưa đủ căn cứ xác nhận)

**Luồng ngoại lệ:**
- Không có ảnh → không cho xác nhận sự cố
- Ảnh không hợp lệ (MIME type, size) → hệ thống từ chối lưu

**Dữ liệu sử dụng:**
- **Incident**: id, vị trí (latitude/longitude), mô tả, status (DETECTED/CONFIRMED/RESOLVED), detection_time, resolution_time
- **IncidentImage**: id, incident_id, file_path, captured_at

**Post-condition:**
- Sự cố đánh dấu RESOLVED
- Ảnh minh chứng được lưu và gắn với sự cố
- Thời gian xử lý (detection_time, resolution_time) được lưu phục vụ thống kê

## Architecture & Key Components

### Entities & Domain Model (UC-01)
- **Incident**: Sự cố xe dừng khẩn cấp (id, latitude, longitude, description, status, detection_time, resolution_time)
- **IncidentImage**: Ảnh minh chứng gắn với sự cố (id, incident_id, file_path, captured_at)
- **HighwaySegment**: Thông tin đoạn đường cao tốc (id, segment_code, location_bounds)
- **User**: Thông tin nhân viên (id, email, password)

**Vòng đời Incident (UC-01):**
- **DETECTED**: Camera/nhân viên phát hiện sự cố, upload ảnh ban đầu
- **CONFIRMED**: Nhân viên xác nhận sự cố dựa trên xem xét ảnh
- **RESOLVED**: Nhân viên đánh dấu xử lý xong, lưu resolution_time

### Layered Structure
- **Controllers** (`controllers/`): REST endpoints (Auth, Dashboard, Incident, Report)
- **Services** (`services/`): Business logic (AuthService, IncidentService, ReportService)
- **Repositories** (`repositories/`): Data access with Spring Data JPA (UserRepository, IncidentRepository, HighwaySegmentRepository)
- **Entities** (`entities/`): JPA models (User, Incident, HighwaySegment)
- **DTOs** (`dtos/`): Transfer objects for request/response (IncidentRequest, IncidentResponse, AuthRequest, AuthResponse)
- **Security** (`securities/`): JWT authentication (JwtFilter, JwtUtil, UserDetailsServiceImpl)
- **Configs** (`configs/`): Framework configuration (SecurityConfig, WebSocketConfig, JwtConfig) — currently stub implementations

### Data Flow (UC-01 Main)
1. Camera hoặc nhân viên phát hiện → POST `/incidents` với ảnh (multipart/form-data)
2. IncidentController xác thực request, gọi IncidentService.createIncident()
3. Service:
   - Validate ảnh (MIME type, size limit)
   - Lưu Incident với status=DETECTED, detection_time=now()
   - Lưu ảnh vào file storage, tạo IncidentImage record
   - Trả về IncidentResponse
4. Dashboard & bản đồ cập nhật hiển thị sự cố mới
5. Nhân viên GET `/incidents/{id}` → xem chi tiết + danh sách ảnh
6. Nhân viên PUT `/incidents/{id}/confirm` → status=CONFIRMED (yêu cầu ≥1 ảnh hợp lệ)
7. Nhân viên PUT `/incidents/{id}/resolve` → status=RESOLVED, resolution_time=now()
8. ReportService tạo báo cáo với lifecycle timestamps & ảnh minh chứng

## Build & Execution

### Build
```bash
mvn clean install
```

### Run Application
```bash
mvn spring-boot:run
```

### Test
```bash
mvn test
```

### Key Build Tools
- **Maven**: Project build (pom.xml uses spring-boot-starter-parent 4.0.2)
- **Lombok**: Annotation processor for getters/setters/constructors
- **MapStruct 1.6.0**: Code-gen entity-to-DTO mapping (configured in pom.xml annotationProcessorPaths)
- **JJWT 0.12.3**: JWT token generation/validation (jjwt-api, jjwt-impl, jjwt-jackson)

## Implementation Status (UC-01)

✅ **COMPLETED:**
1. **Entities**: Incident (with @Builder.Default for images), IncidentImage, User (with Lombok), HighwaySegment
2. **Repositories**: IncidentRepository, IncidentImageRepository, UserRepository, HighwaySegmentRepository with custom query methods
3. **DTOs**: IncidentRequest (with validation), IncidentResponse, IncidentImageResponse, AuthRequest, AuthResponse
4. **MapStruct Mappers**: IncidentMapper, IncidentImageMapper
5. **Services**:
   - IncidentService: createIncident(), confirmIncident(), resolveIncident(), addImage(), listIncidentsByStatus(), getIncidentById()
   - AuthService: register(), login()
   - ReportService: getIncidentReport(), getResolvedIncidentsByDateRange(), getDetailedStatistics()
6. **Controllers**:
   - IncidentController: POST /incidents, GET /incidents/{id}, PUT /incidents/{id}/confirm, PUT /incidents/{id}/resolve, POST /incidents/{id}/images
   - AuthController: POST /auth/register, POST /auth/login
   - DashboardController: GET /dashboard/overview, GET /dashboard/active-incidents
   - ReportController: GET /reports/incidents, GET /reports/statistics, GET /reports/resolved
7. **Security**: SecurityConfig, JwtFilter, JwtUtil (using JJWT 0.12.3), UserDetailsServiceImpl, WebSocketConfig
8. **Image Validation**: MIME type (jpeg/png/gif/webp), size limit (5MB), stored in uploads/incidents/

## API Reference

See **API_USAGE_GUIDE.md** for comprehensive endpoint documentation including:
- Authentication (register, login)
- UC-01 workflow (create incident → confirm → resolve)
- Alternative flows (upload additional images)
- Dashboard endpoints (overview, active incidents)
- Report endpoints (statistics, date range queries)

## Project-Specific Conventions

### Entity-to-DTO Mapping
MapStruct mappers are defined in `mappers/` folder (currently empty but should follow pattern). When adding new entities:
1. Create corresponding DTO in `dtos/`
2. Create mapper interface in `mappers/` with `@Mapper` annotation
3. Services inject and use mappers for entity-to-DTO conversion

### Database Configuration
- **Database**: MySQL on AWS RDS (ap-southeast-2 region)
- **Connection**: `spring.datasource.url` in `application.properties`
- **Credentials**: Username "admin", password in properties (note: credentials in repo—consider env variables for production)
- **DDL Strategy**: `ddl-auto=update` (auto-creates/updates schema)
- **Connection Pooling**: HikariCP with 600s max-lifetime

### Security
- **JWT-based authentication** via JwtFilter (Spring Security filter chain)
- **SecurityConfig**: Fully implemented with SecurityFilterChain bean, password encoder (BCrypt), JWT filter integration
- **JwtUtil**: Uses JJWT 0.12.3 API (Jwts.parser(), parseSignedClaims()) to generate/validate tokens
- **JwtFilter**: Extracts JWT from "Authorization: Bearer {token}" header, validates, sets SecurityContext
- **UserDetailsServiceImpl**: Loads user from database via UserRepository
- **Authentication flow**: AuthController → AuthService → UserRepository → JwtUtil
- **Token expiration**: 24 hours (configurable via JwtConfig)

### Validation & Constraints
- Spring Validation dependencies included (spring-boot-starter-validation)
- Use `@Valid` on controller method parameters, constraint annotations on DTOs
- Example: IncidentRequest validates latitude (-90 to 90), longitude (-180 to 180), description not blank

### Thymeleaf Frontend
- **Templates**: `src/main/resources/templates/` (cache disabled for development)
- Integration with Spring Security via thymeleaf-extras-springsecurity6

## Testing
- Test files in `src/test/java/`
- Use Spring Security Test (`spring-boot-starter-security-test`) for controller/security tests
- Example: HighwayEmergencyStopMonitoringSystemApplicationTests.java

## Common Tasks

### Create Incident with Image (UC-01 Main Flow)
1. Client POST `/api/incidents` with multipart form data:
   - latitude, longitude, description (validated)
   - image file (validated: MIME type, size ≤ 5MB)
2. IncidentController calls IncidentService.createIncident(request, imageFile)
3. Service validates image, saves Incident (status=DETECTED, detection_time=now())
4. Service saves image file to uploads/incidents/ with UUID prefix
5. Service creates IncidentImage entity linking file to incident
6. Service maps entities to DTOs using MapStruct mappers
7. Returns IncidentResponse with incident details and image list
8. Image stored at: `uploads/incidents/{UUID}_{originalFilename}`

### Confirm Incident (UC-01 - Photo Review)
1. Operator views incident details via GET `/incidents/{id}`
2. Operator reviews attached images
3. Operator calls PUT `/incidents/{id}/confirm`
4. Service validates:
   - Incident exists and status=DETECTED
   - Incident has ≥1 IncidentImage
5. Service updates status→CONFIRMED, persists
6. Returns updated IncidentResponse

### Resolve Incident (UC-01 - End State)
1. Operator calls PUT `/incidents/{id}/resolve`
2. Service validates:
   - Incident exists and status!=RESOLVED
3. Service updates:
   - status→RESOLVED
   - resolution_time=now()
4. Persists changes
5. Returns IncidentResponse with full lifecycle timestamps

### Upload Additional Images (UC-01 - Alternative A1)
1. Operator calls POST `/incidents/{id}/images` with new image file
2. Service validates:
   - Incident exists and status!=RESOLVED
   - Image file valid
3. Service saves image file and creates new IncidentImage record
4. Returns updated IncidentResponse with all images (old + new)
   - Tạo IncidentImage mới gắn với incident
   - Return updated IncidentResponse với tất cả ảnh

### Add New Endpoint
1. Create Request/Response DTOs in `dtos/`
2. Create endpoint method in appropriate Controller
3. Create corresponding Service method
4. Use Repository for data access
5. Apply business validation in Service

### Add New Endpoint
1. Create Request/Response DTOs in `dtos/`
2. Create endpoint method in appropriate Controller
3. Create corresponding Service method
4. Use Repository for data access
5. Apply business validation in Service

### Add New Database Entity
1. Create JPA entity in `entities/`
2. Create Repository interface in `repositories/`
3. Create DTO in `dtos/`
4. Create MapStruct mapper in `mappers/`
5. Add Service methods for CRUD operations
6. DDL auto-migration handles schema creation

## Known Limitations & TODOs
- WebSocket support configured but not implemented
- Consider externalizing database credentials to environment variables
- Consider implementing pagination for large incident lists
- Frontend dashboard templates not yet implemented

## External Integrations
- **Spring Data JPA**: Automatic query generation from repository method names
- **Spring Security**: Filter-based request security
- **MySQL Driver** (com.mysql:mysql-connector-j): Runtime dependency

## Always respond in Vietnamese