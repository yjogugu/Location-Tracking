package com.example.locationtracking

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_frofile.*
import kotlinx.android.synthetic.main.friend_list_itme.view.*

class Friend_Adapter(list: ArrayList<Friend_Data>) : RecyclerView.Adapter<Friend_Adapter.Friend_Adapter_Holder>() {
    private var mdata : ArrayList<Friend_Data>? = list //생성자
    //ArrayList<Friend_Data> mdata;
    //
    //    public Friend_Adapter(ArrayList<Friend_Data> list){
    //        mdata = list;
    //    } 자바 버전

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): Friend_Adapter_Holder {
        var view = LayoutInflater.from(p0!!.context).inflate(R.layout.friend_list_itme,p0,false)
        return Friend_Adapter_Holder(view)
    }

    override fun getItemCount(): Int {
        return mdata!!.size
    }

    override fun onBindViewHolder(p0: Friend_Adapter_Holder, p1: Int) {
        p0.freind_name.text = mdata!!.get(p1).title
        Glide.with(p0.itemView.context)
            .load("URL"+ mdata!!.get(p1).image)
            .clone()
            .apply(RequestOptions().centerCrop().circleCrop())
            //.override(50,40)
            .error(R.drawable.baseline_account_box_black_18dp)
            .skipMemoryCache (true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .thumbnail(0.5f)
            .into(p0.freind_image)
    }


    class Friend_Adapter_Holder(view: View) : RecyclerView.ViewHolder(view){
        var freind_name = view.findViewById<TextView>(R.id.friend_list_friend)
        val freind_image = view.findViewById<ImageView>(R.id.friend_list_item_image)
    }
}