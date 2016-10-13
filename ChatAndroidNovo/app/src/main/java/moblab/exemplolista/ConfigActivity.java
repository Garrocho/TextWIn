package moblab.exemplolista;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ConfigActivity extends AppCompatActivity {
    public String novo_IP="";
    public String novo_name="";
    private MainActivity app = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config);
        EditText campoNome = (EditText) findViewById(R.id.nome_novo);
        EditText campoIP = (EditText) findViewById(R.id.internetprotocol);

        SharedPreferences sharedPref = getSharedPreferences("TextWIn", Context.MODE_PRIVATE);
        String ip = sharedPref.getString("ip", "http://192.168.0.103:80");
        campoIP.setText(ip.substring(ip.lastIndexOf("/")+1));
        campoNome.setText(sharedPref.getString("nome", "SemNome"));
    }

    public void config (View view) {
        EditText campoTexto = (EditText) findViewById(R.id.internetprotocol);
        int dados = 0;
        if (campoTexto.getText().toString() != "") {
            novo_IP = "http://" + (campoTexto.getText().toString());

            SharedPreferences sharedPref = getSharedPreferences("TextWIn", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("ip", novo_IP);
            editor.commit();

        }
        EditText novo_nome = (EditText) findViewById(R.id.nome_novo);
        if (novo_nome.getText().toString() != "") {
            novo_name= novo_nome.getText().toString();
            SharedPreferences sharedPref = getSharedPreferences("TextWIn", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("nome", novo_name);
            editor.commit();
        }
        Intent resultIntent = new Intent();
        setResult(10, resultIntent);
        finish();
    }







}

