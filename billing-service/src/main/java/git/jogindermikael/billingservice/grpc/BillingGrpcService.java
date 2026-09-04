package git.jogindermikael.billingservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc.BillingServiceImplBase;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import git.jogindermikael.billingservice.model.BillingAccount;
import git.jogindermikael.billingservice.service.BillingAccountService;
import io.grpc.Status;
import java.util.UUID;

@GrpcService
public class BillingGrpcService extends BillingServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(BillingGrpcService.class);
    private final BillingAccountService accountService;

    public BillingGrpcService(BillingAccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void createBillingAccount(BillingRequest billingRequest,
                                     StreamObserver<BillingResponse> responseObserver) {
        log.info("Create billing account request received for patientId={}", billingRequest.getPatientId());

        try {
            UUID patientId = UUID.fromString(billingRequest.getPatientId());
            BillingAccount account = accountService.create(patientId, "grpc-patient:" + patientId);
            responseObserver.onNext(BillingResponse.newBuilder()
                    .setAccountId(account.getId().toString())
                    .setStatus(account.getStatus())
                    .build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException exception) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(exception.getMessage()).asRuntimeException());
        } catch (Exception exception) {
            log.error("Unable to create billing account", exception);
            responseObserver.onError(Status.INTERNAL.withDescription("Unable to create billing account").asRuntimeException());
        }
    }
}
