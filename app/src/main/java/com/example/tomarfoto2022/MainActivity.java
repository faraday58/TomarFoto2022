package com.example.tomarfoto2022;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {

    private static final int REQUERIR_CODIGO_PERMISO = 14;
    private static final int CODIGO_FOTO = 20;
    private static final int IMAGEN_SELECCIONADA =25 ;
    private AppCompatButton btnAgregarImagen;
    private AppCompatImageView imgvProducto;
    private StorageReference mStorageRef;
    private String urlImagen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStorageRef= FirebaseStorage.getInstance("gs://tiendaapp-3a33b.appspot.com").getReference();
        btnAgregarImagen= findViewById(R.id.btnAgregarImagen);
        imgvProducto= findViewById(R.id.imgvProducto);
        btnAgregarImagen.setOnClickListener(onClickAgregaImagen);
    }

    View.OnClickListener onClickAgregaImagen =View ->{

        //Crear cuadrod de dialogo
        final CharSequence[] opcion= {"Cámara","Elegir de la Galería","Cancelar" };

        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle("Elige una opción");
        builder.setItems(opcion,(dialogo,indice) ->{
            if(opcion[indice]=="Cámara")
            {
                AbrirCamara();
            }
            else if( opcion[indice]=="Elegir de la Galería"){
                AbrirGaleria();

            }
            else
            {
                dialogo.dismiss();
            }
        }
        );
        builder.show();
    };

    private void AbrirGaleria() {
        Intent intentGaleria= new Intent();
        intentGaleria.setType("image/*");
        intentGaleria.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intentGaleria,"Selecciona una imagen"),IMAGEN_SELECCIONADA);

    }

    private void AbrirCamara() {
        Log.i("Cámara","Entramos a la rutina para abrir la cámara");
        pedirPermisos();
        //Abre la cámara
        Intent intent_archivo= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Verificar si se tomó una fotografía
        if(intent_archivo.resolveActivity(getPackageManager()) != null  )
        {
            Log.i("Dialogo","La aplicación de camára está activa");
            //Accciones a realizar después de tomar la fotografía
            startActivityForResult(intent_archivo,CODIGO_FOTO);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( resultCode == RESULT_OK && data !=null  && data.getData() != null )
        {
            switch (requestCode)
            {
                case CODIGO_FOTO:
                    Bundle extras= data.getExtras();
                    Bitmap imagenBitmap = (Bitmap)extras.get("data");
                    imgvProducto.setImageBitmap(imagenBitmap);
                    break;
                case IMAGEN_SELECCIONADA:
                    Uri directorio= data.getData();
                    imgvProducto.setImageURI(directorio);
                    break;
            }

           Uri filepath= data.getData();
            final StorageReference filepathr= mStorageRef.child("productos")
                    .child(filepath.getLastPathSegment());
            filepathr.putFile(filepath).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw new Exception();
                    }
                    return filepathr.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(getApplicationContext(),"Se subió correctamente la imagen",
                                Toast.LENGTH_LONG).show();
                        Uri dowloadlink=task.getResult();
                        urlImagen= dowloadlink.toString();
                        Log.d("Link","Link de descarga de la imagen" +urlImagen);
                    }


                }
            });

        }
    }

    private void pedirPermisos() {
        int permisoCamara= ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
        if( permisoCamara != PackageManager.PERMISSION_GRANTED){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                //Cuadro de dialogo que indica al usuario que se requiere ocupar la cámara
                requestPermissions(new String[]{Manifest.permission.CAMERA},REQUERIR_CODIGO_PERMISO);
            }
        }

    }

}