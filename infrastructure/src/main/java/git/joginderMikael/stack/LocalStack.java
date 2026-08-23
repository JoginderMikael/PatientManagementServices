package git.joginderMikael.stack;
import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.rds.DatabaseInstance;
import software.amazon.awscdk.services.rds.DatabaseInstanceEngine;
import software.amazon.awscdk.services.rds.PostgresEngineVersion;
import software.amazon.awscdk.services.rds.PostgresInstanceEngineProps;

public class LocalStack extends Stack {
    private final Vpc vpc;


    public LocalStack(
            final App scope,
            final String id,
            final StackProps props) {

        super(scope, id, props);

        this.vpc = createVpc();
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
                .build();
    }
    public static void main(final String[] args) {

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
