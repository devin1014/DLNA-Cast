package com.android.cast.dlna.dms;

import com.orhanobut.logger.Logger;

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
        Logger.i("ContentDirectoryService.browse: %s, %s, %s", objectID, browseFlag, filter);
        try {
            DIDLContent didl = new DIDLContent();
            ContentNode contentNode = ContentTree.getNode(objectID);
            if (contentNode == null) {
                Logger.w("ContentDirectoryService.browse result: empty.");
                return new BrowseResult("", 0, 0);
            }
            if (contentNode.isItem()) {
                Logger.w("ContentDirectoryService.browse result: %s", contentNode.getItem().getTitle());
                didl.addItem(contentNode.getItem());
                return new BrowseResult(new DIDLParser().generate(didl), 1, 1);
            } else {
                if (browseFlag == BrowseFlag.METADATA) {
                    didl.addContainer(contentNode.getContainer());
                    return new BrowseResult(new DIDLParser().generate(didl), 1, 1);
                } else {
                    for (Container container : contentNode.getContainer().getContainers()) {
                        didl.addContainer(container);
                        Logger.d("ContentDirectoryService getting child container: " + container.getTitle());
                    }
                    for (Item item : contentNode.getContainer().getItems()) {
                        didl.addItem(item);
                        Logger.d("ContentDirectoryService getting child item: " + item.getTitle());
                    }
                    return new BrowseResult(new DIDLParser().generate(didl),
                            contentNode.getContainer().getChildCount(),
                            contentNode.getContainer().getChildCount());
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.e(ex, "ContentDirectoryService.browse: failed.");
            throw new ContentDirectoryException(ContentDirectoryErrorCode.CANNOT_PROCESS, ex.toString());
        }
    }

    @Override
    public BrowseResult search(String containerId, String searchCriteria,
                               String filter, long firstResult, long maxResults,
                               SortCriterion[] orderBy) throws ContentDirectoryException {
        // You can override this method to implement searching!
        return super.search(containerId, searchCriteria, filter, firstResult, maxResults, orderBy);
    }
}
