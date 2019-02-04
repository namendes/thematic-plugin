package com.dxpfc.thematic.jaxrs;

import com.dxpfc.thematic.model.ThematicConfigProperties;
import com.dxpfc.thematic.model.impl.ThematicConfigPropertiesImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class ThematicPagesUtils {
    private ThematicPagesUtils() {
    }

    // Adopting Initialization-on-demand holder idiom.
    // ref) https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom
    private static class LazyHolder {
        private static final ObjectMapper tempObjectMapper = new ObjectMapper();
        static {
            final SimpleModule module = new SimpleModule();
            final SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
            resolver.addMapping(ThematicConfigProperties.class, ThematicConfigPropertiesImpl.class);

            module.setAbstractTypes(resolver);
            tempObjectMapper.registerModule(module);
        }
        static final ObjectMapper INSTANCE = tempObjectMapper;
    }

    /**
     * Return singleton Jackson ObjectMapper for Universal Pixel related object deserialization.
     * @return
     */
    public static ObjectMapper getThematicObjectMapper() {
        return LazyHolder.INSTANCE;
    }
}