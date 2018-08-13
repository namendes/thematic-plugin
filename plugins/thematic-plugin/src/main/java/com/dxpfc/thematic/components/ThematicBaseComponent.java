package com.dxpfc.thematic.components;

import org.hippoecm.hst.component.support.bean.BaseHstComponent;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.onehippo.cms7.crisp.api.broker.ResourceServiceBroker;
import org.onehippo.cms7.crisp.api.resource.Resource;
import org.onehippo.cms7.services.HippoServiceRegistry;

import java.util.Map;

public class ThematicBaseComponent extends BaseHstComponent {

  @Override
  public void doBeforeRender(HstRequest request, HstResponse response) throws HstComponentException {

    ResourceServiceBroker broker = HippoServiceRegistry.getService(ResourceServiceBroker.class);
    String path = "?rows=%s&account_id=%s&domain_key=%s&request_id=%s&url=%s&fl=%s&sc2_mode=%s&request_type=%s&q=%s&debug=%s&";
    String baseUrl = request.getRequestContext().getBaseURL().getRequestPath();
    String splitUrl[] = baseUrl.split("/");
    int urlLength = splitUrl.length;
    String theme = splitUrl[urlLength-1];
    Map<String,String> properties = getComponentParameters();
    String fl = properties.get("fields");
    String row = properties.get("max_results_per_page");
    String accountId = properties.get("account_id");
    String domainKey = properties.get("domain_key");
    String sc2Mode = properties.get("sc2_mode");
    String requestType = properties.get("request_type");
    String debugMode = properties.get("debug_mode");
    String requestId = "1";
    String url = "hippo:" + baseUrl;

    if (requestType.equals("search")) {
      requestType = "search&search_type=keyword";
    }

    Resource thematic = broker.resolve("thematicResource", String.format(path, row, accountId, domainKey, requestId, url, fl, sc2Mode, requestType, theme, debugMode));
    request.setAttribute("thematic", thematic);

  }
}
