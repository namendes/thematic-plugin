package com.dxpfc.thematic.components;

import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.hippoecm.hst.component.support.bean.BaseHstComponent;
import org.hippoecm.hst.component.support.forms.FormMap;
import org.hippoecm.hst.configuration.hosting.Mount;
import org.hippoecm.hst.configuration.internal.CanonicalInfo;
import org.hippoecm.hst.configuration.sitemap.HstSiteMap;
import org.hippoecm.hst.configuration.sitemap.HstSiteMapItem;
import org.hippoecm.hst.container.RequestContextProvider;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.hst.pagecomposer.jaxrs.model.DocumentRepresentation;
import org.hippoecm.hst.pagecomposer.jaxrs.model.SiteMapItemRepresentation;
import org.hippoecm.hst.pagecomposer.jaxrs.services.PageComposerContextService;
import org.hippoecm.hst.pagecomposer.jaxrs.services.exceptions.ClientError;
import org.hippoecm.hst.pagecomposer.jaxrs.services.exceptions.ClientException;
import org.hippoecm.hst.pagecomposer.jaxrs.services.helpers.LockHelper;
import org.hippoecm.hst.pagecomposer.jaxrs.services.helpers.PagesHelper;
import org.hippoecm.hst.util.HstRequestUtils;
import org.hippoecm.repository.api.NodeNameCodec;
import org.hippoecm.repository.util.JcrUtils;
import org.onehippo.cms7.crisp.api.broker.ResourceServiceBroker;
import org.onehippo.cms7.crisp.api.resource.Resource;
import org.onehippo.cms7.crisp.api.resource.ResourceException;
import org.onehippo.cms7.services.HippoServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Set;

public class ThematicControlPanelComponent extends BaseHstComponent {

  private static final Logger log = LoggerFactory.getLogger(ThematicControlPanelComponent.class);

  private static final String HST_WORKSPACE = "hst:workspace";
  private static final String HST_SITEMAP = "hst:sitemap";
  private static final String HST_SITEMAP_ITEM = "hst:sitemapitem";
  private static final String HST_APPLICATION_ID = "hst:applicationId";
  private static final String HST_PAGES = "hst:pages";
  private static final String HST_WORKSPACE_SITEMAP_CONFIGURATION_PATH = "/" + HST_WORKSPACE + "/" + HST_SITEMAP;
  private static final String HST_COMPONENT_CONFIGURATION_ID = "hst:componentconfigurationid";
  private static final String HST_DEFAULT_PROTOTYPE_PAGE = "/hst:hst/hst:configurations/hst:default/hst:prototypepages";
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


  private static void unlockAndReLockLandingPages(Node sitemapNode, HstRequestContext requestContext) {
    try {
      LockHelper lockHelper = new LockHelper();
      Session session       = requestContext.getSession();
      String hstConfigPath  = requestContext.getResolvedMount().getMount().getChannel().getHstConfigPath();
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
    GET request with query params as search string
    if empty return nothing along with placeholder preview
   */
  @Override
  public void doBeforeRender(HstRequest request, HstResponse response) throws HstComponentException {

    ResourceServiceBroker broker    = HippoServiceRegistry.getService(ResourceServiceBroker.class);
    Map<String, String[]> params    = request.getRequestContext().getBaseURL().getParameterMap();
    Map<String, String> properties  = getComponentParameters();

    String[] themes                 = params.getOrDefault("theme", new String[]{"*"});
    String theme                    = (themes[0].equalsIgnoreCase("*")) ? "*" : "\"" + themes[0] + "\"";
    StringBuffer currentURL         = request.getRequestContext().getServletRequest().getRequestURL();


    String searchEndpoint = buildSearchEndpoint(properties, theme);
    try {
      Resource thematicSearch = broker.resolve(ThematicConstants.CRISP_RESOURCE_THEMATIC_SEARCH, searchEndpoint);
      Object searchResults = thematicSearch.getValue("docs");
      request.setAttribute("numResults", thematicSearch.getValue("numFound"));
      request.setAttribute("searchResults", searchResults != null ? searchResults : new Array[]{});
      request.setAttribute("requestURL", currentURL);
      request.setAttribute("error", new Boolean(false));
    } catch (ResourceException | ResourceAccessException e) {
      request.setAttribute("error", new Boolean(true));
      log.error("Not able to resolve Thematic Search resource :", e);
    }
  }

  @Override
  public void doAction(HstRequest request, HstResponse response) throws HstComponentException {
    String theme;
    Session session;
    try {
      FormMap map = new FormMap(request, new String[]{"theme_name"});
      //TODO::theme = map.getField("theme").getValue().replaceAll(" ", "-");
      theme = map.getField("theme_name").getValue();

      HstRequestContext requestContext = RequestContextProvider.get();
      session = requestContext.getSession();
      String baseSiteMapUuid = requestContext.getResolvedMount().getMount().getChannel().getSiteMapId();
      Node sitemapNode = session.getNodeByIdentifier(baseSiteMapUuid);
      Mount editingMount = getEditingMount(requestContext);
      Node prototypeNode = session.getNode(HST_DEFAULT_PROTOTYPE_PAGE + "/" + THEMATIC_BASE_PAGE);


      Map<String, String> properties = getComponentParameters();
      String thematicPageSitemapPath = properties.get(ThematicConstants.PROPERTIES_THEMATIC_SITEMAP_PATH);
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
        String redirect = request.getRequestContext().getHstLinkCreator().create(cleanPath + theme, requestContext.getResolvedMount().getMount()).toUrlForm(requestContext, true);
        response.sendRedirect(redirect);
      } else {
        SiteMapItemRepresentation siteMapItem = new SiteMapItemRepresentation();
        siteMapItem.setName(theme);
        siteMapItem.setComponentConfigurationId(prototypeNode.getIdentifier());
        String previewThemeSitemapPath = editingMount.getHstSite().getConfigurationPath() + HST_WORKSPACE_SITEMAP_CONFIGURATION_PATH + cleanPath;
        DocumentRepresentation document = new DocumentRepresentation(previewThemeSitemapPath + theme, theme, true, true);
        siteMapItem.setPrimaryDocumentRepresentation(document);

        final Node createdSitemapNode = createSitemapNode(siteMapItem, sitemapNode.getIdentifier(), requestContext, editingMount);
        unlockAndReLockLandingPages(createdSitemapNode, requestContext);

        String redirect = request.getRequestContext().getHstLinkCreator().create(cleanPath + theme, requestContext.getResolvedMount().getMount()).toUrlForm(requestContext, true);
        response.sendRedirect(redirect);
      }


    } catch (RepositoryException | IOException error) {
      log.error("Failed to create new sitemap item for customization", error);
    }
  }

  private Node createSitemapNode(SiteMapItemRepresentation siteMapItem, String finalParentId, HstRequestContext requestContext, Mount editingMount) throws RepositoryException {
    PagesHelper pagesHelper = new PagesHelper();
    pagesHelper.setPageComposerContextService(new PageComposerContextService());
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

  private Mount getEditingMount(HstRequestContext requestContext) throws RepositoryException {
    String renderingMountId = (String) requestContext.getServletRequest().getSession(true).getAttribute(HST_RENDER_MOUNT_CLASSPATH);
    if (renderingMountId == null) {
      throw new IllegalStateException("Could not find rendering mount id on request session.");
    }
    Mount editingMount = requestContext.getVirtualHost().getVirtualHosts().getMountByIdentifier(renderingMountId);
    String msg;
    if (editingMount == null) {
      msg = String.format("Could not find a Mount for identifier + '%s'", renderingMountId);
      throw new IllegalStateException(msg);
    } else if (!PREVIEW_MOUNT.equals(editingMount.getType())) {
      msg = String.format("Expected a preview (decorated) mount but '%s' is not of type preview.", editingMount.toString());
      throw new IllegalStateException(msg);
    }
    String previewWorkspaceSiteMapPath = editingMount.getHstSite().getConfigurationPath() + HST_WORKSPACE_SITEMAP_CONFIGURATION_PATH;
    if (!requestContext.getSession().nodeExists(previewWorkspaceSiteMapPath)) {
      createWorkspaceSiteMapInPreviewAndLive(previewWorkspaceSiteMapPath, requestContext.getSession());
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

  private String buildSearchEndpoint(Map<String, String> properties, String theme) {
    return properties.get("account_id") +
            "/pages?_br_in_auth_key=" + properties.get("auth_key") +
            "&type=" + "all" +
            "&rows=" + properties.get("rows") +
            "&sort_by=" + properties.get("sort_by") +
            "&query=" + theme;
  }

}
