package scan.lucas.com.docscan.models

import scan.lucas.com.docscan.Enum.TipoProcessamento

class ImageMessage(tipo: TipoProcessamento, obj: Any, w: Int, h: Int) {

    var Tipo: TipoProcessamento? = null
    var Obj: Any? = null
    var width: Int = 0
    var height: Int = 0

    init {
        Obj = obj
        Tipo = tipo
        width = w
        height = h
    }
}
