package com.moblab.tethering;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.util.Log;

import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.Response;
import com.github.kittinunf.result.Result;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import kotlin.Triple;
import moblab.exemplolista.ItemListView;
import moblab.exemplolista.MainActivity;

/**
 * Created by charles on 14/08/16.
 */
// Essa AsyncTask e uma Thread que roda em background na aplicacao. O loop dela e infinito
// e fica atualizando lista de mensagens solicitando o webservice.
public class GerenciaRedeD2D extends AsyncTask<String, String, List> {

    public boolean continuar = true;
    public int tempo_cliente = 0;
    public WifiApManager TetheringManager = null;
    public WifiManager wifiManager = null;
    public int netID = -8888;
    public Context contexto = null;

    public GerenciaRedeD2D(Context contexto) {
        this.contexto = contexto;
    }

    @Override
    protected List<ItemListView> doInBackground(String... params) {

        // A AsyncTask fica sempre executando. Posteriormente, essa Thread terá que ser um Service.
        while (continuar) {

            // Verifica primeiro se o dispositivo tem acesso a internet.
            if (!MainActivity.INTERNET) {

                tempo_cliente = ((100 - (int) getBateria()) + 5) * 1000;
                long startTime = System.currentTimeMillis();


                // Começa a Buscar as redes TextWin de acordo com o tempo gerado pelo nivel de bateria.
                while ((System.currentTimeMillis() - startTime) < tempo_cliente) {

                    if (wifiManager == null)
                        wifiManager = (WifiManager) contexto.getSystemService(Context.WIFI_SERVICE);

                    if (!wifiManager.isWifiEnabled())
                        wifiManager.setWifiEnabled(true);

                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    String connected_net = (wifiInfo.getSSID()).toString();

                    if (connected_net != null) {
                        if (connected_net.equalsIgnoreCase("<unknown ssid>")) {
                            wifiManager.startScan();

                            List<ScanResult> apList = wifiManager.getScanResults();

                            WifiConfiguration tmpConfig = null;

                            // Procura uma rede TextWIn dentro das redes wifi escaneadas.
                            for (ScanResult result : apList) {
                                if (result.SSID.contains("TextWIn")) {
                                    tmpConfig = new WifiConfiguration();
                                    tmpConfig.BSSID = result.BSSID;
                                    tmpConfig.SSID = "\"" + result.SSID + "\"";
                                    tmpConfig.priority = 1;
                                    tmpConfig.preSharedKey = "\"" + "123456789" + "\"";
                                    tmpConfig.status = WifiConfiguration.Status.ENABLED;
                                    tmpConfig.hiddenSSID = true;
                                    tmpConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                                    tmpConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                                    tmpConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                                }
                            }

                            // Se encontrar uma rede TextWIn, se conecta a ela.
                            if (tmpConfig != null) {
                                netID = wifiManager.addNetwork(tmpConfig);
                                wifiManager.enableNetwork(netID, true);
                            }
                        }
                    }

                    // Caso o dispositivo esteja conectado a uma rede wifi ou ao Tethering, continua como cliente.
                    else {
                        startTime = System.currentTimeMillis();
                    }
                }

                if (netID != -8888) {
                    wifiManager.removeNetwork(netID);
                    netID = 0;
                }

                // Estourou o tempo de busca de redes, neste caso o tethering é ativado.
                startTime = System.currentTimeMillis();

                while ((System.currentTimeMillis() - startTime) < tempo_cliente) {

                    if (TetheringManager == null)
                        TetheringManager = new WifiApManager(contexto);

                    if (!TetheringManager.isWifiApEnabled())
                        TetheringManager.setWifiApEnabled(null, true);

                    // Busca os clientes conectados ao Tethering.
                    if (TetheringManager.isWifiApEnabled()) {
                        ArrayList<ClientScanResult> clientsAP = getClientsTethering();

                        // Se houver clientes conectados, continua como Tethering.
                        if (clientsAP != null || !clientsAP.isEmpty()) {
                            startTime = System.currentTimeMillis();
                        }
                    }

                }
            }
            else {
                try {
                    Thread.sleep(2000);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    public ArrayList<ClientScanResult> getClientsTethering() {
        ArrayList<ClientScanResult> result = new ArrayList<ClientScanResult>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");

                if ((splitted != null) && (splitted.length >= 4)) {
                    String mac = splitted[3];
                    if (mac.matches("..:..:..:..:..:..")) {
                        boolean isReachable = InetAddress.getByName(splitted[0]).isReachable(300);

                        if (isReachable) {
                            result.add(new ClientScanResult(splitted[0], splitted[3], splitted[5], isReachable));
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(this.getClass().toString(), e.toString());
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                Log.e(this.getClass().toString(), e.getMessage());
            }
        }
        return result;
    }

    public float getBateria() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = contexto.registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return (level / (float) scale) * 100;
    }
}
