package com.dxpfc.thematic.components;

import org.hippoecm.hst.component.support.bean.BaseHstComponent;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.onehippo.cms7.crisp.api.broker.ResourceServiceBroker;
import org.onehippo.cms7.crisp.api.resource.Resource;
import org.onehippo.cms7.services.HippoServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

public class ThematicBaseComponent extends BaseHstComponent {

  private static Logger log = LoggerFactory.getLogger(ThematicBaseComponent.class);
  @Override
  public void doBeforeRender(HstRequest request, HstResponse response) throws HstComponentException {

    // log.info("Thematic Request - "+ "http://"+request.getRequestContext().getBaseURL().getHostName()+request.getRequestContext().getBaseURL().getContextPath()+request.getRequestContext().getBaseURL().getRequestPath());

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
    String feRequestType = properties.get("request_type");
    String debugMode = properties.get("debug_mode");
    String requestId = UUID.randomUUID().toString();
    String url = "hippo:" + baseUrl;

    if (feRequestType.equals("search")) {
      feRequestType = "search&search_type=keyword";
    }

    request.setAttribute("rsp",new Boolean(false));
   try {
     Resource thematic = broker.resolve("thematicResource", String.format(path, row, accountId, domainKey, requestId, url, fl, sc2Mode, feRequestType, theme, debugMode));
     int items = (int) thematic.getValue("response/numFound");
     if (thematic != null && items != 0) {
       request.setAttribute("thematic", thematic);
       request.setAttribute("rsp", new Boolean(true));
     }
   }catch(Throwable e){
     log.error("Thematic Error - ",e);
     request.setAttribute("rsp",new Boolean(false));
   }

  }




}
