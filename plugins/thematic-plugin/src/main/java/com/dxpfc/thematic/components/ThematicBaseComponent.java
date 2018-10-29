package com.dxpfc.thematic.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.onehippo.cms7.crisp.core.resource.jackson.JacksonResource;
import org.onehippo.cms7.essentials.components.CommonComponent;
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

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class ThematicBaseComponent extends CommonComponent {

  private static Logger log = LoggerFactory.getLogger(ThematicBaseComponent.class);

  @Override
  public void doBeforeRender(HstRequest request, HstResponse response) throws HstComponentException {

    ResourceServiceBroker broker = HippoServiceRegistry.getService(ResourceServiceBroker.class);
    HstRequestContext requestContext = request.getRequestContext();
    Map<String, String[]> params = requestContext.getBaseURL().getParameterMap();
    String path = "?rows=%s&account_id=%s&domain_key=%s&request_id=%s&url=%s&fl=%s&sc2_mode=%s&request_type=%s&q=%s&sort=%s&start=%s&debug=%s&";
    String themeName = "";
    String baseUrl = requestContext.getBaseURL().getRequestPath();
    try {
      String splitUrl[] = baseUrl.split("/");
      int urlLength = splitUrl.length;
      themeName = splitUrl[urlLength - 1];
    } catch (ArrayIndexOutOfBoundsException e){
      throw new HstComponentException("Component not installed properly, please check sitemap entry", e);
    }
    Map<String,String> properties = getComponentParameters();

    String sort = params.getOrDefault(ThematicConstants.SORT_ORDER, new String[]{""})[0];
    int page = new Integer(params.getOrDefault(ThematicConstants.PAGINATION, new String[]{"1"})[0]);

    String fl = properties.get(ThematicConstants.PROPERTIES_FIELDS);
    String resultsPerPage = properties.get(ThematicConstants.PROPERTIES_MAX_RESULTS_PER_PAGE);
    String start = Integer.toString((page-1) * Integer.parseInt(resultsPerPage));
    String accountId = properties.get(ThematicConstants.PROPERTIES_ACCOUNT_ID);
    String domainKey = properties.get(ThematicConstants.PROPERTIES_DOMAIN_KEY);
    String sc2Mode = properties.get(ThematicConstants.PROPERTIES_SC2_MODE);
    String feRequestType = properties.get(ThematicConstants.PROPERTIES_REQUEST_TYPE);
    String debugMode = properties.get(ThematicConstants.PROPERTIES_DEBUG_MODE);
    String requestId = UUID.randomUUID().toString();
    String url = ThematicConstants.HIPPO_REF_URL + baseUrl;

    try {
      Resource thematic = broker.resolve("thematicResource", String.format(path, resultsPerPage, accountId, domainKey, requestId, url, fl, sc2Mode, feRequestType, themeName, sort, start, debugMode));

      int items = (int) thematic.getValue("response/numFound");
      String leftNav = (String)thematic.getValue("page_header/left_nav");
      if (thematic != null && items != 0) {
        request.setAttribute("thematic", thematic);
        request.setAttribute("sort",sort);
        request.setAttribute("currentPage",page);
        request.setAttribute("totalPages",1 + ((items-1)/Integer.parseInt(resultsPerPage)));
        request.setAttribute("leftNav",leftNav.isEmpty() ? "" : new JacksonResource(new ObjectMapper().readTree(leftNav)));
      }else
        super.pageNotFound(response);
    } catch (ResourceException | ResourceAccessException | IOException e) {
      log.error("Unable to resolve Thematic resource - ",e);
      super.pageNotFound(response);
    }

  }

}
