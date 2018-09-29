package ru.novikov.random.generator.models.model;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.novikov.random.generator.models.factories.DistribObjectDataSerializableFactory;

import java.io.IOException;

/**
 * @author anovikov
 * @date 17.07.18
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class DistribObject implements IdentifiedDataSerializable {
    private final static int ID = 1;

    private Long timestamp;
    private String clientSeed;
    private String mySeed;

    public DistribObject(Long timestamp, String clientSeed) {
        this.timestamp = timestamp;
        this.clientSeed = clientSeed;
    }

    @Override
    public int getFactoryId() {
        return DistribObjectDataSerializableFactory.ID;
    }

    @Override
    public int getId() {
        return DistribObjectDataSerializableFactory.DISTRIB_OBJ_ID;
    }

    @Override
    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
        objectDataOutput.writeLong(timestamp);
        objectDataOutput.writeUTF(clientSeed);
        objectDataOutput.writeUTF(mySeed);
    }

    @Override
    public void readData(ObjectDataInput objectDataInput) throws IOException {
        timestamp = objectDataInput.readLong();
        clientSeed = objectDataInput.readUTF();
        mySeed = objectDataInput.readUTF();
    }
}
