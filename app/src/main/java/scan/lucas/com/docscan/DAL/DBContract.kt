package scan.lucas.com.docscan.DAL

import android.provider.BaseColumns

object DBContract {
    /* Inner class that defines the table contents */
    class DocumentEntry : BaseColumns {
        companion object {
            val TABLE_NAME = "documents"
            val COLUMN_DOCUMENT_ID = "docid"
            val COLUMN_AWSKEY = "awskey"
            val COLUMN_NAME = "name"
            val COLUMN_DATA_MODIFICACAO = "data_modificacao"
            val COLUMN_TAMANHO = "tamanho"
            val COLUMN_PAGINAS = "paginas"
        }
    }
}