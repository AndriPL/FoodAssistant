package com.slightlynonprofessionalprogrammer.foodassistant.ui.addProduct

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.widget.Toast
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.slightlynonprofessionalprogrammer.foodassistant.R
import com.slightlynonprofessionalprogrammer.foodassistant.data.Product
import com.slightlynonprofessionalprogrammer.foodassistant.ui.main.MainActivity
import kotlinx.android.synthetic.main.add_product.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.drawable.BitmapDrawable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.internal.api.FirebaseNoSignedInUserException
import com.google.firebase.storage.FirebaseStorage
import com.slightlynonprofessionalprogrammer.foodassistant.data.DateParser
import com.squareup.picasso.Picasso
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.net.URL
import java.text.DateFormat
import java.time.LocalDate

class AddProductActivity: AppCompatActivity() {
    private val TAKE_PICTURE = 1000
    private val CAMERA_PERMISSION_CODE = 111
    private val STORAGE_PERMISSION_CODE = 222
    private var currentPath: String? = null
    private lateinit var uid: String
    private var imageUri: Uri? = null
    private var imageURL: String = ""
    private var expiryDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.title = "New product"
        setContentView(R.layout.add_product)
        uid = intent.getStringExtra("userID")

        checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE)
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE)

        add_button_add_prod.setOnClickListener {
            if(product_name_add_prod.text.toString() != "") {
                returnProduct()
            } else {
                product_name_add_prod.setError("Required")
            }
        }

        add_image_button_add_product.setOnClickListener {
            if(checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE) && checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE)) {
                dispatchCameraIntent()
            } else {
                Toast.makeText(this, "Permission is necessary in order to take photo", Toast.LENGTH_SHORT).show();
            }
        }

        callendar_add_prod.setOnDateChangeListener { view, year, month, dayOfMonth ->
            expiryDate = "$dayOfMonth-${month+1}-$year" //TODO - WTF? dlaczego to zaniża miesiąc i tylko miesiąc o 1?
            Log.d(TAG, "$expiryDate")
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun createProduct(): Product{
        var productName = ""

        if(product_name_add_prod.text.toString() != "") {
            productName = product_name_add_prod.text.toString()
        } else {
            Log.e(TAG, "In fun createProduct() creating product object FAILED. Product name is empty.")
            throw IllegalArgumentException("Product name cannot be empty.")
        }
        val product = Product(UUID.randomUUID().toString(), uid, productName, imageURL, expiryDate)
        return product
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == TAKE_PICTURE && resultCode == Activity.RESULT_OK) {
            try{
                val imageFile = File(currentPath)
                imageUri = Uri.fromFile(imageFile)
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                val bitmapDrawable = BitmapDrawable(resources, bitmap)
                add_image_button_add_product.setBackgroundDrawable(bitmapDrawable)
                add_image_button_add_product.text = ""
            } catch (e: IOException){
                Log.e(TAG, "Creating picture FAILED in fun onActivityResult", e)
            }
        }
    }

    private fun dispatchCameraIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(intent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null
            try{
                photoFile = createImage()
            } catch(e: IOException) {
                Log.e(TAG, "Creating image FAILED", e)
            }
            if(photoFile != null) {
                var photoUri = FileProvider.getUriForFile(this, "com.slightlynonprofessionalprogrammer.foodassistant.fileprovider", photoFile)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, TAKE_PICTURE)
            }
        }
    }

    private fun createImage(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageName = "JPEG" + timestamp
        var storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        var image = File.createTempFile(imageName, ".jpg", storageDir)
        currentPath = image.absolutePath
        return image
    }

    @Throws(NullPointerException::class, Exception::class)
    private fun returnProduct() {
        if(imageUri != null) {
            val imageName = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/productImages/$imageName")
            ref.putFile(imageUri!!)
                .addOnSuccessListener {
                    Log.d(TAG, "Uploading image to Firebase Firestore SUCCESSFULL. Image name: $imageName")
                    ref.downloadUrl
                        .addOnSuccessListener { url ->
                            imageURL = url.toString()
                            Log.d(TAG, "Downloading product URL SUCCESSFUL. Product URL: $imageURL")
                            val product = createProduct()
                            Log.d(TAG, "Product: ${product.productName}")
                            intent.putExtra("product", product)
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Downloading product URL FAILED.", e)
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Uploading image to Firebase Firestore FAILED.", e)
                }
        } else {

            val product = createProduct()
            Log.d(TAG, "Product: ${product.productName}")
            intent.putExtra("product", product)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    companion object {
        private const val TAG = "Add Product Activity"
    }

    private fun checkPermission(permission: String, permissionCode: Int): Boolean {
        if (this.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), permissionCode)
        }else{
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray){
        when(requestCode){
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission to Camera GRANTED", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission to Camera DENIED", Toast.LENGTH_SHORT).show();
                }
            }
            STORAGE_PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission to Write into External Storage GRANTED", Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(this, "Permission to Write into External Storage DENIED", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //For API <23
//    private fun checkPermission(permission: String, permissionCode: Int): Boolean {
//        if (ContextCompat.checkSelfPermission(
//                this,
//                permission
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
//
//                           ActivityCompat.requestPermissions(
//                                this,
//                                arrayOf(permission),
//                                permissionCode)
//            } else {
//                return true
//            }
//        }
//        return false
//    }

}