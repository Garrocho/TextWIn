package moblab.exemplolista;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by charles on 22/08/16.
 */
public class ConexaoCliente {

    private ObjectOutputStream enviaDados;
    private ObjectInputStream recebeDados;

    private Socket conexao;
    private int PORTA = 5555;
    private String ENDERECO = "";

    public ConexaoCliente(String endereco, int porta) {
        super();
        this.ENDERECO = endereco;
        this.PORTA = porta;
    }

    public Boolean conectaServidor() {

        try {
            InetAddress endereco = InetAddress.getByName(ENDERECO);
            conexao = new Socket(endereco, PORTA);
            enviaDados = new ObjectOutputStream(conexao.getOutputStream());
            recebeDados = new ObjectInputStream(conexao.getInputStream());
            return true;
        }catch (Exception e) {
            Log.d("TaskEnviarMSGServer", e.getMessage());
            return false;
        }
    }

    public void desconectaServidor() throws IOException {

        recebeDados.close();
        enviaDados.close();
        conexao.close();
    }

    public void enviaMensagem(String mensagem) {

        try {
            enviaDados.writeObject(mensagem);
            enviaDados.flush();
        }
        catch(Exception ex) {
            Log.d("ERRO", ex.getMessage());
        }
    }

    public ObjectOutputStream getEnviaDados() {
        return enviaDados;
    }

    public ObjectInputStream getRecebeDados() {
        return recebeDados;
    }

    public Socket getConexao() {
        return conexao;
    }

    public int getPORTA() {
        return PORTA;
    }

    public String getENDERECO() {
        return ENDERECO;
    }
}