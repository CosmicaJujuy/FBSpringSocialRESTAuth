# FBSpringSocialRESTAuth
Spring Authentication Filter for Stateless REST Endpoints which use Facebook Token for authentication.

We have been looking for a "Spring" solution which secures our REST backends using Facebook OAuth Token that REST clients 
already have in hand. For example: You have a mobile app with Facebook Connect SDK implemented in the app itself and on the other hand 
, you have a backend which provides REST APIs. You want to authenticate REST API calls with Facebook OAuth Token. The solution realizes this scenario. 

Unfortunately, Spring Social Security Framework only secures your stateful HTTP requests, not your stateless REST backend. 

This is an extension of spring social security framework which consists of one component: FacebookTokenAuthenticationFilter. This filter intercepts all REST calls. 
The clients should send Facebook OAuth Token in the url as "input_token" parameter in every request as REST APIs are steteless in nature. The Filter looks for this token and validates it by "debug_token" Graph Api call. 
If the token is validated, the filter tries to match the user with the local user management system.  If there is no such user registered yet, the filter registers the user as a new user. 

You can use this Filter together with standard SocialAuthenticationFilter of Spring Social Security if you have also services other 
than your REST API like a web backend. So you can use the same user management system.

1) Create your user table as follows in MYSQL :
  
  
    CREATE TABLE IF NOT EXISTS `user` (
      `id` varchar(50) NOT NULL,
      `email` varchar(255) NOT NULL COMMENT 'unique',
      `first_name` varchar(255) NOT NULL,
      `last_name` varchar(255) NOT NULL,
      `password` varchar(255) DEFAULT NULL,
      `role` varchar(255) NOT NULL,
      `sign_in_provider` varchar(20) DEFAULT NULL,
      `creation_time` datetime NOT NULL,
      `modification_time` datetime NOT NULL,
      `status` varchar(20) NOT NULL COMMENT 'not used',
      PRIMARY KEY (`id`),
      UNIQUE KEY `email` (`email`)
    );
  
2) Configure your data source in context.xml :

context.xml in tomcat :

    <Resource auth="Container" driverClassName="com.mysql.jdbc.Driver" maxActive="100" maxIdle="30" maxWait="10000" 
    name="jdbc/thingabled" password="..." type="javax.sql.DataSource" url="jdbc:mysql://localhost:3306/..." username="..."/>
  
3) Spring Configuration : We configure the spring security to intercept URLs beginning with "protected" by FacebookTokenAuthenticationFilter for authentication. Authorization will be done by "ROLE_USER_REST_MOBILE" role.
  
  
    <security:http use-expressions="true" pattern="/protected/**"
    create-session="never" entry-point-ref="forbiddenEntryPoint">
      <security:intercept-url pattern="/**"
      access="hasRole('ROLE_USER_REST_MOBILE')" />
    <!-- Adds social authentication filter to the Spring Security filter chain. -->
      <security:custom-filter ref="facebookTokenAuthenticationFilter"
      before="FORM_LOGIN_FILTER" />
    </security:http>
    
    
    <bean id="facebookTokenAuthenticationFilter"
    class="com.ozgen.server.security.oauth.FacebookTokenAuthenticationFilter">
      <constructor-arg index="0" ref="authenticationManager" />
      <constructor-arg index="1" ref="userIdSource" />
      <constructor-arg index="2" ref="usersConnectionRepository" />
      <constructor-arg index="3" ref="connectionFactoryLocator" />
    </bean>
    
    <security:authentication-manager alias="authenticationManager">
      <security:authentication-provider
      ref="socialAuthenticationProvider" />
    </security:authentication-manager>
    
    <!-- Configures the social authentication provider which processes authentication 
    requests made by using social authentication service (FB). -->
    <bean id="socialAuthenticationProvider"
    class="org.springframework.social.security.SocialAuthenticationProvider">
      <constructor-arg index="0" ref="usersConnectionRepository" />
      <constructor-arg index="1" ref="simpleSocialUserDetailsService" />
    </bean>
    
    <bean id="forbiddenEntryPoint"
    class="org.springframework.security.web.authentication.Http403ForbiddenEntryPoint" />
    
    <!-- This bean determines the account ID of the user.-->
    <bean id="userIdSource"
    class="org.springframework.social.security.AuthenticationNameUserIdSource" />
    
    <!-- This is used to hash the password of the user. -->
    <bean id="passwordEncoder"
    class="org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder">
      <constructor-arg index="0" value="10" />
    </bean>
    <!-- This bean encrypts the authorization details of the connection. In 
    our example, the authorization details are stored as plain text. DO NOT USE 
    THIS IN PRODUCTION. -->
    <bean id="textEncryptor" class="org.springframework.security.crypto.encrypt.Encryptors"
    factory-method="noOpText" />
      
4) All stateless REST requests will be intercepted by FacebookTokenAuthenticationFilter to authenticate requests using a valid Facebook Token.
Checks whether Facebook token is valid .
If Facebook token is not valid then request will be denied.
If Facebook token isvalid then the filter will try to authenticate the request via SimpleSocialUserDetailsService. If user and userconnection data isn't available, a new user(via UserService) and UserConnection is created.
  

  
    private Authentication attemptAuthService(...) {
      URIBuilder builder = URIBuilder.fromUri(String.format("%s/debug_token", "https://graph.facebook.com"));
      builder.queryParam("access_token", "...");
      builder.queryParam("input_token", request.getParameter("input_token"));
  
      URI uri = builder.build();
      RestTemplate restTemplate = new RestTemplate();
      JsonNode resp = restTemplate.getForObject(uri, JsonNode.class);
  
      Boolean isValid = resp.path("data").findValue("is_valid").asBoolean();
  
      if (!isValid)
          throw new SocialAuthenticationException("Token is not valid");
  
      AccessGrant accessGrant = new AccessGrant(request.getParameter("input_token"), null, null,
              resp.path("data").findValue("expires_at").longValue());
  
      Connection<?> connection = ((OAuth2ConnectionFactory<?>) authService.getConnectionFactory())
              .createConnection(accessGrant);
      SocialAuthenticationToken token = new SocialAuthenticationToken(connection, null);
      Assert.notNull(token.getConnection());
  
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth == null || !auth.isAuthenticated()) {
          return doAuthentication(authService, request, token);
      } else {
          addConnection(authService, request, token);
          return null;
      }
    }
  
5) Other important sections in the project :

User : Entity which maps 'user' table.

  
    @Entity
    @Table(name = "user")
    public class User extends BaseEntity {

      @Column(name = "email", length = 255, nullable = false, unique = true)
      private String email;
  
      @Column(name = "first_name", length = 255, nullable = false)
      private String firstName;
  
      @Column(name = "last_name", length = 255, nullable = false)
      private String lastName;
  
      @Column(name = "password", length = 255)
      private String password;
  
      @Column(name = "role", length = 255, nullable = false)
      private String rolesString;
  
      @Enumerated(EnumType.STRING)
      @Column(name = "sign_in_provider", length = 20)
      private SocialMediaService signInProvider;
  
      ...
    }


UserRepository : Spring Data JPA repository which will enable us to run CRUD operations on 'User' entity.

    public interface UserRepository extends JpaRepository<User, String> {
      public User findByEmailAndStatus(String email,Status status);
      public User findByIdAndStatus(String id,Status status);
    }


UserService : This spring service will be used to create a new user account inserting data to 'user' table.
  
    @Service
    public class UserService {
      private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
      
      @Autowired
      private UserRepository repository;
      
      @Transactional
      public User registerNewUserAccount(RegistrationForm userAccountData) throws DuplicateEmailException {
          LOGGER.debug("Registering new user account with information: {}", userAccountData);
      
          if (emailExist(userAccountData.getEmail())) {
              LOGGER.debug("Email: {} exists. Throwing exception.", userAccountData.getEmail());
              throw new DuplicateEmailException("The email address: " + userAccountData.getEmail() + " is already in use.");
          }
      
          LOGGER.debug("Email: {} does not exist. Continuing registration.", userAccountData.getEmail());
      
          User registered =User.newEntity();
          registered.setEmail(userAccountData.getEmail());
          registered.setFirstName(userAccountData.getFirstName());
          registered.setLastName(userAccountData.getLastName());
          registered.setPassword(null);
          registered.addRole(User.Role.ROLE_USER_WEB);
          registered.addRole(User.Role.ROLE_USER_REST);
          registered.addRole(User.Role.ROLE_USER_REST_MOBILE);
      
          if (userAccountData.isSocialSignIn()) {
              registered.setSignInProvider(userAccountData.getSignInProvider());
          }
      
          LOGGER.debug("Persisting new user with information: {}", registered);
      
          return repository.save(registered);
      }
      .... 
    }
  
SimpleSocialUserDetailsService : This Spring service Will be used by SocialAuthenticationProvider to authenticate userId of the user.

    @Service
    public class SimpleSocialUserDetailsService implements SocialUserDetailsService {
      private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSocialUserDetailsService.class);
      @Autowired
      private UserRepository repository;
    
      @Override
      public SocialUserDetails loadUserByUserId(String userId) throws UsernameNotFoundException, DataAccessException {
          LOGGER.debug("Loading user by user id: {}", userId);
    
          User user = repository.findByEmailAndStatus(userId, Status.ENABLED);
          LOGGER.debug("Found user: {}", user);
    
          if (user == null) {
              throw new UsernameNotFoundException("No user found with username: " + userId);
          }
    
          ThingabledUserDetails principal = new ThingabledUserDetails(user.getEmail(),user.getPassword(),user.getAuthorities());
          principal.setFirstName(user.getFirstName());
          principal.setId(user.getId());
          principal.setLastName(user.getLastName());
          principal.setSocialSignInProvider(user.getSignInProvider());
    
    
          LOGGER.debug("Found user details: {}", principal);
    
          return principal;
      }
    } 
