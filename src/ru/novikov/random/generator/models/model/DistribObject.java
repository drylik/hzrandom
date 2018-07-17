package ru.novikov.random.generator.models.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author anovikov
 * @date 17.07.18
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class DistribObject {
    private Long timestamp;
    private String clientSeed;
    private String mySeed;

    public DistribObject(Long timestamp, String clientSeed) {
        this.timestamp = timestamp;
        this.clientSeed = clientSeed;
    }
}
