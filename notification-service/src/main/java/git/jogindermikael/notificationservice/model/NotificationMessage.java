package git.jogindermikael.notificationservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name="notification_message")
public class NotificationMessage {
    @Id private UUID id;
    @Column(nullable=false) private UUID recipientId;
    @Column(nullable=false,length=20) private String channel;
    @Column(nullable=false) private String destination;
    @Column(nullable=false) private String template;
    @Column(nullable=false,columnDefinition="TEXT") private String body;
    @Column(nullable=false,length=24) private String status;
    private UUID correlationId;
    @Column(nullable=false) private Instant createdAt;
    @Column(nullable=false) private Instant nextAttemptAt;
    private Instant sentAt;
    @Column(nullable=false) private int attempts;
    private String providerMessageId;
    @Column(columnDefinition="TEXT") private String lastError;
    @Version private long version;

    protected NotificationMessage() {}
    public NotificationMessage(UUID id,UUID recipientId,String channel,String destination,String template,String body,String status,UUID correlationId,Instant createdAt){this.id=id;this.recipientId=recipientId;this.channel=channel.toUpperCase();this.destination=destination;this.template=template;this.body=body;this.status=status;this.correlationId=correlationId;this.createdAt=createdAt;this.nextAttemptAt=createdAt;}
    public UUID id(){return id;} public UUID getId(){return id;} public UUID recipientId(){return recipientId;} public UUID getRecipientId(){return recipientId;}
    public String channel(){return channel;} public String getChannel(){return channel;} public String destination(){return destination;} public String getDestination(){return destination;}
    public String template(){return template;} public String getTemplate(){return template;} public String body(){return body;} public String getBody(){return body;}
    public String status(){return status;} public String getStatus(){return status;} public UUID correlationId(){return correlationId;} public UUID getCorrelationId(){return correlationId;}
    public Instant createdAt(){return createdAt;} public Instant getCreatedAt(){return createdAt;} public Instant getNextAttemptAt(){return nextAttemptAt;} public Instant getSentAt(){return sentAt;}
    public int getAttempts(){return attempts;} public String getProviderMessageId(){return providerMessageId;} public String getLastError(){return lastError;} public long getVersion(){return version;}
    public void scheduleAt(Instant instant){nextAttemptAt=instant;}
    public void markSent(String providerId){attempts++;status="SENT";providerMessageId=providerId;sentAt=Instant.now();lastError=null;}
    public void markFailed(String error,int maxAttempts){attempts++;lastError=error == null ? "Provider delivery failed" : error.substring(0,Math.min(error.length(),1000));status=attempts>=maxAttempts?"DEAD_LETTER":"FAILED";nextAttemptAt=Instant.now().plus((long)Math.pow(2,Math.min(attempts,10)),ChronoUnit.MINUTES);}
    public void applyProviderCallback(String providerStatus,String detail){
        String normalized=providerStatus.toUpperCase();
        if(!java.util.Set.of("DELIVERED","BOUNCED","FAILED").contains(normalized))throw new IllegalArgumentException("Unsupported provider status");
        status=normalized;lastError="DELIVERED".equals(normalized)?null:(detail==null?normalized:detail.substring(0,Math.min(detail.length(),1000)));
        if("DELIVERED".equals(normalized)&&sentAt==null)sentAt=Instant.now();
    }
    public void replay(){
        if(!java.util.Set.of("DEAD_LETTER","FAILED","BOUNCED").contains(status))throw new IllegalStateException("Notification is not replayable");
        status="QUEUED";attempts=0;nextAttemptAt=Instant.now();lastError=null;sentAt=null;providerMessageId=null;
    }
}
