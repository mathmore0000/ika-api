package ika.entities.aux_classes.notifications;

import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class NotificationResponse {
    private UUID id;
    private boolean seen;
    private OffsetDateTime createdAt;
    private OffsetDateTime seenAt;
    private String message;
    @JdbcTypeCode(SqlTypes.JSON)
    private Object detailedMessage;
}
