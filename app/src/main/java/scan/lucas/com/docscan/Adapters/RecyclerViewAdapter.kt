package scan.lucas.com.docscan.Adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import scan.lucas.com.docscan.R
import scan.lucas.com.docscan.Utils.Utils
import java.io.File

class RecyclerViewAdapter(private val list: List<String>) : RecyclerView.Adapter<RecyclerViewAdapter.Fotos>() {

    var mContext: Context? = null

    inner class Fotos(view: View) : RecyclerView.ViewHolder(view) {

        var imageView: ImageView? = null

        init {

            imageView = view.findViewById(R.id.imgAparelho) as ImageView

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Fotos {

        mContext = parent.context
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.fotos_list, parent, false)

        return Fotos(itemView)
    }

    override fun onBindViewHolder(holder: Fotos, position: Int) {

        var bmp = BitmapFactory.decodeFile(File(list.get(position)).absolutePath)
        val ratio = bmp.width.toDouble() / bmp.height.toDouble()

        if (bmp.width > bmp.height) {
            val new_width = Utils.DpsToPixel(300, mContext as Context)

            var newHeight = Math.round(new_width.toDouble() / ratio).toInt()
            //var newHeight = ((new_width)/(Math.sqrt((Math.pow(ratio, 2.0)+1)))).toInt();

            var imageBitmap = ThumbnailUtils.extractThumbnail(bmp
                    , new_width, newHeight)
            //holder.imageView!!.layoutParams.width = new_width
            //holder.imageView!!.layoutParams.height = newHeight
            holder.imageView!!.setImageBitmap(imageBitmap)
        } else {
            val newHeight = Utils.DpsToPixel(300, mContext as Context)
            var new_width = Math.round(newHeight.toDouble() * ratio).toInt()
            //var new_width = ((newHeight)/(Math.sqrt((1)/(Math.pow(ratio, 2.0)+1)))).toInt();

            var imageBitmap = ThumbnailUtils.extractThumbnail(bmp
                    , new_width, newHeight)
            //holder.imageView!!.layoutParams.width = new_width
            //holder.imageView!!.layoutParams.height = newHeight
            holder.imageView!!.setImageBitmap(imageBitmap)
        }
        //holder.imageView!!.requestLayout()

        //holder.itemView!!.computeScroll()
        // holder.itemView!!.invalidate()
    }

    override fun getItemCount(): Int {
        return list.size
    }

}

