package com.dxpfc.thematic.components;

import org.hippoecm.hst.component.support.bean.BaseHstComponent;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.onehippo.cms7.crisp.api.broker.ResourceServiceBroker;
import org.onehippo.cms7.crisp.api.resource.Resource;
import org.onehippo.cms7.crisp.api.resource.ResourceException;
import org.onehippo.cms7.services.HippoServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;

import java.lang.reflect.Array;
import java.util.Map;

public class ThematicControlPanelComponent extends BaseHstComponent {

  private static Logger log = LoggerFactory.getLogger(ThematicControlPanelComponent.class);
  /*
    GET request with query params as search string
    if empty return nothing along with placeholder preview
   */
  @Override
  public void doBeforeRender(HstRequest request, HstResponse response) throws HstComponentException {

    ResourceServiceBroker broker = HippoServiceRegistry.getService(ResourceServiceBroker.class);
    Map<String,String[]> params = request.getRequestContext().getBaseURL().getParameterMap();
    Map<String,String> properties = getComponentParameters();


    String[] themes = params.getOrDefault("theme",new String[]{"*"});
    String theme = (themes[0].equalsIgnoreCase("*")) ? "*" : "\"" + themes[0] + "\"";
    StringBuffer currentURL = request.getRequestContext().getServletRequest().getRequestURL();
    String searchEndpoint = properties.get("account_id") +
            "_lpm_pagedata_v2/select?q=theme:" + theme +
            "&rows=" + properties.get("rows") +
            "&fl=" + properties.get("fl") + "&wt=" + properties.get("wt");
    try{
      Resource thematicSearch = broker.resolve("thematicSearch", searchEndpoint);
      Object searchResults = thematicSearch.getValue("response/docs");
      request.setAttribute("searchResults",searchResults!=null?searchResults:new Array[]{});
      request.setAttribute("requestURL",currentURL);
      request.setAttribute("error", new Boolean(false));
    }catch(ResourceException | ResourceAccessException e){
      request.setAttribute("error", new Boolean(true));
      log.error("Not able to resolve the resource :",e);
    }
  }

  /*
    POST request will make an entry in JCR repository
   */
  @Override
  public void doAction(HstRequest request, HstResponse response) throws HstComponentException {

  }
}
