package com.android.cast.dlna.dms

import org.fourthline.cling.support.contentdirectory.AbstractContentDirectoryService
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException
import org.fourthline.cling.support.model.BrowseFlag
import org.fourthline.cling.support.model.BrowseResult
import org.fourthline.cling.support.model.SortCriterion

class ContentDirectoryService : AbstractContentDirectoryService() {
    @Throws(ContentDirectoryException::class)
    override fun browse(
        objectID: String,
        browseFlag: BrowseFlag,
        filter: String,
        firstResult: Long,
        maxResults: Long,
        orderBy: Array<SortCriterion>
    ): BrowseResult? = ContentFactory.getContent(objectID)

    @Throws(ContentDirectoryException::class)
    override fun search(
        containerId: String,
        searchCriteria: String,
        filter: String,
        firstResult: Long,
        maxResults: Long,
        orderBy: Array<SortCriterion>
    ): BrowseResult {
        // You can override this method to implement searching!
        return super.search(containerId, searchCriteria, filter, firstResult, maxResults, orderBy)
    }
}