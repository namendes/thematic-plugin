package com.dxpfc.thematic.components;

import org.hippoecm.hst.component.support.bean.BaseHstComponent;
import org.hippoecm.hst.component.support.forms.FormMap;
import org.hippoecm.hst.container.RequestContextProvider;
import org.hippoecm.hst.content.annotations.Persistable;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.hst.pagecomposer.jaxrs.model.DocumentRepresentation;
import org.hippoecm.hst.pagecomposer.jaxrs.model.SiteMapItemRepresentation;
import org.hippoecm.hst.pagecomposer.jaxrs.services.helpers.LockHelper;
import org.hippoecm.hst.pagecomposer.jaxrs.services.helpers.SiteMapHelper;
import org.hippoecm.hst.site.HstServices;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.List;

public class ThematicControlPanelComponent extends BaseHstComponent {


  @Override
  public void doBeforeRender(HstRequest request, HstResponse response) throws HstComponentException {
    request.setAttribute("thematic", "");
  }

  @Override
  public void doAction(HstRequest request, HstResponse response) throws HstComponentException {

    Session session = null;
    try {
      FormMap map = new FormMap(request, new String[]{"theme"});
      String theme = map.getField("theme").getValue();
      HstRequestContext requestContext = RequestContextProvider.get();
      String baseSiteMapUuid = requestContext.getResolvedMount().getMount().getChannel().getSiteMapId();
      Node baseSitemapNode = requestContext.getSession().getNodeByIdentifier(baseSiteMapUuid);

      //Node sitemapNODE = requestContext.getSession().getNode("/hst:hst/hst:configurations/hst:default/hst:sitemap/thematic");
      Node prototypeNODE = requestContext.getSession().getNode("/hst:hst/hst:configurations/hst:default/hst:pages/thematicBasePage");

      Node sitemapNODE = baseSitemapNode.getNode("thematic");

      SiteMapItemRepresentation siteMapItem = new SiteMapItemRepresentation();
      siteMapItem.setName(theme);
      siteMapItem.setPageTitle(theme);
      siteMapItem.setComponentConfigurationId(prototypeNODE.getIdentifier());
      DocumentRepresentation document = new DocumentRepresentation("/hst:hst/hst:configurations/dxpfcdemo-preview/hst:workspace/hst:sitemap/thematic/"+ theme,
          theme, true, true);
      siteMapItem.setPrimaryDocumentRepresentation(document);

      SiteMapHelper siteMapHelper = HstServices.getComponentManager().getComponent("siteMapHelper", "org.hippoecm.hst.pagecomposer");

      final Node createdSitemapNode = siteMapHelper.create(siteMapItem, sitemapNODE.getIdentifier());





      session = requestContext.getSession();
      session.save();
      unlockAndRelockLandingPages(createdSitemapNode);
      session = RequestContextProvider.get().getSession();
      Node node = session.getNodeByIdentifier("895fb1b6-410d-4972-9894-6b6a06d2b361");
      if(node != null) {
        node.getProperties();
      }


    } catch (RepositoryException e) {
      e.printStackTrace();
    }

  }

  public static void unlockAndRelockLandingPages(Node sitemapNode) {
    try {
      LockHelper lockHelper = new LockHelper();
      HstRequestContext requestContext = RequestContextProvider.get();
      Session session = requestContext.getSession();
      String hstConfigPath = requestContext.getResolvedMount().getMount().getChannel().getHstConfigPath();
        //unlock the sitemap node and its corresponding hst:component
        lockHelper.unlock(sitemapNode);
        Node createdHstPagesNode = session.getNode(hstConfigPath + "/hst:workspace/" + sitemapNode.getProperty("hst:componentconfigurationid").getString());
        lockHelper.unlock(createdHstPagesNode);
        //session.save();
        lockHelper.acquireLock(sitemapNode, session.getUserID().split(",")[0], 0);
        lockHelper.acquireLock(createdHstPagesNode, session.getUserID().split(",")[0], 0);
        session.save();

    } catch (RepositoryException ex) {
      ex.printStackTrace();
    }
  }
}
