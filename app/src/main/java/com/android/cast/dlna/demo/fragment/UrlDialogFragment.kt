package com.android.cast.dlna.demo.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.android.cast.dlna.demo.R
import com.android.cast.dlna.demo.VideoUrl
import com.android.cast.dlna.demo.videoUrlList

interface OnUrlSelectListener {
    fun onUrlSelected(video: VideoUrl)
}

class CastUrlDialogFragment : DialogFragment() {

    companion object {
        fun show(fragmentManager: FragmentManager, listener: OnUrlSelectListener? = null) {
            CastUrlDialogFragment().apply {
                this.onUrlSelectListener = listener
            }.show(fragmentManager, "CastFragment")
        }
    }

    var onUrlSelectListener: OnUrlSelectListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NO_TITLE, theme)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_cast, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<RecyclerView>(R.id.recycler_view).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ListAdapter()
        }
    }

    private inner class ListAdapter : Adapter<ListViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
            return ListViewHolder(LayoutInflater.from(requireContext()).inflate(R.layout.item_video_url, parent, false))
        }

        override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
            holder.setData(videoUrlList[position])
        }

        override fun getItemCount(): Int = videoUrlList.size
    }

    private inner class ListViewHolder(itemView: View) : ViewHolder(itemView), OnClickListener {
        private val url: TextView = itemView.findViewById(R.id.video_url)
        private val title: TextView = itemView.findViewById(R.id.video_title)

        init {
            itemView.setOnClickListener(this)
        }

        fun setData(data: VideoUrl) {
            itemView.tag = data
            url.text = data.url
            title.text = data.title
        }

        override fun onClick(v: View) {
            (onUrlSelectListener
                ?: parentFragment as? OnUrlSelectListener
                ?: activity as? OnUrlSelectListener)?.onUrlSelected(v.tag as VideoUrl)
            dismiss()
        }
    }
}

