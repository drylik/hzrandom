package ru.novikov.random.generator.models.factories;

import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import ru.novikov.random.generator.models.model.DistribObject;

import java.util.Map;

/**
 * @author a.novikov
 * @date 2018-09-29
 */
public class DistribObjectDataSerializableFactory implements DataSerializableFactory {

    public static final int ID = 1;
    public static final int DISTRIB_OBJ_ID = 1;

    private Map<Integer, IdentifiedDataSerializable> objects = Map.of(DISTRIB_OBJ_ID, new DistribObject());

    @Override
    public IdentifiedDataSerializable create(int i) {
        return objects.get(i);
    }
}
