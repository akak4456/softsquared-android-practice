package com.example.movieapp

import android.content.Context
import android.graphics.Color
import android.text.*
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.text.style.UpdateAppearance
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.example.movieapp.databinding.OneMovieHeaderBinding
import com.example.movieapp.databinding.OneMovieItemExtraInfoBinding
import com.example.movieapp.databinding.OneMovieItemMainBinding
import com.example.movieapp.databinding.OneMovieItemRelateBinding
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthErrorCause
import com.kakao.sdk.user.UserApiClient
import java.lang.reflect.Method


class OneMovieAdapter(private val context: Context, private val oneMovieInfo:OneMovieInfo,private val watchlistDao:WatchlistDao,private val userId:Long): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val TYPE_HEADER = 0
    val TYPE_ITEM_MAIN = 1
    val TYPE_ITEM_RELATE = 2
    val TYPE_ITEM_EXTRA_INFO = 3
    var pageNum = 1
    inner class HeaderViewHolder(private val binding: OneMovieHeaderBinding):RecyclerView.ViewHolder(binding.root){
        fun setUp(){
            binding.leftArrowBtn.setOnClickListener{
                (context as OneMovieActivity).finish()
            }
        }
    }

    inner class ItemMainViewHolder(private val binding: OneMovieItemMainBinding):RecyclerView.ViewHolder(binding.root){
        fun setData(oneMovieInfo: OneMovieInfo) {
            val circularProgressDrawable = CircularProgressDrawable(context)
            circularProgressDrawable.setColorSchemeColors(Color.parseColor("#ff7f00"))
            circularProgressDrawable.strokeWidth = 6f
            circularProgressDrawable.centerRadius = 40f
            circularProgressDrawable.start()

            Glide.with(context)
                .load(TMDBRetrofitClient.imageUrl+oneMovieInfo.oneMovie.poster_path)
                .placeholder(circularProgressDrawable)
                .into(binding.posterImg)
            binding.posterImg.clipToOutline = true

            binding.title.text = oneMovieInfo.oneMovie.title
            binding.releaseDate.text = oneMovieInfo.oneMovie.release_date
            binding.timeLength.text = oneMovieInfo.oneMovie.runtime.toString()+"???"
            binding.rate.text = oneMovieInfo.oneMovie.vote_average.toString()+" ???"

            var overviewText="????????? ?????? ??????"
            if(oneMovieInfo.oneMovie.overview.isNotEmpty()){
                overviewText = oneMovieInfo.oneMovie.overview
            }
            val overview = SpannableStringBuilder(overviewText)
            overview.setSpan(ForegroundColorSpan(Color.parseColor("#9c9fa6")),0,overview.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE )
            var castsText = ""
            val castEnd = (oneMovieInfo.credits.cast.size - 1).coerceAtMost(2)
            for(i in 0..castEnd){
                castsText += oneMovieInfo.credits.cast[i].name
                if(i != castEnd){
                    castsText += " ?? "
                }
            }
            val cast = SpannableStringBuilder("?????? "+castsText)
            cast.setSpan(ForegroundColorSpan(Color.WHITE),0,2,SpannableString.SPAN_INCLUSIVE_INCLUSIVE)
            cast.setSpan(ForegroundColorSpan(Color.parseColor("#9c9fa6")),2,cast.length,SpannableString.SPAN_INCLUSIVE_INCLUSIVE)

            var directorText = "??????"
            for(crew in oneMovieInfo.credits.crew){
                if(crew.job == "Director"){
                    directorText = crew.name
                    break
                }
            }

            val director = SpannableStringBuilder("?????? "+directorText)
            director.setSpan(ForegroundColorSpan(Color.WHITE),0,2,SpannableString.SPAN_INCLUSIVE_INCLUSIVE)
            director.setSpan(ForegroundColorSpan(Color.parseColor("#9c9fa6")),2,director.length,SpannableString.SPAN_INCLUSIVE_INCLUSIVE)

            var writerText = "??????"
            for(crew in oneMovieInfo.credits.crew){
                if(crew.job == "Writer"){
                    writerText = crew.name
                    break
                }
            }

            val writer = SpannableStringBuilder("?????? "+writerText)
            writer.setSpan(ForegroundColorSpan(Color.WHITE),0,2,SpannableString.SPAN_INCLUSIVE_INCLUSIVE)
            writer.setSpan(ForegroundColorSpan(Color.parseColor("#9c9fa6")),2,writer.length,SpannableString.SPAN_INCLUSIVE_INCLUSIVE)

            var producerText = "??????"
            for(crew in oneMovieInfo.credits.crew){
                if(crew.job == "Producer"){
                    producerText = crew.name
                    break
                }
            }

            val producer = SpannableStringBuilder("?????? "+producerText)
            producer.setSpan(ForegroundColorSpan(Color.WHITE),0,2,SpannableString.SPAN_INCLUSIVE_INCLUSIVE)
            producer.setSpan(ForegroundColorSpan(Color.parseColor("#9c9fa6")),2,producer.length,SpannableString.SPAN_INCLUSIVE_INCLUSIVE)
            binding.overviewTv.text = TextUtils.concat(overview,"\n\n",cast,"\n\n",director,"\n\n",writer,"\n\n",producer,"\n")//???????????? ???????????? ????????? ????????? ???????????? ???????????? ??????
            binding.overviewTv.setTrimCollapsedText("?????????")
            binding.overviewTv.setTrimExpandedText("?????????")
            Log.d("TMP","ABC")
            val entities:Array<WatchlistEntity> = watchlistDao.getWatchlistByIdAndOneMovieId(userId,oneMovieInfo.oneMovie.id)
            if(entities.isEmpty()){
                binding.watchlistBtnOnLayout.visibility = View.INVISIBLE
                binding.watchlistBtnOffLayout.visibility = View.VISIBLE
            }else{
                binding.watchlistBtnOnLayout.visibility = View.VISIBLE
                binding.watchlistBtnOffLayout.visibility = View.INVISIBLE
            }
            if(userId != -1L){
                binding.watchlistBtnOnLayout.setOnClickListener{
                    binding.watchlistBtnOnLayout.visibility = View.INVISIBLE
                    binding.watchlistBtnOffLayout.visibility = View.VISIBLE
                    watchlistDao.deleteWatchlist(userId,oneMovieInfo.oneMovie.id)
                    Toast.makeText(context,"?????? ???????????? ?????????????????????",Toast.LENGTH_SHORT).show()
                }
                binding.watchlistBtnOffLayout.setOnClickListener{
                    binding.watchlistBtnOnLayout.visibility = View.VISIBLE
                    binding.watchlistBtnOffLayout.visibility = View.INVISIBLE
                    val watchlistEntity = WatchlistEntity(0,userId,oneMovieInfo.oneMovie.id,oneMovieInfo.oneMovie.poster_path,oneMovieInfo.oneMovie.title,oneMovieInfo.oneMovie.release_date,oneMovieInfo.oneMovie.runtime.toString())
                    watchlistDao.insertWatchlist(watchlistEntity)
                    Toast.makeText(context,"?????? ????????? ?????????????????????",Toast.LENGTH_SHORT).show()
                }
            }else{
                binding.watchlistBtnOffLayout.setOnClickListener{
                    if(UserApiClient.instance.isKakaoTalkLoginAvailable(context)){
                        UserApiClient.instance.loginWithKakaoTalk(context, callback = callback)
                    }else{
                        UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
                    }
                }
            }

        }

    }
    inner class ItemRelateViewHolder(private val binding: OneMovieItemRelateBinding):RecyclerView.ViewHolder(binding.root){
        val rv = binding.rv
        fun setData(title:String) {
            binding.relateTitle.text = title
        }
    }
    inner class ItemExtraInfoViewHolder(private val binding: OneMovieItemExtraInfoBinding):RecyclerView.ViewHolder(binding.root){
        fun setUp(){
            val available = SpannableStringBuilder("???????????? ???????????? ???????????? ????????? ??? ????????????")
            available.setSpan(ForegroundColorSpan(Color.WHITE),0,7,Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            available.setSpan(ColoredUnderlineSpan(Color.WHITE),0,7,Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            available.setSpan(ForegroundColorSpan(Color.parseColor("#9c9fa6")),7,available.length,Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            binding.avaiableDetail.text = available

            val refund = SpannableStringBuilder("???????????? Google Play ?????? ????????? ???????????????.")
            refund.setSpan(ForegroundColorSpan(Color.parseColor("#9c9fa6")),0,5,Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            refund.setSpan(ForegroundColorSpan(Color.WHITE),5,22,Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            refund.setSpan(ColoredUnderlineSpan(Color.WHITE),5,22,Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            refund.setSpan(ForegroundColorSpan(Color.parseColor("#9c9fa6")),22,refund.length,Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            binding.refundDetail.text = refund
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(position){
            0->TYPE_HEADER
            1->TYPE_ITEM_MAIN
            2->TYPE_ITEM_RELATE
            3->TYPE_ITEM_EXTRA_INFO
            else->TYPE_HEADER
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var holder:RecyclerView.ViewHolder? = null
        if(viewType == TYPE_HEADER){
            val bindind = OneMovieHeaderBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            holder = HeaderViewHolder(bindind)
        }else if(viewType == TYPE_ITEM_MAIN){
            val bindind = OneMovieItemMainBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            holder = ItemMainViewHolder(bindind)
        }else if(viewType == TYPE_ITEM_RELATE){
            val bindind = OneMovieItemRelateBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            holder = ItemRelateViewHolder(bindind)
        }else if(viewType == TYPE_ITEM_EXTRA_INFO){
            val bindind = OneMovieItemExtraInfoBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            holder = ItemExtraInfoViewHolder(bindind)
        }

        return holder!!
    }

    override fun getItemCount(): Int = 4

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is HeaderViewHolder){
            val headerViewHolder:HeaderViewHolder = holder
            headerViewHolder.setUp()
        }else if(holder is ItemMainViewHolder){
            val itemMainViewHolder:ItemMainViewHolder = holder
            itemMainViewHolder.setData(oneMovieInfo)
        }else if(holder is ItemRelateViewHolder){
            val itemRelateViewHolder:ItemRelateViewHolder = holder
            val rv:RecyclerView = itemRelateViewHolder.rv
            rv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
            val horizontalAdapter = OneMovieHorizontalAdapter(context)
            rv.adapter = horizontalAdapter
            horizontalAdapter.setList(oneMovieInfo.firstRelate)
            horizontalAdapter.deleteLoading()

            itemRelateViewHolder.setData(oneMovieInfo.oneMovie.title+"???(???) ????????? ??????")
        }else if(holder is ItemExtraInfoViewHolder){
            val itemExtraInfoViewHolder:ItemExtraInfoViewHolder = holder
            itemExtraInfoViewHolder.setUp()
        }
    }

    internal class ColoredUnderlineSpan(private val mColor: Int) : CharacterStyle(),
        UpdateAppearance {
        override fun updateDrawState(tp: TextPaint) {
            try {
                val method: Method = TextPaint::class.java.getMethod(
                    "setUnderlineText",
                    Integer.TYPE,
                    java.lang.Float.TYPE
                )
                method.invoke(tp, mColor, 8.0f)
            } catch (e: Exception) {
                tp.isUnderlineText = true
            }
        }

    }

    val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            when {
                error.toString() == AuthErrorCause.AccessDenied.toString() -> {
                    Toast.makeText(context, "????????? ?????? ???(?????? ??????)", Toast.LENGTH_SHORT).show()
                }
                error.toString() == AuthErrorCause.InvalidClient.toString() -> {
                    Toast.makeText(context, "???????????? ?????? ???", Toast.LENGTH_SHORT).show()
                }
                error.toString() == AuthErrorCause.InvalidGrant.toString() -> {
                    Toast.makeText(context, "?????? ????????? ???????????? ?????? ????????? ??? ?????? ??????", Toast.LENGTH_SHORT).show()
                }
                error.toString() == AuthErrorCause.InvalidRequest.toString() -> {
                    Toast.makeText(context, "?????? ???????????? ??????", Toast.LENGTH_SHORT).show()
                }
                error.toString() == AuthErrorCause.InvalidScope.toString() -> {
                    Toast.makeText(context, "???????????? ?????? scope ID", Toast.LENGTH_SHORT).show()
                }
                error.toString() == AuthErrorCause.Misconfigured.toString() -> {
                    Toast.makeText(context, "????????? ???????????? ??????(android key hash)", Toast.LENGTH_SHORT).show()
                }
                error.toString() == AuthErrorCause.ServerError.toString() -> {
                    Toast.makeText(context, "?????? ?????? ??????", Toast.LENGTH_SHORT).show()
                }
                error.toString() == AuthErrorCause.Unauthorized.toString() -> {
                    Toast.makeText(context, "?????? ?????? ????????? ??????", Toast.LENGTH_SHORT).show()
                }
                else -> { // Unknown
                    Toast.makeText(context, "?????? ??????", Toast.LENGTH_SHORT).show()
                }
            }
        }
        else if (token != null) {
            Toast.makeText(context, "???????????? ?????????????????????.", Toast.LENGTH_SHORT).show()
            (context as OneMovieActivity).finish()
            context.startActivity((context as OneMovieActivity).intent)
        }
    }
}