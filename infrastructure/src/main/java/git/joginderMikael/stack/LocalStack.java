package git.joginderMikael.stack;
import software.amazon.awscdk.*;

public class LocalStack extends Stack {

    public LocalStack(
            final App scope,
            final String id,
            final StackProps props) {

        super(scope, id, props);
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
