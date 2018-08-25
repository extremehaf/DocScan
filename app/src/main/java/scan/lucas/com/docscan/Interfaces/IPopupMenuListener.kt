package scan.lucas.com.docscan.Interfaces

import android.view.MenuItem

interface IPopupMenuListener {
    fun onPopupMenuClicked(menuItem: MenuItem, adapterPosition: Int)
}