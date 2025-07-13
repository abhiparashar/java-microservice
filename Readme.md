# Complete Microservices in Java Mastery Guide

## Table of Contents
1. [Foundation Concepts](#foundation-concepts)
2. [Core Java Technologies](#core-java-technologies)
3. [Spring Boot & Spring Cloud](#spring-boot--spring-cloud)
4. [Communication Patterns](#communication-patterns)
5. [Data Management](#data-management)
6. [Service Discovery & Configuration](#service-discovery--configuration)
7. [Security & Monitoring](#security--monitoring)
8. [Testing Strategies](#testing-strategies)
9. [Deployment & DevOps](#deployment--devops)
10. [Performance & Scalability](#performance--scalability)
11. [Practical Projects](#practical-projects)
12. [FAANG Interview Questions](#faang-interview-questions)
13. [Advanced Topics](#advanced-topics)

---

## Foundation Concepts

### What Are Microservices?
Microservices architecture is a method of developing software applications as a suite of independently deployable, small, modular services. Each service runs its own process and communicates via well-defined APIs.

### Key Characteristics
- **Single Responsibility**: Each service handles one business capability
- **Decentralized**: Services manage their own data and business logic
- **Fault Isolation**: Failure in one service doesn't bring down the entire system
- **Technology Agnostic**: Services can use different technologies
- **Independently Deployable**: Services can be deployed without affecting others

### Microservices vs Monolith

**Monolithic Architecture:**
- Single deployable unit
- Shared database
- Inter-module communication via method calls
- Single technology stack

**Microservices Architecture:**
- Multiple independently deployable services
- Database per service
- Inter-service communication via APIs
- Polyglot programming possible

### Benefits and Challenges

**Benefits:**
- Scalability (scale individual services)
- Technology diversity
- Fault isolation
- Team autonomy
- Faster deployment cycles

**Challenges:**
- Distributed system complexity
- Network latency
- Data consistency
- Service discovery
- Monitoring complexity
- Testing complexity

---

## Core Java Technologies

### Essential Java Concepts for Microservices

#### 1. Concurrency & Threading
```java
// CompletableFuture for async operations
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    // Simulate external service call
    return "Service Response";
});

// Thread pools for managing concurrent requests
ExecutorService executor = Executors.newFixedThreadPool(10);
```

#### 2. HTTP Client Libraries
```java
// Using HttpClient (Java 11+)
HttpClient client = HttpClient.newHttpClient();
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://api.example.com/data"))
    .build();

HttpResponse<String> response = client.send(request, 
    HttpResponse.BodyHandlers.ofString());
```

#### 3. JSON Processing
```java
// Using Jackson
ObjectMapper mapper = new ObjectMapper();
String json = mapper.writeValueAsString(object);
MyClass obj = mapper.readValue(json, MyClass.class);
```

#### 4. Reactive Programming
```java
// Using Project Reactor
Mono<String> mono = Mono.just("Hello")
    .map(String::toUpperCase)
    .delayElement(Duration.ofSeconds(1));

Flux<Integer> flux = Flux.range(1, 5)
    .map(i -> i * 2)
    .filter(i -> i % 4 == 0);
```

---

## Spring Boot & Spring Cloud

### Spring Boot Fundamentals

#### 1. Basic Spring Boot Application
```java
@SpringBootApplication
@RestController
public class UserServiceApplication {
    
    @Autowired
    private UserService userService;
    
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
    
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(user);
    }
}
```

#### 2. Configuration Properties
```java
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {
    private String name;
    private String version;
    private Database database = new Database();
    
    @Data
    public static class Database {
        private String url;
        private String username;
        private String password;
    }
}
```

#### 3. Profiles and Environment-Specific Configuration
```yaml
# application.yml
spring:
  profiles:
    active: dev
    
---
spring:
  profiles: dev
  datasource:
    url: jdbc:h2:mem:testdb
    
---
spring:
  profiles: prod
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb
```

### Spring Cloud Components

#### 1. Service Registration with Eureka
```java
@SpringBootApplication
@EnableEurekaClient
public class UserServiceApplication {
    // Application code
}
```

```yaml
# application.yml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

#### 2. Load Balancing with Ribbon
```java
@RestController
public class OrderController {
    
    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Autowired
    private RestTemplate restTemplate;
    
    @GetMapping("/orders/{id}")
    public Order getOrder(@PathVariable Long id) {
        // This will load balance across user-service instances
        User user = restTemplate.getForObject(
            "http://user-service/users/" + id, User.class);
        return orderService.createOrder(user);
    }
}
```

#### 3. Circuit Breaker with Hystrix
```java
@Component
public class UserServiceClient {
    
    @HystrixCommand(fallbackMethod = "getDefaultUser")
    public User getUser(Long id) {
        // Call to user service
        return restTemplate.getForObject("/users/" + id, User.class);
    }
    
    public User getDefaultUser(Long id) {
        return new User(id, "Default User", "default@example.com");
    }
}
```

#### 4. API Gateway with Spring Cloud Gateway
```java
@SpringBootApplication
public class ApiGatewayApplication {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("user-service", r -> r.path("/users/**")
                .uri("lb://user-service"))
            .route("order-service", r -> r.path("/orders/**")
                .uri("lb://order-service"))
            .build();
    }
}
```

---

## Communication Patterns

### 1. Synchronous Communication

#### REST APIs
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody @Valid User user) {
        User savedUser = userService.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }
}
```

#### GraphQL
```java
@Component
public class UserResolver implements GraphQLQueryResolver {
    
    public User user(Long id) {
        return userService.findById(id);
    }
    
    public List<User> users(int first, int offset) {
        return userService.findAll(PageRequest.of(offset, first));
    }
}
```

### 2. Asynchronous Communication

#### Message Queues with RabbitMQ
```java
@Configuration
@EnableRabbit
public class RabbitConfig {
    
    @Bean
    public Queue userQueue() {
        return new Queue("user.queue", true);
    }
    
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange("user.exchange");
    }
    
    @Bean
    public Binding binding() {
        return BindingBuilder.bind(userQueue())
            .to(exchange())
            .with("user.created");
    }
}

@Component
public class UserEventPublisher {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public void publishUserCreated(User user) {
        UserCreatedEvent event = new UserCreatedEvent(user);
        rabbitTemplate.convertAndSend("user.exchange", 
            "user.created", event);
    }
}

@RabbitListener(queues = "user.queue")
public void handleUserCreated(UserCreatedEvent event) {
    // Handle the event
    log.info("User created: {}", event.getUser().getId());
}
```

#### Event Sourcing with Kafka
```java
@Service
public class OrderEventService {
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    public void publishOrderCreated(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(order);
        kafkaTemplate.send("order-events", event);
    }
}

@KafkaListener(topics = "order-events")
public void handleOrderEvent(OrderCreatedEvent event) {
    // Process the event
    inventoryService.reserveItems(event.getOrder().getItems());
}
```

### 3. Service Mesh Communication

#### Istio Service Mesh
```yaml
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: user-service
spec:
  hosts:
  - user-service
  http:
  - match:
    - headers:
        canary:
          exact: "true"
    route:
    - destination:
        host: user-service
        subset: v2
      weight: 100
  - route:
    - destination:
        host: user-service
        subset: v1
      weight: 100
```

---

## Data Management

### Database Per Service Pattern

#### 1. Service-Specific Databases
```java
// User Service - PostgreSQL
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    // ... other fields
}

// Order Service - MongoDB
@Document(collection = "orders")
public class Order {
    @Id
    private String id;
    private Long userId;
    private List<OrderItem> items;
    private LocalDateTime createdAt;
    // ... other fields
}
```

#### 2. Data Consistency Patterns

**Saga Pattern Implementation:**
```java
@Component
public class OrderSaga {
    
    @SagaOrchestrationStart
    public void processOrder(OrderCreatedEvent event) {
        // Step 1: Reserve inventory
        sagaManager.choreography()
            .step("reserve-inventory")
            .compensate("release-inventory")
            .step("process-payment")
            .compensate("refund-payment")
            .step("ship-order")
            .compensate("cancel-shipment")
            .execute();
    }
    
    @SagaOrchestrationEnd
    public void completeOrder(OrderCompletedEvent event) {
        log.info("Order {} completed successfully", event.getOrderId());
    }
}
```

**CQRS Implementation:**
```java
// Command Side
@Component
public class OrderCommandHandler {
    
    @CommandHandler
    public void handle(CreateOrderCommand command) {
        Order order = new Order(command.getUserId(), command.getItems());
        orderRepository.save(order);
        
        // Publish event
        eventPublisher.publish(new OrderCreatedEvent(order));
    }
}

// Query Side
@Component
public class OrderQueryHandler {
    
    @EventHandler
    public void handle(OrderCreatedEvent event) {
        OrderProjection projection = new OrderProjection(event.getOrder());
        orderProjectionRepository.save(projection);
    }
    
    @QueryHandler
    public List<OrderProjection> handle(GetOrdersQuery query) {
        return orderProjectionRepository.findByUserId(query.getUserId());
    }
}
```

### 3. Distributed Transactions

#### Two-Phase Commit (2PC)
```java
@Component
public class DistributedTransactionManager {
    
    public void executeDistributedTransaction(TransactionContext context) {
        // Phase 1: Prepare
        boolean allPrepared = context.getParticipants().stream()
            .allMatch(participant -> participant.prepare(context));
        
        if (allPrepared) {
            // Phase 2: Commit
            context.getParticipants().forEach(participant -> 
                participant.commit(context));
        } else {
            // Phase 2: Rollback
            context.getParticipants().forEach(participant -> 
                participant.rollback(context));
        }
    }
}
```

---

## Service Discovery & Configuration

### 1. Service Discovery with Consul

#### Service Registration
```java
@Configuration
public class ConsulConfig {
    
    @Bean
    public ConsulClient consulClient() {
        return new ConsulClient("localhost", 8500);
    }
    
    @EventListener
    public void registerService(ApplicationReadyEvent event) {
        NewService service = new NewService();
        service.setId("user-service-1");
        service.setName("user-service");
        service.setAddress("localhost");
        service.setPort(8080);
        
        // Health check
        NewService.Check check = new NewService.Check();
        check.setHttp("http://localhost:8080/health");
        check.setInterval("10s");
        service.setCheck(check);
        
        consulClient().agentServiceRegister(service);
    }
}
```

#### Service Discovery
```java
@Component
public class ServiceDiscoveryClient {
    
    @Autowired
    private ConsulClient consulClient;
    
    public List<ServiceInstance> getServiceInstances(String serviceName) {
        Response<List<HealthService>> response = consulClient
            .getHealthServices(serviceName, true, null);
        
        return response.getValue().stream()
            .map(healthService -> {
                Service service = healthService.getService();
                return new ServiceInstance(service.getId(), 
                    service.getAddress(), service.getPort());
            })
            .collect(Collectors.toList());
    }
}
```

### 2. Configuration Management

#### Spring Cloud Config
```java
@RestController
@RefreshScope
public class ConfigController {
    
    @Value("${app.feature.enabled:false}")
    private boolean featureEnabled;
    
    @Value("${app.message:Default message}")
    private String message;
    
    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("featureEnabled", featureEnabled);
        config.put("message", message);
        return config;
    }
}
```

#### Distributed Configuration with Apache Zookeeper
```java
@Component
public class ZookeeperConfigManager {
    
    private CuratorFramework client;
    
    @PostConstruct
    public void init() {
        client = CuratorFrameworkFactory.newClient("localhost:2181", 
            new ExponentialBackoffRetry(1000, 3));
        client.start();
    }
    
    public String getConfig(String path) {
        try {
            byte[] data = client.getData().forPath(path);
            return new String(data, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get config", e);
        }
    }
    
    public void watchConfig(String path, ConfigChangeListener listener) {
        PathChildrenCache cache = new PathChildrenCache(client, path, true);
        cache.getListenable().addListener(listener);
        try {
            cache.start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start config watcher", e);
        }
    }
}
```

---

## Security & Monitoring

### 1. Security Implementation

#### JWT Authentication
```java
@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private int jwtExpirationInMs;
    
    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);
        
        return Jwts.builder()
            .setSubject(Long.toString(userPrincipal.getId()))
            .setIssuedAt(new Date())
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }
    
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }
}
```

#### OAuth2 with Spring Security
```java
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.oauth2ResourceServer()
            .jwt()
            .jwtAuthenticationConverter(jwtAuthenticationConverter());
    }
    
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = 
            new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        authoritiesConverter.setAuthoritiesClaimName("roles");
        
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }
}
```

### 2. Monitoring & Observability

#### Distributed Tracing with Sleuth & Zipkin
```java
@RestController
public class UserController {
    
    @Autowired
    private Tracer tracer;
    
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        Span span = tracer.nextSpan().name("get-user").start();
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            span.tag("user.id", String.valueOf(id));
            return userService.findById(id);
        } finally {
            span.end();
        }
    }
}
```

#### Metrics with Micrometer
```java
@Component
public class CustomMetrics {
    
    private final Counter userCreatedCounter;
    private final Timer userRequestTimer;
    
    public CustomMetrics(MeterRegistry meterRegistry) {
        this.userCreatedCounter = Counter.builder("users.created")
            .description("Number of users created")
            .register(meterRegistry);
            
        this.userRequestTimer = Timer.builder("user.request.duration")
            .description("User request duration")
            .register(meterRegistry);
    }
    
    public void recordUserCreated() {
        userCreatedCounter.increment();
    }
    
    public void recordUserRequest(Duration duration) {
        userRequestTimer.record(duration);
    }
}
```

#### Health Checks
```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) {
                return Health.up()
                    .withDetail("database", "Available")
                    .withDetail("validationQuery", "SELECT 1")
                    .build();
            } else {
                return Health.down()
                    .withDetail("database", "Validation failed")
                    .build();
            }
        } catch (SQLException e) {
            return Health.down()
                .withDetail("database", "Connection failed")
                .withException(e)
                .build();
        }
    }
}
```

---

## Testing Strategies

### 1. Unit Testing

#### Service Layer Testing
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void shouldCreateUser() {
        // Given
        User user = new User("john@example.com", "John Doe");
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // When
        User result = userService.createUser(user);
        
        // Then
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        verify(userRepository).save(user);
    }
}
```

### 2. Integration Testing

#### Repository Layer Testing
```java
@DataJpaTest
class UserRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void shouldFindUserByEmail() {
        // Given
        User user = new User("john@example.com", "John Doe");
        entityManager.persistAndFlush(user);
        
        // When
        Optional<User> result = userRepository.findByEmail("john@example.com");
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("John Doe");
    }
}
```

#### Web Layer Testing
```java
@WebMvcTest(UserController.class)
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserService userService;
    
    @Test
    void shouldGetUser() throws Exception {
        // Given
        User user = new User("john@example.com", "John Doe");
        when(userService.findById(1L)).thenReturn(user);
        
        // When & Then
        mockMvc.perform(get("/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("john@example.com"))
            .andExpect(jsonPath("$.name").value("John Doe"));
    }
}
```

### 3. Contract Testing

#### Consumer Contract Testing with Pact
```java
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "user-service")
class UserServiceContractTest {
    
    @Pact(consumer = "order-service")
    public RequestResponsePact getUserPact(PactDslWithProvider builder) {
        return builder
            .given("user exists")
            .uponReceiving("a request for user")
            .path("/users/1")
            .method("GET")
            .willRespondWith()
            .status(200)
            .headers(Map.of("Content-Type", "application/json"))
            .body(new PactDslJsonBody()
                .numberType("id", 1)
                .stringType("email", "john@example.com")
                .stringType("name", "John Doe"))
            .toPact();
    }
    
    @Test
    @PactTestFor(pactMethod = "getUserPact")
    void shouldGetUserFromService(MockServer mockServer) {
        // Test implementation
        UserServiceClient client = new UserServiceClient(mockServer.getUrl());
        User user = client.getUser(1L);
        
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getEmail()).isEqualTo("john@example.com");
    }
}
```

### 4. End-to-End Testing

#### Test Containers
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class UserServiceIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:6")
            .withExposedPorts(6379);
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
    }
    
    @Test
    void shouldCreateAndRetrieveUser() {
        // Given
        User user = new User("john@example.com", "John Doe");
        
        // When
        ResponseEntity<User> createResponse = restTemplate.postForEntity(
            "/users", user, User.class);
        
        // Then
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        User createdUser = createResponse.getBody();
        assertThat(createdUser.getId()).isNotNull();
        
        // When
        ResponseEntity<User> getResponse = restTemplate.getForEntity(
            "/users/" + createdUser.getId(), User.class);
        
        // Then
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getEmail()).isEqualTo("john@example.com");
    }
}
```

---

## Deployment & DevOps

### 1. Containerization with Docker

#### Dockerfile
```dockerfile
FROM openjdk:17-jdk-slim

LABEL maintainer="your-email@example.com"

VOLUME /tmp

COPY target/user-service-1.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

#### Multi-stage Build
```dockerfile
FROM maven:3.8.1-openjdk-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY --from=builder /app/target/user-service-1.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 2. Kubernetes Deployment

#### Deployment Configuration
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: user-service
  template:
    metadata:
      labels:
        app: user-service
    spec:
      containers:
      - name: user-service
        image: user-service:1.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: url
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
```

#### Service Configuration
```yaml
apiVersion: v1
kind: Service
metadata:
  name: user-service
spec:
  selector:
    app: user-service
  ports:
    - protocol: TCP
      port: 80
      targetPort: 8080
  type: ClusterIP
```

### 3. CI/CD Pipeline

#### Jenkins Pipeline
```groovy
pipeline {
    agent any
    
    environment {
        DOCKER_REGISTRY = 'your-registry.com'
        DOCKER_IMAGE = 'user-service'
        KUBECONFIG = credentials('kubeconfig')
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Test') {
            steps {
                sh 'mvn clean test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        
        stage('Docker Build') {
            steps {
                script {
                    def image = docker.build("${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${BUILD_NUMBER}")
                    docker.withRegistry("https://${DOCKER_REGISTRY}", 'docker-registry-credentials') {
                        image.push()
                        image.push('latest')
                    }
                }
            }
        }
        
        stage('Deploy') {
            steps {
                sh """
                    kubectl set image deployment/user-service user-service=${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${BUILD_NUMBER}
                    kubectl rollout status deployment/user-service
                """
            }
        }
    }
}
```

---

## Performance & Scalability

### 1. Caching Strategies

#### Redis Caching
```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}

@Service
public class UserService {
    
    @Cacheable(value = "users", key = "#id")
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    @CacheEvict(value = "users", key = "#user.id")
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    @CacheEvict(value = "users", allEntries = true)
    public void clearAllUsers() {
        // This will clear all user cache entries
    }
}
```

#### Application-Level Caching
```java
@Component
public class CacheService {
    
    private final Cache<String, Object> cache;
    
    public CacheService() {
        this.cache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .recordStats()
            .build();
    }
    
    public <T> Optional<T> get(String key, Class<T> type) {
        Object value = cache.getIfPresent(key);
        return Optional.ofNullable(type.cast(value));
    }
    
    public void put(String key, Object value) {
        cache.put(key, value);
    }
}
```

### 2. Database Optimization

#### Connection Pooling
```java
@Configuration
public class DatabaseConfig {
    
    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/userdb");
        config.setUsername("username");
        config.setPassword("password");
        config.setDriverClassName("org.postgresql.Driver");
        
        // Pool settings
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);
        
        return new HikariDataSource(config);
    }
}
```

#### Read Replicas
```java
@Configuration
public class DatabaseRoutingConfig {
    
    @Bean
    @Primary
    public DataSource routingDataSource() {
        RoutingDataSource routingDataSource = new RoutingDataSource();
        
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("master", masterDataSource());
        targetDataSources.put("slave1", slave1DataSource());
        targetDataSources.put("slave2", slave2DataSource());
        
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(masterDataSource());
        
        return routingDataSource;
    }
}

@Service
public class UserService {
    
    @Transactional
    public User createUser(User user) {
        // This will use master database
        return userRepository.save(user);
    }
    
    @Transactional(readOnly = true)
    @ReadOnlyDataSource
    public User findById(Long id) {
        // This will use read replica
        return userRepository.findById(id).orElse(null);
    }
}
```

### 3. Load Balancing & Auto-scaling

#### Kubernetes Horizontal Pod Autoscaler
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: user-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: user-service
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

#### Custom Metrics Scaling
```java
@Component
public class CustomMetricsCollector {
    
    private final MeterRegistry meterRegistry;
    private final Gauge requestsPerSecond;
    
    public CustomMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.requestsPerSecond = Gauge.builder("custom.requests.per.second")
            .description("Requests per second")
            .register(meterRegistry);
    }
    
    @EventListener
    public void handleRequest(RequestProcessedEvent event) {
        // Update custom metrics that can be used for scaling
        requestsPerSecond.set(calculateRequestsPerSecond());
    }
}
```

---

## Practical Projects

### Project 1: E-commerce Microservices (Beginner)

#### Architecture Overview
- **User Service**: User management, authentication
- **Product Service**: Product catalog management
- **Order Service**: Order processing
- **Payment Service**: Payment processing
- **Notification Service**: Email/SMS notifications

#### Implementation Steps

**1. User Service**
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String email;
    
    private String firstName;
    private String lastName;
    private String password;
    private LocalDateTime createdAt;
    
    // Constructors, getters, setters
}

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    
    public User createUser(CreateUserRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        
        // Publish event
        eventPublisher.publishEvent(new UserCreatedEvent(savedUser));
        
        return savedUser;
    }
}
```

**2. Product Service**
```java
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String category;
    private LocalDateTime createdAt;
    
    // Constructors, getters, setters
}

@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    private final ProductService productService;
    
    @GetMapping
    public Page<Product> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category) {
        
        Pageable pageable = PageRequest.of(page, size);
        if (category != null) {
            return productService.findByCategory(category, pageable);
        }
        return productService.findAll(pageable);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return productService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
```

**3. Order Service**
```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long userId;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();
    
    // Constructors, getters, setters
}

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    
    private Long productId;
    private Integer quantity;
    private BigDecimal price;
    
    // Constructors, getters, setters
}

@Service
@Transactional
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    private final ApplicationEventPublisher eventPublisher;
    
    public Order createOrder(CreateOrderRequest request) {
        // Validate products and calculate total
        List<OrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (CreateOrderItemRequest itemRequest : request.getItems()) {
            Product product = productServiceClient.getProduct(itemRequest.getProductId());
            
            if (product == null) {
                throw new ProductNotFoundException("Product not found: " + itemRequest.getProductId());
            }
            
            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setQuantity(itemRequest.getQuantity());
            item.setPrice(product.getPrice());
            
            items.add(item);
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }
        
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setItems(items);
        
        items.forEach(item -> item.setOrder(order));
        
        Order savedOrder = orderRepository.save(order);
        
        // Publish event
        eventPublisher.publishEvent(new OrderCreatedEvent(savedOrder));
        
        return savedOrder;
    }
}
```

### Project 2: Social Media Platform (Intermediate)

#### Architecture Overview
- **User Service**: User profiles, authentication
- **Post Service**: Post creation, management
- **Feed Service**: Timeline generation
- **Notification Service**: Real-time notifications
- **Media Service**: Image/video upload
- **Search Service**: Full-text search

#### Key Features Implementation

**1. Event-Driven Architecture**
```java
@Component
public class SocialEventHandler {
    
    @EventListener
    @Async
    public void handleUserFollowed(UserFollowedEvent event) {
        // Update feed service
        feedService.addFollowerFeed(event.getFollowerId(), event.getFollowedId());
        
        // Send notification
        notificationService.sendFollowNotification(event.getFollowedId(), event.getFollowerId());
    }
    
    @EventListener
    @Async
    public void handlePostCreated(PostCreatedEvent event) {
        // Update followers' feeds
        List<Long> followers = userService.getFollowers(event.getPost().getUserId());
        feedService.distributePostToFollowers(event.getPost(), followers);
        
        // Extract hashtags and mentions
        searchService.indexPost(event.getPost());
    }
}
```

**2. Real-time Notifications with WebSocket**
```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new NotificationWebSocketHandler(), "/notifications")
            .setAllowedOrigins("*");
    }
}

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {
    
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserId(session);
        sessions.put(userId, session);
    }
    
    @EventListener
    public void handleNotification(NotificationEvent event) {
        WebSocketSession session = sessions.get(event.getUserId().toString());
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(event)));
            } catch (Exception e) {
                log.error("Failed to send notification", e);
            }
        }
    }
}
```

### Project 3: Banking System (Advanced)

#### Architecture Overview
- **Account Service**: Account management
- **Transaction Service**: Transaction processing
- **Payment Service**: Payment gateway integration
- **Fraud Detection Service**: ML-based fraud detection
- **Audit Service**: Compliance and audit trails
- **Notification Service**: Transaction alerts

#### Key Implementation Patterns

**1. Saga Pattern for Money Transfer**
```java
@Component
public class MoneyTransferSaga {
    
    @Autowired
    private SagaManager sagaManager;
    
    public void transferMoney(MoneyTransferRequest request) {
        SagaTransaction saga = sagaManager.begin()
            .step("validateAccounts")
                .invoke(accountService::validateAccounts)
                .withCompensation(accountService::releaseValidation)
            .step("checkFraud")
                .invoke(fraudService::checkTransaction)
                .withCompensation(fraudService::releaseCheck)
            .step("debitAccount")
                .invoke(accountService::debitAccount)
                .withCompensation(accountService::creditAccount)
            .step("creditAccount")
                .invoke(accountService::creditAccount)
                .withCompensation(accountService::debitAccount)
            .step("recordTransaction")
                .invoke(transactionService::recordTransaction)
                .withCompensation(transactionService::reverseTransaction)
            .step("sendNotification")
                .invoke(notificationService::sendTransferNotification)
                .withCompensation(notificationService::sendFailureNotification)
            .build();
        
        saga.execute(request);
    }
}
```

**2. Event Sourcing for Transaction History**
```java
@Entity
@Table(name = "transaction_events")
public class TransactionEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String aggregateId;
    private String eventType;
    private String eventData;
    private LocalDateTime timestamp;
    private Long version;
    
    // Constructors, getters, setters
}

@Component
public class TransactionEventStore {
    
    private final TransactionEventRepository eventRepository;
    private final ObjectMapper objectMapper;
    
    public void saveEvent(String aggregateId, DomainEvent event) {
        TransactionEvent transactionEvent = new TransactionEvent();
        transactionEvent.setAggregateId(aggregateId);
        transactionEvent.setEventType(event.getClass().getSimpleName());
        transactionEvent.setEventData(objectMapper.writeValueAsString(event));
        transactionEvent.setTimestamp(LocalDateTime.now());
        transactionEvent.setVersion(getNextVersion(aggregateId));
        
        eventRepository.save(transactionEvent);
    }
    
    public List<DomainEvent> getEvents(String aggregateId) {
        return eventRepository.findByAggregateIdOrderByVersion(aggregateId)
            .stream()
            .map(this::deserializeEvent)
            .collect(Collectors.toList());
    }
}
```

---

## FAANG Interview Questions

### System Design Questions

#### 1. Design a Chat Application like WhatsApp
**Interviewer**: Design a chat application that supports real-time messaging, group chats, and message history.

**Answer Structure**:
```
1. Requirements Gathering
   - Functional: Send/receive messages, group chats, online status
   - Non-functional: 1B users, 10M concurrent, low latency

2. High-Level Architecture
   - User Service: Authentication, user profiles
   - Chat Service: Message routing, group management
   - Message Service: Message persistence, history
   - Notification Service: Push notifications
   - Media Service: File/image sharing

3. Database Design
   - User data: PostgreSQL
   - Messages: Cassandra (for scale)
   - Real-time connections: Redis

4. Communication
   - WebSocket for real-time messaging
   - Message queues for reliability
   - CDN for media files

5. Scalability
   - Horizontal scaling of chat servers
   - Database sharding by user ID
   - Load balancing with consistent hashing
```

**Follow-up Questions**:
- **Q**: How would you handle message ordering?
- **A**: Use vector clocks or Lamport timestamps for distributed ordering, sequence numbers per chat room.

- **Q**: How to handle offline users?
- **A**: Store messages in persistent queue, use push notifications, sync on reconnection.

- **Q**: How to implement read receipts?
- **A**: Store read status per user per message, use acknowledgment system.

#### 2. Design a URL Shortener like bit.ly
**Interviewer**: Design a URL shortening service that can handle millions of URLs.

**Answer Structure**:
```java
// URL Shortener Service
@Service
public class UrlShortenerService {
    
    private final UrlRepository urlRepository;
    private final CacheService cacheService;
    private final Base62Encoder encoder;
    
    public String shortenUrl(String longUrl) {
        // Check if URL already exists
        Optional<Url> existing = urlRepository.findByLongUrl(longUrl);
        if (existing.isPresent()) {
            return existing.get().getShortUrl();
        }
        
        // Generate short URL
        long id = generateUniqueId();
        String shortUrl = encoder.encode(id);
        
        // Store in database
        Url url = new Url(id, longUrl, shortUrl, LocalDateTime.now());
        urlRepository.save(url);
        
        // Cache for quick access
        cacheService.put(shortUrl, longUrl);
        
        return shortUrl;
    }
    
    public String expandUrl(String shortUrl) {
        // Try cache first
        Optional<String> cached = cacheService.get(shortUrl);
        if (cached.isPresent()) {
            return cached.get();
        }
        
        // Fallback to database
        return urlRepository.findByShortUrl(shortUrl)
            .map(Url::getLongUrl)
            .orElseThrow(() -> new UrlNotFoundException("URL not found"));
    }
}
```

**Follow-up Questions**:
- **Q**: How to handle custom short URLs?
- **A**: Check availability, validate format, store mapping separately.

- **Q**: How to implement analytics?
- **A**: Async event publishing, separate analytics service, time-series database.

- **Q**: How to handle expired URLs?
- **A**: TTL field in database, background cleanup job, cache eviction.

### Microservices-Specific Questions

#### 1. How do you handle distributed transactions?
**Answer**: Multiple approaches depending on requirements:

```java
// Saga Pattern Implementation
@Component
public class OrderSagaOrchestrator {
    
    public void processOrder(OrderRequest request) {
        SagaTransaction saga = sagaManager.begin()
            .step("reserveInventory")
                .invoke(() -> inventoryService.reserve(request.getItems()))
                .withCompensation(() -> inventoryService.release(request.getItems()))
            .step("processPayment")
                .invoke(() -> paymentService.charge(request.getPayment()))
                .withCompensation(() -> paymentService.refund(request.getPayment()))
            .step("createOrder")
                .invoke(() -> orderService.create(request))
                .withCompensation(() -> orderService.cancel(request.getOrderId()))
            .build();
        
        saga.execute();
    }
}

// Two-Phase Commit for strong consistency
@Component
public class TwoPhaseCommitManager {
    
    public void executeTransaction(List<TransactionParticipant> participants) {
        // Phase 1: Prepare
        boolean allPrepared = participants.stream()
            .allMatch(participant -> {
                try {
                    return participant.prepare();
                } catch (Exception e) {
                    return false;
                }
            });
        
        if (allPrepared) {
            // Phase 2: Commit
            participants.forEach(TransactionParticipant::commit);
        } else {
            // Phase 2: Abort
            participants.forEach(TransactionParticipant::abort);
        }
    }
}
```

**Follow-up**: When would you use each approach?
- **Saga**: When eventual consistency is acceptable, long-running transactions
- **2PC**: When strong consistency is required, short transactions
- **Event Sourcing**: When audit trail is important, complex business logic

#### 2. How do you implement service discovery?
**Answer**: Multiple patterns with implementation:

```java
// Client-side discovery with Eureka
@Configuration
@EnableEurekaClient
public class ServiceDiscoveryConfig {
    
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public DiscoveryClient discoveryClient() {
        return new EurekaDiscoveryClient();
    }
}

// Server-side discovery with API Gateway
@Configuration
public class GatewayConfig {
    
    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("user-service", r -> r
                .path("/users/**")
                .uri("lb://user-service"))
            .route("order-service", r -> r
                .path("/orders/**")
                .filters(f -> f
                    .circuitBreaker(c -> c.setName("order-cb"))
                    .retry(3))
                .uri("lb://order-service"))
            .build();
    }
}

// Service mesh with Istio
// Virtual Service configuration
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: user-service
spec:
  hosts:
  - user-service
  http:
  - match:
    - headers:
        version:
          exact: "v2"
    route:
    - destination:
        host: user-service
        subset: v2
  - route:
    - destination:
        host: user-service
        subset: v1
```

#### 3. How do you handle cascading failures?
**Answer**: Multiple resilience patterns:

```java
// Circuit Breaker Pattern
@Component
public class ResilientServiceClient {
    
    private final CircuitBreaker circuitBreaker;
    
    public ResilientServiceClient() {
        this.circuitBreaker = CircuitBreaker.ofDefaults("serviceCall");
        circuitBreaker.getEventPublisher()
            .onStateTransition(event -> 
                log.info("Circuit breaker state transition: {}", event));
    }
    
    public Optional<String> callService(String request) {
        Supplier<String> decoratedSupplier = CircuitBreaker
            .decorateSupplier(circuitBreaker, () -> externalService.call(request));
        
        return Try.ofSupplier(decoratedSupplier)
            .recover(throwable -> {
                log.error("Service call failed", throwable);
                return "fallback-response";
            })
            .toJavaOptional();
    }
}

// Bulkhead Pattern
@Configuration
public class BulkheadConfig {
    
    @Bean("userServiceExecutor")
    public Executor userServiceExecutor() {
        return Executors.newFixedThreadPool(10);
    }
    
    @Bean("orderServiceExecutor")
    public Executor orderServiceExecutor() {
        return Executors.newFixedThreadPool(5);
    }
}

@Service
public class IsolatedService {
    
    @Async("userServiceExecutor")
    public CompletableFuture<User> getUserAsync(Long id) {
        return CompletableFuture.completedFuture(userService.getUser(id));
    }
    
    @Async("orderServiceExecutor")
    public CompletableFuture<Order> getOrderAsync(Long id) {
        return CompletableFuture.completedFuture(orderService.getOrder(id));
    }
}

// Rate Limiting
@Component
public class RateLimiter {
    
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    public boolean isAllowed(String key, int requests, Duration duration) {
        Bucket bucket = buckets.computeIfAbsent(key, k -> 
            Bucket4j.builder()
                .addLimit(Bandwidth.classic(requests, Refill.intervally(requests, duration)))
                .build());
        
        return bucket.tryConsume(1);
    }
}
```

### Coding Questions

#### 1. Implement a Distributed Lock
```java
// Redis-based distributed lock
@Component
public class RedisDistributedLock {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final String lockPrefix = "lock:";
    
    public boolean acquireLock(String key, String value, long timeoutMs) {
        String lockKey = lockPrefix + key;
        Boolean result = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, value, Duration.ofMillis(timeoutMs));
        return Boolean.TRUE.equals(result);
    }
    
    public boolean releaseLock(String key, String value) {
        String script = 
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "  return redis.call('del', KEYS[1]) " +
            "else " +
            "  return 0 " +
            "end";
        
        Long result = redisTemplate.execute(
            RedisScript.of(script, Long.class),
            Collections.singletonList(lockPrefix + key),
            value);
        
        return result != null && result == 1;
    }
}

// Usage example
@Service
public class OrderService {
    
    private final RedisDistributedLock distributedLock;
    
    public void processOrder(Long orderId) {
        String lockKey = "order:" + orderId;
        String lockValue = UUID.randomUUID().toString();
        
        if (distributedLock.acquireLock(lockKey, lockValue, 30000)) {
            try {
                // Process order logic
                doProcessOrder(orderId);
            } finally {
                distributedLock.releaseLock(lockKey, lockValue);
            }
        } else {
            throw new OrderProcessingException("Could not acquire lock for order: " + orderId);
        }
    }
}
```

#### 2. Implement Consistent Hashing for Load Balancing
```java
public class ConsistentHashingLoadBalancer {
    
    private final TreeMap<Long, Server> ring = new TreeMap<>();
    private final int virtualNodes;
    
    public ConsistentHashingLoadBalancer(int virtualNodes) {
        this.virtualNodes = virtualNodes;
    }
    
    public void addServer(Server server) {
        for (int i = 0; i < virtualNodes; i++) {
            String virtualNodeName = server.getName() + ":" + i;
            long hash = hash(virtualNodeName);
            ring.put(hash, server);
        }
    }
    
    public void removeServer(Server server) {
        for (int i = 0; i < virtualNodes; i++) {
            String virtualNodeName = server.getName() + ":" + i;
            long hash = hash(virtualNodeName);
            ring.remove(hash);
        }
    }
    
    public Server getServer(String key) {
        if (ring.isEmpty()) {
            return null;
        }
        
        long hash = hash(key);
        Long serverHash = ring.ceilingKey(hash);
        
        if (serverHash == null) {
            serverHash = ring.firstKey();
        }
        
        return ring.get(serverHash);
    }
    
    private long hash(String input) {
        return Hashing.murmur3_128().hashString(input, StandardCharsets.UTF_8).asLong();
    }
}
```

---

## Advanced Topics

### 1. Event Sourcing & CQRS

#### Event Store Implementation
```java
@Entity
@Table(name = "events")
public class EventEntity {
    @Id
    private String id;
    private String aggregateId;
    private String eventType;
    private String eventData;
    private LocalDateTime timestamp;
    private Long version;
    
    // Constructors, getters, setters
}

@Component
public class EventStore {
    
    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;
    
    public void saveEvent(String aggregateId, DomainEvent event) {
        EventEntity entity = new EventEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setAggregateId(aggregateId);
        entity.setEventType(event.getClass().getSimpleName());
        entity.setEventData(serialize(event));
        entity.setTimestamp(LocalDateTime.now());
        entity.setVersion(getNextVersion(aggregateId));
        
        eventRepository.save(entity);
        
        // Publish event to event bus
        eventBus.publish(event);
    }
    
    public List<DomainEvent> getEvents(String aggregateId, long fromVersion) {
        return eventRepository.findByAggregateIdAndVersionGreaterThan(aggregateId, fromVersion)
            .stream()
            .map(this::deserialize)
            .collect(Collectors.toList());
    }
}
```

#### Aggregate Root Pattern
```java
public abstract class AggregateRoot {
    
    private final List<DomainEvent> uncommittedEvents = new ArrayList<>();
    
    protected void addEvent(DomainEvent event) {
        uncommittedEvents.add(event);
    }
    
    public List<DomainEvent> getUncommittedEvents() {
        return Collections.unmodifiableList(uncommittedEvents);
    }
    
    public void clearEvents() {
        uncommittedEvents.clear();
    }
}

@Component
public class OrderAggregate extends AggregateRoot {
    
    private String orderId;
    private OrderStatus status;
    private List<OrderItem> items;
    
    public void createOrder(CreateOrderCommand command) {
        // Business validation
        if (command.getItems().isEmpty()) {
            throw new InvalidOrderException("Order must have at least one item");
        }
        
        // Apply event
        OrderCreatedEvent event = new OrderCreatedEvent(
            command.getOrderId(),
            command.getCustomerId(),
            command.getItems()
        );
        
        apply(event);
        addEvent(event);
    }
    
    private void apply(OrderCreatedEvent event) {
        this.orderId = event.getOrderId();
        this.status = OrderStatus.CREATED;
        this.items = new ArrayList<>(event.getItems());
    }
}
```

### 2. Reactive Programming

#### Reactive Streams Implementation
```java
@Service
public class ReactiveOrderService {
    
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    
    public Mono<Order> processOrder(OrderRequest request) {
        return validateOrder(request)
            .flatMap(this::reserveInventory)
            .flatMap(this::processPayment)
            .flatMap(this::saveOrder)
            .doOnError(this::handleOrderError)
            .doOnSuccess(order -> publishOrderCreatedEvent(order));
    }
    
    private Mono<OrderRequest> validateOrder(OrderRequest request) {
        return Mono.fromCallable(() -> {
            if (request.getItems().isEmpty()) {
                throw new InvalidOrderException("Order must contain items");
            }
            return request;
        });
    }
    
    private Mono<OrderRequest> reserveInventory(OrderRequest request) {
        return Flux.fromIterable(request.getItems())
            .flatMap(item -> inventoryService.reserve(item.getProductId(), item.getQuantity()))
            .then(Mono.just(request));
    }
    
    private Mono<OrderRequest> processPayment(OrderRequest request) {
        return paymentService.processPayment(request.getPaymentDetails())
            .map(paymentResult -> {
                request.setPaymentId(paymentResult.getPaymentId());
                return request;
            });
    }
    
    private Mono<Order> saveOrder(OrderRequest request) {
        Order order = new Order(request);
        return orderRepository.save(order);
    }
}

// Reactive Web Controller
@RestController
public class ReactiveOrderController {
    
    private final ReactiveOrderService orderService;
    
    @PostMapping("/orders")
    public Mono<ResponseEntity<Order>> createOrder(@RequestBody OrderRequest request) {
        return orderService.processOrder(request)
            .map(order -> ResponseEntity.status(HttpStatus.CREATED).body(order))
            .onErrorReturn(ResponseEntity.badRequest().build());
    }
    
    @GetMapping("/orders/{id}")
    public Mono<ResponseEntity<Order>> getOrder(@PathVariable String id) {
        return orderService.findById(id)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping(value = "/orders/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Order> streamOrders() {
        return orderService.streamOrders()
            .delayElements(Duration.ofSeconds(1));
    }
}
```

#### Backpressure Handling
```java
@Component
public class BackpressureHandler {
    
    public Flux<ProcessedData> processWithBackpressure(Flux<Data> dataStream) {
        return dataStream
            .onBackpressureBuffer(1000, BufferOverflowStrategy.DROP_LATEST)
            .flatMap(this::processData, 10) // Concurrency of 10
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)));
    }
    
    public Flux<Data> processWithSampling(Flux<Data> dataStream) {
        return dataStream
            .sample(Duration.ofMillis(100)) // Sample every 100ms
            .distinctUntilChanged()
            .flatMap(this::heavyProcessing);
    }
}
```

### 3. GraphQL Integration

#### GraphQL Schema and Resolvers
```java
// GraphQL Schema (schema.graphqls)
type Query {
    user(id: ID!): User
    users(first: Int, after: String): UserConnection
    order(id: ID!): Order
    orders(userId: ID!, first: Int, after: String): OrderConnection
}

type Mutation {
    createUser(input: CreateUserInput!): User
    updateUser(id: ID!, input: UpdateUserInput!): User
    createOrder(input: CreateOrderInput!): Order
}

type Subscription {
    orderStatusChanged(orderId: ID!): Order
    userNotifications(userId: ID!): Notification
}

// GraphQL Resolvers
@Component
public class UserResolver implements GraphQLQueryResolver, GraphQLMutationResolver {
    
    private final UserService userService;
    private final DataLoader<Long, User> userDataLoader;
    
    // Query resolvers
    public User user(Long id) {
        return userService.findById(id);
    }
    
    public Connection<User> users(int first, String after) {
        return userService.findAll(first, after);
    }
    
    // Mutation resolvers
    public User createUser(CreateUserInput input) {
        return userService.create(input);
    }
    
    public User updateUser(Long id, UpdateUserInput input) {
        return userService.update(id, input);
    }
}

@Component
public class OrderResolver implements GraphQLQueryResolver {
    
    private final OrderService orderService;
    
    // Field resolver for nested data
    public CompletableFuture<User> user(Order order, DataFetchingEnvironment environment) {
        DataLoader<Long, User> userLoader = environment.getDataLoader("userLoader");
        return userLoader.load(order.getUserId());
    }
    
    public CompletableFuture<List<Product>> products(Order order, DataFetchingEnvironment environment) {
        DataLoader<Long, Product> productLoader = environment.getDataLoader("productLoader");
        List<Long> productIds = order.getItems().stream()
            .map(OrderItem::getProductId)
            .collect(Collectors.toList());
        return productLoader.loadMany(productIds);
    }
}

// DataLoader for N+1 problem prevention
@Component
public class DataLoaderRegistry {
    
    @Bean
    public DataLoader<Long, User> userDataLoader(UserService userService) {
        return DataLoader.newDataLoader(keys -> {
            List<User> users = userService.findByIds(keys);
            Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
            
            return CompletableFuture.completedFuture(
                keys.stream()
                    .map(userMap::get)
                    .collect(Collectors.toList())
            );
        });
    }
}
```

#### Real-time Subscriptions
```java
@Component
public class SubscriptionResolver implements GraphQLSubscriptionResolver {
    
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    
    public Publisher<Order> orderStatusChanged(String orderId) {
        return redisTemplate.listenTo(ChannelTopic.of("order:" + orderId))
            .map(message -> objectMapper.readValue(message.getMessage(), Order.class))
            .cast(Order.class);
    }
    
    public Publisher<Notification> userNotifications(String userId) {
        return notificationService.getNotificationStream(userId);
    }
}
```

### 4. Domain-Driven Design (DDD)

#### Bounded Context Implementation
```java
// User Management Bounded Context
@Entity
@Table(name = "users", schema = "user_management")
public class User {
    @Id
    private UserId id;
    private Email email;
    private UserProfile profile;
    private List<Role> roles;
    
    public void changeEmail(Email newEmail, DomainEventPublisher eventPublisher) {
        Email oldEmail = this.email;
        this.email = newEmail;
        
        eventPublisher.publish(new UserEmailChangedEvent(this.id, oldEmail, newEmail));
    }
}

// Value Objects
@Embeddable
public class Email {
    private String value;
    
    public Email(String value) {
        if (!isValid(value)) {
            throw new InvalidEmailException("Invalid email format: " + value);
        }
        this.value = value;
    }
    
    private boolean isValid(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}

@Embeddable
public class UserId {
    private String value;
    
    public UserId() {
        this.value = UUID.randomUUID().toString();
    }
    
    public UserId(String value) {
        this.value = value;
    }
}

// Domain Service
@Service
public class UserRegistrationService {
    
    private final UserRepository userRepository;
    private final EmailVerificationService emailService;
    private final DomainEventPublisher eventPublisher;
    
    public User registerUser(RegisterUserCommand command) {
        // Domain logic
        if (userRepository.existsByEmail(command.getEmail())) {
            throw new UserAlreadyExistsException("User with email already exists");
        }
        
        User user = new User(
            new UserId(),
            new Email(command.getEmail()),
            new UserProfile(command.getFirstName(), command.getLastName())
        );
        
        // Send verification email
        emailService.sendVerificationEmail(user.getEmail());
        
        // Save and publish event
        User savedUser = userRepository.save(user);
        eventPublisher.publish(new UserRegisteredEvent(savedUser.getId()));
        
        return savedUser;
    }
}
```

#### Anti-Corruption Layer
```java
// External service integration with ACL
@Component
public class LegacyUserServiceAdapter {
    
    private final LegacyUserServiceClient legacyClient;
    
    public User findUserById(UserId userId) {
        LegacyUser legacyUser = legacyClient.getUser(userId.getValue());
        return translateToModernUser(legacyUser);
    }
    
    private User translateToModernUser(LegacyUser legacyUser) {
        return new User(
            new UserId(legacyUser.getUserId()),
            new Email(legacyUser.getEmailAddress()),
            new UserProfile(
                legacyUser.getFirstName(),
                legacyUser.getLastName()
            )
        );
    }
    
    public void syncUserToLegacy(User user) {
        LegacyUser legacyUser = translateToLegacyUser(user);
        legacyClient.updateUser(legacyUser);
    }
}
```

### 5. Advanced Security Patterns

#### Zero Trust Security
```java
@Component
public class ZeroTrustSecurityFilter implements Filter {
    
    private final JwtTokenValidator tokenValidator;
    private final DeviceFingerprinting deviceFingerprinting;
    private final RiskAssessment riskAssessment;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        // 1. Validate JWT token
        String token = extractToken(httpRequest);
        if (!tokenValidator.isValid(token)) {
            sendUnauthorized(response);
            return;
        }
        
        // 2. Device fingerprinting
        String deviceFingerprint = deviceFingerprinting.generate(httpRequest);
        if (!deviceFingerprinting.isTrusted(deviceFingerprint)) {
            sendAdditionalAuth(response);
            return;
        }
        
        // 3. Risk assessment
        RiskScore riskScore = riskAssessment.calculateRisk(httpRequest, token);
        if (riskScore.isHigh()) {
            sendStepUpAuth(response);
            return;
        }
        
        chain.doFilter(request, response);
    }
}
```

#### API Security with Rate Limiting and Throttling
```java
@Component
public class AdvancedRateLimiter {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    public boolean isAllowed(String identifier, RateLimitConfig config) {
        String key = "rate_limit:" + identifier;
        String script = """
            local key = KEYS[1]
            local window = tonumber(ARGV[1])
            local limit = tonumber(ARGV[2])
            local current_time = tonumber(ARGV[3])
            
            local current = redis.call('GET', key)
            if current == false then
                redis.call('SET', key, 1)
                redis.call('EXPIRE', key, window)
                return {1, limit - 1}
            else
                local count = tonumber(current)
                if count < limit then
                    redis.call('INCR', key)
                    return {count + 1, limit - count - 1}
                else
                    return {count, 0}
                end
            end
            """;
        
        List<Object> result = redisTemplate.execute(
            RedisScript.of(script, List.class),
            Collections.singletonList(key),
            String.valueOf(config.getWindowSeconds()),
            String.valueOf(config.getLimit()),
            String.valueOf(System.currentTimeMillis() / 1000)
        );
        
        return ((Number) result.get(1)).intValue() > 0;
    }
}
```

### 6. Performance Optimization

#### Database Optimization Patterns
```java
// Read/Write Splitting
@Configuration
public class DatabaseRoutingConfiguration {
    
    @Bean
    @Primary
    public DataSource routingDataSource() {
        RoutingDataSource routingDataSource = new RoutingDataSource();
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("write", writeDataSource());
        dataSourceMap.put("read", readDataSource());
        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(writeDataSource());
        return routingDataSource;
    }
}

// Custom annotation for routing
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReadOnlyRepository {
}

@Aspect
@Component
public class DatabaseRoutingAspect {
    
    @Before("@annotation(readOnlyRepository)")
    public void setReadDataSource(ReadOnlyRepository readOnlyRepository) {
        DatabaseContextHolder.setDataSourceType("read");
    }
    
    @After("@annotation(readOnlyRepository)")
    public void clearDataSource() {
        DatabaseContextHolder.clear();
    }
}

// Query optimization with JPA
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.orders WHERE u.id = :id")
    Optional<User> findByIdWithOrders(@Param("id") Long id);
    
    @Query(value = "SELECT * FROM users WHERE created_at >= :startDate", 
           countQuery = "SELECT count(*) FROM users WHERE created_at >= :startDate",
           nativeQuery = true)
    Page<User> findRecentUsers(@Param("startDate") LocalDateTime startDate, Pageable pageable);
    
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);
}
```

#### JVM Tuning for Microservices
```bash
# Production JVM Settings
JAVA_OPTS="-Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:G1HeapRegionSize=16m \
  -XX:+UseStringDeduplication \
  -XX:+OptimizeStringConcat \
  -XX:+UseCompressedOops \
  -XX:+UseCompressedClassPointers \
  -XX:+PrintGCDetails \
  -XX:+PrintGCTimeStamps \
  -XX:+PrintGCApplicationStoppedTime \
  -Xloggc:/var/log/gc.log \
  -XX:+UseGCLogFileRotation \
  -XX:NumberOfGCLogFiles=5 \
  -XX:GCLogFileSize=100M \
  -Dspring.profiles.active=production"
```

### 7. Observability & Monitoring

#### Custom Metrics and Alerts
```java
@Component
public class BusinessMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter orderCounter;
    private final Timer orderProcessingTime;
    private final Gauge activeUsers;
    
    public BusinessMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.orderCounter = Counter.builder("orders.created")
            .description("Number of orders created")
            .tag("service", "order-service")
            .register(meterRegistry);
            
        this.orderProcessingTime = Timer.builder("order.processing.time")
            .description("Time taken to process an order")
            .register(meterRegistry);
            
        this.activeUsers = Gauge.builder("users.active")
            .description("Number of active users")
            .register(meterRegistry, this, BusinessMetrics::getActiveUserCount);
    }
    
    public void recordOrderCreated(String orderType) {
        orderCounter.increment(Tags.of("type", orderType));
    }
    
    public void recordOrderProcessingTime(Duration duration) {
        orderProcessingTime.record(duration);
    }
    
    private double getActiveUserCount() {
        // Implement logic to count active users
        return userService.getActiveUserCount();
    }
}

// Custom Health Indicators
@Component
public class DatabaseConnectionHealthIndicator implements HealthIndicator {
    
    private final DataSource dataSource;
    
    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(1);
            if (isValid) {
                return Health.up()
                    .withDetail("database", "Available")
                    .withDetail("connectionPool", getConnectionPoolInfo())
                    .build();
            } else {
                return Health.down()
                    .withDetail("database", "Connection validation failed")
                    .build();
            }
        } catch (SQLException e) {
            return Health.down()
                .withDetail("database", "Connection failed")
                .withException(e)
                .build();
        }
    }
}
```

#### Distributed Tracing with Custom Spans
```java
@Component
public class TracingService {
    
    private final Tracer tracer;
    
    public void processOrderWithTracing(Order order) {
        Span span = tracer.nextSpan()
            .name("order-processing")
            .tag("order.id", order.getId().toString())
            .tag("order.type", order.getType())
            .start();
        
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            // Add custom events
            span.annotate("validation.started");
            validateOrder(order);
            span.annotate("validation.completed");
            
            span.annotate("inventory.check.started");
            checkInventory(order);
            span.annotate("inventory.check.completed");
            
            span.annotate("payment.started");
            processPayment(order);
            span.annotate("payment.completed");
            
        } catch (Exception e) {
            span.tag("error", e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }
    
    private void validateOrder(Order order) {
        Span childSpan = tracer.nextSpan()
            .name("order-validation")
            .tag("validation.rules", "basic,inventory,pricing")
            .start();
        
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(childSpan)) {
            // Validation logic
        } finally {
            childSpan.end();
        }
    }
}
```

---

## Advanced Project: Financial Trading Platform

### Architecture Overview
A comprehensive financial trading platform demonstrating advanced microservices patterns:

- **User Service**: Account management, KYC verification
- **Market Data Service**: Real-time market feeds, historical data
- **Trading Service**: Order management, execution
- **Portfolio Service**: Position tracking, P&L calculation
- **Risk Management Service**: Real-time risk monitoring
- **Settlement Service**: Trade settlement, clearing
- **Notification Service**: Real-time alerts
- **Audit Service**: Regulatory compliance, trade reporting

### Key Implementation Highlights

#### 1. High-Frequency Trading Engine
```java
@Component
public class HighFrequencyTradingEngine {
    
    private final DisruptorOrderProcessor orderProcessor;
    private final MarketDataProcessor marketDataProcessor;
    private final RiskManager riskManager;
    
    public void processOrder(TradingOrder order) {
        // Use Disruptor for ultra-low latency
        orderProcessor.processOrder(order);
    }
}

// Disruptor-based order processing
@Configuration
public class DisruptorConfig {
    
    @Bean
    public Disruptor<OrderEvent> orderDisruptor() {
        Disruptor<OrderEvent> disruptor = new Disruptor<>(
            OrderEvent::new,
            1024 * 1024, // Ring buffer size
            DaemonThreadFactory.INSTANCE,
            ProducerType.MULTI,
            new YieldingWaitStrategy()
        );
        
        disruptor.handleEventsWith(new OrderValidationHandler())
            .then(new RiskCheckHandler())
            .then(new OrderExecutionHandler());
            
        return disruptor;
    }
}
```

#### 2. Real-time Market Data Processing
```java
@Component
public class MarketDataStreamProcessor {
    
    public Flux<MarketData> processMarketDataStream() {
        return webSocketClient.receive()
            .map(this::parseMarketData)
            .groupBy(MarketData::getSymbol)
            .flatMap(groupedFlux -> 
                groupedFlux
                    .window(Duration.ofMillis(100))
                    .flatMap(this::aggregateMarketData)
            )
            .doOnNext(this::publishToSubscribers);
    }
    
    private Mono<MarketData> aggregateMarketData(Flux<MarketData> window) {
        return window
            .reduce(new MarketDataAggregator())
            .map(MarketDataAggregator::getResult);
    }
}
```

#### 3. Event Sourcing for Trade Lifecycle
```java
// Trade Aggregate
public class Trade extends AggregateRoot {
    
    private TradeId tradeId;
    private TradeStatus status;
    private BigDecimal quantity;
    private BigDecimal price;
    
    public void execute(ExecuteTradeCommand command) {
        TradeExecutedEvent event = new TradeExecutedEvent(
            command.getTradeId(),
            command.getQuantity(),
            command.getPrice(),
            Instant.now()
        );
        
        apply(event);
        addEvent(event);
    }
    
    public void settle(SettleTradeCommand command) {
        if (status != TradeStatus.EXECUTED) {
            throw new InvalidTradeStateException("Cannot settle non-executed trade");
        }
        
        TradeSettledEvent event = new TradeSettledEvent(
            tradeId,
            command.getSettlementDate(),
            command.getSettlementAmount()
        );
        
        apply(event);
        addEvent(event);
    }
}
```

---

## Learning Path & Next Steps

### Phase 1: Foundation (Weeks 1-4)
1. Master Spring Boot fundamentals
2. Understand REST API design principles
3. Learn Docker and containerization
4. Practice with basic CRUD microservices

### Phase 2: Core Patterns (Weeks 5-8)
1. Implement service discovery with Eureka
2. Add circuit breakers and resilience patterns
3. Master database per service pattern
4. Implement async communication with message queues

### Phase 3: Advanced Concepts (Weeks 9-12)
1. Event sourcing and CQRS implementation
2. Distributed tracing and monitoring
3. Security patterns and OAuth2
4. Performance optimization techniques

### Phase 4: Production Ready (Weeks 13-16)
1. Kubernetes deployment and orchestration
2. CI/CD pipeline implementation
3. Comprehensive testing strategies
4. Monitoring and observability

### Recommended Reading
1. **"Microservices Patterns" by Chris Richardson**
2. **"Building Microservices" by Sam Newman**
3. **"Spring Microservices in Action" by John Carnell**
4. **"Reactive Spring" by Josh Long**

### Practice Platforms
1. **GitHub**: Build and showcase projects
2. **Docker Hub**: Container registry practice
3. **AWS/GCP/Azure**: Cloud deployment
4. **Kubernetes Playground**: Orchestration practice

### Interview Preparation Timeline
- **Month 1-2**: Master fundamentals and build basic projects
- **Month 3**: Implement intermediate projects with advanced patterns
- **Month 4**: Focus on system design and architecture questions
- **Month 5**: Practice coding problems and behavioral questions
- **Month 6**: Mock interviews and final preparation

This comprehensive guide covers everything needed to master microservices in Java and excel in FAANG interviews. Focus on hands-on implementation, understand the trade-offs of different patterns, and always consider scalability, reliability, and maintainability in your solutions.