package com.iglesias.c.appgym.Activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.iglesias.c.appgym.Pojo.DeviceInfo;
import com.iglesias.c.appgym.Presenter.LoginPresenterImpl;
import com.iglesias.c.appgym.R;
import com.iglesias.c.appgym.RestApi.ConstantesRestApi;
import com.iglesias.c.appgym.RestApi.Model.InfoLogin;
import com.iglesias.c.appgym.RestApi.Model.ResultLogin;
import com.iglesias.c.appgym.Service.Bluetooth;
import com.iglesias.c.appgym.View.LoginView;

import java.io.File;

public class LoginActivity extends AppCompatActivity implements LoginView {


    private final String TAG = getClass().getName();
    public static final String EXTRA_NOMBRE = "NOMBRE";
    public static final String EXTRA_DOCUMENTO = "DOCUMENTO";
    public static final String EXTRA_DIAS = "DIAS";
    public static final String EXTRA_ID_HUELLA = "ID_HUELLA";
    public static final String EXTRA_FLAG_SIN_HUELLA = "FLAG_HUELLA";
    public static final String EXTRA_DEVICE_MAC = "DEVICE_MAC";
    public static final String EXTRA_URL_IMAGEN = "URL";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int MY_PERMISSIONS_REQUEST = 2;
    public static final String PASS_ADMIN = "admin123";
    private int estadoConexionBt = Bluetooth.STATE_NONE;
    //public static final String PASS_ADMIN = "";

    Button btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, btn0, btnx, btnIr;
    EditText edtNro;
    private ProgressDialog loading;
    LoginPresenterImpl presenter;
    private TextView txtEstado, txtSucursal;

    Bluetooth bt;
    BluetoothAdapter btAdapter;
    private boolean flagEnvioPeticionSucursal = false;
    private String idSucursal = "";

    private DeviceInfo deviceInfo = new DeviceInfo("", "");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupViews();
        setupLoading();

        if(isExternalStorageWritable()){
            String rutaDirectorioAlmExternoPublico = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + "subdirectorio-gym-publico-pictures";
            File directorioAlmExternoPublico = new File(rutaDirectorioAlmExternoPublico);

            Toast.makeText(this, "esta disponible la memoria", Toast.LENGTH_SHORT).show();
            if (directorioAlmExternoPublico.exists() && directorioAlmExternoPublico.isDirectory()) {
                Toast.makeText(this, "La carpeta ya fue creada", Toast.LENGTH_SHORT).show();
            }
            else {//Se ejecuta si no existe el directorio
                String nombreDirectorioPublico = "subdirectorio-gym-publico-pictures";
                crearDirectorioPublico(nombreDirectorioPublico);
            }

        }else{
            Toast.makeText(this, "no esta disponible la memoria", Toast.LENGTH_SHORT).show();
        }

        presenter = new LoginPresenterImpl(this);

        deviceInfo = presenter.getDeviceInfo();

         setupBt();
    }
    //disponibilidad memoria externa
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
    //creación del directorio de almacenamiento
    public File crearDirectorioPublico(String nombreDirectorio) {
        //Crear directorio público en la carpeta Pictures.
        File directorio = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), nombreDirectorio);
        //Muestro un mensaje en el logcat si no se creo la carpeta por algun motivo
        if (!directorio.mkdirs())
            Toast.makeText(this, "no se creo el directorio", Toast.LENGTH_SHORT).show();

        return directorio;
    }

    private void setupBt() {
        bt = Bluetooth.getInstance(this, mHandler);
        btAdapter = bt.getBtAdapter();
        if (!btAdapter.isEnabled()) {
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);
        } else {
            if (!deviceInfo.getMac().contains(":")) {
                showErrorLoginDialog("No se encuentra ningun dispositivo configurado.");
            } else {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkPermission();
                } else {
                    conectService();
                    conectDevice();
                }
            }
        }

    }

    private void conectService() {
        bt.start();
    }

    private void conectDevice() {
        deviceInfo = presenter.getDeviceInfo();
        bt.connectDevice(deviceInfo.getMac());
    }

    private void setupLoading() {
        loading = new ProgressDialog(this);
        loading.setCancelable(false);
        loading.setMessage(getResources().getString(R.string.str_loading_login));
    }

    void setupViews() {
        btn0 = findViewById(R.id.numero_0);
        btn1 = findViewById(R.id.numero_1);
        btn2 = findViewById(R.id.numero_2);
        btn3 = findViewById(R.id.numero_3);
        btn4 = findViewById(R.id.numero_4);
        btn5 = findViewById(R.id.numero_5);
        btn6 = findViewById(R.id.numero_6);
        btn7 = findViewById(R.id.numero_7);
        btn8 = findViewById(R.id.numero_8);
        btn9 = findViewById(R.id.numero_9);

        btnx = findViewById(R.id.borrar);
        btnIr = findViewById(R.id.sign_in);

        txtEstado = findViewById(R.id.id_txt_estado_login);
        txtSucursal = findViewById(R.id.id_txt_sucursal_login);

        txtEstado.setText("Estado: Desconectado");
        txtSucursal.setText("Sucursal: " + idSucursal);

        edtNro = findViewById(R.id.cedula);
    }

    public void pressNumber(View v) {
        String nro = edtNro.getText().toString();
        if (nro.length() <= 10) {
            edtNro.setText(nro + ((Button) v).getText().toString().trim());
        }
    }

    public void deleteChar(View v) {
        String nro = edtNro.getText().toString();
        if (nro.length() > 0) {
            edtNro.setText(nro.substring(0, nro.length() - 1));
        }
    }

    public void onClickValidateUser(View v) {
        //  bt.sendMessage("3,72,123,255,255,186,255,224,239,128,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,62,106,13,204,55,229,28,139,25,162,75,71,95,201,55,69,103,221,41,75,35,225,77,160,49,161,87,68,84,69,82,135,12,33,106,94,83,10,97,7,14,97,103,201,20,76,28,139,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,49,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,43,96,0,94,0,1,0,3,0,0,0,0,0,0,0,0,0,3,20,49,10,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,44,44,44,44,44,44,44,44,44,44,44,44,44,52,52,44,52,52,44,52,52,44,52,52,44,52,52,44,52,52,44,52,233,0,0,50,0,232,0,0,0,2,41,8,0,0,15,64,39,66,0,255,255,0,0,3,1,255,255,0,0,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,0,0,255,255,4,4,0,0,111,203,175,61,117,19,43,255,91,117,236,248,186,223,91,142,223,225,95,182,121,203,239,189,247,92,95,14,92,234,127,183,31,60,210,191,53,247,166,143,235,142,119,140,44,239,255,99,205,217,254,91,199,197,11,248,205,185,86,94,108,165,83,237,95,250,59,123,105,93,230,151,186,175,190,239,222,87,94,255,179,178,91,219,}");

        if (idSucursal.isEmpty()) { // TODO se niega para pruebas
            showErrorLoginDialog("No se encuentra vinculado a una sucursal");
        } else {
            String nro = edtNro.getText().toString();
            edtNro.setText("");
            presenter.validateUser(nro);
        }
    }

    @Override
    public void showLoading() {
        if (!loading.isShowing()) loading.show();
    }

    @Override
    public void hideLoading() {
        if (loading.isShowing()) loading.dismiss();
    }

    @Override
    public void goToMainActivity(ResultLogin resultLogin) {
        InfoLogin infoLogin = resultLogin.getInfo();

        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        i.putExtra(EXTRA_NOMBRE, infoLogin.getNombre());
        i.putExtra(EXTRA_DIAS, infoLogin.getDias());
        i.putExtra(EXTRA_DOCUMENTO, infoLogin.getNroDocumento());
        i.putExtra(EXTRA_URL_IMAGEN, infoLogin.getUrlImage());
        i.putExtra(EXTRA_ID_HUELLA, infoLogin.getIdHuella());
        i.putExtra(EXTRA_DEVICE_MAC,deviceInfo.getMac());
        i.putExtra(EXTRA_FLAG_SIN_HUELLA, resultLogin.getErrorCode() == ConstantesRestApi.CODE_ERROR_SIN_HUELLA);

        if (infoLogin.getDias() < 7) {
            Toast toast = Toast.makeText(this, "Recuerda que tu plan esta proximo a vencer,aprovecha las ofertas especiales y consultalos con tu asesor", Toast.LENGTH_LONG);
            ((TextView) ((ViewGroup) toast.getView()).getChildAt(0)).setTextSize(20);
            toast.show();
        }

        startActivity(i);

    }

    @Override
    public void showErrorLoginDialog(String msj) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this, R.style.myDialog);

        builder.setTitle(getResources().getString(R.string.str_title_alert_error_login));
        builder.setMessage(msj);
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }


    private void showDialogAdmin() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this, R.style.myDialog);
        final LayoutInflater inflater = getLayoutInflater();

        final View view = inflater.inflate(R.layout.dialog_pass, null);
        builder.setView(view);
        builder.setTitle(getResources().getString(R.string.str_title_alert_pass));
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText edtPassAdmin = view.findViewById(R.id.id_edt_pass_admin);

                if (edtPassAdmin.getText().toString().equals(PASS_ADMIN)) {
                    Intent i = new Intent(LoginActivity.this, ConfiguracionActivity.class);
                    startActivity(i);

                } else {
                    showErrorLoginDialog("Contraseña inválida.");
                }
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    protected void onResume() {
        super.onResume();

        bt = Bluetooth.getInstance(this, mHandler);

    }

    @Override
    protected void onPause() {
        super.onPause();
        //  bt.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //bt.stop();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.id_menu_registrar:
                Intent i = new Intent(this, RegistraActivity.class);
                startActivity(i);
                break;
            case R.id.id_menu_actualizar:
                Intent iActualizar = new Intent(this, CambioHuellaActivity.class);
                startActivity(iActualizar);
                break;
            case R.id.id_menu_sincronizar:
                break;
            case R.id.id_menu_configuracion:
                showDialogAdmin();
                break;

        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_ENABLE_BT) {
            if (btAdapter.isEnabled()) {
                Toast.makeText(this, "Bluetooth encendido", Toast.LENGTH_SHORT).show();
                if (!deviceInfo.getMac().contains(":")) {
                    showErrorLoginDialog("No se encuentra ningun dispositivo configurado.");
                } else {
                    conectService();
                    conectDevice();
                }
            } else {
                Toast.makeText(this, "Se requiere encender el bluetooth", Toast.LENGTH_SHORT).show();

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {

                if (grantResults.length > 0
                        && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    conectService();
                    conectDevice();

                } else {

                    showErrorLoginDialog("Es necesario habilitar los permisos para el funcionamiento del aplicativo.");
                }
                break;

            }

        }

    }

    public void checkPermission() {

        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED)) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST);


        } else {
            conectService();
            conectDevice();

        }
    }


    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Bluetooth.MESSAGE_STATE_CHANGE:
                    if (msg.arg1 == Bluetooth.STATE_CONNECTED) {
                        txtEstado.setText("Estado: Conectado.");
                        bt.sendMessage("p");
                        flagEnvioPeticionSucursal = true;
                        Toast.makeText(getContext(), "Dispositivo conectado con éxito.", Toast.LENGTH_SHORT).show();
                    } else {

                        idSucursal = "";
                        txtEstado.setText("Estado: Conectando...");
                    }

                    Log.e(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    break;
                case Bluetooth.MESSAGE_WRITE:
                    Log.e(TAG, "MESSAGE_WRITE: " + String.valueOf(msg.arg1));

                    break;
                case Bluetooth.MESSAGE_READ:
                    Log.e(TAG, "MESSAGE_READ: " + msg.obj);
                    String msj = String.valueOf(msg.obj);
                    if (flagEnvioPeticionSucursal && msj.contains(":")) {
                        flagEnvioPeticionSucursal = false;
                        idSucursal = msj.split(":")[1];

                    }
                    break;
                case Bluetooth.MESSAGE_DEVICE_NAME:
                    Log.e(TAG, "MESSAGE_DEVICE_NAME " + msg);
                    break;
                case Bluetooth.MESSAGE_TOAST:
                    Log.e(TAG, "MESSAGE_TOAST " + msg.arg1);

                    break;
            }
            txtSucursal.setText("Sucursal: " + idSucursal);
        }
    };


}
