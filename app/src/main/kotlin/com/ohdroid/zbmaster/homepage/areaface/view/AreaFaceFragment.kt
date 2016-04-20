package com.ohdroid.zbmaster.homepage.areaface.view

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.ohdroid.zbmaster.R
import com.ohdroid.zbmaster.application.view.RecycleViewHeaderFooterAdapter
import com.ohdroid.zbmaster.application.view.RecycleViewLoadMoreListener
import com.ohdroid.zbmaster.base.view.BaseFragment
import com.ohdroid.zbmaster.homepage.areaface.model.FaceInfo
import com.ohdroid.zbmaster.homepage.areaface.presenter.AreaFacePresenter
import org.jetbrains.anko.support.v4.find

/**
 * Created by ohdroid on 2016/4/4.
 */
class AreaFaceFragment : BaseFragment(), AreaFaceView {

    val faceList: RecyclerView by lazy { find<RecyclerView>(R.id.rv_face) }
    val freshLayout: SwipeRefreshLayout by lazy { find<SwipeRefreshLayout>(R.id.refresh_layout) }
    val loadingView: View by lazy { find<View>(R.id.loading_view) }
    var faceListAdapter: FaceRecycleViewAdapter? = null
    var faceListAdapterWrap: RecycleViewHeaderFooterAdapter<FaceViewHolder>? = null

    lateinit var presenter: AreaFacePresenter


    var mRecycleViewFootView: TextView? = null

    companion object {
        val TAG: String = "AreaFaceFragment"

        fun launch(manager: FragmentManager, containerId: Int): Fragment {
            println("launch $TAG")

            var fragment: AreaFaceFragment? = null

            if (null == manager.findFragmentByTag(TAG)) {
                fragment = AreaFaceFragment()
                manager.beginTransaction()
                        .add(containerId, fragment, TAG)
                        .commit()
            } else {
                fragment = manager.findFragmentByTag(TAG) as AreaFaceFragment
                manager.beginTransaction()
                        .show(fragment)
                        .commit()
            }

            return fragment
        }
    }

    var footTextView: TextView? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        presenter = component.faceAreaPresenter();
        presenter.attachView(this)
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_area_face, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {

        println("on face fragment view created...........")

        presenter.loadFaceList()//开始请求数据

        //下拉刷新初始化
        freshLayout.setOnRefreshListener { presenter.loadFaceList() }
        freshLayout.setColorSchemeColors(Color.parseColor("#FF9966"), Color.parseColor("#FF6666"), Color.parseColor("#FFCCCC"))


        faceList.addOnScrollListener(loadMoreListener)

        faceList.layoutManager = LinearLayoutManager(context)
        if (null == faceListAdapter) {
            faceListAdapter = FaceRecycleViewAdapter(arrayListOf())
            faceListAdapter!!.listener = object : OnRecycleViewItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    //跳转到表情详情页面
                    presenter.showFaceInfoDetail(position)
                }
            }
        }

        faceListAdapterWrap = RecycleViewHeaderFooterAdapter<FaceViewHolder>(faceListAdapter)
        faceList.adapter = faceListAdapterWrap

    }

    /**
     * 设置footview提示语句
     */
    fun setFootTextViewHint(str: String) {
        println("======$str====$footTextView")

        if (footTextView == null) {

            println("=====================add=================")
            footTextView = TextView(context)
            footTextView!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            footTextView!!.gravity = Gravity.CENTER
            val padding = context.resources.getDimensionPixelSize(R.dimen.padding_8dp)
            footTextView!!.setPadding(0, 0, 0, padding)
            footTextView!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            faceListAdapterWrap?.addFootView(footTextView);

        }
        footTextView!!.text = str;
    }

    inner class FaceRecycleViewAdapter(var faceUrls: MutableList<FaceInfo>) : RecyclerView.Adapter<FaceViewHolder>() {
        var listener: OnRecycleViewItemClickListener? = null

        override fun onBindViewHolder(holder: FaceViewHolder?, position: Int) {
            if (listener != null) {
                holder?.listener = listener
            }
            holder?.setImageViewUrl(faceUrls[position].faceUrl)
            holder?.setImageDescription(faceUrls[position].faceTitle)
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): FaceViewHolder? {
            return FaceViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_area_face, parent, false))
        }

        override fun getItemCount(): Int {
            return faceUrls.size
        }

    }

    class FaceViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        var listener: OnRecycleViewItemClickListener? = null
            set(value) {
                itemView.setOnClickListener({ v -> value!!.onItemClick(v!!, adapterPosition) })
            }

        fun setImageViewUrl(imageUrl: String?) {
            if (null == imageUrl) {
                //TODO 若无地址，那么显示数据异常图标
                return
            }

            val imageView: SimpleDraweeView = itemView.findViewById(R.id.item_image) as SimpleDraweeView
            imageView.setImageURI(Uri.parse(imageUrl), null)//内部还是通过controllerbuidler控制的
        }

        fun setImageDescription(descrition: String?) {
            (itemView.findViewById(R.id.item_content) as TextView).text = descrition
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }


    //=======================================对presenter暴露的接口==============================================

    override fun showErrorView(errorState: Int, errorMessage: String) {
    }

    override fun showFaceInfoDetail(faceInfo: FaceInfo) {
        AreaFaceDetailActivity.launch(context, faceInfo)
    }

    /**
     * 初始化显示表情数据
     */
    override fun showFaceList(faces: MutableList<FaceInfo>, hasMore: Boolean) {

        println("show face info data")

        if ( freshLayout.isRefreshing) {
            freshLayout.isRefreshing = false
        }

        if (faces.size == 0) {
            showEmpty()
            return
        }

        if (loadingView.visibility == View.VISIBLE) {
            val animator: ObjectAnimator = ObjectAnimator.ofFloat(loadingView, "alpha", 1f, 0f)
            animator.duration = 250
            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    loadingView.visibility = View.INVISIBLE
                }

                override fun onAnimationCancel(animation: Animator?) {
                    loadingView.visibility = View.INVISIBLE
                }

                override fun onAnimationStart(animation: Animator?) {
                }
            })
            animator.start()
        }


        faceListAdapter?.faceUrls = faces
        faceListAdapter?.notifyDataSetChanged()

        loadMoreListener.canLoadingMore = hasMore
    }


    val loadMoreListener = object : RecycleViewLoadMoreListener() {
        override fun onLoadMoreData() {
            //loading more data
            setFootTextViewHint(context.resources.getString(R.string.hint_load_more))
            presenter.loadMoreFaceInfo()
        }
    }

    override fun showMoreFaceInfo(hasMore: Boolean) {
        faceListAdapter?.notifyDataSetChanged()
        loadMoreListener.canLoadingMore = hasMore//设置是否可以加载更多
        loadMoreListener.isLoadingMore = false//加载数据完毕
        if (!hasMore) {
            setFootTextViewHint(context.resources.getString(R.string.hint_no_more_data))
        }
    }

    override fun showEmpty() {
        //        faceListAdapterWrap?.setDataState(RecycleViewHeaderFooterAdapter.STATE_NO_DATA, context)
        //        println(".........show empty foot..............")
        //        val emptyView: TextView = TextView(context)
        //        emptyView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        //        emptyView.setTextColor(R.color.material_grey_100)
        //        emptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        //        emptyView.setText(R.string.hint_empty_data)
        //        emptyView.gravity = Gravity.CENTER
        //        faceListAdapterWrap?.addFootView(emptyView)
    }

}