package com.haojiangbo.list.apder;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.haojiangbo.audio.AudioTrackManager;
import com.haojiangbo.ndkdemo.MettingActivite;
import com.haojiangbo.ndkdemo.R;
import com.haojiangbo.widget.VideoSurface;

import java.util.List;

/**
 * 公司list集合
 */
public class VideoItemListApader extends RecyclerView.Adapter<VideoItemListApader.ViewHoder> {

    private List<UserInfoModel> records;

    @Override
    public ViewHoder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.video_item, viewGroup, false);
        return new ViewHoder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHoder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onBindViewHolder(ViewHoder holder, final int position) {
        UserInfoModel model = records.get(position);
        holder.uname.setText(model.getName());
        if(MettingActivite.videoSurfaces.get(model.getUid()) != null){
            return;
        }
        if (model.isCurrent() && MettingActivite.current == null) {
            MettingActivite.current = holder.video;
            if(null != MettingActivite.getInstand()){
                MettingActivite.getInstand().initCamera();
            }
        }
        if(MettingActivite.videoSurfaces.get(model.getUid()) == null){
            MettingActivite.videoSurfaces.put(model.getUid(), holder.video);
            MettingActivite.audioManager.put(model.getUid(), AudioTrackManager.getInstance(model.getUid()));
        }

        /*holder.binding.setVariable(BR.company, records.get(position));
        holder.binding.executePendingBindings();
        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new AseUseCompanyMessage(records.get(position),AseUseCompanyMessage.EDIT));
            }
        });
*/
    }


    /**
     * public void addVideoSurface(ArrayList<String> uids,boolean isUseCurrentView) {
     * LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
     * for (int i = 0; i < uids.size(); i++) {
     * String sessionId = uids.get(i);
     * VideoSurface view = (VideoSurface) layoutInflater.inflate(R.layout.video_pod, null);
     * VideoSurface tmp =  videoSurfaces.get(sessionId);
     * if(ControlProtocolManager.getSessionId().equals(sessionId) && current == null){
     * current = view;
     * }
     * if(null != tmp){
     * continue;
     * }
     * videoSurfaces.put(sessionId,view);
     * videoContainerLayout.addView(view, 400, 400);
     * audioManager.put(sessionId,AudioTrackManager.getInstance(sessionId));
     * }
     * Log.e("xxx","xx");
     * }
     *
     * @return
     */

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class ViewHoder extends RecyclerView.ViewHolder {
        VideoSurface video;
        TextView uname;

        public ViewHoder(@NonNull View itemView) {
            super(itemView);
            video = itemView.findViewById(R.id.item_video_surface);
            uname = itemView.findViewById(R.id.item_video_uname);
        }
    }

    public List<UserInfoModel> getRecords() {
        return records;
    }

    public void setRecords(List<UserInfoModel> records) {
        this.records = records;
    }
}
