package hexlet.code.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;
import java.time.Instant;


@Getter
@Setter
@ToString
public final class Url {

    private long id;

    private String name;

    private Timestamp createdAt;

    public Url(String name) {
        this.name = name;
    }

    public Instant getCreatedAtTime() {
        return createdAt.toInstant();
    }
}

