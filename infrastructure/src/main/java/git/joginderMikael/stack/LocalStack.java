package git.jogindermikael.stack;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.Protocol;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.msk.CfnCluster;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.route53.CfnHealthCheck;
import software.amazon.awscdk.services.secretsmanager.Secret;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class LocalStack extends Stack {
    private final Vpc vpc;
    private final Cluster ecsCluster;


    public LocalStack(
            final App scope,
            final String id,
            final StackProps props) {

        super(scope, id, props);

        this.vpc = createVpc();

        boolean isLocalStack = true; // Flag to toggle between local and cloud

        DatabaseInstance authServiceDb = null;
        DatabaseInstance patientServiceDb = null;
        CfnHealthCheck authDbHealthCheck = null;
        CfnHealthCheck patientDbHealthCheck = null;

        if (!isLocalStack) {
            authServiceDb = createDatabase("AuthServiceDB", "auth-service-db");
            patientServiceDb = createDatabase("PatientServiceDB", "patient-service-db");

            authDbHealthCheck = createDbHealthCheck(authServiceDb, "AuthServiceDBHealthCheck");
            patientDbHealthCheck = createDbHealthCheck(patientServiceDb, "PatientServiceDBHealthCheck");
        }

        CfnCluster mskCluster = null;
        if (!isLocalStack) {
            mskCluster = createMskCluster();
        }

        this.ecsCluster = createEcsCluster();
        Secret jwtSecret = Secret.Builder.create(this, "JwtSigningSecret")
                .secretName("/patient-management/shared/jwt-signing-secret")
                .description("Shared HMAC signing key for local JWT resource-server validation. Replace with OIDC/JWKS in production.")
                .generateSecretString(software.amazon.awscdk.services.secretsmanager.SecretStringGenerator.builder()
                        .excludePunctuation(true)
                        .passwordLength(64)
                        .build())
                .build();
        Map<String, software.amazon.awscdk.services.ecs.Secret> jwtSecretEnv =
                Map.of("JWT_SECRET", software.amazon.awscdk.services.ecs.Secret.fromSecretsManager(jwtSecret));

        FargateService authService = createFargateService("AuthService", "auth-service",
                List.of(4005),
                authServiceDb,
                Map.of(),
                jwtSecretEnv);
        if (authDbHealthCheck != null) authService.getNode().addDependency(authDbHealthCheck);
        if (authServiceDb != null) authService.getNode().addDependency(authServiceDb);

        FargateService billingService = createFargateService("BillingService", "billing-service",
                List.of(4001, 9001),
                null, null, jwtSecretEnv);

        FargateService analyticsService = createFargateService("AnalyticsService", "analytics-service",
                List.of(4002), null, null, jwtSecretEnv);
        if (mskCluster != null) analyticsService.getNode().addDependency(mskCluster);

        FargateService appointmentService = createFargateService("AppointmentService", "appointment-service",
                List.of(4010), null, null, jwtSecretEnv);

        FargateService ehrService = createFargateService("EhrService", "ehr-service",
                List.of(4011), null, null, jwtSecretEnv);

        FargateService insuranceService = createFargateService("InsuranceService", "insurance-service",
                List.of(4012), null, null, jwtSecretEnv);

        FargateService notificationService = createFargateService("NotificationService", "notification-service",
                List.of(4013), null, null, jwtSecretEnv);
        if (mskCluster != null) notificationService.getNode().addDependency(mskCluster);

        FargateService inventoryPharmacyService = createFargateService("InventoryPharmacyService", "inventory-pharmacy-service",
                List.of(4014), null, null, jwtSecretEnv);

        FargateService auditComplianceService = createFargateService("AuditComplianceService", "audit-compliance-service",
                List.of(4015), null, null, jwtSecretEnv);
        if (mskCluster != null) auditComplianceService.getNode().addDependency(mskCluster);

        FargateService patientPortalService = createFargateService("PatientPortalService", "patient-portal-service",
                List.of(4016), null, null, jwtSecretEnv);

        FargateService staffDashboardService = createFargateService("StaffDashboardService", "staff-dashboard-service",
                List.of(4017), null, null, jwtSecretEnv);

        FargateService patientService = createFargateService("PatientService", "patient-service",
                List.of(4000),
                patientServiceDb,
                Map.of(
                        "BILLING_SERVICE_ADDRESS","host.docker.internal",
                        "BILLING_SERVICE_GRPC_PORT", "9001"
                ),
                jwtSecretEnv);
        if (patientServiceDb != null) patientService.getNode().addDependency(patientServiceDb);
        if (patientDbHealthCheck != null) patientService.getNode().addDependency(patientDbHealthCheck);
        patientService.getNode().addDependency(billingService);
        if (mskCluster != null) patientService.getNode().addDependency(mskCluster);
        appointmentService.getNode().addDependency(patientService);
        ehrService.getNode().addDependency(patientService);
        insuranceService.getNode().addDependency(billingService);
        notificationService.getNode().addDependency(appointmentService);
        inventoryPharmacyService.getNode().addDependency(ehrService);
        auditComplianceService.getNode().addDependency(patientService);
        patientPortalService.getNode().addDependency(patientService);
        patientPortalService.getNode().addDependency(appointmentService);
        staffDashboardService.getNode().addDependency(ehrService);

        createApiGatewayService(jwtSecretEnv);

    }



    private Vpc createVpc() {
       return Vpc.Builder.create(this, "PatientManagementVPC")
               .vpcName("PatientManagementVPC")
                .maxAzs(2)
                .build();
    }

    private DatabaseInstance createDatabase(String id, String dbName) {
        return DatabaseInstance.Builder
                .create(this, id)
                .engine(DatabaseInstanceEngine.postgres(PostgresInstanceEngineProps.builder()
                        .version(PostgresEngineVersion.VER_17_2)
                        .build()))
                .vpc(vpc)
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO))
                .allocatedStorage(20)
                .credentials(Credentials.fromGeneratedSecret("admin_user"))
                .databaseName(dbName)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
    }


    private CfnHealthCheck createDbHealthCheck(DatabaseInstance db, String id) {
        // LocalStack doesn't provide valid RDS endpoint attributes; use local defaults for health checks
        return CfnHealthCheck.Builder
                .create(this, id)
                .healthCheckConfig(CfnHealthCheck.HealthCheckConfigProperty.builder()
                        .type("TCP")
                        // Postgres default port for local DB emulation
                        .port(5432)
                        // Use loopback address for local health checks
                        .ipAddress("127.0.0.1")
                        .requestInterval(30)
                        .failureThreshold(3)
                        .build())
                .build();
    }

    private CfnCluster createMskCluster(){
        return CfnCluster.Builder.create(this, "MskCluster")
                .clusterName("kafka-cluster")
                .kafkaVersion("2.8.0")
                .numberOfBrokerNodes(1)
                .brokerNodeGroupInfo(CfnCluster.BrokerNodeGroupInfoProperty.builder()
                        .instanceType("kafka.m5.xlarge")
                        .clientSubnets(vpc.getPrivateSubnets().stream()
                                .map(ISubnet::getSubnetId)
                                .collect(Collectors.toList()))
                        .brokerAzDistribution("DEFAULT")
                        .build())
                .build();
    }


    private Cluster createEcsCluster() {
        return Cluster.Builder
                .create(this, "PatientManagementCluster")
                .vpc(vpc)
                .build();
    }

    private FargateService createFargateService(String id,
                                                String imageName,
                                                List<Integer> ports,
                                                DatabaseInstance db,
                                                Map<String, String> additionalEnvVars,
                                                Map<String, software.amazon.awscdk.services.ecs.Secret> additionalSecrets) {
        FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder
                .create(this, id + "Task")
                .cpu(256)
                .memoryLimitMiB(512)
                .build();

        ContainerDefinitionOptions.Builder containerOptions =
                ContainerDefinitionOptions.builder()
                        .image(ContainerImage.fromRegistry(imageName))
                        .portMappings(ports.stream()
                                .map(port -> PortMapping.builder()
                                        .containerPort(port)
                                        .hostPort(port)
                                        .protocol(Protocol.TCP)
                                        .build())
                                .toList())
                        .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                        .logGroup(LogGroup.Builder.create(this, id + "LogGroup")
                                                .logGroupName("/ecs" + imageName)
                                                .removalPolicy(RemovalPolicy.DESTROY)
                                                .retention(RetentionDays.ONE_DAY)
                                                .build())
                                        .streamPrefix(imageName)
                                .build()));


        Map<String, String> enVars = new HashMap<>();
        enVars.put("SPRING_KAFKA_BOOTSTRAP_SERVERS", "localhost.localstack.cloud:4510, localhost.localstack.cloud:4511, localhost.localstack.cloud:4512");
        if (additionalEnvVars != null) {
            enVars.putAll(additionalEnvVars);
        }
        Map<String, software.amazon.awscdk.services.ecs.Secret> secretVars = new HashMap<>();
        if (additionalSecrets != null) {
            secretVars.putAll(additionalSecrets);
        }

        if(db != null) {
            enVars.put("SPRING_DATASOURCE_URL", "jdbc:postgresql://%s:%s/%s-db".formatted(
                    db.getDbInstanceEndpointAddress(),
                    db.getDbInstanceEndpointPort(),
                    imageName
            ));
            enVars.put("SPRING_DATASOURCE_USERNAME", "admin_user");
            secretVars.put("SPRING_DATASOURCE_PASSWORD",
                    software.amazon.awscdk.services.ecs.Secret.fromSecretsManager(Objects.requireNonNull(db.getSecret()), "password"));
            enVars.put("SPRING_JPA_HIBERNATE_DDL_AUTO", "validate");
            enVars.put("SPRING_SQL_INIT_MODE", "never");
            enVars.put("SPRING_FLYWAY_ENABLED", "true");
            enVars.put("SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT", "60000");
        }

        containerOptions.environment(enVars);
        if (!secretVars.isEmpty()) {
            containerOptions.secrets(secretVars);
        }
        taskDefinition.addContainer(imageName + "container", containerOptions.build());

        return FargateService.Builder.create(this, id)
                .cluster(ecsCluster)
                .taskDefinition(taskDefinition)
                .assignPublicIp(false)
                .serviceName(imageName)
                .build();
    }

    private void createApiGatewayService(Map<String, software.amazon.awscdk.services.ecs.Secret> secrets){

        FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder
                .create(this, "APIGatewayTaskDefinitionv")
                .cpu(256)
                .memoryLimitMiB(512)
                .build();


        ContainerDefinitionOptions containerOptions =
                ContainerDefinitionOptions.builder()
                        .image(ContainerImage.fromRegistry("api-gateway"))
                        .environment(
                                Map.of(
                                        "SPRING_PROFILES_ACTIVE", "prod",
                                        "AUTH_SERVICE_URL", "http://host.docker.internal:4005"
                                )
                        )
                        .secrets(secrets)
                        .portMappings(List.of(4004).stream()
                                .map(port -> PortMapping.builder()
                                        .containerPort(port)
                                        .hostPort(port)
                                        .protocol(Protocol.TCP)
                                        .build())
                                .toList())
                        .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                .logGroup(LogGroup.Builder.create(this, "APIGatewayLogGroup")
                                        .logGroupName("/ecs/api-gateway")
                                        .removalPolicy(RemovalPolicy.DESTROY)
                                        .retention(RetentionDays.ONE_DAY)
                                        .build())
                                .streamPrefix("api-gateway")
                                .build()))
                        .build();

        taskDefinition.addContainer("APIGatewayContainer", containerOptions);

        ApplicationLoadBalancedFargateService apiGateway = ApplicationLoadBalancedFargateService.Builder
                .create(this, "APIGatewayService")
                .cluster(ecsCluster)
                .serviceName("api-gateway")
                .taskDefinition(taskDefinition)
                .desiredCount(1)
                .healthCheckGracePeriod(Duration.seconds(60))
                .build();
    }



    public static void main(final String[] args) {
        // Workaround for ENOTEMPTY error on Windows during jsii cleanup
        // Set as early as possible via static block
        System.setProperty("jsii.keep", "true");

        System.out.println("Starting synthesis...");

        App app = new App(
                AppProps.builder()
                        .outdir("./infrastructure/cdk.out")
                        .build()
        );

        StackProps props = StackProps.builder()
                .synthesizer(new BootstraplessSynthesizer())
                .build();

        new LocalStack(app, "localstack", props);

        app.synth();

        System.out.println(
                "App synthesis complete. Check the 'cdk.out' folder for output files."
        );
    }
}
