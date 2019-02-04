package com.dxpfc.thematic.jaxrs;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.onehippo.repository.jaxrs.CXFRepositoryJaxrsEndpoint;
import org.onehippo.repository.jaxrs.RepositoryJaxrsService;
import org.onehippo.repository.modules.DaemonModule;

public class ThematicDaemonModule implements DaemonModule {

    private static final String END_POINT = "/thematic";

    @Override
    public void initialize(final Session session) throws RepositoryException {

        ThematicPagesService thematicPagesService = new ThematicPagesService();

        RepositoryJaxrsService.addEndpoint(new CXFRepositoryJaxrsEndpoint(END_POINT)
                .singleton(thematicPagesService)
                .singleton(new JacksonJsonProvider(ThematicPagesUtils.getThematicObjectMapper())));
    }

    @Override
    public void shutdown() {
        RepositoryJaxrsService.removeEndpoint(END_POINT);
    }

}