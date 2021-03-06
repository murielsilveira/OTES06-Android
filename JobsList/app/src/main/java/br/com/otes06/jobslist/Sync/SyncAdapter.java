package br.com.otes06.jobslist.Sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import br.com.otes06.jobslist.GatewayRealm.TarefasGatewayRealm;
import br.com.otes06.jobslist.Structs.TarefaStruct;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String TAG = "SyncAdapter";

    ContentResolver contentResolver;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        Log.i(TAG, "Constructor");
        contentResolver = context.getContentResolver();
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        Log.i(TAG, "Constructor");
        contentResolver = context.getContentResolver();
    }


    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "onPerformSync");
//        Realm realm = Realm.getInstance(getContext());
//        realm.beginTransaction();
//        realm.clear(TarefaRealm.class);
//        realm.commitTransaction();
//        realm.close();

        try {
            TarefasGatewayRealm tarefasGateway = new TarefasGatewayRealm(getContext());
            Boolean repeat = true;
            String url = "http://otes02.herokuapp.com/api-rest/tarefas/";
            while (repeat) {

                String response = HttpRequest.get(url).body();
                JSONObject json = new JSONObject(response);
                JSONArray results = json.optJSONArray("results");
                String next = json.getString("next");
                Log.i(TAG, "next:    " + next);
                if (next != null && !next.isEmpty() && !next.equals("null")) {
                    url = next;
                } else {
                    repeat = false;
                    url = null;
                }

                if (results == null) {
                    return;
                }

                Log.i(TAG, "GET response: " + response);
                Log.i(TAG, "GET json: " + results.toString());
                Log.i(TAG, "GET json len: " + results.length());

                int len = results.length();
                for (int i = 0; i < len; i++) {
                    JSONObject jsonRegistro = results.getJSONObject(i);
                    Log.i(TAG, "Obj id: " + jsonRegistro.getInt("id"));
                    Log.i(TAG, "Obj titulo: " + jsonRegistro.getString("titulo"));
                    Log.i(TAG, "Obj descricao: " + jsonRegistro.getString("descricao"));
                    Log.i(TAG, "Obj concluida: " + jsonRegistro.getBoolean("concluida"));

                    TarefaStruct tarefaStruct = new TarefaStruct();
                    tarefaStruct.setId(jsonRegistro.getInt("id"));
                    tarefaStruct.setTitulo(jsonRegistro.getString("titulo"));
                    tarefaStruct.setDescricao(jsonRegistro.getString("concluida"));
                    int id = tarefasGateway.salvar(tarefaStruct);
                }
            }

        } catch (JSONException e) {
            Log.e(TAG, "Erro ao decodificar JSON: " + e.getMessage());
        }

//        int size = ((int) realm.where(TarefaRealm.class).count());
//        int nextID = (int) (realm.where(TarefaRealm.class).maximumInt("id") + 1);
    }
}
