package moblab.listas;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<ItemListView> listaItensView;
    AdapterListView adaptador;
    ListView lista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Criando um Vetor de ItemListView e um adaptador para controlar ele.
        listaItensView = new ArrayList<ItemListView>();
        adaptador = new AdapterListView(this, listaItensView);

        // Criando uma referencia da lista de itens da interface e setando o adaptador nela.
        // O adaptador constroe automaticamente os itens (Vetor de ItemListView) na lista.
        lista = (ListView)findViewById(R.id.lista_itens);
        lista.setAdapter(adaptador);

        lista.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                Log.d("MOBLAB - ID do Item", String.valueOf(i));
                listaItensView.remove(i);
                adaptador = new AdapterListView(MainActivity.this, listaItensView);
                lista.setAdapter(adaptador);
                adaptador.notifyDataSetChanged();
                return true;
            }
        });
    }

    public void adicionarItem(View botao) {

        // Criando a referencia do campo de texto e Buscando o texto no Campo de Texto da Interface.
        EditText campoTextoAdd = (EditText)findViewById(R.id.campo_texto_adicionar);
        String texto = campoTextoAdd.getText().toString();

        // Adiciona o novo texto na lista.
        listaItensView.add(new ItemListView(texto));

        // Cria um novo adaptador para lista modificada.
        adaptador = new AdapterListView(this, listaItensView);

        // Seta novamente o adaptador na lista da interface.
        lista.setAdapter(adaptador);

        /* Como a lista ja tinha um adaptador antes, deve ser executado este metodo para notificar que
        novos dados foram adicionados.
        */
        adaptador.notifyDataSetChanged();
    }
}
