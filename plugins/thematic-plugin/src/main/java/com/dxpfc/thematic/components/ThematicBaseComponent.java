package com.dxpfc.thematic.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.onehippo.cms7.crisp.api.broker.ResourceServiceBroker;
import org.onehippo.cms7.crisp.api.resource.Resource;
import org.onehippo.cms7.crisp.api.resource.ResourceException;
import org.onehippo.cms7.crisp.core.resource.jackson.JacksonResource;
import org.onehippo.cms7.crisp.hst.module.CrispHstServices;
import org.onehippo.cms7.essentials.components.CommonComponent;
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

        ResourceServiceBroker broker = CrispHstServices.getDefaultResourceServiceBroker();
        HstRequestContext requestContext = request.getRequestContext();
        Map<String, String[]> params = requestContext.getBaseURL().getParameterMap();
        String path = "?rows=%s&account_id=%s&domain_key=%s&request_id=%s&url=%s&fl=%s&sc2_mode=%s&request_type=%s&q=%s&sort=%s&start=%s&";
        String themeName = "";
        String baseUrl = requestContext.getBaseURL().getRequestPath();
        try {
            String splitUrl[] = baseUrl.split("/");
            int urlLength = splitUrl.length;
            themeName = splitUrl[urlLength - 1];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new HstComponentException("Component not installed properly, please check sitemap entry", e);
        }
        Map<String, String> properties = getComponentParameters();

        String sort = params.getOrDefault(ThematicConstants.SORT_ORDER, new String[]{""})[0];
        int page = new Integer(params.getOrDefault(ThematicConstants.PAGINATION, new String[]{ThematicConstants.DEFAULT_PAGE})[0]);

        String fl = properties.get(ThematicConstants.PROPERTIES_FIELDS);
        String resultsPerPage = properties.get(ThematicConstants.PROPERTIES_MAX_RESULTS_PER_PAGE);
        String start = Integer.toString((page - 1) * Integer.parseInt(resultsPerPage));
        String accountId = properties.get(ThematicConstants.PROPERTIES_ACCOUNT_ID);
        String domainKey = properties.get(ThematicConstants.PROPERTIES_DOMAIN_KEY);
        String sc2Mode = properties.get(ThematicConstants.PROPERTIES_SC2_MODE);
        String feRequestType = properties.get(ThematicConstants.PROPERTIES_REQUEST_TYPE);
        String defaultSeparator = properties.get(ThematicConstants.PROPERTIES_DEFAULT_SEPARATOR);
        String requestId = UUID.randomUUID().toString();
        String url = ThematicConstants.HIPPO_REF_URL + baseUrl;

        try {
            ThematicExchangeHint hint = new ThematicExchangeHint();
            hint.setMethodName(request.getRequestContext().getServletRequest().getMethod());
            hint.setTheme(themeName);
            Resource thematic = broker.resolve(ThematicConstants.CRISP_RESOURCE_THEMATIC_PAGE, String.format(path, resultsPerPage, accountId, domainKey, requestId, url, fl, sc2Mode, feRequestType, themeName, sort, start));

            int items = (int) thematic.getValue(ThematicConstants.PATH_PRODUCTS_FOUND);
            String leftNav = (String) thematic.getValue(ThematicConstants.PATH_THEME_LEFTNAV);
            if (thematic != null && items != 0) {
                request.setAttribute(ThematicConstants.ATTRIBUTE_THEMATIC_RESPONSE, thematic);
                request.setAttribute(ThematicConstants.ATTRIBUTE_THEMATIC_PRODUCT_SORT, sort);
                request.setAttribute(ThematicConstants.ATTRIBUTE_THEMATIC_CURRENT_PAGE, page);
                request.setAttribute(ThematicConstants.ATTRIBUTE_THEMATIC_TOTAL_PAGES, 1 + ((items - 1) / Integer.parseInt(resultsPerPage)));
                request.setAttribute(ThematicConstants.ATTRIBUTE_THEMATIC_PAGE_LEFTNAV, leftNav.isEmpty() ? "" : new JacksonResource(new ObjectMapper().readTree(leftNav)));
            } else
                super.pageNotFound(response);
        } catch (ResourceException | ResourceAccessException | IOException e) {
            log.error("Unable to resolve Thematic resource - ", e);
            super.pageNotFound(response);
        }
    }

}
