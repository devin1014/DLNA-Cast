package com.android.cast.dlna.dms

import com.android.cast.dlna.core.Logger
import org.fourthline.cling.support.contentdirectory.AbstractContentDirectoryService
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException
import org.fourthline.cling.support.model.BrowseFlag
import org.fourthline.cling.support.model.BrowseResult
import org.fourthline.cling.support.model.SortCriterion

internal class ContentDirectoryService : AbstractContentDirectoryService() {
    private val logger = Logger.create("ContentDirectoryService")

    @Throws(ContentDirectoryException::class)
    override fun browse(
        objectID: String,
        browseFlag: BrowseFlag,
        filter: String,
        firstResult: Long,
        maxResults: Long,
        orderBy: Array<SortCriterion>,
    ): BrowseResult? {
        logger.i("browse: $objectID, $browseFlag, $filter, $firstResult, $maxResults")
        return ContentFactory.getContent(objectID)
    }

    @Throws(ContentDirectoryException::class)
    override fun search(
        containerId: String,
        searchCriteria: String,
        filter: String,
        firstResult: Long,
        maxResults: Long,
        orderBy: Array<SortCriterion>,
    ): BrowseResult {
        logger.i("search: $containerId, $searchCriteria, $filter, $firstResult, $maxResults")
        return super.search(containerId, searchCriteria, filter, firstResult, maxResults, orderBy)
    }
}