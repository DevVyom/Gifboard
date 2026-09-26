package com.vyom.moderngifboard.ime
import android.view.*
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vyom.moderngifboard.model.GifItem
class GifAdapter(private val onClick:(GifItem)->Unit,private val onLong:(GifItem)->Unit):RecyclerView.Adapter<GifAdapter.H>(){
 private val items=mutableListOf<GifItem>()
 fun set(newItems:List<GifItem>){items.clear();items.addAll(newItems);notifyDataSetChanged()}
 fun append(more:List<GifItem>){val s=items.size;items.addAll(more.filter{n->items.none{it.mediaUrl==n.mediaUrl}});notifyItemRangeInserted(s,items.size-s)}
 class H(val image:ImageView):RecyclerView.ViewHolder(image)
 override fun onCreateViewHolder(p:ViewGroup,v:Int)=H(ImageView(p.context).apply{scaleType=ImageView.ScaleType.CENTER_CROP;layoutParams=ViewGroup.LayoutParams(-1,(132*resources.displayMetrics.density).toInt());setPadding(3,3,3,3)})
 override fun getItemCount()=items.size
 override fun onBindViewHolder(h:H,p:Int){val item=items[p];Glide.with(h.image).asGif().load(item.previewUrl).centerCrop().into(h.image);h.image.setOnClickListener{onClick(item)};h.image.setOnLongClickListener{onLong(item);true}}
}