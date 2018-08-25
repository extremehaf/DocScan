package scan.lucas.com.docscan.DAL

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

@RealmClass
open class DocumentoRealm : RealmObject() {
    @PrimaryKey
    var docid: Int = 0
    var awskey: String? = null
    var name: String? = null
    var data_modificacao: Date? = null
    var tamanho: Long = 0
    var paginas: Int = 0
    var thumbnail: ByteArray? = null

}