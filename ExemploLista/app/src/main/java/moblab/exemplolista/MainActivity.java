package moblab.exemplolista;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
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

    ListView listaView;
    List<ItemListView> listaItensView;
    AdapterListView adaptador;
    String nome = "SemNome";


    public void adicionarItem(View botao) {
        EditText campoTexto = (EditText)findViewById(R.id.editText);
        String textoItem = campoTexto.getText().toString();

        ItemListView itemLista = new ItemListView();
        itemLista.setTexto(textoItem);

        String mensagem = textoItem;
        campoTexto.setText("");

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        obterNome();

        this.listaView = (ListView)findViewById(R.id.lista_itens);

        this.listaItensView = new ArrayList<ItemListView>();
        listaItensView.add(new ItemListView("Buscando Mensagens..."));

        this.adaptador = new AdapterListView(this, this.listaItensView);

        this.listaView.setAdapter(this.adaptador);
        new obterMensagensTask().execute("");

    }

    public void obterNome() {
        final EditText txtUrl = new EditText(this);

        new AlertDialog.Builder(this)
                .setTitle("Defina seu nome de usuário")
                .setView(txtUrl)
                .setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        nome = txtUrl.getText().toString();
                    }
                })
                .show();
    }

    class obterMensagensTask extends AsyncTask<String, String, List> {

        public boolean parar = false;

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
                        listaItensView = new ArrayList<ItemListView>();
                        JSONArray jsonarray = null;
                        boolean entrou = false;
                        try {
                            jsonarray = new JSONArray(data);
                            for (int i = 0; i < jsonarray.length(); i++) {
                                JSONObject jsonobject = jsonarray.getJSONObject(i);
                                String nome = jsonobject.getString("nome");
                                String mensagem = jsonobject.getString("mensagem");
                                ItemListView novoItem = new ItemListView(nome + ": " + mensagem);
                                listaItensView.add(novoItem);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        adaptador = new AdapterListView(MainActivity.this, listaItensView);
                        listaView.setAdapter(adaptador);
                    }
                });

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
