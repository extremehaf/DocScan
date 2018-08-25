package scan.lucas.com.docscan.DAL

import android.content.Context
import io.realm.Realm
import io.realm.RealmConfiguration
import scan.lucas.com.docscan.models.Documento

class DbAcess(val context: Context) {
    var config: RealmConfiguration
    var realm: Realm

    init {
        Realm.init(context)
        config = RealmConfiguration.Builder()
                .name("document.realm")
                .build()

        realm = Realm.getInstance(config)
    }

    fun CriarDocumento(doc: Documento) {
        realm.beginTransaction()
        var docRealm = realm.createObject(DocumentoRealm::class.java, ObterUltimoId())
        docRealm.awskey = doc.AWSKey
        docRealm.data_modificacao = doc.DataModificacao
        docRealm.name = doc.Nome
        docRealm.paginas = doc.Paginas
        docRealm.tamanho = doc.Tamanho
        //docRealm.thumbnail = doc.Thumbnail?
        realm.commitTransaction()
    }

    fun Listar(): List<DocumentoRealm> {
        return realm.where(DocumentoRealm::class.java).findAll().toList()
    }

    fun Listar(start: Int): List<DocumentoRealm> {
        return realm.where(DocumentoRealm::class.java).findAll().toList()
    }

    fun ObterUltimoId(): Int {
        val maxId = realm.where(DocumentoRealm::class.java).max("docid")?.toInt()
        if (maxId == null)
            return 0
        else
            return maxId
    }
}