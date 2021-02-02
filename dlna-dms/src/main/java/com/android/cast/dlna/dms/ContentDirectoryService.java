package com.android.cast.dlna.dms;

import org.fourthline.cling.support.contentdirectory.AbstractContentDirectoryService;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryErrorCode;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException;
import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.BrowseResult;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

public class ContentDirectoryService extends AbstractContentDirectoryService {

    @Override
    public BrowseResult browse(String objectID,
                               BrowseFlag browseFlag,
                               String filter,
                               long firstResult,
                               long maxResults,
                               SortCriterion[] orderBy) throws ContentDirectoryException {
        Container resultBean = ContentFactory.getInstance().getContent(objectID);
        DIDLContent content = new DIDLContent();
        for (Container c : resultBean.getContainers()) {
            content.addContainer(c);
        }
        for (Item item : resultBean.getItems()) {
            content.addItem(item);
        }
        int count = resultBean.getChildCount();
        String contentModel;
        try {
            contentModel = new DIDLParser().generate(content);
        } catch (Exception e) {
            throw new ContentDirectoryException(ContentDirectoryErrorCode.CANNOT_PROCESS, e.toString());
        }
        return new BrowseResult(contentModel, count, count);
    }

    @Override
    public BrowseResult search(String containerId, String searchCriteria,
                               String filter, long firstResult, long maxResults,
                               SortCriterion[] orderBy) throws ContentDirectoryException {
        // You can override this method to implement searching!
        return super.search(containerId, searchCriteria, filter, firstResult, maxResults, orderBy);
    }
}
