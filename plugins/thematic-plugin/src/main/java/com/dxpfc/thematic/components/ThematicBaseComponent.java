package com.dxpfc.thematic.components;

import org.hippoecm.hst.component.support.bean.BaseHstComponent;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.onehippo.cms7.crisp.api.broker.ResourceServiceBroker;
import org.onehippo.cms7.crisp.api.resource.Resource;
import org.onehippo.cms7.crisp.api.resource.ResourceException;
import org.onehippo.cms7.services.HippoServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;

import java.util.Map;
import java.util.UUID;

public class ThematicBaseComponent extends BaseHstComponent {

  private static Logger log = LoggerFactory.getLogger(ThematicBaseComponent.class);

  @Override
  public void doBeforeRender(HstRequest request, HstResponse response) throws HstComponentException {

    ResourceServiceBroker broker = HippoServiceRegistry.getService(ResourceServiceBroker.class);
    HstRequestContext requestContext = request.getRequestContext();
    Map<String, String[]> params = requestContext.getBaseURL().getParameterMap();
    String path = "?rows=%s&account_id=%s&domain_key=%s&request_id=%s&url=%s&fl=%s&sc2_mode=%s&request_type=%s&q=%s&sort=%s&start=%s&debug=%s&";
    String baseUrl = requestContext.getBaseURL().getRequestPath();
    String splitUrl[] = baseUrl.split("/");
    int urlLength = splitUrl.length;

    String theme = splitUrl[urlLength-1];

    Map<String,String> properties = getComponentParameters();

    String sort = params.getOrDefault(ThematicConstants.SORT_ORDER, new String[]{""})[0];
    int page = new Integer(params.getOrDefault(ThematicConstants.PAGINATION, new String[]{"0"})[0]);

    String fl = properties.get("fields");
    String row = properties.get("max_results_per_page");
    String start = Integer.toString(page * new Integer(row));
    String accountId = properties.get("account_id");
    String domainKey = properties.get("domain_key");
    String sc2Mode = properties.get("sc2_mode");
    String feRequestType = properties.get("request_type");
    String debugMode = properties.get("debug_mode");
    String requestId = UUID.randomUUID().toString();
    String url = ThematicConstants.HIPPO_REF_URL + baseUrl;


    request.setAttribute("resp",new Boolean(false));
   try {
     Resource thematic = broker.resolve("thematicResource", String.format(path, row, accountId, domainKey, requestId, url, fl, sc2Mode, feRequestType, theme, sort, start, debugMode));

     int items = (int) thematic.getValue("response/numFound");
     if (thematic != null && items != 0) {
       request.setAttribute("thematic", thematic);
       request.setAttribute("sort",sort);
       request.setAttribute("currentPage",page);
       request.setAttribute("totalPages",items/Integer.parseInt(row));
       request.setAttribute("resp", new Boolean(true));
     }
   } catch (ResourceException | ResourceAccessException e) {
     log.error("Unable to resolve Thematic resource - ",e);
     request.setAttribute("resp",new Boolean(false));
   }

  }




}
