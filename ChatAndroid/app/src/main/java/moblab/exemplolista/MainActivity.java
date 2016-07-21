package moblab.exemplolista;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    ListView listaView; // Este e a lista de itens do layout.
    List<ItemListView> listaItensView; // Essa e a lista de itens que contem as mensagens.
    AdapterListView adaptador; // Essa e o adaptador da lista do layout.
    String nome = "SemNome"; // Essa variavel contem o nome do usuario.


    // Esse metodo adiciona uma nova mensagem. Para isso ele pega o texto e envia
    // ao webservice solicitando uma adicao de nova mensagem.
    public void adicionarItem(View botao) {

        if (nome.isEmpty()) {
            Toast.makeText(MainActivity.this, "Você deve definir um nome antes de enviar mensagens!", Toast.LENGTH_SHORT).show();
            obterNome();
        }
        else {
            EditText campoTexto = (EditText) findViewById(R.id.editText);
            String textoItem = campoTexto.getText().toString();

            ItemListView itemLista = new ItemListView();
            itemLista.setTexto(textoItem);

            String mensagem = textoItem;
            campoTexto.setText("");
            ((EditText) findViewById(R.id.editText)).setHint("Escreva sua mensagem aqui...");

            // Solicita ao webservice a adicao de uma nova mensagem.
            Fuel.get("http://192.168.0.103:5000/adicionar?nome=" + nome + "&mensagem=" + mensagem).responseString(new Handler<String>() {
                @Override
                public void failure(Request request, Response response, FuelError error) {
                    Log.d("RESULTADO NO", response.toString());
                }

                @Override
                public void success(Request request, Response response, String data) {
                    Log.d("RESULTADO YES", response.toString());
                }
            });
        }
    }

    // Metodo inicial da aplicacao. Tudo comeca aqui. Configuracao do layout inicial e
    // chamada do metodo para configurar o nome, e configuracao da lista de mensagens.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        obterNome();

        this.listaView = (ListView)findViewById(R.id.lista_itens);

        this.listaItensView = new ArrayList<ItemListView>();

        this.adaptador = new AdapterListView(this, this.listaItensView);

        this.listaView.setAdapter(this.adaptador);

        this.listaView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                String nome = ((TextView) view.findViewById(R.id.item_nome)).getText().toString();
                String mensagem = ((TextView) view.findViewById(R.id.item_texto)).getText().toString();

                if (!nome.equalsIgnoreCase(nome)) {
                    Toast.makeText(MainActivity.this, "Você não pode apagar as mensagens de outras pessoas.", Toast.LENGTH_SHORT).show();
                }
                else {

                    // Solicita ao webservice apagar uma mensagem.
                    Fuel.get("http://192.168.0.103:5000/deletar?nome=" + nome + "&mensagem=" + mensagem).responseString(new Handler<String>() {
                        @Override
                        public void failure(Request request, Response response, FuelError error) {
                            Log.d("RESULTADO NO", response.toString());
                        }

                        @Override
                        public void success(Request request, Response response, String data) {
                            Log.d("RESULTADO YES", response.toString());
                        }
                    });
                }
                return true;

            }
        });

        ((EditText) findViewById(R.id.editText)).setHint("Escreva sua mensagem aqui...");

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
        }
        else {
            nome = retrievedString;
            Toast.makeText(MainActivity.this, "Bem Vindo ao Chat " + nome , Toast.LENGTH_SHORT).show();
        }
    }

    // Essa AsyncTask e uma Thread que roda em background na aplicacao. O loop dela e infinito
    // e fica atualizando lista de mensagens solicitando o webservice.
    class obterMensagensTask extends AsyncTask<String, String, List> {

        public boolean parar = false;
        public boolean entrou = false;

        @Override
        protected List<ItemListView> doInBackground(String... params) {
            parar = false;
            while (!parar) {

                Fuel.get("http://192.168.0.103:5000/mensagens").responseString(new Handler<String>() {
                    @Override
                    public void failure(Request request, Response response, FuelError error) {
                        Log.d("RESULTADO NO", response.toString());
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
                        boolean entrou = false;
                        try {
                            jsonarray = new JSONArray(data);
                            for (int i = 0; i < jsonarray.length(); i++) {
                                JSONObject jsonobject = jsonarray.getJSONObject(i);
                                String nome = jsonobject.getString("nome");
                                String mensagem = jsonobject.getString("mensagem");
                                ItemListView novoItem = new ItemListView(nome, mensagem);
                                listaItensView.add(novoItem);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Atualiza a listView
                        adaptador = new AdapterListView(MainActivity.this, listaItensView);
                        listaView.setAdapter(adaptador);


                        if (!entrou) {
                            entrou = true;
                            listaView.setSelectionFromTop(listaItensView.size(), top);
                        }
                        else {
                            /* Volta a visualizacao da lista para o itemView que ele estava visualizando antes de atualizar. */
                            listaView.setSelectionFromTop(index, top);
                        }
                    }
                });

                // O sleep e para deixar o loop mais devagar. A AsyncTask fica dois segundos sem fazer nada.
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}
