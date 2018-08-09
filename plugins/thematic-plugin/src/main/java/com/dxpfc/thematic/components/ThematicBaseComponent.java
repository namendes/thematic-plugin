package com.dxpfc.thematic.components;

import org.hippoecm.hst.component.support.bean.BaseHstComponent;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.onehippo.cms7.crisp.api.broker.ResourceServiceBroker;
import org.onehippo.cms7.crisp.api.resource.Resource;
import org.onehippo.cms7.services.HippoServiceRegistry;

public class ThematicBaseComponent extends BaseHstComponent {

  @Override
  public void doBeforeRender(HstRequest request, HstResponse response) throws HstComponentException {

    ResourceServiceBroker broker = HippoServiceRegistry.getService(ResourceServiceBroker.class);
    String path = "?rows=100&account_id=5017&domain_key=neimanmarcus_com&request_id=1&url=xx&fl=pid,title,brand,price,sale_price,promotions,thumb_image,sku_thumb_images,sku_swatch_images,sku_color_group,url,price_range,sale_price_range,description&sc2_mode=True&request_type=thematic&q=%s&debug=true&";
    String baseUrl = request.getRequestContext().getBaseURL().getRequestPath();
    String splitUrl[] = baseUrl.split("/");
    int urlLength = splitUrl.length;
    String theme = splitUrl[urlLength-1];
    Resource thematic = broker.resolve("thematicAPI", String.format(path, theme));
    request.setAttribute("thematic", thematic);
  }
}
