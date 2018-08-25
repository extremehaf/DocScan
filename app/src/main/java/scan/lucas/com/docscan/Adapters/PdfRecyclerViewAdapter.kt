package scan.lucas.com.docscan.Adapters

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import kotlinx.android.synthetic.main.item_recycler_pdf_row.view.*
import scan.lucas.com.docscan.Enum.ViewStyleEnum
import scan.lucas.com.docscan.Interfaces.ILoadMoreListener
import scan.lucas.com.docscan.R
import scan.lucas.com.docscan.models.Documento
import java.text.DateFormat


class PdfRecyclerViewAdapter(recyclerView: RecyclerView,
                             viewStyle: ViewStyleEnum,
                             internal var documentos: MutableList<Documento?>,
                             var activity: Activity)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class LoadingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var progressBar: ProgressBar

        init {
            progressBar = view.findViewById<View>(R.id.progressBar1) as ProgressBar
        }
    }

    inner class DocumentosViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var txtTitulo: TextView? = null
        var txtDataCriacao: TextView? = null
        var txtPaginas: TextView? = null
        var txtTamanho: TextView? = null
        var preview: ImageView? = null

        init {

            preview = view.preview_foto
            txtTitulo = view.txt_titulo
            txtDataCriacao = view.txt_data
            txtPaginas = view.txt_paginas
            txtTamanho = view.txt_tamanho

        }
    }

    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1

    private var onLoadMoreListener: ILoadMoreListener? = null
    private var isLoading: Boolean = false
    private val visibleThreshold = 5
    private var lastVisibleItem: Int = 0
    var totalItemCount: Int = 0

    var lastPositions: IntArray? = null


    var viewStyle = ViewStyleEnum.LIST

    init {
        this.viewStyle = viewStyle

        if (viewStyle == ViewStyleEnum.LIST) {

            val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    totalItemCount = linearLayoutManager.itemCount
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()
                    if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        if (onLoadMoreListener != null)
                            onLoadMoreListener!!.onLoadMore()
                        isLoading = true
                    }
                }
            })
        } else {
            val gridLayoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    totalItemCount = gridLayoutManager.itemCount
                    if (lastPositions == null)
                        lastPositions = IntArray(gridLayoutManager.spanCount)

                    lastPositions = gridLayoutManager.findLastCompletelyVisibleItemPositions(lastPositions as IntArray)
                    lastVisibleItem = Math.max(lastPositions!![0], lastPositions!![1])//findMax(lastPositions);

                    if (!isLoading && totalItemCount <= lastVisibleItem + visibleThreshold) {
                        onLoadMoreListener?.onLoadMore()
                        isLoading = true
                    }
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == VIEW_TYPE_ITEM) {

            if (viewStyle == ViewStyleEnum.LIST) {
                val view = LayoutInflater.from(activity)
                        .inflate(R.layout.item_recycler_pdf_row, parent, false)
                return DocumentosViewHolder(view)
            } else {
                val view = LayoutInflater.from(activity).inflate(R.layout.item_recycler_pdf_grid, parent, false)
                return DocumentosViewHolder(view)
            }
        } else {
            val view = LayoutInflater.from(activity).inflate(R.layout.loading, parent, false)
            return LoadingViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is DocumentosViewHolder) {
            val doc = documentos[position]

            holder.txtTitulo!!.text = doc?.Nome
            holder.txtDataCriacao!!.text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(doc?.DataCriacao)
            holder.txtPaginas!!.text = doc?.Paginas.toString()
            if (doc?.Thumbnail != null)
                holder.preview!!.setImageBitmap(doc.Thumbnail)
            else
                holder.preview!!.setImageResource(R.drawable.ic_pdf)

        } else if (holder is LoadingViewHolder) {
            holder.progressBar.isIndeterminate = true
        }
    }

    override fun getItemCount(): Int {
        return if (documentos == null) 0 else documentos.size
    }

    fun setLoaded() {
        isLoading = false
    }

    fun setOnLoadMoreListener(mOnLoadMoreListener: ILoadMoreListener) {
        this.onLoadMoreListener = mOnLoadMoreListener
    }

    override fun getItemViewType(position: Int): Int {
        return if (documentos[position] == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }


    fun setLoading() {
        isLoading = true
    }


}

