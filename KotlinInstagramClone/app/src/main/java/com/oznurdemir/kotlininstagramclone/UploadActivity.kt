package com.oznurdemir.kotlininstagramclone

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.oznurdemir.kotlininstagramclone.databinding.ActivityUploadBinding
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.UUID


class UploadActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionResultLauncher: ActivityResultLauncher<String>
    var selectedPicture: Uri? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage:FirebaseStorage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        registerLauncher()

        auth = FirebaseAuth.getInstance()
        firestore = Firebase.firestore
        storage = Firebase.storage

    }

    fun selected(view: View){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)){
                Snackbar.make(view,"Needed for galeery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission"){
                    //request permission
                    permissionResultLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }.show()
            }else{
                //request permission
                permissionResultLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }else{
            val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            //start activity for result
            activityResultLauncher.launch(intentToGallery)
        }

    }

    fun upload(view: View){
        val uuid = UUID.randomUUID() // Her kaydedilen resme unique isim veriyor.
        val imageName = "$uuid.jpg"
        val reference = storage.reference // Storage deposunu göster.
        val selectedReferance = reference.child("images").child(imageName) // Depoda images adlı klasör aç image.jpg'yi içne koy.
        if(selectedPicture != null){
            selectedReferance.putFile(selectedPicture!!).addOnSuccessListener {
                // download url -> firestore
                val downloadPictureReferance = storage.reference.child("images").child(imageName)
                downloadPictureReferance.downloadUrl.addOnSuccessListener {
                    val downloadUrl = it.toString() // it indirilen kaydedilmiş resmin uri'ı

                    // Veritabanına yazma/kaydetme
                    if(auth.currentUser != null){
                        val postMap = hashMapOf<String, Any>()
                        postMap.put("image",downloadUrl) //indirdiğimiz resmin url'ı
                        postMap.put("user",auth.currentUser!!.email!!) //kullanıcının email adresi
                        postMap.put("comment",binding.commentText.text.toString()) // resim için yorumu
                        postMap.put("date",Timestamp.now()) // resmin yğklendiği tarih

                        firestore.collection("Posts").add(postMap).addOnSuccessListener {
                            finish()
                        }.addOnFailureListener {
                            Toast.makeText(this@UploadActivity,it.localizedMessage,Toast.LENGTH_LONG)
                        }
                    }
                }

            }.addOnFailureListener {
                Toast.makeText(this@UploadActivity,it.localizedMessage,Toast.LENGTH_LONG).show()
            } // Kullanıcı resim seçmişse dosyaya koy.
        }


    }

    fun registerLauncher(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            if(result.resultCode == RESULT_OK){ // Kullanıcı resim seçti mi seçmedi mi?
                val intentForResult = result.data
                if(intentForResult != null){
                    selectedPicture = intentForResult.data // seçilen resmin uri'ı
                    selectedPicture?.let {
                        binding.imageView.setImageURI(it)
                    }
                }
            }
        }

        permissionResultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // permission granted
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            } else {
                // permission denied
                Toast.makeText(this@UploadActivity, "Permission Needed!", Toast.LENGTH_LONG).show()
            }
        }

    }
}