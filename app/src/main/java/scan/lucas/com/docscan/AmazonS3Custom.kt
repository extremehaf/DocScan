package scan.lucas.com.docscan

//import com.amazonaws.services.s3.paginators.ListObjectsV2Iterable;
import android.content.Context
import android.util.Log
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.*
import com.amazonaws.util.IOUtils
import scan.lucas.com.docscan.Enum.OrderBy
import java.io.File


class AmazonS3Custom {
    private var PREFIX = "LMELO"
    private var BUCKET = "osas"
    private var KEY = "AKIAJOKSTNOS5AIEMKXQ"
    private var SECRET = "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"

    var s3Client: AmazonS3Client? = null
    private var credentials: BasicAWSCredentials? = null

    private val sCredProvider: CognitoCachingCredentialsProvider? = null
    private var sTransferUtility: TransferUtility? = null

    constructor(bucketName: String, key: String, secrect: String) {
        this.BUCKET = bucketName
        this.KEY = key
        this.SECRET = secrect
        credentials = BasicAWSCredentials(KEY, SECRET)
        s3Client = AmazonS3Client(credentials)
        s3Client!!.setRegion(Region.getRegion(Regions.SA_EAST_1))

    }

    constructor(s3Client: AmazonS3Client?, credentials: BasicAWSCredentials?) {
        this.s3Client = s3Client
        this.credentials = credentials
    }

    fun ListDir(): ArrayList<String> {
        val files = ArrayList<String>()
        var listing = ListObjectsRequest()
                .withBucketName(this.BUCKET).withPrefix(PREFIX)

        val objectListing = s3Client!!.listObjects(listing)

        for (objectSummary in objectListing.objectSummaries) {
            val key = objectSummary.key
            if (!key.endsWith("/"))
                files.add(key)
        }
        return files
    }

    fun ListDir(order: OrderBy): List<String> {
        val files = ArrayList<String>()
        var listing = ListObjectsRequest()
                .withBucketName(this.BUCKET).withPrefix(PREFIX)

        val objectListing = s3Client!!.listObjects(listing)
        when (order) {
            OrderBy.NomeAsc -> {
                objectListing.objectSummaries.sortBy { s3ObjectSummary: S3ObjectSummary? -> s3ObjectSummary?.key }
            }
            OrderBy.NomeDesc -> {
                objectListing.objectSummaries.sortByDescending { s3ObjectSummary: S3ObjectSummary? -> s3ObjectSummary?.key }
            }
            OrderBy.DataAsc -> {
                objectListing.objectSummaries.sortBy { s3ObjectSummary: S3ObjectSummary? -> s3ObjectSummary?.lastModified }
            }
            OrderBy.DataDesc -> {
                objectListing.objectSummaries.sortByDescending { s3ObjectSummary: S3ObjectSummary? -> s3ObjectSummary?.lastModified }
            }
        }

        return objectListing.objectSummaries
                .filter { obj ->
                    !obj.key.endsWith("/")
                }.map { it.key }
    }

    fun ListDir(order: OrderBy, limit: Int): List<String> {
        val files = ArrayList<String>()
        var listing = ListObjectsRequest()
                .withBucketName(this.BUCKET).withPrefix(PREFIX)

        val objectListing = s3Client!!.listObjects(listing)
        when (order) {
            OrderBy.NomeAsc -> {
                objectListing.objectSummaries.sortBy { s3ObjectSummary: S3ObjectSummary? -> s3ObjectSummary?.key }
            }
            OrderBy.NomeDesc -> {
                objectListing.objectSummaries.sortByDescending { s3ObjectSummary: S3ObjectSummary? -> s3ObjectSummary?.key }
            }
            OrderBy.DataAsc -> {
                objectListing.objectSummaries.sortBy { s3ObjectSummary: S3ObjectSummary? -> s3ObjectSummary?.lastModified }
            }
            OrderBy.DataDesc -> {
                objectListing.objectSummaries.sortByDescending { s3ObjectSummary: S3ObjectSummary? -> s3ObjectSummary?.lastModified }
            }
        }

        return objectListing.objectSummaries
                .filter { obj ->
                    !obj.key.endsWith("/")
                }.map { it.key }
    }

    fun GetListRequest(order: OrderBy, afterItem: String = ""): List<S3ObjectSummary> {
        // Build the list objects request
        val listReq = ListObjectsV2Request().withBucketName(BUCKET)
                .withPrefix(PREFIX)

        if (!afterItem.isNullOrEmpty())
            listReq.withStartAfter(afterItem)

        var result = s3Client!!.listObjectsV2(listReq)

        when (order) {
            OrderBy.NomeAsc -> {
                result.objectSummaries.sortBy { s3ObjectSummary: S3ObjectSummary? -> s3ObjectSummary?.key }
            }
            OrderBy.NomeDesc -> {
                result.objectSummaries.sortByDescending { s3ObjectSummary: S3ObjectSummary? -> s3ObjectSummary?.key }
            }
            OrderBy.DataAsc -> {
                result.objectSummaries.sortBy { s3ObjectSummary: S3ObjectSummary? -> s3ObjectSummary?.lastModified }
            }
            OrderBy.DataDesc -> {
                result.objectSummaries.sortByDescending { s3ObjectSummary: S3ObjectSummary? -> s3ObjectSummary?.lastModified }
            }
        }

        return result.objectSummaries
                .filter { obj ->
                    !obj.key.endsWith("/")
                }
    }

    fun GetListRequest(maxitens: Int, order: OrderBy, afterItem: String = ""): List<S3ObjectSummary> {
        // Build the list objects request
        val listReq = ListObjectsV2Request().withBucketName(BUCKET)
                .withPrefix(PREFIX)
                .withMaxKeys(maxitens)

        if (!afterItem.isNullOrEmpty())
            listReq.withStartAfter(afterItem)

        var result = s3Client!!.listObjectsV2(listReq)

        when (order) {
            OrderBy.NomeAsc -> {
                result.objectSummaries.sortBy { s3ObjectSummary: S3ObjectSummary? -> s3ObjectSummary?.key }
            }
            OrderBy.NomeDesc -> {
                result.objectSummaries.sortByDescending { s3ObjectSummary: S3ObjectSummary? -> s3ObjectSummary?.key }
            }
            OrderBy.DataAsc -> {
                result.objectSummaries.sortBy { s3ObjectSummary: S3ObjectSummary? -> s3ObjectSummary?.lastModified }
            }
            OrderBy.DataDesc -> {
                result.objectSummaries.sortByDescending { s3ObjectSummary: S3ObjectSummary? -> s3ObjectSummary?.lastModified }
            }
        }

        return result.objectSummaries
                .filter { obj ->
                    !obj.key.endsWith("/")
                }
    }

    fun GetObject(key: String): S3Object? {
        //val getrequest = GetObjectRequest(BUCKET,key.toString())
        //Key: LMELO/RelatorioGenerico.pdf
        Log.i("AWS", key)
        val getrequest = GetObjectRequest(BUCKET, key.toString())
        return s3Client?.getObject(getrequest)
    }

    fun GetObjectContent(key: String): S3ObjectInputStream? {
        return s3Client?.getObject(BUCKET, KEY)?.objectContent
    }

    fun GetObjectMetaData(key: String): ObjectMetadata {
        val obj = s3Client!!.getObjectMetadata(BUCKET, key)
        return obj
    }

    fun GetObjectTag(key: String): List<Tag> {
        if (s3Client != null) {
            var objTag = s3Client?.getObjectTagging(GetObjectTaggingRequest(BUCKET, key))

            if (objTag != null && objTag.tagSet != null)
                return objTag.tagSet

            return emptyList()
        }
        return emptyList()
    }

    fun GetObjectTag(key: String, tagName: String): Tag? {
        if (s3Client != null) {
            var objTag = s3Client?.getObjectTagging(GetObjectTaggingRequest(BUCKET, key))

            if (objTag != null && objTag.tagSet != null)
                return objTag.tagSet.firstOrNull { t -> t.key == tagName }

            return null
        }
        return null
    }

    fun SetObjectTag(key: String, tags: List<Tag>): Boolean {
        if (s3Client != null) {
            s3Client?.setObjectTagging(SetObjectTaggingRequest(BUCKET, key, ObjectTagging(tags)))
            return true
        }
        return false

    }

    fun getTransferUtility(context: Context): TransferUtility? {
        if (sTransferUtility == null) {
            sTransferUtility = TransferUtility.builder()
                    .defaultBucket(BUCKET)
                    .context(context)
                    .awsConfiguration(AWSMobileClient.getInstance().configuration)
                    .s3Client(this.s3Client).build()
        }

        return sTransferUtility
    }

    fun DownloadFile(key: String, context: Context): ByteArray? {
        try {
            val s3 = AmazonS3Client(credentials)  // anonymous credentials are possible if this isn't your bucket
            val `object` = s3.getObject(BUCKET, key)
            val byteArray = IOUtils.toByteArray(`object`.objectContent)

            return byteArray
        } catch (e: Exception) {
            Log.e("", "Erro ao baixar: ${e.message} ")
            return null
        }

    }

    fun DownloadFileTransferUtility(key: String, context: Context, tmpFile: File, transferListener: TransferListener) {
        val transferUtility = TransferUtility.builder()
                .context(context)
                .awsConfiguration(AWSMobileClient.getInstance().configuration)
                .defaultBucket(BUCKET)
                .s3Client(s3Client)
                .build()


        val downloadObserver = transferUtility.download(key, tmpFile)

        downloadObserver.setTransferListener(transferListener)
    }
}