package com.ohdroid.zbmaster.login.data

import android.content.Context
import cn.bmob.v3.BmobUser
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.LogInListener
import com.ohdroid.zbmaster.application.di.exannotation.ForApplication
import com.ohdroid.zbmaster.login.data.bmob.BmobLoginManager
import com.ohdroid.zbmaster.login.model.AccountInfo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by ohdroid on 2016/3/14.
 */
class LoginManager @Inject constructor(@ForApplication val context: Context):AccountManager {

    var accountManager: AccountManager? = null

    init {
        println("init Login manager")
        accountManager = BmobLoginManager(context)
    }

//    @Override
//    fun login(userName: String, userPassword: String, logInListener: LoginListener) {
//
//        accountManager?.login(userName, userPassword, logInListener)
//
//    }
//
//    fun regist(accountInfo: AccountInfo, registerListener: LoginListener) {
//        println("bmob register")
//        accountManager?.regist(accountInfo, registerListener)
//    }

    override fun login(userName: String, userPassword: String, loginListener: LoginListener) {
        accountManager?.login(userName, userPassword, loginListener)
    }

    override fun regist(accountInfo: AccountInfo, registerListener: LoginListener) {
        accountManager?.regist(accountInfo, registerListener)
    }

    override fun getUserAccount(): AccountInfo? {
        return null
    }


    interface LoginListener {
        fun onSuccess()
        fun onFailed(msg: String)
    }

}