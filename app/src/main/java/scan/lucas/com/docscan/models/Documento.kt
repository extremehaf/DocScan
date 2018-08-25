package scan.lucas.com.docscan.models

import android.graphics.Bitmap
import java.util.*

class Documento {
    var AWSKey: String? = null
    lateinit var Nome: String
    lateinit var DataCriacao: Date
    lateinit var DataModificacao: Date
    var Tamanho: Long = 0
    var Paginas: Int = 0
    var Thumbnail: Bitmap? = null

    constructor() {
        this.Nome = ""
        this.DataCriacao = Date()
        this.DataModificacao = Date()
        this.Paginas = 0
    }

    constructor(Nome: String, DataCriacao: Date, DataModificacao: Date, Paginas: Int) {
        this.Nome = Nome
        this.DataCriacao = DataCriacao
        this.DataModificacao = DataModificacao
        this.Paginas = Paginas
    }
}