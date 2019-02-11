package com.dxpfc.thematic.jaxrs;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.onehippo.repository.jaxrs.CXFRepositoryJaxrsEndpoint;
import org.onehippo.repository.jaxrs.RepositoryJaxrsService;
import org.onehippo.repository.jaxrs.api.ManagedUserSessionInvoker;
import org.onehippo.repository.modules.AbstractReconfigurableDaemonModule;

public class ThematicDaemonModule extends AbstractReconfigurableDaemonModule {

    private static final String END_POINT = "/thematic";

    private static ThematicDaemonModule defaultModuleInstance;
    private String modulePath;

    @Override
    protected void doConfigure(final Node moduleConfig) throws RepositoryException {
        moduleConfigPath = moduleConfig.getPath();
        modulePath = moduleConfig.getParent().getPath();
    }

    @Override
    public void doInitialize(final Session session) throws RepositoryException {

        ThematicPagesService thematicPagesService = new ThematicPagesService();

        ManagedUserSessionInvoker managedUserSessionInvoker = new ManagedUserSessionInvoker(session);

        RepositoryJaxrsService.addEndpoint(new CXFRepositoryJaxrsEndpoint(END_POINT)
                .invoker(managedUserSessionInvoker)
                .singleton(thematicPagesService)
                .singleton(new JacksonJsonProvider(ThematicPagesUtils.getThematicObjectMapper())));

        defaultModuleInstance = this;
    }
    @Override
    protected void doShutdown() {
        defaultModuleInstance = null;
        RepositoryJaxrsService.removeEndpoint(END_POINT);
    }

    @Override
    protected void onConfigurationChange(final Node moduleConfig) throws RepositoryException {
        doConfigure(moduleConfig);
    }

}