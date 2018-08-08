package org.example.components;

import org.hippoecm.hst.component.support.bean.BaseHstComponent;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.onehippo.cms7.crisp.api.broker.ResourceServiceBroker;
import org.onehippo.cms7.crisp.api.resource.Resource;
import org.onehippo.cms7.services.HippoServiceRegistry;

/**
 * Created by wborgbarthet on 31/07/18.
 */
public class TestComponent extends BaseHstComponent {

    @Override
    public void doBeforeRender(HstRequest request, HstResponse response) throws HstComponentException {

        ResourceServiceBroker broker = HippoServiceRegistry.getService(ResourceServiceBroker.class);
        Resource news = broker.resolve("newsresource", "");

        request.setAttribute("news", news);

    }
}
