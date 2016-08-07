package moblab.hotspottethering;


import java.util.ArrayList;

/*
 * Created by charles on 07/08/16.
 */
public interface FinishScanListener {

    public void onFinishScan(ArrayList<ClientScanResult> clients);

}