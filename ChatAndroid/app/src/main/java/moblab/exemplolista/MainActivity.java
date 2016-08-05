package moblab.exemplolista;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Handler;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    public ListView listaView; // Este e a lista de itens do layout.
    List<ItemListView> listaItensView; // Essa e a lista de itens que contem as mensagens.
    AdapterListView adaptador; // Essa e o adaptador da lista do layout.
    String nome = "SemNome"; // Essa variavel contem o nome do usuario.
    String IP = "http://192.168.0.103:5000";


    public void enviarMensagem(final String nome, final String mensagem) {


        Fuel.get(IP + "/adicionar?nome=" + nome + "&mensagem=" + mensagem).responseString(new Handler<String>() {

            // SEM CONEXAO COM INTERNET, VERIFICA CONEXÃO COM WIFI LOCAL
            @Override
            public void failure(Request request, Response response, FuelError error) {
                // Nesse caso teremos que verificar se existe conexão WIFI e abrir multicast.
                try {
                    InetAddress addr = InetAddress.getByName("228.5.6.7");
                    DatagramSocket serverSocket = new DatagramSocket();

                    String msg = nome + "656789" + mensagem;

                    DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(),

                            msg.getBytes().length, addr, 6789);

                    for (int i = 0; i < 10; i++) {
                        serverSocket.send(msgPacket);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    serverSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(MainActivity.this, "Mensagem Enviada pelo MultiCast!", Toast.LENGTH_SHORT).show();
            }

            // CONEXAO COM INTERNET OK
            @Override
            public void success(Request request, Response response, String data) {
                Toast.makeText(MainActivity.this, "Mensagem Enviada pela Internet!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // Esse metodo adiciona uma nova mensagem. Para isso ele pega o texto e envia
    // ao webservice solicitando uma adicao de nova mensagem.
    public void adicionarItem(View botao) {

        if (nome.isEmpty()) {
            Toast.makeText(MainActivity.this, "Você deve definir um nome antes de enviar mensagens!", Toast.LENGTH_SHORT).show();
            obterNome();
        } else {
            EditText campoTexto = (EditText) findViewById(R.id.editText);
            String textoItem = campoTexto.getText().toString();

            ItemListView itemLista = new ItemListView();
            itemLista.setTexto(textoItem);

            String mensagem = textoItem;
            campoTexto.setText("");
            ((EditText) findViewById(R.id.editText)).setHint("Mensagem");

            enviarMensagem(nome, mensagem);
        }
    }

    // Metodo inicial da aplicacao. Tudo comeca aqui. Configuracao do layout inicial e
    // chamada do metodo para configurar o nome, e configuracao da lista de mensagens.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        obterNome();

        this.listaView = (ListView) findViewById(R.id.lista_itens);

        this.listaItensView = new ArrayList<ItemListView>();

        this.adaptador = new AdapterListView(this, this.listaItensView);

        this.listaView.setAdapter(this.adaptador);

        ((EditText) findViewById(R.id.editText)).setHint("Mensagem");

        // Inicia a Thread que ficara atualizando a lista.
        new obterMensagensTask().execute("");

    }

    // Cria um dialogo para o usuario setar no nome dele na aplicacao.
    public void obterNome() {

        SharedPreferences sharedPref = getSharedPreferences("TextWIn", Context.MODE_PRIVATE);
        String retrievedString = sharedPref.getString("nome", "SemNome");

        if (retrievedString.equalsIgnoreCase("SemNome")) {

            final EditText txtUrl = new EditText(this);

            new AlertDialog.Builder(this)
                    .setTitle("Defina seu nome de usuário")
                    .setView(txtUrl)
                    .setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            nome = txtUrl.getText().toString();
                            SharedPreferences sharedPref = getSharedPreferences("TextWIn", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("nome", nome);
                            editor.commit();
                        }
                    })
                    .show();
        } else {
            nome = retrievedString;
            Toast.makeText(MainActivity.this, "Bem Vindo ao Chat " + nome, Toast.LENGTH_SHORT).show();
        }
    }

    // Essa AsyncTask e uma Thread que roda em background na aplicacao. O loop dela e infinito
    // e fica atualizando lista de mensagens solicitando o webservice.
    class obterMensagensTask extends AsyncTask<String, String, List> {

        public boolean continuar = true;
        public List<String> msgsMC = new ArrayList<>();

        @Override
        protected List<ItemListView> doInBackground(String... params) {

            while (continuar) {

                // Obter mensagens pela internet através do FUEL.
                Fuel.get(IP + "/mensagens").responseString(new Handler<String>() {
                    @Override
                    public void failure(Request request, Response response, FuelError error) {
                        try {
                            InetAddress addr = InetAddress.getByName("228.5.6.7");

                            int index = listaView.getFirstVisiblePosition();
                            View v = listaView.getChildAt(0);
                            int top = (v == null) ? 0 : v.getTop();


                            MulticastSocket clientSocket = new MulticastSocket(6789);
                            clientSocket.joinGroup(addr);

                            while (true) {
                                byte[] buf = new byte[1000];
                                DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                                clientSocket.receive(msgPacket);


                                byte data[] = msgPacket.getData();
                                int i;
                                for (i = 0; i < data.length; i++) {
                                    if (data[i] == '\0')
                                        break;
                                }

                                String msg;

                                try {
                                    msg = new String(data, 0, i, "UTF-8");
                                } catch (UnsupportedEncodingException e) {
                                    Log.d("erro", "UTF-8 encoding is not supported. Can't receive the incoming message.");
                                    e.printStackTrace();
                                    continue;
                                }

                                Log.d("RECEBEU", msg);
                                String[] dados = msg.split("656789");

                                String msgMCNew = dados[0] + dados[1];

                                if (!msgsMC.contains(msgMCNew)) {
                                    msgsMC.add(msgMCNew);

                                    ItemListView novoItem = new ItemListView(dados[0], dados[1]);
                                    listaItensView.add(novoItem);

                                    // Atualiza a listView
                                    adaptador = new AdapterListView(MainActivity.this, listaItensView);
                                    listaView.setAdapter(adaptador);
                                    listaView.setSelectionFromTop(listaItensView.size(), top);

                                    /* Volta a visualizacao da lista para o itemView que ele estava visualizando antes de atualizar. */
                                    listaView.setSelectionFromTop(index, top);
                                }
                                Thread.sleep(500);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void success(Request request, Response response, String data) {

                        /* Obtem a posicao do itemView que o usuario esta vendo na tela atualmente. */
                        int index = listaView.getFirstVisiblePosition();
                        View v = listaView.getChildAt(0);
                        int top = (v == null) ? 0 : v.getTop();

                        // Cria uma nova lista e adiciona todos os itens baixados que estao no data para o listaItensView.
                        listaItensView = new ArrayList<ItemListView>();
                        JSONArray jsonarray = null;
                        try {
                            jsonarray = new JSONArray(data);
                            for (int i = 0; i < jsonarray.length(); i++) {
                                JSONObject jsonobject = jsonarray.getJSONObject(i);
                                String nome = jsonobject.getString("nome");
                                String mensagem = jsonobject.getString("mensagem");

                                String msg = nome + mensagem;

                                if (!msgsMC.contains(msg)) {
                                    msgsMC.add(msg);

                                    ItemListView novoItem = new ItemListView(nome, mensagem);
                                    listaItensView.add(novoItem);

                                    // Atualiza a listView
                                    adaptador = new AdapterListView(MainActivity.this, listaItensView);
                                    listaView.setAdapter(adaptador);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        /* Volta a visualizacao da lista para o itemView que ele estava visualizando antes de atualizar. */
                        listaView.setSelectionFromTop(index, top);
                    }
                });

                // O sleep e para deixar o loop mais devagar. A AsyncTask fica um segundos sem fazer nada.
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}
