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
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Set;

public class ThematicControlPanelComponent extends BaseHstComponent {

  private static final Logger log = LoggerFactory.getLogger(ThematicControlPanelComponent.class);
  private static final String WORKSPACE_SITEMAP_CONFIGURATION_PATH = "/hst:workspace/hst:sitemap";

  private static void unlockAndRelockLandingPages(Node sitemapNode) {
    try {
      LockHelper lockHelper = new LockHelper();
      HstRequestContext requestContext = RequestContextProvider.get();
      Session session = requestContext.getSession();
      String hstConfigPath = requestContext.getResolvedMount().getMount().getChannel().getHstConfigPath();
      //unlock the sitemap node and its corresponding hst:component
      lockHelper.unlock(sitemapNode);
      Node createdHstPagesNode = session.getNode(hstConfigPath + "/hst:workspace/" + sitemapNode.getProperty("hst:componentconfigurationid").getString());
      lockHelper.unlock(createdHstPagesNode);
      lockHelper.acquireLock(sitemapNode, session.getUserID().split(",")[0], 0);
      lockHelper.acquireLock(createdHstPagesNode, session.getUserID().split(",")[0], 0);
      session.save();

    } catch (RepositoryException ex) {
      ex.printStackTrace();
    }
  }

  /*
    GET request with query params as search string
    if empty return nothing along with placeholder preview
   */
  @Override
  public void doBeforeRender(HstRequest request, HstResponse response) throws HstComponentException {

    ResourceServiceBroker broker = HippoServiceRegistry.getService(ResourceServiceBroker.class);
    Map<String, String[]> params = request.getRequestContext().getBaseURL().getParameterMap();
    Map<String, String> properties = getComponentParameters();


    String[] themes = params.getOrDefault("theme", new String[]{"*"});
    String theme = (themes[0].equalsIgnoreCase("*")) ? "*" : "\"" + themes[0] + "\"";
    StringBuffer currentURL = request.getRequestContext().getServletRequest().getRequestURL();
    String searchEndpoint = buildSearchEndpoint(properties, theme);
    try {
      Resource thematicSearch = broker.resolve("thematicSearch", searchEndpoint);
      Object searchResults = thematicSearch.getValue("response/docs");
      request.setAttribute("searchResults", searchResults != null ? searchResults : new Array[]{});
      request.setAttribute("requestURL", currentURL);
      request.setAttribute("error", new Boolean(false));
    } catch (ResourceException | ResourceAccessException e) {
      request.setAttribute("error", new Boolean(true));
      log.error("Not able to resolve the resource :", e);
    }
  }

  @Override
  public void doAction(HstRequest request, HstResponse response) throws HstComponentException {
    String theme;
    Session session;
    try {
      FormMap map = new FormMap(request, new String[]{"theme"});
      theme = map.getField("theme").getValue();
      theme = theme.replaceAll(" ", "-");


      HstRequestContext requestContext = RequestContextProvider.get();
      String baseSiteMapUuid = requestContext.getResolvedMount().getMount().getChannel().getSiteMapId();
      Node baseSitemapNode = requestContext.getSession().getNodeByIdentifier(baseSiteMapUuid);
      Mount editingMount = getEditingMount(requestContext);
      Node prototypeNODE = requestContext.getSession().getNode("/hst:hst/hst:configurations/hst:default/hst:prototypepages/thematicBasePage");


      Map<String, String> properties = getComponentParameters();
      String thematicPageSitemapPath = properties.get("thematicPageSitemapPath");
      String[] sitemapPathList = thematicPageSitemapPath.split("/");

      Node sitemapNODE = baseSitemapNode;
      String cleanPath = "/";
      for (String sitemapItem : sitemapPathList) {
        sitemapItem = StringUtils.strip(sitemapItem);
        if (StringUtils.isNotBlank(sitemapItem)) {
          sitemapNODE = sitemapNODE.getNode(sitemapItem);
          cleanPath = cleanPath + sitemapItem + "/";
        }
      }

      try {
        sitemapNODE.getNode(theme);
        String redirect = request.getRequestContext().getHstLinkCreator().create(cleanPath + theme, requestContext.getResolvedMount().getMount()).toUrlForm(requestContext, true);
        response.sendRedirect(redirect);
      } catch (PathNotFoundException e) {
        SiteMapItemRepresentation siteMapItem = new SiteMapItemRepresentation();
        siteMapItem.setName(theme);
        siteMapItem.setComponentConfigurationId(prototypeNODE.getIdentifier());
        String previewThemeSitemapPath = editingMount.getHstSite().getConfigurationPath() + WORKSPACE_SITEMAP_CONFIGURATION_PATH + cleanPath;
        DocumentRepresentation document = new DocumentRepresentation(previewThemeSitemapPath + theme, theme, true, true);
        siteMapItem.setPrimaryDocumentRepresentation(document);

        final Node createdSitemapNode = createSitemapNode(siteMapItem, sitemapNODE.getIdentifier(), requestContext, editingMount);
        session = requestContext.getSession();
        session.save();
        unlockAndRelockLandingPages(createdSitemapNode);

        String redirect = request.getRequestContext().getHstLinkCreator().create(cleanPath + theme, requestContext.getResolvedMount().getMount()).toUrlForm(requestContext, true);
        response.sendRedirect(redirect);
      }
    } catch (RepositoryException | IOException e) {
      e.printStackTrace();
    }
    final HttpSession finalSession = request.getSession();
    finalSession.setAttribute("success", true);

  }

  private Node createSitemapNode(SiteMapItemRepresentation siteMapItem, String finalParentId, HstRequestContext requestContext, Mount editingMount) throws RepositoryException {
    PagesHelper pagesHelper = new PagesHelper();
    pagesHelper.setPageComposerContextService(new PageComposerContextService());
    LockHelper lockHelper = new LockHelper();
    Session session = requestContext.getSession();
    Node parent = session.getNodeByIdentifier(finalParentId);
    String encodedName = this.getURLDecodedJcrEncodedName(siteMapItem.getName(), requestContext);
    this.validateTarget(session, parent.getPath() + "/" + encodedName, editingMount.getHstSite().getSiteMap());
    Node newSitemapNode = parent.addNode(encodedName, "hst:sitemapitem");
    lockHelper.acquireLock(newSitemapNode, 0L);
    setSitemapItemProperties(siteMapItem, newSitemapNode, editingMount);
    Node prototypePage = session.getNodeByIdentifier(siteMapItem.getComponentConfigurationId());
    String prototypeApplicationId = JcrUtils.getStringProperty(prototypePage, "hst:applicationId", (String) null);
    String targetPageNodeName = this.getSiteMapPathPrefixPart(newSitemapNode) + "-" + prototypePage.getName();
    Node newPage = pagesHelper.create(prototypePage, targetPageNodeName);
    newSitemapNode.setProperty("hst:componentconfigurationid", "hst:pages/" + newPage.getName());
    Map<String, String> modifiedLocalParameters = siteMapItem.getLocalParameters();
    this.setLocalParameters(newSitemapNode, modifiedLocalParameters);
    Set<String> modifiedRoles = siteMapItem.getRoles();
    this.setRoles(newSitemapNode, modifiedRoles);
    if (prototypeApplicationId != null) {
      newSitemapNode.setProperty("hst:applicationId", prototypeApplicationId);
    }
    return newSitemapNode;

  }

  private String getSiteMapPathPrefixPart(Node siteMapNode) throws RepositoryException {
    Node crNode = siteMapNode;

    StringBuilder siteMapPathPrefixBuilder;
    for (siteMapPathPrefixBuilder = new StringBuilder(); crNode.isNodeType("hst:sitemapitem"); crNode = crNode.getParent()) {
      if (siteMapPathPrefixBuilder.length() > 0) {
        siteMapPathPrefixBuilder.insert(0, "-");
      }

      siteMapPathPrefixBuilder.insert(0, crNode.getName());
    }

    return siteMapPathPrefixBuilder.toString();
  }

  private Mount getEditingMount(HstRequestContext requestContext) throws RepositoryException {
    String renderingMountId = (String) requestContext.getServletRequest().getSession(true).getAttribute("org.hippoecm.hst.container.render_mount");
    if (renderingMountId == null) {
      throw new IllegalStateException("Could not find rendering mount id on request session.");
    }
    Mount editingMount = requestContext.getVirtualHost().getVirtualHosts().getMountByIdentifier(renderingMountId);
    String msg;
    if (editingMount == null) {
      msg = String.format("Could not find a Mount for identifier + '%s'", renderingMountId);
      throw new IllegalStateException(msg);
    } else if (!"preview".equals(editingMount.getType())) {
      msg = String.format("Expected a preview (decorated) mount but '%s' is not of type preview.", editingMount.toString());
      throw new IllegalStateException(msg);
    }
    String previewWorkspaceSiteMapPath = editingMount.getHstSite().getConfigurationPath() + WORKSPACE_SITEMAP_CONFIGURATION_PATH;
    if (!requestContext.getSession().nodeExists(previewWorkspaceSiteMapPath)) {
      this.createWorkspaceSiteMapInPreviewAndLive(previewWorkspaceSiteMapPath, requestContext.getSession());
    }
    return editingMount;
  }

  private void setLocalParameters(Node node, Map<String, String> modifiedLocalParameters) throws RepositoryException {
    if (modifiedLocalParameters != null) {
      if (modifiedLocalParameters.isEmpty()) {
        this.removeProperty(node, "hst:parameternames");
        this.removeProperty(node, "hst:parametervalues");
      } else {
        String[][] namesAndValues = this.mapToNameValueArrays(modifiedLocalParameters);
        node.setProperty("hst:parameternames", namesAndValues[0], 1);
        node.setProperty("hst:parametervalues", namesAndValues[1], 1);
      }

    }
  }

  private void setRoles(Node node, Set<String> modifiedRoles) throws RepositoryException {
    if (modifiedRoles != null) {
      if (modifiedRoles.isEmpty()) {
        this.removeProperty(node, "hst:roles");
      } else {
        String[] roles = (String[]) Iterables.toArray(modifiedRoles, String.class);
        node.setProperty("hst:roles", roles, 1);
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
    } else if (!target.contains("/hst:workspace/")) {
      message = String.format("Target '%s' does not contain '%s'.", target, "/hst:workspace/");
      throw new ClientException(message, ClientError.ITEM_NOT_CORRECT_LOCATION);
    } else if (!session.nodeExists(StringUtils.substringBeforeLast(target, "/"))) {
      message = String.format("Parent of target node '%s' does not exist", target);
      throw new ClientException(message, ClientError.INVALID_URL);
    } else if (session.nodeExists(target)) {
      Node targetNode = session.getNode(target);
      if (this.isMarkedDeleted(targetNode)) {
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
      String targetConfig = StringUtils.substringBefore(target, "/hst:workspace");
      String siteMapRelPath;
      if (!siteMapPath.startsWith(targetConfig) && !target.startsWith(StringUtils.substringBefore(siteMapPath, "/hst:sitemap") + "-preview/hst:workspace/hst:sitemap")) {
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
    String liveWorkspacePath = previewWorkspacePath.replace("-preview/", "/");
    session.getNode(previewWorkspacePath).addNode("hst:sitemap", "hst:sitemap");
    if (!session.nodeExists(liveWorkspacePath + "/" + "hst:sitemap")) {
      session.getNode(liveWorkspacePath).addNode("hst:sitemap", "hst:sitemap");
    }

  }

  private void setSitemapItemProperties(SiteMapItemRepresentation siteMapItem, Node jcrNode, Mount editingMount) throws RepositoryException {
    if (siteMapItem.getScheme() != null) {
      this.setProperty(jcrNode, "hst:scheme", siteMapItem.getScheme());
    }

    if (siteMapItem.getPrimaryDocumentRepresentation() != null && siteMapItem.getPrimaryDocumentRepresentation().getPath() != null) {
      String absPath = siteMapItem.getPrimaryDocumentRepresentation().getPath();
      String rootContentPath = editingMount.getContentPath();
      if (absPath.startsWith(rootContentPath + "/")) {
        this.setProperty(jcrNode, "hst:relativecontentpath", absPath.substring(rootContentPath.length() + 1));
      } else if (absPath.equals("")) {
        this.removeProperty(jcrNode, "hst:relativecontentpath");
      } else {
        log.info("Cannot set '{}' for relative content path because does not start with root channel content path '{}'", absPath, rootContentPath + "/");
      }
    } else if (siteMapItem.getRelativeContentPath() != null) {
      this.setProperty(jcrNode, "hst:relativecontentpath", siteMapItem.getRelativeContentPath());
    }

    if (siteMapItem.getPageTitle() != null) {
      this.setProperty(jcrNode, "hst:pagetitle", siteMapItem.getPageTitle());
    }

  }

  private void removeProperty(Node node, String name) throws RepositoryException {
    if (node.hasProperty(name)) {
      node.getProperty(name).remove();
    }

  }

  private void setProperty(Node jcrNode, String propName, String propValue) throws RepositoryException {
    if (StringUtils.isEmpty(propValue)) {
      this.removeProperty(jcrNode, propName);
    } else {
      jcrNode.setProperty(propName, propValue);
    }
  }

  private String getURLDecodedJcrEncodedName(String name, HstRequestContext requestContext) {
    String encoding = this.getEncoding(requestContext);
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
    return "deleted".equals(JcrUtils.getStringProperty(node, "hst:state", (String) null));
  }

  private String buildSearchEndpoint(Map<String, String> properties, String theme) {
    return properties.get("account_id") +
        "_lpm_pagedata_v2/select?q=theme:" + theme +
        "&rows=" + properties.get("rows") +
        "&fl=" + properties.get("fl") + "&wt=" + properties.get("wt");
  }

}
