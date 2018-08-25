package scan.lucas.com.docscan

//import com.android.camera.crop.CropActivity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfPage
import com.itextpdf.text.pdf.PdfPageEventHelper
import com.itextpdf.text.pdf.PdfWriter
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_edicao.*
import kotlinx.android.synthetic.main.content_edicao.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import scan.lucas.com.docscan.Adapters.RecyclerViewAdapter
import java.io.File
import java.io.FileOutputStream
import java.text.DateFormat
import java.util.*


class EdicaoActivity : AppCompatActivity() {

    private var mDialog: ProgressDialog? = null
    val dirs = ArrayList<String>()
    var RecyclerViewLayoutManager: RecyclerView.LayoutManager? = null
    var RecyclerViewHorizontalAdapter: RecyclerViewAdapter? = null
    var HorizontalLayout: LinearLayoutManager? = null
    var ChildView: View? = null
    var recyclerViewItemPosition: Int = 0
    var fileName: String = ""
    var fileUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edicao)
        setSupportActionBar(toolbar)

//        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
//        }


        RecyclerViewLayoutManager = LinearLayoutManager(applicationContext)

        recyclerview1!!.layoutManager = RecyclerViewLayoutManager


        RecyclerViewHorizontalAdapter = RecyclerViewAdapter(AddDiretorios())

        txtPagina.text = "Pagina 1/" + dirs.size
        HorizontalLayout = LinearLayoutManager(this@EdicaoActivity, LinearLayoutManager.HORIZONTAL, false)
        recyclerview1!!.layoutManager = HorizontalLayout

        recyclerview1!!.adapter = RecyclerViewHorizontalAdapter

        recyclerview1!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, state: Int) {
                if (state === RecyclerView.SCROLL_STATE_IDLE) {
                    val position = (recyclerview1!!.layoutManager as LinearLayoutManager)
                            .findFirstCompletelyVisibleItemPosition()
                    recyclerViewItemPosition = position
                    this@EdicaoActivity.runOnUiThread({
                        txtPagina!!.text = "Pagina " + (position + 1) + "/" + dirs.size
                    })
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

            }
        })
        recyclerview1!!.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {

            var gestureDetector = GestureDetector(this@EdicaoActivity, object : GestureDetector.SimpleOnGestureListener() {

                override fun onSingleTapUp(motionEvent: MotionEvent): Boolean {

                    return true
                }

            })

            override fun onInterceptTouchEvent(Recyclerview: RecyclerView, motionEvent: MotionEvent): Boolean {

                ChildView = Recyclerview.findChildViewUnder(motionEvent.x, motionEvent.y)

                if (ChildView != null && gestureDetector.onTouchEvent(motionEvent)) {

                    //Getting clicked value.
                    recyclerViewItemPosition = Recyclerview.getChildAdapterPosition(ChildView)

                    // Showing clicked item value on screen using toast message.
                    Toast.makeText(this@EdicaoActivity, dirs.get(recyclerViewItemPosition), Toast.LENGTH_LONG).show()

                }

                return false
            }

            override fun onTouchEvent(Recyclerview: RecyclerView, motionEvent: MotionEvent) {

            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

            }
        })

        var data = Date()
        var dtFormat = DateFormat.getDateInstance(DateFormat.MEDIUM).format(data)
        titulo.text = "Digitalização $dtFormat"
        edit_titulo.setText("Digitalização $dtFormat")
        edit_titulo.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                btnEditar.visibility = View.VISIBLE
                titulo.visibility = View.VISIBLE

                edit_titulo.visibility = View.GONE
                cancelarEdicao.visibility = View.GONE
                titulo.text = edit_titulo.text
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(edit_titulo.windowToken, 0)
                true
            } else {
                btnEditar.visibility = View.VISIBLE
                titulo.visibility = View.VISIBLE

                edit_titulo.visibility = View.GONE
                cancelarEdicao.visibility = View.GONE

                edit_titulo.setText(titulo.text)
                false
            }
        }
        btnEditar.setOnClickListener { view ->
            btnEditar.visibility = View.GONE
            titulo.visibility = View.GONE

            edit_titulo.visibility = View.VISIBLE
            cancelarEdicao.visibility = View.VISIBLE
        }
        cancelarEdicao.setOnClickListener { view ->
            btnEditar.visibility = View.VISIBLE
            titulo.visibility = View.VISIBLE

            edit_titulo.visibility = View.GONE
            cancelarEdicao.visibility = View.GONE

            edit_titulo.setText(titulo.text)
        }
        btnPdf.setOnClickListener { view ->

            showProgress(true, "Aguarde....", "Criando PDF")
            if (CriarPdf(titulo.text.toString())) {
                AlertDialog.Builder(this@EdicaoActivity)
                        .setMessage("Seu PDF sera enviado para o AWS S3")
                        .setTitle("PDF Criado")
                        .setCancelable(false)
                        .setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->
                            if (fileName.isNullOrEmpty())
                                fileName = "Digitalização ${(DateFormat.getDateInstance(DateFormat.FULL).format(data))}.pdf"
                            val intent = Intent(this@EdicaoActivity, MainActivity::class.java)
                            intent.putExtra("uploadFile", true)
                            intent.putExtra("fileName", fileName)
                            intent.data = fileUri
                            startActivity(intent)
                        })
                        .create()
                        .show()


            }
        }

        imgCrop.setOnClickListener { view ->
            CropImage()
        }
        imgEfeitos.setOnClickListener { view ->
            var intent = Intent(this@EdicaoActivity, FitrosActivity::class.java)

            intent.putExtra("fotoPath", dirs.get(recyclerViewItemPosition))
            startActivityForResult(intent, FILTRO_REQUEST)
        }
        deletarPg.setOnClickListener {
            DeletePage()
        }
        btnSair.setOnClickListener({
            AlertDialog.Builder(this@EdicaoActivity)
                    .setMessage("Deseja cancelar a digitalização?")
                    .setTitle("Sair")
                    .setCancelable(true)
                    .setNegativeButton("Não", DialogInterface.OnClickListener { dialog: DialogInterface, which: Int ->

                    })
                    .setPositiveButton("Sim", DialogInterface.OnClickListener { dialog, which ->
                        val intent = Intent(this@EdicaoActivity, MainActivity::class.java)
                        intent.putExtra("uploadFile", false)
                        startActivity(intent)
                    })
                    .create()
                    .show()
        })
    }

    fun DeletePage() {
        AlertDialog.Builder(this@EdicaoActivity)
                .setMessage("Deseja deletar a pagina?")
                .setTitle("Excluir pagina")
                .setCancelable(true)
                .setNegativeButton("Não", DialogInterface.OnClickListener { dialog: DialogInterface, which: Int ->

                })
                .setPositiveButton("Sim", DialogInterface.OnClickListener { dialog, which ->
                    val pos = recyclerViewItemPosition
                    File(dirs[pos]).delete()
                    File(dirs[pos].replace("processado", "original")).delete()
                    File(dirs[pos].replace("processado", "croped")).delete()
                    dirs.removeAt(pos)
                    RecyclerViewHorizontalAdapter?.notifyItemRemoved(pos)
                })
                .create()
                .show()
    }

    fun CropImage() {
        val caminho = dirs.get(recyclerViewItemPosition).replace("processado", "original")
        val file = File(caminho)
        if (File(caminho).exists()) {
            try {

                var intent = Intent(this@EdicaoActivity, CropActivity::class.java)

                intent.putExtra("fotoPath", caminho)
                intent.putExtra("destino", dirs.get(recyclerViewItemPosition))

                startActivityForResult(intent, CROP_REQUEST)

//                var caminhoUri = Uri.fromFile(file)
//                CropImage.activity(caminhoUri)
//                        .setMultiTouchEnabled(true)
//                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .start(this);
            } catch (e: ActivityNotFoundException) {
                val errorMessage = "Your device doesn't support the crop action!"
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {

                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        supportActionBar!!.hide()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            CROP_REQUEST -> {
                val caminho = dirs.get(recyclerViewItemPosition)
                val matDoc = Imgcodecs.imread(data.data.path)
                Imgproc.cvtColor(matDoc, matDoc, Imgproc.COLOR_RGBA2GRAY)
                Imgproc.adaptiveThreshold(matDoc, matDoc, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 15.0)
                Imgcodecs.imwrite(caminho, matDoc)
                recyclerview1!!.adapter.notifyDataSetChanged()
            }
            FILTRO_REQUEST -> {
                recyclerview1!!.adapter.notifyDataSetChanged()
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == RESULT_OK) {
                    val resultUri = result.uri

                    val caminho = dirs.get(recyclerViewItemPosition)
                    val matDoc = Imgcodecs.imread(resultUri.path)
                    Imgproc.cvtColor(matDoc, matDoc, Imgproc.COLOR_RGBA2GRAY)
                    Imgproc.adaptiveThreshold(matDoc, matDoc, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 15.0)
                    Imgcodecs.imwrite(caminho, matDoc)
                    recyclerview1!!.adapter.notifyDataSetChanged()
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                }


            }
        }

    }

    // function to add items in RecyclerView.
    fun AddDiretorios(): List<String> {

        val file = File(Processamento.caminhoPadrao)
        if (file.isDirectory) {
            var children = file.list().toMutableList()
            for (i in 0 until children.size) {
                if (children[i].contains("processado", true)) {
                    dirs.add(Processamento.caminhoPadrao + "/" + children[i])
                }
            }
        }

        return dirs

    }

    inner class RotateEvent : PdfPageEventHelper() {
        override fun onStartPage(writer: PdfWriter?, document: Document?) {
            writer!!.addPageDictEntry(PdfName.ROTATE, PdfPage.SEASCAPE)
        }
    }

    fun CriarPdf(nome: String): Boolean {
        try {
            val docsFolder = File(Environment.getExternalStorageDirectory().absolutePath + "/Documents")
            if (!docsFolder.exists()) {
                docsFolder.mkdir()
                Log.i(TAG, "Created a new directory for PDF")
            }

            var pdfFile = File(docsFolder.absolutePath, "$nome.pdf")
            pdfFile.createNewFile()
            val output = FileOutputStream(pdfFile)
            val document = Document()
            document.pageSize = PageSize.A4
            var writer = PdfWriter.getInstance(document, output)
            document.open()
            for (dir in dirs) {
                if (File(dir).exists()) {
                    var image = Image.getInstance(dir)
                    if (image.width > image.height) {
                        writer.pageEvent = RotateEvent()
                        image.scaleToFit(PageSize.A4.rotate())
                        document.add(image)
                        writer.pageEvent = RotateEvent()
                    } else {
                        image.scaleToFit(PageSize.A4)
                        document.add(image)
                    }
                }
            }
            document.addCreationDate()
            document.addCreator("DocScan")
            document.close()

            fileUri = Uri.fromFile(pdfFile)
            fileName = pdfFile.name

            Toast.makeText(this@EdicaoActivity, "Pdf criado com sucesso", Toast.LENGTH_SHORT).show()
            return true
        } catch (e: Exception) {
            Toast.makeText(this@EdicaoActivity, "Erro ao criar o pdf ${e.message}", Toast.LENGTH_LONG).show()
            return false
        } finally {
            showProgress(false)
        }

    }

    private fun showProgress(show: Boolean) {
        showProgress(show, "", "")
    }

    private fun showProgress(show: Boolean?, title: String, mensagem: String) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (show!!)
            mDialog = ProgressDialog.show(this, title, mensagem, true)
        else
            mDialog!!.dismiss()
    }

    companion object {
        val FILTRO_REQUEST = 0
        val CROP_REQUEST = 1
        private val TAG = "EdicaoActivity"

    }

}
