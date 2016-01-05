package by.khrapovitsky.notesclient;

import android.os.Environment;

public class Constants {
    public final static String URL_LIST = "http://192.168.43.102:8080/serverarchives/archives/currentday/";
    public final static String URL_DOWNLOAD = "http://192.168.43.102:8080/serverarchives/download/file/";
    public final static String PATH_DIRECTORY = Environment.getExternalStorageDirectory() + "/NotesArchives/";
    public static final String PROCESS_RESPONSE = "by.khrapovitsky.notesclient.service.intent.action.PROCESS_RESPONSE";
}
