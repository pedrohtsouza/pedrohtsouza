package com.example.mainactivity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.mainactivity.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private WifiReceiver wifiReceiver;
    private BatteryReceiver batteryReceiver;
    private BatteryReceiver batteryTimeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadBatterySection();

        Intent intent = new Intent(this, MyIntentService.class);
        intent.putExtra("TELA", "Tela 1");
        startService(intent);

        binding.btnIrTela2.setOnClickListener(view -> {
            Intent intent2 = new Intent(this, SecondActivity.class);
            startActivity(intent2);
        });

//        List<MyContact> contacts = ContactsHelper.getContacts(this);
//
//        for (MyContact contact : contacts) {
//            Log.d("ABR", "ID: " + contact.getId() + ", Name: " + contact.getName());
//        }
//
//        if (contacts.size() >= 1) {
//            contacts.get(0);
//        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        wifiReceiver = new WifiReceiver();
        IntentFilter filter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiReceiver, filter);

        batteryReceiver = new BatteryReceiver();
        IntentFilter filter1 = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter1);

        batteryTimeReceiver = new BatteryReceiver();
        IntentFilter filter2 = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryTimeReceiver, filter2);

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (wifiReceiver != null) {
            unregisterReceiver(wifiReceiver);
            wifiReceiver = null;
        }
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
            batteryReceiver = null;
        }
        if (batteryTimeReceiver != null) {
            unregisterReceiver(batteryTimeReceiver);
            batteryTimeReceiver = null;
        }
    }

    public class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            boolean isConnected = wifi != null && wifi.isConnectedOrConnecting();
            if (isConnected) {
                Log.d("PHTS", "WIFI ATIVADO");
                binding.tvWifi.setText("WIFI ATIVADO");
            } else {
                Log.d("PHTS", "WIFI DESATIVADO");
                binding.tvWifi.setText("WIFI DESATIVADO");
            }
        }
    }

    public class BatteryReceiver extends BroadcastReceiver {

        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        public void onReceive(Context context, Intent intent) {


            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);

            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            if (level != -1 && scale != -1) {
                float batteryPct = (level / (float)scale) * 100f;
                binding.tvnivelBateria.setText(batteryPct + "%");
            }


            BatteryManager batManager = (BatteryManager) getApplicationContext().getSystemService(Context.BATTERY_SERVICE);
            long timeToCharge=0L;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                timeToCharge= batManager.computeChargeTimeRemaining();
            }

            if (timeToCharge != -1){
                timeToCharge = timeToCharge/60000;
                Log.d("PHTS", "Tempo restante de carregamento: " + Math.round(timeToCharge) + " Min");
                binding.tvTimeCharge.setText(timeToCharge + " Min");
            } else {
                binding.tvTimeCharge.setText(timeToCharge + " Min");
            }

        }
    }

    private void loadBatterySection() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);

        registerReceiver(batteryInfoReceiver, intentFilter);
    }
    private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBatteryData(intent);
        }
    };
    private void updateBatteryData(Intent intent) {
        // display battery health
        boolean present = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false);

        if (present) {
            int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);

            switch (health) {
                case BatteryManager.BATTERY_HEALTH_COLD:
                    binding.tvSaude.setText("Cold");
                    break;

                case BatteryManager.BATTERY_HEALTH_DEAD:
                    binding.tvSaude.setText("Dead");
                    break;

                case BatteryManager.BATTERY_HEALTH_GOOD:
                    binding.tvSaude.setText("Good");
                    break;

                case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                    binding.tvSaude.setText("Over Voltage");
                    break;

                case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                    binding.tvSaude.setText("Overheat");
                    break;

                case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                    binding.tvSaude.setText("unspecified_failure");
                    break;

                case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                    binding.tvSaude.setText("Unknown");
                    break;
            }


            // Display plugged status...
            int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);

            switch (plugged) {
                case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                    binding.tvCharging.setText("Wireless");
                    break;

                case BatteryManager.BATTERY_PLUGGED_USB:
                    binding.tvCharging.setText("USB");
                    break;

                case BatteryManager.BATTERY_PLUGGED_AC:
                    binding.tvCharging.setText("AC Charger");
                    break;

                default:
                    binding.tvCharging.setText("S/CARREGAR");
                    break;
            }


            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

            switch (status) {
                case BatteryManager.BATTERY_STATUS_CHARGING:
                    binding.tvStatus.setText("Carregando");
                    break;

                case BatteryManager.BATTERY_STATUS_DISCHARGING:
                    binding.tvStatus.setText("Descarregando");
                    break;

                case BatteryManager.BATTERY_STATUS_FULL:
                    binding.tvStatus.setText("Carregamento completo");
                    break;

                case BatteryManager.BATTERY_STATUS_UNKNOWN:
                    binding.tvStatus.setText("Unknown");
                    break;

                case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                default:
                    binding.tvStatus.setText("Sem carregar");
                    break;
            }

            if (intent.getExtras() != null) {
                String technology = intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);

                if (!"".equals(technology)) {
                    binding.technologyTv.setText("Technologia : " + technology);
                }
            }



        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

