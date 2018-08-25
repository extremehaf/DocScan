package scan.lucas.com.docscan.App

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration


class MyApp : Application() {


    private val dbNome = "document.realm"

    override fun onCreate() {
        super.onCreate()

        //Config Realm for the application
        Realm.init(this)
        val realmConfiguration = RealmConfiguration.Builder()
                .name(dbNome)
                .build()

        Realm.setDefaultConfiguration(realmConfiguration)

    }

    class popuparDB : Realm.Transaction {
        override fun execute(realm: Realm?) {

        }
    }
}