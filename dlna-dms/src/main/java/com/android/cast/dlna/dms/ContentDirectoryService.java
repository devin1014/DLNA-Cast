package com.android.cast.dlna.dms;

import org.fourthline.cling.support.contentdirectory.AbstractContentDirectoryService;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.BrowseResult;
import org.fourthline.cling.support.model.SortCriterion;

public class ContentDirectoryService extends AbstractContentDirectoryService {

    @Override
    public BrowseResult browse(String objectID, BrowseFlag browseFlag, String filter,
                               long firstResult, long maxResults, SortCriterion[] orderBy) throws ContentDirectoryException {
        return ContentFactory.getInstance().getContent(objectID);
    }

    @Override
    public BrowseResult search(String containerId, String searchCriteria, String filter,
                               long firstResult, long maxResults, SortCriterion[] orderBy) throws ContentDirectoryException {
        // You can override this method to implement searching!
        return super.search(containerId, searchCriteria, filter, firstResult, maxResults, orderBy);
    }
}
