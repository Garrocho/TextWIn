package moblab.exemplolista;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by charles on 22/08/16.
 */
public class TaskEnviarMSGServer extends AsyncTask<String, String, String> {

    private int porta = 5555;
    private String IP = "";
    private String mensagem = "";
    public ConexaoCliente conexao = null;

    public TaskEnviarMSGServer(int porta, String IP, String mensagem) {
        this.porta = porta;
        this.IP = IP;
        this.mensagem = mensagem;
        Log.d("TaskEnviarMSGServer", this.IP + " - " + this.porta + " - " + this.mensagem);
    }

    @Override
    protected String doInBackground(String... argumento) {

        conexao = new ConexaoCliente(this.IP, this.porta);

        if (conexao.conectaServidor()) {
            Log.d("TaskEnviarMSGServer", "CONECTADO AO SERVER");
            try {
                conexao.getEnviaDados().writeObject(this.mensagem);
                conexao.getEnviaDados().flush();
                Log.d("TaskEnviarMSGServer", "enviado mensagem");
                conexao.desconectaServidor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
