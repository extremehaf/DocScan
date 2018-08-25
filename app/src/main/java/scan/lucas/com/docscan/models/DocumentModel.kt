package scan.lucas.com.docscan.models

import java.util.*

class DocumentModel(val docid: String,
                    val awskey: String,
                    val name: String,
                    val data_modificacao: Date,
                    val tamanho: Int,
                    val paginas: Int)
