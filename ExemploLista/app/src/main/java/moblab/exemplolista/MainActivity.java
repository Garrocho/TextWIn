package moblab.exemplolista;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

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


    public void adicionarItem(View botao) {
        EditText campoTexto = (EditText)findViewById(R.id.editText);
        String textoItem = campoTexto.getText().toString();

        ItemListView itemLista = new ItemListView();
        itemLista.setTexto(textoItem);

        listaItensView.add(itemLista);
        adaptador = new AdapterListView(this, listaItensView);
        listaView.setAdapter(adaptador);
        adaptador.notifyDataSetChanged();

        String nome = "Charles";
        String mensagem = textoItem;

        Fuel.get("http://192.168.10.106:5000/adicionar?nome=" + nome + "&mensagem=" + mensagem).responseString(new Handler<String>() {
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

        this.listaView = (ListView)findViewById(R.id.lista_itens);


        this.listaItensView = new ArrayList<ItemListView>();


        Fuel.get("http://192.168.10.106:5000/mensagens").responseString(new Handler<String>() {
            @Override
            public void failure(Request request, Response response, FuelError error) {
                Log.d("RESULTADO NO", response.toString());
            }

            @Override
            public void success(Request request, Response response, String data) {
                Log.d("RESULTADO YES", data);
                JSONArray jsonarray = null;
                try {
                    jsonarray = new JSONArray(data);
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                        String nome = jsonobject.getString("nome");
                        String mensagem = jsonobject.getString("mensagem");
                        listaItensView.add(new ItemListView(nome + ": " + mensagem));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        this.adaptador = new AdapterListView(this, this.listaItensView);

        this.listaView.setAdapter(this.adaptador);
    }
}
