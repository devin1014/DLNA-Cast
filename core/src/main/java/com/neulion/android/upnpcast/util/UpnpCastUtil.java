package com.neulion.android.upnpcast.util;

import android.text.TextUtils;

import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.item.VideoItem;
import org.seamless.util.MimeType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * User: liuwei(wei.liu@neulion.com.com)
 * Date: 2018-07-03
 * Time: 10:42
 */
public class UpnpCastUtil
{
    private static final String DIDL_LITE_FOOTER = "</DIDL-Lite>";
    private static final String DIDL_LITE_HEADER = "<?xml version=\"1.0\"?>" + "<DIDL-Lite " + "xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" " +
            "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" " + "xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" " +
            "xmlns:dlna=\"urn:schemas-dlna-org:metadata-1-0/\">";
    private static final String CAST_PARENT_ID = "1";
    private static final String CAST_CREATOR = "NLUpnpCast";
    private static final String CAST_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(CAST_DATE_FORMAT, Locale.US);

    public static String pushMediaToRender(String url, String id, String name, int duration)
    {
        //TODO,the params!
        long size = 0;
        long bitrate = 0;
        Res castRes = new Res(new MimeType(ProtocolInfo.WILDCARD, ProtocolInfo.WILDCARD), size, url);
        castRes.setBitrate(bitrate);
        castRes.setDuration(CastUtils.getStringTime(duration));

        String resolution = "description";
        VideoItem videoItem = new VideoItem(id, CAST_PARENT_ID, name, CAST_CREATOR, castRes);
        videoItem.setDescription(resolution);

        return createItemMetadata(videoItem);
    }

    private static String createItemMetadata(DIDLObject item)
    {
        StringBuilder metadata = new StringBuilder();
        metadata.append(DIDL_LITE_HEADER);
        metadata.append(String.format("<item id=\"%s\" parentID=\"%s\" restricted=\"%s\">", item.getId(), item.getParentID(), item.isRestricted() ? "1" : "0"));
        metadata.append(String.format("<dc:title>%s</dc:title>", item.getTitle()));
        String creator = item.getCreator();
        if (creator != null)
        {
            creator = creator.replaceAll("<", "_");
            creator = creator.replaceAll(">", "_");
        }
        metadata.append(String.format("<upnp:artist>%s</upnp:artist>", creator));
        metadata.append(String.format("<upnp:class>%s</upnp:class>", item.getClazz().getValue()));
        metadata.append(String.format("<dc:date>%s</dc:date>", DATE_FORMAT.format(new Date())));

        // metadata.append(String.format("<upnp:album>%s</upnp:album>",item.get);
        // <res protocolInfo="http-get:*:audio/mpeg:*"
        // resolution="640x478">http://192.168.1.104:8088/Music/07.我醒著做夢.mp3</res>

        Res res = item.getFirstResource();
        if (res != null)
        {
            // protocol info
            String protocolInfo = "";
            ProtocolInfo pi = res.getProtocolInfo();
            if (pi != null)
            {
                protocolInfo = String.format("protocolInfo=\"%s:%s:%s:%s\"", pi.getProtocol(), pi.getNetwork(), pi.getContentFormatMimeType(), pi.getAdditionalInfo());
            }

            // resolution, extra info, not adding yet
            String resolution = "";
            if (!TextUtils.isEmpty(res.getResolution()))
            {
                resolution = String.format("resolution=\"%s\"", res.getResolution());
            }

            // duration
            String duration = "";
            if (!TextUtils.isEmpty(res.getDuration()))
            {
                duration = String.format("duration=\"%s\"", res.getDuration());
            }

            // res begin
            // metadata.append(String.format("<res %s>", protocolInfo)); // no resolution & duration yet
            metadata.append(String.format("<res %s %s %s>", protocolInfo, resolution, duration));

            // url
            metadata.append(res.getValue());

            // res end
            metadata.append("</res>");
        }
        metadata.append("</item>");

        metadata.append(DIDL_LITE_FOOTER);

        return metadata.toString();
    }
}
