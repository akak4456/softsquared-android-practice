package com.example.movieapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieapp.databinding.ActivityWatchlistBinding
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthErrorCause
import com.kakao.sdk.user.UserApiClient

class WatchlistActivity : AppCompatActivity() {
    private lateinit var binding:ActivityWatchlistBinding
    private lateinit var context: Context
    private lateinit var adapter:WatchlistAdapter
    private lateinit var watchlistDao:WatchlistDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWatchlistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        accessDatabase()
        context = this

        binding.bottomAppbar.watchlistBtnOffImg.visibility = View.INVISIBLE
        binding.bottomAppbar.watchlistTv.setTextColor(Color.WHITE)

        binding.bottomAppbar.homeLayout.setOnClickListener{
            val intent = Intent(context,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.loginBtn.setOnClickListener{
            if(UserApiClient.instance.isKakaoTalkLoginAvailable(this)){
                UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)
            }else{
                UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
            }
        }

        UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
            if (error != null) {
                binding.rv.visibility = View.INVISIBLE
                binding.loginLayout.visibility = View.VISIBLE
            }
            else if (tokenInfo != null) {
                binding.rv.visibility = View.VISIBLE
                binding.loginLayout.visibility = View.INVISIBLE
                adapter = WatchlistAdapter(context,watchlistDao.getWatchlistById(tokenInfo.id).toCollection(ArrayList()),watchlistDao)
                binding.rv.layoutManager = LinearLayoutManager(context)
                binding.rv.adapter = adapter
            }
        }
    }

    val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            when {
                error.toString() == AuthErrorCause.AccessDenied.toString() -> {
                    Toast.makeText(this, "????????? ?????? ???(?????? ??????)", Toast.LENGTH_SHORT).show()
                }
                error.toString() == AuthErrorCause.InvalidClient.toString() -> {
                    Toast.makeText(this, "???????????? ?????? ???", Toast.LENGTH_SHORT).show()
                }
                error.toString() == AuthErrorCause.InvalidGrant.toString() -> {
                    Toast.makeText(this, "?????? ????????? ???????????? ?????? ????????? ??? ?????? ??????", Toast.LENGTH_SHORT).show()
                }
                error.toString() == AuthErrorCause.InvalidRequest.toString() -> {
                    Toast.makeText(this, "?????? ???????????? ??????", Toast.LENGTH_SHORT).show()
                }
                error.toString() == AuthErrorCause.InvalidScope.toString() -> {
                    Toast.makeText(this, "???????????? ?????? scope ID", Toast.LENGTH_SHORT).show()
                }
                error.toString() == AuthErrorCause.Misconfigured.toString() -> {
                    Toast.makeText(this, "????????? ???????????? ??????(android key hash)", Toast.LENGTH_SHORT).show()
                }
                error.toString() == AuthErrorCause.ServerError.toString() -> {
                    Toast.makeText(this, "?????? ?????? ??????", Toast.LENGTH_SHORT).show()
                }
                error.toString() == AuthErrorCause.Unauthorized.toString() -> {
                    Toast.makeText(this, "?????? ?????? ????????? ??????", Toast.LENGTH_SHORT).show()
                }
                else -> { // Unknown
                    Toast.makeText(this, "?????? ??????", Toast.LENGTH_SHORT).show()
                }
            }
        }
        else if (token != null) {
            Toast.makeText(this, "???????????? ?????????????????????.", Toast.LENGTH_SHORT).show()
            finish()
            startActivity(intent)
        }
    }

    private fun accessDatabase(){
        val database = WatchlistDatabase.getInstance(this)!!
        watchlistDao = database.watchlistDao()
    }
}