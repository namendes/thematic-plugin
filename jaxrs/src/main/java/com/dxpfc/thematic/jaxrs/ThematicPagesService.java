package com.dxpfc.thematic.jaxrs;

import com.dxpfc.thematic.constants.ThematicConstants;
import com.dxpfc.thematic.model.ThematicConfigProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.hippoecm.hst.configuration.hosting.Mount;
import org.hippoecm.hst.configuration.internal.CanonicalInfo;
import org.hippoecm.hst.configuration.sitemap.HstSiteMap;
import org.hippoecm.hst.configuration.sitemap.HstSiteMapItem;
import org.hippoecm.hst.container.RequestContextProvider;
import org.hippoecm.hst.core.internal.HstMutableRequestContext;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.hst.pagecomposer.jaxrs.cxf.CXFJaxrsHstConfigService;
import org.hippoecm.hst.pagecomposer.jaxrs.model.DocumentRepresentation;
import org.hippoecm.hst.pagecomposer.jaxrs.model.SiteMapItemRepresentation;
import org.hippoecm.hst.pagecomposer.jaxrs.services.AbstractConfigResource;
import org.hippoecm.hst.pagecomposer.jaxrs.services.PageComposerContextService;
import org.hippoecm.hst.pagecomposer.jaxrs.services.exceptions.ClientError;
import org.hippoecm.hst.pagecomposer.jaxrs.services.exceptions.ClientException;
import org.hippoecm.hst.pagecomposer.jaxrs.services.helpers.LockHelper;
import org.hippoecm.hst.pagecomposer.jaxrs.services.helpers.PagesHelper;
import org.hippoecm.hst.platform.api.PlatformServices;
import org.hippoecm.hst.platform.api.model.InternalHstModel;
import org.hippoecm.hst.platform.model.HstModelRegistry;
import org.hippoecm.hst.site.HstServices;
import org.hippoecm.hst.site.request.PreviewDecoratorImpl;
import org.hippoecm.hst.util.HstRequestUtils;
import org.hippoecm.repository.api.NodeNameCodec;
import org.hippoecm.repository.util.JcrUtils;
import org.onehippo.cms7.crisp.api.broker.ResourceServiceBroker;
import org.onehippo.cms7.crisp.api.resource.Resource;
import org.onehippo.cms7.crisp.api.resource.ResourceException;
import org.onehippo.cms7.crisp.core.resource.jackson.JacksonResource;
import org.onehippo.cms7.crisp.hst.module.CrispHstServices;
import org.onehippo.cms7.services.HippoServiceRegistry;
import org.onehippo.cms7.services.cmscontext.CmsSessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Set;

import static org.hippoecm.hst.pagecomposer.jaxrs.services.PageComposerContextService.*;

@Path("/thematicpages/")
public class ThematicPagesService extends AbstractConfigResource {

    private static final Logger log = LoggerFactory.getLogger(ThematicPagesService.class);

    private static final String HST_WORKSPACE = "hst:workspace";
    private static final String HST_SITEMAP = "hst:sitemap";
    private static final String HST_SITEMAP_ITEM = "hst:sitemapitem";
    private static final String HST_APPLICATION_ID = "hst:applicationId";
    private static final String HST_PAGES = "hst:pages";
    private static final String HST_WORKSPACE_SITEMAP_CONFIGURATION_PATH = "/" + HST_WORKSPACE + "/" + HST_SITEMAP;
    private static final String HST_COMPONENT_CONFIGURATION_ID = "hst:componentconfigurationid";
    private static final String HST_DEFAULT_PROTOTYPE_PAGE = "/hst:configurations/hst:default/hst:prototypepages";
    private static final String HST_PARAMETER_NAMES = "hst:parameternames";
    private static final String HST_PARAMETER_VALUES = "hst:parametervalues";
    private static final String HST_ROLES = "hst:roles";
    private static final String HST_SCHEME = "hst:scheme";
    private static final String HST_RELATIVE_CONTENT_PATH = "hst:relativecontentpath";
    private static final String HST_RENDER_MOUNT_CLASSPATH = "org.hippoecm.hst.container.render_mount";
    private static final String HST_PAGE_TITLE = "hst:pagetitle";
    private static final String HST_STATE = "hst:state";

    private static final String PREVIEW_MOUNT = "preview";
    private static final String THEMATIC_BASE_PAGE = "thematicBasePage";


    private static void unlockAndReLockLandingPages(Node sitemapNode, HstRequestContext requestContext, String hstConfigPath) {
        try {
            LockHelper lockHelper = new LockHelper();
            Session session = requestContext.getSession();
            //unlock the sitemap node and its corresponding hst:component
            lockHelper.unlock(sitemapNode);
            Node createdHstPagesNode = session.getNode(hstConfigPath + "/" + HST_WORKSPACE + "/" + sitemapNode.getProperty(HST_COMPONENT_CONFIGURATION_ID).getString());
            lockHelper.unlock(createdHstPagesNode);
            lockHelper.acquireLock(sitemapNode, session.getUserID().split(",")[0], 0);
            lockHelper.acquireLock(createdHstPagesNode, session.getUserID().split(",")[0], 0);
            session.save();

        } catch (RepositoryException error) {
            log.error("Re-locking the new page node failed", error);
        }
    }

    /*
      POST request with query params as search string
      if empty return nothing along with placeholder preview
     */
    @POST
    @Path("/search/{theme}/page/{currentUrl}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchThematicPage(@Context SecurityContext securityContext,
                                          @Context HttpServletRequest request, @PathParam("theme") String themeParam,
                                          @PathParam("currentUrl") String currentURL, ThematicConfigProperties properties) {

        ResourceServiceBroker broker = CrispHstServices.getDefaultResourceServiceBroker();

        String theme = (themeParam.equalsIgnoreCase("*")) ? "*" : "\"" + themeParam + "\"";

        try {

            String thematicPageSitemapPath = properties.getThematicPageSitemapPath();
            String[] sitemapPathList = thematicPageSitemapPath.split("/");

            HstRequestContext requestContext = RequestContextProvider.get();
            //Augment session
            Session session = requestContext.getSession().impersonate(CmsSessionContext.getContext(request.getSession()).getRepositoryCredentials());
            ((HstMutableRequestContext) requestContext).setSession(session);
            //fetching the editing mount "mapped" to the resolved cms mount
            Mount editingMount = getEditingMount(requestContext);
            String thematicSitemapItemPath = editingMount.getChannel().getSiteMapId();
            Node baseSitemapNode = session.getNodeByIdentifier(thematicSitemapItemPath);

            StringBuilder cleanPathBuilder = new StringBuilder();
            for (String sitemapPathItem : sitemapPathList) {
                sitemapPathItem = StringUtils.strip(sitemapPathItem);
                if (StringUtils.isNotBlank(sitemapPathItem)) {
                    baseSitemapNode = baseSitemapNode.getNode(sitemapPathItem);
                    cleanPathBuilder.append(sitemapPathItem).append("/");
                }
            }
            String cleanPath = cleanPathBuilder.toString();

            String searchEndpoint = buildSearchEndpoint(properties, theme);
            Resource thematicSearch = broker.resolve(ThematicConstants.CRISP_RESOURCE_THEMATIC_SEARCH, searchEndpoint);
            Object searchResults = thematicSearch.getValue("docs");
            JacksonResource searchResultsJson = (JacksonResource) searchResults;
            ObjectMapper mapper = new ObjectMapper();
            ArrayNode customizationList = mapper.createArrayNode();
            for (Resource resultDocument : searchResultsJson.getChildren().getCollection()) {
                ObjectNode tempNode = mapper.createObjectNode();
                if (baseSitemapNode.hasNode(resultDocument.getValue("theme").toString())) {
                    tempNode.put("isCustomized", "Page Customized");
                } else {
                    tempNode.put("isCustomized", "");
                }
                customizationList.add(tempNode);
            }

            JacksonResource customizationResource = new JacksonResource(searchResultsJson, customizationList, "customizationList");
            request.setAttribute("customizationList", customizationResource);
            request.setAttribute("numResults", thematicSearch.getValue("numFound"));
            request.setAttribute("urlPath", cleanPath);
            request.setAttribute("searchResults", searchResults != null ? searchResults : new Array[]{});
            request.setAttribute("requestURL", currentURL);
            request.setAttribute("error", new Boolean(false));

            return Response.ok(searchResultsJson.toJsonString(mapper)).build();
        } catch (ResourceException | ResourceAccessException | RepositoryException error) {
            request.setAttribute("error", new Boolean(true));
            log.error("Not able to resolve Thematic Search resource :", error);
        }
        return error("Something went wrong while searching thematic pages");
    }


    @POST
    @Path("/customize/{theme}/page/{pageId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response customizePages(@Context SecurityContext securityContext,
                                   @Context HttpServletRequest request, @Context HttpServletResponse response,
                                   @PathParam("theme") String theme,
                                   @PathParam("pageId") String pageId, ThematicConfigProperties properties) {
        Session session;
        try {
            HstRequestContext requestContext = RequestContextProvider.get();
            //Augment session
            session = requestContext.getSession().impersonate(CmsSessionContext.getContext(request.getSession()).getRepositoryCredentials());
            ((HstMutableRequestContext) requestContext).setSession(session);
            //need go inject the PREVIEW_EDITING_HST_MODEL_ATTR in the requestContext
            simulateCXFJaxrsHstConfigService(requestContext);
            //using platform services to retrieve the mapped HST
            Mount editingMount = getEditingMount(requestContext);
            if(editingMount == null){
                return error("Couldn't find the editing mount");
            }
            Node sitemapNode = session.getNode(editingMount.getChannel().getHstConfigPath()+"/hst:workspace/hst:sitemap");
            String hstSiteName = "/hst:"+editingMount.getHstSite().getName();
            Node prototypeNode = session.getNode(hstSiteName + HST_DEFAULT_PROTOTYPE_PAGE + "/" + THEMATIC_BASE_PAGE);

            String thematicPageSitemapPath = properties.getThematicPageSitemapPath();
            String[] sitemapPathList = thematicPageSitemapPath.split("/");

            StringBuilder cleanPathBuilder = new StringBuilder();
            cleanPathBuilder.append("/");
            for (String sitemapItem : sitemapPathList) {
                sitemapItem = StringUtils.strip(sitemapItem);
                if (StringUtils.isNotBlank(sitemapItem)) {
                    sitemapNode = sitemapNode.getNode(sitemapItem);
                    cleanPathBuilder.append(sitemapItem).append("/");
                }
            }
            String cleanPath = cleanPathBuilder.toString();

            if (sitemapNode.hasNode(theme)) {
                sitemapNode.getNode(theme);
                String redirect = requestContext.getHstLinkCreator().create(cleanPath + theme, requestContext.getResolvedMount().getMount()).toUrlForm(requestContext, true);
                //response.sendRedirect(redirect);
            } else {
                SiteMapItemRepresentation siteMapItem = new SiteMapItemRepresentation();
                siteMapItem.setName(theme);
                siteMapItem.setComponentConfigurationId(prototypeNode.getIdentifier());
                String previewThemeSitemapPath = sitemapNode.getPath();
                DocumentRepresentation document = new DocumentRepresentation(previewThemeSitemapPath + theme, theme, true, true);
                siteMapItem.setPrimaryDocumentRepresentation(document);

                final Node createdSitemapNode = createSitemapNode(siteMapItem, sitemapNode.getIdentifier(), requestContext, editingMount);
                unlockAndReLockLandingPages(createdSitemapNode, requestContext, editingMount.getChannel().getHstConfigPath());

                String redirect = requestContext.getHstLinkCreator().create(cleanPath + theme, requestContext.getResolvedMount().getMount()).toUrlForm(requestContext, true);
                //response.sendRedirect(redirect);
            }
            return Response.ok().build();

        } catch (RepositoryException error) {
            log.error("Failed to create new sitemap item for customization", error);
        }

        return Response.notModified().build();
    }


    private Node createSitemapNode(SiteMapItemRepresentation siteMapItem, String finalParentId, HstRequestContext requestContext, Mount editingMount) throws RepositoryException {
        PagesHelper pagesHelper = new PagesHelper();
        PageComposerContextService pageComposerContextService = HstServices.getComponentManager().getComponent("pageComposerContextService", "org.hippoecm.hst.pagecomposer");
        pagesHelper.setPageComposerContextService(pageComposerContextService);
        LockHelper lockHelper = new LockHelper();
        Session session = requestContext.getSession();
        Node parent = session.getNodeByIdentifier(finalParentId);
        String encodedName = getURLDecodedJcrEncodedName(siteMapItem.getName(), requestContext);
        validateTarget(session, parent.getPath() + "/" + encodedName, editingMount.getHstSite().getSiteMap());
        Node newSitemapNode = parent.addNode(encodedName, HST_SITEMAP_ITEM);
        lockHelper.acquireLock(newSitemapNode, 0L);
        setSitemapItemProperties(siteMapItem, newSitemapNode, editingMount);
        Node prototypePage = session.getNodeByIdentifier(siteMapItem.getComponentConfigurationId());
        String prototypeApplicationId = JcrUtils.getStringProperty(prototypePage, HST_APPLICATION_ID, (String) null);
        String targetPageNodeName = getSiteMapPathPrefixPart(newSitemapNode) + "-" + prototypePage.getName();
        Node newPage = pagesHelper.create(prototypePage, targetPageNodeName);
        newSitemapNode.setProperty(HST_COMPONENT_CONFIGURATION_ID, HST_PAGES + "/" + newPage.getName());
        Map<String, String> modifiedLocalParameters = siteMapItem.getLocalParameters();
        setLocalParameters(newSitemapNode, modifiedLocalParameters);
        Set<String> modifiedRoles = siteMapItem.getRoles();
        setRoles(newSitemapNode, modifiedRoles);
        if (prototypeApplicationId != null) {
            newSitemapNode.setProperty(HST_APPLICATION_ID, prototypeApplicationId);
        }
        return newSitemapNode;

    }

    private String getSiteMapPathPrefixPart(Node siteMapNode) throws RepositoryException {

        StringBuilder siteMapPathPrefixBuilder;
        for (siteMapPathPrefixBuilder = new StringBuilder(); siteMapNode.isNodeType(HST_SITEMAP_ITEM); siteMapNode = siteMapNode.getParent()) {
            if (siteMapPathPrefixBuilder.length() > 0) {
                siteMapPathPrefixBuilder.insert(0, "-");
            }

            siteMapPathPrefixBuilder.insert(0, siteMapNode.getName());
        }

        return siteMapPathPrefixBuilder.toString();
    }

    private Mount getEditingMount(HstRequestContext requestContext) {
        Mount editingMount = null;
        PlatformServices platformService = HippoServiceRegistry.getService(PlatformServices.class);
        String hostGroupName = requestContext.getResolvedMount().getMount().getVirtualHost().getHostGroupName();
        String mountName = requestContext.getResolvedMount().getMount().getName();
        log.debug("Searching the editing mount under {} with name {} ", hostGroupName, mountName);
        for(Mount mount : platformService.getMountService().getPreviewMounts(hostGroupName).values()){
            if(mount.getName().equals(mountName) && mount.isMapped()){
                editingMount = mount;
            }
        }
        return editingMount;
    }

    private void setLocalParameters(Node node, Map<String, String> modifiedLocalParameters) throws RepositoryException {
        if (modifiedLocalParameters != null) {
            if (modifiedLocalParameters.isEmpty()) {
                removeProperty(node, HST_PARAMETER_NAMES);
                removeProperty(node, HST_PARAMETER_VALUES);
            } else {
                String[][] namesAndValues = mapToNameValueArrays(modifiedLocalParameters);
                node.setProperty(HST_PARAMETER_NAMES, namesAndValues[0], 1);
                node.setProperty(HST_PARAMETER_VALUES, namesAndValues[1], 1);
            }

        }
    }

    private void setRoles(Node node, Set<String> modifiedRoles) throws RepositoryException {
        if (modifiedRoles != null) {
            if (modifiedRoles.isEmpty()) {
                removeProperty(node, HST_ROLES);
            } else {
                String[] roles = (String[]) Iterables.toArray(modifiedRoles, String.class);
                node.setProperty(HST_ROLES, roles, 1);
            }

        }
    }

    private String[][] mapToNameValueArrays(Map<String, String> map) {
        int size = map.size();
        String[][] namesAndValues = new String[][]{(String[]) map.keySet().toArray(new String[size]), new String[size]};

        for (int i = 0; i < size; ++i) {
            namesAndValues[1][i] = (String) map.get(namesAndValues[0][i]);
        }

        return namesAndValues;
    }

    private void validateTarget(Session session, String target, HstSiteMap siteMap) throws RepositoryException {
        String message;
        LockHelper lockHelper = new LockHelper();
        if (!(siteMap instanceof CanonicalInfo)) {
            message = String.format("Unexpected sitemap for site '%s' because not an instanceof CanonicalInfo", siteMap.getSite().getName());
            throw new ClientException(message, ClientError.UNKNOWN);
        } else if (!target.contains("/" + HST_WORKSPACE + "/")) {
            message = String.format("Target '%s' does not contain '%s'.", target, "/" + HST_WORKSPACE + "/");
            throw new ClientException(message, ClientError.ITEM_NOT_CORRECT_LOCATION);
        } else if (!session.nodeExists(StringUtils.substringBeforeLast(target, "/"))) {
            message = String.format("Parent of target node '%s' does not exist", target);
            throw new ClientException(message, ClientError.INVALID_URL);
        } else if (session.nodeExists(target)) {
            Node targetNode = session.getNode(target);
            if (isMarkedDeleted(targetNode)) {
                lockHelper.acquireLock(targetNode, 0L);
                targetNode.remove();
            } else {
                message = String.format("Target node '%s' already exists", target);
                throw new ClientException(message, ClientError.ITEM_NAME_NOT_UNIQUE);
            }
        } else {
            CanonicalInfo canonical = (CanonicalInfo) siteMap;
            Node siteMapNode = session.getNodeByIdentifier(canonical.getCanonicalIdentifier());
            String siteMapPath = siteMapNode.getPath();
            String targetConfig = StringUtils.substringBefore(target, "/" + HST_WORKSPACE);
            String siteMapRelPath;
            if (!siteMapPath.startsWith(targetConfig) && !target.startsWith(StringUtils.substringBefore(siteMapPath, "/" + HST_SITEMAP) + "-" + PREVIEW_MOUNT + "/" + HST_WORKSPACE_SITEMAP_CONFIGURATION_PATH)) {
                siteMapRelPath = String.format("Target '%s' is not valid for sitemap '%s'.", target, siteMapPath);
                throw new ClientException(siteMapRelPath, ClientError.ITEM_EXISTS_OUTSIDE_WORKSPACE);
            } else {
                siteMapRelPath = target.substring(siteMapNode.getPath().length() + 1);
                String[] elements = siteMapRelPath.split("/");
                HstSiteMapItem siteMapItem = siteMap.getSiteMapItem(elements[0]);

                for (int i = 1; i < elements.length && siteMapItem != null; ++i) {
                    siteMapItem = siteMapItem.getChild(elements[i]);
                }

                if (siteMapItem == null) {
                    log.debug("Target path '{}' can be created because it does not yet exist.", target);
                } else {
                    log.debug("Target '{}' can be matched in current sitemap. Now check whether the sitemap item that is matched belongs to the current hst:workspace/hst:sitemap, otherwise, it still can't be used");
                    CanonicalInfo item = (CanonicalInfo) siteMapItem;
                    Node siteMapItemNode = session.getNodeByIdentifier(item.getCanonicalIdentifier());
                    String existingItem = siteMapItemNode.getPath();
                    String msg;
                    if (existingItem.startsWith(siteMapPath)) {
                        msg = String.format("Target path '%s' already exists in current sitemap.", target, siteMapPath);
                        throw new ClientException(msg, ClientError.ITEM_EXISTS);
                    } else if (!existingItem.contains("/hst:workspace/hst:sitemap")) {
                        msg = String.format("Target path '%s' already exists in inherited configuration but is there not below hst:workspace/hst:sitemap and thus cannot be added in subproject.", target);
                        throw new ClientException(msg, ClientError.ITEM_EXISTS_OUTSIDE_WORKSPACE);
                    }
                }
            }
        }
    }

    private void createWorkspaceSiteMapInPreviewAndLive(String previewWorkspaceSiteMapPath, Session session) throws RepositoryException {
        String previewWorkspacePath = StringUtils.substringBeforeLast(previewWorkspaceSiteMapPath, "/");
        String liveWorkspacePath = previewWorkspacePath.replace("-" + PREVIEW_MOUNT + "/", "/");
        session.getNode(previewWorkspacePath).addNode(HST_SITEMAP, HST_SITEMAP);
        if (!session.nodeExists(liveWorkspacePath + "/" + HST_SITEMAP)) {
            session.getNode(liveWorkspacePath).addNode(HST_SITEMAP, HST_SITEMAP);
        }

    }

    private void setSitemapItemProperties(SiteMapItemRepresentation siteMapItem, Node jcrNode, Mount editingMount) throws RepositoryException {
        if (siteMapItem.getScheme() != null) {
            setProperty(jcrNode, HST_SCHEME, siteMapItem.getScheme());
        }

        if (siteMapItem.getPrimaryDocumentRepresentation() != null && siteMapItem.getPrimaryDocumentRepresentation().getPath() != null) {
            String absPath = siteMapItem.getPrimaryDocumentRepresentation().getPath();
            String rootContentPath = editingMount.getContentPath();
            if (absPath.startsWith(rootContentPath + "/")) {
                setProperty(jcrNode, HST_RELATIVE_CONTENT_PATH, absPath.substring(rootContentPath.length() + 1));
            } else if (absPath.equals("")) {
                removeProperty(jcrNode, HST_RELATIVE_CONTENT_PATH);
            } else {
                log.info("Cannot set '{}' for relative content path because does not start with root channel content path '{}'", absPath, rootContentPath + "/");
            }
        } else if (siteMapItem.getRelativeContentPath() != null) {
            setProperty(jcrNode, HST_RELATIVE_CONTENT_PATH, siteMapItem.getRelativeContentPath());
        }

        if (siteMapItem.getPageTitle() != null) {
            setProperty(jcrNode, HST_PAGE_TITLE, siteMapItem.getPageTitle());
        }

    }

    private void removeProperty(Node node, String name) throws RepositoryException {
        if (node.hasProperty(name)) {
            node.getProperty(name).remove();
        }

    }

    private void setProperty(Node jcrNode, String propName, String propValue) throws RepositoryException {
        if (StringUtils.isEmpty(propValue)) {
            removeProperty(jcrNode, propName);
        } else {
            jcrNode.setProperty(propName, propValue);
        }
    }

    private String getURLDecodedJcrEncodedName(String name, HstRequestContext requestContext) {
        String encoding = getEncoding(requestContext);
        try {
            String urlDecodedName = URLDecoder.decode(name, encoding);
            return NodeNameCodec.encode(urlDecodedName);
        } catch (UnsupportedEncodingException var4) {
            throw new IllegalArgumentException(String.format("Could not ULR  decode '%s'", name), var4);
        }
    }

    private String getEncoding(HstRequestContext requestContext) {
        return HstRequestUtils.getURIEncoding(requestContext.getServletRequest());
    }

    private boolean isMarkedDeleted(Node node) throws RepositoryException {
        return "deleted".equals(JcrUtils.getStringProperty(node, HST_STATE, (String) null));
    }

    private String buildSearchEndpoint(ThematicConfigProperties properties, String theme) {
        return properties.getAccountId() +
                "/pages?_br_in_auth_key=" + properties.getAuthKey() +
                "&type=" + "all" +
                "&rows=" + properties.getRows() +
                "&sort_by=" + properties.getSortBy() +
                "&query=" + theme;
    }

    private InternalHstModel simulateCXFJaxrsHstConfigService(HstRequestContext requestContext){

        final String contextPath = requestContext.getServletRequest().getHeader("contextPath");
        if (contextPath == null) {
            throw new IllegalArgumentException("'contextPath' header is missing");
        }

        final HstModelRegistry hstModelRegistry = HippoServiceRegistry.getService(HstModelRegistry.class);
        final InternalHstModel liveHstModel = (InternalHstModel)hstModelRegistry.getHstModel(contextPath);
        if (liveHstModel == null) {
            throw new IllegalArgumentException(String.format("Cannot find an hst model for context path '%s'", contextPath));
        }
        requestContext.setAttribute(EDITING_HST_MODEL_LINK_CREATOR_ATTR, liveHstModel.getHstLinkCreator());

        final InternalHstModel liveHstModelSnapshot = new CXFJaxrsHstConfigService.HstModelSnapshot(liveHstModel);
        final InternalHstModel previewHstModelSnapshot = new CXFJaxrsHstConfigService.HstModelSnapshot(liveHstModelSnapshot, new PreviewDecoratorImpl());

        requestContext.setAttribute(LIVE_EDITING_HST_MODEL_ATTR, liveHstModelSnapshot);
        requestContext.setAttribute(PREVIEW_EDITING_HST_MODEL_ATTR, previewHstModelSnapshot);

        return previewHstModelSnapshot;
    }

}
