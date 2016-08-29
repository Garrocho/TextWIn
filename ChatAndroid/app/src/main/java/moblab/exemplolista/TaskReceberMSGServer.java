package moblab.exemplolista;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by charles on 22/08/16.
 */
public class TaskReceberMSGServer extends AsyncTask<String, String, String> {

    public int portaServidor = 5555;
    protected ServerSocket soqueteServidor  = null;
    protected boolean      euServidor        = true;
    private ObjectOutputStream enviaDados;
    private ObjectInputStream recebeDados;
    private MainActivity app = null;

    public TaskReceberMSGServer(MainActivity contexto) {
        this.app = contexto;
        Log.d("TaskReceberMSGServer", "inciando SERVER");
    }

    @Override
    protected String doInBackground(String... strings) {

        Log.d("TaskReceberMSGServer", "ENTREI AQUI 0");

        while (this.euServidor) {

            Log.d("TaskReceberMSGServer", "ENTREI AQUI 1");

            if (soqueteServidor == null) {
                if (abrirSoqueteServidor()) {
                    Log.d("TaskReceberMSGServer", "SOCKET SERVER ABERTO");
                }
                else {
                    Log.d("TaskReceberMSGServer", "SOCKET SERVER NÃO ABERTO");
                    this.euServidor = false;
                }

            }

            else {
                Socket soqueteCliente = null;
                try {
                    Log.d("TaskReceberMSGServer", "AGUARDANDO CLIENTE");
                    soqueteCliente = this.soqueteServidor.accept();
                    Log.d("TaskReceberMSGServer", "CLIENTE CONECTADO");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (soqueteCliente != null) {

                    try {
                        enviaDados = new ObjectOutputStream(soqueteCliente.getOutputStream());
                        recebeDados = new ObjectInputStream(soqueteCliente.getInputStream());

                        String msg = (String)recebeDados.readObject();

                        Log.d("TaskReceberMSGServer ", msg);
                        String[] dados = msg.split("656789");

                        String msgMCNew = dados[0] + dados[1];
                        Log.d("TaskReceberMSGServer ", "FALHA 4");

                        if (!MainActivity.msgsMC.contains(msgMCNew)) {
                            MainActivity.msgsMC.add(msgMCNew);
                            app.atualizaLista(dados[0], dados[1]);
                        }
                        enviaDados.close();
                        recebeDados.close();
                        soqueteCliente.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }

        return null;
    }

    private boolean abrirSoqueteServidor() {
        try {
            this.soqueteServidor = new ServerSocket(this.portaServidor);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
