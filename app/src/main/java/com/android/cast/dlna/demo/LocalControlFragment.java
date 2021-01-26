package com.android.cast.dlna.demo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.cast.dlna.DLNACastManager;
import com.android.cast.dlna.dms.JettyResourceServer;
import com.android.cast.dlna.dms.MediaServer;
import com.android.cast.dlna.dms.Utils;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.item.VideoItem;
import org.seamless.util.MimeType;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LocalControlFragment extends Fragment implements IDisplayDevice {

    private JettyResourceServer mJettyResourceServer;
    private TextView mPickupContent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_local_control, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mJettyResourceServer = new JettyResourceServer();
        mJettyResourceServer.start();

        AndroidUpnpService upnpService = DLNACastManager.getInstance().getService();
        if (upnpService != null) {
            try {
                MediaServer mediaServer = new MediaServer(view.getContext().getApplicationContext(), NetworkUtils.getWiFiIPAddress(getActivity()));
                upnpService.getRegistry().addDevice(mediaServer.getDevice());
            } catch (ValidationException e) {
                e.printStackTrace();
            }
        }

        initComponent(view);
    }

    private void initComponent(View view) {
        mPickupContent = view.findViewById(R.id.local_ctrl_pick_content_text);
        view.findViewById(R.id.local_ctrl_pick_content).setOnClickListener(v -> selectVideo());
        view.findViewById(R.id.local_ctrl_cast).setOnClickListener(v -> {
                    if (mDevice != null) {
                        String metadata = pushMediaToRender(mCastPathUrl, "id", "name", "0", 0);
                        //DLNACastManager.getInstance().cast(mDevice, CastVideo.newInstance(mCastPathUrl, Constants.CAST_ID, Constants.CAST_NAME));
                        DLNACastManager.getInstance().getService().getControlPoint()
                                .execute(new SetAVTransportURI(mDevice.findService(DLNACastManager.SERVICE_AV_TRANSPORT), mCastPathUrl, metadata) {
                                    @Override
                                    public void success(ActionInvocation invocation) {
                                    }

                                    @Override
                                    public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                                    }
                                });
                    }
                }
        );
    }

    private static final String DIDL_LITE_FOOTER = "</DIDL-Lite>";
    private static final String DIDL_LITE_HEADER = "<?xml version=\"1.0\"?>" +
            "<DIDL-Lite " + "xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" " +
            "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" " + "xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" " +
            "xmlns:dlna=\"urn:schemas-dlna-org:metadata-1-0/\">";

    private String pushMediaToRender(String url, String id, String name, String duration, int ItemType) {
        long size = 0;
        long bitrate = 0;
        Res res = new Res(new MimeType(ProtocolInfo.WILDCARD, ProtocolInfo.WILDCARD), size, url);

        String creator = "unknow";
        String resolution = "unknow";
        String metadata = null;

        // switch (ItemType){
        //     case IMAGE_TYPE:
        //         ImageItem imageItem = new ImageItem(id, "0", name, creator, res);
        //         metadata = createItemMetadata(imageItem);
        //         break;
        //     case VIDEO_TYPE:
        VideoItem videoItem = new VideoItem(id, "0", name, creator, res);
        metadata = createItemMetadata(videoItem);
        //     break;
        // case AUDIO_TYPE:
        //     AudioItem audioItem = new AudioItem(id,"0",name,creator,res);
        //     metadata = createItemMetadata(audioItem);
        //     break;
        // }

        // Log.e(TAG, "metadata: " + metadata);
        return metadata;
    }

    private String createItemMetadata(DIDLObject item) {
        StringBuilder metadata = new StringBuilder();
        metadata.append(DIDL_LITE_HEADER);

        metadata.append(String.format("<item id=\"%s\" parentID=\"%s\" restricted=\"%s\">", item.getId(), item.getParentID(), item.isRestricted() ? "1" : "0"));

        metadata.append(String.format("<dc:title>%s</dc:title>", item.getTitle()));
        String creator = item.getCreator();
        if (creator != null) {
            creator = creator.replaceAll("<", "_");
            creator = creator.replaceAll(">", "_");
        }
        metadata.append(String.format("<upnp:artist>%s</upnp:artist>", creator));
        metadata.append(String.format("<upnp:class>%s</upnp:class>", item.getClazz().getValue()));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date now = new Date();
        String time = sdf.format(now);
        metadata.append(String.format("<dc:date>%s</dc:date>", time));

        Res res = item.getFirstResource();
        if (res != null) {
            // protocol info
            String protocolinfo = "";
            ProtocolInfo pi = res.getProtocolInfo();
            if (pi != null) {
                protocolinfo = String.format("protocolInfo=\"%s:%s:%s:%s\"", pi.getProtocol(), pi.getNetwork(), pi.getContentFormatMimeType(), pi
                        .getAdditionalInfo());
            }
            //Log.e(TAG, "protocolinfo: " + protocolinfo);

            // resolution, extra info, not adding yet
            String resolution = "";
            if (res.getResolution() != null && res.getResolution().length() > 0) {
                resolution = String.format("resolution=\"%s\"", res.getResolution());
            }

            // duration
            String duration = "";
            if (res.getDuration() != null && res.getDuration().length() > 0) {
                duration = String.format("duration=\"%s\"", res.getDuration());
            }

            // res begin
            //            metadata.append(String.format("<res %s>", protocolinfo)); // no resolution & duration yet
            metadata.append(String.format("<res %s %s %s>", protocolinfo, resolution, duration));

            // url
            String url = res.getValue();
            metadata.append(url);

            // res end
            metadata.append("</res>");
        }
        metadata.append("</item>");

        metadata.append(DIDL_LITE_FOOTER);

        return metadata.toString();
    }

    private void selectVideo() {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(i, 222);
    }

    private void selectAudio() {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 222);
    }

    private void selectImage() {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 222);
    }

    private String mCastPathUrl = "";

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 222 && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            String path = Utils.getRealPathFromUriAboveApi19(getActivity(), uri);
            mPickupContent.setText(path);
            mCastPathUrl = NetworkUtils.getWiFiIPAddress(getActivity()) + path;
        }
    }

    private Device<?, ?, ?> mDevice;

    @Override
    public void setCastDevice(Device<?, ?, ?> device) {
        mDevice = device;
    }

    @Override
    public void onDestroyView() {
        mJettyResourceServer.stop();
        super.onDestroyView();
    }
}
