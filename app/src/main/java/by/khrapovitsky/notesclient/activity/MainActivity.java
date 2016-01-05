package by.khrapovitsky.notesclient.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Iterator;
import java.util.Map;

import by.khrapovitsky.notesclient.Constants;
import by.khrapovitsky.notesclient.R;
import by.khrapovitsky.notesclient.adapter.NotesCursorAdapter;
import by.khrapovitsky.notesclient.helper.DatabaseHelper;
import by.khrapovitsky.notesclient.model.Archive;
import by.khrapovitsky.notesclient.service.ListArchivesService;

public class MainActivity extends AppCompatActivity{

    private int timeOfAlarm = 60000;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    DatabaseHelper databaseHelper = new DatabaseHelper(this);

    private ArchiveReceiver receiver;

    ListView notesListView = null;

    NotesCursorAdapter adapter = null;
    Cursor cursor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cursor = databaseHelper.getAllForAdapter();
        notesListView = (ListView) findViewById(R.id.listViewNotes);
        adapter = new NotesCursorAdapter(this,R.layout.item_note,cursor,0);
        notesListView.setAdapter(adapter);
        startListArchivesService(timeOfAlarm);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        registerReceiver();
        super.onResume();
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(Constants.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ArchiveReceiver();
        registerReceiver(receiver, filter);
    }

    private class ArchiveReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String status = intent.getStringExtra(ListArchivesService.RESPONSE_STATUS);
            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            Map<String,Archive> archiveHashMap = null;
            Iterator iterator = null;
            if(status.equals("1") || status.equals("2")){
                archiveHashMap = (Map<String, Archive>) intent.getSerializableExtra(ListArchivesService.RESPONSE);
                iterator = archiveHashMap.entrySet().iterator();
            }
            switch (status){
                case "-1":
                    Toast.makeText(getApplicationContext(), "Connection problems!", Toast.LENGTH_LONG).show();
                    break;
                case "0":
                    Toast.makeText(getApplicationContext(), "Nothing to update!", Toast.LENGTH_LONG).show();
                    break;
                case "1":
                    while (iterator.hasNext()) {
                        Map.Entry entry = (Map.Entry)iterator.next();
                        Archive archive = (Archive) entry.getValue();
                        String nameArchive = entry.getKey().toString();
                        databaseHelper.createNote(archive,nameArchive);
                        iterator.remove();
                    }
                    cursor = databaseHelper.getAllForAdapter();
                    adapter.changeCursor(cursor);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "New data was received!", Toast.LENGTH_LONG).show();
                    break;
                case "2":
                    while (iterator.hasNext()) {
                        Map.Entry entry = (Map.Entry)iterator.next();
                        Archive archive = (Archive) entry.getValue();
                        String nameArchive = entry.getKey().toString();
                        databaseHelper.createNote(archive,nameArchive);
                        iterator.remove();
                    }
                    cursor = databaseHelper.getAllForAdapter();
                    adapter.changeCursor(cursor);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "Data received but not completely!", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }

    }

    public void startListArchivesService(int alarmTime){
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ListArchivesService.class);
        pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, timeOfAlarm, pendingIntent);
        Toast.makeText(getApplicationContext(), "Let's start service!", Toast.LENGTH_LONG).show();
    }

}
