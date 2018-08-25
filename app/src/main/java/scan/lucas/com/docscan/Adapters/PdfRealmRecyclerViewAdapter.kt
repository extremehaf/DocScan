package scan.lucas.com.docscan.Adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import io.realm.*
import kotlinx.android.synthetic.main.item_recycler_pdf_row.view.*
import scan.lucas.com.docscan.DAL.DocumentoRealm
import scan.lucas.com.docscan.Enum.OrderBy
import scan.lucas.com.docscan.Enum.ViewStyleEnum
import scan.lucas.com.docscan.Interfaces.IPopupMenuListener
import scan.lucas.com.docscan.R
import scan.lucas.com.docscan.Utils.ToBitmap
import java.text.DateFormat


class PdfRealmRecyclerViewAdapter(recyclerView: RecyclerView, viewStyle: ViewStyleEnum, realm: Realm, data: OrderedRealmCollection<DocumentoRealm>, var onPopupMenuClicked: IPopupMenuListener)
    : RealmRecyclerViewAdapter<DocumentoRealm, PdfRealmRecyclerViewAdapter.DocumentosViewHolder>(data, true), Filterable {

    var realm: Realm
    var viewStyle = ViewStyleEnum.LIST

    init {
        this.viewStyle = viewStyle
        this.realm = realm
        setHasStableIds(true)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentosViewHolder {
        if (viewStyle == ViewStyleEnum.LIST) {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_recycler_pdf_row, parent, false)
            return DocumentosViewHolder(view, onPopupMenuClicked)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recycler_pdf_grid, parent, false)
            return DocumentosViewHolder(view, onPopupMenuClicked)
        }
    }

    override fun onBindViewHolder(holder: DocumentosViewHolder, position: Int) {
        val obj = getItem(position)
        holder.data = obj
        val itemId = obj!!.docid

        holder.txtTitulo!!.text = obj.name
        holder.txtDataCriacao!!.text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(obj.data_modificacao)
        if (viewStyle == ViewStyleEnum.GRID)
            holder.txtPaginas!!.text = "Páginas: ${obj.paginas}"
        else
            holder.txtPaginas!!.text = obj.paginas.toString()
        val nbytes = obj.tamanho
        val tamanhoKb = Math.floor(Math.log(nbytes.toDouble()) / Math.log(1024.00))

        holder.txtTamanho!!.text = "${Math.round(nbytes / Math.pow(1024.00, tamanhoKb))} KB"
        if (obj.thumbnail != null)
            holder.preview!!.setImageBitmap(obj.thumbnail?.ToBitmap())
        else
            holder.preview!!.setImageResource(R.drawable.ic_pdf)
    }

    override fun getItemId(index: Int): Long {

        return getItem(index)!!.docid.toLong()
    }

    fun OrderByData(order: OrderBy? = null) {

        val query = this.realm.where(DocumentoRealm::class.java)
        if (order != null) {
            if (order == OrderBy.DataDesc)
                query.sort("data_modificacao", Sort.DESCENDING)
            else
                query.sort("data_modificacao", Sort.ASCENDING)
        } else {
            query.sort("data_modificacao")
        }

        updateData(query.findAllAsync())
    }

    fun OrderByNome(order: OrderBy? = null) {

        val query = this.realm.where(DocumentoRealm::class.java)
        if (order != null) {
            if (order == OrderBy.NomeDesc)
                query.sort("name", Sort.DESCENDING)
            else
                query.sort("name", Sort.ASCENDING)
        } else {
            query.sort("name")
        }
        updateData(query.findAllAsync())
    }

    fun filterResults(text: String) {

        val query = this.realm.where(DocumentoRealm::class.java)
        if (!(text == null || "".equals(text))) {
            query.contains("name", text, Case.INSENSITIVE)
        }
        updateData(query.findAllAsync())
    }

    override fun getFilter(): Filter {
        val filter = DocumentosFilter(this)
        return filter
    }

    inner class DocumentosFilter(adapter: PdfRealmRecyclerViewAdapter) : Filter() {
        private var adapter: PdfRealmRecyclerViewAdapter? = null

        init {
            this.adapter = adapter
        }

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            return FilterResults()
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            adapter?.filterResults(constraint.toString())
        }

    }

    inner class DocumentosViewHolder(view: View, var popupMenuListener: IPopupMenuListener? = null) : RecyclerView.ViewHolder(view), PopupMenu.OnMenuItemClickListener {

        var txtTitulo: TextView? = null
        var txtDataCriacao: TextView? = null
        var txtPaginas: TextView? = null
        var txtTamanho: TextView? = null
        var preview: ImageView? = null
        var data: DocumentoRealm? = null
        var contextMenu: ImageView? = null

        init {

            preview = view.preview_foto
            txtTitulo = view.txt_titulo
            txtDataCriacao = view.txt_data
            txtPaginas = view.txt_paginas
            txtTamanho = view.txt_tamanho
            contextMenu = view.ic_context
            if (contextMenu != null) {
                contextMenu!!.setOnClickListener {
                    showPopupMenu(it)
                }
            }

        }

        override fun onMenuItemClick(item: MenuItem?): Boolean {
            when (item!!.itemId) {
                R.id.menu_baixar -> {
                    // Call popup menu listener passing the menu item that was clicked as well as the recycler view item position:
                    popupMenuListener!!.onPopupMenuClicked(item, adapterPosition)
                    return true
                }
                R.id.menu_visualizar -> {
                    popupMenuListener!!.onPopupMenuClicked(item, adapterPosition)
                    return true
                }
                else -> return false
            }
        }

        private fun showPopupMenu(view: View) {
            val popup = PopupMenu(view.context, view)
            val inflater = popup.menuInflater
            inflater.inflate(R.menu.menu_pdf, popup.menu)
            popup.setOnMenuItemClickListener(this)
            popup.show()
        }

    }

}