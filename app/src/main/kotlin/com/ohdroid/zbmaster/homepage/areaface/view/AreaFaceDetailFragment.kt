package com.ohdroid.zbmaster.homepage.areaface.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.drawee.view.SimpleDraweeView
import com.ohdroid.zbmaster.MainActivity
import com.ohdroid.zbmaster.R
import com.ohdroid.zbmaster.application.BaseApplication
import com.ohdroid.zbmaster.application.data.api.QiniuApi
import com.ohdroid.zbmaster.base.view.BaseFragment
import com.tencent.connect.share.QQShare
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import org.jetbrains.anko.support.v4.find
import java.io.File
import java.net.URL

/**
 * Created by ohdroid on 2016/4/7.
 */
class AreaFaceDetailFragment : BaseFragment(), View.OnClickListener {


    val ivFaceDetail: SimpleDraweeView by lazy { find<SimpleDraweeView>(R.id.iv_face_detail) }

    val bgLayout: View by lazy { find<View>(R.id.fragment_face_detail) }
    val btnShare: Button by lazy { find<Button>(R.id.btn_share) }

    companion object {
        fun launch(manager: FragmentManager, containerId: Int, imageUrl: String) {
            val fragment: AreaFaceDetailFragment = AreaFaceDetailFragment()
            val args: Bundle = Bundle()
            args.putString("imageUrl", imageUrl)
            fragment.arguments = args

            manager.beginTransaction()
                    .replace(containerId, fragment)
                    .addToBackStack(null)//加入到回退栈
                    .commit()
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_area_face_detail, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val imageUrl = arguments.get("imageUrl") as String
        if (TextUtils.isEmpty(imageUrl)) {
            return
        }
        bgLayout.setOnClickListener { }//消耗点击事件

        //设置为自动播放
        val controller: DraweeController = Fresco.newDraweeControllerBuilder()
                .setUri(Uri.parse(imageUrl))
                .setTapToRetryEnabled(true)//点击重播
                .setAutoPlayAnimations(true)//自动播放
                .build()
        ivFaceDetail.controller = controller
        //        ivFaceDetail.setImageURI(Uri.parse(imageUrl), null)

        btnShare.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_share -> shareImage()
        }
    }

    fun shareImage() {
        //tencent 分享
        val params: Bundle = Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        params.putString(QQShare.SHARE_TO_QQ_TITLE,"让你开车？")
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, arguments.getString("imageUrl"));
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, QiniuApi.LOGO_IMAGE_URL);
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, "ZBMaster");

        val application: BaseApplication = activity.application as BaseApplication
        val mTencent: Tencent = application.applicationComponent.tencentManager()
        mTencent.shareToQQ(activity, params, object : IUiListener {
            override fun onComplete(p0: Any?) {
                println(p0)
            }

            override fun onCancel() {
            }

            override fun onError(p0: UiError?) {
                println(p0?.errorMessage)
            }

        });
    }

}